package com.gp.rainy.Biometric;

import android.os.CancellationSignal;
import androidx.annotation.NonNull;

/**
 * 指纹验证逻辑接口
 */
interface IBiometricPrompt {

    /**
     * 指纹验证
     *
     * @param cancel
     * @param callback
     */
    void authenticate(@NonNull CancellationSignal cancel,
                      @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback);
}
