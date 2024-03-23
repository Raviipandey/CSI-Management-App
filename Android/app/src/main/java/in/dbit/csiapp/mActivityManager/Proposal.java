package in.dbit.csiapp.mActivityManager;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import in.dbit.csiapp.mFragments.datePickerFrag;
import in.dbit.csiapp.mFragments.datePickerFrag_min;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proposal extends AppCompatActivity {

    private SharedPreferenceConfig preferenceConfig;
    String date = null;
    String edate = null;
    String three_track = null;
    EditText description;
    String selectedoption;

    String uname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal);


        Spinner threetrackspinner;
        threetrackspinner = findViewById(R.id.threetrackspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.threetrackarray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        threetrackspinner.setAdapter(adapter);
        description = findViewById(R.id.pdescription);
        description.setMaxLines(5);
        description.setVerticalScrollBarEnabled(true);
        description.setMovementMethod(new ScrollingMovementMethod());
        getSupportActionBar().setTitle("Add Proposal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        Intent intent = getIntent();

        uname = intent.getStringExtra(MainActivity.EXTRA_UNAME);
        uname=preferenceConfig.readNameStatus();

//        fcmtoken = intent.getStringExtra(MainActivity.EXTRA_FCMTOKEN);
//        fcmtoken= preferenceConfig.fetchfcmtoken();

//        fcmtoken = "fnl1bEjUS3idaXa8MniKIv:APA91bENrfM12zxGMGnWZRlCdlbZ9iqAbIvJ0zuV3eqtRVSiLckiE0Fs-p5qFrA1XaNg6KJ2JbmM4B45cXUNRxKiSG2WrZaA4Z-a3VBAyCZlj6QR8pZfPuEQ6aWq-RFI4-DyLpgYgTBB";
        Button submit = findViewById(R.id.submit_praposal);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afterSubmit();
            }
        });

        Button dateOfmeeting = findViewById(R.id.dateOfMeeting);
        dateOfmeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerFrag nf = new datePickerFrag();
                nf.setCallBack(onDate);
                nf.show(getSupportFragmentManager(),"datepicker");

            }
        });

        Button dateOfevent = findViewById(R.id.dateOfevent);
        dateOfevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerFrag_min nf = new datePickerFrag_min();
                nf.setCallBack(onEDate);
                nf.show(getSupportFragmentManager(),"datepicker");

            }
        });


        final Button addF = findViewById(R.id.addField);
        // final TextView done = findViewById(R.id.done);

        final Integer[] othopen = {0,0,0};
        addF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(othopen[0].equals(0))
                {
                    LinearLayout othrL1 = findViewById(R.id.otherL1);
                    othrL1.setVisibility(View.VISIBLE);
                    othopen[0] =1;
                }
                else if(othopen[0].equals(1) && othopen[1].equals(0))
                {
                    LinearLayout othrL2 = findViewById(R.id.otherL2);
                    othrL2.setVisibility(View.VISIBLE);
                    othopen[1] =1;
                }
                else if(othopen[0].equals(1) && othopen[1].equals(1) && othopen[2].equals(0) )
                {
                    LinearLayout othrL3 = findViewById(R.id.otherL3);
                    othrL3.setVisibility(View.VISIBLE);
                    othopen[2] =1;
                    addF.setVisibility(View.GONE);
                    // done.setVisibility(View.VISIBLE);
                }
            }
        });
        threetrackspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // store the selected option as text
