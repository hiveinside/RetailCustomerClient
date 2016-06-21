package lava.retailcustomerclient.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

import lava.retailcustomerclient.ui.CustomerKitApplication;

/**
 * Created by Mridul on 4/20/2016.
 */
public class GetAppsList extends AsyncTask<Void, Void, AppsListToClientObject> {

    private Context context;
    AppsListToClientObject appsListObj;

    public GetAppsList(Context c) {
        context = c;
    }

    void ShowToast (String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    protected void onPostExecute(AppsListToClientObject list) {

        ProcessState.setState(ProcessState.STATE_DONE_GETTING_APPSLIST);

        if (list == null) {
            return;
        }

        /*
         * save start time to calculate elapsed time since getting appslist
         */
        CustomerKitApplication.getApplication(context).getDefaultSharedPreferences()
                .edit()
                .putLong("START_TIME_MILLIS", System.currentTimeMillis())
                .commit();

        ShowToast("Found " + appsListObj.appsList.size() + " apps");
    }

    @Override
    protected AppsListToClientObject doInBackground(Void... voids) {

        try
        {
            URL appsListURL = new URL (Constants.getAppsListURL);
            String responseText="";

            // Create Request to server and get response
            HttpURLConnection connection= (HttpURLConnection) appsListURL.openConnection();

            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream is = connection.getInputStream();
            responseText = IOUtils.toString(is);

            Type listType = new TypeToken<AppsListToClientObject>() {}.getType();
            Gson gson = new Gson();
            appsListObj = gson.fromJson(responseText, listType);

        } catch(Exception ex) {
            Log.e("Download appslist Failed: ", ex.getMessage());

            return null;
        }

        return appsListObj;
    }
}
