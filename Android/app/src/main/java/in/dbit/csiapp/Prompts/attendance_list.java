 package in.dbit.csiapp.Prompts;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import in.dbit.csiapp.R;
import in.dbit.csiapp.SharedPreferenceConfig;
//import in.dbit.csiapp.mAdapter.ExampleItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static in.dbit.csiapp.mFragments.AttendanceSBC.EXTRA_CLASS;

public class attendance_list extends AppCompatActivity {

    String sam;
    String server_url;
    TableLayout tableLayout;
    private SharedPreferenceConfig preferenceConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/attendance/view";
        setContentView(R.layout.activity_attendance_list);
        getSupportActionBar().setTitle("Attendance List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tableLayout = findViewById(R.id.display_table);

        Intent intent = getIntent();
        sam = intent.getStringExtra(EXTRA_CLASS);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());

        //Toast.makeText(this, "Your selected " + sam, Toast.LENGTH_SHORT).show();

        parseJSON();
    }

    private void parseJSON() {
        //creating jsonobject starts
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("year", sam);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //creating jsonobject ends

        //checking data inserted into json object
        final String requestBody = jsonObject.toString();
        Log.i("volleyABC", requestBody);

        //getting response from server starts
        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i("volleyABC", "got response    " + response);
                //Toast.makeText(attendance_list.this, "Logged IN", Toast.LENGTH_SHORT).show();

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject students = jsonArray.getJSONObject(i);

                        TableRow tableRow = new TableRow(attendance_list.this);
                        TextView tv1 = new TextView(attendance_list.this);
                        TextView tv2 = new TextView(attendance_list.this);
                        //     TextView tv3 = new TextView(attendance_list.this);
                        TextView tv4 = new TextView(attendance_list.this);
                        tv1.setText(students.getString("Name"));
                        tv1.setGravity(Gravity.CENTER);
                        tv1.setBackgroundColor(getResources().getColor(R.color.white));
                        tv1.setTextColor(getResources().getColor(R.color.colorPrimary));

                        TableRow.LayoutParams param = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        param.setMargins(1, 0, 1, 1);
                        tv1.setLayoutParams(param);
                        //     tv2.setText(students.getString("total"));
                        tv2.setText(students.getString("hours_spent"));
                        tv2.setGravity(Gravity.CENTER);
                        tv2.setBackgroundColor(getResources().getColor(R.color.white));
                        tv2.setTextColor(getResources().getColor(R.color.colorPrimary));

                        TableRow.LayoutParams param1 = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        param1.setMargins(1, 0, 1, 1);
                        tv2.setLayoutParams(param1);

//                        tv3.setText(students.getString("percent"));
//                        tv3.setGravity(Gravity.CENTER);
//                        tv3.setBackgroundColor(getResources().getColor(R.color.white));
//                        tv3.setTextColor(getResources().getColor(R.color.colorPrimary));
//                        TableRow.LayoutParams param2 = new TableRow.LayoutParams(
//                                TableRow.LayoutParams.WRAP_CONTENT,
//                                TableRow.LayoutParams.WRAP_CONTENT,
//                                1.0f
//                        );
//                        param2.setMargins(1, 0, 1, 1);
//                        tv3.setLayoutParams(param2);

                        tv4.setText(students.getString("RollNo"));
                        tv4.setGravity(Gravity.CENTER);
                        tv4.setBackgroundColor(getResources().getColor(R.color.white));
                        tv4.setTextColor(getResources().getColor(R.color.colorPrimary));
                        TableRow.LayoutParams param2 = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        param2.setMargins(1, 0, 1, 1);
                        tv4.setLayoutParams(param2);
                        tableRow.addView(tv1);
                        tableRow.addView(tv2);
                        //    tableRow.addView(tv3);
                        tableRow.addView(tv4);
                        tableLayout.addView(tableRow);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

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
                    Toast.makeText(attendance_list.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(attendance_list.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(attendance_list.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(attendance_list.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        }) {
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
                headers.put("Authorization", "Bearer " + sessionToken);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
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