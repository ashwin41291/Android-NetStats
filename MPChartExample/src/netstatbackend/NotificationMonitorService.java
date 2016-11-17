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
       // nlservicereciver = new NLServiceReceiver();
       // IntentFilter filter = new IntentFilter();
     //   filter.addAction("com.netstat.notificationservicelistener");
      //  registerReceiver(nlservicereciver,filter);
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
            Log.i("Info",title);
            int progress = extras.getInt("android.progress");
            if(pack.equals("com.android.providers.downloads") && progress==0){
                PackageManager manager = context.getPackageManager();
                List<ApplicationInfo> apps = manager.getInstalledApplications(0);

                for(ApplicationInfo app:apps){
                    String name = manager.getApplicationLabel(app).toString();
                    if(name.equals(title)){
                        long currentTime = System.currentTimeMillis();
                        long lastUpdateTime = manager.getPackageInfo(app.packageName,0).lastUpdateTime;

                            if(!updateTimes.containsKey(app.packageName)){
                                updateTimes.put(app.packageName,currentTime);
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

    boolean isUserApp(ApplicationInfo ai) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (ai.flags & mask) == 0;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        try{
            Log.d("Notification","removing notification");
            Log.v("Notification","adding uodae usage for "+sbn.getPackageName());
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            String title = extras.getString("android.title");
            String packageName = sbn.getPackageName();
            String text = extras.getString("android.text");
            Log.v("Info","Name of app is "+text);
            int progress = extras.getInt("android.progress");
            if(packageName.equals("com.android.providers.downloads")) {
                PackageManager manager = context.getPackageManager();
                List<ApplicationInfo> apps = manager.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo app : apps) {
                    String name = manager.getApplicationLabel(app).toString();
                    if (name.equals(title) && progress==100) {
                        int uid = manager.getApplicationInfo("com.android.providers.downloads",PackageManager.GET_META_DATA).uid;
                        Long startTime = updateTimes.get(app.packageName);
                   //     NetworkStatistic stats = repository.getDataStats(uid,(long)startTime);
                        if(startTime!=null){
                           NetworkStatistic stats = repository.getDataStats(uid,startTime);
                            stats.packageName = app.packageName;
                            stats.appName = name;
                            persistence.addToDb(stats);
                            updateTimes.remove(app.packageName);
                            //to get data usage between startTime and endTime and this will be update Data for the app, then add it to DB
                         //   NetworkStatistic statistic = repository.getDataStats(app.uid,)
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
        }
    }


}
