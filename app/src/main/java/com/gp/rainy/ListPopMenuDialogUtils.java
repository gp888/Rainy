package com.gp.rainy;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import static com.gp.rainy.MaterialDialogUtil.showList;

public class ListPopMenuDialogUtils {

    Context mContext;

    public ListPopMenuDialogUtils(Context mContext) {
        this.mContext = mContext;
    }

    InterfaceItemClick interface_instract;

    public interface InterfaceItemClick {
        void itemclick(int position, Object obj);
    }

    public interface PhotoItemClick {
        void itemclick(int position);
    }

    public void setInterfaceItemClick(InterfaceItemClick itemclick) {
        interface_instract = itemclick;
    }

    /**
     * 图片选择
     **/
    public void showMeSelectPhotoDialog() {
        String[] options = new String[]{"立即拍照", "从本地相册选取"};
        showList(mContext, options, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                if (which == 0) {
                    DeviceUtil.openCamera(mContext);
                } else if (which == 1) {
                    DeviceUtil.openPhotos(mContext);
                }
            }
        });
    }

    /**
     * 图片选择
     **/
    public void showMeSelectCirclePhotoDialog() {
        String[] options = new String[]{"立即拍照", "照片"};
        showList(mContext, options, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                if (which == 0) {
                    DeviceUtil.openCamera(mContext);
                } else if (which == 1) {
                    if (interface_instract != null) {
                        interface_instract.itemclick(which, -1);
                    }
                }
            }
        });
    }

    /**
     * 图片选择
     **/
    public void showWebviewSelectPhotoDialog(final PhotoItemClick photoItemClick) {
        String[] options = new String[]{"立即拍照", "从本地相册选取", "取消"};
        MaterialDialog webViewSelectPhotoDialog = showList(mContext, options, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                if (which == 0) {
                    DeviceUtil.openCamera(mContext);
                } else if (which == 1) {
                    DeviceUtil.openPhotos(mContext);
                }
                if (photoItemClick != null) {
                    photoItemClick.itemclick(which);
                }
            }
        });
        webViewSelectPhotoDialog.setCancelable(false);
    }

}
