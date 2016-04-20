package lava.retailcustomerclient.SERVICES;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.Context;
import android.widget.TextView;

import java.util.List;

import lava.retailcustomerclient.R;
import lava.retailcustomerclient.UI.CustomerKitActivity;
import lava.retailcustomerclient.UTILS.AppInfoObject;

/**
 * Created by Mridul on 4/7/2016.
 */
public class RetailAccessibilityService extends AccessibilityService {
    private static final String TAG = "RetailAccessibilityService";
    private String serviceID = null;

    View mView;

    LayoutInflater inflate;
    TextView textLabel = null;
    static int appOf = 0;


    private static RetailAccessibilityService retailAccessibilityService;
    List<AppInfoObject> appsList = null;


    WindowManager wm = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "ACC::onAccessibilityEvent: " + event.getEventType());

        int  eventType = event.getEventType();
        //TYPE_WINDOWS_CHANGED == Constant Value: 4194304
        //TYPE_WINDOW_CONTENT_CHANGED == Constant Value: 2048
        //TYPE_WINDOW_STATE_CHANGED == Constant Value: 32

        if ( AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType ||
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == eventType ||
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED == eventType ) {

            AccessibilityNodeInfo nodeInfo1 = getRootInActiveWindow();
            if (nodeInfo1 != null) {
                for (int i = 0; i < nodeInfo1.getChildCount(); i++) {
                    AccessibilityNodeInfo nodeInfo = nodeInfo1.getChild(i);

                    Log.i(TAG, "ACC::onAccessibilityEvent: nodeInfo=" + nodeInfo);
                    if (nodeInfo == null) {
                        return;
                    }

                    //if (nodeInfo.getClassName().equals("android.widget.PackageInstallerActivity")) {
                    if (nodeInfo.getClassName().equals("android.widget.Button")) {
                        //if (nodeInfo.getPackageName().equals("com.android.packageinstaller")) {

                        List<AccessibilityNodeInfo> oklist = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/ok_button");
                        List<AccessibilityNodeInfo> openlist = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/done_button");


                        for (AccessibilityNodeInfo node : oklist) {
                            Log.i(TAG, "ACC::onAccessibilityEvent: next or install " + node);
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }

                        for (AccessibilityNodeInfo node : openlist) {
                            Log.i(TAG, "ACC::onAccessibilityEvent: done_button Installation successful" + node);
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                            //Install successful
                            updateProgressOverlay();

                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        retailAccessibilityService = null;

        return false;
    }

    public static RetailAccessibilityService getSharedInstance() {
        return retailAccessibilityService;
    }

    @Override
    public void onServiceConnected() {

        /*AccessibilityServiceInfo serviceInfo = this.getServiceInfo();
        serviceID = serviceInfo.getId();
        Log.e(TAG, "ACC::onServiceConnected: " + serviceID);
        */

        retailAccessibilityService = this;

        Intent dialogIntent = new Intent(this, CustomerKitActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopProgressOverlay();
    }



    public void startProgressOverlay (List<AppInfoObject> foreignappsList) {


        appsList = foreignappsList;


        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay(); // get phone display size
        int width = display.getWidth();  // deprecated - get phone display width
        int height = display.getHeight(); // deprecated - get phone display height


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.LEFT | Gravity.CENTER;

        inflate = (LayoutInflater) getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = inflate.inflate(R.layout.progress_overlay, null);

        //arcProgress = (ArcProgress) mView.findViewById(R.id.arc_progress);
        textLabel = (TextView)mView.findViewById(R.id.appdetail);

        updateProgressOverlay();

        wm.addView(mView, params);
    }

    public void updateProgressOverlay () {

        if (textLabel != null) {
            int numerofapps = appsList.size();

            textLabel.setText("Installing: " + ++appOf + "/" + numerofapps);

            if (appOf > numerofapps) {
                stopProgressOverlay();
            }
        }
    }

    public void stopProgressOverlay () {
        appOf = 0;
        if (mView != null) {
            wm.removeView(mView);
        }
    }
}