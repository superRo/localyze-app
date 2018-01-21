package com.roniti.localyze.api.service;

import com.roniti.localyze.api.model.Example;
import com.roniti.localyze.api.model_single.ExampleSingle;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface GooglePlacesClient {

    String BASE_URL = "https://maps.googleapis.com/maps/";

    // Find nearby results based on location
    @GET("api/place/nearbysearch/json?sensor=true")
    Call<Example> getNearby(@Query("location") String location, @Query("radius") int radius, @Query("key") String key);

    // Find nearby results for specific categories
    @GET("api/place/nearbysearch/json?sensor=true")
    Call<Example> getNearbyByCategory(@Query("type") String type, @Query("location") String location, @Query("radius") int radius, @Query("key") String key);

    // Find results based on search query
    @GET("api/place/textsearch/json?")
    Call<Example> getBySearchQuery(@Query("location") String location, @Query("query") String query, @Query("radius") int radius, @Query("key") String key);

    // Find result based on placeID
    @GET("api/place/details/json?")
    Call<ExampleSingle> getById(@Query("placeid") String placeid, @Query("key") String key);

}
