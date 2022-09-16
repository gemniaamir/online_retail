package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.codecanyon.onlinestore.models.UserClass;
import com.ornach.nobobutton.NoboButton;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.Date;

public class RegistrationPage extends AppCompatActivity {

    ImageView google, facebook;
    NoboButton registerNow;
    TextInputEditText password, email, phone, name;
    ImageView gotoBack;

    private FirebaseAuth mAuth;
    private final String TAG = "Registration";

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    RelativeLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        name = findViewById(R.id.name);
        registerNow = findViewById(R.id.registerNow);
        parentView = findViewById(R.id.parentView);
        gotoBack = findViewById(R.id.gotoBack);

        setupUI(parentView);

        registerNow.setOnClickListener(view -> {
            if (password.getText().toString().trim().equals("") ||
                    email.getText().toString().trim().equals("") ||
                    phone.getText().toString().trim().equals("") ||
                    name.getText().toString().trim().equals("")) {
                TastyToast.makeText(getApplicationContext()
                        , "Fill all fields",
                        TastyToast.LENGTH_LONG,
                        TastyToast.WARNING);
            } else {
                mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                saveUserInFirestore(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                TastyToast.makeText(getApplicationContext(),
                                        task.getException().getMessage(),
                                        TastyToast.LENGTH_LONG, TastyToast.ERROR);
                            }
                        });
            }
        });

        facebook.setOnClickListener(view -> {

        });

        google.setOnClickListener(view -> {

        });

        gotoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegistrationPage.super.onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

    }

    private void saveUserInFirestore(FirebaseUser user) {
        if (isNetworkAvailable()){
            // Creating a user to store in FireStore DB
            UserClass regUser = new UserClass(user.getUid(),
                    name.getText().toString().trim(),
                    user.getEmail(), password.getText().toString().trim(),
                    new Timestamp(new Date()), phone.getText().toString().trim());

            collectionReference = db.collection("Registered Users");
            collectionReference.document(user.getUid()).set(regUser)
                    .addOnSuccessListener(documentReference -> {
                        TastyToast.makeText(getApplicationContext(),
                                "User saved successfully",
                                TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                        super.onBackPressed();
                        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                    })
                    .addOnFailureListener(e -> {
                        TastyToast.makeText(getApplicationContext(),
                                "User data cannot be saved",
                                TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    });
        }else {
            TastyToast.makeText(RegistrationPage.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
        }
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();

        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(RegistrationPage.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        RegistrationPage.super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    // Todo: Add registration via google and facebook
}