package me.angeldevil.autoscrollviewpager;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

class AutoScrollPagerAdapter extends PagerAdapter {

    private PagerAdapter wrappedAdapter;

    AutoScrollPagerAdapter(PagerAdapter wrapped) {
        wrappedAdapter = wrapped;
    }

    @Override
    public int getCount() {
        if (wrappedAdapter == null) {
            return 0;
        } else if (wrappedAdapter.getCount() > 1) {
            return wrappedAdapter.getCount() + 2;
        } else {
            return wrappedAdapter.getCount();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (position == 0) {
            return wrappedAdapter.instantiateItem(container, wrappedAdapter.getCount() - 1);
        } else if (position == wrappedAdapter.getCount() + 1) {
            return wrappedAdapter.instantiateItem(container, 0);
        } else {
            return wrappedAdapter.instantiateItem(container, position - 1);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        wrappedAdapter.destroyItem(container, position, object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return wrappedAdapter.isViewFromObject(view, o);
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        super.startUpdate(container);
        if (wrappedAdapter != null) {
            wrappedAdapter.startUpdate(container);
        }
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        super.finishUpdate(container);
        if (wrappedAdapter != null) {
            wrappedAdapter.finishUpdate(container);
        }
    }


    @Override
    public void restoreState(Parcelable bundle, ClassLoader classLoader) {
        if (wrappedAdapter != null) {
            wrappedAdapter.restoreState(bundle, classLoader);
        }
        super.restoreState(bundle, classLoader);
    }

    @Override
    public Parcelable saveState() {
        if (wrappedAdapter != null) {
            return wrappedAdapter.saveState();
        }
        return super.saveState();
    }
}
