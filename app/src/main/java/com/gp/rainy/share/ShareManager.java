package com.gp.rainy.share;

import android.app.Activity;
import android.content.Context;

import com.gp.rainy.Constants;
import com.gp.rainy.MyLogUtil;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;


public class ShareManager {

    private Context mContext;
    private ShareInterface shareInterface;
    private boolean isNetShare = false; //是否网页分享
    private int success = 0; //判断是哪个平台分享的 1:微信 2:微信朋友圈 3:QQ 4:短信 5:QQ空间
    private static String TAG = ShareManager.class.getSimpleName();

    private UMShareListener umShareListener = new UMShareListener() {

        @Override
        public void onStart(SHARE_MEDIA share_media) {
            MyLogUtil.i(TAG + "----onStart--");
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            MyLogUtil.i(TAG + "-----onResult---isNetShare:" + isNetShare + "--platform:" + platform + "--shareInterface:" + shareInterface);
            if (isNetShare) {
                //传递给网页分享成功的状态
                if (platform.name().equals("WEIXIN")) {
                    success = ChannelTypeEnum.WX.get_id();
                } else if (platform.name().equals("WEIXIN_CIRCLE")) {
                    success = ChannelTypeEnum.WX_CIRCLE.get_id();
                } else if (platform.name().equals("QQ")) {
                    success = ChannelTypeEnum.QQ.get_id();
                } else if (platform.name().equals("QZONE")) {
                    success = ChannelTypeEnum.QZONE.get_id();
                } else if (platform.name().equals("微博")) {
                    success = ChannelTypeEnum.Weibo.get_id();
                }
                if (shareInterface != null) {
                    shareInterface.sendShareHandler(1, "", "", Constants.Share, Constants.share, success + "");
                }
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            MyLogUtil.i(TAG + "----onError--" + t);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            if (shareInterface != null) {
                shareInterface.sendShareHandler(0, "-1", "取消分享", Constants.Share, Constants.share);
            }
        }
    };

    public ShareManager(Context mContext) {
        this.mContext = mContext;
    }

    public void setShareListener(ShareInterface share_interface) {
        this.shareInterface = share_interface;
    }

    public ShareInterface getShareInterface() {
        return shareInterface;
    }

    public void performShare(SharePublicAccountModel shareModel, int shareId, boolean isNetShare) {
        this.isNetShare = isNetShare;
        setSharePlatform(shareId, shareModel);
    }

    private void setSharePlatform(int shareId, SharePublicAccountModel shareModel) {
        ShareAction shareAction = null;
        switch (shareId) {
            case 0:
                shareAction = new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.WEIXIN);
                break;
            case 1:
                shareAction = new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case 2:
                shareAction = new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.QQ);
                break;
            case 3:
                shareAction = new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.QZONE);
                break;
            case 4:
                shareAction = new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.SINA);
                break;
            default:
                break;
        }
        setShareParams(shareAction, shareId, shareModel);
    }

    private void setShareParams(ShareAction shareAction, int shareId, SharePublicAccountModel shareModel) {
        MyLogUtil.i( TAG + "---title:" + shareModel.gettitle() + ";content:" + shareModel.getcontent());
        withMedia(shareAction, shareModel)
                .withText(shareModel.getcontent())
                .setCallback(umShareListener)
                .share();
    }

    private ShareAction withMedia(ShareAction shareAction, SharePublicAccountModel shareModel) {
        switch (shareModel.getShareType()) {
            case ShareType.TEXT:
                break;
            case ShareType.IMAGE:
                UMImage image = new UMImage(mContext, shareModel.getimgurl());
                shareAction.withMedia(image);
                break;
            case ShareType.LINK:
                UMImage thumb = new UMImage(mContext, shareModel.getimgurl());
                UMWeb web = new UMWeb(shareModel.geturl());
                web.setTitle(shareModel.gettitle());
                web.setThumb(thumb);
                web.setDescription(shareModel.getcontent());
                shareAction.withMedia(web);
                break;
            case ShareType.VIDEO:
                break;
            default:
                break;
        }
        return shareAction;
    }
}