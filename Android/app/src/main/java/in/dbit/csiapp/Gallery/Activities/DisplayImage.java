package in.dbit.csiapp.Gallery.Activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import in.dbit.csiapp.Gallery.DisplayImageAdapter.GalleryImageAdapter;
import in.dbit.csiapp.Gallery.Interfaces.IRecyclerViewClickListener;
import in.dbit.csiapp.Prompts.MainActivity;
import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.Gallery.ImageFilePath;

import in.dbit.csiapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class DisplayImage extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int REQUEST_IMAGE_PICKER = 2;
    private static final String FILE_PROVIDER_AUTHORITY = "in.dbit.csiapp.fileprovider";

    String PARENT_PATH = "";
    private SharedPreferenceConfig preferenceConfig;
    String urole;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton Fab;
    ArrayList<String> imagesUrl;
    GalleryImageAdapter galleryImageAdapter;
    private RequestQueue mRequestQueue;
    ImageButton deleteButton;


    //updated variables use to upload images only
    ProgressDialog progress;
    Uri selectedImage;
    OkHttpClient client;
    RequestBody request_body;
    ArrayList<RequestBody> images;

    @Override
    protected void onStart() {
        super.onStart();

        initURL3();
    }


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        urole = preferenceConfig.readRoleStatus();
        setContentView(R.layout.activity_display_image);
        getSupportActionBar().setTitle("Gallery");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        PARENT_PATH = i.getStringExtra("YEAR");
        Log.i("path",PARENT_PATH);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        Fab = (FloatingActionButton) findViewById(R.id.fab);

        mRequestQueue = Volley.newRequestQueue(this);
        imagesUrl = new ArrayList<>();

        deleteButton = findViewById(R.id.delete);
        deleteButton.setVisibility(View.INVISIBLE);

        if(urole.equals("PR Head")){
            Fab.setVisibility(View.VISIBLE);
        }
        else{
            Fab.setVisibility(View.GONE);
        }


        Fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), 1);
                Log.d("FabClickListener", "FAB button clicked!");
                UploadImages();
            }
        });

        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        final IRecyclerViewClickListener listener = new IRecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {

            }
            @Override
            public void onLongClick(View v, int adapterPosition) {


            }
            @Override
            public void onItemClick(int position, String imageUrl) {
                Intent i = new Intent(getApplicationContext(), FullScreenActivity.class);
                i.putExtra("IMAGES",imagesUrl);
                i.putExtra("POSITION",position);
                startActivity(i);
            }
        };

        galleryImageAdapter = new GalleryImageAdapter(this, imagesUrl, listener, PARENT_PATH , deleteButton ,  urole);
        recyclerView.setAdapter(galleryImageAdapter);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the deleteSelectedImages method in your adapter
                galleryImageAdapter.deleteSelectedImages();
                deleteButton.setVisibility(View.INVISIBLE);
            }
        });
        initURL3();

        Log.i("imagesUrl", String.valueOf(imagesUrl));



    }




    public void initURL3() {

        //String url = "http://192.168.43.84:8080/view";
        String url = getApplicationContext().getResources().getString(R.string.server_url) + "/gallery/view";    //Main Server URL
        //String url = "http://192.168.42.156:8080/view";
        //creating jsonobject starts
        final JSONObject jsonObject = new JSONObject();
        try {
            Log.d("This is the path", PARENT_PATH);
            jsonObject.put("path", PARENT_PATH);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        //creating jsonobject ends

        //checking data inserted into json object
        final String requestBody = jsonObject.toString();
        Log.i("volleyABC", requestBody);

        //getting response from server starts
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.i("volleyABC" ,"got response    "+response);
                //Toast.makeText(DisplayImage.this, "Got Event List", Toast.LENGTH_SHORT).show();
                imagesUrl.clear();

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    Log.i("json length", String.valueOf(jsonArray.length()));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String event =  jsonArray.getString(i);
                        Log.i("event" ,"event " + i + " :- " + jsonArray.getString(i));
                        event = event.replace("localhost","192.168.43.84");
                        //event = event.replace("localhost","192.168.42.156");
                        Log.i("events " + i, event);
                        imagesUrl.add(event);
                    }
                    Log.i("imagesUrlPre", String.valueOf(imagesUrl));
                    galleryImageAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(galleryImageAdapter);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener()  {

            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "An error occurred"; // Default message
                try {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        JSONObject data = new JSONObject(responseBody);
                        errorMessage = data.optString("error", errorMessage); // Extract custom message
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if ("Session expired".equals(errorMessage)) {
                    Toast.makeText(DisplayImage.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(DisplayImage.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DisplayImage.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(DisplayImage.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String sessionToken = preferenceConfig.readSessionToken();
                Log.d("RequestHeaders", "Sending token: " + sessionToken); // Add this line
                headers.put("Authorization", "Bearer " + sessionToken);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

        mRequestQueue.add(stringRequest);
    }

    private void UploadImages() {
        progress = new ProgressDialog(DisplayImage.this);

        //button = findViewById(R.id.pick_image);
        //tv = findViewById(R.id.textView);

        //calling requestMethod
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.READ_MEDIA_IMAGES},100);
                return;
            }
        }

        enable_button();
    }

    private void enable_button() {

        //button.setOnClickListener(new View.OnClickListener() {
        //@Override
        //public void onClick(View v) {

        //open
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), 1);

        //  }
        //});

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            enable_button();
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        progress.dismiss();
//    }

    //Created by Sanku... 03/07/2019... 10:30 AM.... Upload Single image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            //String url = "http://192.168.43.84:8080/path";
            String url = getApplicationContext().getResources().getString(R.string.server_url) + "/gallery/path";    //Main Server URL
            //String url = "http://192.168.42.156:8080/path";
            //creating jsonobject starts
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("path", PARENT_PATH);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            //creating jsonobject ends

            //checking data inserted into json object
            final String requestBody = jsonObject.toString();
            Log.i("volleyABC", requestBody);

            //getting response from server starts
            StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,url,new com.android.volley.Response.Listener<String>(){
                @Override
                public void onResponse(String response) {

                    Log.i("volleyABCPATH" ,"got response    "+response);
                    //Toast.makeText(DisplayImage.this, "Got Event List", Toast.LENGTH_SHORT).show();

                    afterActivityResult(data);
                }
            },new com.android.volley.Response.ErrorListener()  {

                @Override
                public void onErrorResponse(VolleyError error) {
                    String errorMessage = "An error occurred"; // Default message
                    try {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            JSONObject data = new JSONObject(responseBody);
                            errorMessage = data.optString("error", errorMessage); // Extract custom message
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if ("Session expired".equals(errorMessage)) {
                        Toast.makeText(DisplayImage.this, "Session expired", Toast.LENGTH_LONG).show();
                    } else if ("Another device has logged in".equals(errorMessage)) {
                        Toast.makeText(DisplayImage.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(DisplayImage.this, errorMessage, Toast.LENGTH_LONG).show();
                    }

                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        // Handle logout if session is expired or taken over
                        preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                        Intent loginIntent = new Intent(DisplayImage.this, MainActivity.class);
                        startActivity(loginIntent);
                        finish();
                    }
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
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String sessionToken = preferenceConfig.readSessionToken();
                    Log.d("RequestHeaders", "Sending token: " + sessionToken); // Add this line
                    headers.put("Authorization", "Bearer " + sessionToken);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            //sending JSONOBJECT String to server ends

            RequestQueue mRequestQueue = Volley.newRequestQueue(this);
            mRequestQueue.add(stringRequest);

        }
    }

    public void afterActivityResult(Intent data) {
        progress.setTitle("Uploading");
        progress.setMessage("Please wait...");
        progress.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if(data.getClipData() != null) {

                        int Count = data.getClipData().getItemCount();
                        Log.i("Total Count", String.valueOf(Count));
                        Log.i("data", String.valueOf(data));

                        MultipartBody.Builder buildernew = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM);

                        for (int i = 0; i < Count; i++) {
                            Log.i("checkingLoop", "This is loop " + i);
                            selectedImage = data.getClipData().getItemAt(i).getUri();

                            Log.i("sanket", String.valueOf(selectedImage));

                            String realPath = ImageFilePath.getPath(DisplayImage.this, data.getClipData().getItemAt(i).getUri());
                            Log.i("finalPathReal", realPath);

                            File f = new File(realPath);
                            String content_type = getMimeType(realPath);
                            //String content_type = "image/*";
                            Log.i("content_type", "CT :- " + content_type);

                            client = new OkHttpClient();
                            RequestBody file_body = RequestBody.create(MediaType.parse("image/*"), f);
                            Log.i("file_body", String.valueOf(file_body));
                            Log.i("file_path substring", realPath.substring(realPath.lastIndexOf("/") + 1));

                            images = new ArrayList<>();
                            images.add(request_body);

                            buildernew.addFormDataPart("file", realPath.substring(realPath.lastIndexOf("/")), file_body);
                        }

                        RequestBody requestBody = buildernew.build();

                        request_body = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                //.addFormDataPart("type",content_type)
                                .addFormDataPart("", images.toString())
                                //.addFormDataPart("file", realPath.substring(realPath.lastIndexOf("/") + 1), file_body)
                                .build();
                        Log.i("requesting body", request_body.toString());

                        okhttp3.Request request = new okhttp3.Request.Builder()
                                //.url("http://192.168.43.84:8080/upload")
                                .url(getApplicationContext().getResources().getString(R.string.server_url) + "/gallery/upload")    //Main Server URL)
                                //.url("http://192.168.42.156:8080/upload")
                                .post(requestBody)
                                .build();

                        Log.i("request", String.valueOf(request));

                        try {
                            okhttp3.Response response = client.newCall(request).execute();
                            Log.i("response on upload", "Response" + response);
                            initURL3();

                            if (!response.isSuccessful()) {
                                throw new IOException("Error : " + response);
                            }

                            progress.dismiss();

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("MultiPart", "Something went wrong");
                        }
                    }
                    else {

                        selectedImage = data.getData();

                        Log.i("sanket", String.valueOf(selectedImage));

                        String realPath = ImageFilePath.getPath(DisplayImage.this, data.getData());
                        Log.i("finalPathReal",realPath);

                        File f = new File(realPath);
                        String content_type = getMimeType(realPath);
                        //String content_type = "image/*";
                        Log.i("content_type2", "CT :- " + content_type);

                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectTimeout(100, TimeUnit.SECONDS)
                                .writeTimeout(100, TimeUnit.SECONDS)
                                .readTimeout(300, TimeUnit.SECONDS)
                                .build();

                        RequestBody file_body = RequestBody.create(MediaType.parse("image/*"), f);
                        Log.i("file_body", String.valueOf(file_body));
                        Log.i("file_path substring", realPath.substring(realPath.lastIndexOf("/") + 1));

                        RequestBody request_body = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                //.addFormDataPart("type",content_type)
                                .addFormDataPart("file", realPath.substring(realPath.lastIndexOf("/") + 1), file_body)
                                .build();

                        okhttp3.Request request = new okhttp3.Request.Builder()
                                //.url("http://192.168.43.84:8080/upload")
                                .url(getApplicationContext().getResources().getString(R.string.server_url) + "/gallery/upload")
                                //.url("http://192.168.42.156:8080/upload")
                                .post(request_body)
                                .build();

                        Log.i("request", String.valueOf(request));

                        try {
                            okhttp3.Response response = client.newCall(request).execute();
                            Log.i("response", "Response" + response);
                            initURL3();

                            if (!response.isSuccessful()) {
                                throw new IOException("Error : " + response);
                            }

                            progress.dismiss();

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("MultiPart", "Something went wrong", e);
                        }
                    }
                }
            }
        });
        t.start();
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