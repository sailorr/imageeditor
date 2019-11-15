package com.sailor.imaging;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sailor.imaging.core.IMGText;
import com.sailor.imaging.core.font.FontManager;
import com.sailor.imaging.core.font.FontStyleBean;
import com.sailor.imaging.view.FontAttrSettingView;


/**
 * -description: 生命周期 构造函数-show()-oncreate()-onStart()
 * -author: created by tang on 2019/10/30 15:15
 */
public class IMGTextEditDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = "IMGTextEditDialog";

    private EditText mEditText;

    private Callback mCallback;

    private IMGText mDefaultText;

    private FontAttrSettingView attrSettingView;

    protected InputMethodManager inputMethodManager;

    /**
     * @param context
     * @param callback
     */
    public IMGTextEditDialog(Context context, Callback callback) {
        super(context, R.style.ImageTextDialog);
        setContentView(R.layout.image_text_dialog);
        mCallback = callback;
        Window window = getWindow();
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        initView();
    }

    private void initView() {
        mEditText = findViewById(R.id.et_text);
        attrSettingView = findViewById(R.id.font_setting);
    }


    public void setText(IMGText text) {
        mDefaultText = text;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        if (mDefaultText != null) {
            mEditText.setText(mDefaultText.getText());
            mEditText.setTextColor(mDefaultText.getColor());
            if (!mDefaultText.isEmpty()) {
                mEditText.setSelection(mEditText.length());
            }
            mDefaultText = null;
        } else mEditText.setText("");
        //更新再次编辑 选择的字体颜色
        attrSettingView.setEditText(mEditText);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    public void reset() {
        setText(new IMGText(null, Color.WHITE));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btn_confirm) {
            onDone();
        } else if (vid == R.id.btn_close) {
            dismiss();
        }
    }

    private void onDone() {
        String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text) && mCallback != null) {
            FontStyleBean fontStyleBean  = attrSettingView.getStyleBean();
            fontStyleBean.setTextContent(text);
            fontStyleBean.setColorCheckId(attrSettingView.mColorGroup.getCheckColor());
            FontManager.INSTANCE.update(fontStyleBean);
            mCallback.onText(new IMGText(text, mEditText.getCurrentTextColor()));
        }
        dismiss();
    }


    public interface Callback {
        void onText(IMGText text);
    }
}
