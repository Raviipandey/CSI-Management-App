package in.dbit.csiapp.mActivityManager;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import in.dbit.csiapp.Prompts.Manager;
import in.dbit.csiapp.R;
import in.dbit.csiapp.mAdapter.MediaPagerAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Creative_form extends AppCompatActivity {

    String poster_url = "";
    String video_url = "";
    String uRole;
    private static final int REQUEST_CODE_FILE_PICKER = 1;
    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_VIDEO = 1;
    private Context context;

    private File mSelectedFile;
//    private String filePath;
    private LinearLayout mPreviewLayout;
    ImageView imagePreview;
    private Button floatingButton;
    private View newLayout;

    private ViewPager viewPager;
    private MediaPagerAdapter mediaPagerAdapter;
    CirclePageIndicator indicator ;
    private List<String> mediaUrls = new ArrayList<>();




    public String mediaType = "Image", eid;
    public String server_url;
    String name, theme, eventDate, description, creativeBudget, date1;
    String dSpeaker, dVenue, dFeeCSI, dFeeNonCSI, dPrize, dPublicityBudget, dGuestBudget;

    TextView eventName, eventTheme, event_date, eventDescription, creative_budget;
    TextView speaker, venue, fee_csi, fee_non_csi, prize, publicity_budget, guest_budget, video_preview;

    Button uploadImage, uploadVideo, submit;
    Uri selectedImage;
    OkHttpClient client;
    RequestBody request_body;
    ArrayList<RequestBody> images;
    ProgressDialog progress;


    public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/creative/viewpropdetail";
        setContentView(R.layout.activity_creative_form);
        mPreviewLayout = findViewById(R.id.preview_layout);





        Log.i("sanket testing", "entered");
        //Toast.makeText(this, "creative form", Toast.LENGTH_SHORT).show();
        eventName = (TextView)findViewById(R.id.name);
        eventTheme = (TextView)findViewById(R.id.theme);
        event_date = (TextView)findViewById(R.id.ed);
        speaker = (TextView) findViewById(R.id.speaker);
        venue = (TextView) findViewById(R.id.venue);
        fee_csi = (TextView) findViewById(R.id.fee_csi);
        fee_non_csi = (TextView) findViewById(R.id.fee_non_csi);
        prize = (TextView) findViewById(R.id.prize);
        eventDescription = (TextView)findViewById(R.id.desc_pd);
        creative_budget = (TextView)findViewById(R.id.cb);
        publicity_budget = (TextView) findViewById(R.id.pb);
        guest_budget = (TextView) findViewById(R.id.gb);
        video_preview = (TextView) findViewById(R.id.video_preview);
        imagePreview = (ImageView) findViewById(R.id.image_preview);

        Intent intent = getIntent();
        eid = intent.getStringExtra(Creative.EXTRA_EID);
        uRole = intent.getStringExtra("uRole");
        Log.i("cpm_id",eid);

        insertSrv();

        progress = new ProgressDialog(Creative_form.this);

        getSupportActionBar().setTitle("Creative Form");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        uploadImage = (Button) findViewById(R.id.uploadImage);
        uploadVideo = (Button) findViewById(R.id.uploadVideo);
        submit = (Button) findViewById(R.id.submit_praposal);



        if(!uRole.equals("Creative Head")) {
            uploadImage.setVisibility(View.GONE);
            uploadVideo.setVisibility(View.GONE);
            submit.setVisibility(View.GONE);

            TextView upload_text = findViewById(R.id.upload_text);
            upload_text.setVisibility(View.GONE);

            TextView image_text = findViewById(R.id.upload_image_text);
            image_text.setText("Poster");

            TextView video_text = findViewById(R.id.upload_video_text);
            video_text.setText("Video Url");
        }

        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.indicator);



        List<String> mediaUrls = new ArrayList<>();
