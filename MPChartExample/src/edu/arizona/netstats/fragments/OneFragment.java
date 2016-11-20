package edu.arizona.netstats.fragments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import edu.arizona.netstats.R;
import edu.arizona.netstats.custom.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import netstatbackend.AppObject;
import netstatbackend.AppStatsRepository;
import netstatbackend.NetworkStat;
import netstatbackend.NetworkStatistic;
import netstatbackend.Persistence;
import netstatbackend.UsageStat;


public class OneFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,MediaPlayer.OnSeekCompleteListener,OnChartValueSelectedListener{

//    BarChart chart;
//    LineChart lineChart;
//    SeekBar mSeekBarX,mSeekBarY;
//    Typeface mTfLight;
//    ArrayList<NetworkStatistic> stats ;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Persistence db;
    public static PackageManager managerInstance;


    public OneFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mRecyclerView = (RecyclerView)view.findViewById(R.id.updates_apps_list);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(getContext());

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one, container, false);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }



    private void updateList(ArrayList<NetworkStat> stats)
    {
        UpdateUsageAdapter adapter = new UpdateUsageAdapter(stats);
        mRecyclerView.setAdapter(adapter);
    }

    private class AsyncTaskRunner extends AsyncTask<Context, String, String> {
        Persistence db;
        ArrayList<NetworkStat> stats;

        @Override
        protected String doInBackground(Context... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                // Do your long operations here and return the result
               Context context = params[0];
            stats = new ArrayList<>();
                db = new Persistence(context);
                PackageManager manager = context.getPackageManager();
                List<PackageInfo> apps = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_PROVIDERS|PackageManager.GET_META_DATA);
                for (PackageInfo app : apps) {
                    ApplicationInfo info = manager.getApplicationInfo(app.packageName,0);
                    if (app.requestedPermissions != null && isUserApp(info)) {

                                Log.d("name ","App name is "+info.name);
                                long lastUpdateTime = manager.getPackageInfo(app.packageName, 0).lastUpdateTime;
                                int id = manager.getApplicationInfo(app.packageName, 0).uid;
                                AppObject obj = new AppObject();
                                obj.packageName = app.packageName;
                                obj.appName = manager.getApplicationLabel(info).toString();
                                obj.applicationIcon = manager.getApplicationIcon(app.packageName);
                                NetworkStat stat = db.getStats(obj,app.lastUpdateTime);
                                stats.add(stat);

                        }
                    }

                // Sleeping for given time period
           //     Thread.sleep(time);

            } catch (Exception e) {
                Log.e("Error",e.getMessage());
               // resp = e.getMessage();
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Collections.sort(stats,getComparator());
            updateList(stats);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {
            //finalResult.setText(text[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        boolean isUserApp(ApplicationInfo ai) {
            int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            return (ai.flags & mask) == 0;
        }

        private Comparator<NetworkStat> getComparator(){
            Comparator comp = new Comparator<NetworkStat>(){

                @Override
                public int compare(NetworkStat lhs, NetworkStat rhs) {
                    if(lhs.totalUsageInBytes>rhs.totalUsageInBytes)
                        return -1;
                    else return 1;
                }
            };
            return comp;
        }
    }
}
