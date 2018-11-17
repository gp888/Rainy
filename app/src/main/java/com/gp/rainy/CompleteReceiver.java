package com.gp.rainy;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.greenrobot.event.EventBus;

import static com.gp.rainy.App.globalContext;

public class CompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.e("new intent ", action.toString());
        if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            return;
        }

        long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        long downloadID = PreferenceUtils.getPreferenceLong(globalContext, Constants.DOWNLOAD_ID, -1);
        if (completeDownloadId != downloadID) {
            return;
        }

        String downloadPath = FileUtils.getDownloadPath(downloadID);
//        AppUpdateUtil.installApp(downloadPath);

        int status = FileUtils.getDownloadStatus(downloadID);
        if (status != DownloadManager.STATUS_SUCCESSFUL) {
            EventBus.getDefault().post(new WebviewEvent(WebviewEvent.TYPE_DOWN, 0, "0"));
        } else {
            EventBus.getDefault().post(new WebviewEvent(WebviewEvent.TYPE_DOWN, 0, "1"));
        }
    }
}

