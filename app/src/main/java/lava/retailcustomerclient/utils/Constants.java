package lava.retailcustomerclient.utils;

import android.os.Build;

import lava.retailcustomerclient.deviceutils.PhoneUtils;

/**
 * Created by Mridul on 5/11/2016.
 */
public class Constants {

    public static final String accessibilityServiceId = "lava.retailcustomerclient/.services.RetailAccessibilityService";
    public static final String wifiSSID = "RetailFreeWiFi";
    public static final String getAppsListURL = "http://192.168.43.1:8888/?getAppsList";
    public static final String submitDataURL = "http://192.168.43.1:8888/submitCustData";
    public static final String promoterBaseURL = "http://192.168.43.1:8888";

    public static final int PACKAGE_INSTALL_TIMEOUT = 1200000; // ms // // TODO: 6/28/2016 decide if 2 mins is enough



    public static final String androidPackageInstallerPkg[] = {
            "com.android.packageinstaller",
            "com.google.android.packageinstaller"};
}
