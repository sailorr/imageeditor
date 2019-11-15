package com.sailor.imaging.core.font;

/**
 * -description:
 * -author: created by tang on 2019/10/30 16:03
 */
public class FontStyleBean {

    //水印文字内容
    private String textContent="";

    //选择字体样式
    private int fontCheckId;

    //选择字体样式
    private String fontStyleName="";

    //透明度百分比
    private int seekValue=100;

    //阴影状态
    private boolean mShadowCheckStaus=false;

    //透明度值
    private int colorAlpha=255;

    //选择的颜色
    private int colorCheckId;


    public String getFontStyleName() {
        return fontStyleName;
    }

    public void setFontStyleName(String fontStyleName) {
        this.fontStyleName = fontStyleName;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public int getFontCheckId() {
        return fontCheckId;
    }

    public void setFontCheckId(int fontCheckId) {
        this.fontCheckId = fontCheckId;
    }

    public int getSeekValue() {
        return seekValue;
    }

    public void setSeekValue(int seekValue) {
        this.seekValue = seekValue;
    }

    public boolean isShadowCheckStaus() {
        return mShadowCheckStaus;
    }

    public void setShadowCheckStaus(boolean shadowCheckStaus) {
        this.mShadowCheckStaus = shadowCheckStaus;
    }

    public int getColorAlpha() {
        return colorAlpha;
    }

    public void setColorAlpha(int colorAlpha) {
        this.colorAlpha = colorAlpha;
    }

    public int getColorCheckId() {
        return colorCheckId;
    }

    public void setColorCheckId(int colorCheckId) {
        this.colorCheckId = colorCheckId;
    }
}
