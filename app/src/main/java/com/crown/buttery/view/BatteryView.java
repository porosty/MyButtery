package com.crown.buttery.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.crown.buttery.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * 自定义电池控件
 */

public class BatteryView extends ImageView {

    private static final String TAG = "BatteryView";

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;

    private static final int STARCOUNT = 100;

    private static final float[][] STAR_LOCATION = new float[][]{
            {0.5f, 0.2f}, {0.68f, 0.35f}, {0.5f, 0.05f},
            {0.15f, 0.15f}, {0.5f, 0.5f}, {0.15f, 0.8f},
            {0.2f, 0.3f}, {0.77f, 0.4f}, {0.75f, 0.5f},
            {0.8f, 0.55f}, {0.9f, 0.6f}, {0.1f, 0.7f},
            {0.1f, 0.1f}, {0.7f, 0.8f}, {0.5f, 0.6f}
    };

    private Resources mResources;
    private Bitmap mStarBitmap;
    private Drawable mBackgroundDrawable;
    private Drawable mFrameDrawable;
    private Drawable mTopMaskDrawable;
    private Bitmap mBrushLineBitmap;
    private Bitmap mBrushTailBitmap;

    private int mStarWidth;
    private int mStarHeight;

    private int mFloatTransLowSpeed;
    private int mFloatTransMidSpeed;
    private int mFloatTransFastSpeed;

    private int mTotalWidth;
    private int mTotalHeight;
    private Rect mRectContent;
    private Paint mPaint;
    private ArrayList<StarInfo> mStarInfos = new ArrayList<StarInfo>();
    private Rect mSrcRect;
    private Rect mDestRect;
    private int blushHeight = 0;
    private Rect mBrushLineRect;
    private Rect mBrushTailRect;

