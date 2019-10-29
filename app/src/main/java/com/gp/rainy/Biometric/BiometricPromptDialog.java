package com.gp.rainy.Biometric;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gp.rainy.R;


/**
 * 指纹验证Dialog
 */
public class BiometricPromptDialog extends DialogFragment {

    /**
     * 正常状态
     */
    public static final int STATE_NORMAL = 1;
    /**
     * 指纹不匹配
     */
    public static final int STATE_FAILED = 2;
    /**
     * 指纹验证超过次数
     */
    public static final int STATE_ERROR = 3;
    /**
     * 验证成功
     */
    public static final int STATE_SUCCEED = 4;
    private ImageView ivFingerprint;
    private TextView tvApprovalPassword;
    private RelativeLayout rlClose;
    private TextView tvPrompt;
    private AppCompatActivity mActivity;
    private OnBiometricPromptDialogActionCallback mDialogActionCallback;

    private boolean isShowPasswordView;

    public interface OnBiometricPromptDialogActionCallback {
        void onDialogDismiss();

        void onApprovalPassword();

        void onCancel();
    }

    public static BiometricPromptDialog newInstance() {
        BiometricPromptDialog dialog = new BiometricPromptDialog();
        return dialog;
    }

    public void setOnBiometricPromptDialogActionCallback(OnBiometricPromptDialogActionCallback callback) {
        mDialogActionCallback = callback;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupWindow(getDialog().getWindow());
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_biometric_prompt_dialog, container);

        RelativeLayout rootView = view.findViewById(R.id.root_view);
        rootView.setClickable(false);

        ivFingerprint = view.findViewById(R.id.iv_fingerprint);
        tvApprovalPassword = view.findViewById(R.id.tv_approval_password);
        rlClose = view.findViewById(R.id.rl_close);
        tvPrompt = view.findViewById(R.id.tv_prompt);

        tvApprovalPassword.setVisibility(View.GONE);
        tvApprovalPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDialogActionCallback != null) {
                    mDialogActionCallback.onApprovalPassword();
                }

                dismiss();
            }
        });
        rlClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDialogActionCallback != null) {
                    mDialogActionCallback.onCancel();
                }
                dismiss();
            }
        });
        ivFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog);
        }
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mDialogActionCallback != null) {
            mDialogActionCallback.onDialogDismiss();
        }
    }

    private void setupWindow(Window window) {
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.dimAmount = 0;
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(lp);
            window.setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public void setState(int state) {
        switch (state) {
            case STATE_NORMAL:
                rlClose.setVisibility(View.VISIBLE);
                break;
            case STATE_FAILED:
                Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_animation);
                tvPrompt.setText(mActivity.getString(R.string.fingerprint_prompt_failed));
                rlClose.setVisibility(View.VISIBLE);
                tvPrompt.startAnimation(shake);
                break;
            case STATE_ERROR:
                tvPrompt.setText(mActivity.getString(R.string.fingerprint_prompt_error));
                rlClose.setVisibility(View.GONE);

                tvPrompt.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mActivity.isFinishing()) {
                            dismiss();
                        }
                    }
                }, 500);
                break;
            case STATE_SUCCEED:
                tvPrompt.setText(mActivity.getString(R.string.fingerprint_prompt_success));
                rlClose.setVisibility(View.VISIBLE);

                tvPrompt.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mActivity.isFinishing()) {
                            dismiss();
                        }
                    }
                }, 500);
                break;
            default:
                break;
        }
    }

    public void hideenPasswordView(boolean isShow) {
        isShowPasswordView = isShow;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isShowPasswordView) {
            tvApprovalPassword.setVisibility(View.VISIBLE);
        } else {
            tvApprovalPassword.setVisibility(View.GONE);
        }
    }
}
