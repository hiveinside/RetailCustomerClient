package lava.retailcustomerclient.ui;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;

import java.util.List;

import lava.retailcustomerclient.R;
import lava.retailcustomerclient.services.APKInstallCheckService;
import lava.retailcustomerclient.utils.AppDownloader;
import lava.retailcustomerclient.utils.AppInfoObject;
import lava.retailcustomerclient.utils.Constants;
import lava.retailcustomerclient.utils.GetAppsList;
import lava.retailcustomerclient.deviceutils.PhoneUtils;
import lava.retailcustomerclient.utils.ProcessState;


public class CustomerKitActivity extends Activity implements AppDownloader.AppDownloadCallback {

    private List<AppInfoObject> appsList;
    int downloadCount = 0;
    Button installButton;

    APKInstallCheckService apkInstallCheckService;
    final Messenger activityMessenger = new Messenger(new IncomingHandler());
    Messenger serviceMessenger = null;
    boolean mBound = false;
    public static final int MSG_UPDATE_UI = 1000;


    void ShowToast (String text) {
        Toast.makeText(CustomerKitActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (appsList != null) {
            setGridAdapter();
        }

        // Bind to LocalService
        Intent intent = new Intent(this, APKInstallCheckService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE); // dont autocreate

        if (mBound) {
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
        }

        UpdateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //startService(new Intent(CustomerKitActivity.this, APKInstallCheckService.class));
        ProcessState.setState(ProcessState.STATE_NOT_STARTED);
        startProcess();
    }

    private void startProcess() {

        setContentView(R.layout.activity_first);

        if (PhoneUtils.isAccessibilityEnabled(this, Constants.accessibilityServiceId) == false) {

            TextView infoText = (TextView)findViewById(R.id.infoText);
            infoText.setText("Please enable [RetailJunction] service");

            Button button = (Button)findViewById(R.id.button);
            button.setText("Open Accessibility Setting -->");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAccessabilitySettings(v);
                    // // TODO: 5/24/2016 check if you can bring existing activity to top
                    finish();
                }
            });

        } else {

            // check if connected to right hotspot
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            String currentSSID = wifiInfo.getSSID();
            Log.e("WiFI SSID", currentSSID );


           // if(currentSSID.compareToIgnoreCase(Constants.wifiSSID))
            if (currentSSID.equals("\"" + Constants.wifiSSID + "\"")) {

                ProcessState.setState(ProcessState.STATE_CONNECTED);
                setContentView(R.layout.activity_main);
                installButton = (Button) findViewById(R.id.doInstall);

                TextView statusText = (TextView)findViewById(R.id.statusText);
                statusText.setText("Connected to: " + currentSSID);

                fetchAppsList();

            } else {

                ProcessState.setState(ProcessState.STATE_NOT_STARTED);

                TextView infoText = (TextView)findViewById(R.id.infoText);
                infoText.setText("Not connected to " + Constants.wifiSSID);

                Button button = (Button)findViewById(R.id.button);
                button.setText("Retry");

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startProcess();
                    }
                });

            }
        }
    }

    private void fetchAppsList() {

        // get apps list from Promoter's phone
        GetAppsList g = new GetAppsList();
        g.setContext(this);
        try {
            //.get makes it blocking
            // // TODO: 5/11/2016 make this unblocking. http error can cause UI hang
            ProcessState.setState(ProcessState.STATE_GETTING_APPSLIST);
            appsList = g.execute().get();

            // update grid
            if (appsList != null) {
                setGridAdapter();

                installButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doCompleteProcess();
                    }
                });

            } else {
                // no apps. // TODO: 5/11/2016 handle error
                ShowToast("No apps found");
            }
        } catch (Exception ee) {
            ProcessState.setState(ProcessState.STATE_CONNECTED);
            ShowToast("Failed to get appslist");
        }
    }


    public void openAccessabilitySettings(View view) {
        if (PhoneUtils.isAccessibilityEnabled(this, Constants.accessibilityServiceId) == false) {

            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 0);
        }
    }

    public void setGridAdapter () {
        GridView gridview = (GridView) findViewById(R.id.appsgrid);
        gridview.setAdapter(new GridViewAdapter(CustomerKitActivity.this, getApplicationContext(), appsList));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(CustomerKitActivity.this, "" + appsList.get(position).packageName,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void doCompleteProcess() {
        //ShowToast("Install Pressed ..");

        // 1) Download apps
        // 2) Install Apps
        // 3) Collect install data
        // 4) Collect Phone info (IMEI, andoid id, brand, model, Resolution, MAC, timestamp, ...
        // 5) Send to RetailJunction
        // 6) Uninstall Kit
        //reset it
        downloadCount = 0;

        //disable installButton
        installButton.setEnabled(false);


        //Step 1: Download Apps
        ProcessState.setState(ProcessState.STATE_DOWNLOADING_APKS);
        AppDownloader a = new AppDownloader(this);
        a.download(getApplicationContext().getFilesDir().getAbsolutePath(), appsList);
    }

    public void updateButtonText(String msg) {
        if (installButton != null) {
            installButton.setText(msg);
        }
    }


    @Override
    public void onApkDownloadCompleted(BaseDownloadTask task) {

        ((AppInfoObject)task.getTag()).downloadDone = true;
        downloadCount++;

        // check if all are downloaded
        if( downloadCount == appsList.size()) {
            ProcessState.setState(ProcessState.STATE_DONE_DOWNLOADING_APKS);
            ShowToast("All apk's downloaded.");


            // Bind to LocalService - AUTO CREATE
            Intent intent = new Intent(this, APKInstallCheckService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            apkInstallCheckService.installApps(appsList);

            //enable installButton again when everything is done
            // // TODO: 5/26/2016 enable after all installs are done. not before. After enabling, change onClick()
        }
    }

    @Override
    public void onApkDownloadError(BaseDownloadTask task) {
        // // TODO: 5/11/2016 decide what to do

    }

    @Override
    public void onApkDownloadProgress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        updateButtonText("Downloading " + ((AppInfoObject)task.getTag()).appName + "..." + Integer.toString((soFarBytes*100/totalBytes)+1) + "%");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            APKInstallCheckService.LocalBinder binder = (APKInstallCheckService.LocalBinder) service;
            apkInstallCheckService = binder.getService();
            serviceMessenger = binder.getMessenger();
            mBound = true;

            try {
                Message msg = Message.obtain(null, APKInstallCheckService.MSG_REGISTER_CLIENT);
                msg.replyTo = activityMessenger;
                serviceMessenger.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_UI:
                    String msgText = msg.getData().getString("str1");
                    //ShowToast("MSG_UPDATE_UI: " + msgText);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    void UpdateUI() {
        int state = ProcessState.getState();

        switch(state) {
            case ProcessState.STATE_NOT_STARTED:
                break;
            case ProcessState.STATE_CONNECTED:
                break;
            case ProcessState.STATE_GETTING_APPSLIST:
                break;
            case ProcessState.STATE_DONE_GETTING_APPSLIST:
                break;
            case ProcessState.STATE_DOWNLOADING_APKS:
                break;
            case ProcessState.STATE_DONE_DOWNLOADING_APKS:
                break;
            case ProcessState.STATE_INSTALLING_APKS:
                break;
            case ProcessState.STATE_DONE_INSTALLING_APKS:
                break;
            case ProcessState.STATE_COLLECTING_DEVICE_DATA:
                break;
            case ProcessState.STATE_DONE_COLLECTING_DEVICE_DATA:
                break;
            case ProcessState.STATE_SUBMITTING_DATA:
                break;
            case ProcessState.STATE_DONE_SUBMITTING_DATA:
                // update button onClick - uninstall
                installButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uninstallSelf();
                    }
                });
                installButton.setText("Process complete. Press to remove kit & exit.");
                installButton.setEnabled(true);
                break;

            default:
                Log.e("UpdateUI", "Bad state "+state);
                break;
        }
    }

    void uninstallSelf() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:lava.retailcustomerclient"));
        startActivity(intent);


        Uri packageUri = Uri.parse("package:org.klnusbaum.test");
        Intent uninstallIntent =
                new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        startActivity(uninstallIntent);
    }
}

