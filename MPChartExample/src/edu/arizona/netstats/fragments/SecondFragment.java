package edu.arizona.netstats.fragments;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.anupcowkur.reservoir.ReservoirPutCallback;
import com.google.gson.reflect.TypeToken;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.vincentbrison.openlibraries.android.dualcache.Builder;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.arizona.netstats.R;
import edu.arizona.netstats.custom.AppUtility;
import edu.arizona.netstats.custom.UsageListAdapter;
import netstatbackend.AppObject;
import netstatbackend.AppStatsRepository;
import netstatbackend.NetworkStat;
import netstatbackend.NetworkStatistic;
import netstatbackend.Persistence;
import netstatbackend.UsageStat;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Persistence db;
    private ProgressWheel wheel;
    private ArrayList<UsageStat> usageStats;
    private ArrayList<ApplicationInfo> apps;
    private TextView displayText;

    public SecondFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        mRecyclerView = (RecyclerView)view.findViewById(R.id.apps_list);
        mRecyclerView.setHasFixedSize(true);


        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

//        AsyncTaskRunner task = new AsyncTaskRunner();
//        task.execute(view.getContext());
        wheel = (ProgressWheel)view.findViewById(R.id.progress_wheel_two);
        wheel.setBarColor(Color.RED);
        wheel.spin();

        displayText = (TextView)view.findViewById(R.id.display_text_second);
      UsageDataRetriever retriever = new UsageDataRetriever();
        retriever.execute(getContext());


    }




    private void setUsageListData(ArrayList<UsageStat> usageStats){
        this.usageStats = usageStats;
        if(usageStats.size()==0){
            displayText.setVisibility(View.VISIBLE);
            displayText.setText("No app usage data between 2 " +
                    "updates.");
        }
        else{
            displayText.setVisibility(View.INVISIBLE);
        }
        UsageListAdapter adapter = new UsageListAdapter(usageStats);
        mRecyclerView.setAdapter(adapter);
        wheel.stopSpinning();
    }


    private class UsageDataRetriever extends AsyncTask<Context,String,String>
    {

        private ArrayList<UsageStat> uStats;
        @Override
        protected String doInBackground(Context... params) {
            Context ctxt = params[0];
            uStats = new ArrayList<>();
            File dir = new File(getContext().getFilesDir().getAbsolutePath()+"/usageFiles");
            try {
                if (dir.exists()) {
                    File[] usageFiles = dir.listFiles();
                    for (File f : usageFiles) {
                        String appTitle = f.getName();
                        UsageStat stat = new UsageStat();
                        stat.appName = appTitle;
                        stat.icon = getIcon(appTitle);
                        FileInputStream fis = new FileInputStream(f);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] b = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(b)) != -1) {
                            bos.write(b, 0, bytesRead);
                        }
                        byte[] bytes = bos.toByteArray();
                        int events = Integer.parseInt(new String(bytes));
                        stat.foregroundEvents = events;
                        uStats.add(stat);
                    }
                }
            }
            catch (Exception e){
                Log.e("E/NetStats",e.getMessage());
            }
             return null;
        }

        private Drawable getIcon(String appTitle){
            PackageManager manager = getContext().getPackageManager();
            List<ApplicationInfo> apps = manager.getInstalledApplications(PackageManager.GET_META_DATA);
            for(ApplicationInfo info:apps){
                String label = manager.getApplicationLabel(info).toString();
                if(label.equals(appTitle)||appTitle.startsWith(label)){
                    return manager.getApplicationIcon(info);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String res){
            setUsageListData(uStats);
        }
    }



}
