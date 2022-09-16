package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.models.UserClass;

public class Splash extends AppCompatActivity {

    ImageView icon;

    private static int SPLASH_TIME_OUT = 2000;
    int PERMISSION_ALL = 1;

    String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET};

    SharedPreferences mPrefs;

    private FirebaseAuth mAuth;
    UserClass loggedInUser = new UserClass();
    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        icon = findViewById(R.id.icon);

        // Change status bar android
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.splash_background));

        // Animate icon to fade out
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(4000);

        icon.setAnimation(fadeOut);
        fadeOut.start();

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else{
            new Handler().postDelayed(() -> {
                if (mPrefs.getString("FirstTime", "").equals("NO")){
                    // Check if user is signed in (non-null) and update UI accordingly.
                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    if(currentUser != null){
                        getUserDetailFromFireStoreDB(currentUser.getUid(), "");
                    }else {
                        startActivity(new Intent(Splash.this, LoginPage.class));
                        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                    }
                }else {
                    startActivity(new Intent(Splash.this, IntroActivity.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                }
            }, SPLASH_TIME_OUT);
        }

    }

    private void getUserDetailFromFireStoreDB(String uid, String from){
        // TODO After deleting an account from Firebase Authentication account is still logged in application.
        // We are checking if account still exists in FireStore User table. If not then we will take this user no longer exists.
        // This is not a proper way. It will use FireStore Read operation unnecessarily.
        // Find a proper solution regarding deletion of a user and automatically SignOut from application.

        collectionReference = db.collection("Registered User");
        DocumentReference usersRef = collectionReference.document(uid);

        usersRef.get().addOnCompleteListener(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            if (documentSnapshot.exists()){
                loggedInUser.setID(documentSnapshot.getId());
                loggedInUser = documentSnapshot.toObject(UserClass.class);

                // Save User Detail into SharedPreference for future use
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(loggedInUser);
                prefsEditor.putString("User", json);
                prefsEditor.apply();

                // There is a user already logged in. We can advance to Home Activity
                startActivity(new Intent(this, Home.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            }else {
                // User not found
                startActivity(new Intent(this, LoginPage.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(() -> {
                        if (mPrefs.getString("FirstTime", "").equals("NO")){
                            // Check if user is signed in (non-null) and update UI accordingly.
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            if(currentUser != null){
                                getUserDetailFromFireStoreDB(currentUser.getUid(), "");
                            }else {
                                startActivity(new Intent(Splash.this, LoginPage.class));
                                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                            }
                        }else {
                            startActivity(new Intent(Splash.this, IntroActivity.class));
                            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                        }
                      }, SPLASH_TIME_OUT);

                } else {
                    Toast.makeText(Splash.this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}