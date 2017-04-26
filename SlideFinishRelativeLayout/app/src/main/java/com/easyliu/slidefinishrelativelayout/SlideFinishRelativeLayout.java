package com.easyliu.slidefinishrelativelayout;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by EasyLiu on 09/04/2017.
 */

public class SlideFinishRelativeLayout extends RelativeLayout {
    private static final String TAG = SlideFinishRelativeLayout.class.getSimpleName();
    private static final boolean DEBUG = true;
    private IOnSlideToFinish mOnSlideToFinish;
    private VelocityTracker mVelocityTracker;
    private int mSlideFinishVelocity;//滑动finish的速度，当大于等于这个速度的时候不管是否滑动到有效距离，直接finish
    private int mMaximumVelocity;//最大速度
    private Scroller mScroller;
    private int mTouchSlop;//最小滑动距离
    private int mDownX;//Action_Down事件的X轴坐标，用于判断边界滑动是否有效
    private int mLastX;
    private int mLastY;
    private ViewGroup mParentView;//父ViewGroup，作为滑动的对象
    private int mWidth;
    private boolean mIsFinishing;//是否正在结束
    private boolean mIsOriginal;//是不是在初始位置
    private SlideMode mSlideMode;//滑动模式
    private int mSlideEdgeXMax; //边界滑动的时候落点X轴坐标的最大值
    private boolean mIsBeingDraging;//是否正在拖动，用于事件拦截
    private boolean mIsSlideEnable; //是否使能滑动
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    private static final float TIME_FRACTION_LEFT = (float) 1.4;
    private static final float TIME_FRACTION_RIGHT = (float) 0.4;
    private static final float TIME_FRACTION_RIGHT_IMMEDIATELY = (float) 0.15;
    private static final float SLIDE_FINISH_PARTITION = (float) (1 / 3.0);
    private static final float EDGE_DOWN_X_MAX_PARTITION = (float) 1 / 10;
    private static final int DEFAULT_MINIMUM_SLIDE_FINISH_VELOCITY = 3000;//最小的滑动finish速度,单位为pix/s

    //滑动模式
    public enum SlideMode {
        EDGD, //边缘
        ALL   //全部
    }

