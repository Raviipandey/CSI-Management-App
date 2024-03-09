package in.dbit.csiapp.mActivityManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.Prompts.Manager;
import in.dbit.csiapp.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


public class Publicity extends AppCompatActivity {

    private SharedPreferenceConfig preferenceConfig;
    public static final String READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;


    String urole1;
    LinearLayout pr_lay;
    Button edit_pr, submit_pr;
//    String eid;
    private static String eid;
    private static String apiurl;


    TextView eventName, eventTheme, event_date, eventDescription, speaker, venue, fee_csi, fee_non_csi, prize, cr_budget, pub_budget, guest_budget, tvFileStatus;
    EditText target_aud, comments, money_c, money_s;
    CheckBox reg_desk, inclass_pub;
    LinearLayout comments_layout;
    private LinearLayout checkboxContainer;
    private ArrayList<String> checkboxNames = new ArrayList<>();
    private List<CheckBox> checkBoxList = new ArrayList<>();
    private List<CheckBox> checkedboxes = new ArrayList<>();

    private static final int REQUEST_CODE = 1;

    private static final String TAG = "YourActivity";

    private Button selectFileButton, deleteButton;
    private String currentFileName = null;

    private Button downloadButtonForOthers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicity);



        getSupportActionBar().setTitle("Publicity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        urole1 = preferenceConfig.readRoleStatus();
        submit_pr = findViewById(R.id.submit_pl);
        edit_pr = findViewById(R.id.edit_pr_req);
        pr_lay = findViewById(R.id.pr_pl);
        checkboxContainer = findViewById(R.id.pub_checkbox_container);
//        Toast.makeText(Publicity.this,"role: "+urole1,Toast.LENGTH_SHORT).show();
//        Log.i("volleyABC ", urole1);
        Button downloadButton = findViewById(R.id.download_button);

        eventName = (TextView) findViewById(R.id.name_pl);
        tvFileStatus = findViewById(R.id.tvFileStatus);
        eventTheme = (TextView) findViewById(R.id.theme_pl);
        event_date = (TextView) findViewById(R.id.ed_pl);
        speaker = (TextView) findViewById(R.id.speaker_pl);
        venue = (TextView) findViewById(R.id.venue_pl);
        fee_csi = (TextView) findViewById(R.id.fee_csi_pl);
        fee_non_csi = (TextView) findViewById(R.id.fee_non_csi_pl);
        prize = (TextView) findViewById(R.id.prize_pl);
        eventDescription = (TextView) findViewById(R.id.desc_pl);
        target_aud = findViewById(R.id.pr_target);
        comments = findViewById(R.id.pr_comments);
        money_c = findViewById(R.id.pr_m_coll);
        money_s = findViewById(R.id.pr_m_spent);
        reg_desk = findViewById(R.id.pr_desk);
        inclass_pub = findViewById(R.id.pr_inclass);
        cr_budget = findViewById(R.id.cb);
        pub_budget = findViewById(R.id.pb);
        guest_budget = findViewById(R.id.gb);

//        Button testdownloadButton = findViewById(R.id.testdownload);
//        testdownloadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String pdfUrl = "http://192.168.1.106:9000/publicity/download?eid=9";
//                new DownloadPdfTask().execute(pdfUrl);
//            }
//        });

        Bundle extras = getIntent().getExtras();
        if (extras == null) {

        } else {
            eid = extras.getString("EID");

        }

        Button pub_add_checkbox = findViewById(R.id.pub_add_checkbox_button);

        if (urole1.equals("PR Head")) {
            edit_pr.setVisibility(View.VISIBLE);
            pub_add_checkbox.setVisibility(View.GONE);
            pr_lay.setVisibility(View.GONE);

        } else {
            pr_lay.setVisibility(View.VISIBLE);
            submit_pr.setVisibility(View.GONE);
            reg_desk.setEnabled(false);
            inclass_pub.setEnabled(false);
            findViewById(R.id.pr_target).setEnabled(false);
            findViewById(R.id.pr_comments).setEnabled(false);
            findViewById(R.id.pr_m_coll).setEnabled(false);
            findViewById(R.id.pr_m_spent).setEnabled(false);
            findViewById(R.id.delete_button).setEnabled(false);
        }

        edit_pr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edit_pr.setVisibility(View.GONE);
                pub_add_checkbox.setVisibility(View.VISIBLE);
                pr_lay.setVisibility(View.VISIBLE);
            }
        });

        submit_pr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volley_send();

            }
        });
        volley_get();
        pub_add_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNamePrompt();
            }
        });


        selectFileButton = findViewById(R.id.select_file_button);
