package com.sailor.imaging.core;

import android.graphics.*;
import android.util.Log;

import com.sailor.imaging.core.clip.IMGClip;
import com.sailor.imaging.core.clip.IMGClipWindow;
import com.sailor.imaging.core.homing.IMGHoming;
import com.sailor.imaging.core.sticker.IMGSticker;
import com.sailor.imaging.core.util.IMGUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 2017/11/21 下午10:03.
 */

public class IMGImage {

    private static final String TAG = "IMGImage";

    private Bitmap mBgBitmap, mDoodleBitmap;

    /**
     * 完整图片边框
     */
    private RectF mFrame = new RectF();

    /**
     * 裁剪图片边框（显示的图片区域）
     */
    private RectF mClipFrame = new RectF();

    private RectF mTempClipFrame = new RectF();

    /**
     * 裁剪模式前状态备份
     */
    private RectF mBackupClipFrame = new RectF();

    private float mBackupClipRotate = 0;

    private float mRotate = 0, mTargetRotate = 0;

    private boolean isRequestToBaseFitting = false;

    private boolean isAnimCanceled = false;

    /**
     * 裁剪模式时当前触摸锚点
     */
    private IMGClip.Anchor mAnchor;

    private boolean isSteady = true;

    private Path mShade = new Path();

    /**
     * 裁剪窗口
     */
    private IMGClipWindow mClipWin = new IMGClipWindow();


    /**
     * 编辑模式
     */
    private IMGMode mMode = IMGMode.NONE;

    private boolean isFreezing = mMode == IMGMode.CLIP;

    /**
     * 可视区域，无Scroll 偏移区域
     */
    private RectF mWindow = new RectF();

    /**
     * 是否初始位置
     */
    private boolean isInitialHoming = false;

    /**
     * 当前选中贴片
     */
    private IMGSticker mForeSticker;

    /**
     * 为被选中贴片
     */
    private List<IMGSticker> mBackStickers = new ArrayList<>();

    /**
     * 涂鸦路径
     */
    private List<IMGPath> mDoodles = new ArrayList<>();
    //被移除的涂鸦路径
    private List<IMGPath> mRevertDoodles = new ArrayList<>();

    /**
     * 马赛克路径
     */
    private List<IMGPath> mErraser = new ArrayList<>();

    private static final int MIN_SIZE = 500;

    private static final int MAX_SIZE = 10000;

    private Paint mPaint, mMosaicPaint, mShadePaint, mEraserPaint;

    private Matrix M = new Matrix();

    private static final boolean DEBUG = false;

    private static final Bitmap DEFAULT_IMAGE;

    private static final int COLOR_SHADE = 0xCC000000;

    static {
        DEFAULT_IMAGE = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    }

    private float mScale;

