package com.sailor.imaging.core.font;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * -description:
 * -author: created by tang on 2019/10/30 9:38
 */
public enum FontManager {
    INSTANCE;
    private Typeface mHei, mFang, mKai, mYahei;

    public final static String HEI_TI = "heiti", SONG_TI = "songti", KAI_TI = "kaiti", YA_HEI = "yahei";

    private Map<String, FontStyleBean> cacheFontStyle;

    public void init(Context context) {
        final AssetManager mgr = context.getAssets();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("IMGTextEditDialog", "run: ");
                mHei = Typeface.createFromAsset(mgr, "fonts/simhei.ttf");
                mFang = Typeface.createFromAsset(mgr, "fonts/simfang.ttf");
                mKai = Typeface.createFromAsset(mgr, "fonts/simkai.ttf");
                mYahei = Typeface.createFromAsset(mgr, "fonts/msyh.ttf");

                cacheFontStyle = new HashMap<>();
            }
        }).start();
    }

    public Typeface getTypeface(String fontName) {
        switch (fontName) {
            case HEI_TI:
                return mHei;
            case SONG_TI:
                return mFang;
            case KAI_TI:
                return mKai;
            case YA_HEI:
                return mYahei;
        }
        return null;
    }

    public void update(FontStyleBean fontStyleBean) {
        cacheFontStyle.put(fontStyleBean.getTextContent(), fontStyleBean);
    }

    public FontStyleBean getFontStyle(String text) {
        return cacheFontStyle.get(text);
    }
}
