package com.gp.rainy.share;

import android.text.TextUtils;

import com.gp.rainy.utils.DeviceUtil;

import java.io.Serializable;

public class SharePublicAccountModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int shareType; //1:文本 2:图片 3:链接 4:视频 5:音乐 6:GIF 7:微信小程序
    public String ret = "";
    public String content = "";
    public String title = "";
    public String url = "";
    public String imgurl = "";

    public void setShareType( int shareType) {
        this.shareType = shareType;
    }

    public int getShareType() {
        return shareType;
    }

    public void setret(String ret) {
        this.ret = ret;
    }

    public String getret() {
        if (!TextUtils.isEmpty(this.ret)) {
            return (this.ret);
        } else {
            return "";
        }
    }

    public void setcontent(String content) {
        this.content = content;
    }

    public String getcontent() {
        if (!TextUtils.isEmpty(this.content)) {
            return (this.content);
        } else {
            return "";
        }
    }

    public void settitle(String title) {
        this.title = DeviceUtil.replaceBlank(title);
    }

    public String gettitle() {
        if (!TextUtils.isEmpty(this.title)) {
            return (this.title);
        } else {
            return "";
        }
    }

    public void seturl(String url) {
        this.url = url;
    }

    public String geturl() {
        if (!TextUtils.isEmpty(this.url)) {
            return (this.url);
        } else {
            return "";
        }
    }

    public void setimgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getimgurl() {
        if (!TextUtils.isEmpty(this.imgurl)) {
            return (this.imgurl);
        } else {
            return "";
        }
    }
}