//        Button downloadButton = findViewById(R.id.download_button);
        if (!urole1.equals("PR Head")) {
            selectFileButton.setVisibility(View.GONE);

        }
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf"); // Specify the desired MIME type
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        deleteButton = findViewById(R.id.delete_button);


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUploadedFile();
            }
        });


        // Execute ApiRequestTask to fetch data and handle UI accordingly
        new ApiRequestTask(this, new ApiRequestTask.ApiRequestCallback() {
            @Override
            public void onApiResult(String[] result) {
                if (result != null) {
                    if (result.length == 2) {
                        // File exists, update UI accordingly
                        handleFileExistence(result[0], result[1]);
                    } else if (result.length == 1) {
                        // File not found, update UI accordingly
                        handleFileNotFound(result[0]);
                    }
                }
            }
        }).execute();

    }



    public static class ApiRequestTask extends AsyncTask<Void, Void, String[]> {

        private final Context context;
        private static final String TAG = "ApiRequestTask";

//        private final String apiUrl = "https://csiapp.dbit.in/publicity/fetchpr?eid=" + eid;

        private final ApiRequestCallback callback;

        public ApiRequestTask(Context context, ApiRequestCallback callback) {
            this.context = context;
            this.callback = callback;
        }
        public interface ApiRequestCallback {
            void onApiResult(String[] result);
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            try {
                String apiUrl = context.getString(R.string.server_url) + "/publicity/fetchpr?eid="+ eid;
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    return parseApiResponse(response.toString());
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error making API request: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (callback != null) {
                callback.onApiResult(result);
            }
        }

        private String[] parseApiResponse(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has("filename") && jsonResponse.has("url")) {
                    String filename = jsonResponse.getString("filename");
                    String url = jsonResponse.getString("url");
                    return new String[]{filename, url};
                } else if (jsonResponse.has("error")) {
                    String error = jsonResponse.getString("error");
                    return new String[]{error};
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
            }

            return null;
        }
    }


    private void handleFileExistence(String filename, String url) {
        tvFileStatus.setVisibility(View.GONE); // Hide the status message
        selectFileButton.setEnabled(false); // Disable the select file button
        Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setVisibility(View.VISIBLE); // Show the download button

        if ("PR Head".equalsIgnoreCase(urole1)) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build the download request
                OkHttpClient client = new OkHttpClient();
                okhttp3.Request downloadRequest = new okhttp3.Request.Builder()
                        .url(getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/download?eid=" + eid)
                        .build();

                // Execute the download request asynchronously
                client.newCall(downloadRequest).enqueue(new Callback() {
                    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {

                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        }

                        String contentDisposition = response.header("Content-Disposition");
                        String fileName = extractFileName(contentDisposition);

                        // Create a file with the downloaded content
                        byte[] bytes = response.body().bytes();

                        // Get the content resolver
                        ContentResolver resolver = getContentResolver();

                        // Set up the ContentValues to insert into the MediaStore
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);

                        // For Android Q and above, use the Downloads directory
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                        }

                        // Insert the file into the MediaStore
                        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                        if (uri != null) {
                            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                                outputStream.write(bytes);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Publicity.this, "File downloaded successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Publicity.this, "Error saving file", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }



                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Toast.makeText(Publicity.this, "Error downloading file", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void handleFileNotFound(String error) {
        // File not found, update UI accordingly
        // Enable the select file button or take appropriate action
        Button downloadButton = findViewById(R.id.download_button);
        selectFileButton.setEnabled(true);
        deleteButton.setVisibility(View.GONE);
        downloadButton.setVisibility(View.GONE);

        // You may also display an error message using Toast or any other UI component
        Toast.makeText(Publicity.this, "File not found: " + error, Toast.LENGTH_SHORT).show();
    }


    // Helper method to extract filename from Content-Disposition header
    private String extractFileName(String contentDisposition) {
        String fileName = "default_filename.pdf";  // Default filename if extraction fails

        if (contentDisposition != null) {
            Matcher matcher = Pattern.compile("filename\\s*=\\s*\"([^\"]+)\"").matcher(contentDisposition);
            if (matcher.find()) {
                fileName = matcher.group(1);
            }
        }

        return fileName;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try to download file again
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showUploadConfirmationDialog(final Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Upload");
        builder.setMessage("Do you want to upload the selected file?");

        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Proceed with file upload
                uploadFile(fileUri);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteUploadedFile() {
        // Build the delete request
        OkHttpClient client = new OkHttpClient();
        // Use FormBody to send eid as part of the request body
        RequestBody formBody = new FormBody.Builder()
                .add("eid", eid)
                .build();

        okhttp3.Request deleteRequest = new okhttp3.Request.Builder()
                .url(getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/delete")
                .post(formBody)
                .build();

        // Execute the delete request asynchronously
        client.newCall(deleteRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Publicity.this, "Error deleting file", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Publicity.this, "File deleted successfully", Toast.LENGTH_SHORT).show();
                        selectFileButton.setEnabled(true); // Enable the select file button
                        findViewById(R.id.download_button).setVisibility(View.GONE); // Hide the download button
                        findViewById(R.id.delete_button).setVisibility(View.GONE); // Hide the delete button

                        // Update SharedPreferences to reflect that the file has been deleted
                        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                        editor.putBoolean(eid, false);
                        editor.apply();
                    }
                });
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                showUploadConfirmationDialog(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadFile(Uri fileUri) {
        String eventNameText = eventName.getText().toString();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);

            // Check if a file has already been uploaded for this eid
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isFileUploaded = sharedPreferences.getBoolean(eid, false);
            if (isFileUploaded) {
                Toast.makeText(this, "You have already uploaded a file for this eid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create OkHttp3 client and builder
            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);

            // Add file to the builder
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            RequestBody requestBody = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), bytes);
            builder.addFormDataPart("pdfFile", "file.pdf", requestBody);
            builder.addFormDataPart("eid", eid);
            Log.i("eid for server upload",  eid);

            builder.addFormDataPart("eventname", eventNameText); // Make sure this matches the server's expected field name
            Log.i("event name",  eventNameText);

            // Build the request
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/upload")
                    .post(builder.build())
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Publicity.this, "Error uploading file", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    // Save the eid in SharedPreferences to indicate that a file has been uploaded for this eid
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(eid, true);
                    editor.apply();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selectFileButton.setEnabled(false); // Disable the select file button
                            Button downloadButton = findViewById(R.id.download_button);
                            downloadButton.setVisibility(View.VISIBLE); // Show the download button
                            downloadButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Build the download request

                                    okhttp3.Request downloadRequest = new okhttp3.Request.Builder()
                                            .url(getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/download?eid=" + eid)
                                            .build();

                                    // Execute the download request asynchronously
                                    client.newCall(downloadRequest).enqueue(new okhttp3.Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(Publicity.this, "Error downloading file", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                            if (!response.isSuccessful()) {
                                                throw new IOException("Unexpected code " + response);
                                            }

                                            // Try to extract the file name from the Content-Disposition header
                                            String contentDisposition = response.header("Content-Disposition");
                                            String downloadedFileName = "downloaded_file.pdf"; // Default file name if header parsing fails
                                            if (contentDisposition != null && contentDisposition.contains("filename=")) {
                                                downloadedFileName = contentDisposition.split("filename=")[1].replaceAll("\"", "");
                                            }

                                            // Create a file with the downloaded content
                                            byte[] bytes = response.body().bytes();
                                            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                            File file = new File(downloadsDir, downloadedFileName);
                                            FileOutputStream outputStream = new FileOutputStream(file);
                                            outputStream.write(bytes);
                                            outputStream.close();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Publicity.this, "File downloaded successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Publicity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            selectFileButton.setEnabled(false);
                            findViewById(R.id.download_button).setVisibility(View.VISIBLE);
                            findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }
    }



    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }



    private void showNamePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name");

        final View inputView = getLayoutInflater().inflate(R.layout.name_prompt, null);
        builder.setView(inputView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = ((EditText) inputView.findViewById(R.id.name_edit_text)).getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    addCheckbox(name);
                    Log.i("checkboxx" , name);
                } else {
                    Toast.makeText(Publicity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    public void addCheckbox(String name) {
//        LinearLayout checkboxContainer;


        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(name);
        checkboxContainer.addView(checkBox);
        checkBoxList.add(checkBox);


        Log.i("listt",checkBoxList.get(0).getText().toString());
//        CheckBox checkBox = new CheckBox(this);
//        checkBox.setText(name);
//        checkboxContainer.addView(checkBox);
    }


    //volley function
    public void volley_get(){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eid", eid);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonObject.toString();
        Log.i("volleyABC ", requestBody);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/viewEvent", new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
//                ret[0]=response;
                Log.i("volleyABC4985" ,"got response    "+response);
                //Toast.makeText(Creative_form.this, "Logged IN", Toast.LENGTH_SHORT).show();
                Log.i("volleyABC4985" ,"got response    "+response);
                //Toast.makeText(Creative_form.this, "Logged IN", Toast.LENGTH_SHORT).show();
                Log.i("volleyABC" ,"reposnsde"+response);
                if(response != null){
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        // Log.i("tracking uid","main Activity "+UID);

                        eventName.setText(jsonObject1.getString("proposals_event_name"));
                        eventTheme.setText(jsonObject1.getString("proposals_event_category"));

                        speaker.setText(jsonObject1.getString("speaker"));
                        venue.setText(jsonObject1.getString("proposals_venue"));
                        fee_csi.setText(jsonObject1.getString("proposals_reg_fee_csi"));
                        fee_non_csi.setText(jsonObject1.getString("proposals_reg_fee_noncsi"));
                        prize.setText(jsonObject1.getString("proposals_prize"));
                        eventDescription.setText(jsonObject1.getString("proposals_desc"));

                        if((int) jsonObject1.get("pr_desk_publicity")==1) {
                            reg_desk.setChecked(true);
                        }
                        else reg_desk.setChecked(false);
                        if((int) jsonObject1.get("pr_class_publicity")==1){
                            inclass_pub.setChecked(true);
                        }
                        else inclass_pub.setChecked(false);

                        target_aud.setText(jsonObject1.getString("pr_member_count"));
                        comments.setText(jsonObject1.getString("pr_comment"));
                        money_c.setText(jsonObject1.getString("pr_rcd_amount"));
                        money_s.setText(jsonObject1.getString("pr_spent"));
                        cr_budget.setText(jsonObject1.getString("proposals_creative_budget"));
                        pub_budget.setText(jsonObject1.getString("proposals_publicity_budget"));
                        guest_budget.setText(jsonObject1.getString("proposals_guest_budget"));

                        LinearLayout tasksContainer = findViewById(R.id.pub_tasks_container);
                        String tasksString = jsonObject1.getString("tasks");
                        String statusString = jsonObject1.getString("status");

                        // Split the comma-separated string into an array of individual status values
                        String[] statusValues = statusString.split(",");

                        JSONArray tasksArray = new JSONArray(tasksString);
                        JSONArray statusArray = new JSONArray(statusValues);

                        for (int i = 0; i < tasksArray.length(); i++) {
                            String taskName = tasksArray.getString(i);
                            String taskstatus = statusArray.getString(i);
                            Log.i("server se aaya array", taskName );
                            CheckBox checkBox = new CheckBox(getApplicationContext());
                            checkBox.setText(taskName);
                            checkBox.setTextColor(getResources().getColor(R.color.DarkBlue));
                            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

//                            if(!urole1.equals("Tech Head")){
//                                checkBox.setClickable(false);
//                            }
                            if(urole1.equals("PR Head")){
                                checkBox.setEnabled(true);
                            }
                            else {
                                checkBox.setEnabled(false);
                            }
                            if(taskstatus.equals("1")){
                                checkBox.setChecked(true);
                                Log.i("checked" , taskstatus);
                            }
                            else{
                                checkBox.setChecked(false);
                            }
                            tasksContainer.addView(checkBox);

                        }



                        String eventDate=jsonObject1.getString("proposals_event_date");
                        String date = eventDate.substring(8,10) + "/" + eventDate.substring(5,7) + "/" + eventDate.substring(0,4);
                        event_date.setText(date);
                        getSupportActionBar().setTitle(jsonObject1.getString("name"));


                        //Send data to Manager.java starts
                        // Call manager.java file i.e. Activity with navigation drawer activity
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        },new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {

                try{
                    Log.i("volleyABC" ,Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(Publicity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show(); //This method is used to show pop-up on the screen if user gives wrong uid
                    error.printStackTrace();}
                catch (Exception e)
                {
                    Toast.makeText(Publicity.this,"Check Network",Toast.LENGTH_SHORT).show();

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
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
//        return ret[0];


    }

    public void volley_send(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eid", eid);
            jsonObject.put("pr_member_count", target_aud.getText().toString());
//            jsonObject.put("in_class", inclass_pub.getText().toString());
            jsonObject.put("pr_rcd_amount", money_c.getText().toString());
            jsonObject.put("pr_spent", money_s.getText().toString());
            jsonObject.put("pr_comment",comments.getText().toString());
            if(reg_desk.isChecked())
                jsonObject.put("pr_desk_publicity",1);
            else jsonObject.put("pr_desk_publicity",0);

            if(inclass_pub.isChecked())
                jsonObject.put("pr_class_publicity",1);
            else jsonObject.put("pr_class_publicity",0);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = jsonObject.toString();
        Log.i("volleyABC ", requestBody);

        // Create a RequestQueue to handle the API request
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a string array to store the checked checkboxes
        ArrayList<String> checkedCheckboxes = new ArrayList<>();

        // Create a string array to store the status of checkboxes
        ArrayList<Integer> checkboxStatus = new ArrayList<>();

        // Add the checked checkboxes to the string array
        for (int i = 0; i < checkBoxList.size(); i++) {
            View view = checkBoxList.get(i);
            CheckBox checkBox = (CheckBox) view;
            checkedCheckboxes.add(checkBox.getText().toString());
            if(checkBox.isChecked()){
                checkboxStatus.add(1);
//                checkedCheckboxes.add("1");
            }
            else{
                checkboxStatus.add(0);
//                checkedCheckboxes.add("0");
//                checkedCheckboxes.add((checkBox.getText()+"0"));
            }

            Log.i("arrayss" , checkedCheckboxes.toString());
        }






        // Create a JSON object to store the data to be sent to the server
        JSONObject jsonObjectnew = new JSONObject();

        try {
            // Add the checkedCheckboxes list to the JSON object
            jsonObjectnew.put("checkedCheckboxes", new JSONArray(checkedCheckboxes));
            jsonObjectnew.put("eid", eid);
            jsonObjectnew.put("checkboxStatus", new JSONArray(checkboxStatus));
            Log.i("statusarray", String.valueOf(checkboxStatus));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a POST request to the server with the data
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/addcheckbox", jsonObjectnew,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley Response", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Volley Error", error.toString());
            }
        });

        final String[] ret = new String[1];
        ret[0]=null;
        String r=null;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/editPublicity", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                ret[0]=response;
                Toast.makeText(Publicity.this,"Submitted",Toast.LENGTH_SHORT).show();
//                finish();
                Intent manager = new Intent(Publicity.this, Manager.class);
                startActivity(manager);
                finish();



            }
        },new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {

                try{
                    Log.i("volleyABC" ,Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(Publicity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show(); //This method is used to show pop-up on the screen if user gives wrong uid
                    error.printStackTrace();}
                catch (Exception e)
                {
                    Toast.makeText(Publicity.this,"Check Network",Toast.LENGTH_SHORT).show();

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
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

//        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
        requestQueue.add(jsonObjectRequest);



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