package in.dbit.csiapp.mActivityManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import in.dbit.csiapp.Prompts.MainActivity;
import in.dbit.csiapp.R;

public class Forgetpassword extends AppCompatActivity {

    private EditText txtemail; // Corrected initialization
    private String email;
    private FirebaseAuth auth; // Corrected initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);

        // Initialize EditText
        txtemail = findViewById(R.id.emailEditText); // Corrected initialization

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance(); // Corrected initialization

        Button resetpass = findViewById(R.id.resetPasswordButton);

        resetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatedata();
            }
        });
    }

    private void validatedata() {
        email = txtemail.getText().toString();
        if (email.isEmpty()) {
            txtemail.setError("Required");
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
        }
        else{
            forgetpass();
        }
    }

    private void forgetpass() {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Forgetpassword.this, "Check your mail", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Forgetpassword.this, MainActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(Forgetpassword.this, "Error :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
