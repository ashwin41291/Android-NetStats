package com.xxmassdeveloper.mpchartexample.fragments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.xxmassdeveloper.mpchartexample.R;
import com.xxmassdeveloper.mpchartexample.custom.DayAxisValueFormatter;
import com.xxmassdeveloper.mpchartexample.custom.LongDateStringFormatter;
import com.xxmassdeveloper.mpchartexample.custom.MyAxisValueFormatter;
import com.xxmassdeveloper.mpchartexample.custom.XYMarkerView;

import java.util.ArrayList;
import java.util.List;

import netstatbackend.AppStatsRepository;
import netstatbackend.NetworkStatistic;


public class OneFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,MediaPlayer.OnSeekCompleteListener,OnChartValueSelectedListener{

    BarChart chart;
    SeekBar mSeekBarX,mSeekBarY;
    Typeface mTfLight;
    ArrayList<NetworkStatistic> stats ;

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
      chart.setOnChartValueSelectedListener(this);

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        IAxisValueFormatter xAxisFormatter = new LongDateStringFormatter();

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);

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
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
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
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

        XYMarkerView mv = new XYMarkerView(view.getContext(), xAxisFormatter);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart
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
        List<PackageInfo> apps = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_PROVIDERS);

        try {
            AppStatsRepository repository = new AppStatsRepository(view.getContext());
            for (PackageInfo app : apps) {
                ApplicationInfo info = manager.getApplicationInfo(app.packageName,0);
                if (app.requestedPermissions != null) {
                    for(int i=0;i<app.requestedPermissions.length;i++) {
                        if(app.requestedPermissions[i].equals("android.permission.INTERNET")) {
                            Log.d("permissions ",app.packageName);
                            long lastUpdateTime = manager.getPackageInfo(app.packageName, 0).lastUpdateTime;
                            int id = manager.getApplicationInfo(app.packageName, 0).uid;
                            NetworkStatistic stat = repository.getDataStats(id, lastUpdateTime);
                            stats.add(stat);
                        }
                    }
                }
            }
            fillCharts();
        }
        catch (PackageManager.NameNotFoundException exception){
            Log.e("NameNotFoundException",exception.getMessage());
        }
        Log.d("Stats size","Update stats collected for "+stats.size()+" apps");
    }

    private void fillCharts()
    {
        ArrayList<BarEntry> yVals = new ArrayList<>();
        int i=0;
        for(NetworkStatistic stat:stats){
            double usage = stat.usageInBytes;

            BarEntry entry = new BarEntry(i++,(long)(usage/(1024*1024)),"Facebook");

            yVals.add(entry);
        }

        BarDataSet dataSet = new BarDataSet(yVals,"App usage data");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(mTfLight);
        data.setBarWidth(0.9f);

        chart.setData(data);
        chart.notifyDataSetChanged();
    }
}
