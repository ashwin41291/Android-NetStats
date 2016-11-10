package com.xxmassdeveloper.mpchartexample.fragments;

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
import com.xxmassdeveloper.mpchartexample.R;
import com.xxmassdeveloper.mpchartexample.custom.UIDAppNameFormatter;
import com.xxmassdeveloper.mpchartexample.custom.MyAxisValueFormatter;
import com.xxmassdeveloper.mpchartexample.custom.XYMarkerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import netstatbackend.AppStatsRepository;
import netstatbackend.NetworkStatistic;


public class OneFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,MediaPlayer.OnSeekCompleteListener,OnChartValueSelectedListener{

    BarChart chart;
    LineChart lineChart;
    SeekBar mSeekBarX,mSeekBarY;
    Typeface mTfLight;
    ArrayList<NetworkStatistic> stats ;
    public static PackageManager managerInstance;


    public OneFragment() {
        // Required empty public constructor
        stats = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){


  //      mSeekBarX = (SeekBar) view.findViewById(R.id.seekBar1);
   //     mSeekBarY = (SeekBar) view.findViewById(R.id.seekBar2);

        chart = (BarChart) view.findViewById(R.id.barChartStats);
      //chart.setOnChartValueSelectedListener(this);
        chart.setOnChartValueSelectedListener(this);
      //  lineChart.setDrawBarShadow(false);
       // chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
     //   chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

//        IAxisValueFormatter xAxisFormatter = new LargeValueFormatter();
//
        XAxis xAxis =chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
      xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
//      //  xAxis.setGranularity(1f); // only intervals of 1 day
//     //   xAxis.setLabelCount(7);
//        xAxis.setValueFormatter(xAxisFormatter);

        IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(mTfLight);
        rightAxis.setLabelCount(8, false);
       // rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setEnabled(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        l.setWordWrapEnabled(true);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

//        XYMarkerView mv = new XYMarkerView(view.getContext(), xAxisFormatter);
//        mv.setChartView(chart); // For bounds control
//        chart.setMarker(mv); // Set the marker to the chart
        getUsageStats(view);
    //    setData(12, 50);

        // setting data
     //   mSeekBarY.setProgress(50);
      // mSeekBarX.setProgress(12);

       // mSeekBarY.setOnSeekBarChangeListener(this);
       // mSeekBarX.setOnSeekBarChangeListener(this);
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

    private void getUsageStats(View view) {
        PackageManager manager = view.getContext().getPackageManager();
        managerInstance = manager;
        List<PackageInfo> apps = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_PROVIDERS);

        try {
            AsyncTaskRunner task = new AsyncTaskRunner();
            task.execute(view.getContext());

        }
        catch (Exception exception){
            Log.e("NameNotFoundException",exception.getMessage());
        }
        Log.d("Stats size","Update stats collected for "+stats.size()+" apps");
    }

    private void fillCharts()
    {
        Collections.sort(stats);
        List<NetworkStatistic> statsSubset;
        if(stats.size()>5)
          statsSubset = stats.subList(0,6);
        else
        statsSubset = stats;

        float i=0;
        int[] colors = {Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW,Color.CYAN,Color.GRAY};
        int j=0;
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        for(NetworkStatistic stat:statsSubset){
            double usage = stat.usageInBytes/(1024.0*1024.0);

            ArrayList<BarEntry> yVals = new ArrayList<>();
            BarEntry entry = new BarEntry(i++,(float) usage,stat.appName);


            yVals.add(entry);
            BarDataSet dataSet = new BarDataSet(yVals,stat.appName);
            dataSet.setColor(colors[j++]);
            dataSet.setValueFormatter(new UIDAppNameFormatter());

            dataSets.add(dataSet);
        }






        BarData data = new BarData(dataSets);
        data.setValueTextSize(8f);
        data.setValueTypeface(mTfLight);


       data.setBarWidth(0.9f);
       // chart.set
        chart.setData(data);
        chart.invalidate();
        chart.notifyDataSetChanged();


    }

    private class AsyncTaskRunner extends AsyncTask<Context, String, String> {



        @Override
        protected String doInBackground(Context... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                // Do your long operations here and return the result
               Context context = params[0];
            stats = new ArrayList<>();
                AppStatsRepository repository = new AppStatsRepository(context);
                PackageManager manager = context.getPackageManager();
                List<PackageInfo> apps = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_PROVIDERS|PackageManager.GET_META_DATA);
                for (PackageInfo app : apps) {
                    ApplicationInfo info = manager.getApplicationInfo(app.packageName,0);
                    if (app.requestedPermissions != null) {
                        for(int i=0;i<app.requestedPermissions.length;i++) {
                            if(app.requestedPermissions[i].equals("android.permission.INTERNET")) {
                                Log.d("name ","App name is "+info.name);
                                long lastUpdateTime = manager.getPackageInfo(app.packageName, 0).lastUpdateTime;
                                int id = manager.getApplicationInfo(app.packageName, 0).uid;
                                NetworkStatistic stat = repository.getDataStats(id, lastUpdateTime);
                                String[] packageNameParts = app.packageName.split("\\.");
                                stat.appName = String.valueOf(info.loadLabel(manager));
                                stat.uid = id;
                                stats.add(stat);
                            }
                        }
                    }
                }
                // Sleeping for given time period
           //     Thread.sleep(time);

            } catch (Exception e) {
                e.printStackTrace();
               // resp = e.getMessage();
            }
            return "test";
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            fillCharts();
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
    }
}
