package lava.retailcustomerclient;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * Created by Mridul on 4/5/2016.
 */



public class Utils {

    Context context;
    public Utils(Context mContext) {
        this.context = mContext;
    }

    public DeviceInfo getDeviceInfo() {

/*
        public String IMEI;
        public String android_id;
        public String brand;
        public String product;
        public String model;
        public String screenH;
        public String screenW;
        public String mac_id;
        public long timestamp;
*/

        DeviceInfo devInfo = new DeviceInfo();

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        devInfo.IMEI = telephonyManager.getDeviceId();
        devInfo.android_id = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        devInfo.manufacturer = Build.MANUFACTURER;
        devInfo.product = Build.PRODUCT;
        devInfo.model = Build.MODEL;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        devInfo.screenH = metrics.heightPixels;
        devInfo.screenW = metrics.widthPixels;

        WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        devInfo.mac_id = info.getMacAddress();

        devInfo.timestamp = System.currentTimeMillis();
        return devInfo;
    }

    public boolean isAccessibilityEnabled(Context mContext, String id) {

        AccessibilityManager am = (AccessibilityManager) mContext
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
                if (id.equals(service.getId())) {
                        return true;
                }
        }
        return false;
    }
}

