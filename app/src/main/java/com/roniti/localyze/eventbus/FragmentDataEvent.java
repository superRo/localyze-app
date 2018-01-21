package com.roniti.localyze.eventbus;


import com.roniti.localyze.api.model.Result;

import java.util.ArrayList;

public class FragmentDataEvent {

    public ArrayList<Result> listResults;

    public FragmentDataEvent(ArrayList<Result> listResults) {
        this.listResults = listResults;
    }


}
