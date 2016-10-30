package netstatbackend;

import android.app.Notification;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sridh on 10/21/2016.
 */


public class NotificationMonitorService extends NotificationListenerService {

    Context context;
    NLServiceReceiver nlservicereciver;
    Persistence persistence;
    AppStatsRepository repository;
    HashMap<String,Long> updateTimes;

    public static boolean isNotificationAccessEnabled = false;

    @Override
    public IBinder onBind(Intent mIntent) {
        IBinder mIBinder = super.onBind(mIntent);
        isNotificationAccessEnabled = true;
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        boolean mOnUnbind = super.onUnbind(mIntent);
        isNotificationAccessEnabled = false;
        return mOnUnbind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isNotificationAccessEnabled=true;
        Log.v("Starting","Starting NotificationMonitorService");
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netstat.notificationservicelistener");
        registerReceiver(nlservicereciver,filter);
        context = getApplicationContext();
        persistence = new Persistence(context);
        repository = new AppStatsRepository(context);
        updateTimes = new HashMap<String,Long>();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {

            Log.i("Info","**********  onNotificationPosted");
            Log.i("Info","ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
            String pack = sbn.getPackageName();
//            final StringBuilder sb = new StringBuilder(sbn.getNotification().tickerText.length());
//            sb.append(sbn.getNotification().tickerText);
//            String ticker = sb.toString();
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            String title = extras.getString("android.title");
            int progress = (int)extras.get(Notification.EXTRA_PROGRESS);
            if(pack.equals("com.android.providers.downloads") && progress==0 && (notification.category.equals(Notification.CATEGORY_PROGRESS))){
                PackageManager manager = context.getPackageManager();
                List<ApplicationInfo> apps = manager.getInstalledApplications(0);
                for(ApplicationInfo app:apps){
                    if(app.name.equals(title)){
                        long currentTime = System.currentTimeMillis();
                        NetworkStatistic statistic = repository.getDataStats(app.uid,currentTime);
                        if(statistic.usageInBytes==0)
                        {
                            //Update in DB that app is to be uninstalled
                        }
                        else{
                            if(!updateTimes.containsKey(app.packageName)){
                                updateTimes.put(app.packageName,currentTime);
                            }
                            else {
                                updateTimes.remove(app.packageName);
                                updateTimes.put(app.packageName,currentTime);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception nnfe)
        {
            Log.e("Error",nnfe.getMessage());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        try{
            Log.d("Notification","removing notification");
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            String title = extras.getString("android.title");
            String packageName = sbn.getPackageName();
            String text = extras.getString("android.text");
            if(packageName.equals("com.android.providers.downloads")) {
                PackageManager manager = context.getPackageManager();
                List<ApplicationInfo> apps = manager.getInstalledApplications(0);
                for (ApplicationInfo app : apps) {
                    if (app.name.equals(title)) {
                        Long startTime = updateTimes.get(app.packageName);
                        if(startTime!=null){
                            long endTime = System.currentTimeMillis();
                            //to get data usage between startTime and endTime and this will be update Data for the app, then add it to DB
                         //   NetworkStatistic statistic = repository.getDataStats(app.uid,)
                        }
                    }
                }
            }
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
        }
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            StatusBarNotification[] notifications = NotificationMonitorService.this.getActiveNotifications();
            for(StatusBarNotification sbn:notifications) {
                String pack = sbn.getPackageName();
                String ticker = sbn.getNotification().tickerText.toString();
                Bundle extras = sbn.getNotification().extras;
                String title = extras.getString("android.title");
                String text = extras.getCharSequence("android.text").toString();
                Log.i("Title", title);
                Log.i("Text", text);
                UsageStatsManager statsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                PackageManager manager = getPackageManager();
                boolean isActive = statsManager.isAppInactive(pack);
                ApplicationInfo info = null;
                try {
                    info = manager.getApplicationInfo(pack, 0);
                }
                catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                AppObject obj = new AppObject();
                obj.applicationIcon = info.loadIcon(manager);
                obj.uid = info.uid;
                obj.appName = info.name;
                obj.packageName = pack;
                NetworkStat stat = AppStatsRepository.getDataUsage(obj);
                Persistence persistence = new Persistence(context);
                boolean result = persistence.addToDb(stat);
            }


        }
    }
}
