package in.dbit.csiapp.mActivityManager;

import android.content.Intent;
import android.os.Handler;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import in.dbit.csiapp.Prompts.MainActivity;
import in.dbit.csiapp.R;
import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.mAdapter.PraposalAdapter;
import in.dbit.csiapp.mAdapter.PraposalItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Technical extends AppCompatActivity implements  PraposalAdapter.OnItemClickedListener {

    private RecyclerView rv;
    private PraposalAdapter mPraposalAdapter;
    private ArrayList<PraposalItem> mPraposalList;
    private RequestQueue mRequestQueue;
    private SharedPreferenceConfig preferenceConfig;
    private String server_url;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technical);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());

        getSupportActionBar().setTitle("Technical");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipe();

        mPraposalList = new ArrayList<>();
        rv=findViewById(R.id.recycler_view_T);
        rv.setLayoutManager(new LinearLayoutManager(Technical.this));
        mRequestQueue = Volley.newRequestQueue(Technical.this);
        parseJSON(); //This method is used to get list of Agendas from server

        rv.setAdapter(new PraposalAdapter(Technical.this,mPraposalList));
    }

    void swipe() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresher_P);
        //swipeRefreshLayout.setColorSchemeResources(R.color.Red,R.color.OrangeRed,R.color.Yellow,R.color.GreenYellow,R.color.BlueViolet);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        int min = 65;
                        int max = 95;

                        parseJSON();
                    }
                }, 1000);
            }
        });
    }

    public void parseJSON() {
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/creative/listcreative";   //Main Server URL
        //server_url = "http://192.168.43.110:8081/creative/viewListEvents";

        mPraposalList.clear();

        StringRequest stringRequest =new StringRequest(Request.Method.GET,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                swipeRefreshLayout.setRefreshing(false);
                Log.i("volleyABC" ,"got response    "+response);
//                Toast.makeText(Technical.this,response ,Toast.LENGTH_SHORT).show();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    TextView no_tech_text = findViewById(R.id.no_tech);
                    if(jsonArray.length() > 0) {
                        rv.setVisibility(View.VISIBLE);

                        no_tech_text.setVisibility(View.GONE);
                    }
                    else {
                        no_tech_text.setText("Nothing to display");
                    }

                    for(int i=0; i< jsonArray.length(); i++) {
                        JSONObject minutes = jsonArray.getJSONObject(i);

                        String eid = minutes.getString("cpm_id");
                        String date = minutes.getString("proposals_event_date");
                        String Name = minutes.getString("proposals_event_name");
                        //String status = minutes.getString("status");
                        String theme =minutes.getString("proposals_event_category");
                        //String points = minutes.getString("minute");

                        //in the above variable date we are not getting date in DD:MM:YYYYY
                        //so we are creating new variable date1 to get our desire format
                        String date1 = date.substring(8,10) + "/" + date.substring(5,7) + "/" + date.substring(0,4);

                        mPraposalList.add(new PraposalItem(eid,"Date: "+date1, Name,"T","Theme: "+ theme));

                    }

                    mPraposalAdapter = new PraposalAdapter(Technical.this, mPraposalList);
                    rv.setAdapter(mPraposalAdapter);
                    mPraposalAdapter.setOnItemClickListener(Technical.this);

                } catch (JSONException e) {
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
                    Toast.makeText(Technical.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(Technical.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Technical.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(Technical.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        }){ @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            String sessionToken = preferenceConfig.readSessionToken();
            Log.d("RequestHeaders", "Sending token: " + sessionToken); // Add this line
            headers.put("Authorization", "Bearer " + sessionToken);
            return headers;
        }};
        mRequestQueue.add(stringRequest);
    }

    @Override
    public void onItemClick(int position) {
        PraposalItem clickedItem = mPraposalList.get(position);
        //Toast.makeText(Technical.this,clickedItem.getmEid() , Toast.LENGTH_SHORT).show();
        Intent technical_form = new Intent(Technical.this,Technical_form.class);
        String id = clickedItem.getmEid();
        technical_form.putExtra("EID", id);
        startActivity(technical_form);

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