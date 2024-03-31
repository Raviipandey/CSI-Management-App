package in.dbit.csiapp.mActivityManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import in.dbit.csiapp.Prompts.MainActivity;
import in.dbit.csiapp.R;

public class Forgetpassword extends AppCompatActivity {

    private EditText txtemail;
    private String email;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);

        txtemail = findViewById(R.id.emailEditText);
        client = new OkHttpClient();

        Button resetpass = findViewById(R.id.resetPasswordButton);



        resetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatedata();
            }
        });
    }

    private void validatedata() {
        email = txtemail.getText().toString().trim();

        if (email.isEmpty()) {
            txtemail.setError("Email is required");
            txtemail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtemail.setError("Enter a valid email");
            txtemail.requestFocus();
            return;
        }

        forgetpass();
    }

    private void forgetpass() {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        Request request = new Request.Builder()
                .url(getApplicationContext().getResources().getString(R.string.server_url) + "/login/resetpassword")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Forgetpassword.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            Snackbar.make(findViewById(android.R.id.content), "Reset link has been sent to your email", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Email not found , try again", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
