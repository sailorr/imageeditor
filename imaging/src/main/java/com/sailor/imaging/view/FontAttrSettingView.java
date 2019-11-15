package com.sailor.imaging.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.sailor.imaging.R;
import com.sailor.imaging.core.font.FontManager;
import com.sailor.imaging.core.font.FontStyleBean;
import com.sailor.imaging.core.util.ColorUtils;

/**
 * -description:
 * -author: created by tang on 2019/10/24 11:19
 */
public class FontAttrSettingView extends LinearLayoutCompat implements RadioGroup.OnCheckedChangeListener {
    private String[] titles = new String[]{"键盘", "样式", "字体"};
    private EditText mEditText;
    public IMGColorGroup mColorGroup;
    private SeekBar mSeekBar;
    private TextView mSeekValue;
    public CheckBox mCheckBox;
    private RadioButton defaultRbtn, heiRbtn, songRbtn, kaiRbtn, yaheiRbtn;
    private RadioGroup mFontGroup;


    public FontAttrSettingView(Context context) {
        this(context, null);
    }

    public FontAttrSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_font_setting, this);
        initView();
    }

    public void setEditText(EditText editText) {
        this.mEditText = editText;
        mEditText.setOnClickListener(mOnClickListener);
        String textContext = mEditText.getText().toString();
        if (!textContext.equals("")) {
            mStyleBean = FontManager.INSTANCE.getFontStyle(textContext);
            //设置初始状态
            mSeekBar.setProgress(mStyleBean.getSeekValue());
            mCheckBox.setChecked(mStyleBean.isShadowCheckStaus());
            if (mStyleBean.getFontCheckId() == 0) {
                defaultRbtn.setChecked(true);
            } else {
                mFontGroup.check(mStyleBean.getFontCheckId());
            }
            mColorGroup.setCheckColor(mStyleBean.getColorCheckId());
        }
    }

    private TabLayout mTabLayout;
    private ViewSwitcher viewSwitcher;
    protected InputMethodManager inputMethodManager;
    private FontStyleBean mStyleBean;

    private void initView() {
        mStyleBean = new FontStyleBean();

        mTabLayout = findViewById(R.id.tab);
        viewSwitcher = findViewById(R.id.vs);

        mSeekValue = findViewById(R.id.tv_sbvalue);
        mFontGroup = findViewById(R.id.rg_font);
        mFontGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mSeekBar = findViewById(R.id.sb_trans);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);


        mCheckBox = findViewById(R.id.cb_shadow);

        mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

        defaultRbtn = findViewById(R.id.default_rb);
        heiRbtn = findViewById(R.id.hei_rb);
        songRbtn = findViewById(R.id.song_rb);
        kaiRbtn = findViewById(R.id.kai_rb);
        yaheiRbtn = findViewById(R.id.yahei_rb);
        heiRbtn.setTypeface(FontManager.INSTANCE.getTypeface(FontManager.HEI_TI));
        songRbtn.setTypeface(FontManager.INSTANCE.getTypeface(FontManager.SONG_TI));
        kaiRbtn.setTypeface(FontManager.INSTANCE.getTypeface(FontManager.KAI_TI));
        yaheiRbtn.setTypeface(FontManager.INSTANCE.getTypeface(FontManager.YA_HEI));

        mColorGroup = findViewById(R.id.rg_color);
        mColorGroup.setOnCheckedChangeListener(this);
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        initTab();
    }

//
//    //透明度
//    private static int colorAlpha = 255;
//    //文字阴影
//    public static boolean checkStatus = false;
//    //透明度值
//    private static int seekProgress = 100;
//    //选中的rb
//    private static int checkedId = 0;
//    //选中的字体
//    public static String fontName = "";

    //阴影选择事件
    private CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mStyleBean.setShadowCheckStaus(isChecked);
        }
    };

    //edit点击事件 tab移动到键盘栏
    private View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.et_text) {
                mTabLayout.getTabAt(0).select();
            }
        }
    };

    //字体选择监听
    RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int id) {
            mStyleBean.setFontCheckId(id);
            if (id == heiRbtn.getId()) {
                mStyleBean.setFontStyleName(FontManager.HEI_TI);
            } else if (id == songRbtn.getId()) {
                mStyleBean.setFontStyleName(FontManager.SONG_TI);
            } else if (id == yaheiRbtn.getId()) {
                mStyleBean.setFontStyleName(FontManager.YA_HEI);
            } else if (id == kaiRbtn.getId()) {
                mStyleBean.setFontStyleName(FontManager.KAI_TI);
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mStyleBean.setSeekValue(progress);
            mSeekValue.setText(progress + "%");
            int colorAlpha = (int) (progress / 100d * 255);
            mStyleBean.setColorAlpha(colorAlpha);
            if (mEditText != null) {
                setColor(colorAlpha, mEditText.getCurrentTextColor());
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void initTab() {
        for (int i = 0; i < titles.length; i++) {
            TabLayout.Tab tab = mTabLayout.newTab();
            tab.setTag(i);
            tab.setText(titles[i]);
            mTabLayout.addTab(tab);
        }
        mTabLayout.getTabAt(0).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch ((int) tab.getTag()) {
                    case 0:
//                        mAttrClickListener.onKeyboardClick();
                        showSoftKeyboard();
                        viewSwitcher.setVisibility(View.GONE);
                        break;
                    case 1:
                        hideSoftKeyboard();
                        //键盘隐藏带有动画效果，直接显示，会造成键盘未隐藏，view已经显示，所有view会显示在键盘上分，会有一下闪烁
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                viewSwitcher.setVisibility(View.VISIBLE);
                                viewSwitcher.setDisplayedChild(0);
                            }
                        }, 100);
                        break;
                    case 2:
                        hideSoftKeyboard();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                viewSwitcher.setVisibility(View.VISIBLE);
                                viewSwitcher.setDisplayedChild(1);
                            }
                        }, 100);
//                        mAttrClickListener.onFontClick();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    protected void hideSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    private void showSoftKeyboard() {
        inputMethodManager.showSoftInput(mEditText, 1);
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        setColor(mStyleBean.getColorAlpha(), mColorGroup.getCheckColor());
    }

    private void setColor(int colorAlpha, int color) {
        int[] ints = ColorUtils.int2RGB(color);
        mEditText.setTextColor(Color.argb(colorAlpha, ints[0], ints[1], ints[2]));
    }

    public   FontStyleBean getStyleBean(){
        return mStyleBean;
    }

}