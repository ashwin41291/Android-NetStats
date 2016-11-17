package netstatbackend;

import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sridhar on 9/25/2016.
 */

public class AppStatsRepository {

    private PackageManager manager;
    private  ArrayList<AppObject> appObjects;
    private  Activity activity;
    private  Context context;
    private UsageStatsManager usageManager;

    public AppStatsRepository(Activity activity){
       this.activity = activity;
        manager = activity.getPackageManager();
    }

    public AppStatsRepository(Context context){
        this.context = context;
        manager = context.getPackageManager();
    }
    public ArrayList<AppObject> getInstalledApplications(){
        appObjects = new ArrayList<>();
        List<ApplicationInfo> packages = manager.getInstalledApplications(PackageManager.GET_META_DATA);
        try {
            for (ApplicationInfo appInfo : packages) {
                PackageInfo info = manager.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS);
                String[] permissions = info.requestedPermissions;
                for(String permission:permissions){
                    if(permission.equals("android.permission.INTERNET")){
                        ApplicationInfo aInfo = info.applicationInfo;
                        AppObject appObj = new AppObject();
                        appObj.applicationIcon = manager.getApplicationIcon(aInfo);
                        appObj.appName = manager.getApplicationLabel(appInfo).toString();
                        appObj.uid = aInfo.uid;
                        appObjects.add(appObj);
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return  appObjects;
    }
    public static NetworkStat getDataUsage(AppObject appObj) {
        if(appObj==null)
            throw new NullPointerException();
        double received
                = (double) TrafficStats.getUidRxBytes(appObj.uid) / (1024 * 1024);
        double sent = (double) TrafficStats.getUidTxBytes(appObj.uid) / (1024 * 1024);

        double total = received + sent;
       NetworkStat stat = new NetworkStat();
       stat.app = appObj;
       stat.totalUsageInBytes = total;
       return  stat;
    }

    public Date getLastBootDate(){
        long seconds = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        return new Date(seconds);
    }

    public NetworkStatistic getDataStats(int uid,long updateTime){
        NetworkStatsManager manager = (NetworkStatsManager)context.getSystemService(Context.NETWORK_STATS_SERVICE);
      //  Context context = this.activity.getApplicationContext();
        NetworkStatistic mobileStats = getMobileBytes(manager,uid,updateTime);
        NetworkStatistic wifiStats = getWifiBytes(manager,uid,updateTime);
        NetworkStatistic result = new NetworkStatistic();
        result.uid = uid;
        result.usageInBytes = (mobileStats!=null?mobileStats.usageInBytes:0)+ (wifiStats!=null?wifiStats.usageInBytes:0);
        result.startDate = updateTime;
        result.endDate = System.currentTimeMillis();

        return result;
    }



    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }



    private NetworkStatistic getMobileBytes(NetworkStatsManager manager,int uid,long updateTime){
        try{
            if(manager!=null){
                long currentTime = System.currentTimeMillis();
                NetworkStatistic stat = new NetworkStatistic();
                stat.uid = uid;
              //  Context context = this.activity.getApplicationContext();
                String subscriberId = getSubscriberId(context,ConnectivityManager.TYPE_MOBILE);
                NetworkStats buckets = manager.querySummary(ConnectivityManager.TYPE_MOBILE,subscriberId,updateTime,currentTime);
                long usage = 0;
                while(buckets.hasNextBucket()){
                    NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                    boolean filled = buckets.getNextBucket(bucket);
                    if(filled && bucket.getUid()==uid) {


                       usage = usage+ bucket.getRxBytes() + bucket.getTxBytes();


                    }
                }
                stat.usageInBytes = usage;
                buckets.close();
                return stat;
            }
        }
        catch (Exception exception){
            Log.e("RemoteException",exception.getMessage());
        }
        return null;
    }

    private NetworkStatistic getWifiBytes(NetworkStatsManager manager,int uid,long updateTime){
        try{
            if(manager!=null){
                long currentTime = System.currentTimeMillis();
                NetworkStatistic stat = new NetworkStatistic();
             //   Context context = this.activity.getApplicationContext();
                String subscriberId = getSubscriberId(context,ConnectivityManager.TYPE_MOBILE);
                NetworkStats buckets = manager.querySummary(ConnectivityManager.TYPE_WIFI,"",updateTime,currentTime);
                long usage = 0;
                while(buckets.hasNextBucket()){
                    NetworkStats.Bucket bucket=new NetworkStats.Bucket();
                    buckets.getNextBucket(bucket);
                    if(bucket.getUid()==uid) {
                     usage = usage + bucket.getTxBytes()+bucket.getRxBytes();
                    }
                }
                stat.usageInBytes = usage;
                buckets.close();
                return stat;
            }
        }
        catch (Exception exception){
            Log.e("RemoteException",exception.getMessage());
        }
        return null;
    }

    public UsageStat getUsageStat(String packageName,long startTime,long endTime){
        usageManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> stats=usageManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,startTime,endTime);
        UsageStat statistic = new UsageStat();
        for(UsageStats stat:stats){
            if(stat.getPackageName().equals(packageName)){
                statistic.packageName = packageName;
                statistic.timeInForeground  = stat.getTotalTimeInForeground();
                break;
            }
        }

//        while (events.hasNextEvent()){
//            UsageEvents.Event event = new UsageEvents.Event();
//            events.getNextEvent(event);
//            if(event.getPackageName().equals(packageName) && event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && !appOpened && !userInteraction){
//                foregroundEvents++;
//                Log.d("Event",packageName+" "+event.getClass());
//                appOpened=true;
//                userInteraction=true;
//            }
//            if(event.getPackageName().equals(packageName) && event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND){
//
//                appOpened=false;
//            }
//            if(event.getPackageName().equals(packageName) && event.getEventType() == UsageEvents.Event.USER_INTERACTION){
//
//                userInteraction=false;
//            }
//
//        }


        return statistic;
    }
}
