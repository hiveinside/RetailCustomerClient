package lava.retailcustomerclient.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lava.retailcustomerclient.ui.CustomerKitActivity;

/**
 * Created by Mridul on 5/12/2016.
 */
public class AppInstaller {

    private static final String TAG = "AppInstaller";
    CustomerKitActivity activityContext;
    List<AppInfoObject> installList;

    public AppInstaller(CustomerKitActivity activity) {
        this.activityContext = activity;
    }


    public void installApps(List<AppInfoObject> installList) {
        this.installList = installList;

        Log.d(TAG, "Starting app installation");



        for ( int i=10; i < installList.size(); i++) {
            String apkInternalPath = activityContext.getApplicationContext().getFilesDir().getAbsolutePath() + "/apks/";
            String apkExternalPath = Environment.getExternalStorageDirectory() + "/AppsShare/temp/";

            //Copy file to external memory first
            String fromFileName = apkInternalPath + installList.get(i).packageName + ".apk";
            String toFileName = apkExternalPath + installList.get(i).packageName + ".apk";

            File origFile = new File(fromFileName);
            File tempFile = new File(toFileName);
            tempFile.mkdirs();

            try {
                FileUtils.copyFile(origFile, tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                // make sure file permissions are set
                tempFile.setReadable(true, false);

                ComponentName c;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(tempFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                //intent.setComponent(new ComponentName(activityContext.getPackageName(), "AppInstaller"));
                //intent.setPackage(activityContext.getApplicationContext().getPackageName());
                activityContext.startActivity(intent);
            }
        }

        // testing.. dont start
        //retailAccessibilityService.startProgressOverlay(appsList);
    }
}
