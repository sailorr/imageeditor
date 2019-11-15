package com.sailor.imaging.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.sailor.imaging.core.IMGImage;
import com.sailor.imaging.core.IMGMode;
import com.sailor.imaging.core.IMGPath;
import com.sailor.imaging.core.IMGText;
import com.sailor.imaging.core.anim.IMGHomingAnimator;
import com.sailor.imaging.core.homing.IMGHoming;
import com.sailor.imaging.core.sticker.IMGSticker;
import com.sailor.imaging.core.sticker.IMGStickerPortrait;

/**
 * Created by felix on 2017/11/14 下午6:43.
 */
public class IMGView extends FrameLayout implements Runnable,
        ValueAnimator.AnimatorUpdateListener, IMGStickerPortrait.Callback {

    private static final String TAG = "IMGView";

    private IMGMode mMode = IMGMode.NONE;

    private IMGImage mImage = new IMGImage();

    private GestureDetector mGDetector;

    private ScaleGestureDetector mSGDetector;

    private IMGHomingAnimator mHomingAnimator;

    private Pen mPen = new Pen();

    private int mPointerCount = 0;

    private Paint mDoodlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mEraserPaint;


    private static final boolean DEBUG = true;

    {
        // 涂鸦画刷
        mDoodlePaint.setStyle(Paint.Style.STROKE);
        mDoodlePaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH);//画笔粗细
        mDoodlePaint.setColor(Color.WHITE);//画笔默认颜色
        mDoodlePaint.setStrokeCap(Paint.Cap.ROUND);//开始点为圆形
        mDoodlePaint.setStrokeJoin(Paint.Join.ROUND);//结束点为圆形
        mDoodlePaint.setAntiAlias(true);
        mDoodlePaint.setDither(true);

        //橡皮擦
        mEraserPaint = new Paint();

        mEraserPaint.setAlpha(0);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        //上面这句代码是橡皮擦设置的重点（重要的事是不是一定要说三遍）
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH);

    }

    public IMGView(Context context) {
        this(context, null, 0);
    }

    public IMGView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMGView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mPen.setMode(mImage.getMode());
        mGDetector = new GestureDetector(context, new MoveAdapter());
    }

    public void setImageBitmap(Bitmap image) {
        mImage.setBitmap(image);

        invalidate();
    }

    public void setMode(IMGMode mode) {

        // 保存现在的编辑模式
        this.mMode = mode;

        // 设置新的编辑模式
        mImage.setMode(mode);
        mPen.setMode(mode);


        // 矫正区域
        onHoming();
    }


    /**
     * 是否真正修正归位
     */
    boolean isHoming() {
        return mHomingAnimator != null
                && mHomingAnimator.isRunning();
    }

    private void onHoming() {
        invalidate();
        stopHoming();
        startHoming(mImage.getStartHoming(getScrollX(), getScrollY()),
                mImage.getEndHoming(getScrollX(), getScrollY()));
    }

    private void startHoming(IMGHoming sHoming, IMGHoming eHoming) {
        if (mHomingAnimator == null) {
            mHomingAnimator = new IMGHomingAnimator();
            mHomingAnimator.addUpdateListener(this);
        }
        mHomingAnimator.setHomingValues(sHoming, eHoming);
        mHomingAnimator.start();
    }

    private void stopHoming() {
        if (mHomingAnimator != null) {
            mHomingAnimator.cancel();
        }
    }

    public void setPenColor(int color) {
        mMode = IMGMode.DOODLE;
        mDoodlePaint.setColor(color);
        mPen.setColor(color);
        mPen.setMode(IMGMode.DOODLE);
    }

    private float paintSize = IMGPath.BASE_DOODLE_WIDTH;

    public void setPenSize(int size) {
        paintSize = 2 + 2 * size * size;
        mPen.setWidth(2 + 2 * size * size);

        mDoodlePaint.setStrokeWidth(paintSize);
        mEraserPaint.setStrokeWidth(paintSize);

    }

    public void onRevert() {
        mImage.revertDoodle();
        invalidate();
    }

    public void cancelRevert() {
        mImage.cancelRevertDoodle();
        invalidate();
    }


    public IMGMode getMode() {
        return mImage.getMode();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onDrawImages(canvas);
    }

    private Paint getPaint() {
        switch (mMode) {
            case DOODLE:
                return mDoodlePaint;
            case ERRASER:
                return mEraserPaint;
        }
        return mDoodlePaint;
    }

    private void onDrawImages(Canvas canvas) {
        //背景
        mImage.onDrawImage(canvas);
        // 涂鸦
        mImage.onDrawDoodles();
        mImage.drawPahth(mPen.getPath(),getPaint());
        //文字
        mImage.onDrawStickers(canvas);
    }


    public Bitmap saveBitmap() {
        mImage.stickAll();

        float scale = 1f / mImage.getScale();

        RectF frame = new RectF(mImage.getClipFrame());

        // 旋转基画布
        Matrix m = new Matrix();
        m.setRotate(mImage.getRotate(), frame.centerX(), frame.centerY());
        m.mapRect(frame);

        // 缩放基画布
        m.setScale(scale, scale, frame.left, frame.top);
        m.mapRect(frame);

        Bitmap bitmap = Bitmap.createBitmap(Math.round(frame.width()),
                Math.round(frame.height()), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        // 平移到基画布原点&缩放到原尺寸
        canvas.translate(-frame.left, -frame.top);
        canvas.scale(scale, scale, frame.left, frame.top);

        onDrawImages(canvas);

        return bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mImage.onWindowChanged(right - left, bottom - top);
        }
    }

    public <V extends View & IMGSticker> void addStickerView(V stickerView, LayoutParams params) {
        if (stickerView != null) {

            addView(stickerView, params);

            stickerView.registerCallback(this);
            mImage.addSticker(stickerView);
        }
    }

    public void addStickerText(IMGText text) {
        IMGStickerTextView textView = new IMGStickerTextView(getContext());

        textView.setText(text);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

        // Center of the drawing window.
        layoutParams.gravity = Gravity.CENTER;

        textView.setX(getScrollX());
        textView.setY(getScrollY());

        addStickerView(textView, layoutParams);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return onInterceptTouch(ev) || super.onInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    boolean onInterceptTouch(MotionEvent event) {
        if (isHoming()) {
            stopHoming();
            return true;
        } else if (mImage.getMode() == IMGMode.CLIP) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(this);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                postDelayed(this, 1200);
                break;
        }
        return onTouch(event);
    }

    boolean onTouch(MotionEvent event) {

        if (isHoming()) {
            // Homing
            return false;
        }

        mPointerCount = event.getPointerCount();

        boolean handled = false;

        IMGMode mode = mImage.getMode();

        if (mode == IMGMode.NONE || mode == IMGMode.CLIP) {
            handled |= onTouchNONE(event);
        } else if (mPointerCount > 1) {
            onPathDone();
            handled |= onTouchNONE(event);
        } else {
            handled |= onTouchPath(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mImage.onTouchDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mImage.onTouchUp(getScrollX(), getScrollY());
                onHoming();
                break;
        }

        return handled;
    }


    private boolean onTouchNONE(MotionEvent event) {
        return mGDetector.onTouchEvent(event);
    }

    private boolean onTouchPath(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return onPathBegin(event);
            case MotionEvent.ACTION_MOVE:
                return onPathMove(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return mPen.isIdentity(event.getPointerId(0)) && onPathDone();
        }
        return false;
    }

    private boolean onPathBegin(MotionEvent event) {
        mPen.reset(event.getX(), event.getY() - mImage.getFrame().top);
        mPen.setIdentity(event.getPointerId(0));
        return true;
    }

    private boolean onPathMove(MotionEvent event) {
        if (mPen.isIdentity(event.getPointerId(0))) {
            mPen.lineTo(event.getX(), event.getY() - mImage.getFrame().top);
            invalidate();
            return true;
        }
        return false;
    }

    private boolean onPathDone() {
        if (mPen.isEmpty()) {
            return false;
        }
        mImage.addPath(mPen.toPath());
        mPen.reset();
        invalidate();
        return true;
    }

    @Override
    public void run() {
        // 稳定触发
        if (!onSteady()) {
            postDelayed(this, 500);
        }
    }

    boolean onSteady() {

        if (!isHoming()) {
            mImage.onSteady(getScrollX(), getScrollY());
            onHoming();
            return true;
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this);
        mImage.release();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        toApplyHoming((IMGHoming) animation.getAnimatedValue());
    }


    private void toApplyHoming(IMGHoming homing) {
        mImage.setScale(homing.scale);
        mImage.setRotate(homing.rotate);
        if (!onScrollTo(Math.round(homing.x), Math.round(homing.y))) {
            invalidate();
        }
    }

    private boolean onScrollTo(int x, int y) {
        if (getScrollX() != x || getScrollY() != y) {
            scrollTo(x, y);
            return true;
        }
        return false;
    }

    @Override
    public <V extends View & IMGSticker> void onDismiss(V stickerView) {
        mImage.onDismiss(stickerView);
        invalidate();
    }

    @Override
    public <V extends View & IMGSticker> void onShowing(V stickerView) {
        mImage.onShowing(stickerView);
        invalidate();
    }

    @Override
    public <V extends View & IMGSticker> boolean onRemove(V stickerView) {
        if (mImage != null) {
            mImage.onRemoveSticker(stickerView);
        }
        stickerView.unregisterCallback(this);
        ViewParent parent = stickerView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(stickerView);
        }
        return true;
    }


    private boolean onScroll(float dx, float dy) {
        IMGHoming homing = mImage.onScroll(getScrollX(), getScrollY(), -dx, -dy);
        if (homing != null) {
            toApplyHoming(homing);
            return true;
        }
        return onScrollTo(getScrollX() + Math.round(dx), getScrollY() + Math.round(dy));
    }

    private class MoveAdapter extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return IMGView.this.onScroll(distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private static class Pen extends IMGPath {

        private int identity = Integer.MIN_VALUE;


        void reset() {
            this.path.reset();
            this.identity = Integer.MIN_VALUE;
        }

        void reset(float x, float y) {
            this.path.reset();
            this.path.moveTo(x, y);
            this.identity = Integer.MIN_VALUE;
        }

        void setIdentity(int identity) {
            this.identity = identity;
        }

        boolean isIdentity(int identity) {
            return this.identity == identity;
        }

        void lineTo(float x, float y) {
            this.path.lineTo(x, y);
        }

        boolean isEmpty() {
            return this.path.isEmpty();
        }

        IMGPath toPath() {
            return new IMGPath(new Path(this.path), getMode(), getColor(), getWidth());
        }
    }
}
