package in.dbit.csiapp.mActivityManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.IOException;

import in.dbit.csiapp.Prompts.MainActivity;
import in.dbit.csiapp.R;
import okhttp3.*;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private Button submitPasswordButton;

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        // Initialize UI elements
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        submitPasswordButton = findViewById(R.id.submitPasswordButton);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // Extract the path after "/app/"
            String path = data.getPath();
            if (path != null && path.startsWith("/app/")) {
                // Extract the content from the path
                token = path.substring(5); // Remove "/app/" from the path
                Log.i("Deep link", token);
                Toast.makeText(ResetPasswordActivity.this, token, Toast.LENGTH_LONG).show();
            }
        }

        // Add text change listener to confirmPasswordEditText
        confirmPasswordEditText.addTextChangedListener(confirmPasswordTextWatcher);

        // Set click listener for submit button
        submitPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve passwords entered by the user
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                // Check if passwords match
                if (password.equals(confirmPassword)) {
                    // Passwords match, proceed with sending the data to the server
                     // Replace with actual token received from deep link
                    sendPasswordToServer(token, password);
                } else {
                    // Passwords don't match, show error
                    passwordLayout.setError("Passwords do not match");
                    confirmPasswordLayout.setError("Passwords do not match");
                }
            }
        });
    }

    // TextWatcher for confirmPasswordEditText to clear the error when the text changes
    private final TextWatcher confirmPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            // Clear the error when text changes
            confirmPasswordLayout.setError(null);
        }
    };

    private void sendPasswordToServer(String token, String newPassword) {
        OkHttpClient client = new OkHttpClient();

        // Create JSON request body
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, "{\"token\":\"" + token + "\",\"newPassword\":\"" + newPassword + "\"}");

        // Create POST request targeting the /login/newpassword route
        Request request = new Request.Builder()
                .url(getApplicationContext().getResources().getString(R.string.server_url)+"/login/newpassword")
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
            }


            @Override

            public void onResponse(Call call, Response response) throws IOException {
                // Handle response
                if (!response.isSuccessful()) {
                    if (response.code() == 404) {
                        // Display error message for 404 response
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ResetPasswordActivity.this, "Password reset token invalid or expired", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (response.code() == 400) {
                        // Display error message for 400 response (Bad Request)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ResetPasswordActivity.this, "New password must not be the same as the last three passwords.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (response.code() == 500) {
                        // Display error message for 500 response (Internal Server Error)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ResetPasswordActivity.this, "Internal Server Error. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle other unexpected response codes
                        throw new IOException("Unexpected code " + response.code());
                    }
                } else {
                    // Display success message in the UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ResetPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
                        }
                    });
                }
            }


        });
    }

}
