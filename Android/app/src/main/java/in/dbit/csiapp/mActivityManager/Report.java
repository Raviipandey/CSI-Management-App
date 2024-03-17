package in.dbit.csiapp.mActivityManager;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import in.dbit.csiapp.R;
import in.dbit.csiapp.mAdapter.PraposalAdapter;
import in.dbit.csiapp.mAdapter.PraposalItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Report extends AppCompatActivity implements PraposalAdapter.OnItemClickedListener{

    private RecyclerView rv;
    private PraposalAdapter mPraposalAdapter;
    private ArrayList<PraposalItem> mPraposalList;
    private RequestQueue mRequestQueue;
    private String server_url, eid;
    private long downloadID;



    String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        getSupportActionBar().setTitle("Reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPraposalList = new ArrayList<>();
        rv=findViewById(R.id.recycler_view_report);

        rv.setLayoutManager(new LinearLayoutManager(Report.this));
        mRequestQueue = Volley.newRequestQueue(Report.this);
        parseJSON(); //This method is used to get list of Agendas from server

        rv.setAdapter(new PraposalAdapter(Report.this,mPraposalList));
        FloatingActionButton downloadtemplate = findViewById(R.id.downloadtemplate);

        downloadtemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle download template button click
                downloadTemplate();
            }
        });
    }

    private void downloadTemplate() {
        // URL of the template to download
        String templateUrl = getApplicationContext().getResources().getString(R.string.server_url) + "/server_uploads/report_template/ReportTemplate.docx";

        // Directory to save the downloaded file
        String fileName = "Report Template";
        String downloadDirectory = Environment.DIRECTORY_DOWNLOADS;

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(templateUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setTitle("Report Template")
                .setDescription("Downloading " + fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(downloadDirectory, fileName);

        downloadID = downloadManager.enqueue(request);
        checkDownloadStatus();
    }

    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Report.this, "Starting download template", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            downloading = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Report.this, "Failed to download template", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    cursor.close();
                }
            }
        }).start();
    }





    public void parseJSON() {
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/reports/list";   //Main Server URL
        // server_url = "http://192.168.43.110:8081/creative/viewListEvents";

        mPraposalList.clear();

        StringRequest stringRequest =new StringRequest(Request.Method.GET,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.i("volleyABC" ,"got response    "+response);
//                Toast.makeText(Creative.this,response ,Toast.LENGTH_SHORT).show();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    TextView no_report_text = findViewById(R.id.no_report);
                    if(jsonArray.length() > 0) {
                        rv.setVisibility(View.VISIBLE);
                        no_report_text.setVisibility(View.GONE);
                    }
                    else {
                        no_report_text.setText("No Reports to display");
                    }
                    for(int i=0; i< jsonArray.length(); i++) {
                        JSONObject minutes = jsonArray.getJSONObject(i);

                        Log.d("Jsonresponse for report" , String.valueOf(minutes));
                        eid = minutes.getString("cpm_id");
                        String date = minutes.getString("proposals_event_date");
                        Name = minutes.getString("proposals_event_name");
//                        String status = minutes.getString("status");
                        String theme =minutes.getString("proposals_event_category");
                        //String points = minutes.getString("minute");

                        //in the above variable date we are not getting date in DD:MM:YYYY
                        //so we are creating new variable date1 to get our desire format
                        String date1 = date.substring(8,10) + "/" + date.substring(5,7) + "/" + date.substring(0,4);

                        mPraposalList.add(new PraposalItem(eid,"Date: "+date1, Name,"C","Theme: "+ theme));
//                        JSONObject minutes = jsonArray.getJSONObject(i);
//
//                        String eid = minutes.getString("eid");
//                        String Name = minutes.getString("name");
////                        String status = minutes.getString("status");
//                        String theme = minutes.getString("theme");

                        //in the above variable date we are not getting date in DD:MM:YYYYY
                        //so we are creating new variable date1 to get our desire format
//                        String date1 = date.substring(8,10) + "/" + date.substring(5,7) + "/" + date.substring(0,4);

//                        mPraposalList.add(new PraposalItem(eid,null, Name, theme, "No extra"));

                    }
                    mPraposalAdapter = new PraposalAdapter(Report.this, mPraposalList);
                    rv.setAdapter(mPraposalAdapter);
                    mPraposalAdapter.setOnItemClickListener(Report.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("volleyABC" ,"Got error in connecting server");
                try {
                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.i("volleyABC", Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(Report.this, "Error:-" + statusCode, Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                } catch(Exception e) {
                    Toast.makeText(Report.this, "Check Network",Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRequestQueue.add(stringRequest);
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

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(Report.this,Reportmanager.class);
        PraposalItem clickedItem = mPraposalList.get(position);
        String EventName = clickedItem.getmName();
        String EventId = clickedItem.getmEid();
        intent.putExtra("eName", EventName);
        Log.i("Passing eid to Display" , EventId);
        intent.putExtra("eid" , EventId);
        startActivity(intent);
    }
}
