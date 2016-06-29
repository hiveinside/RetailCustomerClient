package lava.retailcustomerclient.deviceutils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;

import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import lava.retailcustomerclient.BuildConfig;
import lava.retailcustomerclient.utils.DeviceInfoObject;

/**
 * Created by Mridul on 4/5/2016.
 */



public class PhoneUtils {


    public static DeviceInfoObject getDeviceInfo(Context context) {

        DeviceInfoObject devInfo = new DeviceInfoObject();

        devInfo.imei = getIMEI(context);
        devInfo.android_device_id = getDeviceID(context);
        devInfo.manufacturer = Build.BRAND; //Build.MANUFACTURER;
        devInfo.product = Build.PRODUCT;
        devInfo.model = Build.MODEL;

        devInfo.mac_id = getMacID(context);
        devInfo.resolution = getDeviceResolution(context);

        // TODO: 5/17/2016 get device info "check"
        devInfo.dpi = getDeviceDensity(context);
        devInfo.os_version = Build.VERSION.RELEASE;
        devInfo.board_cpu = Build.BOARD;
        devInfo.device_id = "Check"; // // TODO: 6/7/2016 figure this
        devInfo.subscriber_id = getIMSI(context); //IMSI
        devInfo.language = getLocale(); //locale
        devInfo.timezone = getTimeZone(); //timezone
        devInfo.operator = getOperator(context); //MCC/MNC40445
        devInfo.launcher_app = getDefaultLauncher(context); //which launcher is used
        devInfo.phone_num = getPhoneNum(context); // try to get phone number
        devInfo.android_api = getSdkVersion();
        devInfo.network_status = getNetworkStatus(context); //2g/3g/4g/wifi
        devInfo.rooted = isRooted(); // rooted status
        devInfo.int_avail = getAvailInternalStorage(); //available internal mem
        devInfo.int_total = getTotalInternalStorage(); //Total internal mem
        devInfo.ext_avail = getAvailExternalStorage(); //available internal mem
        devInfo.ext_total = getTotalExternalStorage(); //Total external mem
        devInfo.ram_total = getTotalRAM(context); //RAM size
        devInfo.ram_avail = getAvailableRAM(context); //RAM size
        devInfo.kit_version_code = BuildConfig.VERSION_CODE;// kit app version
        devInfo.kit_version_name = BuildConfig.VERSION_NAME;// kit app version

        return devInfo;
    }

    public static boolean isAccessibilityEnabled(Context mContext, String id) {

        AccessibilityManager am = (AccessibilityManager) mContext
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            Log.d("Accessibility:", service.getId());
            if (id.equals(service.getId())) {
                return true;
            }
        }
        return false;
    }

    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        return DoubleSimUtil.getImei(context, telephonyManager);
    }

    private static String getIMSI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSubscriberId();
    }

    private static String getDeviceID(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private static String getMacID(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getMACAddress("wlan0"); // // TODO: 6/29/2016 decide on wlan0
        } else {
            return info.getMacAddress();
        }
    }

    private static String getDeviceResolution(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels + "x" + metrics.widthPixels;
    }

    private static int getDeviceDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();;
        return metrics.densityDpi;
    }

    private static int getSimState(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSimState();
    }

    private static String getPhoneNum(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    public static int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    private static int getNetworkStatus(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return 2; //2g
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return 3; //3g
            case TelephonyManager.NETWORK_TYPE_LTE:
                return 4; //4g
            default:
                return -1; // error
        }
    }

    private static boolean isRooted() {

        boolean sufound = false;
        boolean busyboxfound = false;
        String[] places = { "/sbin/",
                            "/system/bin/",
                            "/system/xbin/",
                            "/data/local/xbin/",
                            "/data/local/bin/",
                            "/system/sd/xbin/",
                            "/system/bin/failsafe/",
                            "/data/local/"};

        for (String path : places) {
            if ( new File( path + "su").exists() ) {
                sufound = true;
                break;
            }
            if ( new File( path + "busybox").exists() ) {
                sufound = true;
                break;
            }
        }
        return sufound || busyboxfound;
    }

    private static String getLocale() {
        return Locale.getDefault().toString();
    }

    private static String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    private static String getOperator(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkOperator();
    }

    private static String getDefaultLauncher(Context context) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_HOME);

        ResolveInfo res = context.getPackageManager().resolveActivity(intentToResolve, 0);

        if (res != null && res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
            return res.activityInfo.packageName;
        } else {
            return null;
        }
    }

    private static long getAvailableRAM(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    private static long getTotalRAM(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }

    private static long getAvailInternalStorage() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        return statFs.getAvailableBlocksLong() * blockSize;
    }

    private static long getTotalInternalStorage() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        return statFs.getBlockCountLong() * blockSize;
    }

    private static long getAvailExternalStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        return statFs.getAvailableBlocksLong() * blockSize;
    }

    private static long getTotalExternalStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        return statFs.getBlockCountLong() * blockSize;
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return  mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}
