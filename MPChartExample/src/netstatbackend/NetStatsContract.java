package netstatbackend;

import android.provider.BaseColumns;

/**
 * Created by sridh on 10/21/2016.
 */

public final class NetStatsContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private NetStatsContract() {
    }

    /* Inner class that defines the table contents */
    public static class NetStatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "appUsage";
        public static final String COLUMN_NAME_APPNAME = "appName";
        public static final String COLUMN_NAME_PACKAGENAME = "packageName";
        public static final String COLUMN_NAME_STARTTIME = "startTime";
        public static final String COLUMN_NAME_ENDTIME = "endTime";
        public static final String COLUMN_NAME_TOTALUSAGEINBYTES = "totalUsageInBytes";
        public static final String COLUMN_NAME_ISUPDATE = "isUpdate";
    }


}

