
package edu.arizona.netstats.core;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anupcowkur.reservoir.Reservoir;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import edu.arizona.netstats.R;
import  edu.arizona.netstats.fragments.*;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import netstatbackend.AppStatsRepository;
import netstatbackend.NetworkStatistic;
import netstatbackend.NotificationMonitorService;
import netstatbackend.Persistence;

import netstatbackend.UsageStat;


import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class MainActivity extends AppCompatActivity  {

    private static final int READ_PHONE_STATE_REQUEST = 37;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      // addDatabaseEntries();
        Intent i = new Intent(this,NotificationMonitorService.class);
        JobScheduler scheduler = (JobScheduler)this.getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);

        ComponentName serviceName = new ComponentName(this.getApplicationContext(), DataJobService.class);


        JobInfo info = new JobInfo.Builder(1,serviceName).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(300*1000).build();
       // scheduler.schedule(info);
        startService(i);
        if(!NotificationMonitorService.isNotificationAccessEnabled) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);

        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        requestPermissions();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        ImageView settings = (ImageView)findViewById(R.id.info_icon);
        settings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                new MaterialDialog.Builder(v.getContext())
                        .title("NetStats")
                        .content("NetStats helps you identify the number of \"wasted\" updates based on the app usage between any two updates. The app lists the cumulative data wasted across such updates and" +
                                "the total number of such useless updates out of the overall updates for each app.\n\n Developed by Sridhar & Aswin under the guidance of Dr. Chris Gniady, University of Arizona.\n\n " +
                                "Credits: Icons made by Freepik, Madebyoliver from www.flaticon.com \n" +
                                "\n" +
                                "")
                        .show();
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_update_usage);
      //  tabLayout.getTabAt(1).setIcon(R.drawable.ic_time);
      //  statusBarNotify();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }



//    private void addDatabaseEntries(){
//        try {
//            Persistence persistence = new Persistence(this.getApplicationContext());
//            PackageManager manager = this.getPackageManager();
//            List<PackageInfo> apps = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_PROVIDERS);
//            for (PackageInfo app : apps) {
//                String updateTime = String.valueOf(manager.getPackageInfo(app.packageName, 0).lastUpdateTime);
//                String[] permissions = app.requestedPermissions;
//                ApplicationInfo info = manager.getApplicationInfo(app.packageName,0);
//
//                if(permissions!=null && info.name!=null) {
//                    for(int i=0;i<permissions.length;i++) {
//                        if(permissions[i].equals("android.permission.INTERNET")) {
//
//                            Log.v(app.packageName, "Update time for app is " + app.lastUpdateTime);
//                            persistence.addToDb(app.packageName, updateTime);
//                        }
//                    }
//                }
//            }
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OneFragment(), "UPDATES");
     //   adapter.addFragment(new SecondFragment(), "USAGE");

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
            return;
        }
    //    initTextViews();
     //   fillData(packageNameEd.getText().toString());
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            requestReadNetworkHistoryAccess();
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
            return;
        }
    }

    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }

    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
         //   fillData(packageNameEd.getText().toString());
            Log.i("Info","Access granted");
        }
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }


    }
}