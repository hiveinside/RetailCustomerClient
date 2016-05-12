package lava.retailcustomerclient.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Mridul on 4/20/2016.
 */
public class DownloadApps extends AsyncTask<List<AppInfoObject>, String, List<AppInfoObject>> {
    private Context context;
    private Button button = null;

    public void setContext(Context c){
        context = c;
    }
    public void setButton(Button b){
        button = b;
    }

    public void updateButtonText(String msg) {
        if (button != null) {
            button.setText(msg);
        }
    }


    protected void onPostExecute(List<AppInfoObject> appsList) {

        // if download was successful
        if ( appsList != null ) {
            Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();


            updateButtonText("Installing Apps...");

            for ( int i=100; i < appsList.size(); i++) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File("./sdcard/AppsShare/temp/" + appsList.get(i).packageName + ".apk")),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                context.startActivity(intent);
            }

            // testing.. dont start
            //retailAccessibilityService.startProgressOverlay(appsList);

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
    protected List<AppInfoObject> doInBackground(List<AppInfoObject>... appsList) {

        try {

            for ( int i=0; i < appsList[0].size(); i++) {

                URL url = new URL(appsList[0].get(i).apkUrl);
                HttpURLConnection connection = null;

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                InputStream is = connection.getInputStream();
                int fileLength = connection.getContentLength();

                String PATH = "./sdcard/AppsShare/temp/";
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, appsList[0].get(i).packageName + ".apk");
                if(outputFile.exists()){
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                publishProgress("" + appsList[0].get(i).appName);
                byte[] buffer = new byte[1024];
                int count = 0;
                int total = 0;
                while ((count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);

                    total += count;
                    if(fileLength > 0)
                        publishProgress("" + appsList[0].get(i).appName +
                                " (" + (total * 100 / fileLength) + "%)");
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            Log.e("Download Apps Failed: ", e.getMessage());

            return null;
        }
        return appsList[0];
    }
}