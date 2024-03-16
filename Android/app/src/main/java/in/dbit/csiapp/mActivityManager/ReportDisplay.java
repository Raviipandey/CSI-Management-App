package in.dbit.csiapp.mActivityManager;
import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import in.dbit.csiapp.R;
import com.github.barteksc.pdfviewer.PDFView;
import org.apache.commons.io.IOUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReportDisplay extends AppCompatActivity {

    PDFView pdfView;
    String url = "http://192.168.1.106:9000/report/";

    String eName;
    String eid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Log of report new url"  ,url);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_display);
        getSupportActionBar().setTitle("Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        eid = intent.getStringExtra("eid");
        eName = intent.getStringExtra("eName");
        eName = eName + ".pdf";
//        url = url + eid;
        url += eName;
        Log.i("url testing report", url);

        pdfView = (PDFView)findViewById(R.id.pdfView);

        //This is function read PDF from Assets
        //Note: - pdf is not their in assets foolder
        //pdfView.fromAsset("Student FAQs.pdf").load();

        //This is function read PDF from URL

//      String pdfurl = "http://localhost:9000/server_uploads/publicity_pdf/1_Mumbai%20Hackathon_publicity.pdf";


        new GenerateReportTask().execute(eid);

//        new RetrievePDFStream().execute(url);
        //This is function read PDF from bytes
        //new RetrievePDFBytes().execute("http://ancestralauthor.com/download/sample.pdf");

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ReportDisplay.this, eName, Toast.LENGTH_SHORT).show();
                Toast.makeText(ReportDisplay.this, "Downloading Started", Toast.LENGTH_SHORT).show();
                StartDownloading(v);
            }
        });
    }

    private class GenerateReportTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                // Construct the URL for the generate route with the eid parameter
                String generateUrl = "http://192.168.1.106:9000/generate?eid=" + params[0];

                // Open a connection to the generate route URL (you can use HttpURLConnection or any other HTTP library)
                URL url = new URL(generateUrl);
                urlConnection = (HttpURLConnection) url.openConnection();

                // Handle the response if needed (e.g., check for HTTP success codes)

            } catch (IOException e) {
                Log.e("GenerateReportTask", "Error sending request to generate route", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // After generating the report, start the download task
            new RetrievePDFStream().execute(url);
        }
    }
    public void StartDownloading(View view) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                return;
            }
        }

        DownloadBooks(url, eName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            DownloadBooks(url, eName);
        }
    }

    public void DownloadBooks(String url, String title) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        request.setMimeType("application/pdf");
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        downloadManager.enqueue(request);
    }

    class RetrievePDFBytes extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... strings) {
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if(urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException e) {
                Log.i("sank","sanket");
                return null;
            }
            try {
                return IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            pdfView.fromBytes(bytes).load();

        }
    }

    private class RetrievePDFStream extends AsyncTask<String, Void, InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException e) {
                Log.i("sank", "sanket");
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            pdfView.fromStream(inputStream).load();
        }
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