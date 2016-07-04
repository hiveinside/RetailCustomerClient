package lava.retailcustomerclient.ui;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.crashlytics.android.Crashlytics;
import com.liulishuo.filedownloader.FileDownloader;

import io.fabric.sdk.android.Fabric;
import lava.retailcustomerclient.utils.NetworkUtils;
import lava.retailcustomerclient.utils.ProcessState;

/**
 * Created by Mridul on 5/11/2016.
 */
public class CustomerKitApplication extends Application {

    SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
       // NetworkUtils.forceWifi(this);
        FileDownloader.init(getApplicationContext());
        mSharedPreferences = getSharedPreferences("default", MODE_PRIVATE);
        ProcessState.getInstance();

        Fabric.with(this, new Crashlytics());
    }


    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;

    }

    public static CustomerKitApplication getApplication(Context context) {
        return (CustomerKitApplication) context.getApplicationContext();
    }
}
