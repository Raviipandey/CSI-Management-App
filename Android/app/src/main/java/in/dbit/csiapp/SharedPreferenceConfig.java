package in.dbit.csiapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import in.dbit.csiapp.R;

public class SharedPreferenceConfig {

    private SharedPreferences sharedPreferences;
    private Context context;
    EditText userid, password ;



    public SharedPreferenceConfig(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.login_preference), Context.MODE_PRIVATE);

    }

    public void writeLoginStatus(boolean status, String mobno, String pwd, String userid , String role, String UserName, String ProfileUrl, String Fcmtoken, String sessionToken){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userid",mobno);
        editor.putString("password",pwd);
        editor.putString("userid2" , userid);
        editor.putString("role",role);//update
        editor.putString("userName",UserName);
        editor.putString("profileURL",ProfileUrl);
        editor.putString("fcmtoken" , Fcmtoken);
        editor.putString("newSessionToken",sessionToken);
        editor.apply();
        //editor.putBoolean(context.getResources().getString(R.string.login_status_preference), status);
       // editor.commit();
    }
    public void logoutUser() {
        // Clear shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Or explicitly remove keys
        editor.apply();

        // Optionally, add a method to notify the backend about the logout
    }


    public String readLoginStatus(){
        //boolean status = false;

        String name = sharedPreferences.getString("userid2","");
        String password = sharedPreferences.getString("password","");

        if(name!="" && password!=""){
            return name;
        }
        else
            return "";
        //status = sharedPreferences.getBoolean(context.getResources().getString(R.string.login_status_preference), false);
        //return status;
    }

    public String fetchfcmtoken(){

        String fcmtoken = sharedPreferences.getString("fcmtoken" , "");
        return fcmtoken;
    }




    public String readRoleStatus(){
        //boolean status = false;

            return sharedPreferences.getString("role","");
        //status = sharedPreferences.getBoolean(context.getResources().getString(R.string.login_status_preference), false);
        //return status;
    }

    public String readNameStatus(){

        return sharedPreferences.getString("userName","");
    }

    public String readUrlStatus(){

        return sharedPreferences.getString("profileURL","");
    }


    // Method to fetch the session token
    public String readSessionToken() {
        return sharedPreferences.getString("newSessionToken", "No token found");
    }
}
