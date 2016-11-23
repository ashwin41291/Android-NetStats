package edu.arizona.netstats.fragments;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.anupcowkur.reservoir.ReservoirPutCallback;
import com.google.gson.reflect.TypeToken;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.vincentbrison.openlibraries.android.dualcache.Builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        try{
            File dir = new File(getContext().getFilesDir().getAbsolutePath()+"/usage");
            if(!dir.exists())
                dir.mkdir();
            File usageFile = new File(dir.getAbsolutePath()+"/usageFile");
            if(!usageFile.exists())
            {
                wheel = (ProgressWheel)view.findViewById(R.id.progress_wheel_two);
                wheel.setBarColor(Color.RED);
                wheel.spin();
                UsageTaskRunner runner = new UsageTaskRunner();
                runner.execute(getContext());
            }
            else {
                wheel = (ProgressWheel)view.findViewById(R.id.progress_wheel_two);
                wheel.setBarColor(Color.RED);
                wheel.spin();
                FileInputStream fis = new FileInputStream(usageFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                ArrayList<UsageStat> statistics = (ArrayList<UsageStat>) ois.readObject();
                PackageManager manager = getContext().getPackageManager();
                for(UsageStat stat:statistics){
                    ApplicationInfo info = manager.getApplicationInfo(stat.packageName,PackageManager.GET_META_DATA);
                    stat.icon = manager.getApplicationIcon(info);
                }
                setUsageListData(statistics);

                ois.close();
                fis.close();
            }

        }
        catch (Exception e){
            Log.e("E/NetStats",e.getMessage());
        }


        UsageTaskUpdater updater = new UsageTaskUpdater();
        updater.execute(getContext());

      


    }

    private ArrayList<UsageStat> getUsageStats(){
        return this.usageStats;
    }


    private void setUsageListData(ArrayList<UsageStat> usageStats){
        this.usageStats = usageStats;
        UsageListAdapter adapter = new UsageListAdapter(usageStats);
        mRecyclerView.setAdapter(adapter);
        wheel.stopSpinning();
    }

    private class UsageTaskRunner extends AsyncTask<Context,String,String>{

        private ArrayList<UsageStat> uStats;
        Context ctxt;
        @Override
        protected String doInBackground(Context... params) {
            Context context = (Context)params[0];
            ctxt = context;
            uStats = new ArrayList<>();
            try{
                PackageManager manager = context.getPackageManager();
                AppUtility utility = new AppUtility(context);
                AppStatsRepository repository = new AppStatsRepository(context);
                List<PackageInfo>  packages = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_PROVIDERS|PackageManager.GET_META_DATA);
                for(PackageInfo pack:packages){
                    ApplicationInfo info = manager.getApplicationInfo(pack.packageName,0);
                    if(!utility.isAppPreLoaded(pack.packageName) && !utility.isAppPreLoaded(pack.packageName)) {
                       UsageStat stat = new UsageStat();

                       stat.foregroundEvents = 0;
                       // UsageStat stat = repository.getUsageStat(pack.packageName, pack.lastUpdateTime, System.currentTimeMillis());
                        stat.packageName = pack.packageName;
                        stat.appName = manager.getApplicationLabel(info).toString();
                        stat.icon = manager.getApplicationIcon(info);
                        uStats.add(stat);
                   }
                }
            }
            catch (Exception e){
                Log.e("Error",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            Collections.sort(uStats,getComparator());

            setUsageListData(uStats);

         //   UsageTaskUpdater updater = new UsageTaskUpdater();
          //  updater.execute(ctxt);
        }

        boolean isUserApp(ApplicationInfo ai) {
            int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            return (ai.flags & mask) == 0;
        }

        private Comparator<UsageStat> getComparator(){
            Comparator comp = new Comparator<UsageStat>(){

                @Override
                public int compare(UsageStat lhs, UsageStat rhs) {
                    if(lhs.foregroundEvents>rhs.foregroundEvents)
                        return 1;
                    else return -1;
                }
            };
            return comp;
        }
    }



    private class UsageTaskUpdater extends AsyncTask<Context,String,String>{

        private ArrayList<UsageStat> finalUsageStats;
        @Override
        protected String doInBackground(Context... params) {
            Context context = (Context)params[0];
            finalUsageStats = new ArrayList<>();

            try{
                List<UsageStat> stats = getUsageStats();
                PackageManager manager = context.getPackageManager();
                AppStatsRepository repository = new AppStatsRepository(context);
                AppUtility utility = new AppUtility(getContext());
                List<PackageInfo> packages = manager.getInstalledPackages(PackageManager.GET_META_DATA);
                for(PackageInfo pack:packages) {
                    ApplicationInfo info = manager.getApplicationInfo(pack.packageName, 0);
                    if (!utility.isAppPreLoaded(pack.packageName) && !utility.isAppPreLoaded(pack.packageName)) {
                        UsageStat test = repository.getUsageStat(pack.packageName, pack.lastUpdateTime, System.currentTimeMillis());
                        test.packageName = pack.packageName;
                       // ApplicationInfo info = manager.getApplicationInfo(pack.packageName, PackageManager.GET_META_DATA);
                        // test.foregroundEvents = test.foregroundEvents;
                        //  UsageStat stat = repository.getUsageStat(pack.packageName, pack.lastUpdateTime, System.currentTimeMillis());
                        test.appName = manager.getApplicationLabel(info).toString();
                        test.icon = manager.getApplicationIcon(info);
                        finalUsageStats.add(test);
                    }
                }
            }
            catch (Exception e){
                Log.e("Error",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            Collections.sort(finalUsageStats,getComparator());

            UsageListAdapter adapter = new UsageListAdapter(finalUsageStats);
            mRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            wheel.stopSpinning();

            try{
                File dir = new File(getContext().getFilesDir().getAbsolutePath()+"/usage");
                if(!dir.exists())
                    dir.mkdir();
                FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath()+"/usageFile");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(finalUsageStats);
                fos.close();
                oos.close();
            }
            catch (Exception e){
                Log.e("E/NetStats",e.getMessage());
            }
        }

        boolean isUserApp(ApplicationInfo ai) {
            int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            return (ai.flags & mask) == 0;
        }

        private Comparator<UsageStat> getComparator(){
            Comparator comp = new Comparator<UsageStat>(){

                @Override
                public int compare(UsageStat lhs, UsageStat rhs) {
                    if(lhs.foregroundEvents>rhs.foregroundEvents)
                        return 1;
                    else return -1;
                }
            };
            return comp;
        }
    }

}
