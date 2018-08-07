package com.gp.rainy;

public class WebviewEvent {

    public final static int TYPE_WX_PAY_RESULT = 0;

    public int type; // 业务类型
    public int result; // 返回结果
    public String errStr; // 返回结果

    public WebviewEvent(int type, int result, String errStr) {
        this.result = result;
        this.type = type;
        this.errStr = errStr;
    }

}
