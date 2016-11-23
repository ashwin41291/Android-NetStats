package edu.arizona.netstats.custom;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppUtility {

    private static final String SYSTEM_PACKAGE_NAME = "android";
    private PackageManager mPackageManager = null;

    public AppUtility(Context context) {
        mPackageManager = (PackageManager) context.getPackageManager();
    }

    /**
     * Match signature of application to identify that if it is signed by system
     * or not.
     *
     * @param packageName
     *            package of application. Can not be blank.
     * @return <code>true</code> if application is signed by system certificate,
     *         otherwise <code>false</code>
     */
    public boolean isSystemApp(String packageName) {
        try {
            // Get packageinfo for target application
            PackageInfo targetPkgInfo = mPackageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            // Get packageinfo for system package
            PackageInfo sys = mPackageManager.getPackageInfo(
                    SYSTEM_PACKAGE_NAME, PackageManager.GET_SIGNATURES);
            // Match both packageinfo for there signatures
            return (targetPkgInfo != null && targetPkgInfo.signatures != null && sys.signatures[0]
                    .equals(targetPkgInfo.signatures[0]));
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if application is preloaded. It also check if the application is
     * signed by system certificate or not.
     *
     * @param packageName
     *            package name of application. Can not be null.
     * @return <code>true</code> if package is preloaded and system.
     */
    public boolean isAppPreLoaded(String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package name can not be null");
        }
        try {
            ApplicationInfo ai = mPackageManager.getApplicationInfo(
                    packageName, 0);
            // First check if it is preloaded.
            // If yes then check if it is System app or not.
            if (ai != null
                    && (ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                // Check if signature matches
                if (isSystemApp(packageName) == true) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}