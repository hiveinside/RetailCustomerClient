package lava.retailcustomerclient.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created by Mridul on 4/21/2016.
 */
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
        } else {
            Toast.makeText(serviceContext, "Failed to submit data. Process not completed.", Toast.LENGTH_LONG).show();
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
