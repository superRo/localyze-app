package com.roniti.localyze.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageButton;
import android.app.AlertDialog.Builder;
import android.widget.TextView;

import com.roniti.localyze.R;
import com.roniti.localyze.helpers.Utils;


public class AboutDialog extends DialogFragment {

    public AboutDialog() {
        // Required empty public constructor
    }


    /* ------------- Lifecycle methods ------------- */

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_about, null);
        Builder builder = new Builder(getActivity());
        builder.setView(dialogView);
        closeButtonControl(dialogView);
        changeColorBasedOnPreference(dialogView);
        openTermsAndConditions(dialogView);
        return builder.create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


     /* ------------- Click methods ------------- */

    // Handles onclick method
    class ClickActions implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            AboutDialog.this.dismiss();
        }
    }

    // X button closes dialog
    private void closeButtonControl(View view) {
        ImageButton buttonCloseDialog =  view.findViewById(R.id.closeButton);
        buttonCloseDialog.setOnClickListener(new ClickActions());
    }

    // Open Google terms and conditions
    private void openTermsAndConditions(View dialogView) {
        TextView termsTextView = dialogView.findViewById(R.id.termsTextView);
        termsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.google.com/policies/privacy/"));
                startActivity(intent);
            }
        });
    }


    /* ------------- UI methods ------------- */

    private void changeColorBasedOnPreference(View view) {
        //Call the SharedPreferences
        int parsedColor = Utils.getColorPreference(getActivity());
        TextView aboutTitle = view.findViewById(R.id.aboutTitle);
        aboutTitle.setBackgroundColor(parsedColor);
    }


}