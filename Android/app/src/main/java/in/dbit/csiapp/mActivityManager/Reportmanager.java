package in.dbit.csiapp.mActivityManager;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import in.dbit.csiapp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Reportmanager extends AppCompatActivity {

    PDFView pdfView;

//    private static final String PDF_URL = "http://192.168.1.101:9000/server_uploads/reports/1_Mumbai%20Hackathon_report.pdf";
    private static final String FILE_NAME = "report.pdf";


    String eName;
    String eid;

    FloatingActionButton uploadreport;
    FloatingActionButton download , deletereport;
    TextView noreport;


    String server_url , downloadurl , filename;
    private static final int PICK_PDF_REQUEST = 1;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_manager);
        getSupportActionBar().setTitle("Report");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        server_url = getResources().getString(R.string.server_url);

        noreport = findViewById(R.id.defaultmsg);
        uploadreport = findViewById(R.id.Uploadreport);
        download = findViewById(R.id.Downloadreport);
        deletereport = findViewById(R.id.deletereport);

        uploadreport.hide();
        download.hide();
        deletereport.hide();

        pdfView = findViewById(R.id.pdfView);


        uploadreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf"); // Specify the desired MIME type
                startActivityForResult(intent, REQUEST_CODE);
            }
        });



        eid = intent.getStringExtra("eid");
        eName = intent.getStringExtra("eName");


        new CheckReportTask().execute();

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(downloadurl, filename);

            }
        });

        deletereport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFileFromServer(eid);
            }
        });




        // Make HTTP request to check if the report exists


    }

    private class DownloadPdfTask extends AsyncTask<String, Void, File> {

        @Override
        protected File doInBackground(String... strings) {
            String pdfUrl = strings[0];
            try {
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // Create a temporary file to store the downloaded PDF
                File file = new File(getCacheDir(), FILE_NAME);
                FileOutputStream outputStream = new FileOutputStream(file);

                // Download the PDF content
                InputStream inputStream = connection.getInputStream();
                IOUtils.copy(inputStream, outputStream);
                outputStream.close();

                return file;
            } catch (IOException e) {
                Log.e("Pdf task", "Error downloading PDF", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file != null) {
                displayPdf(file);
            } else {

            }
        }
    }

    private void displayPdf(File file) {
        pdfView.fromFile(file)
                .load();
    }



    private class CheckReportTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                URL url = new URL(server_url + "/reports/fetchreport?eid=" + eid);
                Log.i("Report fetching", String.valueOf(url));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    String response = IOUtils.toString(inputStream, "UTF-8");
                    JSONObject jsonObject = new JSONObject(response);
                    String filename = jsonObject.getString("filename");
                    String downloadUrl = jsonObject.getString("url");
                    return new String[]{filename, downloadUrl};
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null; // Error occurred
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (result != null && result.length == 2) {
                // Report exists, show download button
                noreport.setVisibility(View.INVISIBLE);
                download.show();
                deletereport.show();
                filename = result[0];
                downloadurl = result[1];
                new DownloadPdfTask().execute(downloadurl);

            } else {
                // Report doesn't exist, show upload button
                uploadreport.show();
            }
        }
    }

    // Inside Reportmanager class


    private void uploadFile(Uri fileUri) {
//        String eventNameText = eventName.getText().toString();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);


            // Create OkHttp3 client and builder
            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);

            // Add file to the builder
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            RequestBody requestBody = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), bytes);
            Log.i("Requestbody here" , requestBody.toString());
            builder.addFormDataPart("report", "file.pdf", requestBody);
            builder.addFormDataPart("eid", eid);
            Log.i("eid for server upload",  eid);

            builder.addFormDataPart("eventname", eName); // Make sure this matches the server's expected field name
//            Log.i("event name",  filename);

            // Build the request
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(getApplicationContext().getResources().getString(R.string.server_url) + "/reports/upload")
                    .post(builder.build())
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Reportmanager.this, "Error uploading file", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uploadreport.setEnabled(false); // Disable the select file button
                            download.show(); // Show the download button
                            new CheckReportTask().execute();
                            new DownloadPdfTask().execute(downloadurl);
                            download.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    downloadFile(downloadurl, filename);
                                }
                            });
                            Toast.makeText(Reportmanager.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            uploadreport.hide();
                            noreport.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
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



    private void downloadFile(String downloadUrl, String filename) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle(filename);
        request.setDescription("Downloading report");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    private void deleteFileFromServer(String eid) {
        OkHttpClient client = new OkHttpClient();

        // Create JSON object with the eid
        JSONObject json = new JSONObject();
        try {
            json.put("eid", eid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create request body
        RequestBody requestBody = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));

        // Create request
        Request request = new Request.Builder()
                .url(getResources().getString(R.string.server_url) + "/reports/delete")
                .post(requestBody)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Handle successful response
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Perform actions after successful deletion
                        // For example, show a toast message
                        Toast.makeText(Reportmanager.this, "File deleted successfully", Toast.LENGTH_SHORT).show();
                        // Hide the download button
                        download.hide();
                        // Show the upload button
                        uploadreport.show();
                        // Hide the delete button
                        deletereport.hide();
                        // Stop displaying the PDF
                        pdfView.setVisibility(View.GONE);
                        // Show the default message
                        noreport.setVisibility(View.VISIBLE);
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




}
