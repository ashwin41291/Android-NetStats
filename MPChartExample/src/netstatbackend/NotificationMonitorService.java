package netstatbackend;

import android.app.Notification;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
//            if(pack.equals("com.android.providers.downloads") && progress==0){
//                PackageManager manager = context.getPackageManager();
//                List<ApplicationInfo> apps = manager.getInstalledApplications(0);
//
//                for(ApplicationInfo app:apps){
//                    String name = manager.getApplicationLabel(app).toString();
//                    Log.v("App label - ","Description - "+app.loadDescription(manager));
//                    if((title.equals(name)||title.toLowerCase().contains(name.toLowerCase())||name.toLowerCase().contains(title.toLowerCase()))){
//                        long currentTime = System.currentTimeMillis();
//                        long lastUpdateTime = manager.getPackageInfo(app.packageName,0).lastUpdateTime;
//
//                            if(!updateTimes.containsKey(app.packageName)){
//                                updateTimes.put(app.packageName,currentTime);
//                            }
//
//                        }
//                    }
//
//            }
            if(pack.equals("com.android.providers.downloads") && progress==0) {
                long currentTime = System.currentTimeMillis();
                long usage = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes();
                if(!updateTimes.containsKey(title)){
                    updateTimes.put(title,usage);
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
            Log.v("Notification","adding update usage for "+sbn.getPackageName());
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            String title = extras.getString("android.title");
            String packageName = sbn.getPackageName();
            String text = extras.getString("android.text");
            Log.v("Info","Name of app is "+text);
            int progress = extras.getInt("android.progress");
//            if(packageName.equals("com.android.providers.downloads")) {
//                PackageManager manager = context.getPackageManager();
//                List<ApplicationInfo> apps = manager.getInstalledApplications(PackageManager.GET_META_DATA);
//                for (ApplicationInfo app : apps) {
//                    String name = manager.getApplicationLabel(app).toString();
//                    if ((title.equals(name)||title.toLowerCase().contains(name.toLowerCase())||name.toLowerCase().contains(title.toLowerCase()))) {
//                        int uid = manager.getApplicationInfo("com.android.providers.downloads",PackageManager.GET_META_DATA).uid;
//                        Long startTime = updateTimes.get(app.packageName);
//                   //     NetworkStatistic stats = repository.getDataStats(uid,(long)startTime);
//                        if(startTime!=null){
//                           NetworkStatistic stats = repository.getDataStats(uid,startTime);
//                            stats.packageName = app.packageName;
//                            stats.appName = name;
//                            persistence.addToDb(stats);
//                            updateTimes.remove(app.packageName);
//                            //to get data usage between startTime and endTime and this will be update Data for the app, then add it to DB
//                         //   NetworkStatistic statistic = repository.getDataStats(app.uid,)
//                        }
//                        break;
//                    }
//                }
//            }
            if(packageName.equals("com.android.providers.downloads")){
                PackageManager manager = getPackageManager();
                int uid = manager.getApplicationInfo("com.android.providers.downloads",PackageManager.GET_META_DATA).uid;
                Long usage = updateTimes.get(title);
                if(usage!=null) {
                    long finalUsage = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes();
                    long diff = finalUsage - usage;
                   NetworkStatistic statistic = new NetworkStatistic();
                    statistic.appName = title;
                    statistic.usageInBytes = diff;
                    statistic.packageName = title;
                  //  persistence.addToFirebase(statistic);
                    File dir = context.getFilesDir();
                    File appFiles = new File(dir.getAbsolutePath()+"/netstats");
                    if(!appFiles.exists())
                        appFiles.mkdir();
                    if(checkFileExists(dir.getAbsolutePath()+"/netstats/"+title))
                    {
                        long value = readFile(dir.getAbsolutePath()+"/netstats/"+title);
                        FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + "/netstats/" + title);
                        fos.write(String.valueOf(value+statistic.usageInBytes).getBytes());
                        statistic.usageInBytes = statistic.usageInBytes+value;
                        fos.close();
                    }
                    else {
                        FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + "/netstats/" + title);
                        fos.write(String.valueOf(statistic.usageInBytes).getBytes());
                        fos.close();
                    }
                    persistence.addToDb(statistic);
                    updateTimes.remove(title);
                }
            }
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
        }
    }

    private long readFile(String file) {
        try {
            FileInputStream fis = new FileInputStream(new File(file));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            byte[] bytes = bos.toByteArray();
            long usage = Long.parseLong(new String(bytes));
            return usage;
        }
        catch (Exception e){
            Log.e("E/NetStats",e.getMessage());
        }
        return -1;
    }

    private boolean checkFileExists(String file){
        File f = new File(file);
        return f.exists();
    }




}