    public SlideFinishRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mSlideFinishVelocity = DEFAULT_MINIMUM_SLIDE_FINISH_VELOCITY;
        mIsOriginal = true;
        mSlideMode = SlideMode.EDGD;
        mSlideEdgeXMax =
                (int) (context.getResources().getDisplayMetrics().widthPixels * EDGE_DOWN_X_MAX_PARTITION);
        mIsSlideEnable = true;
    }

    public void setSlideEnable(boolean slideEnable) {
        mIsSlideEnable = slideEnable;
    }

    public void setSlideMode(SlideMode slideMode) {
        mSlideMode = slideMode != null ? slideMode : SlideMode.EDGD;
    }

    public void setOnSildeToFinishListener(IOnSlideToFinish onSlideToFinish) {
        this.mOnSlideToFinish = onSlideToFinish;
    }

    public interface IOnSlideToFinish {
        void onFinish();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mParentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else {
            //滑动完成，判断是否需要finish
            if (mOnSlideToFinish != null && mIsFinishing) {
                mOnSlideToFinish.onFinish();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        //如果不使能就不拦截
        if (!mIsSlideEnable) {
            return false;
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // 如果这个触摸被取消了，或者手指抬起来了就不拦截
            resetTouchState();
            return false;
        }
        //正在拖动且不是ACTION_DOWN事件，就拦截
        if (action != MotionEvent.ACTION_DOWN && mIsBeingDraging) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getRawX();
                mLastX = mDownX;
                mLastY = (int) ev.getRawY();
                mIsBeingDraging = false;//在Action_down的时候不拦截，如果拦截的话，接下来的up和cancel事件都是交给自身处理，
                // 子View就收不到事件了,且接下来onIntercept方法都不会再调用了
                mActivePointerId = ev.getPointerId(0);
                if (DEBUG)
                    Log.d(TAG, "Intercept Action_Down mLastX=:" + mLastX + " mLastY=:" + mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getRawX();
                int moveY = (int) ev.getRawY();
                if (DEBUG) Log.d(TAG, "Intercept Action_MOVE moveX=:" + moveX + " moveY=:" + moveY);
                int deltaX = moveX - mLastX;
                int deltaY = moveY - mLastY;
                if (Math.abs(deltaX) > mTouchSlop && Math.abs(deltaY) < Math.abs(deltaX)) {
                    mIsBeingDraging = true;//初始化为拦截
                    requestParentDisallowInterceptTouchEvent(true);
                    if (mSlideMode == SlideMode.EDGD) {
                        if (mDownX > mSlideEdgeXMax) {
                            mIsBeingDraging = false;
                        }
                    } else {//不是边界滑动的时候，需要考虑到滑动冲突
                        if (deltaX != 0 && canScroll(this, false, deltaX, moveX, moveY)) {
                            mIsBeingDraging = false;
                        }
                    }
                }
                break;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        return mIsBeingDraging;
    }

    /**
     * 控制父View是否拦截事件
     *
     * @param disallowIntercept
     */
    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /**
     * 这段代码来自于ViewPager，用于判断视图V的子视图是否可以滑动，从而进行事件拦截
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     *               or just its children (false).
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child
                        .getTop() && y + scrollY < child.getBottom() && canScroll(child, true, dx,
                        x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && ViewCompat.canScrollHorizontally(v, -dx);
    }

    private void resetTouchState() {
        if ((mScroller != null) && !mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        releaseVelocityTracker();
        mIsBeingDraging = false;
        mIsFinishing = false;
        mIsOriginal = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果不使能就不响应
        if (!mIsSlideEnable) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mLastX = mDownX = (int) event.getRawX();
                mLastY = (int) event.getRawY();
                mActivePointerId = event.getPointerId(0);
                if (DEBUG) Log.d(TAG, "Action_Down mLastX=:" + mLastX + " mLastY=:" + mLastY);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getRawX();
                int moveY = (int) event.getRawY();
                if (DEBUG) Log.d(TAG, "Action_MOVE moveX=:" + moveX + " moveY=:" + moveY);
                int deltaX = moveX - mLastX;
                int deltaY = moveY - mLastY;
                mLastX = moveX;
                mLastY = moveY;
                //特殊情况：当没有子类或者有子类但是没有响应事件的时候，
                //onIntercept只会在Action_Down的时候才会调用，此时mIsBeingDraging就为false
                if (!mIsBeingDraging) {
                    if (Math.abs(deltaX) > mTouchSlop && Math.abs(deltaY) < Math.abs(deltaX)) {
                        mIsBeingDraging = true;
                    }
                }
                if (mIsBeingDraging) {
                    if (deltaX > 0) {//往右
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        float xVelocity = VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);//得到x方向的速度
                        if (xVelocity >= mSlideFinishVelocity) {
                            mIsFinishing = true;
                            mIsBeingDraging = false;//防止下面的ACTION_UP里面的代码再次调用
                            scrollToFinishImmediately();   //大于某一个速度,直接finish
                            if (DEBUG) Log.d(TAG, "scrollToFinishImmediately");
                            break;
                        }
                        mParentView.scrollBy(-deltaX, 0);
                        mIsOriginal = false;
                    } else {
                        //往左
                        if (!mIsOriginal) {
                            //最多到最左边
                            int currentScrollX = mParentView.getScrollX();
                            if (currentScrollX <= 0) {
                                int delta = currentScrollX - deltaX > 0 ? 0 : -deltaX;
                                mParentView.scrollBy(delta, 0);
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsBeingDraging) {
                    mIsBeingDraging = false;
                    if (mParentView.getScrollX() <= (int) (-mWidth * SLIDE_FINISH_PARTITION)) {
                        mIsFinishing = true;
                        scrollToFinish();
                        if (DEBUG) Log.d(TAG, "scrollToFinish");
                    } else {
                        mIsFinishing = false;
                        scrollToOriginal();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 释放速度追踪器
     */
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            mParentView = (ViewGroup) this.getParent();
            mWidth = this.getWidth();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if ((mScroller != null) && !mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        super.onDetachedFromWindow();
    }

    /**
     * 滑动到初始位置
     */
    private void scrollToOriginal() {
        int deltaX = -mParentView.getScrollX();
        mScroller.startScroll(mParentView.getScrollX(), 0, deltaX, 0,
                (int) (deltaX * TIME_FRACTION_LEFT));
        postInvalidate();
        mIsOriginal = true;
    }

    /**
     * 滑动finish
     */
    private void scrollToFinish() {
        int deltaX = -(mParentView.getScrollX() + mWidth);
        mScroller.startScroll(mParentView.getScrollX(), 0, deltaX + 1, 0,
                (int) (-deltaX * TIME_FRACTION_RIGHT));
        postInvalidate();
    }

    /**
     * 立即结束
     */
    private void scrollToFinishImmediately() {
        int deltaX = -(mParentView.getScrollX() + mWidth);
        mScroller.startScroll(mParentView.getScrollX(), 0, deltaX + 1, 0,
                (int) (-deltaX * TIME_FRACTION_RIGHT_IMMEDIATELY));
        postInvalidate();
    }
}
