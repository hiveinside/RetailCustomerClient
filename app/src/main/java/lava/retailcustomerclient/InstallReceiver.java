package lava.retailcustomerclient;

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
        // String packageName = intent.getData().getEncodedSchemeSpecificPart();


        //This log never displays if the constructor is in or commented out
        // Log.d("InstallReceiver", "Install detected: " + packageName + " " + intent.getAction());
        Log.d("InstallReceiver", "Install detected intent action: " + intent.getAction());
    }

}
