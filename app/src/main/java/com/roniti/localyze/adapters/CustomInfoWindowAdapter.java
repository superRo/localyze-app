package com.roniti.localyze.adapters;

import android.app.Activity;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.roniti.localyze.R;
import com.roniti.localyze.api.model.Result;
import com.roniti.localyze.db.DatabaseHandler;
import com.roniti.localyze.helpers.UIConstants;
import com.roniti.localyze.helpers.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mContents;

    // Used for database connection
    private DatabaseHandler handler;

    public CustomInfoWindowAdapter(Activity context) {
        mContents = context.getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        handler = new DatabaseHandler(context);
    }

    // The API will first call getInfoWindow() and if null is returned, it will then call getInfoContents().
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker, mContents);
        return mContents;
    }

    // Attach data to InfoWindow layout
    private void render(Marker marker, View view) {

        Result place = (Result) marker.getTag();

        //Place image based on place type
        String type = place.getTypes().get(0);
        int badge = Utils.attachCategoryImage(type, UIConstants.CategoryImageRequestTags.REQUEST_CAT);
        ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

        // Set favorite heart
        boolean isFave = handler.faveExists(place.getPlaceId());
        if(isFave) {
            view.findViewById(R.id.favoritesHeartSignPopup).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.favoritesHeartSignPopup).setVisibility(View.INVISIBLE);
        }

        // Set title of custom info window
        String title = marker.getTitle();
        TextView titleUi = view.findViewById(R.id.title);
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }

        // Set address to snippet of custom info window
        String snippet = marker.getSnippet();
        TextView snippetUi = view.findViewById(R.id.snippet);
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }

    }

}
