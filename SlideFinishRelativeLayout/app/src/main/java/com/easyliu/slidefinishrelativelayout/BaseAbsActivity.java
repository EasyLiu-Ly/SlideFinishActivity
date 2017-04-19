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
public abstract class BaseAbsActivity extends AppCompatActivity {
  private SlideFinishRelativeLayout mSlideFinishRelativeLayout;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void setContentView(@LayoutRes int layoutResID) {
    View viewRoot = LayoutInflater.from(this).inflate(R.layout.activity_base, null);
    mSlideFinishRelativeLayout =
        (SlideFinishRelativeLayout) viewRoot.findViewById(R.id.layout_root);
    mSlideFinishRelativeLayout.setOnSildeToFinishListener(
        new SlideFinishRelativeLayout.IOnSlideToFinish() {
          @Override public void onFinish() {
            BaseAbsActivity.this.finish();
          }
        });
    mSlideFinishRelativeLayout.setSlideMode(SlideFinishRelativeLayout.SlideMode.EDGD);
    ViewStub viewStub = (ViewStub) viewRoot.findViewById(R.id.layout_content);
    viewStub.setLayoutResource(layoutResID);
    viewStub.inflate();
    super.setContentView(viewRoot);
  }

  /**
   * 设置滑动模式
   */
  protected void setSlideMode(SlideFinishRelativeLayout.SlideMode slideMode) {
    if (mSlideFinishRelativeLayout != null) {
      mSlideFinishRelativeLayout.setSlideMode(slideMode);
    }
  }
}
