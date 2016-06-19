package lava.retailcustomerclient.ui;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.liulishuo.filedownloader.FileDownloader;

import lava.retailcustomerclient.utils.NetworkUtils;
import lava.retailcustomerclient.utils.ProcessState;

/**
 * Created by Mridul on 5/11/2016.
 */
public class CustomerKitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkUtils.forceWifi(this);
        FileDownloader.init(getApplicationContext());

        ProcessState.getInstance();
    }
}
