package in.dbit.csiapp.mFragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import in.dbit.csiapp.mReqAdapter.RequestListAdapter;
import in.dbit.csiapp.mReqAdapter.RequestListItem;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AttendancePR extends Fragment {

    private View rootView;
    private RecyclerView mRecyclerView;
    private RequestListAdapter mRequestListAdapter;
    private ArrayList<RequestListItem> mRequestList;
    private RequestQueue mRequestQueue;
    SwipeRefreshLayout swipeRefreshLayout;

    private String uname , fcmtoken , name;

    private SharedPreferenceConfig preferenceConfig;

    StringBuffer sb = null;

    public static AttendancePR newInstance() {
        return new AttendancePR();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Attendance");
        rootView = inflater.inflate(R.layout.activity_attendance_pr,null);

        // Check if the activity is launched from a notification click action
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals("AttendancePR_ACTIVITY")) {
            // Activity is launched from a notification click action
            // Add your handling code here
            // For example, you can display a Toast message indicating that the activity is launched from a notification
            Toast.makeText(getActivity(), "Activity launched from notification click action", Toast.LENGTH_SHORT).show();
        } else {
            // Activity is not launched from a notification click action
            // Proceed with your regular code
            swipe();

            mRecyclerView = rootView.findViewById(R.id.recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            mRequestList = new ArrayList<>();

            mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

            preferenceConfig = new SharedPreferenceConfig(getActivity().getApplicationContext());

            uname = preferenceConfig.readNameStatus();

            Log.i("AttPR","Started");
            parseJSON();  //Get list of requests

            Button confirm = (Button) rootView.findViewById(R.id.confirm_requests);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parsejson2();  //Method to Confirm Requests
                    parsejson3();  //Method to Reject Requests
                    Toast.makeText(getActivity(), "Attendance Record Updated", Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
                }
            });
        }

        return rootView;
    }


