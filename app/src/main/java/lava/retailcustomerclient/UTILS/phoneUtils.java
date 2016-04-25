package lava.retailcustomerclient.utils;

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



public class phoneUtils {

    Context context;
    public phoneUtils(Context mContext) {
        this.context = mContext;
    }

    public DeviceInfoObject getDeviceInfo() {

        /*
        public String imei; // take care of multiple SIM case
        public String android_device_id;
        public String manufacturer;
        public String product;
        public String model;
        public String mac_id;
        public String resolution;
        public int dpi;
        public String os_version; //
        public String board_cpu; // decide board or CPU type
        public String device_id; // check what is it
        public String subscriber_id; //IMSI
        public String language; //locale
        public String timezone; //timezone
        public String operator; //MCC/MNC40445
        public String launcher_app; //which launcher is used
        public String phone_num; // try to get phone number
        public String android_api; // API level
        public int networkStatus; //2g/3g/4g/wifi
        public int rooted_yes_no; // rooted status
        public int internalAvail; //available internal mem
        public int internalTotal; //Total internal mem
        public int extAvail; //available internal mem
        public int extTotal; //Total external mem
        public int ramSize; //RAM size
        public String kitVersion;// kit app version
        */

        DeviceInfoObject devInfo = new DeviceInfoObject();

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        devInfo.imei = telephonyManager.getDeviceId();
        devInfo.android_device_id = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        devInfo.manufacturer = Build.BRAND; //Build.MANUFACTURER;
        devInfo.product = Build.PRODUCT;
        devInfo.model = Build.MODEL;

        WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        devInfo.mac_id = info.getMacAddress();

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        devInfo.resolution = metrics.heightPixels + "x" + metrics.widthPixels;

        /*
        public int dpi;
        public String os_version; //
        public String board_cpu; // decide board or CPU type
        public String device_id; // check what is it
        public String subscriber_id; //IMSI
        public String language; //locale
        public String timezone; //timezone
        public String operator; //MCC/MNC40445
        public String launcher_app; //which launcher is used
        public String phone_num; // try to get phone number
        public String android_api; // API level
        public int networkStatus; //2g/3g/4g/wifi
        public int rooted_yes_no; // rooted status
        public int internalAvail; //available internal mem
        public int internalTotal; //Total internal mem
        public int extAvail; //available internal mem
        public int extTotal; //Total external mem
        public int ramSize; //RAM size
        public String kitVersion;// kit app version
        */

        devInfo.dpi = metrics.densityDpi;
        devInfo.os_version = Build.VERSION.RELEASE;
        devInfo.board_cpu = Build.BOARD;
        devInfo.device_id = "Check"; // check what is it
        devInfo.subscriber_id = "Check"; //IMSI
        devInfo.language = "Check"; //locale
        devInfo.timezone = "Check"; //timezone
        devInfo.operator = "Check"; //MCC/MNC40445
        devInfo.launcher_app = "Check"; //which launcher is used
        devInfo.phone_num = "Check"; // try to get phone number
        devInfo.android_api = Build.VERSION.SDK_INT;
        devInfo.networkStatus = 0; //2g/3g/4g/wifi
        devInfo.rooted_yes_no = 0; // rooted status
        devInfo.internalAvail = 0; //available internal mem
        devInfo.internalTotal = 0; //Total internal mem
        devInfo.extAvail = 0; //available internal mem
        devInfo.extTotal = 0; //Total external mem
        devInfo.ramSize = 0; //RAM size
        devInfo.kitVersion = "Check";// kit app version

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

