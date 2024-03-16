package in.dbit.csiapp.mActivityManager;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import in.dbit.csiapp.R;
import com.squareup.picasso.Picasso;

public class CreativePosterFull extends AppCompatActivity {

    String posterUrl;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creative_poster_full);

        Intent intent = getIntent();
        posterUrl = intent.getStringExtra("poster_url");

        imageView = findViewById(R.id.image);
        loadImageUrl(posterUrl);
//        Toast.makeText(this, posterUrl, Toast.LENGTH_SHORT).show();
    }

    private void loadImageUrl(String url) {
        Picasso.get().load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // Handle success
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle error
                    }
                });
    }

}