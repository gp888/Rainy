package com.gp.rainy.Biometric;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Android 6.0版本指纹功能方法实现
 *
 * @author wdp
 */
@RequiresApi(Build.VERSION_CODES.M)
public class BiometricPromptApi23Impl implements IBiometricPrompt {

    private AppCompatActivity mActivity;
    private BiometricPromptDialog mDialog;
    private FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private BiometricPromptManager.OnBiometricIdentifyCallback mManagerIdentifyCallback;
    private FingerprintManager.AuthenticationCallback mFmAuthCallback
            = new FingerprintManageCallbackImpl();

    public BiometricPromptApi23Impl(AppCompatActivity activity) {
        mActivity = activity;

        mFingerprintManager = getFingerprintManager(activity);
    }

    @Override
    public void authenticate(@Nullable CancellationSignal cancel,
                             @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback) {
        //指纹识别的回调
        mManagerIdentifyCallback = callback;

        //BiometricPromptDialog.OnBiometricPromptDialogActionCallback是自定义dialog的回调
        mDialog = BiometricPromptDialog.newInstance();
        mDialog.setOnBiometricPromptDialogActionCallback(new BiometricPromptDialog.OnBiometricPromptDialogActionCallback() {
            @Override
            public void onDialogDismiss() {
                //当dialog消失的时候，包括点击userPassword、点击cancel、和识别成功之后
                if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                    mCancellationSignal.cancel();
                }
            }

            @Override
            public void onApprovalPassword() {
                //一些情况下，用户还可以选择使用密码
                if (mManagerIdentifyCallback != null) {
                    mManagerIdentifyCallback.onUsePassword();
                }
            }

            @Override
            public void onCancel() {
                //点击cancel键
                if (mManagerIdentifyCallback != null) {
                    mManagerIdentifyCallback.onCancel();
                }
            }
        });
        mActivity.getSupportFragmentManager().beginTransaction()
                .add(mDialog, mDialog.getClass().getSimpleName()).commitAllowingStateLoss();
        //没有allowstateloss
//        mDialog.show(mActivity.getSupportFragmentManager(), mDialog.getClass().getSimpleName());
        mCancellationSignal = cancel;
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                mDialog.dismiss();
            }
        });

        try {
            //authenticate第一个参数为区分用户，暂传null
            getFingerprintManager(mActivity).authenticate(
                    null, mCancellationSignal,
                    0, mFmAuthCallback, null);
//            CryptoObjectHelper cryptoObjectHelper = new CryptoObjectHelper();
//            getFingerprintManager(mActivity).authenticate(
//                    cryptoObjectHelper.buildCryptoObject(), mCancellationSignal,
//                    0, mFmAuthCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FingerprintManageCallbackImpl extends FingerprintManager.AuthenticationCallback {

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            mDialog.setState(BiometricPromptDialog.STATE_ERROR);
            //errorCode=5是取消，errorCode=7是错误
            if (errorCode == 5) {
                mManagerIdentifyCallback.onCancel();
                return;
            }

            mManagerIdentifyCallback.onError(errorCode, errString.toString());
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            mDialog.setState(BiometricPromptDialog.STATE_FAILED);
            mManagerIdentifyCallback.onFailed();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            mDialog.setState(BiometricPromptDialog.STATE_FAILED);
            mManagerIdentifyCallback.onFailed();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            mDialog.setState(BiometricPromptDialog.STATE_SUCCEED);

            mManagerIdentifyCallback.onSucceeded();
        }
    }

    private FingerprintManager getFingerprintManager(Context context) {
        if (mFingerprintManager == null) {
            mFingerprintManager = context.getSystemService(FingerprintManager.class);
        }
        return mFingerprintManager;
    }

    public boolean isHardwareDetected() {
        return mFingerprintManager != null && mFingerprintManager.isHardwareDetected();
    }

    public boolean hasEnrolledFingerprints() {
        return mFingerprintManager != null && mFingerprintManager.hasEnrolledFingerprints();
    }

    /**
     * 是否显示密码View
     *
     * @param isShow true:显示
     */
    public void hiddenPasswordView(boolean isShow) {
        if (mDialog != null) {
            mDialog.hideenPasswordView(isShow);
        }
    }
}
