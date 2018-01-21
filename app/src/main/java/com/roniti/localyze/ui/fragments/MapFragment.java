package com.roniti.localyze.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roniti.localyze.R;
import com.roniti.localyze.adapters.CustomInfoWindowAdapter;
import com.roniti.localyze.api.model.Result;
import com.roniti.localyze.eventbus.FragmentDataEvent;
import com.roniti.localyze.eventbus.FragmentDataOnRotationEvent;
import com.roniti.localyze.helpers.Utils;
import com.roniti.localyze.ui.activities.PlaceDetails;
import com.roniti.localyze.helpers.UIConstants;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    // Used for Google Map and Places API
    private GoogleMap mMap;
    private MapView mMapView;
    private List<Marker> mMarkers = new ArrayList<>();

    // Used for Eventbus
    EventBus bus = EventBus.getDefault();

    // Used for orientation change
    private boolean orientationChanged;
    ArrayList<Result> listResults;
    private static final String RESULTS_TAG = "RESULTS";


    public MapFragment() {
        // Required empty public constructor
    }


    /* ------------- Lifecyle methods ------------- */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);

        listResults = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = view.findViewById(R.id.mapView);
        mMapView.getMapAsync(this);
        mMapView.onCreate(savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        this.mMapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    /* ------------- Map methods ------------- */

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        // Set customized info window adapter
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getActivity()));

        // Set a listener for info window events.
        mMap.setOnInfoWindowClickListener(this);

        // Zoom only using drag, no toolbar
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Remove directions toolbar from map
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        mMapView.onResume();

        // Enabling MyLocation layer of Google Map
        if(Utils.locationPermissionIsGranted(getActivity())){
            mMap.setMyLocationEnabled(true);
        }

        // On orientation change, display markers
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if(listResults.size() > 0 && orientationChanged == true) {
                    addMarkersToMap(listResults);
                    orientationChanged = false;
                }
            }
        });

    }


    /* ------------- Marker methods ------------- */

    // Add markers to map
    private void addMarkersToMap(ArrayList<Result> listResults) {

        // Bug fix: Don't add markers if map is not visible
        if(mMapView.getWidth() == 0) {
            return;
        }

        clearAllPlaceMarkersOnMap();

        int listResultsSize = listResults.size();

        for (int i = 0; i < listResultsSize; i++) {
            LatLng latLng = new LatLng(listResults.get(i).getGeometry().getLocation().getLat(), listResults.get(i).getGeometry().getLocation().getLng());
            String placeName = listResults.get(i).getName();
            String placeAddress = listResults.get(i).getVicinity();

            // Used to set icons for marker and infowindow
            BitmapDescriptor bitmapMarker = setMarkerIcon(listResults.get(i).getTypes().get(0));

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(placeName)
                    .snippet(placeAddress)
                    .icon(bitmapMarker));
            marker.setTag(listResults.get(i));
            mMarkers.add(marker);
        }

        // Pan to see all markers in map view.
        setMarkersView();

    }

    // Pan to see all markers in map view.
    private void setMarkersView() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = (int) pxFromDp(45); // offset from edges of the map in pixels

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//        mMap.moveCamera(cu);
        mMap.animateCamera(cu);

    }

    // Resize map icons
    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    // Convert px to dp for marker resizing
    private float pxFromDp(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    // Set marker icon
    private BitmapDescriptor setMarkerIcon(String type) {

        BitmapDescriptor bitmapMarker;
        Bitmap newSize;
        int badge = Utils.attachCategoryImage(type, UIConstants.CategoryImageRequestTags.REQUEST_PIN);
        int width = (int) pxFromDp(47);
        int height = (int) pxFromDp(61);
        newSize = resizeMapIcons(getResources().getResourceName(badge), width, height);
        bitmapMarker = BitmapDescriptorFactory.fromBitmap(newSize);

        return bitmapMarker;
    }

    // Clear all markers from map
    private void clearAllPlaceMarkersOnMap() {
        mMap.clear();
        mMarkers.clear();
    }

    // Send GoogleID through customInfoWindow
    @Override
    public void onInfoWindowClick(Marker marker) {
        Result tag = (Result) marker.getTag();
        Intent intent = new Intent(getActivity(), PlaceDetails.class);
        intent.putExtra(PlaceDetails.GOOGLE_ID_KEY, tag.getPlaceId());
        startActivity(intent);
    }


    /* ------------- Event methods ------------- */

    // Receive results from MainActivity
    @Subscribe
    public void onEvent(FragmentDataEvent event) {
            listResults = event.listResults;
            addMarkersToMap(event.listResults);
    }

    // Receive results from Main Activity on orientation changed
    @Subscribe
        public void onEvent(FragmentDataOnRotationEvent event) {
            listResults = event.listResults;
            orientationChanged = true;
    }


}