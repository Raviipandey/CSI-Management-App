package in.dbit.csiapp.mFragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.dbit.csiapp.R;

public class About_us extends Fragment {

    View rootView;

    public static About_us newInstance() {
        return new About_us();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_about_us, container, false);
        getActivity().setTitle("About Us");
        return rootView;
    }
}
