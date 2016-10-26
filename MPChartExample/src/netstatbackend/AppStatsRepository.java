package netstatbackend;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.SystemClock;

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

    public AppStatsRepository(Activity activity){
       this.activity = activity;
        manager = activity.getPackageManager();
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

}
