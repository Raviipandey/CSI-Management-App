package com.example.csi.mActivityManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.example.csi.Prompts.Manager;
import com.example.csi.R;
import com.example.csi.SharedPreferenceConfig;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Publicity extends AppCompatActivity {

    private SharedPreferenceConfig preferenceConfig;
    String urole1=null;
    LinearLayout pr_lay;
    Button edit_pr,submit_pr;
    String eid;
    TextView eventName, eventTheme, event_date, eventDescription, speaker, venue, fee_csi, fee_non_csi, prize, cr_budget, pub_budget, guest_budget;
    EditText target_aud,comments, money_c , money_s;
    CheckBox reg_desk , inclass_pub;
    LinearLayout comments_layout;
    private LinearLayout checkboxContainer;
    private ArrayList<String> checkboxNames = new ArrayList<>();
    private List<CheckBox> checkBoxList = new ArrayList<>();
    private List<CheckBox> checkedboxes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicity);
        getSupportActionBar().setTitle("Publicity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        urole1=preferenceConfig.readRoleStatus();
        submit_pr=findViewById(R.id.submit_pl);
        edit_pr=findViewById(R.id.edit_pr_req);
        pr_lay=findViewById(R.id.pr_pl);
        checkboxContainer=findViewById(R.id.pub_checkbox_container);
//        Toast.makeText(Publicity.this,"role: "+urole1,Toast.LENGTH_SHORT).show();
//        Log.i("volleyABC ", urole1);


        eventName = (TextView)findViewById(R.id.name_pl);
        eventTheme = (TextView)findViewById(R.id.theme_pl);
        event_date = (TextView)findViewById(R.id.ed_pl);
        speaker = (TextView) findViewById(R.id.speaker_pl);
        venue = (TextView) findViewById(R.id.venue_pl);
        fee_csi = (TextView) findViewById(R.id.fee_csi_pl);
        fee_non_csi = (TextView) findViewById(R.id.fee_non_csi_pl);
        prize = (TextView) findViewById(R.id.prize_pl);
        eventDescription = (TextView)findViewById(R.id.desc_pl);
        target_aud=findViewById(R.id.pr_target);
        comments=findViewById(R.id.pr_comments);
        money_c=findViewById(R.id.pr_m_coll);
        money_s=findViewById(R.id.pr_m_spent);
        reg_desk=findViewById(R.id.pr_desk);
        inclass_pub=findViewById(R.id.pr_inclass);
        cr_budget=findViewById(R.id.cb);
        pub_budget=findViewById(R.id.pb);
        guest_budget=findViewById(R.id.gb);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {

        } else {
            eid=extras.getString("EID");

        }

        Button pub_add_checkbox = findViewById(R.id.pub_add_checkbox_button);

        if(urole1.equals("PR Head")){
            edit_pr.setVisibility(View.VISIBLE);
            pub_add_checkbox.setVisibility(View.GONE);
        }
        else {
            pr_lay.setVisibility(View.VISIBLE);
            submit_pr.setVisibility(View.GONE);
            reg_desk.setEnabled(false);
            inclass_pub.setEnabled(false);
            findViewById(R.id.pr_target).setEnabled(false);
            findViewById(R.id.pr_comments).setEnabled(false);
            findViewById(R.id.pr_m_coll).setEnabled(false);
            findViewById(R.id.pr_m_spent).setEnabled(false);
        }

        edit_pr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edit_pr.setVisibility(View.GONE);
                pub_add_checkbox.setVisibility(View.VISIBLE);
                pr_lay.setVisibility(View.VISIBLE);
            }
        });

        submit_pr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volley_send();

            }
        });
        volley_get();
        pub_add_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNamePrompt();
            }
        });
    }

    private void showNamePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name");

        final View inputView = getLayoutInflater().inflate(R.layout.name_prompt, null);
        builder.setView(inputView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = ((EditText) inputView.findViewById(R.id.name_edit_text)).getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    addCheckbox(name);
                    Log.i("checkboxx" , name);
                } else {
                    Toast.makeText(Publicity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    public void addCheckbox(String name) {
//        LinearLayout checkboxContainer;


        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(name);
        checkboxContainer.addView(checkBox);
        checkBoxList.add(checkBox);


        Log.i("listt",checkBoxList.get(0).getText().toString());
//        CheckBox checkBox = new CheckBox(this);
//        checkBox.setText(name);
//        checkboxContainer.addView(checkBox);
    }

    //volley function
    public void volley_get(){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eid", eid);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonObject.toString();
        Log.i("volleyABC ", requestBody);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/viewEvent", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                ret[0]=response;
                Log.i("volleyABC4985" ,"got response    "+response);
                //Toast.makeText(Creative_form.this, "Logged IN", Toast.LENGTH_SHORT).show();
                Log.i("volleyABC4985" ,"got response    "+response);
                //Toast.makeText(Creative_form.this, "Logged IN", Toast.LENGTH_SHORT).show();
                Log.i("volleyABC" ,"reposnsde"+response);
                if(response != null){
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        // Log.i("tracking uid","main Activity "+UID);

                        eventName.setText(jsonObject1.getString("proposals_event_name"));
                        eventTheme.setText(jsonObject1.getString("proposals_event_category"));

                        speaker.setText(jsonObject1.getString("speaker"));
                        venue.setText(jsonObject1.getString("proposals_venue"));
                        fee_csi.setText(jsonObject1.getString("proposals_reg_fee_csi"));
                        fee_non_csi.setText(jsonObject1.getString("proposals_reg_fee_noncsi"));
                        prize.setText(jsonObject1.getString("proposals_prize"));
                        eventDescription.setText(jsonObject1.getString("proposals_desc"));

                        if((int) jsonObject1.get("pr_desk_publicity")==1) {
                            reg_desk.setChecked(true);
                        }
                        else reg_desk.setChecked(false);
                        if((int) jsonObject1.get("pr_class_publicity")==1){
                            inclass_pub.setChecked(true);
                        }
                        else inclass_pub.setChecked(false);
                        
                        target_aud.setText(jsonObject1.getString("pr_member_count"));
                        comments.setText(jsonObject1.getString("pr_comment"));
                        money_c.setText(jsonObject1.getString("pr_rcd_amount"));
                        money_s.setText(jsonObject1.getString("pr_spent"));
                        cr_budget.setText(jsonObject1.getString("proposals_creative_budget"));
                        pub_budget.setText(jsonObject1.getString("proposals_publicity_budget"));
                        guest_budget.setText(jsonObject1.getString("proposals_guest_budget"));

                        LinearLayout tasksContainer = findViewById(R.id.pub_tasks_container);
                        String tasksString = jsonObject1.getString("tasks");
                        JSONArray tasksArray = new JSONArray(tasksString);

                        for (int i = 0; i < tasksArray.length(); i++) {
                            String taskName = tasksArray.getString(i);
                            Log.i("server se aaya array", taskName );
                            CheckBox checkBox = new CheckBox(getApplicationContext());
                            checkBox.setText(taskName);
                            tasksContainer.addView(checkBox);
                        }

                        String eventDate=jsonObject1.getString("proposals_event_date");
                        String date = eventDate.substring(8,10) + "/" + eventDate.substring(5,7) + "/" + eventDate.substring(0,4);
                        event_date.setText(date);
                        getSupportActionBar().setTitle(jsonObject1.getString("name"));


                        //Send data to Manager.java starts
                        // Call manager.java file i.e. Activity with navigation drawer activity
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        },new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {

                try{
                    Log.i("volleyABC" ,Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(Publicity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show(); //This method is used to show pop-up on the screen if user gives wrong uid
                    error.printStackTrace();}
                catch (Exception e)
                {
                    Toast.makeText(Publicity.this,"Check Network",Toast.LENGTH_SHORT).show();

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
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
//        return ret[0];


    }

    public void volley_send(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eid", eid);
            jsonObject.put("pr_member_count", target_aud.getText().toString());
//            jsonObject.put("in_class", inclass_pub.getText().toString());
            jsonObject.put("pr_rcd_amount", money_c.getText().toString());
            jsonObject.put("pr_spent", money_s.getText().toString());
            jsonObject.put("pr_comment",comments.getText().toString());
            if(reg_desk.isChecked())
                jsonObject.put("pr_desk_publicity",1);
            else jsonObject.put("pr_desk_publicity",0);

            if(inclass_pub.isChecked())
                jsonObject.put("pr_class_publicity",1);
            else jsonObject.put("pr_class_publicity",0);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = jsonObject.toString();
        Log.i("volleyABC ", requestBody);

        // Create a RequestQueue to handle the API request
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a string array to store the checked checkboxes
        ArrayList<String> checkedCheckboxes = new ArrayList<>();

        // Add the checked checkboxes to the string array
        String BoxStatus = "";
        for (int i = 0; i < checkBoxList.size(); i++) {
            View view = checkBoxList.get(i);
            CheckBox checkBox = (CheckBox) view;
            checkedCheckboxes.add(checkBox.getText().toString());
            Log.i("arrayss" , checkedCheckboxes.toString());
            if (checkBox.isChecked()) {
                BoxStatus = "1";
            }
            else{
                BoxStatus = "0";
            }
        }

        // Create a JSON object to store the data to be sent to the server
        JSONObject jsonObjectnew = new JSONObject();

        try {
            // Add the checkedCheckboxes list to the JSON object
            jsonObjectnew.put("checkedCheckboxes", new JSONArray(checkedCheckboxes));
            jsonObjectnew.put("eid", eid);
            jsonObjectnew.put("status", BoxStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a POST request to the server with the data
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/addcheckbox", jsonObjectnew,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley Response", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Volley Error", error.toString());
            }
        });

        final String[] ret = new String[1];
        ret[0]=null;
        String r=null;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,getApplicationContext().getResources().getString(R.string.server_url) + "/publicity/editPublicity", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                ret[0]=response;
                Toast.makeText(Publicity.this,"Submitted",Toast.LENGTH_SHORT).show();
//                finish();
                Intent manager = new Intent(Publicity.this, Manager.class);
                startActivity(manager);
                finish();



            }
        },new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {

                try{
                    Log.i("volleyABC" ,Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(Publicity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show(); //This method is used to show pop-up on the screen if user gives wrong uid
                    error.printStackTrace();}
                catch (Exception e)
                {
                    Toast.makeText(Publicity.this,"Check Network",Toast.LENGTH_SHORT).show();

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
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        //sending JSONOBJECT String to server ends

//        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
        requestQueue.add(jsonObjectRequest);



    }


//    get data to set values
//    public void getData(){
//
//        final JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("eid", eid);
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//        //creating jsonobject ends
//
//        //checking data inserted into json object
////        String response = volley(jsonObject, getApplicationContext().getResources().getString(R.string.server_url) + "/creative/viewpropdetail");
//
//
//
//    } Log.i("volleyABC" ,"reposnsde"+response);
//        if(response != null){
//        try {
//            JSONObject jsonObject1 = new JSONObject(response);
//            // Log.i("tracking uid","main Activity "+UID);
//
//            eventName.setText(jsonObject1.getString("name"));
//            eventTheme.setText(jsonObject1.getString("theme"));
//
//            speaker.setText(jsonObject1.getString("speaker"));
//            venue.setText(jsonObject1.getString("venue"));
//            fee_csi.setText(jsonObject1.getString("reg_fee_c"));
//            fee_non_csi.setText(jsonObject1.getString("reg_fee_nc"));
//            prize.setText(jsonObject1.getString("prize"));
//            eventDescription.setText(jsonObject1.getString("description"));
//
//
//            String eventDate=jsonObject1.getString("event_date");
//            String date = eventDate.substring(8,10) + "/" + eventDate.substring(5,7) + "/" + eventDate.substring(0,4);
//            event_date.setText(date);
//
//            //Send data to Manager.java starts
//            // Call manager.java file i.e. Activity with navigation drawer activity
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }





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
