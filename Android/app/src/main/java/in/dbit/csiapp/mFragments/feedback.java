package in.dbit.csiapp.mFragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebSettings;
import android.webkit.WebView;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import in.dbit.csiapp.R;
//import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.mReqAdapter.RequestListItem;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class feedback extends Fragment {
    View rootView;
    String server_url;
    //    private SharedPreferenceConfig preferenceConfig;
//    private SharedPreferences mpref;
    private String usrid, usrname = "";
    EditText feedback_text;
    TextView name_text_v;

    //Button save_feedback;
    private Button feedbackButton;
    private WebView feedbackWebView;

    String feedback = null;
    JSONObject jsonObject = new JSONObject();
    private RequestQueue mRequestQueue;
    private ArrayList<RequestListItem> mRequestList;


    public static feedback newInstance() {
        return new feedback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_feedback, container, false);
        server_url = rootView.getResources().getString(R.string.server_url) + "/feedback";
        getActivity().setTitle("FAQs");


//        Bundle bundle = getArguments();
        usrid = this.getArguments().getString("id");
        usrname = this.getArguments().getString("name");
        Log.i("volleyABC", "to feedback section" + usrid + usrname);
//        Toast.makeText(getActivity(), "Feedback section" + usrid + usrname, Toast.LENGTH_SHORT).show();

        //  feedback_text = rootView.findViewById(R.id.text_feedback);
        name_text_v = rootView.findViewById(R.id.name_feedback);
        feedbackButton = rootView.findViewById(R.id.feedback_button);
        feedbackWebView = rootView.findViewById(R.id.feedback_webview);
        //  save_feedback = rootView.findViewById(R.id.feedback_save);
        mRequestList = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());


        name_text_v.setText(usrname);
        // Set up the onClickListener for the feedback button
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedbackForm();
            }
        });
        return rootView;
    }
    private boolean feedbackFormOpened = false;
    private void showFeedbackForm() {

        if (!feedbackFormOpened) {
            // Configure WebView settings
            WebSettings webSettings = feedbackWebView.getSettings();
            webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed

            // Google Form URL
            String googleFormUrl = "https://forms.gle/sa2LGBrWCLmorzCW6";

            // Load the Google Forms page into the WebView
            feedbackWebView.loadUrl(googleFormUrl);

            feedbackWebView.setVisibility(View.VISIBLE);
            feedbackButton.setVisibility(View.VISIBLE);

            feedbackFormOpened = true; // Set the flag to true to prevent multiple clicks
        }else {
            // If the form is already opened, reset the WebView and show the button
            feedbackWebView.setVisibility(View.GONE);
            feedbackButton.setVisibility(View.VISIBLE);
            feedbackFormOpened = false; // Reset the flag to allow clicking again
        }
    }

//        save_feedback.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                feedback = feedback_text.getText().toString();
//
//                //below two lines closes keyborad input on click of save button
//                InputMethodManager inputManager = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getActivity().getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//
////                Toast.makeText(getActivity(), "Feedback section save clicked" + feedback, Toast.LENGTH_SHORT).show();
//                setJason();
//                send_data();
//                feedback_text.setText("");
//
//            }
//        });

    //name_text_v.setText(usrname);


//        SharedPreferenceConfig preferenceConfig;
//        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
//        mpref=getSharedPreferences(pref_name,MODE_PRIVATE);
//        String stored_usrid=mpref.getString("username","");
//


    public void setJason() {

        if (feedback == null) {
            Toast.makeText(getActivity(), "Enter Date", Toast.LENGTH_SHORT).show();
        } else {

            try {
                jsonObject.put("id", usrid); //value from bundle
                jsonObject.put("name", usrname);
                jsonObject.put("feedback", feedback);

                Log.i("info123json obj", String.valueOf(jsonObject));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

//    public  void send_data(){
//        final String requestBody = jsonObject.toString();
//        Log.i("volleyABC123",requestBody);
//
//        //getting response from server starts
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.i("volleyABC", "got response    " + response);
//                Toast.makeText(getActivity(), "Thank you for feedback", Toast.LENGTH_SHORT).show();
//
//                //this will close feedback and return to main page
//                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                try {
//                    String statusCode = String.valueOf(error.networkResponse.statusCode);
//                    Log.i("volleyABC", Integer.toString(error.networkResponse.statusCode));
//                    Toast.makeText(getActivity(), "Error:-" + statusCode, Toast.LENGTH_SHORT).show();
//                    error.printStackTrace();
//                } catch(Exception e) {
//                    Toast.makeText(getActivity(), "Check Network",Toast.LENGTH_SHORT).show();
//                }
//            }
//        }){
//            @Override
//            public byte[] getBody() throws AuthFailureError {
//                try {
//                    return requestBody.getBytes("utf-8");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//        };
//
//        mRequestQueue.add(stringRequest);
//    }
}