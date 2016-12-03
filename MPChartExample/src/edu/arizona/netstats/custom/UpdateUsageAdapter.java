package edu.arizona.netstats.custom;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import edu.arizona.netstats.R;
import netstatbackend.NetworkStat;

/**
 * Created by sridh on 11/15/2016.
 */

public class UpdateUsageAdapter extends RecyclerView.Adapter<UpdateUsageAdapter.ViewHolder> {
    private List<NetworkStat> apps;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView appName;
        public ImageView icon;
        public TextView usage;
        public TextView updatesCount;
        public ViewHolder(View v) {
            super(v);
            appName = (TextView)v.findViewById(R.id.app_update_name);
            icon = (ImageView)v.findViewById(R.id.app_update_icon);
            usage = (TextView)v.findViewById(R.id.app_update_usage);
            updatesCount = (TextView)v.findViewById(R.id.app_useless_updates);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UpdateUsageAdapter(List<NetworkStat> apps) {
        this.apps = apps;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UpdateUsageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        try {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_usage_layout, parent, false);
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
        holder.updatesCount.setText(String.valueOf(obj.uselessUpdates)+"/"+String.valueOf(obj.totalUpdates));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return apps.size();
    }
}

