package com.xxmassdeveloper.mpchartexample.custom;

import android.graphics.drawable.Icon;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xxmassdeveloper.mpchartexample.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import netstatbackend.AppObject;
import netstatbackend.NetworkStat;

/**
 * Created by Ash on 11/8/16.
 */

public class AppsListAdapter extends RecyclerView.Adapter<AppsListAdapter.ViewHolder> {
    private List<NetworkStat> apps;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView appName;
        public ImageView icon;
        public TextView usage;
        public ViewHolder(View v) {
            super(v);
            appName = (TextView)v.findViewById(R.id.app_name);
            icon = (ImageView)v.findViewById(R.id.app_icon);
            usage = (TextView)v.findViewById(R.id.usage);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AppsListAdapter(List<NetworkStat> apps) {
        this.apps = apps;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AppsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        try {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_layout, parent, false);
            // set the view's size, margins, paddings and layout parameters

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        return null;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NetworkStat obj = this.apps.get(position);
        holder.appName.setText(obj.app.appName);
        holder.icon.setImageDrawable(obj.app.applicationIcon);
        double usage = obj.totalUsageInBytes/(1024.0*1024.0);
        Double toBeTruncated = new Double(usage);

        Double truncatedDouble = BigDecimal.valueOf(toBeTruncated)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
        holder.usage.setText(String.valueOf(truncatedDouble)+" MB");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return apps.size();
    }
}