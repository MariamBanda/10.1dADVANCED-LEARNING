package com.prac.learning;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button loginBtn;
    TextView signupLink;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        signupLink = findViewById(R.id.signupText);
        dbHelper = new DatabaseHelper(this);

        loginBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                Cursor cursor = dbHelper.getUser(username);
                if (cursor != null && cursor.moveToFirst()) {
                    int passwordIndex = cursor.getColumnIndex("password");
                    int interestsIndex = cursor.getColumnIndex("interests");


                    if (passwordIndex != -1 && interestsIndex != -1) {
                        String storedPassword = cursor.getString(passwordIndex);
                        if (storedPassword.equals(password)) {
                            // Successfully logged in
                            String storedInterests = cursor.getString(interestsIndex);
                            ArrayList<String> interestList = new ArrayList<>(Arrays.asList(storedInterests.split(",")));
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.putExtra("username", username);
                            intent.putStringArrayListExtra("interests", interestList);

                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
