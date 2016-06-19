package lava.retailcustomerclient.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lava.retailcustomerclient.R;
import lava.retailcustomerclient.deviceutils.PhoneUtils;
import lava.retailcustomerclient.ui.CustomerKitActivity;
import lava.retailcustomerclient.ui.CustomerKitApplication;
import lava.retailcustomerclient.utils.AppInfoObject;
import lava.retailcustomerclient.utils.Constants;
import lava.retailcustomerclient.utils.PackageManagerUtils;
import lava.retailcustomerclient.utils.ProcessState;
import lava.retailcustomerclient.utils.PromoterInfoObject;
import lava.retailcustomerclient.utils.SubmitDataObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;




/**
 * Created by Mridul on 4/12/2016.
 */
public class APKInstallCheckService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    final Messenger serviceMessenger = new Messenger(new IncomingHandler());
    public static final int MSG_REGISTER_CLIENT = 1001;
    public static final int MSG_UNREGISTER_CLIENT = 1002;
    public static final int MSG_COMMAND_FROM_UI = 1003;


    public static final int MSG_PACKAGE_INSTALL_CHECK = 1004;

    public static final int MSG_RETRY_SUBMISSION = 1005;


    private final Service mService = this;




    private Handler mHandler;

    static WindowManager wm;
    static View mView;
    static int nextIndex;

    LayoutInflater inflate;
    static Context serviceContext;

    static private List<AppInfoObject> installList;
    HashSet<String> alreadyInstalledAppsList;

    void ShowToast (String text) {
        Toast.makeText(serviceContext, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceContext = this;
        mHandler = new PackageCheckHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            onApkInstallDone(intent.getStringExtra("installed_package"));
            //sendMessageToUI(intent.getStringExtra("installed_package"));
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public APKInstallCheckService getService() {
            // Return this instance of LocalService so clients can call public methods
            return APKInstallCheckService.this;
        }
        public Messenger getMessenger() {
            // Return this instance of LocalService so clients can call public methods
            return serviceMessenger;
        }
    }

    class PackageCheckHandler extends  Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PACKAGE_INSTALL_CHECK:
                    onApkInstallFailure((String)msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;

                case MSG_COMMAND_FROM_UI:
                    ShowToast(msg.toString());
                    break;

                case MSG_RETRY_SUBMISSION:
                    ProcessState.setState(ProcessState.STATE_COLLECTING_DEVICE_DATA);
                    SubmitData s = new SubmitData(serviceContext);
                    s.execute(getSubmitDataObject());
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
    private void sendMessageToUI(int message, Object obj) {
        for (int i=0; i<mClients.size(); i++) {
            try {
                //Send data as a String
                /*Bundle b = new Bundle();
                b.putString("str1", "ab" + str + "cd");
                */Message msg = Message.obtain(null, CustomerKitActivity.MSG_ASK_FOR_WIFI);
                //msg.setData(b);
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }







    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void startOverlay() {

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
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.LEFT | Gravity.CENTER;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        inflate = (LayoutInflater) getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = inflate.inflate(R.layout.progress_overlay, null);

        if (mView != null) {
            ImageView cancelImage = (ImageView) mView.findViewById(R.id.cancelImage);

            cancelImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopOverlay();
                }
            });

            updateOverlay();

            wm.addView(mView, params);
        }
    }

    private static void stopOverlay() {
        if(mView != null) {
            wm.removeView(mView);
            mView = null;
        }

        nextIndex = 0;
    }

    private static void updateOverlay() {
        if (mView != null) {
            TextView textLabel = (TextView) mView.findViewById(R.id.appdetail);
            if (textLabel != null) {
                textLabel.setText("Installing: " + installList.get(nextIndex).appName + "\n(" + (nextIndex+1) + "/" + installList.size() + ")");
            }
        }
    }

    public void installApps(List<AppInfoObject> installList) {

        this.installList = installList;
        if (installList == null) {
            Log.d("installApps", "list is null");
            return;
        }

        alreadyInstalledAppsList = PackageManagerUtils.getInstalledPackages(serviceContext);

        CustomerKitApplication.getApplication(this).getDefaultSharedPreferences()
                .edit()
                .putBoolean("report_submitted", false)
                .commit();


        ProcessState.setState(ProcessState.STATE_INSTALLING_APKS);
        Log.d("installApps", "Starting app installation");

        startOverlay();

        // start Installs
        // reset static variables
        nextIndex = 0;

        continueInstallApps();
    }

    private void continueInstallApps() {

        String apkInternalPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/apks/";
        String apkExternalPath = Environment.getExternalStorageDirectory() + "/AppsShare/temp/";

        File file = new File(apkExternalPath);
        file.mkdirs(); // ensure directory is present

        // all apps done?
        if (nextIndex >= installList.size()) {
            onAllApkInstallDone();
            return;
        } else {
            updateOverlay();
        }


        if (possibleToInstallPkg(apkInternalPath + installList.get(nextIndex).packageName + ".apk") == false) {
            skipApkInstall();
        } else if (isAlreadyInstalled(installList.get(nextIndex).packageName) == true) {
            skipApkInstall();
        } else {

            //Copy file to external memory first
            String fromFileName = apkInternalPath + installList.get(nextIndex).packageName + ".apk";
            String toFileName = apkExternalPath + installList.get(nextIndex).packageName + ".apk";

            File origFile = new File(fromFileName);
            File tempFile = new File(toFileName);

            try {
                FileUtils.copyFile(origFile, tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                // make sure file permissions are set
                tempFile.setReadable(true, false);

                mHandler.removeCallbacksAndMessages(null);
                Message m = mHandler.obtainMessage(MSG_PACKAGE_INSTALL_CHECK, installList.get(nextIndex).packageName);
                mHandler.sendMessageDelayed(m, Constants.PACKAGE_INSTALL_TIMEOUT);

                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setDataAndType(Uri.fromFile(tempFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                //intent.setComponent(new ComponentName(activityContext.getPackageName(), "AppInstaller"));
                //intent.setPackage(activityContext.getApplicationContext().getPackageName());
                startActivity(intent);
            }
        }
    }

    private boolean isAlreadyInstalled(String packageName) {
        return alreadyInstalledAppsList.contains(packageName);
    }

    private boolean possibleToInstallPkg(String fullname) {

        PackageManager pm = serviceContext.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(fullname, 0);

        if (pi == null) {
            // Trying to parse non existent file.
            return false;
        }
        ShowToast(fullname);
        //ShowToast(pi.packageName);
        return true;
    }

    private void skipApkInstall() {
        Log.e("skipApkInstall", "skipped: " + installList.get(nextIndex).packageName);
        if (installList != null) {
            ShowToast("Skipping " + "\"" + installList.get(nextIndex).appName + "\"");

            installList.get(nextIndex).installDone = 2; // skipped - already present
            installList.get(nextIndex).installts = System.currentTimeMillis();

            nextIndex++;

            continueInstallApps();
        }
    }




    private void onAllApkInstallDone() {
        ProcessState.setState(ProcessState.STATE_DONE_INSTALLING_APKS);
        stopOverlay();

        //reset static data
        nextIndex = 0;

        //Collect Installation & Device data & submit
        ProcessState.setState(ProcessState.STATE_COLLECTING_DEVICE_DATA);
        SubmitData s = new SubmitData(serviceContext);
        s.execute(getSubmitDataObject());
    }

    private void onApkInstallDone(String packageName) {

        Log.e("onApkInstallDone", "Installed: " + packageName);
        if (installList != null) {

            installList.get(nextIndex).installDone = 1; // installed
            installList.get(nextIndex).installts = System.currentTimeMillis();

            nextIndex++;

            continueInstallApps();
        }
    }

    private void onApkInstallFailure(String packageName) {

        Log.e("onApkInstallFailure", "Not Installed: " + packageName);
        if (installList != null) {

            installList.get(nextIndex).installDone = -1; // installed
            installList.get(nextIndex).installts = System.currentTimeMillis();
            nextIndex++;
            continueInstallApps();
        }
    }

    private static SubmitDataObject getSubmitDataObject() {

        SubmitDataObject data = new SubmitDataObject();

        // fill blank promoter info -- will be overwritten by promoter
        data.promoterInfo = new PromoterInfoObject();
        data.promoterInfo.promoterId = null; // just to be safe
        data.promoterInfo.imei = null;
        data.promoterInfo.android_id = null;
        data.promoterInfo.model = null;
        data.promoterInfo.shareAppVersionCode = 0; // just to be safe
        data.promoterInfo.shareAppVersionName = null; // just to be safe

        data.deviceDetails = PhoneUtils.getDeviceInfo(serviceContext);

        data.installRecords = installList;

        ProcessState.setState(ProcessState.STATE_DONE_COLLECTING_DEVICE_DATA);


        return data;
    }









    public class SubmitData extends AsyncTask<SubmitDataObject, String, Boolean> {
        private Context serviceContext;

        public SubmitData(Context serviceContext) {
            this.serviceContext = serviceContext;
        }

        protected void onPostExecute(Boolean result) {

            ProcessState.setState(ProcessState.STATE_DONE_SUBMITTING_DATA);

            // // TODO: 5/13/2016 tell UI
            if (result == true) {
                Toast.makeText(serviceContext, "Data submitted", Toast.LENGTH_LONG).show();
                CustomerKitApplication.getApplication(mService).getDefaultSharedPreferences()
                        .edit()
                        .putBoolean("report_submitted", true)
                        .commit();

            } else {
                Toast.makeText(serviceContext, "Failed to submit data. Process not completed.", Toast.LENGTH_LONG).show();
                sendMessageToUI(CustomerKitActivity.MSG_ASK_FOR_WIFI, null);
            }
        }


        public String convertToJSON(SubmitDataObject d) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            return gson.toJson(d);
        }

        @Override
        protected Boolean doInBackground(SubmitDataObject... custInfo) {

            try {
                ProcessState.setState(ProcessState.STATE_SUBMITTING_DATA);

                String URL = Constants.submitDataURL; // ?submitCustData will be dont in params

                HttpPost httpPost = new HttpPost(URL);
                HttpClient client = new DefaultHttpClient();

                httpPost.setHeader("Content-type", "application/json");
                httpPost.setHeader("Accept", "application/json");


                //JSONObject obj = new JSONObject();
                //obj.put("submitCustData", convertToJSON(custInfo[0]));

                httpPost.setEntity(new StringEntity(convertToJSON(custInfo[0]), "UTF-8"));


                HttpResponse response = client.execute(httpPost);
                if (response.getStatusLine().getStatusCode() != 200){
                    return false;
                }
                HttpEntity entity = response.getEntity();
                Log.e("submitCustData", entity.getContent().toString());


            } catch (Exception e) {
                Log.e("Submit data Failed: ", e.getMessage());
                // TODO: 6/10/2016 Give retry option... dont lose collected data

                return false;
            }
            return true;
        }
    }

}
