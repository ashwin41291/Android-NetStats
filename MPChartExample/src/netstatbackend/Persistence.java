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

    public boolean addToDb(NetworkStat stat){
        try {
            Log.i("Saving", "Saving to database");
            SQLiteDatabase db = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_APPNAME, stat.app.appName);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME, stat.startTime);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_ENDTIME, stat.endTime);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_PACKAGENAME, stat.app.packageName);
            values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES, stat.totalUsageInBytes);
            if (stat.isUpdate)
                values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_ISUPDATE, 1);
            else
                values.put(NetStatsContract.NetStatsEntry.COLUMN_NAME_ISUPDATE, 0);
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

    public ArrayList<NetworkStat> getStats(AppObject app, boolean update){
        ArrayList<NetworkStat> stats = new ArrayList<>();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        String[] projection = {
                NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME,
                NetStatsContract.NetStatsEntry.COLUMN_NAME_ENDTIME,
                NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES
        };
        String selection=null;
        String[] selectionArgs = null;
        if(update) {
            selection = NetStatsContract.NetStatsEntry.COLUMN_NAME_PACKAGENAME + "=? and " + NetStatsContract.NetStatsEntry.COLUMN_NAME_ISUPDATE + "=?";
            selectionArgs = new String[2];
            selectionArgs[0] = app.packageName;
            selectionArgs[1] = "1";
        }
        else {
            selection = NetStatsContract.NetStatsEntry.COLUMN_NAME_PACKAGENAME + "=?";
            selectionArgs = new String[2];
            selectionArgs[0] = app.packageName;
            selectionArgs[1] = "0";
        }

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
            stat.isUpdate = update;
            stats.add(stat);
        }while(c.moveToNext());

        return stats;
    }
}
