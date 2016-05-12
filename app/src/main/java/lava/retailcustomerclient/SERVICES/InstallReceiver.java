package lava.retailcustomerclient.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;

import lava.retailcustomerclient.ui.CustomerKitActivity;

/**
 * Created by Mridul on 4/12/2016.
 */
public class InstallReceiver extends BroadcastReceiver {

    CustomerKitActivity activity;

    public InstallReceiver(CustomerKitActivity customerKitActivity) {
        //This log will display in the logcat
        Log.d("InstallReceiver", "InstallReceiver constructor called.");
        this.activity = customerKitActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //String packageName = intent.getData().getEncodedSchemeSpecificPart();


        //This log never displays if the constructor is in or commented out
        //Log.e("InstallReceiver", "Install detected: " + packageName + " " + intent.getAction());
        if ((intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) &&
                (intent.getComponent().getPackageName().equals(context.getPackageName()))) {
            // success; include in data to send back
            //Log.e("InstallReceiver", "Installed: " + packageName);


        }
    }

    public interface ApkInstallCallback {
        public void onApkInstallCompleted();

        public void onApkInstallError();
    }
}
