package in.dbit.csiapp.Prompts;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.FirebaseMessagingService;

import in.dbit.csiapp.R;

import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.mActivityManager.Forgetpassword;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener {




    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;
    private SharedPreferenceConfig preferenceConfig; //.....6/6/2019

    public static final String EXTRA_MOBNO = "com.example.csimanagementsystem.EXTRA_MOBNO";
    public static final String EXTRA_UNAME = "com.example.csimanagementsystem.EXTRA_UNAME";
    public static final String EXTRA_UROLE = "com.example.csimanagementsystem.EXTRA_UROLE";
    public static final String EXTRA_URL = "com.example.csimanagementsystem.EXTRA_URL";

    public static final String EXTRA_USERID = "com.example.csimanagementsystem.EXTRA_USERID";

    public static final String EXTRA_FCMTOKEN = "com.example.csimanagementsystem.EXTRA_FCMTOKEN";
    public static final String EXTRA_SESSIONTOKEN = "com.example.csimanagementsystem.EXTRA_SESSIONTOKEN";

    String server_url;
    String sessiontoken;
    //Main Server URL
    private SharedPreferences mpref; //asdfg
    private static final String pref_name="";


    String uid=" ",pstring=" " , mobno = "" , token;
    ImageView logo;
    Animation splash,fadein,fadeout;
    View lay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestNotificationPermission();
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/login";
//        Toast.makeText(this, "this is server_rl from string " + server_url , Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_main);
        preferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        animation();
        //.....6/6/2019

        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null && uri.getPath().equals("/login/newpassword")) {

                Toast.makeText(MainActivity.this , "Login with your new password" ,Toast.LENGTH_LONG).show();
                // This activity was started from the deep link
                // Perform actions accordingly
                // For example, navigate to specific fragment or perform required tasks
            }
        }


  

        if(preferenceConfig.readLoginStatus()!=""){
            Intent manager = new Intent(MainActivity.this, Manager.class);
            manager.putExtra(EXTRA_MOBNO, preferenceConfig.readLoginStatus());
            manager.putExtra(EXTRA_UROLE, preferenceConfig.readRoleStatus());
            manager.putExtra(EXTRA_UNAME,preferenceConfig.readNameStatus());
            manager.putExtra(EXTRA_USERID,preferenceConfig.readNameStatus());
            //Log.i("New Error", preferenceConfig.readUrlStatus());
            manager.putExtra(EXTRA_URL, preferenceConfig.readUrlStatus());

//            Intent manager = new Intent(this, Manager.class);
            startActivity(manager);
            finish();
        }


        sessiontoken= preferenceConfig.readSessionToken();




        //.....6/6/2019

        Button Login =(Button) findViewById(R.id.Login_button);
        final EditText mobileno =(EditText) findViewById(R.id.mobileno);



        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("abcdef" ,"Reached Listner" + server_url);

                mobno =mobileno.getText().toString();

                TextInputEditText pword =findViewById(R.id.password);
                pstring =pword.getText().toString();

                pstring = encryptPassword(pstring);


                //validation part starts
                //if(uid.length()==10 ) { // && pstring.length()>=7


                getfcmtoken();  //this method contains json part of the login page
                // }
                // else {

                //Log.i("" ,"Not satisfied condition");
                //Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                //This method is used to show pop-up on the screen if user gives wrong uid
                //}
                //validation part ends
            }
        });

        mpref=getSharedPreferences(pref_name,MODE_PRIVATE);
        String stored_usrid=mpref.getString("username","");
//        usrid.setText(stored_usrid);

        TextView resetpass = findViewById(R.id.resetPasswordTextView);
        resetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this , Forgetpassword.class));
            }
        });
