package netstatbackend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by sridh on 10/21/2016.
 */

public class Persistence {
    private NetStatsSQLHelper sqlHelper;
    public Persistence(Context context){
        sqlHelper = new NetStatsSQLHelper(context);
    }

    public boolean addToDb(String packageName,String time)
    {
        try{
            Log.i("Info","Adding update time entry for "+packageName);
            SQLiteDatabase db = sqlHelper.getWritableDatabase();
            boolean exists = checkForUpdatedData(packageName,time);

            ContentValues values = new ContentValues();
            values.put(UpdateInfo.UpdateInfoEntry.COLUMN_NAME_PACKAGENAME,packageName);
            values.put(UpdateInfo.UpdateInfoEntry.COLUMN_NAME_UPDATETIME,time);
            long rowId=0;
            if(!exists) {
                rowId = db.insert(UpdateInfo.UpdateInfoEntry.TABLE_NAME, null, values);
                Log.v("Info","Added first entry successfully for "+packageName);
            }
            else{
                rowId = db.update(UpdateInfo.UpdateInfoEntry.TABLE_NAME,values,null,null);
                Log.v("Info","Updated entry for "+packageName);
            }
            if(rowId!=-1)
                return true;
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        return false;
    }

    private boolean checkForUpdatedData(String packageName,String time){
        ArrayList<NetworkStat> stats = new ArrayList<>();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        String[] projection = {
                UpdateInfo.UpdateInfoEntry.COLUMN_NAME_UPDATETIME
        };
        String selection=UpdateInfo.UpdateInfoEntry.COLUMN_NAME_PACKAGENAME+"=?";
        String[] selectionArgs = {packageName};
        Cursor c = db.rawQuery("select updateTime from updateInfo where packageName=?",selectionArgs);
        if(c.moveToNext()==false)
            return false;
        else
            return true;
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
        }
        catch (Exception e){
            Log.e("Error",e.getMessage());
            return false;
        }
            return true;
    }

    public ArrayList<NetworkStat> getStats(AppObject app){
        ArrayList<NetworkStat> stats = new ArrayList<>();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        String[] projection = {
                NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME,
                NetStatsContract.NetStatsEntry.COLUMN_NAME_ENDTIME,
                NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES
        };
        String selection= NetStatsContract.NetStatsEntry.COLUMN_NAME_PACKAGENAME + "=?";;
        String[] selectionArgs = {app.packageName};


        Cursor c = db.query(NetStatsContract.NetStatsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
        c.moveToFirst();
        do{
            String startDate = c.getString(c.getColumnIndex(NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME));
            String endDate = c.getString(c.getColumnIndex(NetStatsContract.NetStatsEntry.COLUMN_NAME_ENDTIME));
            double usage = c.getDouble(c.getColumnIndex(NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES));
            NetworkStat stat = new NetworkStat();
            stat.app = app;
            stat.totalUsageInBytes = usage;
            stat.startTime = startDate;
            stat.endTime = endDate;
            stats.add(stat);
        }while(c.moveToNext());
        c.close();
        return stats;
    }


}
