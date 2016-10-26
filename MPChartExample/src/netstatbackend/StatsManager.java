package netstatbackend;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.util.Log;

import java.net.NetPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sridh on 10/25/2016.
 */

public class StatsManager {
    UsageStatsManager usageManager;
    NetworkStatsManager netManager;

    public StatsManager(Context context){
        usageManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        netManager = (NetworkStatsManager)context.getSystemService(Context.NETWORK_STATS_SERVICE);
    }

    public ArrayList<UsageStat> getUsageStats(String packageName,long startTime,long endTime){
       List<UsageStats> stats =  usageManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,startTime,endTime);
        ArrayList<UsageStat> ustats = new ArrayList<>();
        for(UsageStats stat:stats){
            UsageStat ustat = new UsageStat();
            ustat.packageName = stat.getPackageName();
            ustat.lastAccessTime = stat.getLastTimeUsed();
            ustat.timeInForeground = stat.getTotalTimeInForeground();
            ustats.add(ustat);
        }
        return ustats;
    }

    public ArrayList<NetworkStat> getNetworkStats(String uid,long startTime,long endTime){
        ArrayList<NetworkStat> nstats =null;
        try {
            NetworkStats netStats = netManager.queryDetails(ConnectivityManager.TYPE_WIFI, uid, startTime, endTime);
            nstats = new ArrayList<>();
            while (netStats.hasNextBucket()){
                NetworkStats.Bucket b = new NetworkStats.Bucket();
                netStats.getNextBucket(b);
                NetworkStat stat = new NetworkStat();
                stat.app = new AppObject();
                stat.endTime = String.valueOf(b.getEndTimeStamp());
                stat.startTime = String.valueOf(b.getStartTimeStamp());
                stat.totalUsageInBytes = b.getRxBytes() + b.getTxBytes();
                nstats.add(stat);
            }
        }
        catch (RemoteException re){
            Log.e("Error",re.getMessage());
        }
        return  nstats;
    }


}
