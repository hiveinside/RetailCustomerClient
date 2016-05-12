package lava.retailcustomerclient.ui;

import android.app.Application;

import com.liulishuo.filedownloader.FileDownloader;

/**
 * Created by Mridul on 5/11/2016.
 */
public class CustomerKitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FileDownloader.init(getApplicationContext());
    }
}
