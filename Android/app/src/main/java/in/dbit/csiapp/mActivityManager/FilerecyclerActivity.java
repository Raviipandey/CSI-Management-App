package in.dbit.csiapp.mActivityManager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.csi.R;

import in.dbit.csiapp.mAdapter.FilesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FilerecyclerActivity extends AppCompatActivity implements FilesAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private FilesAdapter filesAdapter;
    private List<FileItem> fileList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creative_form);

        recyclerView = findViewById(R.id.recycler_file);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        // Make an HTTP GET request to fetch files from the server
        fetchFilesFromServer();


    }

    private void fetchFilesFromServer() {
        String serverUrl = getApplicationContext().getResources().getString(R.string.server_url) + "creative/fetch/"; // Construct the URL with the event ID

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, serverUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject fileObject = response.getJSONObject(i);
                                String eid = fileObject.getString("cpm_id");
                                String fileName = fileObject.getString("creative_heading");
                                String fileUrl = fileObject.getString("creative_url");

                                // Replace the placeholder descriptions and dates with actual data
                                String fileDescription = "File Description";
                                String fileDate = "File Date";

                                // Create a FileItem object and add it to fileList
                                fileList.add(new FileItem(fileName, fileUrl, fileDescription, fileDate));
                                filesAdapter = new FilesAdapter(fileList);
                                recyclerView.setAdapter(filesAdapter);
                            }
                            filesAdapter.notifyDataSetChanged(); // Notify adapter of data change
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(FilerecyclerActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error and display a toast message
                        error.printStackTrace();
                        Toast.makeText(FilerecyclerActivity.this, "Error fetching files from the server", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }



    @Override
    public void onItemClick(FileItem item) {
        // Handle item click here, e.g., open the selected file
        // You can pass the file data to a new activity or fragment for viewing
        String fileUrl = item.getFileUrl();

        // Implement your logic to open or preview the file using the fileUrl
        // For example, you can use an Intent to open a PDF viewer or a web browser
        // Replace this with your actual logic based on the file type
    }
}
