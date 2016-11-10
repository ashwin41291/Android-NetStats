package netstatbackend;

import android.graphics.drawable.Drawable;

/**
 * Created by sridh on 10/25/2016.
 */

public class UsageStat {
    public String packageName;
    public String appName;
    public long timeInForeground;
    public long lastAccessTime;
    public int foregroundEvents;
    public Drawable icon;
}
