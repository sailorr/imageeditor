package com.sailor.imaging.core;

import android.graphics.Color;
import android.text.TextUtils;

/**
 * Created by felix on 2017/12/1 下午2:43.
 */

public class IMGText {

    private String text;

    private int color = Color.WHITE;

    private boolean shadow;

    private  String fontName;

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public IMGText(String text, int color) {
        this.text = text;
        this.color = color;
        this.shadow = shadow;
        this.fontName = fontName;
    }


    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(text);
    }

    public int length() {
        return isEmpty() ? 0 : text.length();
    }

    @Override
    public String toString() {
        return "IMGText{" +
                "text='" + text + '\'' +
                ", color=" + color +
                '}';
    }
}
