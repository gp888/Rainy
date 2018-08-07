package com.gp.rainy.share;


public enum ChannelTypeEnum {

    ALL("全部", 0),
    WX("微信", 1),
    WX_CIRCLE("微信朋友圈", 2),
    QQ("QQ", 3),
    SMS("短信", 4),
    QZONE("QQ空间", 5);

    private String _name;
    private int _id = 0;

    public int get_id() {
        return _id;
    }

    ChannelTypeEnum(String name, int id) {
        this._name = name;
        this._id = id;
    }

    public static ChannelTypeEnum getEnum(int id) {
        for (ChannelTypeEnum typeEnum : ChannelTypeEnum.values()) {
            if (typeEnum.get_id() == id) {
                return typeEnum;
            }
        }
        return null;
    }
}
