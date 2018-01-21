package com.roniti.localyze.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.roniti.localyze.receivers.NetworkReceiver;
import com.roniti.localyze.R;
import com.roniti.localyze.eventbus.UpdateRadiusEvent;
import com.roniti.localyze.helpers.UIConstants;
import com.roniti.localyze.helpers.Utils;

import org.greenrobot.eventbus.EventBus;

public class SearchRadiusDialog extends DialogFragment {

    private int[] radiusValues;
    private String unitOfLength;
    private NumberPicker np;
    private Dialog searchDialog;
    private EventBus bus;
    Button okButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the preferred measurement and set-up radiusValues array based on it
        getUnitOfLength();

        // Get the current preferred radius
        int currentRadius = Utils.getRadiusPreference(getActivity());

        // Set-up dialog fragment view
        searchDialog = new Dialog(getActivity());
        searchDialog.setTitle(R.string.searchRadiusDialogLabel);
        searchDialog.setContentView(R.layout.dialog_number_picker);

        // Set-up number picker
        setUpNumberPicker(currentRadius);

        // OK Button - saves new preferred radius
        okButton = searchDialog.findViewById(R.id.btnOK);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                searchDialog.dismiss();
                int selectedRadius = radiusValues[np.getValue()];
                savePreferredRadius(selectedRadius);

                if(NetworkReceiver.internetIsConnected && Utils.locationPermissionIsGranted(getActivity())){
                    updateResultsWithNewRadius();
                }
            }
        });

        // X button
        ImageButton imagebuttonClose = searchDialog.findViewById(R.id.imgbtnClose);
        imagebuttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDialog.dismiss();
            }
        });

        // Change color of top bar
        changeColorBasedOnPreference(searchDialog);

        searchDialog.show();

        bus = EventBus.getDefault();

        return searchDialog;

    }



    /* ------------- NumberPicker methods ------------- */

    private void setUpNumberPicker(int currentRadius) {
        np = searchDialog.findViewById(R.id.numberPickerSearchRadius);
        np.setMinValue(0);
        np.setMaxValue(radiusValues.length - 1);
        np.setWrapSelectorWheel(false);
        np.setValue(indexOf(currentRadius, radiusValues));

        if(unitOfLength.equals(UIConstants.PreferenceKeys.MEASUREMENT_UNIT_VALUE_KM)) {
            np.setDisplayedValues(displayMetersArray(radiusValues));
        } else {
            np.setDisplayedValues(displayYardsArray(radiusValues));
        }
    }


    /* ------------- Radius methods ------------- */

    // Gets the index of current radius in the array, so number picker opens with current radius selected
    private static int indexOf(int value, int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    // Saves new preferred radius
    private void savePreferredRadius(int newVal) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(UIConstants.PreferenceKeys.RADIUS, newVal).apply();
    }


    /* ------------- UI methods ------------- */

    private void changeColorBasedOnPreference(Dialog view) {
        //Call the SharedPreferences
        int parsedColor = Utils.getColorPreference(getActivity());
        TextView aboutTitle = view.findViewById(R.id.searchRadiusTitle);
        aboutTitle.setBackgroundColor(parsedColor);
        okButton.setTextColor(parsedColor);
    }


    /* ------------- Display Kilometer and Mile Arrays in NumberPicker methods ------------- */

    private void getUnitOfLength() {
        unitOfLength = PreferenceManager.getDefaultSharedPreferences(getActivity()).
                getString(UIConstants.PreferenceKeys.MEASUREMENT_UNIT, UIConstants.PreferenceKeys.MEASUREMENT_UNIT_VALUE_KM);

        radiusValues = unitOfLength.equals(UIConstants.PreferenceKeys.MEASUREMENT_UNIT_VALUE_KM) ?
                getResources().getIntArray(R.array.searchRadiusValuesMeters) :
                getResources().getIntArray(R.array.searchRadiusValuesYardsInMeters);
    }

    private String[] displayMetersArray(int[] values) {
        String[] array = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = displayMeters(values[i]);
        }
        return array;
    }

    private String displayMeters(int meters) {

        if(meters < 1000) {
            return meters + " m";
        }
        else {
            double km = Math.floor((((double) meters) / 1000.0d) * 10.0d) / 10.0d;
            if (km - ((double) ((int) km)) < 0.01d) {
                return ((int) km) + " " + "km";
            }
            return km + " " + "km";
        }

    }

    private String[] displayYardsArray(int[] values) {
        String[] array = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = displayYards((int) (((double) values[i]) * 1.09361d));
        }
        return array;
    }

    private String displayYards(int yards) {
        if (yards < 1760) {
            return yards + " " + "yd";
        } else {
            double miles = Math.floor((((double) yards) / 1760.0d) * 10.0d) / 10.0d;
            if (miles - ((double) ((int) miles)) < 0.01d) {
                return ((int) miles) + " " + "mi";
            }
            return miles + " " + "mi";
        }
    }


   /* ------------- Event methods ------------- */

    private void updateResultsWithNewRadius() {
        bus.post(new UpdateRadiusEvent());
    }


}