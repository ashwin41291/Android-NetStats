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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
            ArrayList<NetworkStat> stats = new ArrayList<>();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String android_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            DatabaseReference ref = database.getReference("netstats");
            DatabaseReference child = ref.child(android_id);
            PackageManager manager = context.getPackageManager();
            List<PackageInfo> packages = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_META_DATA | PackageManager.GET_PROVIDERS);
            File dir = context.getFilesDir();
            File[] files = new File(dir.getAbsolutePath()+"/netstats").listFiles();
            if(files!=null){
                for(File f:files){
                    String name = f.getName();
                    NetworkStat stat = new NetworkStat();
                    stat.app = new AppObject();
                    stat.app.appName = name;
                    stat.endTime = String.valueOf(f.lastModified());
                    FileInputStream fis = new FileInputStream(f);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] b = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(b)) != -1) {
                        bos.write(b, 0, bytesRead);
                    }
                    byte[] bytes = bos.toByteArray();
                    long usage = Long.parseLong(new String(bytes));

                    stat.totalUsageInBytes = usage;
                    stats.add(stat);
                }
            }
            child.setValue(stats);
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
