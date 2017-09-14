package me.angeldevil.autoscrollviewpager.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyActivity extends AppCompatActivity {

    private ViewPager contentPager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(configuration);

        contentPager = findViewById(R.id.pager);
        contentPager.setOffscreenPageLimit(2);
        contentPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                if (i == 0) {
                    return new AutoScrollPagerFragment();
                }
                return TextFragment.newInstance("Fragment " + i);
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
    }

}
