package com.roniti.localyze.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.roniti.localyze.R;
import com.roniti.localyze.api.model_single.ExampleSingle;
import com.roniti.localyze.api.service.GooglePlacesClient;
import com.roniti.localyze.db.DatabaseHandler;
import com.roniti.localyze.eventbus.UpdateFavoritesEvent;
import com.roniti.localyze.helpers.Utils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceDetails extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String GOOGLE_ID_KEY = "googleIDKey";

    // Used for UI components
    private String phoneNumber, website, address, googleMapsURL, placeName, googlePlaceID;
    private TextView placeDetailsName, placeDetailsOpenClosed, placeDetailsRatingNumber, placeDetailsAddressTextView, placeDetailsCurrentOpenHours,
            placeDetailsAllOpeningHoursTextView, placeDetailsPhoneTextView, seeMorePicturesLabel;
    private RatingBar starRatings;
    private ImageView arrowToggle, mainImage;
    private ConstraintLayout callButtonLayout, directionsButtonLayout, websiteButtonLayout, shareButtonLayout;
    private DatabaseHandler handler;
    private FloatingActionButton fab;
    private EventBus bus;

    // Used for loadingSpinner
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        bus = EventBus.getDefault();

        //Progress Dialog
        progressDialog = new ProgressDialog(this);

        // Set-up all views and their onClick methods
        setupViews();
        setupClickListeners();

        //Access to database
        handler = new DatabaseHandler(this);

        // Grab the selected GooglePlaceID from main activity
        googlePlaceID = getIntent().getStringExtra(GOOGLE_ID_KEY);

        // Call API to get details about place
        GooglePlacesClient service = Utils.startGooglePlacesAPICall();
        getSearchResultsBasedOnID(service);

        // Top toolbar, scrolls up
        setupToolbar();

        // Favorites button
        favoriteButtonAction();

    }

    /* ------------ UI methods ------------ */

    private void setupViews() {
        placeDetailsName = (TextView) findViewById(R.id.placeDetailsName);
        placeDetailsOpenClosed = (TextView) findViewById(R.id.placeDetailsOpenClosed);
        placeDetailsRatingNumber = (TextView) findViewById(R.id.placeDetailsRatingNumber);
        placeDetailsAddressTextView = (TextView) findViewById(R.id.placeDetailsAddressTextView);
        placeDetailsCurrentOpenHours = (TextView) findViewById(R.id.placeDetailsCurrentOpenHours);
        placeDetailsAllOpeningHoursTextView = (TextView) findViewById(R.id.placeDetailsAllOpeningHoursTextView);
        placeDetailsPhoneTextView = (TextView) findViewById(R.id.placeDetailsPhoneTextView);
        starRatings = (RatingBar) findViewById(R.id.placeDetailsRatingBar);
        arrowToggle = (ImageView) findViewById(R.id.arrowToggle);
        mainImage = (ImageView) findViewById(R.id.placeDetailsMainImage);
        seeMorePicturesLabel = (TextView) findViewById(R.id.seeMorePicturesLabel);
        callButtonLayout = (ConstraintLayout) findViewById(R.id.callButtonLayout);
        directionsButtonLayout = (ConstraintLayout) findViewById(R.id.directionsButtonLayout);
        websiteButtonLayout = (ConstraintLayout) findViewById(R.id.websiteButtonLayout);
        shareButtonLayout = (ConstraintLayout) findViewById(R.id.shareButtonLayout);
    }

    private void favoriteButtonAction() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        faveButtonColor();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(handler.faveExists(googlePlaceID)) {
                    Snackbar.make(view, R.string.removedFavorite, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    handler.deleteFave(googlePlaceID);
                    isNotFave();
                    updateFavoritesEvent();
                } else {
                    handler.saveFave(googlePlaceID);
                    isFave();
                    Snackbar.make(view, R.string.addedFavorite, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    // Sets color of Favorites button on arrival to activity
    private void faveButtonColor() {
        if(handler.faveExists(googlePlaceID)){
            isFave();
        } else {
            isNotFave();
        }
    }

    // Calls all changes applied when place is a favorite
    private void isFave() {
        fab.setColorFilter(Color.WHITE);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
    }

    // Calls all changes applied when place is not a favorite
    private void isNotFave() {
        fab.setColorFilter(Color.GRAY);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
    }


    /* ------------ Utility methods ------------ */

    private void setupClickListeners() {

        // Call button
        callButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call_action();
            }
        });

        // Directions button
        directionsButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address_action();
            }
        });

        // Website button
        websiteButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                website_action();
            }
        });

        // Share button
        shareButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share_action();
            }
        });

        // Label to get more information opens Google Maps URL
        seeMorePicturesLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address_action();
            }
        });

        // Phone number TextView open Dial Pad
        placeDetailsPhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call_action();
            }
        });
    }

    public void toggleOpeningHours(View view) {
        if (placeDetailsAllOpeningHoursTextView.getVisibility() == View.GONE) {
            placeDetailsAllOpeningHoursTextView.setVisibility(View.VISIBLE);
            arrowToggle.setImageResource(R.drawable.ic_keyboard_arrow_up_24dp);
        } else {
            placeDetailsAllOpeningHoursTextView.setVisibility(View.GONE);
            arrowToggle.setImageResource(R.drawable.ic_keyboard_arrow_down_24dp);
        }
    }

    private void call_action() {
        if(phoneNumber != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.noPhoneNumberMessage, Toast.LENGTH_SHORT).show();
        }

    }

    private void address_action() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.setData(Uri.parse(googleMapsURL));
        startActivity(intent);
    }

    private void website_action() {
        if(website != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(website));
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.noWebsiteNotification, Toast.LENGTH_SHORT).show();
        }
    }

    private void share_action() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText) + placeName + "!\n" + address + "\n" + googleMapsURL);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject) + placeName);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }


    /* ------------ Toolbar methods ------------ */

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(placeName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Ensures back button doesn't reload prior activity & map
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


     /* ------------ Google Place API methods ------------ */

    private void getSearchResultsBasedOnID(GooglePlacesClient service) {

        Utils.startLoadingSpinner(this, progressDialog);

        final String GOOGLE_MAPS_KEY = getString(R.string.google_maps_key);

        Call<ExampleSingle> idCall = service.getById(googlePlaceID, GOOGLE_MAPS_KEY);

        idCall.enqueue(new Callback<ExampleSingle>() {
            @Override
            public void onResponse(Call<ExampleSingle> call, Response<ExampleSingle> response) {

                googleMapsURL = response.body().getResult().getUrl();

                // Add featured image
                if(response.body().getResult().getPhotos() != null) {
                    String photoReferenceID = response.body().getResult().getPhotos().get(0).getPhotoReference();
                    String imageURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" + photoReferenceID + "&key=" + GOOGLE_MAPS_KEY;
                    Picasso.with(getBaseContext()).load(imageURL).into(mainImage);
                }

                // Add name details
                placeName = response.body().getResult().getName();
                placeDetailsName.setText(placeName);
                getSupportActionBar().setTitle(placeName);

                // Add address details
                address = response.body().getResult().getVicinity();
                placeDetailsAddressTextView.setText(address);

                // Add phone number details
                if(response.body().getResult().getFormattedPhoneNumber() != null) {
                    phoneNumber = response.body().getResult().getFormattedPhoneNumber();}
                if(response.body().getResult().getInternationalPhoneNumber() != null) {
                    placeDetailsPhoneTextView.setText(response.body().getResult().getInternationalPhoneNumber());
                } else {
                    placeDetailsPhoneTextView.setText(R.string.unavailable);
                }

                // Add website details
                if(response.body().getResult().getWebsite() != null) {
                    website = response.body().getResult().getWebsite();
                }

                // Add ratings
                if(response.body().getResult().getRating() != null) {
                    placeDetailsRatingNumber.setText(response.body().getResult().getRating().toString());
                    starRatings.setRating(Float.parseFloat(response.body().getResult().getRating().toString()));
                }

                // Add label if open or closed
                if(response.body().getResult().getOpeningHours() != null) {
                    placeDetailsOpenClosed.setText(response.body().getResult().getOpeningHours().getOpenNow()? getString(R.string.openNow): getString(R.string.closedNow));
                }

                // Add opening hours for the day
                if(response.body().getResult().getOpeningHours() != null) {
                    List<String> listOfDays = response.body().getResult().getOpeningHours().getWeekdayText();

                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    String openingHours = "";

                    switch(day) {
                        case 1:
                            openingHours = listOfDays.get(6);
                            break;
                        case 2:
                            openingHours = listOfDays.get(0);
                            break;
                        case 3:
                            openingHours = listOfDays.get(1);
                            break;
                        case 4:
                            openingHours = listOfDays.get(2);
                            break;
                        case 5:
                            openingHours = listOfDays.get(3);
                            break;
                        case 6:
                            openingHours = listOfDays.get(4);
                            break;
                        case 7:
                            openingHours = listOfDays.get(5);
                            break;
                        default:
                            break;
                    }

                    openingHours = openingHours.substring(openingHours.indexOf(": ") + 1).trim();
                    placeDetailsCurrentOpenHours.setText(openingHours);

                    // Add weekly opening hours
                    String weekdayListedText = "";
                    for(String weekday : listOfDays) {
                        weekdayListedText += weekday + "\n";
                    }
                    placeDetailsAllOpeningHoursTextView.setText(weekdayListedText.trim());
                } else {
                    placeDetailsCurrentOpenHours.setText(R.string.unavailable);
                    arrowToggle.setVisibility(View.INVISIBLE);
                }

                Utils.endLoadingSpinner(progressDialog);
            }

            @Override
            public void onFailure(Call<ExampleSingle> call, Throwable t) {
                Utils.endLoadingSpinner(progressDialog);
            }
        });

    }


    /* ------------- Event methods ------------- */

    // Updates favorites in map/list
    private void updateFavoritesEvent() {
        bus.post(new UpdateFavoritesEvent("changeFave"));
    }
}