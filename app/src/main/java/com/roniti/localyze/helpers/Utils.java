package com.roniti.localyze.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.roniti.localyze.R;
import com.roniti.localyze.api.service.GooglePlacesClient;
import com.roniti.localyze.ui.activities.MainActivity;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class Utils {

    /* ------------- UI methods ------------- */

    // Set category image
    public static int attachCategoryImage(String type, String request) {
        int badge, pinBadge;

        switch(type) {
            case UIConstants.PlaceCategories.CATEGORY_airport:
            case UIConstants.PlaceCategories.CATEGORY_travel_agency:
                badge = R.drawable.cat_airport;
                pinBadge = R.drawable.pin_airport;
                break;
            case UIConstants.PlaceCategories.CATEGORY_aquarium:
                badge = R.drawable.cat_aquarium;
                pinBadge = R.drawable.pin_aquarium;
                break;
            case UIConstants.PlaceCategories.CATEGORY_atm:
                badge = R.drawable.cat_atm;
                pinBadge = R.drawable.pin_atm;
                break;
            case UIConstants.PlaceCategories.CATEGORY_bank:
                badge = R.drawable.cat_bank;
                pinBadge = R.drawable.pin_bank;
                break;
            case UIConstants.PlaceCategories.CATEGORY_bar:
            case UIConstants.PlaceCategories.CATEGORY_liquor_store:
                badge = R.drawable.cat_bar;
                pinBadge = R.drawable.pin_bar;
                break;
            case UIConstants.PlaceCategories.CATEGORY_beauty_salon:
            case UIConstants.PlaceCategories.CATEGORY_hair_care:
            case UIConstants.PlaceCategories.CATEGORY_spa:
                badge = R.drawable.cat_beauty_salon;
                pinBadge = R.drawable.pin_beauty_salon;
                break;
            case UIConstants.PlaceCategories.CATEGORY_cafe:
                badge = R.drawable.cat_cafe;
                pinBadge = R.drawable.pin_cafe;
                break;
            case UIConstants.PlaceCategories.CATEGORY_car_dealer:
            case UIConstants.PlaceCategories.CATEGORY_car_rental:
            case UIConstants.PlaceCategories.CATEGORY_car_repair:
            case UIConstants.PlaceCategories.CATEGORY_car_wash:
                badge = R.drawable.cat_car;
                pinBadge = R.drawable.pin_car;
                break;
            case UIConstants.PlaceCategories.CATEGORY_church:
            case UIConstants.PlaceCategories.CATEGORY_mosque:
            case UIConstants.PlaceCategories.CATEGORY_place_of_worship:
            case UIConstants.PlaceCategories.CATEGORY_synagogue:
            case UIConstants.PlaceCategories.CATEGORY_hindu_temple:
            case UIConstants.PlaceCategories.CATEGORY_cemetery:
                badge = R.drawable.cat_church;
                pinBadge = R.drawable.pin_church;
                break;
            case UIConstants.PlaceCategories.CATEGORY_fire_station:
            case UIConstants.PlaceCategories.CATEGORY_police:
                badge = R.drawable.cat_fire_station;
                pinBadge = R.drawable.pin_fire_station;
                break;
            case UIConstants.PlaceCategories.CATEGORY_gas_station:
                badge = R.drawable.cat_gas_station;
                pinBadge = R.drawable.pin_gas_station;
                break;
            case UIConstants.PlaceCategories.CATEGORY_gym:
                badge = R.drawable.cat_gym;
                pinBadge = R.drawable.pin_gym;
                break;
            case UIConstants.PlaceCategories.CATEGORY_hospital:
            case UIConstants.PlaceCategories.CATEGORY_doctor:
            case UIConstants.PlaceCategories.CATEGORY_dentist:
            case UIConstants.PlaceCategories.CATEGORY_pharmacy:
            case UIConstants.PlaceCategories.CATEGORY_physiotherapist:
                badge = R.drawable.cat_hospital;
                pinBadge = R.drawable.pin_hospital;
                break;
            case UIConstants.PlaceCategories.CATEGORY_lodging:
            case UIConstants.PlaceCategories.CATEGORY_neighborhood:
            case UIConstants.PlaceCategories.CATEGORY_room:
            case UIConstants.PlaceCategories.CATEGORY_real_estate_agency:
                badge = R.drawable.cat_lodging;
                pinBadge = R.drawable.pin_lodging;
                break;
            case UIConstants.PlaceCategories.CATEGORY_movie_rental:
            case UIConstants.PlaceCategories.CATEGORY_movie_theater:
                badge = R.drawable.cat_movie;
                pinBadge = R.drawable.pin_movie;
                break;
            case UIConstants.PlaceCategories.CATEGORY_museum:
            case UIConstants.PlaceCategories.CATEGORY_courthouse:
            case UIConstants.PlaceCategories.CATEGORY_funeral_home:
            case UIConstants.PlaceCategories.CATEGORY_city_hall:
            case UIConstants.PlaceCategories.CATEGORY_local_government_office:
            case UIConstants.PlaceCategories.CATEGORY_embassy:
            case UIConstants.PlaceCategories.CATEGORY_art_gallery:
            case UIConstants.PlaceCategories.CATEGORY_stadium:
            case UIConstants.PlaceCategories.CATEGORY_locality:
            case UIConstants.PlaceCategories.CATEGORY_sublocality:
            case UIConstants.PlaceCategories.CATEGORY_post_office:
            case UIConstants.PlaceCategories.CATEGORY_country:
            case UIConstants.PlaceCategories.CATEGORY_establishment:
                badge = R.drawable.cat_museum;
                pinBadge = R.drawable.pin_museum;
                break;
            case UIConstants.PlaceCategories.CATEGORY_night_club:
            case UIConstants.PlaceCategories.CATEGORY_amusement_park:
            case UIConstants.PlaceCategories.CATEGORY_casino:
            case UIConstants.PlaceCategories.CATEGORY_bowling_alley:
                badge = R.drawable.cat_night_club;
                pinBadge = R.drawable.pin_night_club;
                break;
            case UIConstants.PlaceCategories.CATEGORY_park:
            case UIConstants.PlaceCategories.CATEGORY_campground:
            case UIConstants.PlaceCategories.CATEGORY_rv_park:
            case UIConstants.PlaceCategories.CATEGORY_natural_feature:
            case UIConstants.PlaceCategories.CATEGORY_florist:
                badge = R.drawable.cat_park;
                pinBadge = R.drawable.pin_park;
                break;
            case UIConstants.PlaceCategories.CATEGORY_parking:
                badge = R.drawable.cat_parking;
                pinBadge = R.drawable.pin_parking;
                break;
            case UIConstants.PlaceCategories.CATEGORY_school:
            case UIConstants.PlaceCategories.CATEGORY_book_store:
            case UIConstants.PlaceCategories.CATEGORY_library:
            case UIConstants.PlaceCategories.CATEGORY_university:
                badge = R.drawable.cat_school;
                pinBadge = R.drawable.pin_school;
                break;
            case UIConstants.PlaceCategories.CATEGORY_shopping_mall:
            case UIConstants.PlaceCategories.CATEGORY_shoe_store:
            case UIConstants.PlaceCategories.CATEGORY_home_goods_store:
            case UIConstants.PlaceCategories.CATEGORY_jewelry_store:
            case UIConstants.PlaceCategories.CATEGORY_clothing_store:
            case UIConstants.PlaceCategories.CATEGORY_convenience_store:
            case UIConstants.PlaceCategories.CATEGORY_department_store:
            case UIConstants.PlaceCategories.CATEGORY_electronics_store:
            case UIConstants.PlaceCategories.CATEGORY_store:
            case UIConstants.PlaceCategories.CATEGORY_bicycle_store:
            case UIConstants.PlaceCategories.CATEGORY_furniture_store:
            case UIConstants.PlaceCategories.CATEGORY_hardware_store:
                badge = R.drawable.cat_shopping_mall;
                pinBadge = R.drawable.pin_shopping_mall;
                break;
            case UIConstants.PlaceCategories.CATEGORY_restaurant:
            case UIConstants.PlaceCategories.CATEGORY_meal_delivery:
            case UIConstants.PlaceCategories.CATEGORY_meal_takeaway:
            case UIConstants.PlaceCategories.CATEGORY_bakery:
                badge = R.drawable.cat_restaurant;
                pinBadge = R.drawable.pin_restaurant;
                break;
            case UIConstants.PlaceCategories.CATEGORY_transit_station:
            case UIConstants.PlaceCategories.CATEGORY_train_station:
            case UIConstants.PlaceCategories.CATEGORY_taxi_stand:
            case UIConstants.PlaceCategories.CATEGORY_subway_station:
            case UIConstants.PlaceCategories.CATEGORY_bus_station:
                badge = R.drawable.cat_transit_station;
                pinBadge = R.drawable.pin_transit_station;
                break;
            case UIConstants.PlaceCategories.CATEGORY_zoo:
            case UIConstants.PlaceCategories.CATEGORY_pet_store:
            case UIConstants.PlaceCategories.CATEGORY_veterinary_care:
                badge = R.drawable.cat_zoo;
                pinBadge = R.drawable.pin_zoo;
                break;
            default:
                badge = R.drawable.cat_plumber;
                pinBadge = R.drawable.pin_plumber;
                break;
            // Passing badge = 0 will clear the image view.
        }

        if (request.equals(UIConstants.CategoryImageRequestTags.REQUEST_CAT)) {
            return badge;
        } else {
            return pinBadge;
        }


    }

    // Check if portrait orientation
    public static boolean isPortrait(Context mContext) {
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    // Check if device is tablet
    public static boolean isTablet(Context mContext) {
        try {
            // Compute screen size
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            float screenWidth  = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth, 2) +
                    Math.pow(screenHeight, 2));
            // Tablet devices have a screen size greater than 6 inches
            return size >= 7;
        } catch(Throwable t) {
            return false;
        }
    }

    // Progress Dialog when connecting to API
    public static void startLoadingSpinner(Activity activity, ProgressDialog progressDialog) {
        progressDialog.setMessage(activity.getString(R.string.findingResults)); // Setting Message
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.setCancelable(false);
        progressDialog.show(); // Display Progress Dialog
    }
    public static void endLoadingSpinner(ProgressDialog progressDialog) {
        progressDialog.dismiss();
    }

    // Warning toasts
    public static void toastNoInternetConnection(Context mContext) {
        Toast.makeText(mContext, R.string.internetRequiredNotice, Toast.LENGTH_SHORT).show();
    }
    public static void toastLocationRequiredNotice(Context mContext) {
        Toast.makeText(mContext, R.string.locationRequiredNotice, Toast.LENGTH_SHORT).show();
    }


    /* ------------- SharedPreferences methods ------------- */

    // Get SharedPreference on measurement
    public static String getMeasurementPreference(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences (mContext);
        return prefs.getString(UIConstants.PreferenceKeys.MEASUREMENT_UNIT, UIConstants.PreferenceKeys.MEASUREMENT_UNIT_VALUE_KM);
    }

    // Get SharedPreference radius
    public static int getRadiusPreference(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences (activity);
        int currentRadius = prefs.getInt(UIConstants.PreferenceKeys.RADIUS, 500);
        return currentRadius;
    }

    // Get SharedPreference color
    public static int getColorPreference(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String requestedValue = prefs.getString(UIConstants.PreferenceKeys.COLOR_CHOICE, Integer.toString(UIConstants.PreferenceKeys.COLOR_CHOICE_DEFAULT));
        return Color.parseColor(requestedValue);
    }


    /* ------------- Google API methods ------------- */

    // Start API call to GooglePlaces
    public static GooglePlacesClient startGooglePlacesAPICall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GooglePlacesClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GooglePlacesClient service = retrofit.create(GooglePlacesClient.class);

        return service;
    }


    /* ------------- Permissions methods ------------- */

    // Check if location permission was granted
    public static boolean locationPermissionIsGranted(Context context) {
        return checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

}
