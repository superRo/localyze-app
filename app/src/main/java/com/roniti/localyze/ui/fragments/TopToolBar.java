package com.roniti.localyze.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.roniti.localyze.receivers.NetworkReceiver;
import com.roniti.localyze.R;
import com.roniti.localyze.eventbus.NearbyDataEvent;
import com.roniti.localyze.eventbus.QueryDataEvent;
import com.roniti.localyze.ui.dialogs.CategoryDialog;
import com.roniti.localyze.helpers.UIConstants;
import com.roniti.localyze.ui.dialogs.SearchRadiusDialog;
import com.roniti.localyze.helpers.Utils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class TopToolBar extends Fragment implements PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener {

    private OnFragmentInteractionListener mListener;
    private SearchView querySearchView;
    private EventBus bus;

    private static String POPUP_CONSTANT = "mPopup";
    private static String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";

    public TopToolBar() {
        // Required empty public constructor
    }


    /* ------------- Lifecyle methods ------------- */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_tool_bar, container, false);

        // Open drawer using hamburger menu button
        hamburgerMenuButtonSetup(view);

        // Search view and button
        querySearchView = view.findViewById(R.id.querySearchView);
        querySearchView.setOnQueryTextListener(this);
        searchButtonSetup(view);

        // Find locations nearby
        nearbyButtonSetup(view);

        // Open filter menu
        filterButtonSetup(view);

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus = EventBus.getDefault();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /* ------------- UI methods ------------- */

    private void hamburgerMenuButtonSetup(View view) {
        ImageButton menuButton = view.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onFragmentInteraction();
                }
            }
        });
    }

    private void filterButtonSetup(View view) {
        ImageButton filterButton = view.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterMenu(view);
            }
        });
    }

    private void nearbyButtonSetup(View view) {
        ImageButton gpsButton = view.findViewById(R.id.gpsButton);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks that location permission is granted
                if (Utils.locationPermissionIsGranted(getActivity())) {
                    if(NetworkReceiver.internetIsConnected) {
                        sendHereQueryToMapFragment();
                    } else {
                        Utils.toastNoInternetConnection(getActivity());
                    }
                }  else {
                    Utils.toastLocationRequiredNotice(getActivity());
                }
            }
        });
    }

    private void searchButtonSetup(View view) {
        ImageButton searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks that location permission is granted
                if (Utils.locationPermissionIsGranted(getActivity())) {
                    if(NetworkReceiver.internetIsConnected) {
                        onQueryTextSubmit(querySearchView.getQuery().toString());
                        hideKeyboard();
                    } else {
                        Utils.toastNoInternetConnection(getActivity());
                    }
                }  else {
                    Utils.toastLocationRequiredNotice(getActivity());
                }

            }
        });
    }

    private void changeColorsBasedOnPreference(PopupMenu popup) {
        int parsedColor = Utils.getColorPreference(getActivity());
        popup.getMenu().getItem(0).getIcon().setTint(parsedColor);
        popup.getMenu().getItem(1).getIcon().setTint(parsedColor);

    }


    /* ------------- Search methods ------------- */

    // Enable search for search button in keyboard + searchButton image
    @Override
    public boolean onQueryTextSubmit(String query) {
        if(querySearchView.getQuery().length() > 0) {
            sendSearchQueryToMapFragment(query);
        } else {
            Snackbar.make(getView(), R.string.searchEmptyInputText, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void hideKeyboard() {
        View view2 = getActivity().getCurrentFocus();
        if (view2 != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }


    /* ------------- Interface communication methods ------------- */

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }


    /* ------------- Filter options methods ------------- */

    // Shows the filtering menu
    public void showFilterMenu(View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        try {
            // Reflection apis to enforce show icon
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(POPUP_CONSTANT)) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        changeColorsBasedOnPreference(popup);
        popup.show();

    }

    // Open SearchRadiusDialog
    public void showRadiusDialog()    {
        SearchRadiusDialog dialog = new SearchRadiusDialog();
        dialog.show(getActivity().getFragmentManager(), UIConstants.Fragments.SEARCH_RADIUS_DIALOG_TAG);
    }

    // Filter menu
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pmnuRadius:
                showRadiusDialog();
                break;
            case R.id.pmnuCategory:
                CategoryDialog dFragment = new CategoryDialog();
                dFragment.show(getActivity().getFragmentManager(), UIConstants.Fragments.CATEGORY_DIALOG_TAG);
                break;
        }

        return false;
    }


   /* ------------- Event methods ------------- */

    private void sendSearchQueryToMapFragment(String query) {
        bus.post(new QueryDataEvent(query));
    }

    private void sendHereQueryToMapFragment() {
        bus.post(new NearbyDataEvent());
    }

}