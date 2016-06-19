package lava.retailcustomerclient.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
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

import java.util.ArrayList;
import java.util.List;

import lava.retailcustomerclient.R;
import lava.retailcustomerclient.ui.CustomerKitActivity;
import lava.retailcustomerclient.utils.AppInfoObject;
import lava.retailcustomerclient.utils.Constants;

/**
 * Created by Mridul on 4/7/2016.
 */
public class RetailAccessibilityService extends AccessibilityService {
    private static final String TAG = "RetailAccessibilityService";

    private static RetailAccessibilityService retailAccessibilityService;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "ACC::onAccessibilityEvent: " + event.getEventType());

        AccessibilityNodeInfo mEventSource = event.getSource();
        boolean isAndroidInstaller = false;
        String androidInstallerPkgName = Constants.getAndroidPackageInstallerName();
        isAndroidInstaller = androidInstallerPkgName.contains(event.getPackageName());

        if (isAndroidInstaller) {
            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
            if (rootInActiveWindow == null) {
                rootInActiveWindow = mEventSource;
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                    mEventSource.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }
            }

            List<String> buttonLabels = new ArrayList();
            buttonLabels.add("OK");
            buttonLabels.add("DONE");
            buttonLabels.add("NEXT");
            buttonLabels.add("INSTALL");
            buttonLabels.add("COMPLETE");

            if (buttonLabels != null && !buttonLabels.isEmpty()) {
                for (String buttonText : buttonLabels) {
                    clickAccessibilityNodeByText(rootInActiveWindow, buttonText);
                }
            }
            //clickAccessibilityNodeByView(event, "com.android.packageinstaller:id/ok_button");
            //clickAccessibilityNodeByView(event, "com.android.packageinstaller:id/done_button");
        }
    }

    private void clickAccessibilityNodeByText(AccessibilityNodeInfo rootInActiveWindow, String buttonText) {

        if (rootInActiveWindow == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodes = rootInActiveWindow.findAccessibilityNodeInfosByText(buttonText);

        if (buttonText != null && nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                AccessibilityNodeInfo node = nodes.get(i);
                if (node.isEnabled() && node.getText() != null) {
                    if (node.getText().toString().equalsIgnoreCase(buttonText)) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);


                        // // TODO: 6/7/2016 figure out below code.
                        Rect outBounds = new Rect();
                        node.getBoundsInScreen(outBounds);
                        AccessibilityNodeInfo parentNode = node.getParent();
                        if (parentNode != null && parentNode.isEnabled()) {
                            int j = 0;
                            while (j < parentNode.getChildCount() && parentNode.getChild(j) != null) {
                                Rect childRect = new Rect();
                                parentNode.getChild(j).getBoundsInScreen(childRect);
                                boolean contains;
                                if ((childRect.centerX() < outBounds.left || childRect.centerX() > outBounds.right) && (outBounds.centerX() < childRect.left || outBounds.centerX() > childRect.right)) {
                                    contains = false;
                                } else {
                                    contains = true;
                                }
                                if (parentNode.getChild(j).isEnabled() && contains && parentNode.getChild(j).getText() == null) {
                                    Log.e("clickAccessibilityNodeByText", parentNode.getChild(j).toString());
                                    //parentNode.getChild(j).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                j++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void clickAccessibilityNodeByView(AccessibilityEvent event, String viewID) {


        int  eventType = event.getEventType();
        //TYPE_WINDOWS_CHANGED == Constant Value: 4194304
        //TYPE_WINDOW_CONTENT_CHANGED == Constant Value: 2048
        //TYPE_WINDOW_STATE_CHANGED == Constant Value: 32

        if ( AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType ||
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == eventType ||
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED == eventType ) {

            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);

                    Log.i(TAG, "ACC::onAccessibilityEvent: nodeInfo=" + nodeInfo);
                    if (nodeInfo == null) {
                        return;
                    }

                    //if (nodeInfo.getClassName().equals("android.widget.PackageInstallerActivity")) {
                    if (nodeInfo.getClassName().equals("android.widget.Button")) {
                        //if (nodeInfo.getPackageName().equals("com.android.packageinstaller")) {

                        List<AccessibilityNodeInfo> nodelist = nodeInfo.findAccessibilityNodeInfosByViewId(viewID);

                        for (AccessibilityNodeInfo node : nodelist) {
                            Log.i(TAG, "ACC::onAccessibilityEvent: next or install " + node);
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            |Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            |Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
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
    }
}