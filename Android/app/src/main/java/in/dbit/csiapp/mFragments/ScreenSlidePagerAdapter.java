package in.dbit.csiapp.mFragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_PAGES = 3;

    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // Return a NEW Fragment instance in getItem (different for each position)
        switch(position) {
            case 0:
                return new Slide1Fragment();
            case 1:
                return new Slide2Fragment();
            case 2:
                return new Slide3Fragment();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}