// Add your media URLs here
//        mediaUrls.add("https://foundations.projectpythia.org/_images/GitHub-logo.png");
//        mediaUrls.add("http://192.168.1.106:9000/creative/Spark%20AR%20_Banner.jpg");
//        mediaUrls.add("https://foundations.projectpythia.org/_images/GitHub-logo.png");
//        mediaUrls.add("http://192.168.1.106:9000/creative/Spark%20AR%20_Poster.mp4");

//        mediaPagerAdapter = new MediaPagerAdapter(this, mediaUrls);
//        viewPager.setAdapter(mediaPagerAdapter);



        Button browsefile = findViewById(R.id.browse_file_button);
        browsefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBrowseFileButtonClick(view);
            }
        });



        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaType="Image";
                //Toast.makeText(Creative_form.this, mediaType, Toast.LENGTH_SHORT).show();
                UploadPosters();
            }
        });

        uploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaType="Video";
                //Toast.makeText(Creative_form.this, mediaType, Toast.LENGTH_SHORT).show();
                UploadVideos();
            }
        });

//

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProposal();
            }
        });

        // Make a POST request to fetch media URLs
        String url = getApplicationContext().getResources().getString(R.string.server_url) + "/creative/fetch";
        JSONObject jsonParams = new JSONObject();
        try {
            // Replace "your_eid_value" with the actual value for 'eid'
            jsonParams.put("eid", eid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Parse the JSON response and get image URLs
                        List<String> mediaUrls = parseResponse(response);
                        Log.i("Media ke urlsss", String.valueOf(mediaUrls));

                        // Set up ViewPager with the fetched media URLs
                        setupViewPager(mediaUrls);

                        // Set up GestureDetector inside the response listener
                        GestureDetector gestureDetector = new GestureDetector(Creative_form.this, new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public void onLongPress(MotionEvent e) {
                                // Handle long press event
                                int currentItem = viewPager.getCurrentItem();
                                if (currentItem >= 0 && currentItem < mediaUrls.size()) {
                                    String videoUrl = mediaUrls.get(currentItem);

                                    // Start the download process
                                    downloadVideo(videoUrl);
                                }
                            }
                        });

                        viewPager.setOnTouchListener((v, event) -> {
                            gestureDetector.onTouchEvent(event);
                            return false;
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                }
        );

// Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }



    private void downloadVideo(String videoUrl) {
        // Show a toast indicating that the download has started
        Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();

        // Parse the file name from the URL
        String fileName = getFileNameFromUrl(videoUrl);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
        request.setTitle("Video Download");
        request.setDescription("Downloading video");

        // Specify the local destination for the downloaded file with the parsed file name
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // Get download service and enqueue the request
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    // Helper method to extract file name from the URL
    private String getFileNameFromUrl(String url) {
        String[] segments = url.split("/");
        return segments[segments.length - 1];
    }



    private List<String> parseResponse(JSONObject response) {
//        List<String> mediaUrls = new ArrayList<>();

        try {
            JSONArray imageUrls = response.getJSONArray("imageUrls");
            for (int i = 0; i < imageUrls.length(); i++) {
                String imageUrl = imageUrls.getString(i);
                mediaUrls.add(imageUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaUrls;
    }


    private void setupViewPager(List<String> mediaUrls) {

        if (mediaUrls.isEmpty()) {
            // Set default image from local resources if no URLs are available
            mediaUrls.add(getApplicationContext().getResources().getString(R.string.server_url) + "/creative/default_image.png");
        }

        mediaPagerAdapter = new MediaPagerAdapter(this, mediaUrls);
        viewPager.setAdapter(mediaPagerAdapter);
        indicator.setViewPager(viewPager);
    }







    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(getApplicationContext()));
        LayoutInflater inflater = getActivity(getApplicationContext()).getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_layout, null);
        builder.setView(view)
                .setTitle("Uploaded Files")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(getApplicationContext())));

        // Replace this with your list of file names
        List<String> fileNames = new ArrayList<>();
        // Populate fileNames with your data

        // Create an instance of your custom adapter and set it to the RecyclerView
        FileListAdapter adapter = new FileListAdapter(getActivity(getApplicationContext()), fileNames);
        recyclerView.setAdapter(adapter);

        return builder.create();
    }

    private Activity getActivity(Context context) {
        this.context = context;
        return (Activity) context;
    }


    public void onBrowseFileButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select file"), REQUEST_CODE_FILE_PICKER);
    }



    private void submitProposal() {
        //creating jsonobject starts
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cpm_id", eid);
            jsonObject.put("poster", poster_url);
            jsonObject.put("video", video_url);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        //creating jsonobject ends

        //checking data inserted into json object
        final String requestBody = jsonObject.toString();
        Log.i("volleyABC123", requestBody);

        //getting response from server starts
        StringRequest stringRequest = new StringRequest(Request.Method.POST,getApplicationContext().getResources().getString(R.string.server_url) + "/creative/submit",new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.i("volleyABC4985" ,"got response    "+response);
                Toast.makeText(Creative_form.this, "Data Submitted", Toast.LENGTH_SHORT).show();
            }
        },new Response.ErrorListener()  {

            @Override
            public void onErrorResponse(VolleyError error) {

                try{
                    Log.i("volleyABC" ,Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(Creative_form.this, "Invalid Credentials", Toast.LENGTH_SHORT).show(); //This method is used to show pop-up on the screen if user gives wrong uid


                    error.printStackTrace();}
                catch (Exception e)
                {
                    Toast.makeText(Creative_form.this,"Check Network",Toast.LENGTH_SHORT).show();}
            }
        }){
            //sending JSONOBJECT String to server starts
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
    }



    private void insertSrv()
    {
        //creating jsonobject starts
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cpm_id", eid);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        //creating jsonobject ends

        //checking data inserted into json object
        final String requestBody = jsonObject.toString();
        Log.i("volleyABC123", requestBody);

        //getting response from server starts
        StringRequest stringRequest = new StringRequest(Request.Method.POST,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.i("volleyABC4985" ,"got response    "+response);
                //Toast.makeText(Creative_form.this, "Logged IN", Toast.LENGTH_SHORT).show();

                Intent manager = new Intent(Creative_form.this, Manager.class);

                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    // Log.i("tracking uid","main Activity "+UID);
                    name = jsonObject1.getString("proposals_event_name");
                    theme = jsonObject1.getString("proposals_event_category");
                    eventDate = jsonObject1.getString("proposals_event_date");
                    dSpeaker = jsonObject1.getString("speaker");
                    dVenue = jsonObject1.getString("proposals_venue");
                    dFeeCSI = jsonObject1.getString("proposals_reg_fee_csi");
                    dFeeNonCSI = jsonObject1.getString("proposals_reg_fee_noncsi");
                    dPrize = jsonObject1.getString("proposals_prize");
                    description = jsonObject1.getString("proposals_desc");
                    creativeBudget = jsonObject1.getString("proposals_creative_budget");
                    dPublicityBudget = jsonObject1.getString("proposals_publicity_budget");
                    dGuestBudget = jsonObject1.getString("proposals_guest_budget");
                    poster_url = jsonObject1.getString("creative_url");
                    video_url = jsonObject1.getString("creative_url");
                    if(poster_url.equals("")) {
                        imagePreview.setEnabled(false);
                    }
                    else {
                        imagePreview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent creative_form = new Intent(Creative_form.this, CreativePosterFull.class);
                                creative_form.putExtra("creative_url", poster_url);
                                startActivity(creative_form);
                            }
                        });
                    }
                    loadImageUrl();
                    loadVideoUrl();
                    Log.i("sanket", poster_url + " !!!!!! " + video_url);
                    getSupportActionBar().setTitle(name);
                    date1 = eventDate.substring(8,10) + "/" + eventDate.substring(5,7) + "/" + eventDate.substring(0,4);

                    //Send data to Manager.java starts
                    // Call manager.java file i.e. Activity with navigation drawer activity
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                eventName.setText(name);
                eventTheme.setText(theme);
                event_date.setText(date1);
                speaker.setText(dSpeaker);
                venue.setText(dVenue);
                fee_csi.setText(dFeeCSI);
                fee_non_csi.setText(dFeeNonCSI);
                prize.setText(dPrize);
                eventDescription.setText(description);
                creative_budget.setText(creativeBudget);
                publicity_budget.setText(dPublicityBudget);
                guest_budget.setText(dGuestBudget);

            }
        },new Response.ErrorListener()  {

            @Override
            public void onErrorResponse(VolleyError error) {

                try{
                    Log.i("volleyABC" ,Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(Creative_form.this, "Invalid Credentials", Toast.LENGTH_SHORT).show(); //This method is used to show pop-up on the screen if user gives wrong uid


                    error.printStackTrace();}
                catch (Exception e)
                {
                    Toast.makeText(Creative_form.this,"Check Network",Toast.LENGTH_SHORT).show();}
            }
        }){
            //sending JSONOBJECT String to server starts
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
    }

    private void loadImageUrl() {
        imagePreview.setEnabled(true);

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable(){
            @Override
            public void run() {
                Picasso.get().load(poster_url)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(imagePreview, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("response_poster", "SUCCESS");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.i("response_poster", "error");
                            }
                        });
            }
        });


    }

    private void loadVideoUrl() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Log.i("response_v_url", video_url);
                // Stuff that updates the UI
                video_preview.setText(Html.fromHtml("<a href=\""+ video_url + "\">" + "Click here to view" + "</a>"));

                video_preview.setClickable(true);
                video_preview.setMovementMethod (LinkMovementMethod.getInstance());
            }
        });
    }

    private void UploadPosters() {

        //calling requestMethod
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }
        }

        enable_poster_button();
    }

    private void UploadVideos() {

        //calling requestMethod
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }
        }

        enable_video_button();
    }









    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            if(mediaType.equals("image")) {
                enable_poster_button();
            }
            else {
                enable_video_button();
            }
        }
    }

    private void enable_poster_button() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Poster"), 1);

    }

    private void enable_video_button() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("video/*");
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Video"), 1);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Spinner videoTypeSpinner = findViewById(R.id.video_type_spinner);
        Spinner photoTypeSpinner = findViewById(R.id.layout_type_spinner);
        photoTypeSpinner.setVisibility(View.GONE);
        videoTypeSpinner.setVisibility(View.GONE);
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();

        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        String displayName = documentFile.getName();
        String mimeType = documentFile.getType();

        try {
            InputStream inputStream = this.getContentResolver().openInputStream(documentFile.getUri());
            File outputFile = new File(this.getCacheDir(), displayName);
            OutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            String filePath = outputFile.getAbsolutePath();
            Log.i("uri file path" , filePath);

            if (requestCode == REQUEST_CODE_FILE_PICKER && resultCode == RESULT_OK) {
                mSelectedFile = new File(filePath);
//                String mimeType = getContentResolver().getType(uri);
                if (mimeType != null && mimeType.startsWith("image/")) {
                    // Photo file selected, hide the second Spinner
                    photoTypeSpinner.setVisibility(View.VISIBLE);
                } else if (mimeType != null && mimeType.startsWith("video/")) {
                    // Video file selected, show the second Spinner
                    videoTypeSpinner.setVisibility(View.VISIBLE);
                }
                else {
                    // mimeType is either null or doesn't match image or video, hide both Spinners
                    photoTypeSpinner.setVisibility(View.GONE);
                    videoTypeSpinner.setVisibility(View.GONE);
                }
            }

            Button uploadButton = findViewById(R.id.upload_button);
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("filepath", filePath);
                    // Create a Runnable for additional UI updates
                    Runnable uiUpdateCallback = new Runnable() {
                        @Override
                        public void run() {
                            // Additional UI updates can be performed here
                        }
                    };
                    // Call the uploadFile() function with the file path and uiUpdateCallback
                    uploadFile(filePath, uiUpdateCallback);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fetchMediaUrls() {
        // Make a POST request to fetch media URLs
        String url = getApplicationContext().getResources().getString(R.string.server_url) + "/creative/fetch";
        JSONObject jsonParams = new JSONObject();
        try {
            // Replace "your_eid_value" with the actual value for 'eid'
            jsonParams.put("eid", eid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Parse the JSON response and get image URLs
                        List<String> mediaUrls = parseResponse(response);
                        Log.i("Fetched Media URLs", String.valueOf(mediaUrls));

                        // Update the UI with the fetched media URLs
                        updateUIWithMediaUrls(mediaUrls);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.e("Fetch Media URLs", "Error: " + error.getMessage());
                    }
                }
        );

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void updateUIWithMediaUrls(List<String> mediaUrls) {
        // Set up ViewPager with the fetched media URLs
        setupViewPager(mediaUrls);

        // Set up GestureDetector inside the response listener
        GestureDetector gestureDetector = new GestureDetector(Creative_form.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                // Handle long press event
                int currentItem = viewPager.getCurrentItem();
                if (currentItem >= 0 && currentItem < mediaUrls.size()) {
                    String videoUrl = mediaUrls.get(currentItem);

                    // Start the download process
                    downloadVideo(videoUrl);
                }
            }
        });

        viewPager.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }


    public void uploadFile(String filePath , Runnable uiUpdateCallback) {
        Spinner photoTypeSpinner = findViewById(R.id.layout_type_spinner);
        Spinner videoTypeSpinner = findViewById(R.id.video_type_spinner);
        final String[] fileheader = { "None" };
        progress.setTitle("Uploading");
        progress.setMessage("Please wait...");
        progress.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                File file = new File(filePath);
//                String content_type = getMimeType(filePath);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .writeTimeout(100, TimeUnit.SECONDS)
                        .readTimeout(300, TimeUnit.SECONDS)
                        .build();

                RequestBody file_body;
                if(mediaType.equals("image")) {
                    file_body = RequestBody.create(MediaType.parse("image/*"), file);
                    fileheader[0] = photoTypeSpinner.getSelectedItem().toString();
                } else {
                    file_body = RequestBody.create(MediaType.parse("video/*"), file);
                    fileheader[0] = videoTypeSpinner.getSelectedItem().toString();
                }

                RequestBody request_body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("eid", eid)
                        .addFormDataPart("fileheader", fileheader[0])
                        .addFormDataPart("file", file.getName(), file_body)
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(getApplicationContext().getResources().getString(R.string.server_url) + "/creative/upload")    //Main Server URL)
                        .post(request_body)
                        .build();

                try {
                    okhttp3.Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Parse the response and get the updated media URLs
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        List<String> updatedMediaUrls = parseResponse(jsonResponse);

                        // Update the UI on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update the media URLs and notify the adapter
                                mediaUrls.clear();
                                mediaUrls.addAll(updatedMediaUrls);
                                mediaPagerAdapter.notifyDataSetChanged();

                                // Call the provided callback for additional UI updates
                                if (uiUpdateCallback != null) {
                                    uiUpdateCallback.run();
                                }

                                // Dismiss the progress dialog
                                progress.dismiss();

                                // Fetch media URLs just after updating the UI
                                fetchMediaUrls();
                            }
                        });
                    } else {
                        // Handle unsuccessful response
                        throw new IOException("Error : " + response);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    Log.e("MultiPart", "Error during file upload", e);

                    // Handle the error, e.g., display a toast or alert dialog
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error during file upload", Toast.LENGTH_SHORT).show();
                            // Dismiss the progress dialog
                            progress.dismiss();
                        }
                    });
                }
            }
        });
        t.start();
    }




    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    private String getMimeType(String path) {

        String extention = path.substring(path.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extention);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // TODO Auto-generated method sub
        int id= item.getItemId();
        if (id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
