package in.dbit.csiapp.Prompts;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.joooonho.SelectableRoundedImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ProfileEdit extends AppCompatActivity {

    String server_url;     //Main Server URL
    //String server_url="http://192.168.43.84:8080/profile/edit";
    String position_s, UProfile;
    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView imageButton;


    public void onEditProfilePhotoClick(View view) {
        // Open the gallery for image selection
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

//    private void uploadImageToServer(String filePath, String fileName) {
//
//        String url = getApplicationContext().getResources().getString(R.string.server_url) + "/profile/profileupload";
//        OkHttpClient client = new OkHttpClient();
//
//        File file = new File(filePath);
//        MediaType mediaType = MediaType.parse("image/jpeg"); // Adjust this according to your image type
//
//        // Create RequestBody instance from file
//        RequestBody fileBody = RequestBody.create(mediaType, file);
//
//        // MultipartBody Builder
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("profilePic", fileName, fileBody); // Ensure this field name matches with the server's expected field name
//
//        // Adding additional data if needed
//        // For example, if you need to send the user ID
//        builder.addFormDataPart("userId", getIntent().getStringExtra("core_role_id"));
//
//        RequestBody requestBody = builder.build();
//
//        // Build your request
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
//        // Making the request
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
//                runOnUiThread(() -> {
//                    try {
//                        if (response.isSuccessful() && response.body() != null) {
//                            String responseData = response.body().string();
//                            // Handle the server response
//                            Toast.makeText(ProfileEdit.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(ProfileEdit.this, "Server responded with error: " + response.code(), Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Toast.makeText(ProfileEdit.this, "Error handling server response", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() -> Toast.makeText(ProfileEdit.this, "Failed to Upload Image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//            }
//        });
//    }

    private class UploadImageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String filePath = params[0];
            String fileName = params[1];

            String url = getApplicationContext().getResources().getString(R.string.server_url) + "/profile/profileupload";
            OkHttpClient client = new OkHttpClient();

            File file = new File(filePath);
            MediaType mediaType = MediaType.parse("image/jpeg");

            RequestBody fileBody = RequestBody.create(mediaType, file);

            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("profilePic", fileName, fileBody);

            builder.addFormDataPart("userId", getIntent().getStringExtra("core_id"));

            RequestBody requestBody = builder.build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    // Handle the server response
                    Log.i("AsyncTask", "Image Uploaded Successfully");
                } else {
                    Log.e("AsyncTask", "Server responded with error: " + response.code());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("AsyncTask", "Error handling server response: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // You can perform UI updates here if needed
        }
    }
    private void uploadImageToServer(String filePath, String fileName) {
        new UploadImageTask().execute(filePath, fileName);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            String filePath = getRealPathFromURI(selectedImageUri);
            String fileName = getFileNameFromUri(selectedImageUri);
            uploadImageToServer(filePath, fileName);
        }
    }

    // Method to get the real path from URI
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/profile/edit";
        setContentView(R.layout.activity_profile_edit);
        Log.i("url lelo", server_url);

        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);





        //declaring variables
        Button save_button = findViewById(R.id.save_button);
        final TextView id = findViewById(R.id.id_E);
        final TextView name = findViewById(R.id.profile_name_E);
        final EditText email = findViewById(R.id.email_E);
        final EditText phn = findViewById(R.id.phn_E);
        final RadioGroup yr = findViewById(R.id.year_E);
        final RadioGroup branch = findViewById(R.id.branch_E);
        final EditText rol = findViewById(R.id.rollNo_E);
        //final RadioGroup batch = findViewById(R.id.batch_E);
        SelectableRoundedImageView imageView = findViewById(R.id.profile_photo_E);


        //getting data from profile

        UProfile = getIntent().getStringExtra("core_profilepic_url");

        id.setText(getIntent().getStringExtra("core_id"));
        name.setText(getIntent().getStringExtra("core_en_fname"));
        email.setText(getIntent().getStringExtra("core_email"));
        phn.setText(getIntent().getStringExtra("core_mobileno"));
        rol.setText(getIntent().getStringExtra("core_rollno"));
        position_s = getIntent().getStringExtra("core_role_id");
        String Year = getIntent().getStringExtra("core_class");
        String Branch = getIntent().getStringExtra("core_branch");
        String profileImageUrl = getIntent().getStringExtra("core_profilepic_url");
        Picasso.get().load(profileImageUrl)
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(imageView);
        switch (Year) {
            case "FE":
                RadioButton FE = findViewById(R.id.radio_FE);
                FE.setChecked(true);
                break;
            case "SE":
                RadioButton SE = findViewById(R.id.radio_SE);
                SE.setChecked(true);
                break;
            case "TE":
                RadioButton TE = findViewById(R.id.radio_TE);
                TE.setChecked(true);
                break;
            default:
                RadioButton BE = findViewById(R.id.radio_BE);
                BE.setChecked(true);
                break;
        }


        switch (Branch) {
            case "EXTC":
                RadioButton EXTC = findViewById(R.id.radio_extc);
                EXTC.setChecked(true);
                break;
            case "COMPS":
                RadioButton Comps = findViewById(R.id.radio_comps);
                Comps.setChecked(true);
                break;
            case "MECH":
                RadioButton MECH = findViewById(R.id.radio_mech);
                MECH.setChecked(true);
                break;
            default:
                RadioButton IT = findViewById(R.id.radio_IT);
                IT.setChecked(true);
                break;

        }