    {
        mShade.setFillType(Path.FillType.WINDING);

        // Doodle&Mosaic 's paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);


        //橡皮擦
        mEraserPaint = new Paint();
        mEraserPaint.setAlpha(0);
        //这个属性是设置paint为橡皮擦重中之重
        //这是重点
        //下面这句代码是橡皮擦设置的重点
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //上面这句代码是橡皮擦设置的重点（重要的事是不是一定要说三遍）
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH);
    }

    public IMGImage() {
        mBgBitmap = DEFAULT_IMAGE;

        if (mMode == IMGMode.CLIP) {
            initShadePaint();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        this.mBgBitmap = bitmap;
        onImageChanged();

    }

    public IMGMode getMode() {
        return mMode;
    }

    public void setMode(IMGMode mode) {

        if (this.mMode == mode) return;

        moveToBackground(mForeSticker);

        this.mMode = mode;
    }

    private void rotateStickers(float rotate) {
        M.setRotate(rotate, mClipFrame.centerX(), mClipFrame.centerY());
        for (IMGSticker sticker : mBackStickers) {
            M.mapRect(sticker.getFrame());
            sticker.setRotation(sticker.getRotation() + rotate);
            sticker.setX(sticker.getFrame().centerX() - sticker.getPivotX());
            sticker.setY(sticker.getFrame().centerY() - sticker.getPivotY());
        }
    }

    private void initShadePaint() {
        if (mShadePaint == null) {
            mShadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mShadePaint.setColor(COLOR_SHADE);
            mShadePaint.setStyle(Paint.Style.FILL);
        }
    }


    public boolean isDoodleEmpty() {
        return mDoodles.isEmpty();
    }


    public RectF getClipFrame() {
        return mClipFrame;
    }

    private Canvas mDoodleCanvas;

    private void makeDoodleBitmap() {
        if (mDoodleCanvas == null) {
            mDoodleBitmap = Bitmap.createBitmap((int) mFrame.right, (int) (mFrame.bottom - mFrame.top), Bitmap.Config.ARGB_8888);
            mDoodleCanvas = new Canvas(mDoodleBitmap);
        }
    }

    private void onImageChanged() {
        isInitialHoming = false;
        onWindowChanged(mWindow.width(), mWindow.height());

    }

    public RectF getFrame() {
        return mFrame;
    }

    public boolean onClipHoming() {
        return mClipWin.homing();
    }

    public IMGHoming getStartHoming(float scrollX, float scrollY) {
        return new IMGHoming(scrollX, scrollY, getScale(), getRotate());
    }

    public IMGHoming getEndHoming(float scrollX, float scrollY) {
        IMGHoming homing = new IMGHoming(scrollX, scrollY, getScale(), getTargetRotate());

        if (mMode == IMGMode.CLIP) {
            RectF frame = new RectF(mClipWin.getTargetFrame());
            frame.offset(scrollX, scrollY);
            if (mClipWin.isResetting()) {

                RectF clipFrame = new RectF();
                M.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                M.mapRect(clipFrame, mClipFrame);

                homing.rConcat(IMGUtils.fill(frame, clipFrame));
            } else {
                RectF cFrame = new RectF();

                // cFrame要是一个暂时clipFrame
                if (mClipWin.isHoming()) {
//
//                    M.mapRect(cFrame, mClipFrame);

//                    mClipWin

                    M.setRotate(getTargetRotate() - getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                    M.mapRect(cFrame, mClipWin.getOffsetFrame(scrollX, scrollY));

                    homing.rConcat(IMGUtils.fitHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()));


                } else {
                    M.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                    M.mapRect(cFrame, mFrame);
                    homing.rConcat(IMGUtils.fillHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()));
                }

            }
        } else {
            RectF clipFrame = new RectF();
            M.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
            M.mapRect(clipFrame, mClipFrame);

            RectF win = new RectF(mWindow);
            win.offset(scrollX, scrollY);
            homing.rConcat(IMGUtils.fitHoming(win, clipFrame, isRequestToBaseFitting));
            isRequestToBaseFitting = false;
        }

        return homing;
    }

    public <S extends IMGSticker> void addSticker(S sticker) {
        if (sticker != null) {
            moveToForeground(sticker);
        }
    }

    public void addPath(IMGPath path) {
        if (path == null) return;
        mDoodles.add(path);

    }

    private void moveToForeground(IMGSticker sticker) {
        if (sticker == null) return;

        moveToBackground(mForeSticker);

        if (sticker.isShowing()) {
            mForeSticker = sticker;
            // 从BackStickers中移除
            mBackStickers.remove(sticker);
        } else sticker.show();
    }

    private void moveToBackground(IMGSticker sticker) {
        if (sticker == null) return;

        if (!sticker.isShowing()) {
            // 加入BackStickers中
            if (!mBackStickers.contains(sticker)) {
                mBackStickers.add(sticker);
            }

            if (mForeSticker == sticker) {
                mForeSticker = null;
            }
        } else sticker.dismiss();
    }

    public void stickAll() {
        moveToBackground(mForeSticker);
    }

    public void onDismiss(IMGSticker sticker) {
        moveToBackground(sticker);
    }

    public void onShowing(IMGSticker sticker) {
        if (mForeSticker != sticker) {
            moveToForeground(sticker);
        }
    }

    public void onRemoveSticker(IMGSticker sticker) {
        if (mForeSticker == sticker) {
            mForeSticker = null;
        } else {
            mBackStickers.remove(sticker);
        }
    }

    public void onWindowChanged(float width, float height) {
        Log.e("TestTag", "IMGImage-onWindowChanged: w->" + width + " h:" + height);
        if (width == 0 || height == 0) {
            return;
        }

        mWindow.set(0, 0, width, height);

        if (!isInitialHoming) {
            onInitialHoming(width, height);
        } else {

            // Pivot to fit window.
            M.setTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
            M.mapRect(mFrame);
            M.mapRect(mClipFrame);
        }

        mClipWin.setClipWinSize(width, height);
    }

    private void onInitialHoming(float width, float height) {
        mFrame.set(0, 0, mBgBitmap.getWidth(), mBgBitmap.getHeight());
        mClipFrame.set(mFrame);
        mClipWin.setClipWinSize(width, height);

        if (mClipFrame.isEmpty()) {
            return;
        }

        toBaseHoming();

        isInitialHoming = true;
        onInitialHomingDone();
    }


    private void toBaseHoming() {
        if (mClipFrame.isEmpty()) {
            // Bitmap invalidate.
            return;
        }

        mScale = Math.min(
                mWindow.width() / mClipFrame.width(),
                mWindow.height() / mClipFrame.height()
        );

        // Scale to fit window.
        M.setScale(mScale, mScale, mClipFrame.centerX(), mClipFrame.centerY());
        M.postTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
        M.mapRect(mFrame);
        M.mapRect(mClipFrame);
    }

    private void onInitialHomingDone() {
        if (mMode == IMGMode.CLIP) {
            mClipWin.reset(mClipFrame, getTargetRotate());
        }
    }

    public void onDrawImage(Canvas canvas) {
        canvas.clipRect(mClipWin.isClipping() ? mFrame : mClipFrame);
        //创建水印图层
        makeDoodleBitmap();
        // 绘制背景图片
        canvas.drawBitmap(mBgBitmap, null, mFrame, null);
        //绘制水印图片
        canvas.drawBitmap(mDoodleBitmap, null, mFrame, null);
    }

    public void onDrawDoodles() {
        Log.e("TestTag", "IMGImage-onDrawDoodles:  mDoodles size--" + mDoodles.size());
        DoodleNumManager.INSTANCE.mListener.onDoodleNum(mDoodles.size(), mRevertDoodles.size());
        for (IMGPath path : mDoodles) {
            mDoodleCanvas.drawPath(path.getPath(), getPaint(path));
        }
    }

    public void drawPahth(Path path ,Paint paint){
        if (!path.isEmpty()){
            mRevertDoodles.clear();
        }
        mDoodleCanvas.drawPath(path,paint);
    }


    public void revertDoodle() {
        mDoodleCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        if (!mDoodles.isEmpty()) {
            //被移除的路径
            IMGPath path = mDoodles.get(mDoodles.size() - 1);
            mRevertDoodles.add(path);
            mDoodles.remove(mDoodles.size() - 1);
            Log.e("TestTag", "IMGImage-revertDoodle:mDoodles size--" + mDoodles.size());
        }
    }

    public void cancelRevertDoodle() {
        mDoodleCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        if (!mRevertDoodles.isEmpty()) {
            //取消移除的路径
            IMGPath path = mRevertDoodles.get(mRevertDoodles.size() - 1);
            mDoodles.add(path);
            mRevertDoodles.remove(mRevertDoodles.size() - 1);
        }
    }

    private Paint getPaint(IMGPath path) {
        switch (path.getMode()) {
            case DOODLE:
                mPaint.setColor(path.getColor());
                mPaint.setStrokeWidth(path.getWidth());
                return mPaint;
            case ERRASER:
                mEraserPaint.setStrokeWidth(path.getWidth());
                return mEraserPaint;
        }
        return mPaint;
    }

    public Canvas getCanvas() {
        return mDoodleCanvas;
    }


    public void onDrawStickerClip(Canvas canvas) {
        M.setRotate(getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        M.mapRect(mTempClipFrame, mClipWin.isClipping() ? mFrame : mClipFrame);
        canvas.clipRect(mTempClipFrame);
    }

    public void onDrawStickers(Canvas canvas) {
        if (mBackStickers.isEmpty()) return;
        canvas.save();
        for (IMGSticker sticker : mBackStickers) {
            if (!sticker.isShowing()) {
                float tPivotX = sticker.getX() + sticker.getPivotX();
                float tPivotY = sticker.getY() + sticker.getPivotY();

                canvas.save();
                M.setTranslate(sticker.getX(), sticker.getY());
                M.postScale(sticker.getScale(), sticker.getScale(), tPivotX, tPivotY);
                M.postRotate(sticker.getRotation(), tPivotX, tPivotY);

                canvas.concat(M);
                sticker.onSticker(canvas);
                canvas.restore();
            }
        }
        canvas.restore();
    }


    public void onTouchDown(float x, float y) {
        isSteady = false;
        moveToBackground(mForeSticker);
    }

    public void onTouchUp(float scrollX, float scrollY) {
        if (mAnchor != null) {
            mAnchor = null;
        }
    }

    public void onSteady(float scrollX, float scrollY) {
        isSteady = true;
        onClipHoming();
        mClipWin.setShowShade(true);
    }


    public IMGHoming onScroll(float scrollX, float scrollY, float dx, float dy) {
        if (mMode == IMGMode.CLIP) {
            mClipWin.setShowShade(false);
            if (mAnchor != null) {

                RectF clipFrame = new RectF();
                M.setRotate(getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                M.mapRect(clipFrame, mFrame);

                RectF frame = mClipWin.getOffsetFrame(scrollX, scrollY);
                IMGHoming homing = new IMGHoming(scrollX, scrollY, getScale(), getTargetRotate());
                homing.rConcat(IMGUtils.fillHoming(frame, clipFrame, mClipFrame.centerX(), mClipFrame.centerY()));
                return homing;
            }
        }
        return null;
    }

    public float getTargetRotate() {
        return mTargetRotate;
    }

    public void setTargetRotate(float targetRotate) {
        this.mTargetRotate = targetRotate;
    }


    public float getRotate() {
        return mRotate;
    }

    public void setRotate(float rotate) {
        mRotate = rotate;
    }

    public float getScale() {
        return 1f * mFrame.width() / mBgBitmap.getWidth();
    }

    public void setScale(float scale) {
        setScale(scale, mClipFrame.centerX(), mClipFrame.centerY());
    }

    public void setScale(float scale, float focusX, float focusY) {
        onScale(scale / getScale(), focusX, focusY);
    }

    public void onScale(float factor, float focusX, float focusY) {

        if (factor == 1f) return;

        if (Math.max(mClipFrame.width(), mClipFrame.height()) >= MAX_SIZE
                || Math.min(mClipFrame.width(), mClipFrame.height()) <= MIN_SIZE) {
            factor += (1 - factor) / 2;
        }

        M.setScale(factor, factor, focusX, focusY);
        M.mapRect(mFrame);
        M.mapRect(mClipFrame);

        // 修正clip 窗口
        if (!mFrame.contains(mClipFrame)) {
//            mClipFrame.intersect(mFrame);
        }

        for (IMGSticker sticker : mBackStickers) {
            M.mapRect(sticker.getFrame());
            float tPivotX = sticker.getX() + sticker.getPivotX();
            float tPivotY = sticker.getY() + sticker.getPivotY();
            sticker.addScale(factor);
            sticker.setX(sticker.getX() + sticker.getFrame().centerX() - tPivotX);
            sticker.setY(sticker.getY() + sticker.getFrame().centerY() - tPivotY);
        }
    }


    public boolean isFreezing() {
        return isFreezing;
    }

    private void setFreezing(boolean freezing) {
        if (freezing != isFreezing) {
            rotateStickers(freezing ? -getRotate() : getTargetRotate());
            isFreezing = freezing;
        }
    }

    public void onHomingCancel(boolean isRotate) {
        isAnimCanceled = true;
        Log.d(TAG, "Homing cancel");
    }

    public void release() {
        if (mBgBitmap != null && !mBgBitmap.isRecycled()) {
            mBgBitmap.recycle();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (DEFAULT_IMAGE != null) {
            DEFAULT_IMAGE.recycle();
        }
    }
}
