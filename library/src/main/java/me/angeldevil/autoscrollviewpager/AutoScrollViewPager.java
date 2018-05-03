package me.angeldevil.autoscrollviewpager;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class AutoScrollViewPager extends ViewPager {

    private InnerOnPageChangeListener innerOnPageChangeListener = new InnerOnPageChangeListener();;

    public interface OnPageClickListener {
        void onPageClick(AutoScrollViewPager pager, int position);
    }

    private static final int MSG_AUTO_SCROLL = 0;
    private static final int DEFAULT_INTERNAL_IM_MILLIS = 2000;

    private PagerAdapter wrappedPagerAdapter;
    private PagerAdapter wrapperPagerAdapter;
    private OnPageChangeListener mOnPageChangeListener;
    private List<OnPageChangeListener> mOnPageChangeListeners = new LinkedList<>();
    private AutoScrollFactorScroller scroller;
    private H handler;

    private InnerDataSetObserver mObserver = new InnerDataSetObserver();

    private boolean autoScroll = false;
    private int intervalInMillis;

    private float mInitialMotionX;
    private float mInitialMotionY;
    private int touchSlop;
    private OnPageClickListener onPageClickListener;

    private class H extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTO_SCROLL:
                    setCurrentItem(getCurrentItem() + 1);
                    sendEmptyMessageDelayed(MSG_AUTO_SCROLL, intervalInMillis);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public AutoScrollViewPager(Context context) {
        super(context);
        init();
    }

    public AutoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        super.addOnPageChangeListener(innerOnPageChangeListener);

        handler = new H();
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pauseAutoScroll();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (autoScroll) {
            startAutoScroll();
        }
    }

    public void startAutoScroll() {
        startAutoScroll(intervalInMillis != 0 ? intervalInMillis : DEFAULT_INTERNAL_IM_MILLIS);
    }

    public void startAutoScroll(int intervalInMillis) {
        // Only post scroll message when necessary.
        if (getCount() > 1) {
            this.intervalInMillis = intervalInMillis;
            autoScroll = true;
            pauseAutoScroll();
            handler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, intervalInMillis);
        }
    }

    public void stopAutoScroll() {
        autoScroll = false;
        pauseAutoScroll();
    }

    public void setInterval(int intervalInMillis) {
        this.intervalInMillis = intervalInMillis;
    }

    public void setScrollFactor(double factor) {
        setScrollerIfNeeded();
        scroller.setFactor(factor);
    }

    public OnPageClickListener getOnPageClickListener() {
        return onPageClickListener;
    }

    public void setOnPageClickListener(OnPageClickListener onPageClickListener) {
        this.onPageClickListener = onPageClickListener;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (autoScroll) {
            if (hasWindowFocus) {
                startAutoScroll();
            } else {
                pauseAutoScroll();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListeners.add(listener);
    }

    @Override
    public void clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners();
        mOnPageChangeListeners.clear();
        super.addOnPageChangeListener(innerOnPageChangeListener);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (wrappedPagerAdapter != null && mObserver != null) {
            wrappedPagerAdapter.unregisterDataSetObserver(mObserver);
        }
        wrappedPagerAdapter = adapter;
        if (wrappedPagerAdapter != null && mObserver != null) {
            wrappedPagerAdapter.registerDataSetObserver(mObserver);
        }
        wrapperPagerAdapter = (wrappedPagerAdapter == null) ? null : new AutoScrollPagerAdapter(adapter);
        super.setAdapter(wrapperPagerAdapter);

        setCurrentItem(0, false);
    }

    @Override
    public PagerAdapter getAdapter() {
        // In order to be compatible with ViewPagerIndicator
        return wrappedPagerAdapter;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item + 1);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item + 1, smoothScroll);
    }

    @Override
    public int getCurrentItem() {
        int curr = super.getCurrentItem();
        if (wrappedPagerAdapter != null && wrappedPagerAdapter.getCount() > 1) {
            if (curr == 0) {
                curr = wrappedPagerAdapter.getCount() - 1;
            } else if (curr == wrapperPagerAdapter.getCount() - 1) {
                curr = 0;
            } else {
                curr = curr - 1;
            }
        }
        return curr;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (getCurrentItemOfWrapper() + 1 == getCountOfWrapper()) {
                    setCurrentItem(0, false);
                } else if (getCurrentItemOfWrapper() == 0) {
                    setCurrentItem(getCount() - 1, false);
                }
                pauseAutoScroll();
                mInitialMotionX = ev.getX();
                mInitialMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float lastMotionX = ev.getX();
                float lastMotionY = ev.getY();
                if ((int) Math.abs(lastMotionX - mInitialMotionX) > touchSlop
                        || (int) Math.abs(lastMotionY - mInitialMotionY) > touchSlop) {
                    mInitialMotionX = 0.0f;
                    mInitialMotionY = 0.0f;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (autoScroll) {
                    startAutoScroll();
                }

                // Manually swipe not affected by scroll factor.
                if (scroller != null) {
                    final double lastFactor = scroller.getFactor();
                    scroller.setFactor(1);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            scroller.setFactor(lastFactor);
                        }
                    });
                }

                lastMotionX = ev.getX();
                lastMotionY = ev.getY();
                if ((int) mInitialMotionX != 0 && (int) mInitialMotionY != 0) {
                    if ((int) Math.abs(lastMotionX - mInitialMotionX) < touchSlop
                            && (int) Math.abs(lastMotionY - mInitialMotionY) < touchSlop) {
                        mInitialMotionX = 0.0f;
                        mInitialMotionY = 0.0f;
                        if (onPageClickListener != null) {
                            onPageClickListener.onPageClick(this, getCurrentItem());
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get current item of the outer wrapper adapter.
     */
    private int getCurrentItemOfWrapper() {
        return super.getCurrentItem();
    }

    /**
     * Get item count of the outer wrapper adapter.
     */
    private int getCountOfWrapper() {
        if (wrapperPagerAdapter != null) {
            return wrapperPagerAdapter.getCount();
        }
        return 0;
    }

    /**
     * Get item count of the adapter which is set by user
     */
    private int getCount() {
        if (wrappedPagerAdapter != null) {
            return wrappedPagerAdapter.getCount();
        }
        return 0;
    }

    private void setScrollerIfNeeded() {
        if (scroller != null) {
            return;
        }
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            Field interpolatorField = ViewPager.class.getDeclaredField("sInterpolator");
            interpolatorField.setAccessible(true);
            scroller = new AutoScrollFactorScroller(getContext(), (Interpolator) interpolatorField.get(null));
            scrollerField.set(this, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseAutoScroll() {
        handler.removeMessages(MSG_AUTO_SCROLL);
    }

    private class InnerOnPageChangeListener implements OnPageChangeListener {
        private int mLastSelectedPage = -1;

        private InnerOnPageChangeListener() {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == SCROLL_STATE_IDLE && getCount() > 1) {
                if (getCurrentItemOfWrapper() == 0) {
                    // scroll to the last page
                    setCurrentItem(getCount() - 1, false);
                } else if (getCurrentItemOfWrapper() == getCountOfWrapper() - 1) {
                    // scroll to the first page
                    setCurrentItem(0, false);
                }
            }
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageScrollStateChanged(state);
            }
            for (OnPageChangeListener onPageChangeListener : mOnPageChangeListeners) {
                onPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mOnPageChangeListener != null && position > 0 && position < getCount()) {
                mOnPageChangeListener.onPageScrolled(position - 1, positionOffset, positionOffsetPixels);
            }
            for (OnPageChangeListener onPageChangeListener : mOnPageChangeListeners) {
                onPageChangeListener.onPageScrolled(position - 1, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            if (mOnPageChangeListener != null || mOnPageChangeListeners.size() > 0) {
                final int pos;
                // Fix position
                if (position == 0) {
                    pos = getCount() - 1;
                } else if (position == getCountOfWrapper() - 1) {
                    pos = 0;
                } else {
                    pos = position - 1;
                }

                if (mLastSelectedPage != pos) {
                    mLastSelectedPage = pos;
                    if (mOnPageChangeListener != null) {
                        mOnPageChangeListener.onPageSelected(pos);
                    }
                    for (OnPageChangeListener onPageChangeListener : mOnPageChangeListeners) {
                        onPageChangeListener.onPageSelected(pos);
                    }
                }
            }
        }
    }

    private class InnerDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            if (wrapperPagerAdapter != null) {
                wrapperPagerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onInvalidated() {
            if (wrapperPagerAdapter != null) {
                wrapperPagerAdapter.notifyDataSetChanged();
            }
        }
    }

}
