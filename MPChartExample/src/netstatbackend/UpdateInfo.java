package netstatbackend;

import android.provider.BaseColumns;

/**
 * Created by sridh on 10/28/2016.
 */

public final class UpdateInfo {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private UpdateInfo() {
    }

    /* Inner class that defines the table contents */
    public static class UpdateInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "updateInfo";
        public static final String COLUMN_NAME_PACKAGENAME="packageName";
        public static final String COLUMN_NAME_USAGE = "usage";
    }


}
