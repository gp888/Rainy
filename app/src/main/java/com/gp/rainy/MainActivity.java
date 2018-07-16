package com.gp.rainy;

import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.toWebview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WebViewActivity.class));
            }
        });

    }


    private void vibrateShort() {
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }

    private void vibrateLong(){
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        long[] patter = {1000, 1000, 1000, 1000};
        vibrator.vibrate(patter, 0);
    }

    private void stopVibrate() {
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        assert vibrator != null;
        vibrator.cancel();
    }
}
