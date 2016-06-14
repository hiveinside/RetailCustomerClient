package lava.retailcustomerclient.utils;

/**
 * Created by Mridul on 6/14/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lava.retailcustomerclient.BuildConfig;

/**
 * Created by saurabh on 2/23/16.
 */
public class PackageManagerUtils {

    public static int getVersionCode(Context context) {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName(Context context) {
        return BuildConfig.VERSION_NAME;

    }

    public static String getInstallerPackageName(Context context, String packagename) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getInstallerPackageName(packagename);
        } catch (Exception ex) {
            return null;
        }
    }


    public static int getPackageVersionCode(Context context, String packagename) {

        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(packagename, 0);
            return pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static boolean isInstalled(Context context, String packagename) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packagename);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;


    }

    public static boolean isSystemApp(Context context) {
        //here we check if we have the system permission
        String permission = "android.permission.INSTALL_PACKAGES";
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    public static HashSet<String> getInstalledPackages(Context context) {

        final List<PackageInfo> pkgAppsList = context.getPackageManager().getInstalledPackages(0);

        HashSet<String> packages = new HashSet<String>();
        for (PackageInfo info : pkgAppsList) {
            packages.add(info.packageName);
        }

        return packages;
    }
}