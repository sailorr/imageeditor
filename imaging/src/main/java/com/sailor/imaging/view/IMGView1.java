package com.sailor.imaging.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by felix on 2017/11/14 下午6:43.
 */
public class IMGView1 extends View {

    public IMGView1(Context context) {
        this(context, null, 0);
    }

    public IMGView1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMGView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private Paint mPaint;

    private void initialize(Context context) {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(5);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, null, mFrame, null);
    }

    private Bitmap mBitmap;
    private RectF mFrame = new RectF();

    public void setImageBitmap(Bitmap image) {
        this.mBitmap = image;
        mFrame.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        invalidate();
    }
}