//                String selectedOption = parent.getItemAtPosition(position).toString();
                selectedoption = parent.getItemAtPosition(position).toString();
                if(selectedoption.equals("Select Category")){
                    three_track = "";
                }
                if(selectedoption.equals("Academics")){
                    three_track = "1";
                }
                if(selectedoption.equals("Aspiration")){
                    three_track = "2";
                }
                if(selectedoption.equals("Wellness")){
                    three_track = "3";
                }
                Log.i("spinner ka input" , selectedoption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    // Method to fetch all FCM tokens from the server
    private void fetchAllTokens() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getApplicationContext().getResources().getString(R.string.server_url)+"/proposal/getalltoken", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray tokensArray = new JSONArray(response);
                    Log.i("FCM SERVER" , String.valueOf(tokensArray));
                    for (int i = 0; i < tokensArray.length(); i++) {
                        String fcmToken = tokensArray.getString(i); // Parse each token as a string
                        // Call the method to send notification for each FCM token
                        sendNotification(fcmToken);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
    }



    // Method to send notification after successful proposal submission
    private void sendNotification(String fcmtoken) {
        // Construct the notification payload
        JSONObject notification = new JSONObject();
        try {
            notification.put("to", fcmtoken); // Using the FCM token obtained earlier
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", "New Proposal Added");
            notificationBody.put("body", "A new proposal has been added by" + uname);
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



    DatePickerDialog.OnDateSetListener onDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String formattedMonth = (monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : String.valueOf(monthOfYear + 1);
            String formattedDay = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);

            date = year + "-" + formattedMonth + "-" + formattedDay;

            //TextView outputDate = rootView.findViewById(R.id.date);
            // outputDate.setText(date);
            Log.i("info1234", date);
            TextView show_date_P_m = findViewById(R.id.date_P_m);
            show_date_P_m.setText(date);
            sendDate(date);
        }
    };


    DatePickerDialog.OnDateSetListener onEDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String formattedMonth = (monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : String.valueOf(monthOfYear + 1);
            String formattedDay = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);

            edate = year + "-" + formattedMonth + "-" + formattedDay;

            Log.i("info1234", edate + "event");
            TextView edate_s = findViewById(R.id.showdate);
            edate_s.setText(edate);
            edate_s.setVisibility(View.VISIBLE);
        }
    };


    public void sendDate( String date)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("date",date);
            Log.i("info123",jsonObject.toString());
            sendData(getApplicationContext().getResources().getString(R.string.server_url) + "/proposal/viewagenda",jsonObject,0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setSpinner (String response)
    {

        Spinner spinner = findViewById(R.id.agendaSpinner);
        ArrayAdapter<String> arrayAdapter1;
        List<String> arr = new ArrayList<String>();

        if (response!=null) {
            arr.add("SELECT");
            try {

                JSONObject jsonObject1 = new JSONObject(response);

                Log.i("info123", response);

                JSONArray jagenda = jsonObject1.getJSONArray("agenda");


                for (int i = 0; i < jagenda.length(); i++)
                    arr.add(jagenda.getString(i));

                // Log.i("info123" , Arrays.toString(arr));

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("info123", "error in extracting");

            }
        }

        arrayAdapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,arr);
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(arrayAdapter1);


    }



    public void sendData(String server_url , JSONObject jsonObject , final int flag)
    {
        final String requestBody = jsonObject.toString();
        Log.i("info123", requestBody);

        StringRequest stringRequest =new StringRequest(Request.Method.POST,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.i("info123" ,"got response    "+response);

                if(flag==0){

                    if(response.length()==13){Toast.makeText(Proposal.this,"choose other date",Toast.LENGTH_SHORT).show(); setSpinner(null);}
                    else setSpinner(response);
                } //send date

                else{Toast.makeText(Proposal.this,"Submitted new proposal",Toast.LENGTH_SHORT).show();} //send praposal

            }
        },new Response.ErrorListener()  {

            @Override
            public void onErrorResponse(VolleyError error) {

                try{

                    Log.i("info123" ,Integer.toString(error.networkResponse.statusCode));
                    error.printStackTrace();}
                catch (Exception e)
                {
                    Toast.makeText(Proposal.this,"Check Network",Toast.LENGTH_SHORT).show();}

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
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(Proposal.this.getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public void afterSubmit()
    {
        String preview ="",preSub="";

        EditText pname= findViewById(R.id.praposal_name);
        String pnames = pname.getText().toString();

        EditText ptheme= findViewById(R.id.ptheme);
        String pthemes = ptheme.getText().toString();

        EditText pdesc= findViewById(R.id.pdescription);
        String pdescs = pdesc.getText().toString();

        EditText pcb= findViewById(R.id.creativebudget);
        String pcbs = pcb.getText().toString();


        EditText ppb= findViewById(R.id.publicitybdget);
        String ppbs = ppb.getText().toString();

        EditText pguest= findViewById(R.id.guestp);
        String pguests = pguest.getText().toString();

        int total = Integer.valueOf(pcbs) + Integer.valueOf(ppbs) + Integer.valueOf(pguests);

//        21sep
        EditText speaker_e= findViewById(R.id.speaker_p);
        String speaker_s = speaker_e.getText().toString();

        EditText venue_e= findViewById(R.id.venue_p);
        String  venue_s= venue_e.getText().toString();

        EditText csi_f = findViewById(R.id.fee_csi);
        String csi_s = csi_f.getText().toString();

        EditText ncsi_f= findViewById(R.id.fee_non_csi);
        String  ncsi_s= ncsi_f.getText().toString();

        EditText prize_e= findViewById(R.id.prize_p);
        String  prize_s= prize_e.getText().toString();
//        21sep

        EditText poth1= findViewById(R.id.other1B);
        String poths1 = poth1.getText().toString();
        EditText pothF1= findViewById(R.id.other1F);
        String pothFs1 = pothF1.getText().toString();

        EditText poth2= findViewById(R.id.other2B);
        String poths2 = poth2.getText().toString();
        EditText pothF2= findViewById(R.id.other2F);
        String pothFs2 = pothF2.getText().toString();

        EditText poth3= findViewById(R.id.other3B);
        String poths3 = poth3.getText().toString();
        EditText pothF3= findViewById(R.id.other3F);
        String pothFs3 = pothF3.getText().toString();

        Spinner agenda = findViewById(R.id.agendaSpinner);

        String agendas=null;
        if (agenda.getSelectedItem()!=null)
            agendas=agenda.getSelectedItem().toString();
        Log.i("info123","Passed here");

        if(pnames.length() <1){Toast.makeText(Proposal.this,"Enter Name ",Toast.LENGTH_SHORT).show();}
        else if(pthemes.length() <1){Toast.makeText(Proposal.this,"Enter Theme",Toast.LENGTH_SHORT).show();}
        else if(edate.length() <1){Toast.makeText(Proposal.this,"Enter Event date",Toast.LENGTH_SHORT).show();}
        else if(speaker_s.length() <1){Toast.makeText(Proposal.this,"Enter Speaker's Detail",Toast.LENGTH_SHORT).show();}
        else if(venue_s.length() <1){Toast.makeText(Proposal.this,"Enter Venue",Toast.LENGTH_SHORT).show();}
        else if(csi_s.length() <1){Toast.makeText(Proposal.this,"Enter CSI Members Fee",Toast.LENGTH_SHORT).show();}
        else if(ncsi_s.length() <1){Toast.makeText(Proposal.this,"Enter Non-CSI Members Fee",Toast.LENGTH_SHORT).show();}
        else if(prize_s.length() <1){Toast.makeText(Proposal.this,"Enter Prize Money",Toast.LENGTH_SHORT).show();}
        else if(pdescs.length() <1){Toast.makeText(Proposal.this,"Enter Description",Toast.LENGTH_SHORT).show();}
        else if(pcbs.length() <1){Toast.makeText(Proposal.this,"Enter Creative Budget",Toast.LENGTH_SHORT).show();}
        else if(ppbs.length() <1){Toast.makeText(Proposal.this,"Enter Publicity Budget ",Toast.LENGTH_SHORT).show();}
        else if(pguests.length() <1){Toast.makeText(Proposal.this,"Enter guests ",Toast.LENGTH_SHORT).show();}

        else if ((poths1.length() >0 && pothFs1.length() ==0) || (poths1.length() ==0 && pothFs1.length() >0)){Toast.makeText(Proposal.this,"Enter 1st other field",Toast.LENGTH_SHORT).show();}

        else if ((poths2.length() >0 && pothFs2.length() ==0)|| (poths2.length() ==0 && pothFs2.length() >0)){Toast.makeText(Proposal.this,"Enter 2nd other field",Toast.LENGTH_SHORT).show();}

        else if ((poths3.length() >0 && pothFs3.length() ==0)||(poths3.length() ==0 && pothFs3.length() >0)){Toast.makeText(Proposal.this,"Enter 3rd other field",Toast.LENGTH_SHORT).show();}

        else if(agendas==null || agendas=="SELECT"){Toast.makeText(Proposal.this,"Enter agenda",Toast.LENGTH_SHORT).show();}
        else{

            JSONObject jsub = new JSONObject();
            if (poths1.length() >0 && pothFs1.length() >0) {
                try {
                    jsub.put(pothFs1,poths1);
                    preSub+="\nOtherField1  "+pothFs1+" : "+poths1;
                    Log.i("i123",jsub.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (poths2.length() >0 && pothFs2.length() >0) {try {
                jsub.put(pothFs2,poths2);
                preSub+="\nOtherField2  "+pothFs2+" : "+poths2;
                Log.i("i123",jsub.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }}
            if (poths3.length() >0 && pothFs3.length() >0) {try {
                jsub.put(pothFs3,poths3);
                preSub+="\nOtherField3  "+pothFs3+" : "+poths3;
                Log.i("i123",jsub.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }}
            if(three_track == null){
                {Toast.makeText(Proposal.this,"3 Track cannot be empty",Toast.LENGTH_SHORT).show();}
            }
            JSONObject jsonobject = new JSONObject();
            try {
                jsonobject.put("proposals_event_name",pnames);
                preview+="Name : "+pnames;
//                21sep


                preview+="\nSpeaker : "+speaker_s;jsonobject.put("speaker",speaker_s);

                preview+="\nVenue : "+venue_s;jsonobject.put("proposals_venue",venue_s);

                preview+="\nRegistration Fee \n CSI Member : "+csi_s;jsonobject.put("proposals_reg_fee_csi",csi_s);

                preview+="\nNon-CSI member : "+ncsi_s;jsonobject.put("proposals_reg_fee_noncsi",ncsi_s);

                preview+="\nWorth Prize : "+prize_s;jsonobject.put("proposals_prize",prize_s);

                jsonobject.put("proposals_event_category",pthemes);
                preview+="\nTheme : "+pthemes;
                jsonobject.put("proposals_three_track",three_track);
                preview+="\nTheme : "+pthemes;
                jsonobject.put("proposals_event_date",edate);
                preview+="\nEvent date : "+edate;
                jsonobject.put("proposals_desc",pdescs);
                preview+="\nDescription : "+pdescs;
                jsonobject.put("agenda",agendas);
                preview+="\nAgenda : "+agendas;
//                jsonobject.put("date",date);
//                preview+="\nDate : "+date;
                jsonobject.put("cb",pcbs);
                preview+="\nCreative Budget : "+pcbs;
                jsonobject.put("pb",ppbs);
                preview+="\nPublicity Budget : "+ppbs;
                jsonobject.put("gb",pguests);
                preview+="\nGuests : "+pguests;
                Log.i("i123",jsonobject.toString());
                jsonobject.put("ob",jsub);
                preview+="\nOther Field : "+preSub;
                jsonobject.put("proposals_total_budget",total);
                preview+="\nTotal Budget : "+total;

//                jsonobject.put("pb",ppbs);
//                preview+="\nPublicity Budget : "+ppbs;
//                jsonobject.put("gb",pguests);
//                preview+="\nGuests : "+pguests;
//                Log.i("i123",jsonobject.toString());
//                jsonobject.put("ob",jsub);
//                preview+="\nOther Field : "+preSub;
                Log.i("i123",jsonobject.toString());

                customDialog(preview,jsonobject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }




    }


    public void customDialog(String message , final JSONObject jsonobject){
        final androidx.appcompat.app.AlertDialog.Builder builderSingle = new androidx.appcompat.app.AlertDialog.Builder(Proposal.this);
        //builderSingle.setIcon(R.drawable.ic_notification);
        builderSingle.setTitle("Preview");
        builderSingle.setMessage(message);

        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builderSingle.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        sendData(getApplicationContext().getResources().getString(R.string.server_url) + "/proposal/createproposal",jsonobject,1);
                        fetchAllTokens();
                        finish();

                    }
                });

        builderSingle.show();
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
