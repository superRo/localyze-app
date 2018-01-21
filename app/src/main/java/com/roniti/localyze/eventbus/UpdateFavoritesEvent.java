package com.roniti.localyze.eventbus;


public class UpdateFavoritesEvent {

    public String requestType;

    public UpdateFavoritesEvent(String requestType) {
        this.requestType = requestType;
    }

}
