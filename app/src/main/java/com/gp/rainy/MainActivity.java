package com.gp.rainy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gp.rainy.fingerprint.FingerPrintActivity;
import com.gp.rainy.location.ILocation;
import com.gp.rainy.location.LocationManager;
import com.gp.rainy.location.LocationPresenter;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.toWebview).setOnClickListener(listener);
        findViewById(R.id.location).setOnClickListener(listener);
        findViewById(R.id.writeToSDCard).setOnClickListener(listener);
        findViewById(R.id.readSDCardFile).setOnClickListener(listener);

        findViewById(R.id.writeToInnerFile).setOnClickListener(listener);
        findViewById(R.id.readInnerFile).setOnClickListener(listener);

        findViewById(R.id.download).setOnClickListener(listener);
        findViewById(R.id.finger).setOnClickListener(listener);
        findViewById(R.id.media).setOnClickListener(listener);
        findViewById(R.id.netmedia).setOnClickListener(listener);
        findViewById(R.id.getpackage).setOnClickListener(listener);

        if (requestStoagePermission()) {
            FileUtils.createProjectSdcardFile();
        }
        LocationManager.checkLocationPermission(this);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.toWebview:
                    startActivity(new Intent(MainActivity.this, WebViewActivity.class));
                    break;
                case R.id.location:
                    LocationPresenter lp = new LocationPresenter(MainActivity.this, new ILocation() {
                        @Override
                        public void locationSuccess(String lat, String lon, String location) {
                            Log.d(TAG, "location-lat:" + lat + "-lon:" + lon + "-address:" + location);
                        }

                        @Override
                        public void locationFailed() {
                            Log.d(TAG, "location failed");
                        }
                    });
                    lp.doLocation();
                    break;
                case R.id.writeToSDCard:
                    if (writeToSDCardFile("aa", "bb")) {
                        Toast.makeText(MainActivity.this, "file success", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.readSDCardFile:
                    String content = readSDCardFile("aa");
                    if (!TextUtils.isEmpty(content)) {
                        Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.writeToInnerFile:
                    if (writeToInnerFile("cc", "dd")) {
                        Toast.makeText(MainActivity.this, "file success", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.readInnerFile:
                    String content1 = readInnerFile("cc");
                    if (!TextUtils.isEmpty(content1)) {
                        Toast.makeText(MainActivity.this, content1, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.download:
                    String ss = "http://img2.cache.netease.com/photo/0001/2017-04-28/CJ45TBS419BR0001.jpg";//http://test.bjyishubiyeji.com:9013/jssdk.zip
                    FileUtils.download(ss, "");
                    break;
                case R.id.finger:
                    Intent intent = new Intent(MainActivity.this, FingerPrintActivity.class);
                    intent.putExtra("type", "clear");
                    startActivity(intent);
                    break;
                case R.id.media:
                    AudioManager.getInstance(MainActivity.this).playeLocalMedia("zjsvoice112.mp3");
                    break;
                case R.id.netmedia:
                    AudioManager.getInstance(MainActivity.this).playeNetMedia("http://img2.cache.netease.com/photo/0001/2017-04-28/CJ45TBS419BR0001.jpg");
                    break;
                case R.id.getpackage:
                    DeviceUtil.loadApps(MainActivity.this);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取手机Android 版本（4.4、5.0、5.1 ...）
     *
     * @return
     */
    public static String getBuildVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机Android API等级（22、23 ...）
     *
     * @return
     */
    public static int getBuildLevel() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 判断网络是否可用
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        assert cm != null;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public void saveMessage(String key, String value) {
        PreferenceUtils.setPreferenceString(this, key, value);
    }

    public void getMessage(String key) {
        PreferenceUtils.getPreferenceString(this, key, "");
    }

    //sdcard 目录
    public boolean writeToSDCardFile(String fileName, String Content) {
        boolean isSuccess = FileUtils.writeSDCardFile(Environment.getExternalStorageDirectory() + Constants.DIR_DOWNLOAD + fileName, Content, true);
        return isSuccess;
    }

    public String readSDCardFile(String fileName) {
        String content = FileUtils.readFile(Environment.getExternalStorageDirectory() + Constants.DIR_DOWNLOAD + fileName);
        return content;
    }

    public boolean writeToInnerFile(String fileName, String Content) {
        boolean isSuccess = FileUtils.writeInnerFile( this, Content, fileName);
        return isSuccess;
    }

    public String readInnerFile(String fileName) {
        String content = FileUtils.readInnerFile(this, fileName);
        return content;
    }

    public boolean requestStoagePermission() {
        boolean isPermissionAllow = false;
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, Constants.STORAGE_PERMISSION_REQ_CODE);
        } else {
            isPermissionAllow = true;
        }
        return isPermissionAllow;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.LOCATION_PERMISSION_REQ_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "permission sucess", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission Denied
//                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.STORAGE_PERMISSION_REQ_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FileUtils.createProjectSdcardFile();
                } else {
                    Toast.makeText(this, "需要开启存储权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public class MyTask extends AsyncTask<String, Integer, String>{

        private Context context;
        private MaterialDialog mProgressDialog;

        public MyTask(Context context) {
            super();
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = MaterialDialogUtil.showProgress(context, context.getString(R.string.downloading));
            mProgressDialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... params) {
//            int i = 0;
//            publishProgress(i);
            FileUtils.download(params[0], "");
            return "ok";
        }

        @Override
        protected void onProgressUpdate (Integer... progress) {
            super.onProgressUpdate(progress);
//            mProgressDialog.setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if ("ok".equals(result)) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

}
