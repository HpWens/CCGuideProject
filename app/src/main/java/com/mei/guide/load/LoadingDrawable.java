package com.mei.guide.load;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Handler;


/**
 * 加载loading
 */
public class LoadingDrawable extends Drawable {

    private static final int KEEP_TIME = 12;
    private static final int WAIT_TIME = 5;
    private static final int DEGREES_START = 9;
    private static final int DEGREES_END = 153;
    private static final int DEGREES_ITEM = 8;
    private static final int DEGREES_ITEM_LARGE = 60;
    private static final long DELAY_TIME = 30;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF mCircleSmallRectF = new RectF();
    private STATUS mSTATUS = STATUS.IDLE;
    private int mArcDegrees = 0;
    private int mCanvasDegrees = 0;
    private int mCurrentKeepTime = 0;
    private int mWaitKeepTime = 0;
    private int mStrokeWidth = 0;
    private Handler mHandler = new Handler();
    private boolean mShow = true;
    private int mWidth;

    public LoadingDrawable(Context context) {
        super();
        mStrokeWidth = dp2px(context, 3);
        mWidth = dp2px(context, 33);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setShader(new SweepGradient(mWidth / 2, mWidth / 2, new int[]{0xFF2A92FF, 0xFF00F2C2}, null));
        mArcDegrees = DEGREES_START;
    }

    @Override
    public void draw(Canvas canvas) {
        mCanvasDegrees %= 360;
        drawItem(canvas, mCanvasDegrees + 180);
        drawItem(canvas, mCanvasDegrees);
    }

    public void show(boolean show) {
        mShow = show;
        mArcDegrees = 0;
        mCanvasDegrees = 0;
        if (show) {
            HandlerUtils.release(mHandler);
            startAnimation();
        } else {
            HandlerUtils.release(mHandler);
        }
    }

    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public boolean isShow() {
        return mShow;
    }

    private void drawItem(Canvas canvas, int canvasDegrees) {
        canvas.save();
        canvas.translate(getBounds().left, getBounds().top);
        canvas.rotate(canvasDegrees, getBounds().width() / 2, getBounds().height() / 2);
        canvas.drawArc(mCircleSmallRectF, 0, mArcDegrees, false, mPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mCircleSmallRectF.left = mStrokeWidth;
        mCircleSmallRectF.top = mStrokeWidth;
        mCircleSmallRectF.right = bounds.width() - mStrokeWidth;
        mCircleSmallRectF.bottom = bounds.height() - mStrokeWidth;
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    private void startAnimation() {
        HandlerUtils.postDelayed(mHandler, new Runnable() {
            @Override
            public void run() {
                switch (mSTATUS) {
                    case IDLE:
                        mSTATUS = STATUS.ADD;
                        mArcDegrees += DEGREES_ITEM;
                        mCanvasDegrees += DEGREES_ITEM;
                        break;
                    case ADD:
                        mArcDegrees += DEGREES_ITEM;
                        if (mArcDegrees > DEGREES_END) {
                            mArcDegrees = DEGREES_END;
                            mSTATUS = STATUS.SUB;
                        }
                        mCanvasDegrees += DEGREES_ITEM;
                        break;
//					case KEEP:
//						mCurrentKeepTime++;
//						mCanvasDegrees += DEGREES_ITEM_LARGE;
//						if (mCurrentKeepTime >= KEEP_TIME) {
//							mCurrentKeepTime = 0;
//							mSTATUS = STATUS.SUB;
//						}
//						break;
                    case SUB:
                        mArcDegrees -= DEGREES_ITEM;
                        mCanvasDegrees += DEGREES_ITEM * 2;
                        if (mArcDegrees < DEGREES_START) {
                            mArcDegrees = DEGREES_START;
                            mSTATUS = STATUS.WAIT;
                        }
                        break;
                    case WAIT:
                        mWaitKeepTime++;
                        mCanvasDegrees += DEGREES_ITEM;
                        if (mWaitKeepTime >= WAIT_TIME) {
                            mWaitKeepTime = 0;
                            mSTATUS = STATUS.IDLE;
                        }
                }
                invalidateSelf();
                startAnimation();
            }
        }, DELAY_TIME);
    }

    private enum STATUS {
        IDLE,
        ADD,
        KEEP,
        SUB,
        WAIT,
    }

    @Override
    public int getIntrinsicHeight() {
        return mWidth;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }
}
