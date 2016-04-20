package lava.retailcustomerclient.UI;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import lava.retailcustomerclient.R;
import lava.retailcustomerclient.SERVICES.RetailAccessibilityService;
import lava.retailcustomerclient.UTILS.AppInfoObject;
import lava.retailcustomerclient.UTILS.DeviceInfoObject;
import lava.retailcustomerclient.UTILS.DownloadApps;
import lava.retailcustomerclient.UTILS.GetAppsList;
import lava.retailcustomerclient.UTILS.phoneUtils;


public class CustomerKitActivity extends Activity {

    private List<AppInfoObject> appsList;
    private static final String accessibilityServiceId = "lava.retailcustomerclient/.SERVICES.RetailAccessibilityService";
    private phoneUtils u = null;

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
        startProcess();
    }

    private void startProcess() {

        setContentView(R.layout.activity_first);
        u = new phoneUtils(this);

        if (u.isAccessibilityEnabled(this, accessibilityServiceId) == false) {

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


           // if(currentSSID.compareToIgnoreCase("RetailFreeWiFi"))
            if (currentSSID.equals("\"RetailFreeWiFi\"")) {

                setContentView(R.layout.activity_main);
                retailAccessibilityService = RetailAccessibilityService.getSharedInstance();

                TextView statusText = (TextView)findViewById(R.id.statusText);
                statusText.setText("Connected to: " + currentSSID);

                // get apps list from Promoter's phone
                GetAppsList g = new GetAppsList();
                g.setContext(this);
                try {
                    appsList = g.execute().get();

                    // update grid
                    setGridAdapter();

                } catch (Exception ee) {
                    ((Button)findViewById(R.id.doInstall)).setText("Failed to get appslist");
                }

            } else {

                TextView infoText = (TextView)findViewById(R.id.infoText);
                infoText.setText("Not connected to 'RetailFreeWiFi'");

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
        if (u.isAccessibilityEnabled(this, accessibilityServiceId) == false) {

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

    public void doCompleteProcess(View view) {
        //ShowToast("Install Pressed ..");

        // 1) Download apps
        // 2) Install Apps
        // 3) Collect install data
        // 4) Collect Phone info (IMEI, andoid id, brand, model, Resolution, MAC, timestamp, ...
        // 5) Send to RetailJunction
        // 6) Uninstall Kit



        //Step 1: Download Apps
        //Step 2: Install Apps
        DownloadApps d = new DownloadApps();
        d.setContext(this);
        d.setButton((Button)findViewById(R.id.doInstall));
        d.execute(appsList);

        //Step 3: Collect install Data

        //Step 4: Collect Phone info (IMEI, andoid id, brand, model, Resolution, MAC, timestamp, ...
        DeviceInfoObject devInfo;
        devInfo = u.getDeviceInfo();

        //Step 5: Send to RetailJunction
        SubmitData submitData = new SubmitData();
        submitData.setContext(this);
        submitData.execute(devInfo);

        //Step 6: Uninstall Kit

/*
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:lava.retailcustomerclient"));
        startActivity(intent);
*/

        //stopService(new Intent(getApplication(), ProgressOverlayService.class));
    }






    private class SubmitData extends AsyncTask<DeviceInfoObject, String, Boolean> {
        private Context context;
        public void setContext(Context contextf){
            context = contextf;
        }

        public void updateButtonText(String msg) {
            Button button = (Button)findViewById(R.id.doInstall);
            if (button != null)
                button.setText(msg);
        }

        protected void onPostExecute(Boolean result) {

            // if download was successful
            if ( result == true ) {
                updateButtonText("Done. Press to uninstall kit.");
            } else {
                updateButtonText("Failed to Submit data");
            }
        }

        public String toJson (DeviceInfoObject d) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            return gson.toJson(d);
        }

        @Override
        protected Boolean doInBackground(DeviceInfoObject... devInfo) {

            try {
                String URL = "http://192.168.43.1:8080/?submitCustData";

                HttpPost httpPost = new HttpPost(URL);
                HttpClient client = new DefaultHttpClient();
                HttpParams params = new BasicHttpParams();

                ArrayList<NameValuePair> pair = new ArrayList<NameValuePair>();
                pair.add(new BasicNameValuePair("data", toJson(devInfo[0])));

                httpPost.setEntity(new UrlEncodedFormEntity(pair));

                HttpResponse response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();

            } catch (Exception e) {
                Log.e("Submit data Failed: ", e.getMessage());

                return false;
            }
            return true;
        }
    }
}

