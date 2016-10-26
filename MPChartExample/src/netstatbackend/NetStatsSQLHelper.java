package netstatbackend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sridh on 10/21/2016.
 */

public class NetStatsSQLHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NetStats.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NetStatsContract.NetStatsEntry.TABLE_NAME + " (" +
                    NetStatsContract.NetStatsEntry._ID + " INTEGER PRIMARY KEY," +
                    NetStatsContract.NetStatsEntry.COLUMN_NAME_APPNAME + TEXT_TYPE + COMMA_SEP +
                    NetStatsContract.NetStatsEntry.COLUMN_NAME_PACKAGENAME + TEXT_TYPE + COMMA_SEP +
                    NetStatsContract.NetStatsEntry.COLUMN_NAME_STARTTIME + TEXT_TYPE + COMMA_SEP +
                    NetStatsContract.NetStatsEntry.COLUMN_NAME_ENDTIME + TEXT_TYPE + COMMA_SEP +
                    NetStatsContract.NetStatsEntry.COLUMN_NAME_TOTALUSAGEINBYTES + REAL_TYPE + ")";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NetStatsContract.NetStatsEntry.TABLE_NAME;
    public NetStatsSQLHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
