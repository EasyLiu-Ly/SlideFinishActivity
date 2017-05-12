package com.easyliu.slidefinishrelativelayout.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.easyliu.slidefinishrelativelayout.R;
import com.easyliu.slidefinishrelativelayout.slidefinish.BaseSlideFinishActivity;
import com.easyliu.slidefinishrelativelayout.slidefinish.SlideFinishRelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SlideFinishActivity extends BaseSlideFinishActivity {
    private ViewPager mViewPager;
    private InnerAdapter mAdapter;
    private static final int[] COLORS = {Color.MAGENTA, Color.CYAN, Color.GREEN, Color.RED};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_finish);
        initStatusBar();
        initView();
        setSlideMode(SlideFinishRelativeLayout.SlideMode.ALL);
        enableSlideFinish(true);
        mViewPager.setVisibility(View.VISIBLE);
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        List<View> list = new ArrayList<>();
        for (int i = 0; i < COLORS.length; i++) {
            ImageView imageView = new ImageView(this);
            ColorDrawable colorDrawable = new ColorDrawable();
            colorDrawable.setColor(COLORS[i]);
            imageView.setImageDrawable(colorDrawable);
            list.add(imageView);
        }
        mAdapter = new InnerAdapter(this, list);
        mViewPager.setAdapter(mAdapter);
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private static class InnerAdapter extends PagerAdapter {
        private List<View> mList;
        private List<String> mTitles;
        private WeakReference<Context> mContext;

        InnerAdapter(Context context, List<View> views) {
            mContext = new WeakReference<Context>(context);
            mList = views;
            for (int i = 0; i < mList.size(); i++) {
                if (mTitles == null) {
                    mTitles = new ArrayList<>();
                }
                mTitles.add("Title" + i);
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return mList.indexOf(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mList.get(position);
            ((ViewPager) container).addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(mList.get(position));
        }
    }
}
