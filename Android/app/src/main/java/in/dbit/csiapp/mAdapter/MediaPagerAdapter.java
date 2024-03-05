package in.dbit.csiapp.mAdapter;
import com.example.csi.R;
// MediaPagerAdapter.java


import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MediaPagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> mediaUrls;
    private LayoutInflater layoutInflater;

    public MediaPagerAdapter(Context context, List<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mediaUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.item_media, container, false);

        String mediaUrl = mediaUrls.get(position);

        if (isImage(mediaUrl)) {
            ImageView imageView = itemView.findViewById(R.id.imageView);
            // Load image using Glide
            Glide.with(context).load(mediaUrl).into(imageView);
        } else if (isVideo(mediaUrl)) {
            VideoView videoView = itemView.findViewById(R.id.videoView);
            // Load video using the VideoView
            videoView.setVideoURI(Uri.parse(mediaUrl));
            videoView.start();
        }

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private boolean isImage(String mediaUrl) {
        // Convert the mediaUrl to lowercase for case-insensitive comparison
        String lowerCaseUrl = mediaUrl.toLowerCase();
        return lowerCaseUrl.endsWith(".jpg") || lowerCaseUrl.endsWith(".png");
    }

    private boolean isVideo(String mediaUrl) {
        // Convert the mediaUrl to lowercase for case-insensitive comparison
        String lowerCaseUrl = mediaUrl.toLowerCase();
        return lowerCaseUrl.endsWith(".mp4") || lowerCaseUrl.endsWith(".avi");
    }
}
