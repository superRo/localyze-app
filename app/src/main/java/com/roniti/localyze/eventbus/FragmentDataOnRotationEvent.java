package com.roniti.localyze.eventbus;

import com.roniti.localyze.api.model.Result;

import java.util.ArrayList;

public class FragmentDataOnRotationEvent {

    public ArrayList<Result> listResults;

    public FragmentDataOnRotationEvent(ArrayList<Result> listResults) {
        this.listResults = listResults;
    }


}
