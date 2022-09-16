package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.models.UserClass;
import com.ornach.nobobutton.NoboButton;

public class ProfileDetail extends AppCompatActivity {

    TextView fullName, email, memberSince, phoneNo, password;
    ImageView backButton;
    UserClass loggedInUser;
    SharedPreferences mPrefs;
    NoboButton changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        backButton = findViewById(R.id.backButton);
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        phoneNo = findViewById(R.id.phoneNo);
        memberSince = findViewById(R.id.memberSince);
        password = findViewById(R.id.password);
        changePassword = findViewById(R.id.changePassword);

        backButton.setOnClickListener(view -> {
            ProfileDetail.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        loadPreferencesAndViews();

        changePassword.setOnClickListener(view -> {
            startActivity(new Intent(ProfileDetail.this, ChangePassword.class));
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });
    }

    private void loadPreferencesAndViews(){
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");

        loggedInUser = gson.fromJson(userJson, UserClass.class);

        fullName.setText(loggedInUser.getFullName());
        email.setText(loggedInUser.getEmail());
        phoneNo.setText(loggedInUser.getPhone());
        Timestamp time = loggedInUser.getCreationDate();
        memberSince.setText(time.toDate().toString());
        password.setText(loggedInUser.getPassword());
    }
}