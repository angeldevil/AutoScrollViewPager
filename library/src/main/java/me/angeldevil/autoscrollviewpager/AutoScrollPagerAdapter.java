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
        return wrappedAdapter != null
               ? wrappedAdapter.getPageTitle(getRealPosition(position))
               : super.getPageTitle(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return wrappedAdapter != null ? wrappedAdapter.getItemPosition(object) : super.getItemPosition(object);
    }

    @Override
    public float getPageWidth(int position) {
        return wrappedAdapter != null
               ? wrappedAdapter.getPageWidth(getRealPosition(position))
               : super.getPageWidth(position);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (wrappedAdapter != null) {
            wrappedAdapter.setPrimaryItem(container, getRealPosition(position), object);
        } else {
            super.setPrimaryItem(container, position, object);
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return wrappedAdapter.instantiateItem(container, getRealPosition(position));
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

    private int getRealPosition(int position) {
        if (wrappedAdapter != null && wrappedAdapter.getCount() > 1) {
            if (position == 0) {
                return wrappedAdapter.getCount() - 1;
            } else if (position == wrappedAdapter.getCount() + 1) {
                return 0;
            } else {
                return position - 1;
            }
        }
        return position;
    }
}
