package com.gp.rainy;

import android.media.MediaPlayer;
import android.support.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;

public class App extends MultiDexApplication {

    public static App globalContext;
    public static MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;

        if (DeviceUtil.isMainProcess(globalContext)) {
            Stetho.initializeWithDefaults(this);
            mediaPlayer = new MediaPlayer();
        }

    }
}
