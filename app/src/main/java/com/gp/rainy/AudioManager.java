package com.gp.rainy;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.gp.rainy.utils.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;

public class AudioManager {

    private static String TAG = AudioManager.class.getSimpleName();
    protected MediaPlayer mediaPlayer;
    public static AudioManager instance = null;
    private Context mContext;
    private mediaPlayerListener mediaPlayer_Listener;

    public synchronized static AudioManager getInstance(Context mContext) {
        if (instance == null) {
            instance = new AudioManager(mContext);
        }
        return instance;
    }

    private AudioManager(Context mContext) {
        this.mContext = mContext;
        mediaPlayer = App.mediaPlayer;
    }

    public void Stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void playeNetMedia(String audioPath) {
        if (!audioPath.startsWith("http")) {
            return;
        }
        final String fileName = audioPath.substring(audioPath.lastIndexOf("/"), audioPath.length());
        File voiceFile = new File(Constants.DIR_MEDIA + fileName);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    return;
                }
            }
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        // 调用download方法开始下载
        if (!voiceFile.exists()) {
            OkHttpUtils
                    .get()
                    .url(audioPath)
                    .build()
                    .execute(new FileCallBack(Constants.DIR_MEDIA, fileName){

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLogUtil.e(TAG + "onError :" + e.getMessage());
                            ToastUtil.showToastShort("语音下载失败");
                        }

                        @Override
                        public void onResponse(File response, int id) {
                            MyLogUtil.e(TAG + "onResponse :" + response.getAbsolutePath());
                            mediaPlaye(fileName);
                        }
                    });
        } else {
            try {
                mediaPlaye(fileName);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.showToastShort("播放失败");
            }
        }
    }

    public void playeLocalMedia(String audioFileName) {
        if (audioFileName.startsWith("http")) {
            return;
        }
        try {
            setAudioMaxVolumn();
            audioFileName = "sound/" + audioFileName;
            AssetManager assetManager = mContext.getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd(audioFileName);
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        return;
                    }
                }
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            mediaPlaye(fileDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playMedia(String file, String url, int type, WebViewManager webViewManager) {
        mediaPlaye(file, url, type, webViewManager);
    }

    /***
     * assets媒体
     **/
    private void mediaPlaye(AssetFileDescriptor fileDescriptor) {
        try {
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastShort("播放失败");
        }
    }

    /***
     * 媒体播放
     *
     * @param file
     **/
    private void mediaPlaye(String file, String url, int type, WebViewManager webViewManager) {
        Bundle bundle = new Bundle();
        try {
            if (0 == type) {
                Uri uri = Uri.parse(url);
                mediaPlayer.setDataSource(mContext, uri);
            } else if (1 ==type){
                mediaPlayer.setDataSource(file);
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                }
            });
            bundle.putInt("success", 1);
            webViewManager.sendHandler(1, "", "", Constants.PlaySound, Constants.playSound, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastShort("播放失败");
            bundle.putInt("success", 0);
            webViewManager.sendHandler(1, "", "", Constants.PlaySound, Constants.playSound, bundle);
        }
        webViewManager = null;
    }

    /***
     * file媒体播放
     **/
    private void mediaPlaye(String fileName) {
        try {
            mediaPlayer.setDataSource(Constants.DIR_MEDIA + fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                    if (mediaPlayer_Listener != null) {
                        mediaPlayer_Listener.mediaPlayerSucc();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastShort("播放失败");
            if (mediaPlayer_Listener != null) {
                mediaPlayer_Listener.mediaPlayerFailed();
            }
        }
    }

    private void setAudioMaxVolumn() {
        android.media.AudioManager am = (android.media.AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);//实例化AudioManager对象
        int audioMaxVolumn = am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC); //返回当前AudioManager对象的最大音量值
        am.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, audioMaxVolumn, 0);
    }

    /***
     * 设置播放事件
     */
    public void setMediaPlayerListener(mediaPlayerListener mediaPlayer_Listener) {
        this.mediaPlayer_Listener = mediaPlayer_Listener;
    }

    public interface mediaPlayerListener{
        void mediaPlayerStart();
        void mediaPlayerSucc();
        void mediaPlayerFailed();
    }
}
