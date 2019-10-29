package com.gp.rainy.Biometric;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


/**
 * 生物识别管理类
 *
 * @author wdp
 */
public class BiometricPromptManager {

    private static final String TAG = "BiometricPromptManager";
    private IBiometricPrompt mImpl;
    private AppCompatActivity mActivity;

    public interface OnBiometricIdentifyCallback {
        /**
         * 使用密码审批
         */
        void onUsePassword();

        /**
         * 成功
         */
        void onSucceeded();

        /**
         * 认证失败，可能情况是手指移动太快、指纹感应器异常
         */
        void onFailed();

        /**
         * 认证错误，错误的手指
         *
         * @param code
         * @param reason
         */
        void onError(int code, String reason);

        /**
         * 点击cancel键
         */
        void onCancel();
    }

    public static BiometricPromptManager from(AppCompatActivity activity) {
        return new BiometricPromptManager(activity);
    }

    public BiometricPromptManager(AppCompatActivity activity) {
        mActivity = activity;
        if (isAboveApi23()) {
            mImpl = new BiometricPromptApi23Impl(activity);
        }
    }

    public boolean isAboveApi23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public void authenticate(@NonNull OnBiometricIdentifyCallback callback) {
        mImpl.authenticate(new CancellationSignal(), callback);
    }

    public void authenticate(@NonNull CancellationSignal cancel,
                             @NonNull OnBiometricIdentifyCallback callback) {
        mImpl.authenticate(cancel, callback);
    }

    /**
     * 是否设置了指纹
     *
     * @return
     */
    public boolean hasEnrolledFingerprints() {
        if (isAboveApi23()) {
            return ((BiometricPromptApi23Impl) mImpl).hasEnrolledFingerprints();
        } else {
            return false;
        }
    }

    /**
     * 硬件是否支持指纹
     *
     * @return
     */
    public boolean isHardwareDetected() {
        if (isAboveApi23()) {
            return ((BiometricPromptApi23Impl) mImpl).isHardwareDetected();
        } else {
            return false;
        }
    }

    /**
     * 是否设置了锁屏
     *
     * @return
     */
    public boolean isKeyguardSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) mActivity.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            return true;
        }

        return false;
    }

    /**
     * 是否符合指纹使用条件
     *
     * @return
     */
    public boolean isBiometricPromptEnable() {
        return isAboveApi23()
                && isHardwareDetected()
                && hasEnrolledFingerprints()
                && isKeyguardSecure();
    }

    /**
     * 是否显示忘记密码按钮，必须放在authenticate方法后调用
     *
     * @param isShow
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public void isShowPasswordView(boolean isShow) {
        if (mImpl instanceof BiometricPromptApi23Impl) {
            ((BiometricPromptApi23Impl) mImpl).hiddenPasswordView(isShow);
        }
    }
}
