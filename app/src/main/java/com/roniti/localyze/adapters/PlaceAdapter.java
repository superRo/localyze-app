package com.roniti.localyze.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.roniti.localyze.R;
import com.roniti.localyze.api.model.Result;
import com.roniti.localyze.db.DatabaseHandler;
import com.roniti.localyze.helpers.UIConstants;
import com.roniti.localyze.ui.activities.MainActivity;
import com.roniti.localyze.ui.activities.PlaceDetails;
import com.roniti.localyze.helpers.UIConstants.*;
import com.roniti.localyze.helpers.Utils;

import java.util.ArrayList;


public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private ArrayList<Result> placeList;
    private Context mContext;
    String unitDisplay;

    // Used for database connection
    private DatabaseHandler handler;

    public PlaceAdapter(Context context, ArrayList<Result> placeList) {
        this.mContext = context;
        this.placeList = placeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceAdapter.ViewHolder holder, int position) {
        holder.bind(placeList.get(position));

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView listRestaurantName, listRestaurantAddress, listDistance;
        private ImageView listCategoryImage, faveHeart;

        public ViewHolder(View itemView) {
            super(itemView);

            listRestaurantName = itemView.findViewById(R.id.listRestaurantName);
            listRestaurantAddress = itemView.findViewById(R.id.listRestaurantAddress);
            listDistance = itemView.findViewById(R.id.listDistance);
            listCategoryImage = itemView.findViewById(R.id.listCategoryImage);
            faveHeart = itemView.findViewById(R.id.favoritesHeartSignRow);

            // Get preference of kilometers or miles
            unitDisplay = Utils.getMeasurementPreference(mContext);

            // Make individual items clickable
            itemView.setOnClickListener(this);

            handler = new DatabaseHandler(mContext);

        }

        public void bind(Result result){

            listRestaurantName.setText(result.getName());
            listRestaurantAddress.setText(result.getVicinity());
            listDistance.setText(distanceCalculator(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(), unitDisplay));

            //Place image based on place type
            String category = result.getTypes().get(0);
            listCategoryImage.setImageResource(Utils.attachCategoryImage(category, CategoryImageRequestTags.REQUEST_CAT));

            // Set favorite heart
            boolean isFave = handler.faveExists(result.getPlaceId());
            if(isFave) {
                faveHeart.setVisibility(View.VISIBLE);
            } else {
                faveHeart.setVisibility(View.INVISIBLE);

            }

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Result result = placeList.get(position);
            Intent intent = new Intent(mContext, PlaceDetails.class);
            intent.putExtra(PlaceDetails.GOOGLE_ID_KEY, result.getPlaceId());
            mContext.startActivity(intent);
        }

        // Calculate distance between current location and search results
        public String distanceCalculator(double latitudePlace, double longitudePlace, String unit){

            int test;
            double latitudeLocation = MainActivity.getCurrentLatitude();
            double longitudeLocation = MainActivity.getCurrentLongitude();

            //Calculates distance based on coordinates
            double earthRadius = 6371000; //meters
            double dLat = Math.toRadians(latitudePlace-latitudeLocation);
            double dLng = Math.toRadians(longitudePlace-longitudeLocation);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(Math.toRadians(latitudeLocation)) * Math.cos(Math.toRadians(latitudePlace)) *
                            Math.sin(dLng/2) * Math.sin(dLng/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            float dist = (float) (earthRadius * c)/1000;

            //Changes units to miles if user prefers miles
            if(unit.equals(PreferenceKeys.MEASUREMENT_UNIT_VALUE_MI)){
                dist *= 0.621371;
            }

            //Rounds to nearest hundredths
            dist = (float) ((int) ((dist * 100.0) + 0.5) / 100.0);

            if(unit.equals(PreferenceKeys.MEASUREMENT_UNIT_VALUE_KM) && dist < 1.0) {
                // If under 1 kilometer, show distance in meters
                dist *= 1000;
                return (int) dist + " m";
            } else if(dist > 99.9) {
                // If distance is over 100, removes decimals
                test = (int) dist;
                return test + " " + unit;
            } else {
                return dist + " " + unit;

            }
        }


    }
}
