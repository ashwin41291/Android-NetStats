package edu.arizona.netstats.core;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import netstatbackend.AppObject;
import netstatbackend.NetStatsSQLHelper;
import netstatbackend.NetworkStat;
import netstatbackend.Persistence;

/**
 * Created by sridh on 11/13/2016.
 */

public class DataSyncService  {

    Context context;
    public DataSyncService(Context context){
        this.context = context;
    }

    public void storeData() {
        try {
            Persistence persistence = new Persistence(context);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String android_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            DatabaseReference ref = database.getReference(android_id);

            PackageManager manager = context.getPackageManager();
            List<PackageInfo> packages = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_META_DATA | PackageManager.GET_PROVIDERS);
            for (PackageInfo pack : packages) {
                ApplicationInfo info = manager.getApplicationInfo(pack.packageName, 0);
                if(isUserApp(info)){
                    AppObject obj = new AppObject();
                    obj.appName = manager.getApplicationLabel(info).toString();
                    obj.packageName = pack.packageName;
                    ArrayList<NetworkStat> stats = persistence.getStats(obj);
                    ref.setValue(stats);
                }
            }
        }
        catch (Exception ex){
            Log.e("Error",ex.getMessage());
        }
    }

    boolean isUserApp(ApplicationInfo ai) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (ai.flags & mask) == 0;
    }

}