    public BatteryView(Context context) {
        this(context, null);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mResources = getContext().getResources();
        mPaint = new Paint();
        mFloatTransLowSpeed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f,
                mResources.getDisplayMetrics());
        mFloatTransMidSpeed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.75f,
                mResources.getDisplayMetrics());
        mFloatTransFastSpeed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f,
                mResources.getDisplayMetrics());
        initBitmapInfo();
    }

    /**
     * 初始化小球位图信息
     */
    private void initBitmapInfo() {
        mFrameDrawable = mResources.getDrawable(R.drawable.bottom_battery_frame);
        mBackgroundDrawable = mResources.getDrawable(R.drawable.bottom_battery_bg);
        mTopMaskDrawable = mResources.getDrawable(R.drawable.top_battery_mask);

        mStarBitmap = ((BitmapDrawable) mResources.getDrawable(R.drawable.star)).getBitmap();
        mBrushLineBitmap = ((BitmapDrawable) mResources.getDrawable(R.drawable.brush_line)).getBitmap();
        mBrushTailBitmap = ((BitmapDrawable) mResources.getDrawable(R.drawable.brush_tail)).getBitmap();

        mStarWidth = mStarBitmap.getWidth();
        mStarHeight = mStarBitmap.getHeight();

        mSrcRect = new Rect(0, 0, mStarWidth, mStarHeight);
        mBrushLineRect = new Rect(0, 0, mBrushLineBitmap.getWidth(), mBrushLineBitmap.getHeight());
        mBrushTailRect = new Rect(0, 0, mBrushTailBitmap.getWidth(), mBrushTailBitmap.getHeight());
        mDestRect = new Rect();
    }

    /**
     * 创建球对象
     */
    private void initStarInfo() {
        StarInfo starInfo = null;
        Random random = new Random();
        for (int i = 0; i < STARCOUNT; i++) {
            // 初始化星球大小
            starInfo = new StarInfo();
            // 获取星球大小比例
            float starSize = getStarSize(0.4f, 0.9f);
            starInfo.sizePercent = starSize;
            // 初始化漂浮速度
            int randomSpeed = random.nextInt(3);
            switch (randomSpeed) {
                case 0:
                    starInfo.speed = mFloatTransLowSpeed;
                    break;
                case 1:
                    starInfo.speed = mFloatTransMidSpeed;
                    break;
                case 2:
                    starInfo.speed = mFloatTransFastSpeed;
                    break;
                default:
                    starInfo.speed = mFloatTransMidSpeed;
                    break;
            }
            // 初始化星球透明度
            starInfo.alpha = getStarSize(0.3f, 0.8f);
            // 初始化星球位置
            float[] starLocation = STAR_LOCATION[i % 15];
            starInfo.xLocation = (int) (starLocation[0] * mTotalWidth);
            starInfo.yLocation = (int) (starLocation[1] * mTotalHeight);
            Log.i(TAG, "xLocation = " + starInfo.xLocation + "--yLocation = "
                    + starInfo.yLocation);
            Log.i(TAG, "stoneSize = " + starSize + "---stoneAlpha = "
                    + starInfo.alpha);
            // 初始化星球运动方向
            starInfo.direction = getStarDirection();

            mStarInfos.add(starInfo);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制电池背景
        mBackgroundDrawable.setBounds(mRectContent);
        mBackgroundDrawable.draw(canvas);

        //绘制飘浮的球
        for (StarInfo starInfo : mStarInfos) {
            drawStarDynamic(starInfo, canvas, mPaint);
            resetStarFloat(starInfo);
        }

        //绘制电池前景
        mFrameDrawable.setBounds(mRectContent);
        mFrameDrawable.draw(canvas);

        //绘制电池膜
        mTopMaskDrawable.setBounds(mRectContent);
        canvas.save();
        int top = mTotalHeight - blushHeight;
        canvas.clipRect(0, 0, mTotalWidth, top);
        mTopMaskDrawable.draw(canvas);
        canvas.restore();

        //绘制扫描线尾巴
        mDestRect.set(0, top, mTotalWidth, top + blushHeight/5);
        canvas.drawBitmap(mBrushTailBitmap, mBrushTailRect, mDestRect, null);

        //绘制扫描线
        mDestRect.set(0, top, mTotalWidth, top + mBrushLineBitmap.getHeight());
        canvas.drawBitmap(mBrushLineBitmap, mBrushLineRect, mDestRect, null);

        blushHeight = blushHeight + 10;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();

        initStarInfo();
        mRectContent = new Rect(0, 0, mTotalWidth, mTotalHeight);
    }

    /**
     * 初始化星球运行方向
     */
    private int getStarDirection() {
        Random random = new Random();
        int randomInt = random.nextInt(4);
        int direction = 0;
        switch (randomInt) {
            case 0:
                direction = LEFT;
                break;
            case 1:
                direction = RIGHT;
                break;
            case 2:
                direction = TOP;
                break;
            case 3:
                direction = BOTTOM;
                break;

            default:
                break;
        }
        return direction;
    }

    /**
     * 随机产生球大小
     */
    private float getStarSize(float start, float end) {
        float nextFloat = (float) Math.random();
        if (start < nextFloat && nextFloat < end) {
            return nextFloat;
        } else {
            // 如果不处于想要的数据段，则再随机一次，因为不断递归有风险
            return (float) Math.random();
        }
    }

    /**
     * 绘制一个球
     *
     * @param starInfo
     * @param canvas
     * @param paint
     */
    private void drawStarDynamic(StarInfo starInfo,
                                 Canvas canvas, Paint paint) {

        float starAlpha = starInfo.alpha;
        int xLocation = starInfo.xLocation;
        int yLocation = starInfo.yLocation;
        float sizePercent = starInfo.sizePercent;

        xLocation = (int) (xLocation / sizePercent);
        yLocation = (int) (yLocation / sizePercent);

        mDestRect.set(xLocation, yLocation, xLocation + mSrcRect.width(), yLocation + mSrcRect.height());

        paint.setAlpha((int) (starAlpha * 255));
        canvas.save();
        canvas.scale(sizePercent, sizePercent);
        canvas.drawBitmap(mStarBitmap, mSrcRect, mDestRect, paint);
        canvas.restore();
    }

    /**
     * 根据速度，修改 star 的位置
     *
     * @param starInfo
     */
    private void resetStarFloat(StarInfo starInfo) {
        switch (starInfo.direction) {
            case LEFT:
                starInfo.xLocation -= starInfo.speed;
                break;
            case RIGHT:
                starInfo.xLocation += starInfo.speed;
                break;
            case TOP:
                starInfo.yLocation -= starInfo.speed;
                break;
            case BOTTOM:
                starInfo.yLocation += starInfo.speed;
                break;
            default:
                break;
        }
    }

    /**
     * 星球
     *
     * @author AJian
     */
    private static class StarInfo {
        // 缩放比例
        float sizePercent;
        // x位置
        int xLocation;
        // y位置
        int yLocation;
        // 透明度
        float alpha;
        // 漂浮方向
        int direction;
        // 漂浮速度
        int speed;
    }
}
