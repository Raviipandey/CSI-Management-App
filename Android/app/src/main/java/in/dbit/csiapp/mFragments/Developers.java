package in.dbit.csiapp.mFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.dbit.csiapp.R;

public class Developers extends Fragment {
    View rootView;
    public static Developers newInstance() {
        return new Developers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_developers, container, false);
        getActivity().setTitle("Developers");
        return rootView;
    }

}
