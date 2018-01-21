package com.roniti.localyze.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.roniti.localyze.receivers.NetworkReceiver;
import com.roniti.localyze.R;
import com.roniti.localyze.adapters.PagerAdapter;
import com.roniti.localyze.api.model.Example;
import com.roniti.localyze.api.model.Result;
import com.roniti.localyze.api.model_single.ExampleSingle;
import com.roniti.localyze.api.model_single.ResultSingle;
import com.roniti.localyze.api.service.GooglePlacesClient;
import com.roniti.localyze.db.DatabaseHandler;
import com.roniti.localyze.eventbus.CategoryDataEvent;
import com.roniti.localyze.eventbus.FragmentDataEvent;
import com.roniti.localyze.eventbus.FragmentDataOnRotationEvent;
import com.roniti.localyze.eventbus.NearbyDataEvent;
import com.roniti.localyze.eventbus.QueryDataEvent;
import com.roniti.localyze.eventbus.UpdateFavoritesEvent;
import com.roniti.localyze.eventbus.UpdateMeasurementEvent;
import com.roniti.localyze.eventbus.UpdateRadiusEvent;
import com.roniti.localyze.ui.dialogs.AboutDialog;
import com.roniti.localyze.ui.fragments.TopToolBar;
import com.roniti.localyze.helpers.UIConstants.Fragments;
import com.roniti.localyze.helpers.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TopToolBar.OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // Used for UI
    private DrawerLayout drawer;
    private TabLayout tabLayout;
    private FloatingActionButton fab;

    // Used for Eventbus
    private EventBus bus = EventBus.getDefault();
    public static boolean initialStartupOrientationIsLandscape;

    // Used for location API
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static boolean initialLaunch = true;
    private static double currentLatitude, currentLongitude;

    // Used for location permission
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final int REQUEST_PERMISSION_SETTING = 2;
    private static boolean askForPermissionOnLaunch = true;

    // Used for Google Places API
    private static final String ZERO_RESULTS = "ZERO_RESULTS";
    private static final String SEARCH_NEARBY = "searchNearby";
    private static final String SEARCH_FAVES = "searchFaves";
    private static final String SEARCH_QUERY = "searchQuery";
    private static final String SEARCH_CATEGORY = "searchCategory";
    private static String lastSearch, lastQuery;
    private static GooglePlacesClient service;
    public static ArrayList<Result> listResults;

    // Used for passing information on orientation change
    private static final String RESULTS_TAG = "RESULTS";

    // Used for loadingSpinner
    private ProgressDialog progressDialog;

    // Used for database connection
    private DatabaseHandler handler;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();


    /* ------------- Lifecycle Methods ------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bus.register(this);

        // Get results from prior search for orientation change
        if (savedInstanceState == null) {
            listResults = new ArrayList<>();
            // Bug fix: see ListHolderFragment onResume()
            if (!Utils.isPortrait(this)) {initialStartupOrientationIsLandscape = true;}
        } else {
            listResults = savedInstanceState.getParcelableArrayList(RESULTS_TAG);
        }

        // Registers BroadcastReceiver to track network connection changes.
        registerConnectivityBroadcastReceiver();

        // Initialize settings to default values on first app opening
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        // Set default radius if none is previously selected
        Utils.getRadiusPreference(this);

        // Drawer
        setupNavigationDrawer(toolbar);

        // API calls
        if (NetworkReceiver.internetIsConnected) {
            service = Utils.startGooglePlacesAPICall();
            buildGoogleAPIClient();
            locationRequest();
        }

        //Access to database
        handler = new DatabaseHandler(this);

        //Progress Dialog
        progressDialog = new ProgressDialog(this);

        // Set tabs & viewpager on portrait view
        if (Utils.isPortrait(this)) {
            setupTabLayout();
            setupViewPager();
        }

        // Set floatingActionButton - Opens/closes favorites
        setupFavoritesButton();

        // Disable automatic keyboard pop-up
        disableAutomaticKeyboard();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        disableAutomaticKeyboard();

        // Change colors of the UI based on preference selected
        changeColorsBasedOnPreference();

        // Prompt the user for permission on launch. After initial request, other permission methods will handle response
        if(askForPermissionOnLaunch) {
            showLocationPermission();
            askForPermissionOnLaunch = false;
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        // Send saved prior results to map/list tabs on orientation change
        if(listResults.size() > 0) {
            sendResultsToMapAndList(bus);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(RESULTS_TAG, listResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregisters BroadcastReceiver when app is destroyed.
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }

    }


    /* ------------- UI Methods ------------- */

    private void setupFavoritesButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks that internet is connected
                if (NetworkReceiver.internetIsConnected) {
                    getFavorites();
                } else {
                    Utils.toastNoInternetConnection(MainActivity.this);
                }
            }
        });

    }

    private void setupNavigationDrawer(Toolbar toolbar) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.mapLabel));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.listLabel));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    private void setupViewPager() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    //Disable automatic keyboard popup
    private void disableAutomaticKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    // Toolbar colors change based on selected preferences
    private void changeColorsBasedOnPreference() {

        //Call the SharedPreferences
        int parsedColor = Utils.getColorPreference(this);

        ConstraintLayout toolbarConstraintLayout = (ConstraintLayout) findViewById(R.id.toolbarConstraintLayout);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        toolbarConstraintLayout.setBackgroundColor(parsedColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(parsedColor));

        if(Utils.isPortrait(this)) {
            // Tabs in activity
            tabLayout.setTabTextColors(ColorStateList.valueOf(parsedColor));
            tabLayout.setSelectedTabIndicatorColor(parsedColor);
        }
    }


    /* ------------- Drawer Menu Methods ------------- */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id) {
            case R.id.nav_rate:
                rate_action();
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_about:
                AboutDialog dFragment = new AboutDialog();
                dFragment.show(getSupportFragmentManager(), Fragments.ABOUT_DIALOG_TAG);
                break;
            case R.id.nav_share:
                share_action();
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void share_action() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareAppTextPart1) + getString(R.string.app_name) + getString(R.string.exclamationMark));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + getString(R.string.shareAppTextPart2) + "\\n\\" + "https://play.google.com/store/apps/details?id=com.roniti.localyze");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }

    private void rate_action() {
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    @Override
    public void onFragmentInteraction() {
        drawer.openDrawer(GravityCompat.START);
    }


    /* ------------- Permission methods ------------- */

    public void showLocationPermission() {
        // Check if the location permission has been granted
        if (Utils.locationPermissionIsGranted(this)) {
            // Will automatically reach onConnect() and grab location
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display a SnackBar with a button to request the missing permission.
                Snackbar.make(findViewById(R.id.content_nav_drawer_constraint_layout), R.string.locationAccessRequiredWarning,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSION_REQUEST_LOCATION);
                        }
                    }
                }).show();

            } else {
                // Request the permission. The result will be received in onRequestPermissionResult().
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                // If onConnected() has already launched before permission is granted, this ensures location is updated
                if (mGoogleApiClient.isConnected()) {onConnectedGetLocation();}
            } else {
                // Permission has been denied
                Snackbar.make(findViewById(R.id.content_nav_drawer_constraint_layout), R.string.locationAccessRequiredWarning,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Checks if "Don't ask again" is selected. If so, open settings panel so permission is granted manually
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                // Request the permission
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_REQUEST_LOCATION);
                            } else {
                                // Open settings panel
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getBaseContext().getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            }
                        }
                    }
                }).show();
            }
        }
    }

    // Launches when returning from settings panel
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showLocationPermission();
    }


    /* ------------- Broadcast Receiver methods ------------- */

    private void registerConnectivityBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);
    }


    /* ------------- Localization methods ------------- */

    // Call Google Location API client
    private void buildGoogleAPIClient(){
        // Call Google Location API client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // Create the LocationRequest object
    private void locationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        onConnectedGetLocation();
    }

    private void onConnectedGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Get last location
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            // Last location not found
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);}
        else {
            // Last location exists
            handleLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLocation(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        if(initialLaunch) {
            getNearbyResults();
            initialLaunch = false;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        handleLocation(location);
    }

    public static double getCurrentLatitude() {
        return currentLatitude;
    }

    public static double getCurrentLongitude() {
        return currentLongitude;
    }


    /* ------------- Service methods ------------- */

    // Gets results of nearby locations
    private void getNearbyResults() {

        // Start loading dialog
        Utils.startLoadingSpinner(this, progressDialog);

        String GOOGLE_MAPS_KEY = getString(R.string.google_maps_key);

        Call<Example> call = service.getNearby(getCurrentLatitude() + "," + getCurrentLongitude(), Utils.getRadiusPreference(this), GOOGLE_MAPS_KEY);
        call.enqueue(new Callback<Example>() {

            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                // Track last search type
                setLastSearchRequest(SEARCH_NEARBY, "");

                if (response.body().getStatus().equals(ZERO_RESULTS)) {
                    noResultsNotification();
                    return;
                }

                listResults = (ArrayList<Result>) response.body().getResults();
                sendResultsToFragments();

                Snackbar.make(findViewById(R.id.content_nav_drawer_constraint_layout), R.string.showingNearby, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Utils.endLoadingSpinner(progressDialog);
            }


        });
    }

    // Gets results of list of favorites
    private void getFavorites() {

        ArrayList<String> favorites = handler.getFaveIDS();
        final int favoritesSize = favorites.size();

        String GOOGLE_MAPS_KEY = getString(R.string.google_maps_key);

        if (favoritesSize == 0) {
            Snackbar.make(findViewById(R.id.content_nav_drawer_constraint_layout), R.string.noFavoritesToShow, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        // Start loading dialog
        Utils.startLoadingSpinner(this, progressDialog);

        if(listResults != null) {listResults.clear();}

        for (int i = 0; i < favoritesSize; i++) {
            Call<ExampleSingle> call = service.getById(favorites.get(i), GOOGLE_MAPS_KEY);
            call.enqueue(new Callback<ExampleSingle>() {
                @Override
                public void onResponse(Call<ExampleSingle> call, Response<ExampleSingle> response) {

                    // Track last search type
                    setLastSearchRequest(SEARCH_FAVES, "");

                    ResultSingle resultSingle = response.body().getResult();
                    Result newResult = new Result(resultSingle.getGeometry(), resultSingle.getName(), resultSingle.getPlaceId(), resultSingle.getTypes(), resultSingle.getVicinity());
                    listResults.add(newResult);

                    if (listResults.size() == favoritesSize) {
                        sendResultsToFragments();
                        Snackbar.make(findViewById(R.id.content_nav_drawer_constraint_layout), R.string.showingFavorites, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }

                @Override
                public void onFailure(Call<ExampleSingle> call, Throwable t) {
                    // Close loading dialog
                    Utils.endLoadingSpinner(progressDialog);
                }
            });
        }
    }

    // Gets results based on query
    private void getQueryResults(final String query) {

        // Start loading dialog
        Utils.startLoadingSpinner(this, progressDialog);

        String GOOGLE_MAPS_KEY = getString(R.string.google_maps_key);

        Call<Example> call = service.getBySearchQuery(currentLatitude + "," + currentLongitude, query, Utils.getRadiusPreference(this), GOOGLE_MAPS_KEY);
        call.enqueue(new Callback<Example>() {

            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                // Track last search type
                setLastSearchRequest(SEARCH_QUERY, query);

                if (response.body().getStatus().equals(ZERO_RESULTS)) {
                    noResultsNotification();
                    return;
                }

                listResults = (ArrayList<Result>) response.body().getResults();
                sendResultsToFragments();
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                // Close loading dialog
                Utils.endLoadingSpinner(progressDialog);
            }


        });
    }

    // Gets results based on category
    private void getCategoryResults(final String category) {

        // Start loading dialog
        Utils.startLoadingSpinner(this, progressDialog);

        String GOOGLE_MAPS_KEY = getString(R.string.google_maps_key);

        Call<Example> call = service.getNearbyByCategory(category, currentLatitude + "," + currentLongitude, Utils.getRadiusPreference(this), GOOGLE_MAPS_KEY);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                // Track last search type
                setLastSearchRequest(SEARCH_CATEGORY, category);

                if (response.body().getStatus().equals(ZERO_RESULTS)) {
                    noResultsNotification();
                    return;
                }

                listResults = (ArrayList<Result>) response.body().getResults();
                sendResultsToFragments();

            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                // Close loading dialog
                Utils.endLoadingSpinner(progressDialog);
            }
        });

    }

    // Notification if no results
    private void noResultsNotification() {
        // Close loading dialog
        Utils.endLoadingSpinner(progressDialog);
        Snackbar.make(findViewById(R.id.content_nav_drawer_constraint_layout), R.string.noResultsQuery, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    // Send results to map and list fragments
    private void sendResultsToFragments() {
        // Send data to adapter & map
        bus.post(new FragmentDataEvent(listResults));

        // Close loading dialog
        Utils.endLoadingSpinner(progressDialog);
    }

    // Tracks last search request type. In case radius is changed, search will refresh with new results
    private void setLastSearchRequest(String searchType, String lastQuery) {
        lastSearch = searchType;
        this.lastQuery = lastQuery;
    }


    /* ------------- Event methods ------------- */

    // Send results on rotation to map and list
    public static void sendResultsToMapAndList(EventBus bus) {
        bus.post(new FragmentDataOnRotationEvent(listResults));
    }

    // Below are events that launch on request from other activities/fragments
    @Subscribe
    public void onEvent(NearbyDataEvent event) {
        getNearbyResults();
    }

    @Subscribe
    public void onEvent(QueryDataEvent event) {
        getQueryResults(event.query);
    }

    @Subscribe
    public void onEvent(CategoryDataEvent event) {
        getCategoryResults(event.category);
    }

    @Subscribe
    public void onEvent(UpdateRadiusEvent event) {
        switch(lastSearch) {
            case SEARCH_NEARBY:
                getNearbyResults();
                break;
            case SEARCH_QUERY:
                getQueryResults(lastQuery);
                break;
            case SEARCH_CATEGORY:
                getCategoryResults(lastQuery);
                break;
            default:
                break;
        }

    }

    @Subscribe
    public void onEvent(UpdateMeasurementEvent event) {
        // This will force the list to update with correct measurement
        bus.post(new FragmentDataEvent(listResults));
    }

    @Subscribe
    public void onEvent(UpdateFavoritesEvent event) {
        if(lastSearch.equals(SEARCH_FAVES)){
            if(event.requestType.equals("DeleteAll") || handler.getFaveIDS().size() == 0) {
                // If favorites are deleted, clear list and map
                listResults.clear();
                bus.post(new FragmentDataEvent(listResults));
            } else {
                // If favorites are changed, update map and list
                getFavorites();
            }
        }
    }

}