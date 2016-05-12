package lava.retailcustomerclient.utils;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lava.retailcustomerclient.ui.CustomerKitActivity;

/**
 * Created by Mridul on 5/11/2016.
 */
public class AppDownloader {

    private static final String TAG = "ClientAppDownloader";
    private static String APK_DIR = "";

    CustomerKitActivity activity;
    List<AppInfoObject> downloadList;

    public AppDownloader(CustomerKitActivity activity) {
        this.activity = activity;
    }

    public void download(String ROOT_DIR, List<AppInfoObject> downloadList) {

        this.downloadList = downloadList;
        APK_DIR = ROOT_DIR + "/apks/";

        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(mFileDownloadListener);

        final List<BaseDownloadTask> tasks = new ArrayList<>();

        // Doing 3 installs only for quick testig
        for (int i = 10; i < downloadList.size(); i++) {
            final String link = downloadList.get(i).apkUrl;
            final String destFile = APK_DIR + downloadList.get(i).packageName + ".apk"; // dont add .apk till completed + ".apk";
            Log.d(TAG, "download link: " + link + " " + destFile);

            tasks.add(FileDownloader.getImpl().create(link).setPath(destFile).setListener(mFileDownloadListener).setTag(downloadList.get(i)));
        }
        // do not want each task's download progress's callback,
        //queueSet.disableCallbackProgressTimes();

        // we just consider which task will completed.
        // auto retry 1 time if download fail
        queueSet.setAutoRetryTimes(1);

        // start download in serial order
        // // TODO: 5/11/2016 check this.. direct connect is fast. may want to do in parallel 
        queueSet.downloadSequentially(tasks);

        queueSet.start();
    }

    final FileDownloadListener mFileDownloadListener = new FileDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Log.d(TAG, "download pending : " + task.getPath() + " sofar " + soFarBytes + " TotalBytes : " + totalBytes);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            //Log.d(TAG, "download progress : " + task.getPath() + " sofar " + soFarBytes + " TotalBytes : " + totalBytes);
            activity.onApkDownloadProgress(task, soFarBytes, totalBytes);

        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {
            Log.d(TAG, "download blockComplete : " + task.getPath());
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            Log.d(TAG, "download completed : " + task.getPath());

            // match checksum, delete if doesnt match
            // match with what was sent from cloud
            // // TODO: 5/11/2016 match checksum.. delete file if not matched 
            // match checksum

            // refresh UI
            activity.onApkDownloadCompleted(task);
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Log.d(TAG, "download paused : " + task.getPath() + " sofar " + soFarBytes + " TotalBytes : " + totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            Log.d(TAG, "download error : " + task.getPath() + " Exception " + e);

            // refresh UI
            activity.onApkDownloadError(task);

            // pause/stop rest of the queue
            FileDownloader.getImpl().pause(mFileDownloadListener);
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            Log.d(TAG, "download warn : " + task.getPath());
        }
    };

    public interface AppDownloadCallback {
        public void onApkDownloadCompleted(BaseDownloadTask task);

        public void onApkDownloadError(BaseDownloadTask task);

        public void onApkDownloadProgress(BaseDownloadTask task, int soFarBytes, int totalBytes);
    }
}
