package com.gp.rainy.share;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class ShareType {

    public static final int TEXT = 1;  //文本
    public static final int IMAGE = 2; //图片
    public static final int LINK = 3;  //链接
    public static final int VIDEO = 4; //视频

    @IntDef({TEXT, IMAGE, LINK, VIDEO}) //替代枚举的方案，使用IntDef保证类型安全
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShareTypeChecker {
    }
}