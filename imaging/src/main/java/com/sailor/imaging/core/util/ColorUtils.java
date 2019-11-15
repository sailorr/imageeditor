package com.sailor.imaging.core.util;

/**
 * -description:
 * -author: created by tang on 2019/10/28 16:29
 */
public class ColorUtils {

    public  static int[] int2RGB(int color){
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return new int[]{red,green,blue};
    }

}
