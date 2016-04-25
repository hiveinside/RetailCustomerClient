package lava.retailcustomerclient.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Mridul on 4/12/2016.
 */
public class InstallReceiver extends BroadcastReceiver {

    public InstallReceiver() {
        //This log will display in the logcat
        Log.d("InstallReceiver", "InstallReceiver constructor called.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getEncodedSchemeSpecificPart();


        //This log never displays if the constructor is in or commented out
        //Log.e("InstallReceiver", "Install detected: " + packageName + " " + intent.getAction());
        if ((intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) &&
                (intent.getComponent().getPackageName().equals(context.getPackageName()))) {
            // success; include in data to send back
            Log.e("InstallReceiver", "Installed: " + packageName);
        }
    }
}
