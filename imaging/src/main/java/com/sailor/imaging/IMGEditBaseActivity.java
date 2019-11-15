package com.sailor.imaging;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sailor.imaging.core.DoodleNumManager;
import com.sailor.imaging.core.IMGMode;
import com.sailor.imaging.core.IMGText;
import com.sailor.imaging.view.IMGColorGroup;
import com.sailor.imaging.view.IMGView;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

/**
 * Created by felix on 2017/12/5 下午3:08.
 */

abstract class IMGEditBaseActivity extends Activity implements View.OnClickListener,
        IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener {

    protected IMGView mImgView;

    private RadioGroup mModeGroup;

    private IMGColorGroup mColorGroup;
    private ConstraintLayout mLayout;
    private ImageView lastStep, nextStep;
    private RadioButton mPaintButton;
    private IndicatorSeekBar mSeekBar;
    private TextView mErraserTv;

//    private IMGColorGroup mColorGroup;

    private IMGTextEditDialog mTextDialog;


//    private View mLayoutOpSub;

//    private ViewSwitcher mOpSwitcher, mOpSubSwitcher;

    public static final int OP_HIDE = -1;

    public static final int OP_NORMAL = 0;

    public static final int OP_CLIP = 1;

    public static final int OP_SUB_DOODLE = 0;

    public static final int OP_SUB_MOSAIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            setContentView(R.layout.image_edit_activity);
            initViews();
            mImgView.setImageBitmap(bitmap);
            onCreated();
        } else finish();
    }

    public void onCreated() {

    }

    private void initViews() {
        mImgView = findViewById(R.id.image_canvas);
        mLayout = findViewById(R.id.cl_paint);

        lastStep = findViewById(R.id.img_lastStep);
        lastStep.setOnClickListener(this);
        nextStep = findViewById(R.id.img_nextStep);
        nextStep.setOnClickListener(this);

        mPaintButton = findViewById(R.id.rb_paint);
        mPaintButton.setOnClickListener(this);

        mErraserTv = findViewById(R.id.tv_eraser);
        mErraserTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onModeClick(IMGMode.ERRASER);
            }
        });

        mSeekBar = findViewById(R.id.sk_paintSize);
        mSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {

            @Override
            public void onSeeking(SeekParams seekParams) {
                mImgView.setPenSize(seekParams.progress);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        mModeGroup = findViewById(R.id.rg_mode);
        mModeGroup.setOnCheckedChangeListener(this);
        mColorGroup = findViewById(R.id.rg_color);
        mColorGroup.setOnCheckedChangeListener(this);

        DoodleNumManager.INSTANCE.mListener = new DoodleNumManager.DoodleNumListener() {
            @Override
            public void onDoodleNum(int doodleNum, int mRevertDoodleNum) {
                lastStep.setEnabled(doodleNum!=0);
                nextStep.setEnabled(mRevertDoodleNum!=0);
                Log.e("TestTag", "onDoodleNum: " + doodleNum + " mRevertDoodleNum--" + mRevertDoodleNum);
            }
        };
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.rb_paint) {
            isShowControl = !isShowControl;
            if (isShowControl) {
                onModeClick(IMGMode.DOODLE);
                showControl(View.VISIBLE);
            } else {
                onModeClick(IMGMode.NONE);
                showControl(View.GONE);
            }
        } else if (vid == R.id.rb_editText) {
            onTextModeClick();
        } else if (vid == R.id.img_save) {
            onDoneClick();
        } else if (vid == R.id.img_cancer) {
            onCancelClick();
        } else if (vid == R.id.img_lastStep) {
            mImgView.onRevert();
        } else if (vid == R.id.img_nextStep) {
            mImgView.cancelRevert();
        }
    }


    public void onTextModeClick() {
        mTextDialog = new IMGTextEditDialog(this, this);
        onModeClick(IMGMode.NONE);
        mTextDialog.show();
    }


    private boolean isShowControl;

    @Override
    public final void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.rg_mode) {
            if (checkedId != R.id.rb_paint) {
                isShowControl = false;
                showControl(View.GONE);
            }
        } else {
            onColorChanged(mColorGroup.getCheckColor());
        }
    }

    private void showControl(int visibility) {
        mLayout.setVisibility(visibility);
        lastStep.setVisibility(visibility);
        nextStep.setVisibility(visibility);
    }


//
//    public void setOpSubDisplay(int opSub) {
//        if (opSub < 0) {
//            mLayoutOpSub.setVisibility(View.GONE);
//        } else {
//            mOpSubSwitcher.setDisplayedChild(opSub);
//            mLayoutOpSub.setVisibility(View.VISIBLE);
//        }
//    }


    public abstract Bitmap getBitmap();

    //选择编辑模式
    public abstract void onModeClick(IMGMode mode);

    //取消
    public abstract void onCancelClick();

    //保存
    public abstract void onDoneClick();

    //画笔颜色
    public abstract void onColorChanged(int checkedColor);

    //文字
    @Override
    public abstract void onText(IMGText text);


}
