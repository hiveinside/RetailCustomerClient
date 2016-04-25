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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mridul on 4/20/2016.
 */
public class GetAppsList extends AsyncTask<Void, Void, List<AppInfoObject>> {

    private Context context;
    List<AppInfoObject> myAppsList;

    public void setContext(Context c){
        context = c;
    }

    void ShowToast (String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    protected void onPostExecute(List<AppInfoObject> list) {


        // ShowToast(message);
        // Grid view
        if (list == null) {
            return;
        }

        ShowToast("Found " + myAppsList.size() + " apps");
    }

    @Override
    protected List<AppInfoObject> doInBackground(Void... voids) {

        try
        {
            URL appsListURL = new URL ("http://192.168.43.1:8888/?getAppsList");
            String responseText="";

            // Create Request to server and get response
            HttpURLConnection connection= (HttpURLConnection) appsListURL.openConnection();

            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream is = connection.getInputStream();
            responseText = IOUtils.toString(is);

            Type listType = new TypeToken<ArrayList<AppInfoObject>>() {}.getType();
            Gson gson = new Gson();
            myAppsList = gson.fromJson(responseText, listType);

        } catch(Exception ex) {
            Log.e("Download appslist Failed: ", ex.getMessage());

            return null;
        }

        return myAppsList;
    }
}
