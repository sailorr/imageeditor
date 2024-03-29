package com.sailor.imaging.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RadioGroup;

/**
 * Created by felix on 2017/12/1 下午3:07.
 */

public class IMGColorGroup extends RadioGroup {

    public IMGColorGroup(Context context) {
        super(context);
    }

    public IMGColorGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
//        LayoutInflater.from(context).inflate(R.layout.image_color_layout,this);
    }

    public int getCheckColor() {
        int checkId = getCheckedRadioButtonId();
        IMGColorRadio radio = findViewById(checkId);
        if (radio != null) {
            return radio.getColor();
        }
        return Color.WHITE;
    }

    public void setCheckColor(int color) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            IMGColorRadio radio = (IMGColorRadio) getChildAt(i);
            if (radio.getColor() == color) {
                radio.setChecked(true);
                break;
            }
        }
    }


}
