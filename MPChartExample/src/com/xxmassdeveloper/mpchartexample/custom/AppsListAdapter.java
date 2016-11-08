package com.xxmassdeveloper.mpchartexample.custom;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xxmassdeveloper.mpchartexample.R;

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
        public TextView appName,usage;
        public ViewHolder(View v) {
            super(v);
            appName = (TextView)v.findViewById(R.id.appName);
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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.apps_list_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NetworkStat obj = this.apps.get(position);
        holder.appName.setText(obj.app.appName);
        holder.usage.setText(String.valueOf(obj.totalUsageInBytes));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return apps.size();
    }
}
