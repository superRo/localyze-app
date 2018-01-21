package com.roniti.localyze.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.roniti.localyze.R;
import com.roniti.localyze.db.DatabaseHandler;
import com.roniti.localyze.eventbus.UpdateFavoritesEvent;
import com.roniti.localyze.eventbus.UpdateMeasurementEvent;
import com.roniti.localyze.ui.dialogs.AboutDialog;
import com.roniti.localyze.helpers.UIConstants;

import org.greenrobot.eventbus.EventBus;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Attach fragment to activity
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        // Back button goes back to map, doesn't refresh map activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public static class SettingsFragment extends PreferenceFragment {

        private EventBus bus;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            bus = EventBus.getDefault();

            // Actions for preferences
            findPreference(UIConstants.PreferenceKeys.MEASUREMENT_UNIT).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    bus.post(new UpdateMeasurementEvent());
                    return true;
                }
            });

            findPreference(UIConstants.PreferenceKeys.DELETE_ALL_FAVORITES).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    deleteAllFaves();
                    return true;
                }
            });

            findPreference(UIConstants.PreferenceKeys.ABOUT_KEY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showAboutDialog();
                    return true;
                }
            });


        }


        /* ------------- Actions for preferences ------------- */

        // Action for the DELETE_ALL_FAVORITES preference
        private void deleteAllFaves() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle(R.string.deleteFavoritesLabel);
            dialog.setMessage(R.string.deleteAllFavesQuestion);
            dialog.setNegativeButton(R.string.no, null);
            dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new DatabaseHandler(getActivity()).deleteAllFaves();
                    bus.post(new UpdateFavoritesEvent("DeleteAll"));
                    Snackbar.make(getView(), R.string.favoritesWereDeletedNotice, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            dialog.show();
        }

        // Action for ABOUT_KEY preference
        private void showAboutDialog() {
            new AboutDialog().show( (((FragmentActivity) getActivity()).getSupportFragmentManager()), UIConstants.Fragments.ABOUT_DIALOG_TAG);
        }

    }

}