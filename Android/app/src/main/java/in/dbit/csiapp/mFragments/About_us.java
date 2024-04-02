package in.dbit.csiapp.mFragments;

import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import in.dbit.csiapp.R;
import in.dbit.csiapp.mFragments.ScreenSlidePagerAdapter;

public class About_us extends Fragment {

    private ViewPager viewPager;
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_about_us, container, false);

        viewPager = rootView.findViewById(R.id.viewPager);
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = rootView.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        getActivity().setTitle("About Us");

        return rootView;
    }
}