//        String Batch = getIntent().getStringExtra("batch");
//        switch (Batch) {
//            case "A":
//                RadioButton batch_a = findViewById(R.id.radio_A);
//                batch_a.setChecked(true);
//                break;
//            case "B":
//                RadioButton batch_b = findViewById(R.id.radio_B);
//                batch_b.setChecked(true);
//                break;
//            case "C":
//                RadioButton batch_c = findViewById(R.id.radio_C);
//                batch_c.setChecked(true);
//                break;
//            default:
//                RadioButton batch_d = findViewById(R.id.radio_D);
//                batch_d.setChecked(true);
//                break;
//        }
//        Log.i("check data incoming", getIntent().getStringExtra("year") + " " + getIntent().getStringExtra("branch") + " " + getIntent().getStringExtra("batch"));
        Log.i("volleyABC", "position value" + position_s + getIntent().getStringExtra("core_id"));



        save_button.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {

                //Intent profile_after_edit = new Intent(ProfileEdit.this, MainActivity.class);

                String id_s = id.getText().toString();
                String name_s = name.getText().toString();
                String email_s = email.getText().toString();
                String prof_url = profileImageUrl;
                if (!isEmailValid(email_s)) {
                    // email.setError("Invalid Email Address");
                    Log.i("emailerror", "wrong email" + email_s);
                    email_s = getIntent().getStringExtra("email");
                    Toast.makeText(ProfileEdit.this, "wrong email continueing with existing email", Toast.LENGTH_SHORT).show();
                    Log.i("emailerror", "posted email" + email_s);
                }
                String phn_s = phn.getText().toString();
                String rol_s = rol.getText().toString();
                String yr_s;
                String branch_s;
                //    String batch_s;

                RadioButton yr_b = findViewById(yr.getCheckedRadioButtonId());
                RadioButton br_b = findViewById(branch.getCheckedRadioButtonId());
                //      RadioButton batch_b = findViewById(batch.getCheckedRadioButtonId());

                if (yr_b != null) yr_s = yr_b.getText().toString();
                else yr_s = getIntent().getStringExtra("year");
                if (br_b != null) branch_s = br_b.getText().toString();
                else branch_s = getIntent().getStringExtra("branch");
                //    if (batch_b != null) batch_s = batch_b.getText().toString();
                //    else batch_s = getIntent().getStringExtra("batch");

                post_edited_info(id_s, name_s, email_s, phn_s, yr_s, branch_s, rol_s , prof_url);//posting data on server
                Log.i("ID idhar ayega", id_s);
                //profile_after_edit.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(profile_after_edit);
                //ProfileEdit.this.getSupportFragmentManager().beginTransaction().replace(R.id.containerID, Profile.newInstance()).commit();
                //finish();



            }
        });

    }


//    private void loadImageUrl(String url) {
//        Picasso.get().load(url).placeholder(R.mipmap.ic_launcher)
//                .error(R.mipmap.ic_launcher)
//                .into(imageButton, new com.squareup.picasso.Callback() {
//                    @Override
//                    public void onSuccess() {
//                        // Handle success
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        // Handle error
//                    }
//                });
//    }

    void post_edited_info(String id_s, String name_s, String email_s, String phn_s, String yr_s, String branch_s, String rol_s, final String batch_s)
    {
        Log.i("volleyABC", "Reached in get info");

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id",id_s);
            jsonObject.put("name",name_s);
//            jsonObject.put("core_role_id",position_s); //actual value will get from loginpage
            jsonObject.put("core_email",email_s);
            jsonObject.put("core_mobileno",phn_s);
            jsonObject.put("year",yr_s);
            jsonObject.put("branch",branch_s);
            jsonObject.put("rollno",rol_s);
            //   jsonObject.put("core_profile_url" , profileurl);
//            jsonObject.put("batch",batch_s);
            Log.i("volleyABC", "Created jason");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.i("volleyABC", "error in jason creation");
        }

        final String requestBody = jsonObject.toString();
        Log.i("final json", requestBody);
        server_url = getApplicationContext().getResources().getString(R.string.server_url) + "/profile/edit/?id="+id_s+"&name="+name_s+"&core_email="+email_s+"&core_mobileno="+phn_s+"&year="+yr_s+"&branch="+branch_s+"&rollno="+rol_s;
        Log.i("Naya url", server_url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("volleyABC", "onResponse:edit reached ");
                Toast.makeText(ProfileEdit.this,"Profile Updated",Toast.LENGTH_SHORT).show();
                //Intent profile_after_edit =new Intent(ProfileEdit.this, MainActivity.class);

                //startActivity(profile_after_edit);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    //String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.i("volleyABC" ,"edit"+Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(ProfileEdit.this,"Invalid Username or Password",Toast.LENGTH_SHORT).show();
                    error.printStackTrace();}
                catch (Exception e)
                {
                    Log.i("volleyABC" ,"edit exception");
                    Toast.makeText(ProfileEdit.this,"Check Network",Toast.LENGTH_SHORT).show();}

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
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    //Below Method is used to close the app after using Back Press
    /*
    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(a);
    }*/

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