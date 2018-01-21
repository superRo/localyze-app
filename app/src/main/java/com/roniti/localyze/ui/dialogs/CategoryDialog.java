package com.roniti.localyze.ui.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.roniti.localyze.R;
import com.roniti.localyze.adapters.CategoryDialogAdapter;
import com.roniti.localyze.helpers.Utils;


public class CategoryDialog extends DialogFragment implements View.OnClickListener {

    private final int NUM_OF_COLUMNS = 3;
    private CategoryDialogAdapter adapter;
    private RecyclerView recycler;

    public CategoryDialog() {
        // Required empty public constructor
    }


    /* ------------- Lifecycle methods ------------- */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        // Set appropriate size of dialog & text based on screen size & orientation
        setFragmentSize();
        setTitleTextSize();

        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_categories, container, false);
        recycler = view.findViewById(R.id.categoriesRecyclerView);
        recycler.setLayoutManager(new GridLayoutManager(getActivity(), NUM_OF_COLUMNS));
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setHasFixedSize(true);
        adapter = new CategoryDialogAdapter(getActivity(), CategoryDialog.this, getResources().getStringArray(R.array.categoryTypeValues), getResources().getStringArray(R.array.categoryTypes));
        recycler.setAdapter(adapter);

        closeButtonControl(view);
        changeColorBasedOnPreference(view);

        return view;
    }


    /* ------------- Click methods ------------- */

    @Override
    public void onClick(View v) {
        // Close dialog
        CategoryDialog.this.dismiss();

    }

    private void closeButtonControl(View view) {
        ImageButton buttonCloseDialog =  view.findViewById(R.id.closeCategoryButton);
        buttonCloseDialog.setOnClickListener(this);
    }


    /* ------------- UI methods ------------- */

    private void changeColorBasedOnPreference(View view) {
        //Call the SharedPreferences
        int parsedColor = Utils.getColorPreference(getActivity());

        TextView categoryTitle = view.findViewById(R.id.categoryTitle);
        categoryTitle.setBackgroundColor(parsedColor);
    }

    private void setFragmentSize() {

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

        if(Utils.isPortrait(getActivity())){
            if(Utils.isTablet(getActivity())){
                params.width = getResources().getDimensionPixelSize(R.dimen.category_dialog_width_xlarge);
                params.height = getResources().getDimensionPixelSize(R.dimen.category_dialog_height_xlarge);
            } else {
                params.width = getResources().getDimensionPixelSize(R.dimen.category_dialog_width);
                params.height = getResources().getDimensionPixelSize(R.dimen.category_dialog_height);
            }
        } else {
            params.width = getResources().getDimensionPixelSize(R.dimen.category_dialog_width_landscape);
            params.height = getResources().getDimensionPixelSize(R.dimen.category_dialog_height_landscape);
        }
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }

    private void setTitleTextSize() {
        // If tablet, make title bar text size larger
        if(Utils.isTablet(getActivity())) {
            TextView categoryBar = getView().findViewById(R.id.categoryTitle);
            categoryBar.setTextSize(20);
        }
    }


}

