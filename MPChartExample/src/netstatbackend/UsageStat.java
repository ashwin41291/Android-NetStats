package netstatbackend;

import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by sridh on 10/25/2016.
 */

public class UsageStat implements Serializable {
    public String packageName;
    public String appName;

    public int foregroundEvents;
    public Drawable icon;
    private static final long serialVersionUID = -2518143671167959230L;

    private void writeObject(ObjectOutputStream o)
            throws IOException {

        o.writeObject(packageName);
        o.writeObject(appName);
       // o.writeObject(icon);
        o.writeInt(foregroundEvents);
    }

    private void readObject(ObjectInputStream o)
            throws IOException, ClassNotFoundException {

        packageName = (String) o.readObject();
        appName = (String) o.readObject();
      //  icon = (Drawable)o.readObject();
        foregroundEvents = o.readInt();

    }

}
