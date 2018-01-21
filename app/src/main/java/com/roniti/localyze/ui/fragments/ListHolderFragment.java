package com.roniti.localyze.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.roniti.localyze.R;
import com.roniti.localyze.adapters.PlaceAdapter;
import com.roniti.localyze.api.model.Result;
import com.roniti.localyze.eventbus.FragmentDataEvent;
import com.roniti.localyze.eventbus.FragmentDataOnRotationEvent;
import com.roniti.localyze.helpers.Utils;
import com.roniti.localyze.ui.activities.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class ListHolderFragment extends Fragment {

    // Used for recyclerView and its adapter
    private static PlaceAdapter adapter;
    private static RecyclerView recycler;

    // Used for Eventbus
    private EventBus bus = EventBus.getDefault();

    // Used for orientation change
    private boolean orientationChanged;
    ArrayList<Result> listResults;

    public ListHolderFragment() {
        // Required empty public constructor
    }


    /* ------------- Lifecycle methods ------------- */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Utils.isPortrait(getActivity())) {
            bus.register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_holder, container, false);

        if(Utils.isPortrait(getActivity())) {
            // Set-up recycler view list
            recycler = view.findViewById(R.id.recyclerViewList);
            recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
            recycler.setItemAnimator(new DefaultItemAnimator());

            //Designate that all items in the list will have the same size
            recycler.setHasFixedSize(true);
        }
        //Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Bug fix: Fixes bug if app is opened on landscape and is then rotated to portrait causing event to launch before initial creation of fragments by PagerAdapter
        if(listResults == null && MainActivity.initialStartupOrientationIsLandscape) {
            MainActivity.sendResultsToMapAndList(bus);
            MainActivity.initialStartupOrientationIsLandscape = false;
        }

        // Add markers on orientation to map and list
        if(Utils.isPortrait(getActivity()) && listResults != null && orientationChanged == true) {
            sendDataToAdapter(listResults);
            orientationChanged = false;
        }
    }


    /* ------------- RecyclerView methods ------------- */

    private void sendDataToAdapter(ArrayList<Result> listResults) {
        adapter = new PlaceAdapter(getActivity(), listResults);
        recycler.setAdapter(adapter);
    }


    /* ------------- Event methods ------------- */

    // Receive results from Main Activity
    @Subscribe
    public void onEvent(FragmentDataEvent event) {
        sendDataToAdapter(event.listResults);
    }

    // Receive results from Main Activity on orientation changed
    @Subscribe
    public void onEvent(FragmentDataOnRotationEvent event) {
            listResults = event.listResults;
            orientationChanged = true;
    }
}