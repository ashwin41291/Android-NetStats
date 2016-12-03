package netstatbackend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sridh on 10/21/2016.
 */

public class Persistence {
    private NetStatsSQLHelper sqlHelper;
    private Context context;
    public Persistence(Context context){
        sqlHelper = new NetStatsSQLHelper(context);
        this.context = context;
    }



    public boolean addToDb(NetworkStatistic stat){
        try {
            Log.i("Saving", "Saving to database");
            SQLiteDatabase db = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_APPNAME, stat.appName);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME, stat.startDate);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_ENDTIME, stat.endDate);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_PACKAGENAME, stat.packageName);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES, stat.usageInBytes);

            long rowId = db.insert(NetStatsContract.NetStatsEntry.TABLE_NAME, null, values);
            if (rowId == -1)
                return false;
            db.close();
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
            return false;
        }
            return true;
    }

    public NetworkStat getStats(AppObject app,long lastUpdateTime) {
        NetworkStat stat = new NetworkStat();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        String[] projection = {
                NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME,
                NetStatsContract.NetStatsEntry.COLUMN_NAME_ENDTIME,
                NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES
        };
        String selection = NetStatsContract.NetStatsEntry.COLUMN_NAME_PACKAGENAME + "=? and " + NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME + ">?";
        ;
        String[] selectionArgs = {app.packageName, String.valueOf(lastUpdateTime)};


        Cursor c = db.query(NetStatsContract.NetStatsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        boolean res = c.moveToFirst();
        double totalUsageInBytes = 0.0;
        if (res){
            do {
                totalUsageInBytes += c.getDouble(c.getColumnIndex(NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES));
            } while (c.moveToNext());
         }
        stat.totalUsageInBytes = totalUsageInBytes;
        stat.app = app;

        c.close();
        return stat;
    }


    public void addToFirebase(NetworkStatistic stat){
        try{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String android_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            DatabaseReference ref = database.getReference("netstats");
            DatabaseReference child = ref.child(android_id);
            DatabaseReference app = child.child(stat.appName);
            DatabaseReference update = app.child(UUID.randomUUID().toString());

            update.setValue(stat);
        }
        catch (Exception e){
            Log.e("Test",e.getMessage());
        }
    }

    public boolean addVictimAppToDb(String packageName,double usageInBytes){
        try {
            SQLiteDatabase db = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(UpdateInfo.UpdateInfoEntry.COLUMN_NAME_PACKAGENAME, packageName);
            values.put(UpdateInfo.UpdateInfoEntry.COLUMN_NAME_USAGE, usageInBytes);
            long rowId = db.insert(UpdateInfo.UpdateInfoEntry.TABLE_NAME, null, values);
            if (rowId == -1)
                return false;
        }
        catch (Exception e){
            Log.e("DB",e.getMessage());
            return false;
        }
        return true;
    }

    public ArrayList<NetworkStat> getVictimApps(){
        ArrayList<NetworkStat> stats = new ArrayList<>();
        SQLiteDatabase db  = sqlHelper.getReadableDatabase();
        String[] columns = {
                UpdateInfo.UpdateInfoEntry.COLUMN_NAME_PACKAGENAME,
                UpdateInfo.UpdateInfoEntry.COLUMN_NAME_USAGE
        };
        try {
            Cursor c = db.query(UpdateInfo.UpdateInfoEntry.COLUMN_NAME_USAGE, columns, null, null, null, null, null);
            c.moveToFirst();
            do {
                String appName = c.getString(c.getColumnIndex(UpdateInfo.UpdateInfoEntry.COLUMN_NAME_PACKAGENAME));
                double usage = c.getDouble(c.getColumnIndex(UpdateInfo.UpdateInfoEntry.COLUMN_NAME_USAGE));
                NetworkStat stat = new NetworkStat();
                stat.app = new AppObject();
                stat.app.appName = appName;
                stat.totalUsageInBytes = usage;
                stats.add(stat);
            } while (c.moveToNext());

            c.close();
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        return stats;
    }



}
