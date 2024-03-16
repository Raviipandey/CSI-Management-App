package in.dbit.csiapp.mFragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.ViewPagerAdapter;
import in.dbit.csiapp.mActivityManager.Creative;
import in.dbit.csiapp.mActivityManager.Report;
import in.dbit.csiapp.mActivityManager.Technical;
import in.dbit.csiapp.mActivityManager.praposal_recycler;
import in.dbit.csiapp.mActivityManager.publcity_recycler;
import in.dbit.csiapp.Gallery.Activities.DisplayEventName;

import in.dbit.csiapp.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;


public class ActivityManager extends Fragment  {

    String roll_text;

    View rootView;
    public String server_url;
    GridLayout mainGrid;
    //below 4 objects are used for image slider
    ViewPager viewPager;
    LinearLayout sliderDotsPanel;
    private int dotsCount;
    private ImageView[] dots;
    private SharedPreferenceConfig preferenceConfig;


    public  static ActivityManager newInstance()
    {
        return new ActivityManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.READ_MEDIA_IMAGES},100);
            }
        }
        rootView = inflater.inflate(R.layout.manager_activity,container,false);
//        ScrollView sv = new ScrollView(this);
//        sv.addView(rootView);
        getActivity().setTitle("Activity Manager");
        mainGrid = (GridLayout) rootView.findViewById(R.id.mainGrid);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        Bundle bundle = getArguments();
        Log.i("Sanket_testing",bundle.toString());
        roll_text = this.getArguments().getString("uRole");
        preferenceConfig = new SharedPreferenceConfig(getActivity().getApplicationContext());
        roll_text=preferenceConfig.readRoleStatus();

        sliderDotsPanel = (LinearLayout) rootView.findViewById(R.id.SliderDots);

        //Fetch image URLs and setup the ViewPager
        fetchImageUrls();

        //set Event
        setSingleEvent(mainGrid);


        return rootView;


    }

    private void fetchImageUrls() {
        String url = getActivity().getApplicationContext().getResources().getString(R.string.server_url) + "/images/list-images"; // Adjust to your server's actual URL

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<String> imageUrls = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            String imageUrl = response.getString(i);
                            Log.d("Image URL", imageUrl); // Log each URL
                            imageUrls.add(imageUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    imageSlider(imageUrls); // Proceed to set up the image slider with the URLs
                },
                error -> error.printStackTrace());

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext()); // Ensure context is available
        requestQueue.add(jsonArrayRequest);
    }





    // image slider animation
    public static class DepthPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float position) {

            if (position < -1){    // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.setAlpha(0);

            }
            else if (position <= 0){    // [-1,0]
                page.setAlpha(1);
                page.setTranslationX(0);
                page.setScaleX(1);
                page.setScaleY(1);

            }
            else if (position <= 1){    // (0,1]
                page.setTranslationX(-position*page.getWidth());
                page.setAlpha(1-Math.abs(position));
                page.setScaleX(1-Math.abs(position));
                page.setScaleY(1-Math.abs(position));

            }
            else {    // (1,+Infinity]
                // This page is way off-screen to the right.
                page.setAlpha(0);

            }


        }
    }
// animation ends


    private void imageSlider(List<String> imageUrls) {


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUrls);

        viewPager.setAdapter(viewPagerAdapter);

        dotsCount = viewPagerAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i =0; i < dotsCount; i++){

            dots[i] = new ImageView(getActivity());
            dots[i].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.nonactive_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            sliderDotsPanel.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i< dotsCount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nonactive_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new MyTimeTask(), 2000, 4000);

    }

    public class MyTimeTask extends TimerTask {

        @Override
        public void run() {

            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (viewPager.getCurrentItem() == 0){
                        viewPager.setCurrentItem(1);
                    } else if(viewPager.getCurrentItem() == 1){
                        viewPager.setCurrentItem(2);
                    } else if(viewPager.getCurrentItem() == 2){
                        viewPager.setCurrentItem(3);
                    } else if(viewPager.getCurrentItem() == 3){
                        viewPager.setCurrentItem(4);
                    } else if(viewPager.getCurrentItem() == 4){
                        viewPager.setCurrentItem(5);
                    } else if(viewPager.getCurrentItem() == 5){
                        viewPager.setCurrentItem(6);
                    } else if(viewPager.getCurrentItem() == 6){
                        viewPager.setCurrentItem(0);
                    }

                }
            });

        }
    }

    private void setSingleEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for(int i=0; i<mainGrid.getChildCount(); i++){
            //You can see, all child item is CardView, so we just object to CardView
            CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getActivity(), "Clicked at index "+ finalI, Toast.LENGTH_SHORT).show();
                    switch (finalI){
                        case 0:
                            startActivity(new Intent(getActivity(), praposal_recycler.class));
                            break;
                        case 1:
                            Intent creative = new Intent(getActivity(), Creative.class);
//                            Toast.makeText(getActivity(), roll_text, Toast.LENGTH_SHORT).show();
                            creative.putExtra("uRole",roll_text);
                            startActivity(creative);
                            break;
                        case 2: startActivity(new Intent(getActivity(), publcity_recycler.class));
                            break;
                        case 3: startActivity(new Intent(getActivity(), Technical.class));
                            break;
                        case 4: startActivity(new Intent(getActivity(), DisplayEventName.class));
                            break;
                        case 5: startActivity(new Intent(getActivity(), Report.class));
                            break;
                    }
                }
            });
        }
    }

    public String toString() {
        return "Activity Manager";
    }
}
