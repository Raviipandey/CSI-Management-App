package in.dbit.csiapp.mFragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Attendance extends Fragment {

    View rootView;
    JSONObject jsonObject = new JSONObject();
    String checkboxData=null;
    String date=null;
    String server_url; //Main Server URL
    //String server_url="http://192.168.43.84:8080/request";
    String rsn="";
    String miss="";
    String slots="";
    String UID="";

    String uname;

    private SharedPreferenceConfig preferenceConfig;

    public static Attendance newInstance() {
        return new Attendance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_request_attendance,container,false);
        server_url = rootView.getResources().getString(R.string.server_url) + "/attendance/request";
        getActivity().setTitle("Attendance");
        Bundle bundle = getArguments();
        UID = this.getArguments().getString("id");
        Log.i("tracking uid","in manager sending to profile "+UID);

        Button datePicker= rootView.findViewById(R.id.dateBtn);

        preferenceConfig = new SharedPreferenceConfig(getActivity().getApplicationContext());
        Intent intent = getActivity().getIntent();

        uname = intent.getStringExtra(MainActivity.EXTRA_UNAME);
        uname=preferenceConfig.readNameStatus();

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerFrag dpf = new datePickerFrag().newInstance();
                dpf.setCallBack(onDate);
                dpf.show(getFragmentManager().beginTransaction(), "DatePickerFragment");
            }
        });

        Button timepicker = rootView.findViewById(R.id.timeBtn);
        final TextView timeslot = rootView.findViewById(R.id.selectedtime);

        timepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout l = rootView.findViewById(R.id.checkBox);
                l.setVisibility(View.VISIBLE);
            }
        });
        EditText reason = rootView.findViewById(R.id.reason);
        reason.setMaxLines(5);
        reason.setVerticalScrollBarEnabled(true);
        reason.setMovementMethod(new ScrollingMovementMethod());
        EditText missed = rootView.findViewById(R.id.sub_miss);
        missed.setMaxLines(5);
        missed.setVerticalScrollBarEnabled(true);
        missed.setMovementMethod(new ScrollingMovementMethod());

        Button timeslotOK = rootView.findViewById(R.id.CheckBoxOK);
        timeslotOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout l = rootView.findViewById(R.id.checkBox);
                l.setVisibility(View.GONE);

                checkboxData="";

                CheckBox c1 = rootView.findViewById(R.id.checkBox1);
                CheckBox c2 = rootView.findViewById(R.id.checkBox2);
                CheckBox c3 = rootView.findViewById(R.id.checkBox3);
                CheckBox c4 = rootView.findViewById(R.id.checkBox4);
                CheckBox c5 = rootView.findViewById(R.id.checkBox5);
                CheckBox c6 = rootView.findViewById(R.id.checkBox6);
                CheckBox c7 = rootView.findViewById(R.id.checkBox7);

                if(c1.isChecked()){
                    checkboxData=checkboxData+"1";
                    slots=slots+"9.00AM-10.00AM ,";
                }else {
                    checkboxData=checkboxData+"0";
                }
                if(c2.isChecked()){
                    checkboxData=checkboxData+"1";
                    slots=slots+"10.00AM-11.00AM ,";
                }else {
                    checkboxData=checkboxData+"0";
                }
                if(c3.isChecked()){
                    checkboxData=checkboxData+"1";
                    slots=slots+"11.15AM-12.15PM ,";
                }else {
                    checkboxData=checkboxData+"0";
                }
                if(c4.isChecked()){
                    checkboxData=checkboxData+"1";
                    slots=slots+"12.15PM-1.15PM ,";
                }else {
                    checkboxData=checkboxData+"0";
                }
                if(c5.isChecked()){
                    checkboxData=checkboxData+"1";
                    slots=slots+"2.00PM-3.00PM ,";
                }else {
                    checkboxData=checkboxData+"0";
                }
                if(c6.isChecked()){
                    checkboxData=checkboxData+"1";
                    slots=slots+"3.00PM-4.00PM ,";
                }else {
                    checkboxData=checkboxData+"0";
                }
                if(c7.isChecked()){
                    checkboxData=checkboxData+"1";
                    slots=slots+"4.00PM-5.00PM ,";
                }else {
                    checkboxData=checkboxData+"0";
                }
                Log.i("info123", checkboxData);

            }
        });

        Button submit = rootView.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                EditText reason = rootView.findViewById(R.id.reason);
                rsn = reason.getText().toString();
                EditText missed = rootView.findViewById(R.id.sub_miss);
                miss = missed.getText().toString();


                if(date==null)  {
                    Toast.makeText(getActivity(),"Enter Date",Toast.LENGTH_SHORT).show(); }
                else if(checkboxData==null)  {Toast.makeText(getActivity(),"Enter Timeslots",Toast.LENGTH_SHORT).show();}
                else  if (miss.length()==0) {Toast.makeText(getActivity(),"Enter Subjects Missed",Toast.LENGTH_SHORT).show();}
                else  if (rsn.length()==0) {Toast.makeText(getActivity(),"Enter Reason",Toast.LENGTH_SHORT).show();}

                else  customDialog("Date:  "+date+"\n"+"Slots: "+slots+"\n"+"Subjects missed: "+miss+ "\n" + "Reason: "+rsn+"\n");



            }
        });

        return rootView;
    }

    private void fetchAllTokens() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getActivity().getResources().getString(R.string.server_url)+"/proposal/getcvctoken", new Response.Listener<String>() {
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
                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Session token is invalid or expired, log the user out
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(getActivity(), MainActivity.class); // Assuming LoginActivity is your login activity
                    startActivity(loginIntent);
                    getActivity().finish(); // Correctly calling finish() on the activity instance
                    Toast.makeText(getActivity(), "Session expired, please log in again", Toast.LENGTH_LONG).show();
                } else {
                    // Handle other errors
                    Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
                }
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



    // Method to send notification after successful proposal submission
    private void sendNotification(String fcmtoken) {
        // Construct the notification payload
        JSONObject notification = new JSONObject();
        try {
            notification.put("to", fcmtoken); // Using the FCM token obtained earlier
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", "New request for attendance");

            notificationBody.put("body", uname + " has missed some lectures");
            notification.put("notification", notificationBody);

            // Add intent to open TechnicalForm activity when notification is clicked
            JSONObject data = new JSONObject();
            data.put("click_action", ".Technical_form"); // Adjust with your TechnicalForm activity class name
            notification.put("data", data);
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

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public String toString() {
        return "Attendance";
    }

    DatePickerDialog.OnDateSetListener onDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {


            date= String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                    + "-" + String.valueOf(dayOfMonth);

            TextView outputDate = rootView.findViewById(R.id.date);
            outputDate.setText(date);
            Log.i("info123",date);
        }
    };

    public  int  setJason()
    {
        int flag=1;

        if(date==null)  {Toast.makeText(getActivity(),"Enter Date",Toast.LENGTH_SHORT).show(); return  2;}
        else if(checkboxData==null)  {Toast.makeText(getActivity(),"Enter Timeslots",Toast.LENGTH_SHORT).show(); return 2;}
        else  if (miss.length()==0) {Toast.makeText(getActivity(),"Enter Subject Missed",Toast.LENGTH_SHORT).show(); return 2;}
        else  if (rsn.length()==0) {Toast.makeText(getActivity(),"Enter Reason",Toast.LENGTH_SHORT).show(); return 2;}

        try {
            jsonObject.put("id",UID); //value from bundle
            jsonObject.put("date",date);

            Log.i("info123", String.valueOf(jsonObject));

            Log.i("info123", checkboxData);

            for(int i=0;i<7;i++)
            {
                String value="";
                value=value+checkboxData.charAt(i);
                jsonObject.put("s"+(i+1)+"",value);

                Log.i("info123", String.valueOf(jsonObject));
            }
            jsonObject.put("missed", miss);
            Log.i("info123", String.valueOf(jsonObject));

            jsonObject.put("reason",rsn);
            Log.i("info123", String.valueOf(jsonObject));

        } catch (JSONException e) {
            flag=0;
            e.printStackTrace();
        }
        return flag;
    }

    public void sendrequest(){

        final String requestBody = jsonObject.toString();
        Log.i("info123", requestBody);

        StringRequest stringRequest =new StringRequest(Request.Method.POST,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.i("info123" ,"got response    "+response);
                Toast.makeText(getActivity(),"Submitted ",Toast.LENGTH_SHORT).show();
                //on ok response take back to respective page
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();

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
                    Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(getActivity(), "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(loginIntent);
                    getActivity().finish();
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
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(stringRequest);

    }

    public void customDialog(String message){
        final androidx.appcompat.app.AlertDialog.Builder builderSingle = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
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
                        if(setJason()==1)
                        {
                            sendrequest();
                            fetchAllTokens();
                        }

                        Log.i("info123", String.valueOf(jsonObject));
                    }
                });

        builderSingle.show();
    }
}