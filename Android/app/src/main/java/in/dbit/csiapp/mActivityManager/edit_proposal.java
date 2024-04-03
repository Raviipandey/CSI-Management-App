package in.dbit.csiapp.mActivityManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import in.dbit.csiapp.Prompts.Manager;
import in.dbit.csiapp.R;
import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.mFragments.datePickerFrag_min;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class edit_proposal extends AppCompatActivity {

    EditText e_name,e_theme,e_desc,e_edate,e_cb,e_pb,e_gb;
    private SharedPreferenceConfig preferenceConfig;
    TextView edate_s;
    Button edit;
    String eid,date=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_proposal);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        getSupportActionBar().setTitle("Edit Proposal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        e_name = findViewById(R.id.name);
        e_theme = findViewById(R.id.theme);
        e_desc = findViewById(R.id.description);
        e_edate = findViewById(R.id.edate);
        edate_s = findViewById(R.id.showdate_edit_p);
         e_cb = findViewById(R.id.cbudget);
         e_pb = findViewById(R.id.pbudget);
        e_gb = findViewById(R.id.gbudget);
        edit=findViewById(R.id.edit_button);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {

        } else {
            String data = extras.getString("data");
            eid=extras.getString("cpm_id");
            Log.i("volleyABC response", eid+data);

//            Toast.makeText(edit_proposal.this, data,Toast.LENGTH_SHORT).show();
            try {
                set(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendData();
            }
        });



        DatePickerDialog.OnDateSetListener onEDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {


                date = String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                        + "-" + String.valueOf(dayOfMonth);

                //TextView outputDate = rootView.findViewById(R.id.date);
                // outputDate.setText(date);
                Log.i("info1234", date+"event");
                TextView edate_s = findViewById(R.id.showdate_edit_p);
                edate_s.setText((String) date);
                edate_s.setVisibility(View.VISIBLE);
            }
        };

        Button dateOfevent = findViewById(R.id.dateOfevent_edit_p);
        dateOfevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerFrag_min nf = new datePickerFrag_min();
                nf.setCallBack(onEDate);
                nf.show(getSupportFragmentManager(),"datepicker");

            }
        });


    }
    void set(String response) throws JSONException {


        JSONObject res = new JSONObject(response);


        e_name.setText(res.getString("proposals_event_name"));
        e_theme.setText(res.getString("proposals_event_category"));
         date=res.getString("proposals_event_date");
        date = date.substring(0,4) + "-" + date.substring(5,7) + "-" + date.substring(8,10);
        e_edate.setText(date);
        edate_s.setText((String) date);
        e_desc.setText(res.getString("proposals_desc"));
        e_cb.setText(res.getString("proposals_creative_budget"));
        e_pb.setText(res.getString("proposals_publicity_budget"));
        e_gb.setText(res.getString("proposals_guest_budget"));
        Log.i("response" , response);


    }

    void sendData() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("eid",eid);
            jsonObject.put("name",e_name.getText().toString());
            jsonObject.put("date", date);
            jsonObject.put("theme",e_theme.getText().toString());
            jsonObject.put("description", e_desc.getText().toString());
            jsonObject.put("cb", e_cb.getText().toString());
            jsonObject.put("pb", e_pb.getText().toString());
            jsonObject.put("gb", e_gb.getText().toString());

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonObject.toString();
        Log.i("volleyABC ", "edited request body"+requestBody);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,getApplicationContext().getResources().getString(R.string.server_url) + "/proposal/editproposal", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i("volleyABC response", response);
                Toast.makeText(edit_proposal.this,"Edited",Toast.LENGTH_SHORT).show();//it will not occur as authenticating at start
//                finish();
                Intent manager = new Intent(edit_proposal.this, Manager.class);
                startActivity(manager);
                finish();

//
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
                    Toast.makeText(edit_proposal.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(edit_proposal.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(edit_proposal.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(edit_proposal.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }

        }){
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
        RequestQueue requestQueue= Volley.newRequestQueue(edit_proposal.this);
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
