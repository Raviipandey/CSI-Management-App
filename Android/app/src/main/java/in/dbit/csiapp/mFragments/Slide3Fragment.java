package in.dbit.csiapp.mFragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import in.dbit.csiapp.R;

import in.dbit.csiapp.R;
public class Slide3Fragment extends Fragment {
    @NonNull
    @Override
    public CreationExtras getDefaultViewModelCreationExtras() {
        return super.getDefaultViewModelCreationExtras();
    }


    public Slide3Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide3_fragment, container, false);

        // Share the App
        view.findViewById(R.id.shareLayout).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            // Add your app's link in the EXTRA_TEXT field
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out CSI-DBIT app! https://play.google.com/store/apps/details?id=in.dbit.csiapp");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });


        // Rate Us on Google Play
        view.findViewById(R.id.rateLayout).setOnClickListener(v -> {
            final String appPackageName = getActivity().getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        // Contact Us
        view.findViewById(R.id.contactLayout).setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:csidbit.management@gmail.com"));
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
        });

        return view;
    }
}
