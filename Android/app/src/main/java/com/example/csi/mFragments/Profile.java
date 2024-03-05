package com.example.csi.mFragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.csi.Prompts.ProfileEdit;
import com.example.csi.R;
import com.joooonho.SelectableRoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Profile extends Fragment {

    String server_url;   //Main Server URL
    //String server_url="http://192.168.43.84:8080/profile";
    //string to store position as we are not showing it in any textview
    String position_s=" ", UID, UProfile;
    View rootView;
    ImageView imageButton;
    private String profileImageUrl;

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        final SwipeRefreshLayout swipeRefreshLayout1 = rootView.findViewById(R.id.refresher1);
        swipeRefreshLayout1.setRefreshing(true);
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout1.setRefreshing(false);

                int min = 65;
                int max = 95;

                get_data();
            }
        },1000);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        rootView = inflater.inflate(R.layout.activity_profile,container,false);
//        Log.i("id print hogi", UID);
        UID = this.getArguments().getString("id");
        server_url = rootView.getResources().getString(R.string.server_url) + "/profile/?id="+UID;
        Log.i("naya",server_url);
        getActivity().setTitle("My Profile");
        Bundle bundle = getArguments();


        UProfile = this.getArguments().getString("core_profilepic_url");
        swipe();
        imageButton = rootView.findViewById(R.id.profile_photo);
        loadImageUrl(UProfile);

        Button edit_button = rootView.findViewById(R.id.edit_button);

        //decalring varriables
        TextView id = rootView.findViewById(R.id.id);
        TextView role = rootView.findViewById(R.id.role);
        //id will get from called intent
        // id.setText(getIntent().getStringExtra("id from respective intent"));
        id.setText(UID);

        get_data();//fetch data from server



        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("volleyABC", "onClick: reached here");

                TextView id = rootView.findViewById(R.id.id);
                TextView role = rootView.findViewById(R.id.role);
                TextView name = rootView.findViewById(R.id.profile_name);
                TextView email = rootView.findViewById(R.id.email);
                TextView phn = rootView.findViewById(R.id.phn);
                TextView yr = rootView.findViewById(R.id.year);
                TextView roln = rootView.findViewById(R.id.rollNo);
                TextView batch = rootView.findViewById(R.id.batch);
                TextView  branch= rootView.findViewById(R.id.branch);
                SelectableRoundedImageView imageView = rootView.findViewById(R.id.profile_photo);

                Intent edit_profile =new Intent(getActivity(), ProfileEdit.class);

                //passing data to edit intent so only required data will be changed else everything will remain same
                edit_profile.putExtra("core_id",id.getText().toString());
                edit_profile.putExtra("role_name",role.getText().toString());
                edit_profile.putExtra("core_en_fname",name.getText().toString());
                edit_profile.putExtra("core_role_id",position_s);
                edit_profile.putExtra("core_email",email.getText().toString());
                edit_profile.putExtra("core_mobileno",phn.getText().toString());
                edit_profile.putExtra("core_class",yr.getText().toString());
                edit_profile.putExtra("core_branch",branch.getText().toString());
                edit_profile.putExtra("core_rollno",roln.getText().toString());
                edit_profile.putExtra("core_profilepic_url", profileImageUrl);
//                edit_profile.putExtra("batch",batch.getText().toString());
                startActivity(edit_profile);
                //finish();
            }
        });

        return rootView;
    }

    private void loadImageUrl(String url) {
        Picasso.get().load(url)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageButton, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Handle success
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle error
                    }
                });
    }

    void get_data() {
        JSONObject jsonObject = new JSONObject();
        TextView  id= rootView.findViewById(R.id.id);
        String id_s =  id.getText().toString();

        try {
            jsonObject.put("id",id_s); //actual value shud be id_s
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonObject.toString();
        Log.i("volleyABC ", requestBody);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i("volleyABC", response);
                //Toast.makeText(MainActivity.this,response, Toast.LENGTH_SHORT).show();
                set_data(response);//set data in textiles
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    //String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.i("volleyABC" ,Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(getActivity(),"Invalid Username or Password",Toast.LENGTH_SHORT).show();//it will not occur as authenticating at start
                    error.printStackTrace();}
                catch (Exception e)
                {
                    Log.i("volleyABC" ,"exception");
                    Toast.makeText(getActivity(),"Check Network",Toast.LENGTH_SHORT).show();} //occur if connection not get estabilished
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
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    void set_data(String data) {
        Log.i("volleyABC", "set_data called"+data);
        TextView role = rootView.findViewById(R.id.role);
        TextView id = rootView.findViewById(R.id.id);
        TextView name = rootView.findViewById(R.id.profile_name);
        TextView email = rootView.findViewById(R.id.email);
        TextView phn = rootView.findViewById(R.id.phn);
        TextView yr = rootView.findViewById(R.id.year);
        TextView roln = rootView.findViewById(R.id.rollNo);
        TextView batch = rootView.findViewById(R.id.batch);
        TextView  branch = rootView.findViewById(R.id.branch);
        TextView membershipLeft = rootView.findViewById(R.id.membershipLeft);
        SelectableRoundedImageView imageView = rootView.findViewById(R.id.profile_photo);

        JSONObject fetchedData ;
        try {
            fetchedData= new JSONObject(data);
            role.setText(fetchedData.getString("role_name"));
            id.setText(fetchedData.getString("core_id"));
            name.setText(fetchedData.getString("core_en_fname"));
            position_s= fetchedData.getString("core_role_id");
            Log.i("volleyABC", "position value in main"+position_s);
            email.setText(fetchedData.getString("core_email"));
            phn.setText(fetchedData.getString("core_mobileno"));
            yr.setText(fetchedData.getString("core_class"));
            branch.setText(fetchedData.getString("core_branch"));
            roln.setText(fetchedData.getString("core_rollno"));
//            batch.setText(fetchedData.getString("batch"));
            String membershipLeftValue = fetchedData.getString("membership_left");
            String textToShow = membershipLeftValue + " years";
            membershipLeft.setText(textToShow);

            profileImageUrl = fetchedData.getString("core_profilepic_url");
            Picasso.get().load(fetchedData.getString("core_profilepic_url"))
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .into(imageView);

            Log.i("volleyABC", "set_data: created json object all");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void swipe() {
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresher1);
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

                        get_data();
                    }
                },1000);
            }
        });
    }
}
