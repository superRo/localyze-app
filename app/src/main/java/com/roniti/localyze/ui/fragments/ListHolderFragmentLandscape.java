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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;


/*  Major bug note:
    There was a significant issue caused by the viewpager/adapter if the landscape layout didn't also have tabs and viewpager.
    On orientation change, it cached every instance of the fragments in the viewpager tabs and then created new fragments on return
    to portrait orientation. This happened infinitely, adding an additional 2 instances of fragments on each orientation change. After many
    attempts at solving it cleanly (difficult with a PagerAdapter), creating this new activity for the landscape layout was the best hack
    that worked well enough. It's not perfect and it's a temp solution, but it's better than no solution at all!
 */

public class ListHolderFragmentLandscape extends Fragment {

    // Used for recyclerView and its adapter
    private static PlaceAdapter adapter;
    private static RecyclerView recycler;

    // Used for Eventbus
    private EventBus bus = EventBus.getDefault();

    // Used for orientation change
    private boolean orientationChanged;
    ArrayList<Result> listResults;


    public ListHolderFragmentLandscape() {
        // Required empty public constructor
    }


    /* ------------- Lifecycle methods ------------- */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(listResults != null && orientationChanged == true) {
            sendDataToAdapter(listResults);
            orientationChanged = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_holder, container, false);

            // Set-up recycler view list
            recycler = view.findViewById(R.id.recyclerViewList);
            recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
            recycler.setItemAnimator(new DefaultItemAnimator());

            //Designate that all items in the list will have the same size
            recycler.setHasFixedSize(true);

            // Remove second Google logo from screen
            view.findViewById(R.id.poweredByGoogleImage).setVisibility(View.GONE);

        //Inflate the layout for this fragment
        return view;
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