package com.gp.rainy.utils;

import android.annotation.TargetApi;
        import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
        import android.os.Build;
        import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
        import android.view.WindowManager;

import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.reflect.Field;
        import java.lang.reflect.Method;


public class StatusBarUtil {
    public final static int TYPE_MIUI = 0;
    public final static int TYPE_FLYME = 1;
    public final static int TYPE_M = 3;//6.0

    @IntDef({TYPE_MIUI,
            TYPE_FLYME,
            TYPE_M})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     * @param colorId 颜色
     */
    public static void  setStatusBarColor(Activity activity, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(colorId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTintManager,需要先将状态栏设置为透明
            setTranslucentStatus(activity);
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
            systemBarTintManager.setStatusBarTintEnabled(true);//显示状态栏
            systemBarTintManager.setStatusBarTintColor(colorId);//设置状态栏颜色
        }
    }

    /**
     * 设置状态栏透明
     */
    @TargetApi(19)
    public static void setTranslucentStatus(Activity activity) {
        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //清除透明状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //设置状态栏颜色必须添加
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);//设置透明
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //19
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置沉浸式状态栏
     *
     * @param fontIconDark 状态栏字体和图标颜色是否为深色
     */
    public static void setImmersiveStatusBar(Activity activity,boolean fontIconDark) {
        setTranslucentStatus(activity);
        if (fontIconDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setStatusBarFontIconDark(activity,TYPE_M);
            } else if (OSUtils.isMiui()) {
                setStatusBarFontIconDark(activity,TYPE_MIUI);
            } else if (OSUtils.isFlyme()) {
                setStatusBarFontIconDark(activity,TYPE_FLYME);
            } else {//其他情况下我们将状态栏设置为灰色，就不会看不见字体
                setStatusBarColor(activity,Color.LTGRAY);//灰色
            }
        }
    }

    /**
     * 设置文字颜色
     */
    public static void setStatusBarFontIconDark(Activity activity,@ViewType int type) {
        switch (type) {
            case TYPE_MIUI:
                setMiuiUI(activity,true);
                break;
            case TYPE_M:
                setCommonUI(activity);
                break;
            case TYPE_FLYME:
                setFlymeUI(activity,true);
                break;
        }
    }

    //设置6.0的字体
    public static void setCommonUI(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    //设置Flyme的字体
    public static void setFlymeUI(Activity activity,boolean dark) {
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置MIUI字体
    public static void setMiuiUI(Activity activity,boolean dark) {
        try {
            Window window = activity.getWindow();
            Class clazz = activity.getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gpStatus(Activity activity, int colorId) {
        Window window = activity.getWindow();
        //API 版本大于23时，安卓6.0以上 状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//API level 最小为23,
            window.getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(colorId);
        }

        //安卓 API版本大于 21 安卓5.0以上 状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(colorId);

            ViewGroup mContentView = window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null){
                ViewCompat.setFitsSystemWindows(mChildView,true);//是否为系统 View 预留出空间, 当设置为 true 时,会预留出状态栏的空间
            }

        }
        //安卓 API版本大于 19 安卓 4.4以上 状态栏颜色修改支持的最低版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT <Build.VERSION_CODES.LOLLIPOP){
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup mContentView = window.findViewById(Window.ID_ANDROID_CONTENT);
            View statusBarView = mContentView.getChildAt(0);
            int statusBarHeight = getStatusBarHeight(activity);
            if (statusBarView != null && statusBarView.getLayoutParams()!=null&& statusBarView.getLayoutParams().height == statusBarHeight){
                //避免添加重复View
                statusBarView.setBackgroundColor(Color.RED);
                return;
            }
            if (statusBarView != null){
                ViewCompat.setFitsSystemWindows(statusBarView,true);
            }
            statusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,statusBarHeight);
            statusBarView.setBackgroundColor(colorId);
            mContentView.addView(statusBarView,0,lp);
        }
    }

    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
