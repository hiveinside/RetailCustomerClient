package lava.retailcustomerclient.ui;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import lava.retailcustomerclient.services.InstallReceiver;
import lava.retailcustomerclient.services.ProgressOverlayService;
import lava.retailcustomerclient.services.RetailAccessibilityService;
import lava.retailcustomerclient.utils.AppDownloader;
import lava.retailcustomerclient.utils.AppInfoObject;
import lava.retailcustomerclient.utils.AppInstaller;
import lava.retailcustomerclient.utils.Constants;
import lava.retailcustomerclient.utils.GetAppsList;
import lava.retailcustomerclient.utils.PhoneUtils;


public class CustomerKitActivity extends Activity implements AppDownloader.AppDownloadCallback {

    private List<AppInfoObject> appsList;
    int downloadCount = 0;
    Button installButton;



    RetailAccessibilityService retailAccessibilityService = null;


    void ShowToast (String text) {
        Toast.makeText(CustomerKitActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (appsList != null) {
            setGridAdapter();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(CustomerKitActivity.this, ProgressOverlayService.class));
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
                }
            });

        } else {

            // check if connected to right hotspot
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            String currentSSID = wifiInfo.getSSID();
            Log.e("WiFI SSID", currentSSID );


           // if(currentSSID.compareToIgnoreCase(Constants.wifiSSID))
            if (currentSSID.equals("\"" + Constants.wifiSSID + "\"")) {

                setContentView(R.layout.activity_main);
                installButton = (Button) findViewById(R.id.doInstall);

                retailAccessibilityService = RetailAccessibilityService.getSharedInstance();

                TextView statusText = (TextView)findViewById(R.id.statusText);
                statusText.setText("Connected to: " + currentSSID);

                // get apps list from Promoter's phone
                GetAppsList g = new GetAppsList();
                g.setContext(this);
                try {
                    //.get makes is blocking
                    // // TODO: 5/11/2016 make this unblocking. http error can cause UI hang
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
                    }
                } catch (Exception ee) {
                    installButton.setText("Failed to get appslist");
                }

            } else {

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
        AppDownloader a = new AppDownloader(this);
        a.download(getApplicationContext().getFilesDir().getAbsolutePath(), appsList);

        //Step 2: Install Apps - wait till all downloads complete.
        // // TODO: 5/11/2016 or we will start install as soon as a apk is available? 

        //Step 3: Collect install Data

        //Step 4: Collect Phone info (IMEI, andoid id, brand, model, Resolution, MAC, timestamp, ...
  /*      DeviceInfoObject devInfo;
        devInfo = PhoneUtils.getDeviceInfo();
*/
        //Step 5: Send to RetailJunction
/*        SubmitData submitData = new SubmitData();
        submitData.setContext(this);
        submitData.setButton(installButton);
        submitData.execute(devInfo);*/


        //Step 6: delete temp apk files

        //Step 7: Uninstall Kit

/*
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:lava.retailcustomerclient"));
        startActivity(intent);
*/

        //stopService(new Intent(getApplication(), ProgressOverlayService.class));
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

        //updateButtonText("Completed " + ((AppInfoObject)task.getTag()).appName);

        // check if all are downloaded
        if( downloadCount == appsList.size() - 10) {
            ShowToast("All apk's downloaded.");


            AppInstaller a = new AppInstaller(this);
            a.installApps(appsList);

            //enable installButton again when everything is done
            installButton.setEnabled(true);
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
    protected void onDestroy() {
        super.onDestroy();

    }
}

