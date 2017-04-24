package com.easyliu.slidefinishrelativelayout;

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;

/**
 * 需要有SlideFinish效果的Activity继承自这个自Activity即可
 */
public abstract class BaseSlideFinishActivity extends AppCompatActivity {
    private SlideFinishRelativeLayout mSlideFinishRelativeLayout;
    private boolean mIsOpenSlideFinish;
    private SlideFinishRelativeLayout.SlideMode mSlideMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mIsOpenSlideFinish = true;
        mSlideMode = SlideFinishRelativeLayout.SlideMode.EDGD;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (mIsOpenSlideFinish) {
            View viewRoot = LayoutInflater.from(this).inflate(R.layout.activity_base, null);
            mSlideFinishRelativeLayout =
                    (SlideFinishRelativeLayout) viewRoot.findViewById(R.id.layout_root);
            mSlideFinishRelativeLayout.setOnSildeToFinishListener(
                    new SlideFinishRelativeLayout.IOnSlideToFinish() {
                        @Override
                        public void onFinish() {
                            BaseSlideFinishActivity.this.finish();
                        }
                    });
            mSlideFinishRelativeLayout.setSlideMode(mSlideMode);
            mSlideFinishRelativeLayout.setSlideEnable(mIsOpenSlideFinish);
            ViewStub viewStub = (ViewStub) viewRoot.findViewById(R.id.layout_content);
            viewStub.setLayoutResource(layoutResID);
            viewStub.inflate();
            super.setContentView(viewRoot);
        } else {
            super.setContentView(layoutResID);
        }
    }

    /**
     * 设置滑动模式
     *
     * @param slideMode
     */

    protected void setSlideMode(SlideFinishRelativeLayout.SlideMode slideMode) {
        if (slideMode != null) {
            mSlideMode = slideMode;
            if (mSlideFinishRelativeLayout != null) {
                mSlideFinishRelativeLayout.setSlideMode(slideMode);
            }
        }
    }

    /**
     * 是否使能滑动finish
     *
     * @param isOpenSlideFinish
     */
    protected void enableSlideFinish(boolean isOpenSlideFinish) {
        mIsOpenSlideFinish = isOpenSlideFinish;
        if (mSlideFinishRelativeLayout != null) {
            mSlideFinishRelativeLayout.setSlideEnable(isOpenSlideFinish);
        }
    }
}
