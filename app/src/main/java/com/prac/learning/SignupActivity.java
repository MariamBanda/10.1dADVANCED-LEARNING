package com.prac.learning;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText username, email, confirmEmail, password, confirmPassword, phone;
    Button createAccountBtn;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        confirmEmail = findViewById(R.id.confirmEmail);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        phone = findViewById(R.id.phone);
        createAccountBtn = findViewById(R.id.createAccountBtn);
        dbHelper = new DatabaseHelper(this);

        createAccountBtn.setOnClickListener(v -> {
            String uname = username.getText().toString().trim();
            String em = email.getText().toString().trim();
            String cem = confirmEmail.getText().toString().trim();
            String pw = password.getText().toString().trim();
            String cpw = confirmPassword.getText().toString().trim();
            String ph = phone.getText().toString().trim();

            if (uname.isEmpty() || em.isEmpty() || cem.isEmpty() || pw.isEmpty() || cpw.isEmpty() || ph.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!em.equals(cem)) {
                Toast.makeText(this, "Emails do not match", Toast.LENGTH_SHORT).show();
            } else if (!pw.equals(cpw)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {

                String interests = "";
                if (dbHelper.addUser(uname, em, pw, interests)) {

                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                        .putString("username", uname)
                        .putString("email", em)
                        .apply();

                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignupActivity.this, InterestsActivity.class);
                    intent.putExtra("username", uname);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
