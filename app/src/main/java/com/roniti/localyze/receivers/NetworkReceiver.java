package com.roniti.localyze.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.roniti.localyze.R;
import com.roniti.localyze.helpers.Utils;

public class NetworkReceiver extends BroadcastReceiver {

    // Whether the internet is connected
    public static boolean internetIsConnected = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null) {
            internetIsConnected = true;
        } else {
            internetIsConnected = false;
            Utils.toastNoInternetConnection(context);
        }
    }

}
