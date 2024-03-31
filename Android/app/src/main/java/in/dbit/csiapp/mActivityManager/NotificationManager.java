package in.dbit.csiapp.mActivityManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.dbit.csiapp.Prompts.MainActivity;
import in.dbit.csiapp.R;
import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.mAdapter.NotificationAdapter;
import in.dbit.csiapp.mAdapter.NotificationItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationManager extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    String server_url , uid;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    private SharedPreferenceConfig preferenceConfig;

    private SwipeRefreshLayout swipeRefreshLayout;

    TextView defaultmsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificationmanager);
        getSupportActionBar().setTitle("Notifications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        defaultmsg = findViewById(R.id.defaultmsg);
        defaultmsg.setVisibility(View.INVISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        Intent intent = getIntent();
        uid = intent.getStringExtra(MainActivity.EXTRA_USERID);
        uid=preferenceConfig.readLoginStatus();

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        fetchNotificationData();
    }

    private void fetchNotificationData() {
        String url = getApplicationContext().getResources().getString(R.string.server_url)+"/notification/fetchnotifications?user_id=" + uid;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (call.isCanceled()) {
                            Toast.makeText(NotificationManager.this, "Request canceled", Toast.LENGTH_SHORT).show();
                        } else {
                            int httpErrorCode = 0;
                            if (call != null && call.isExecuted() && call.request() != null && call.request().body() != null && call.request().body().contentType() != null) {
                                try {
                                    httpErrorCode = call.isExecuted() ? call.execute().code() : 0;
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            if (httpErrorCode == 404) {
                                defaultmsg.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(NotificationManager.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();

                    if (responseData.isEmpty()) {
                        // If response body is empty, show default message
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                defaultmsg.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(responseData);
                            if (jsonArray.length() == 0) {
                                // If response body has no data, show default message
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                        defaultmsg.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                // Parse JSON data and update UI
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String title = jsonObject.getString("title");
                                    String heading = jsonObject.getString("heading");
                                    String imageUrl = jsonObject.getString("url");
                                    String cat_id = jsonObject.getString("cat_id");

                                    final NotificationItem item = new NotificationItem(title, heading, imageUrl);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            notificationList.add(item);
                                            adapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(NotificationManager.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(NotificationManager.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id= item.getItemId();
        if (id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRefresh() {
        // Set refreshing state to true to prevent further refresh actions
        swipeRefreshLayout.setRefreshing(true);

        // Clear existing data and notify adapter
        notificationList.clear();
        adapter.notifyDataSetChanged();

        // Fetch new notification data
        fetchNotificationData();
    }
}
