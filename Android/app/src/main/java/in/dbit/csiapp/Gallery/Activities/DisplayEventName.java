package in.dbit.csiapp.Gallery.Activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import in.dbit.csiapp.Gallery.EventNameAdapter.EventItem;
import in.dbit.csiapp.Gallery.EventNameAdapter.EventNameAdapter;
import in.dbit.csiapp.Gallery.ExampleDialogue;
import in.dbit.csiapp.Prompts.MainActivity;
import in.dbit.csiapp.R;
import in.dbit.csiapp.SharedPreferenceConfig;;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DisplayEventName extends AppCompatActivity implements EventNameAdapter.OnItemClickListener, ExampleDialogue.ExampleDialogListener {


    public static final String EXTRA_EVENT = "FullPath";
    private SharedPreferenceConfig preferenceConfig;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private EventNameAdapter mEventNameAdapter;
    private ArrayList<EventItem> mEventList;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_event_name);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        getSupportActionBar().setTitle("Gallery");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        mEventList = new ArrayList<>();
        mEventList.clear();
        mEventList.add(new EventItem("+\nAdd New File"));

        mEventNameAdapter = new EventNameAdapter(DisplayEventName.this, mEventList);
        mRecyclerView.setAdapter(mEventNameAdapter);

        mRequestQueue = Volley.newRequestQueue(this);
        parseJSON();
    }

    private void parseJSON() {
        //String url = "http://192.168.43.84:8080/event";
        String url =  getApplicationContext().getResources().getString(R.string.server_url) + "/gallery/event";    //Main Server URL
        //String url = "http://192.168.42.156:8080/event";
        //creating jsonobject starts
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("path","");
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
                mEventList.clear();

                Log.i("volleyABC" ,"got response    "+response);
                //Toast.makeText(DisplayEventName.this, "Got Event List", Toast.LENGTH_SHORT).show();

                //Intent manager = new Intent(MainActivity.this, Manager.class);
                //String UROLE="", USERNAME="", ProfileURL="";

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    Log.i("json length", String.valueOf(jsonArray.length()));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String event =  jsonArray.getString(i);
                        Log.i("event" ,"event " + i + " :- " + jsonArray.getString(i));
                        mEventList.add(new EventItem(event));
                    }
                    mEventList.add(new EventItem("+\nAdd academic year"));

                    mEventNameAdapter.notifyDataSetChanged();
                    //mEventNameAdapter = new EventNameAdapter(MainActivity.this, mEventList);
                    mRecyclerView.setAdapter(mEventNameAdapter);
                    mEventNameAdapter.setOnItemClickListener(DisplayEventName.this);
                }
                catch (JSONException e) {
                    Log.i("sankey", "caught in catch");
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
                    Toast.makeText(DisplayEventName.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(DisplayEventName .this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DisplayEventName.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(DisplayEventName.this, MainActivity.class);
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

    @Override
    public void onItemClick(int position) {
        if (position == mEventList.size()-1) {
            //Toast.makeText(this, "New Event", Toast.LENGTH_SHORT).show();
            openDialog();
        }
        else {
            Intent year = new Intent(this, DisplayYear.class);
            EventItem clickedItem = mEventList.get(position);

            year.putExtra(EXTRA_EVENT, clickedItem.getEventName() + "/");
            //Toast.makeText(DisplayEventName.this, clickedItem.getEventName() + "/", Toast.LENGTH_SHORT).show();
            startActivity(year);
        }
    }

    public void openDialog() {
        ExampleDialogue exampleDialogue = new ExampleDialogue();
        exampleDialogue.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(String EventName) {
        Toast.makeText(this, EventName, Toast.LENGTH_SHORT).show();
        createDirectory(EventName);
    }

    private void createDirectory(final String Directory) {

        //String url = "http://192.168.43.84:8080/mkdir";
        String url = getApplicationContext().getResources().getString(R.string.server_url) + "/gallery/mkdir";    //Main Server URL
        //String url = "http://192.168.42.156:8080/mkdir";

        //creating jsonobject starts
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("path", "");
            jsonObject.put("fname", Directory);
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

                Log.i("got response", response);
                try {
                    mEventList.clear();
                    JSONArray jsonArray = new JSONArray(response);

                    Log.i("json length", String.valueOf(jsonArray.length()));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String event =  jsonArray.getString(i);
                        Log.i("event" ,"event " + i + " :- " + jsonArray.getString(i));
                        mEventList.add(new EventItem(event));
                    }
                    mEventList.add(new EventItem("+\nAdd New File"));

                    mEventNameAdapter = new EventNameAdapter(DisplayEventName.this, mEventList);
                    mRecyclerView.setAdapter(mEventNameAdapter);
                    mEventNameAdapter.setOnItemClickListener(DisplayEventName.this);
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
                    Toast.makeText(DisplayEventName.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(DisplayEventName.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DisplayEventName.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(DisplayEventName.this, MainActivity.class);
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
