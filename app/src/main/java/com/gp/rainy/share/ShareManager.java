package com.gp.rainy.share;

import android.app.Activity;
import android.content.Context;

import com.google.gson.JsonObject;
import com.gp.rainy.Constants;
import com.gp.rainy.MyLogUtil;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;


public class ShareManager {

    private Context mContext;

    private ShareInterface share_interface;

    private boolean isNetShare = false; //是否网页分享
    private int success = 0; //判断是哪个平台分享的 1:微信 2:微信朋友圈 3:QQ 4:短信 5:QQ空间
    private UMShareListener umShareListener = new UMShareListener() {

        @Override
        public void onStart(SHARE_MEDIA share_media) {
            MyLogUtil.i("ShareManager----onStart--");
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            MyLogUtil.i("ShareManager-----onResult---isNetShare:" + isNetShare + "--platform:" + platform + "--share_interface:" + share_interface);
            if (isNetShare) {
                //传递给网页分享成功的状态
                if (platform.name().equals("WEIXIN")) {
                    success = ChannelTypeEnum.WX.get_id();
                } else if (platform.name().equals("WEIXIN_CIRCLE")) {
                    success = ChannelTypeEnum.WX_CIRCLE.get_id();
                } else if (platform.name().equals("QQ")) {
                    success = ChannelTypeEnum.QQ.get_id();
                } else if (platform.name().equals("SMS")) {
                    success = ChannelTypeEnum.SMS.get_id();
                } else if (platform.name().equals("QZONE")) {
                    success = ChannelTypeEnum.QZONE.get_id();
                }
                if (share_interface != null) {
                    share_interface.sendShareHandler(1, "", "", Constants.Share, Constants.share, success + "");
                }
            } else {
                JsonObject attrsObj = new JsonObject();
                attrsObj.addProperty("infoType", "1");
                if (share_interface != null) {
                    share_interface.callShareHttpPost(attrsObj);
                }
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            MyLogUtil.i("ShareManager----onError--" + t);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            if (share_interface != null) {
                share_interface.sendShareHandler(0, "-1", "取消分享", Constants.Share, Constants.share);
            }
        }
    };

    public ShareManager(Context mContext) {
        this.mContext = mContext;
    }

    public void setShareListener(ShareInterface share_interface) {
        this.share_interface = share_interface;
    }

    public ShareInterface getShare_interface() {
        return share_interface;
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
                shareAction = new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.SMS);
                break;
            default:
                break;
        }
        setShareParams(shareAction, shareId, shareModel);
    }

    private void setShareParams(ShareAction shareAction, int shareId, SharePublicAccountModel shareModel) {
        MyLogUtil.i("ShareManager---title:" + shareModel.gettitle() + ";content:" + shareModel.getcontent());
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