//    private void fetchAllTokens() {
//        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, getActivity().getResources().getString(R.string.server_url)+"/proposal/getalltoken", new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    JSONArray tokensArray = new JSONArray(response);
//                    Log.i("FCM SERVER" , String.valueOf(tokensArray));
//                    for (int i = 0; i < tokensArray.length(); i++) {
//                        String fcmToken = tokensArray.getString(i); // Parse each token as a string
//                        // Call the method to send notification for each FCM token
//                        sendNotification(fcmToken);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//            }
//        });
//        requestQueue.add(stringRequest);
//    }



    // Method to send notification after successful proposal submission


    public String toString() {
        return "Attendance PR";
    }

    void swipe() {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresher_P);
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

    private void parseJSON() {
        mRequestList.clear();
        //String url = "http://192.168.43.84:8080/requestlist";
        String url = rootView.getResources().getString(R.string.server_url) + "/attendance/requestlist";   //Main Server URL
        Log.i("AttPR","ParseJSON");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i("AttPR","got response"+response);
                    swipeRefreshLayout.setRefreshing(false);
                    JSONArray jsonArray = new JSONArray(response);

                    if(jsonArray.length() == 0) {
                        TextView no_att = (TextView) rootView.findViewById(R.id.no_attendance);
                        no_att.setVisibility(View.VISIBLE);

                        mRecyclerView.setVisibility(View.GONE);

                        Button confirm = (Button) rootView.findViewById(R.id.confirm_requests);
                        confirm.setVisibility(View.GONE);
                    }
                    else {
                        TextView no_att = (TextView) rootView.findViewById(R.id.no_attendance);
                        no_att.setVisibility(View.GONE);

                        mRecyclerView.setVisibility(View.VISIBLE);

                        Button confirm = (Button) rootView.findViewById(R.id.confirm_requests);
                        confirm.setVisibility(View.VISIBLE);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject request = jsonArray.getJSONObject(i);
                            Log.i("AttPR", "got response" + request);

                            String requestId = request.getString("ad_id");
                            Log.i("AttPR", "got response" + requestId);
                            //String requestId = request.getString("id");
                            name = request.getString("core_en_fname");
                            Log.i("AttPR", "got response" + name);
                            String date = request.getString("core_date");
                            String date1 = date.substring(8, 10) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4);
                            Log.i("AttPR", "got response" + date);
                            int lec1 = request.getInt("s1");
                            int lec2 = request.getInt("s2");
                            int lec3 = request.getInt("s3");
                            int lec4 = request.getInt("s4");
                            int lec5 = request.getInt("s5");
                            int lec6 = request.getInt("s6");
                            int lec7 = request.getInt("s7");
                            Log.i("AttPR", "got response" + lec1 + lec2 + lec3 + lec4 + lec5 + lec6 + lec7);
                            String missed = request.getString("core_lecsmissed_sub");
                            Log.i("AttPR", "got response" + missed);
                            String reason = request.getString("core_reason");
                            Log.i("AttPR", "got response" + reason);

                            fcmtoken = request.getString("fcm_token");
                            Log.i("Attendance fcm" , fcmtoken);

                            //We can't display the lecture slots in the s1 s2 s3... format to PR Head
                            //So we are using timeslot variable to send time slots in proper manner
                            String timeSlot = "";
                            if (lec1 == 1)
                                timeSlot = timeSlot + "09:00AM - 10:00AM, ";
                            if (lec2 == 1)
                                timeSlot = timeSlot + "10:00AM - 11:00AM, ";
                            if (lec3 == 1)
                                timeSlot = timeSlot + "11:15AM - 12:15PM, ";
                            if (lec4 == 1)
                                timeSlot = timeSlot + "12:15PM - 01:15PM, ";
                            if (lec5 == 1)
                                timeSlot = timeSlot + "02:00PM - 03:00PM, ";
                            if (lec6 == 1)
                                timeSlot = timeSlot + "03:00PM - 04:00PM, ";
                            if (lec7 == 1)
                                timeSlot = timeSlot + "04:00PM - 05:00PM ";

                            mRequestList.add(new RequestListItem(requestId, name, date1, timeSlot, missed, reason));
                        }

                        mRequestListAdapter = new RequestListAdapter(getActivity(), mRequestList);
                        mRecyclerView.setAdapter(mRequestListAdapter);
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
        }){ @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            String sessionToken = preferenceConfig.readSessionToken();
            Log.d("RequestHeaders", "Sending token: " + sessionToken); // Add this line
            headers.put("Authorization", "Bearer " + sessionToken);
            return headers;
        }};

        if (mRequestListAdapter == null) {
            mRequestListAdapter = new RequestListAdapter(getActivity(), mRequestList);
            mRecyclerView.setAdapter(mRequestListAdapter);
        } else {
            // Notify the adapter about the data change
            mRequestListAdapter.notifyDataSetChanged();
        }
        mRequestQueue.add(stringRequest);

    }


    private void sendAcceptedNotification(String fcmtoken) {
        // Construct the notification payload
        JSONObject notification = new JSONObject();
        try {
            notification.put("to", fcmtoken); // Using the FCM token obtained earlier
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", "Attendance accepted");

            notificationBody.put("body", "Your attendance request has been accepted");
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

    private void sendRejectedNotification(String fcmtoken) {
        // Construct the notification payload
        JSONObject notification = new JSONObject();
        try {
            notification.put("to", fcmtoken); // Using the FCM token obtained earlier
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", "Attendance rejected");

            notificationBody.put("body", "Your attendance request has been rejected");
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



    public void parsejson2() {
        String server_url = rootView.getResources().getString(R.string.server_url) + "/attendance/finallist";   //Main Server URL

        sb = new StringBuffer();
        ArrayList finalConReq = new ArrayList();

        String confReq = "";
        for (RequestListItem rli : mRequestListAdapter.mConfirmRequestList) {
            sb.append(rli.getRequestID());
            finalConReq.add(rli.getRequestID());
            Log.i("request list" , String.valueOf(rli));
        }

        Gson gson = new Gson();
        confReq = gson.toJson(finalConReq);

        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<finalConReq.size();i++)
        {
            jsonArray.put(finalConReq.get(i));
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("accepted", jsonArray);


        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        //Note: If u want to send data to the server in JSONArray Format. Don't convert ArrayList Directly to JSON
        //Instead add one by one element to JSONARRAY and then send that JSONArray as JSONObject

        final String requestBody = jsonArray.toString();
        Log.i("volleyABC123",requestBody);
        final String requestBody2 = jsonObject.toString();
        Log.i("volleyABC456",requestBody2);

        //getting response from server starts

        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("volleyABC", "got response    " + response);
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
                    return requestBody2.getBytes("utf-8");
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

        mRequestQueue.add(stringRequest);

        //Below if else condition is just used to display the data we are sending to server
//        if (mRequestListAdapter.mConfirmRequestList.size()>0)
//        {
//            Toast.makeText(getActivity(),sb.toString(),Toast.LENGTH_SHORT).show();
//            Toast.makeText(getActivity(),confReq, Toast.LENGTH_SHORT).show();
//            Log.i("jsonArray",confReq);
//        }
//        else
//        {
//            Toast.makeText(getActivity(),"No Request has been selected",Toast.LENGTH_SHORT).show();
//        }
    }

    public void parsejson3() {
        String server_url = rootView.getResources().getString(R.string.server_url) + "/attendance/reject";

        sb = new StringBuffer();
        ArrayList finalRejReq = new ArrayList();

        String rejReq = "";
        for (RequestListItem rli : mRequestListAdapter.mRejectRequestList) {
            sb.append(rli.getRequestID());
            finalRejReq.add(rli.getRequestID());

        }
        Gson gson = new Gson();
        rejReq = gson.toJson(finalRejReq);

        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<finalRejReq.size();i++)
        {
            jsonArray.put(finalRejReq.get(i));
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("rejected", jsonArray);


        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        //Note: If u want to send data to the server in JSONArray Format. Don't convert ArrayList Directly to JSON
        //Instead add one by one element to JSONARRAY and then send that JSONArray as JSONObject

        final String requestBody = jsonArray.toString();
        Log.i("volleyABC123",requestBody);
        final String requestBody2 = jsonObject.toString();
        Log.i("volleyABC456",requestBody2);

        //getting response from server starts

        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("volleyABC", "got response    " + response);
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
                    return requestBody2.getBytes("utf-8");
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

        mRequestQueue.add(stringRequest);

        //Below if else condition is just used to display the data we are sending to server
        if (mRequestListAdapter.mRejectRequestList.size()>0)
        {
//            Toast.makeText(getActivity(),sb.toString(),Toast.LENGTH_SHORT).show();
//            Toast.makeText(getActivity(),rejReq, Toast.LENGTH_SHORT).show();
            Log.i("jsonArray",rejReq);
        }
        else
        {
            Log.i("volleyABC", "No request has been selected");
//            Toast.makeText(getActivity(),"No Request has been selected",Toast.LENGTH_SHORT).show();
        }
    }
}