//
    }



    // Method to request notification permission
    private void requestNotificationPermission() {
        // Check if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Check if the version of Android is Marshmallow or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Show an explanation to the user
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        // Display a dialog explaining why the permission is needed
                        new AlertDialog.Builder(this)
                                .setTitle("Notification Permission")
                                .setMessage("This app needs permission to send you notifications.")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Request the permission
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                                NOTIFICATION_PERMISSION_REQUEST);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                NOTIFICATION_PERMISSION_REQUEST);
                    }
                }
            } else {
                // Request the permission without explanation (for devices running versions < Marshmallow)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getfcmtoken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Token retrieval successful
                        token = task.getResult();
                        Log.i("My fcm token", token);
                        // Now you can proceed to make your network request with the token
                        insertSrv();
                    } else {
                        // Token retrieval failed
                        Log.e("FCM Token Error", "Failed to retrieve FCM token: " + task.getException());
                    }
                });
    }

    private void insertSrv()
    {
        // Before using the session token
        String currentSessionToken = preferenceConfig.readSessionToken();
        Log.i("CurrentSessionToken", "Session Token before use: " + currentSessionToken);

        //creating jsonobject starts
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobno", mobno);
            jsonObject.put("password", pstring);

            jsonObject.put("fcmtoken" , token);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        //creating jsonobject ends

        //checking data inserted into json object
        final String requestBody = jsonObject.toString();
        Log.i("volleyABC main ", requestBody);

        //getting response from server starts
        StringRequest stringRequest = new StringRequest(Request.Method.POST,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.i("volleyABC" ,"got response    "+response);
                Toast.makeText(MainActivity.this, "Logged IN", Toast.LENGTH_SHORT).show();

                Intent manager = new Intent(MainActivity.this, Manager.class);
                String USERID="" , UROLE="", USERNAME="", ProfileURL="" , FCMTOKEN = "", SESSIONTOKEN = "";

                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    // Log.i("tracking uid","main Activity "+UID);
                    Log.i("Profile response" , String.valueOf(jsonObject1));
                    USERID = jsonObject1.getString("userid");
                    USERNAME = jsonObject1.getString("name");
                    UROLE = jsonObject1.getString("role");
                    ProfileURL = jsonObject1.getString("dp");
                    FCMTOKEN = jsonObject1.getString("fcmtoken");
                    SESSIONTOKEN = jsonObject1.getString("newSessionToken");


                    //sharedPreference.... 6/6/2019

                    Log.i("Sharedpreferences", USERID + USERNAME + UROLE + ProfileURL + FCMTOKEN + SESSIONTOKEN);
                    preferenceConfig.writeLoginStatus(true,mobno,pstring,USERID ,UROLE,USERNAME,ProfileURL,FCMTOKEN, SESSIONTOKEN);
                    //sharedPreference.... 6/6/2019
                    String storedToken = preferenceConfig.readSessionToken();
                    Log.i("StoredSessionToken", "Session Token right after storage: " + storedToken);


                    SharedPreferences.Editor editor=mpref.edit();
                    editor.putString("username",mobno);
                    editor.putString("password",pstring);
                    editor.putString("urole",UROLE);
                    editor.putString("userid" , USERID);
                    editor.putString("newSessionToken", SESSIONTOKEN);
                    editor.apply();
                    finish();

                    //Send data to Manager.java starts
                    // Call manager.java file i.e. Activity with navigation drawer activity
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                //startActivity(manager);
                manager.putExtra(EXTRA_MOBNO, mobno);
                manager.putExtra(EXTRA_USERID , USERID);
                manager.putExtra(EXTRA_UNAME, USERNAME);
                manager.putExtra(EXTRA_UROLE, UROLE);
                manager.putExtra(EXTRA_URL, ProfileURL);
                manager.putExtra(EXTRA_FCMTOKEN, FCMTOKEN);
                manager.putExtra(EXTRA_SESSIONTOKEN, SESSIONTOKEN);
                //Send data to Manager.java ends
                startActivity(manager);
                //
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
                    Toast.makeText(MainActivity.this, "Session expired", Toast.LENGTH_LONG).show();
                } else if ("Another device has logged in".equals(errorMessage)) {
                    Toast.makeText(MainActivity.this, "Another device has logged in", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    // Handle logout if session is expired or taken over
                    preferenceConfig.writeLoginStatus(false, "", "", "", "", "", "", "", "");
                    Intent loginIntent = new Intent(MainActivity.this, MainActivity.class);
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

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest); // get response from server
    }

    private String encryptPassword(String password) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                while (hex.length() < 2) {
                    hex = "0" + hex;
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //Afif's Work
    public void animation(){
        logo = findViewById(R.id.csilogo);
        splash = AnimationUtils.loadAnimation(this,R.anim.splashlogo);
        fadein = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        splash.setAnimationListener(this);
        lay = findViewById(R.id.main);
        logo.setAnimation(splash);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        lay.setAnimation(fadein);
        lay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }



}



