package in.dbit.csiapp.Prompts;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import in.dbit.csiapp.R;
import in.dbit.csiapp.SharedPreferenceConfig;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddMinute extends AppCompatActivity {

    private SharedPreferenceConfig preferenceConfig;

    AutoCompleteTextView mCreateAgenda;
    Button mAddMinute, mAddTask;
    String Agenda, Points, Creator, Absentee, server_url, date, time, uname , uid;
    EditText  mCreatePoints, mTask, mAbsentee;
    Spinner spinner;
    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("i07","Entered1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_minute);
        getSupportActionBar().setTitle("Add Minute");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        Intent intent = getIntent();
        Creator = intent.getStringExtra("id"); //getting User ID from MinuteManager


        uname = intent.getStringExtra(MainActivity.EXTRA_UNAME);
        uname=preferenceConfig.readNameStatus();

        uid = intent.getStringExtra(MainActivity.EXTRA_USERID);
        uid=preferenceConfig.readLoginStatus();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        date = dateFormat.format(calendar.getTime());
        Log.i("date",date);
        time = timeFormat.format(calendar.getTime());
        Log.i("time",time );




        server_url=getApplicationContext().getResources().getString(R.string.server_url) + "/minutes/create";  //Main Server URL
        //server_url="http://192.168.43.84:8080/minutes/create";

        //local spinner starts

//        spinner = findViewById(R.id.csi_members);
//        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.csi_members_name, android.R.layout.simple_spinner_item);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(spinnerAdapter);
        //spinnerAdapter.add("Sanket Deshmukh");
        //spinnerAdapter.add("Afif Shaikh");
        //spinnerAdapter.notifyDataSetChanged();

        //local spinner ends

        mAddTask = findViewById(R.id.add_task);
        mCreatePoints = findViewById(R.id.create_points);
        mTask = findViewById(R.id.task);

        //dynamic spinner

        RequestQueue mQueue;
        Spinner spinner;

        mQueue = Volley.newRequestQueue(getApplicationContext());
        spinner = findViewById(R.id.members);
//                String url = "http://yourserver.com/data.json";
        String url = getApplicationContext().getResources().getString(R.string.server_url) + "/minutes/members";
        Log.i("queue ke andar aaya" , url);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("response aaya" , response);
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray array = json.getJSONArray("members");
                            ArrayList<String> list = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                list.add(array.getString(i));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

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
                            Toast.makeText(AddMinute.this, "Session expired", Toast.LENGTH_LONG).show();
                        } else if ("Another device has logged in".equals(errorMessage)) {
                            Toast.makeText(AddMinute.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AddMinute.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            // Handle logout if session is expired or taken over
                            preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                            Intent loginIntent = new Intent(AddMinute.this, MainActivity.class);
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
            }};

        mQueue.add(request);


        mAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow mainRow = findViewById(R.id.row1);
                mainRow.setVisibility(View.VISIBLE);
                TableRow tablerow;
                tableLayout = findViewById(R.id.table);
                TextView tv1, tv2;

                tablerow = new TableRow(AddMinute.this);
                tablerow.setClickable(true);
                /*tablerow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editbtn.setVisibility(View.VISIBLE);

                        final TextView sample1 = (TextView) tablerow.getChildAt(0);
                        final TextView sample2 = (TextView) tablerow.getChildAt(1);

                        et1.setText(sample1.getText());
                        et2.setText(sample2.getText());

                        editbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sample1.setText(et1.getText());
                                sample2.setText(et2.getText());

                                et1.setText("");
                                et2.setText("");

                                editbtn.setVisibility(View.GONE);
                            }
                        });
                        //Toast.makeText(MainActivity.this, sample.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });*/

                tv1 = new TextView(AddMinute.this);
                tv2 = new TextView(AddMinute.this);

                String sam = mTask.getText().toString();
                mTask.setText("");
                tv1.setText(sam);

                tv1.setGravity(Gravity.CENTER);
                tv1.setBackgroundColor(getResources().getColor(R.color.white));
                tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tableborder, 0, 0, 0);

                TableRow.LayoutParams param = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.1f
                );
                param.setMargins(1, 0, 1, 1);
                tv1.setLayoutParams(param);

                sam = spinner.getSelectedItem().toString();
                spinner.setSelection(0);
                tv2.setText(sam);

                tv2.setGravity(Gravity.CENTER);
                tv2.setBackgroundColor(getResources().getColor(R.color.white));
                tv2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tableborder, 0, 0, 0);
                tv2.setTextColor(getResources().getColor(R.color.colorPrimary));

                TableRow.LayoutParams param1 = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        0.1f
                );
                param1.setMargins(0, 0, 1, 1);
                tv2.setLayoutParams(param1);

                tablerow.addView(tv1);
                tablerow.addView(tv2);

                tableLayout.addView(tablerow);
                //tablerow = (TableRow) tableLayout.getChildAt(1);
                //tablerow.setClickable(true);
                Toast.makeText(AddMinute.this, (CharSequence) spinner.getSelectedItem(), Toast.LENGTH_SHORT).show();
            }
        });

        mCreateAgenda = findViewById(R.id.create_agenda);
        mCreatePoints = findViewById(R.id.create_points);
        mAbsentee = findViewById(R.id.absentee);
        mCreatePoints.setMaxLines(10);
        mCreatePoints.setVerticalScrollBarEnabled(true);
        mCreatePoints.setMovementMethod(new ScrollingMovementMethod());
        mAddMinute = findViewById(R.id.add_minute);

        mAddMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("i234","Add Minute");
                Agenda = mCreateAgenda.getText().toString();
                Points = mCreatePoints.getText().toString();
                Absentee = mAbsentee.getText().toString();

                //createMinuteTesting();
                createNewMinute();
                fetchAllTokens(); //sending new created minute to server

                finish();
            }

        });
    }

    // Method to fetch all FCM tokens from the server
    // Method to fetch all FCM tokens and core_ids from the server
    private void fetchAllTokens() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getApplicationContext().getResources().getString(R.string.server_url)+"/proposal/getalltoken", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray tokensArray = new JSONArray(response);
                    Log.i("FCM SERVER", String.valueOf(tokensArray));
                    JSONArray idsArray = new JSONArray(); // Array to store core_ids
                    for (int i = 0; i < tokensArray.length(); i++) {
                        JSONObject tokenObject = tokensArray.getJSONObject(i);
                        String fcmToken = tokenObject.getString("fcm_token"); // Parse FCM token
                        String coreId = tokenObject.getString("core_id"); // Parse core_id
                        if(!coreId.equals(uid)){
                            idsArray.put(coreId);
                        }
                         // Store core_id in idsArray
                        // Call the method to send notification for each FCM token
                        sendNotification(fcmToken);
                    }
                    Log.i("Core IDs", idsArray.toString());
                    createNotification("New Minutes of meet added", uname + " has added the minutes of recent meeting.", Integer.parseInt(uid), idsArray , "1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){ @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            String sessionToken = preferenceConfig.readSessionToken();
            headers.put("Authorization", "Bearer " + sessionToken);
            return headers;
        }};
        requestQueue.add(stringRequest);
    }

    private void createNotification(String title, String body, int senderId, JSONArray receiverIds , String cat_id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nd_title", title);
            jsonBody.put("nd_body", body);
            jsonBody.put("nd_sender_id", senderId);
            jsonBody.put("nd_receiver_ids", receiverIds);
            jsonBody.put("nc_id" , cat_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getApplicationContext().getResources().getString(R.string.server_url)+"/notification/createnotification", jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("CREATE_NOTIFICATION", "Notification created successfully");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }



    // Method to send notification after successful proposal submission
    private void sendNotification(String fcmtoken) {
        // Construct the notification payload
        JSONObject notification = new JSONObject();
        try {
            notification.put("to", fcmtoken); // Using the FCM token obtained earlier
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", "New Minutes of meet added");
            notificationBody.put("body",  uname + " has added the minutes of recent meeting.");
            notification.put("notification", notificationBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send",
                response -> Log.d("FCM", "Notification sent successfully"),
                error -> Log.e("FCM", "Failed to send notification: " + error.getMessage())) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return notification.toString().getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }

            }


            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "key=AAAA-xbkyRA:APA91bF2uRduQA3hfb72XF9B7sjfw0vU1AN1YyrbutqPn34Fbn7fF6fGrj8xgfdCR6au12lFrafusW03uZjVwUXmFV6DPlixorLCIVZuv-r6YyyEOVWj8d6cOfna7FcG96d3_-hbSx3B");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void createNewMinute() {
        Log.i("i234","Create Minute");
        JSONArray jsonArray = new JSONArray();

        TableRow mainRow = findViewById(R.id.row1);
        if(mainRow.getVisibility() == View.VISIBLE) {
            for (int i = 1; i < tableLayout.getChildCount(); i++) {
                TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
                //Toast.makeText(this, tableRow.toString(), Toast.LENGTH_SHORT).show();

                TextView textView1 = (TextView) tableRow.getChildAt(0);
                TextView textView2 = (TextView) tableRow.getChildAt(1);

                JSONObject jsonObject1 = new JSONObject();
                try {
                    jsonObject1.put("task", textView1.getText());
                    jsonObject1.put("person", textView2.getText());

                    jsonArray.put(jsonObject1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Toast.makeText(AddMinute.this, "Please Assign at least 1 Task", Toast.LENGTH_SHORT).show();
        }

        final JSONObject jsonObject1 = new JSONObject();
        try {
            jsonObject1.put("minutes",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JSONObject jsonObject = new JSONObject();
        try {
            Log.i("i234","Send JSON");
            jsonObject.put("id",Creator);
            jsonObject.put("agenda", Agenda);
            jsonObject.put("points", Points);
            jsonObject.put("absentee", Absentee);
            jsonObject.put("work", jsonObject1);
            jsonObject.put("date", date);
            jsonObject.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonObject.toString();
        Log.i("i234",requestBody);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    Log.i("i234" ,"got response    "+response);
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
                    Toast.makeText(AddMinute.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(AddMinute.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddMinute.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(AddMinute.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        }) {

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
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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