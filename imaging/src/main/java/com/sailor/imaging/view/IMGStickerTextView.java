package com.sailor.imaging.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.sailor.imaging.IMGTextEditDialog;
import com.sailor.imaging.core.font.FontManager;
import com.sailor.imaging.core.IMGText;
import com.sailor.imaging.core.font.FontStyleBean;

/**
 * Created by felix on 2017/11/14 下午7:27.
 */
public class IMGStickerTextView extends IMGStickerView implements IMGTextEditDialog.Callback {

    private static final String TAG = "IMGStickerTextView";

    private TextView mTextView;

    private IMGText mText;

    private IMGTextEditDialog mDialog;

    private static float mBaseTextSize = -1f;

    private static final int PADDING = 26;

    private static final float TEXT_SIZE_SP = 12f;

    public IMGStickerTextView(Context context) {
        this(context, null, 0);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onInitialize(Context context) {
        if (mBaseTextSize <= 0) {
            mBaseTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TEXT_SIZE_SP, context.getResources().getDisplayMetrics());
        }
        super.onInitialize(context);
    }

    @Override
    public View onCreateContentView(Context context) {
        mTextView = new TextView(context);
        mTextView.setTextSize(mBaseTextSize);
        mTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
        mTextView.setTextColor(Color.WHITE);
        return mTextView;
    }

    public void setText(IMGText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            String textContent = mText.getText();
            FontStyleBean fontStyleBean = FontManager.INSTANCE.getFontStyle(textContent);
            Typeface typeface = FontManager.INSTANCE.getTypeface(fontStyleBean.getFontStyleName());
            mTextView.setText(textContent);
            mTextView.setTextColor(mText.getColor());
            mTextView.setTypeface(typeface);
            if (fontStyleBean.isShadowCheckStaus()) {
                mTextView.setShadowLayer(20, 20, 20, Color.BLACK);
            } else {
                mTextView.setShadowLayer(0, 0, 0, Color.BLACK);
            }
        }
    }

    public IMGText getText() {
        return mText;
    }

    @Override
    public void onContentTap() {
        IMGTextEditDialog dialog = new IMGTextEditDialog(getContext(), this);
        dialog.setText(mText);
        dialog.show();
    }

    private IMGTextEditDialog getDialog() {
//        if (mDialog == null) {
        mDialog = new IMGTextEditDialog(getContext(), this);
//        }
        return mDialog;
    }

    @Override
    public void onText(IMGText text) {
        setText(text);
    }
}