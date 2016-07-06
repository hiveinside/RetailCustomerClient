package lava.retailcustomerclient.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import lava.retailcustomerclient.services.APKInstallCheckService;

/**
 * Created by Mridul on 4/12/2016.
 */
public class InstallReceiver extends BroadcastReceiver {

    public InstallReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getEncodedSchemeSpecificPart();


        //This log never displays if the constructor is in or commented out
        //Log.e("InstallReceiver", "Install detected: " + packageName + " " + intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            // success; include in data to send back
            Log.e("InstallReceiver", "Installed: " + packageName);

            //APKInstallCheckService.onApkInstallDone(packageName);
            Intent i = new Intent(context, APKInstallCheckService.class);
            i.setAction(intent.getAction());
            i.putExtra("installed_package", packageName);
            context.startService(i);
        }
    }
}
