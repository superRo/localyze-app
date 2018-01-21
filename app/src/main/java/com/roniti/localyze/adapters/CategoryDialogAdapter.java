package com.roniti.localyze.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.roniti.localyze.helpers.UIConstants;
import com.roniti.localyze.receivers.NetworkReceiver;
import com.roniti.localyze.R;
import com.roniti.localyze.eventbus.CategoryDataEvent;
import com.roniti.localyze.helpers.Utils;
import com.roniti.localyze.ui.activities.MainActivity;
import com.roniti.localyze.ui.dialogs.CategoryDialog;

import org.greenrobot.eventbus.EventBus;


public class CategoryDialogAdapter extends RecyclerView.Adapter<CategoryDialogAdapter.ViewHolder> {

    private Context mContext;
    private String[] categoryValues;
    private String[] categoryNames;
    private CategoryDialog categoryDialog;
    private EventBus bus = EventBus.getDefault();


    public CategoryDialogAdapter(Context context, CategoryDialog categoryDialog, String[] categoryValues, String[] categoryNames) {
        this.mContext = context;
        this.categoryDialog = categoryDialog;
        this.categoryValues = categoryValues;
        this.categoryNames = categoryNames;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_category_dialog_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryDialogAdapter.ViewHolder holder, int position) {
        holder.categoryTextHolder.setText(categoryNames[position]);
        holder.categoryImageHolder.setImageResource(Utils.attachCategoryImage(categoryValues[position], UIConstants.CategoryImageRequestTags.REQUEST_CAT));
    }

    @Override
    public int getItemCount() {
        return categoryValues.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView categoryTextHolder;
        private ImageView categoryImageHolder;


        public ViewHolder(View itemView) {
            super(itemView);

            categoryTextHolder = itemView.findViewById(R.id.categoryTextHolder);
            categoryImageHolder = itemView.findViewById(R.id.categoryImageHolder);

            // Make individual items clickable
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {

            if(Utils.locationPermissionIsGranted(mContext)) {
                if(NetworkReceiver.internetIsConnected) {
                    int position = getAdapterPosition();
                    String category = categoryValues[position];
                    sendCategoryQueryToMapFragment(category);
                } else {
                    Utils.toastNoInternetConnection(mContext);
                }
            } else {
                Utils.toastLocationRequiredNotice(mContext);
            }
        }

    }


    /* ------------- Event methods ------------- */

    private void sendCategoryQueryToMapFragment (String category) {
        categoryDialog.dismiss();
        bus.post(new CategoryDataEvent(category));
    }
}
