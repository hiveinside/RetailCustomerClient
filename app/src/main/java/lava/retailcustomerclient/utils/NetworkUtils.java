package lava.retailcustomerclient.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.v4.BuildConfig;

/**
 * Created by saurabh on 6/19/16.
 */
public class NetworkUtils {

    public static void forceWifi(Context context) {

        Context ctx = context.getApplicationContext();

        final ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

            //TODO : do something here


        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ||
                Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {


            Network[] network = connectivityManager.getAllNetworks();
            if(network != null && network.length >0 ){
                for(int i = 0 ; i < network.length ; i++){
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network[i]);
                    int networkType = networkInfo.getType();
                    if(ConnectivityManager.TYPE_WIFI == networkType ){
                        connectivityManager.setProcessDefaultNetwork(network[i]);
                        //connectivityManager.bindProcessToNetwork(network[i]);
                    }
                }
            }

        } else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {

            Network[] network = connectivityManager.getAllNetworks();
            if(network != null && network.length >0 ){
                for(int i = 0 ; i < network.length ; i++){
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network[i]);
                    int networkType = networkInfo.getType();
                    if(ConnectivityManager.TYPE_WIFI == networkType ){
                        connectivityManager.bindProcessToNetwork(network[i]);
                    }
                }
            }




        }


    }
}
