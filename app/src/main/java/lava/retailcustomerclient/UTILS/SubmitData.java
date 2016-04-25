package lava.retailcustomerclient.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by Mridul on 4/21/2016.
 */
public class SubmitData extends AsyncTask<DeviceInfoObject, String, Boolean> {
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

    protected void onPostExecute(Boolean result) {

        // if download was successful
        if ( result == true ) {
            Toast.makeText(context,"Process Complete", Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(context,"Error in submitting data", Toast.LENGTH_SHORT);
        }
        updateButtonText("Done. Press to uninstall kit.");
    }

    public String toJson (DeviceInfoObject d) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(d);
    }

    @Override
    protected Boolean doInBackground(DeviceInfoObject... devInfo) {

        try {

            String URL = "http://192.168.43.1:8888/"; // ?submitCustData will be dont in params

            HttpPost httpPost = new HttpPost(URL);
            HttpClient client = new DefaultHttpClient();

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

/*
            JSONObject obj = new JSONObject();
            obj.put("submitCustData", toJson(devInfo[0]));
            Log.e("submitCustData", toJson(devInfo[0]));

*/
            httpPost.setEntity(new StringEntity(toJson(devInfo[0]), "UTF-8"));


            HttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 200){
                return false;
            }
            HttpEntity entity = response.getEntity();

            Log.e("submitCustData", entity.getContent().toString());


        } catch (Exception e) {
            Log.e("Submit data Failed: ", e.getMessage());

            return false;
        }
        return true;
    }
}
