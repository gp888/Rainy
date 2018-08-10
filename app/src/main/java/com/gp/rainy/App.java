package com.gp.rainy;

import android.media.MediaPlayer;
import android.support.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

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
            initUMAPI();
        }

    }

    private void initUMAPI() {
        /**
         * 初始化UMcommon库
         * 参数1:上下文，不能为空
         * 参数2:【友盟+】 AppKey
         * 参数3:【友盟+】 Channel
         * 参数4:设备类型，UMConfigure.DEVICE_TYPE_PHONE为手机、UMConfigure.DEVICE_TYPE_BOX为盒子，默认为手机
         * 参数5:Push推送业务的secret
         */
        UMConfigure.init(this, null, "c_home", UMConfigure.DEVICE_TYPE_PHONE, null);

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

        if (!DeviceUtil.isApkDebugable()) {
            UMConfigure.setLogEnabled(true);
        }
        MobclickAgent.openActivityDurationTrack(false);
        PlatformConfig.setWeixin(BuildConfig.THIRDPART_WEIXIN_APPID, BuildConfig.THIRDPART_WEIXIN_APPSECRET);
        PlatformConfig.setQQZone(BuildConfig.THIRDPART_QQ_APPID, BuildConfig.THIRDPART_QQ_APPKEY);
        PlatformConfig.setSinaWeibo(BuildConfig.THIRDPART_SINA_APPID, BuildConfig.THIRDPART_SINA_APPSECRET, "http://sns.whalecloud.com");
    }
}
