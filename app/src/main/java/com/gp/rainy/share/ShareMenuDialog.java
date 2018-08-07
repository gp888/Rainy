package com.gp.rainy.share;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gp.rainy.Constants;
import com.gp.rainy.DeviceUtil;
import com.gp.rainy.R;
import com.gp.rainy.WebViewActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShareMenuDialog extends Dialog {

    private LayoutInflater inflater;
    private View view;

    private HorizontalScrollView shareMenuSVA, shareMenuSVB;
    private LinearLayout shareMenuA, shareMenuB;

    private final int size = DeviceUtil.getScreenWidth() / 5;

    public interface OnButtonClickListener {
        void onButtonClick(int type, int id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.anim_dialog_bottom);

        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = DeviceUtil.getScreenWidth();
        p.height = LayoutParams.WRAP_CONTENT;
        setContentView(view, p);

        setCanceledOnTouchOutside(true);
    }

    public ShareMenuDialog(Context context, int from, String rightMenu, boolean isCollect, final boolean isNet, final OnButtonClickListener mOnButtonClickListener) {
        super(context, R.style.dialog_bottom);

        inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.dialog_share_menu, null);
        shareMenuSVA = view.findViewById(R.id.sv_share_menu_a);
        shareMenuSVB = view.findViewById(R.id.sv_share_menu_b);
        shareMenuA = view.findViewById(R.id.ll_share_menu_a);
        shareMenuB = view.findViewById(R.id.ll_share_menu_b);
        LinearLayout shareMenuCancle = view.findViewById(R.id.ll_share_menu_cancle);

        shareMenuCancle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        initData(context, from, rightMenu, isCollect, isNet, mOnButtonClickListener);
    }

    private void initData(Context context, int from, String rightMenu, boolean isCollect, boolean isNet, OnButtonClickListener mOnButtonClickListener) {
        List<ArrayMap<String, Integer>> listA = new ArrayList<>();
        ArrayMap<String, Integer> map = new ArrayMap<>();
        map.put("type", 1);
        map.put("id", 0);
        map.put("title", R.string.umeng_socialize_text_weixin_key);
        map.put("icon", R.drawable.ic_share_wx);

        ArrayMap<String, Integer> map1 = new ArrayMap<>();
        map1.put("type", 1);
        map1.put("id", 1);
        map1.put("title", R.string.umeng_socialize_text_weixin_circle_key);
        map1.put("icon", R.drawable.ic_share_wx_circle);

        ArrayMap<String, Integer> map2 = new ArrayMap<>();
        map2.put("type", 1);
        map2.put("id", 2);
        map2.put("title", R.string.umeng_socialize_text_qq_key);
        map2.put("icon", R.drawable.ic_share_qq);

        ArrayMap<String, Integer> map3 = new ArrayMap<>();
        map3.put("type", 1);
        map3.put("id", 3);
        map3.put("title", R.string.umeng_socialize_text_qq_zone_key);
        map3.put("icon", R.drawable.ic_share_qq_circle);

        if (from == Constants.TO_WEBVIEW_FROM_JS) {
            listA.add(map);
            listA.add(map1);
            listA.add(map2);
            listA.add(map3);

            setMenuData(context, shareMenuA, listA, isNet, mOnButtonClickListener);
        }
    }

    private void setMenuData(Context context, LinearLayout shareMenu, List<ArrayMap<String, Integer>> list, final boolean isNet, final OnButtonClickListener mOnButtonClickListener) {
        for (int i = 0; i < list.size(); i++) {
            View view = inflater.inflate(R.layout.umeng_socialize_shareboard_item, null);
            ImageView icon = view.findViewById(R.id.umeng_socialize_shareboard_image);
            TextView textview = view.findViewById(R.id.umeng_socialize_shareboard_pltform_name);
            if (!TextUtils.isEmpty(list.get(i).get("title").toString())) {
                textview.setText(context.getString(list.get(i).get("title")));
            }
            if (!TextUtils.isEmpty(list.get(i).get("icon").toString())) {
                icon.setImageResource(list.get(i).get("icon"));
            }
            view.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            view.setTag(list.get(i).get("type") + "," + list.get(i).get("id"));
            shareMenu.addView(view);
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String[] tagArr = ((String) v.getTag()).split(",");
                    int type = Integer.parseInt(tagArr[0]);
                    int id = Integer.parseInt(tagArr[1]);
                    mOnButtonClickListener.onButtonClick(type, id);
                    if (!isNet) {
                        dismiss();
                    }
                }
            });
        }
    }
}
