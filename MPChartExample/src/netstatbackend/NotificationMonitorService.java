package netstatbackend;

import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sridh on 10/21/2016.
 */


public class NotificationMonitorService extends NotificationListenerService {

    Context context;
    NLServiceReceiver nlservicereciver;

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
        Log.v("Starting","Starting NotificationMonitorService");
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netstat.notificationservicelistener");
        registerReceiver(nlservicereciver,filter);
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {

            Log.i("Info","**********  onNotificationPosted");
            Log.i("Info","ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
            String pack = sbn.getPackageName();
            String ticker = sbn.getNotification().tickerText.toString();
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString("android.title");
            String text = extras.getCharSequence("android.text").toString();
        }
        catch (Exception nnfe)
        {
            Log.e("Error",nnfe.getMessage());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){

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
