package com.gp.rainy;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

public class MaterialDialogUtil {

    public static MaterialDialog showProgress(Context context, int content) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .progress(true, 0)
                .show();
    }

    public static MaterialDialog showProgress(Context context, String content) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .progress(true, 0)
                .show();
    }

    public static MaterialDialog showConfirm(Context context, int title, int content, int agree) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(agree)
                .show();
    }

    public static MaterialDialog showConfirm(Context context, int content, int agree) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .show();
    }

    public static MaterialDialog showConfirm(Context context, String content, int agree) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .show();
    }

    public static MaterialDialog showConfirm(Context context, int content, int agree, MaterialDialog.SingleButtonCallback positive) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .onPositive(positive)
                .show();
    }

    public static MaterialDialog showAlert(Context context, int content, int agree, int disagree, MaterialDialog.SingleButtonCallback positive) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .negativeText(disagree)
                .onPositive(positive)
                .show();
    }

    public static MaterialDialog showAlert(Context context, String content, int agree, int disagree, MaterialDialog.SingleButtonCallback positive) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .negativeText(disagree)
                .onPositive(positive)
                .show();
    }

    public static MaterialDialog showAlert(Context context, String content, String agree, String disagree, MaterialDialog.SingleButtonCallback positive) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .negativeText(disagree)
                .onPositive(positive)
                .show();
    }

    /**
     * 添加取消Callback
     */
    public static MaterialDialog showAlert(Context context, String content, int agree, int disagree, MaterialDialog.SingleButtonCallback positive, MaterialDialog.SingleButtonCallback negative) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .negativeText(disagree)
                .onPositive(positive)
                .onNegative(negative)
                .show();
    }

    public static MaterialDialog showAlert(Context context, int content, int agree, int disagree, MaterialDialog.SingleButtonCallback positive, MaterialDialog.SingleButtonCallback negative) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(agree)
                .negativeText(disagree)
                .onPositive(positive)
                .onNegative(negative)
                .show();
    }

    public static MaterialDialog showList(Context context, int items, MaterialDialog.ListCallback callback) {
        return new MaterialDialog.Builder(context)
                .items(items)
                .itemsCallback(callback)
                .show();
    }

    public static MaterialDialog showList(Context context, String[] items, MaterialDialog.ListCallback callback) {
        return new MaterialDialog.Builder(context)
                .items(items)
                .itemsCallback(callback)
                .show();
    }

    public static MaterialDialog showList(Context context, String[] items, int selectedIndex, MaterialDialog.ListCallbackSingleChoice callback) {
        return new MaterialDialog.Builder(context)
                .items(items)
                .itemsCallbackSingleChoice(selectedIndex, callback)
                .show();
    }
}
