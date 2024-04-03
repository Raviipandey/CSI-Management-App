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

public class Creative extends AppCompatActivity implements PraposalAdapter.OnItemClickedListener {

    public static final String EXTRA_EID = "com.example.csimanagementsystem.EXTRA_EID";
    String uRole;

    private RecyclerView rv;
    private PraposalAdapter mPraposalAdapter;
    private SharedPreferenceConfig preferenceConfig;
    private ArrayList<PraposalItem> mPraposalList;
    private RequestQueue mRequestQueue;
    private String server_url, eid;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creative);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());

        Intent intent = getIntent();
        uRole = intent.getStringExtra("uRole");

        swipe();

        getSupportActionBar().setTitle("Creative");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPraposalList = new ArrayList<>();
        rv=findViewById(R.id.recycler_view_P);
        rv.setLayoutManager(new LinearLayoutManager(Creative.this));
        mRequestQueue = Volley.newRequestQueue(Creative.this);
        parseJSON(); //This method is used to get list of Agendas from server

        rv.setAdapter(new PraposalAdapter(Creative.this,mPraposalList));
        //mPraposalAdapter.setOnItemClickListener(Creative.this);

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
       // server_url = "http://192.168.43.110:8081/creative/viewListEvents";

        mPraposalList.clear();

        StringRequest stringRequest =new StringRequest(Request.Method.GET,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                swipeRefreshLayout.setRefreshing(false);
                Log.i("volleyABC" ,"got response    "+response);
//                Toast.makeText(Creative.this,response ,Toast.LENGTH_SHORT).show();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    TextView no_creative_text = findViewById(R.id.no_cr);
                    if(jsonArray.length() > 0) {
                        rv.setVisibility(View.VISIBLE);

                        no_creative_text.setVisibility(View.GONE);
                    }
                    else {
                        no_creative_text.setText("Nothing to display");
                    }
                    for(int i=0; i< jsonArray.length(); i++) {
                        JSONObject minutes = jsonArray.getJSONObject(i);

                        eid = minutes.getString("cpm_id");
                        Log.i("creative id" , eid);
                        String date = minutes.getString("proposals_event_date");
                        String Name = minutes.getString("proposals_event_name");
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
                    mPraposalAdapter = new PraposalAdapter(Creative.this, mPraposalList);
                    rv.setAdapter(mPraposalAdapter);
                    mPraposalAdapter.setOnItemClickListener(Creative.this);
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
                    Toast.makeText(Creative.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(Creative.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Creative.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(Creative.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String sessionToken = preferenceConfig.readSessionToken();
                Log.d("RequestHeaders", "Sending token: " + sessionToken); // Add this line
                headers.put("Authorization", "Bearer " + sessionToken);
                return headers;
            }
        };
        mRequestQueue.add(stringRequest);
    }
    @Override
    public void onItemClick(int position) {
        PraposalItem clickedItem = mPraposalList.get(position);
        //Toast.makeText(Creative.this,clickedItem.getmEid() , Toast.LENGTH_SHORT).show();
        Intent creative_form = new Intent(Creative.this,Creative_form.class);
        String id = clickedItem.getmEid();
        creative_form.putExtra(EXTRA_EID, id);
        creative_form.putExtra("uRole", uRole);
        Log.i("testing",id);
        startActivity(creative_form);
        //write here code when press back
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
