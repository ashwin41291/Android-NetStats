package com.xxmassdeveloper.mpchartexample.custom;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.xxmassdeveloper.mpchartexample.fragments.OneFragment;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by sridh on 10/31/2016.
 */

public class UIDAppNameFormatter implements IValueFormatter {

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        int uid = (int)entry.getX();
        PackageManager manager = OneFragment.managerInstance;
        String applicationName="";
        try {
            applicationName="";
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        return applicationName;
    }
}
