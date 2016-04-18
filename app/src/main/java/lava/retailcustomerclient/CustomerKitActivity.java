package lava.retailcustomerclient;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class CustomerKitActivity extends Activity {

    private List<AppInfo> appsList;
    private GridViewAdapter mAdapter;
    private static final String accessibilityServiceId = "lava.retailcustomerclient/.RetailAccessibilityService";
    private Utils u = null;

    private ArcProgress arcProgress = null;
    View mView;
    LayoutInflater inflate;

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
        u = new Utils(this);

        if (u.isAccessibilityEnabled(this, accessibilityServiceId) == false) {

            TextView infoText = (TextView)findViewById(R.id.infoText);
            infoText.setText("Please enable [RetailJunction] service");

            Button button = (Button)findViewById(R.id.install);
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
                new getAppsList().execute();
            } else {

                TextView infoText = (TextView)findViewById(R.id.infoText);
                infoText.setText("Not connected to 'RetailFreeWiFi'");

                Button button = (Button)findViewById(R.id.install);
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


    public void installApps(View view) {
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
        d.execute(appsList);

        //Step 3: Collect install Data

        //Step 4: Collect Phone info (IMEI, andoid id, brand, model, Resolution, MAC, timestamp, ...
        DeviceInfo devInfo;
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


    private void setGridAdapter () {
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


    private class getAppsList extends AsyncTask<String, Void, String> {

        protected void onPostExecute(String message) {


            // ShowToast(message);
            // Grid view
            setGridAdapter();
        }

        @Override
        protected String doInBackground(String... params) {


            String URL = "http://192.168.43.1:8080/?getAppsList";
            Log.e("httpget", URL);
            String responseText="";

            try
            {
                // Create Request to server and get response
              /*  URL url1=new URL(URL);
                HttpURLConnection urlConnection= (HttpURLConnection) url1.openConnection();
                int responseCode=urlConnection.getResponseCode();
                InputStream is=urlConnection.getIn
                byte[] d1=IOUtils.toByteArray(is);
                String str1=IOUtils.toString(is);
                byte[] data=new byte[is.available()];
                String rxString=new String(data);*/
                HttpGet httpget = new HttpGet(URL);
                HttpClient Client = new DefaultHttpClient();
                //ResponseHandler<String> responseHandler = new BasicResponseHandler();


                HttpResponse response = Client.execute(httpget);
                HttpEntity entity = response.getEntity();
                responseText = EntityUtils.toString(entity);

                Type listType = new TypeToken<ArrayList<AppInfo>>() {}.getType();
                Gson gson = new Gson();
                appsList = gson.fromJson(responseText, listType);

            }
            catch(Exception ex)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowToast("Failed");
                    }
                });

            }

            return responseText;
        }
    }


    private class DownloadApps extends AsyncTask<List<AppInfo>, String, List<AppInfo>> {
        private Context context;
        public void setContext(Context contextf){
            context = contextf;
        }

        public void updateButtonText(String msg) {
            Button button = (Button)findViewById(R.id.install);
            button.setText(msg);
        }


        protected void onPostExecute(List<AppInfo> appsList) {

            // if download was successful
            if ( appsList != null ) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();


                updateButtonText("Installing Apps...");

                for ( int i=0; i < appsList.size(); i++) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File("./sdcard/AppsShare/temp/" + appsList.get(i).packageName + ".apk")),
                            "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                    context.startActivity(intent);
                }

                retailAccessibilityService.startProgressOverlay(appsList);

            } else {
                updateButtonText("Failed to download Apps");
            }
        }


        @Override
        protected void onProgressUpdate(String... item) {
            //Toast.makeText(context, "Downloading " + item[0], Toast.LENGTH_SHORT).show();
            updateButtonText("Downloading " + item[0] + "...");
        }

        @Override
        protected List<AppInfo> doInBackground(List<AppInfo>... appsList) {

            try {

                for ( int i=0; i < appsList[0].size(); i++) {

                    HttpGet httpget = new HttpGet(appsList[0].get(i).apkUrl);
                    HttpClient Client = new DefaultHttpClient();
                    HttpResponse response = Client.execute(httpget);
                    HttpEntity entity = response.getEntity();
                    InputStream is = response.getEntity().getContent();


                    String PATH = "./sdcard/AppsShare/temp/";
                    File file = new File(PATH);
                    file.mkdirs();
                    File outputFile = new File(file, appsList[0].get(i).packageName + ".apk");
                    if(outputFile.exists()){
                        outputFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(outputFile);

                    publishProgress("" + appsList[0].get(i).AppName);
                    byte[] buffer = new byte[1024];
                    int len1 = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    is.close();
                }
            } catch (Exception e) {
                Log.e("DnldApps Failed: ", e.getMessage());

                return null;
            }
            return appsList[0];
        }
    }


    private class SubmitData extends AsyncTask<DeviceInfo, String, Boolean> {
        private Context context;
        public void setContext(Context contextf){
            context = contextf;
        }

        public void updateButtonText(String msg) {
            Button button = (Button)findViewById(R.id.install);
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

        public String toJson (DeviceInfo d) {

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            return gson.toJson(d);
        }

        @Override
        protected Boolean doInBackground(DeviceInfo... devInfo) {

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

