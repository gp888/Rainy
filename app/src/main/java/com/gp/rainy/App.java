package com.gp.rainy;

import android.support.multidex.MultiDexApplication;

public class App extends MultiDexApplication {

    public static App globalContext;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
    }
}
