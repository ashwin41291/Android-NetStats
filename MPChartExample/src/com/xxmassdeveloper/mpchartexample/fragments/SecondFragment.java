package com.xxmassdeveloper.mpchartexample.fragments;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xxmassdeveloper.mpchartexample.R;
import com.xxmassdeveloper.mpchartexample.custom.AppsListAdapter;

import java.util.ArrayList;
import java.util.List;

import netstatbackend.AppObject;
import netstatbackend.AppStatsRepository;
import netstatbackend.NetworkStat;
import netstatbackend.NetworkStatistic;
import netstatbackend.Persistence;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Persistence db;

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
        db = new Persistence(view.getContext());
        PackageManager manager = view.getContext().getPackageManager();
        ArrayList<NetworkStat> apps = new ArrayList<>();
        AppStatsRepository repository = new AppStatsRepository(view.getContext());
        List<PackageInfo> packages = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_PROVIDERS);
        try {
            for (PackageInfo pack : packages) {
                ApplicationInfo app = manager.getApplicationInfo(pack.packageName, 0);
                NetworkStatistic stat = repository.getDataStats(app.uid,pack.lastUpdateTime);
                if(app.name!=null && stat.usageInBytes<=1024*1024){
                    NetworkStat appToBeDeleted = new NetworkStat();
                    appToBeDeleted.totalUsageInBytes= stat.usageInBytes;
                    appToBeDeleted.app = new AppObject();
                    appToBeDeleted.app.packageName = pack.packageName;
                    appToBeDeleted.app.appName = app.name;
                    apps.add(appToBeDeleted);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // specify an adapter (see also next example)
        mAdapter = new AppsListAdapter(apps);
        mRecyclerView.setAdapter(mAdapter);
    }
}
