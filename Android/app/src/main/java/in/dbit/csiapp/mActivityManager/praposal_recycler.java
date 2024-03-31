package in.dbit.csiapp.mActivityManager;

import android.content.Intent;
import android.os.Handler;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class praposal_recycler extends AppCompatActivity implements  PraposalAdapter.OnItemClickedListener  {
    public static String eid ="hello";
    public static String st=null;
    private SharedPreferenceConfig preferenceConfig;
    String urole1=null;
    private Button add_praposal;
    private RecyclerView rv;
    private PraposalAdapter mPraposalAdapter;
    private ArrayList<PraposalItem> mPraposalList;
    private RequestQueue mRequestQueue;
    private String server_url;

    EditText SearchInput;
    CharSequence search="";
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_praposal_recycler);
        getSupportActionBar().setTitle("Proposal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        urole1=preferenceConfig.readRoleStatus();

        SearchInput = findViewById(R.id.search_bar);
        rv = findViewById(R.id.recycler_view_praposal);
        mPraposalList = new ArrayList<>();
        mPraposalAdapter = new PraposalAdapter(praposal_recycler.this, mPraposalList);

        rv.setLayoutManager(new LinearLayoutManager(praposal_recycler.this));
        rv.setAdapter(mPraposalAdapter);
        mRequestQueue = Volley.newRequestQueue(praposal_recycler.this);


        Log.i("info123","p2");

        Log.i("info123","p3");
        swipe();
        parseJSON(); //This method is used to get list of Agendas from server
        Log.i("info123","p4");

        Log.i("info123","p5");



        SearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPraposalAdapter.getFilter().filter(s);
                search = s;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



//        praposal add button

        add_praposal = (Button) findViewById(R.id.praposal_add);
        if(urole1.equals("Tech Head") || urole1.equals("Event Head")) {
            add_praposal.setVisibility(View.VISIBLE);
        }
        add_praposal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(praposal_recycler.this ,Proposal.class);
//                intent.putExtra("id",UID);
//                intent.putExtra(EXTRA_FLAG, FLAG);
                startActivity(intent);
            }
        });

//        praposal add button finishes here

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
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/proposal/viewlistproposal/";   //Main Server URl
        mPraposalList.clear();

        StringRequest stringRequest =new StringRequest(Request.Method.GET,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                swipeRefreshLayout.setRefreshing(false);
                Log.i("volleyABC" ,"got response    "+response);
                mPraposalList.clear();
//                Toast.makeText(praposal_recycler.this,response ,Toast.LENGTH_SHORT).show();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    TextView no_prop_text = findViewById(R.id.no_pro);
                    if(jsonArray.length() > 0) {
                        rv.setVisibility(View.VISIBLE);

                        no_prop_text.setVisibility(View.GONE);
                    }
                    else {
                        no_prop_text.setText("No pending Requests");
                    }
                    for(int i=0; i< jsonArray.length(); i++) {
                            JSONObject minutes = jsonArray.getJSONObject(i);

                            String eid = minutes.getString("cpm_id");
                            String Name = minutes.getString("proposals_event_name");
                            String status = minutes.getString("proposals_status");
                            String theme = minutes.getString("proposals_event_category");
                            String date = minutes.getString("proposals_event_date");

                            //in the above variable date we are not getting date in DD:MM:YYYYY
                            //so we are creating new variable date1 to get our desire format
                           String date1 = date.substring(8,10) + "/" + date.substring(5,7) + "/" + date.substring(0,4);

                            if(urole1.equals("HOD") && (status.equals("2") || status.equals("3") || status.equals("-3"))){ //for hod, only sbc approved and self approved will be shown
                                    mPraposalList.add(new PraposalItem(eid,"Date: "+date1, Name, status,"Theme: "+ theme));

                            }
                            else if(urole1.equals("SBC") && (status.equals("1") || status.equals("2") || status.equals("3") || status.equals("-3") || status.equals("-2"))) { //for sbc, only chairperson approved, self approved and hod approved will be shown
                                mPraposalList.add(new PraposalItem(eid,"Date: "+date1, Name, status,"Theme: "+ theme));
                            }
                            else if(urole1.equals("Chairperson") && (status.equals("1") || status.equals("0") || status.equals("2") || status.equals("3") || status.equals("-1") || status.equals("-2") || status.equals("-3"))) { //for chairperson, fresh proposal, sbc approved, self approved and hod approved will be shown
                                mPraposalList.add(new PraposalItem(eid,"Date: "+date1, Name, status,"Theme: "+ theme));
                            }
                            else if(!urole1.equals("HOD") && !urole1.equals("SBC") && !urole1.equals("Chairperson")) {
                                mPraposalList.add(new PraposalItem(eid,"Date: "+date1, Name, status,"Theme: "+ theme));

                            }


                    }

                    mPraposalAdapter.notifyDataSetChanged();

                    mPraposalAdapter.setOnItemClickListener(praposal_recycler.this);
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
                    Toast.makeText(praposal_recycler.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(praposal_recycler.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(praposal_recycler.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(praposal_recycler.this, MainActivity.class);
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
         eid =clickedItem.getmEid();
         st = clickedItem.getmStatus();


        Intent proposal_desc = new Intent(praposal_recycler.this,proposal_desc.class);
        proposal_desc.putExtra(st,st);
        proposal_desc.putExtra(eid,eid);
        startActivity(proposal_desc);  //here sbc head can approve the praposal that info should show in this

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
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        parseJSON();
    }
}
