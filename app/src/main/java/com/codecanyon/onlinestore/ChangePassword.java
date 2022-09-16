package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.models.UserClass;
import com.ornach.nobobutton.NoboButton;
import com.sdsmdg.tastytoast.TastyToast;

public class ChangePassword extends AppCompatActivity {

    ImageView backButton;
    TextInputEditText oldPassword, newPassword, confirmPassword;
    NoboButton changePassword;
    UserClass loggedInUser;
    RelativeLayout parentView;

    SharedPreferences mPrefs;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    DocumentReference usersRef;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Get current signed in user
        currentUser = mAuth.getCurrentUser();

        parentView = findViewById(R.id.parentView);
        backButton = findViewById(R.id.backButton);
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        changePassword = findViewById(R.id.changePassword);

        loadPreferencesAndViews();

        backButton.setOnClickListener(view -> {
            ChangePassword.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        changePassword.setOnClickListener(view -> {
            if (oldPassword.getText().toString().trim().equals(loggedInUser.getPassword().trim())){
                if (confirmPassword.getText().toString().trim()
                        .equals(newPassword.getText().toString().trim())){
                    if (!confirmPassword.getText().toString().trim().equals("")){
                        if (isNetworkAvailable()){
                            usersRef.update("password",newPassword.getText().toString().trim()).addOnSuccessListener(unused -> {
                                currentUser.updatePassword(newPassword.getText().toString().trim()).addOnSuccessListener(unused1 -> {
                                    TastyToast.makeText(ChangePassword.this, "Password updated successfully",
                                            TastyToast.LENGTH_LONG, TastyToast.INFO);
                                    showBottomDialog("Password Change Successful",
                                            "You need to Sign Out and re-login with new password.",
                                            "Sign Out", "Cancel");
                                }).addOnFailureListener(e ->
                                        TastyToast.makeText(ChangePassword.this, e.getMessage(),
                                                TastyToast.LENGTH_LONG, TastyToast.INFO));
                            }).addOnFailureListener(e ->
                                    TastyToast.makeText(ChangePassword.this, e.getMessage(),
                                            TastyToast.LENGTH_LONG, TastyToast.INFO));
                        }else {
                            TastyToast.makeText(ChangePassword.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
                        }
                    }else {
                        TastyToast.makeText(ChangePassword.this, "Empty string not allowed",
                                TastyToast.LENGTH_LONG, TastyToast.INFO);
                    }
                }else {
                    TastyToast.makeText(ChangePassword.this, "Password doesn't matches",
                            TastyToast.LENGTH_LONG, TastyToast.CONFUSING);
                }
            }else {
                TastyToast.makeText(ChangePassword.this, "Please provide right password",
                        TastyToast.LENGTH_LONG, TastyToast.CONFUSING);
            }
        });

        setupUI(parentView);
    }

    private void loadPreferencesAndViews(){
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");

        loggedInUser = gson.fromJson(userJson, UserClass.class);

        collectionReference = db.collection("Registered User");
        usersRef = collectionReference.document(loggedInUser.getUID());
    }

    private void showBottomDialog(String title, String message, String positiveText, String negativeText){
        new BottomDialog.Builder(this)
                .setTitle(title)
                .setContent(message)
                .setCancelable(false)
                .setIcon(R.drawable.grocerycart)
                .setPositiveText(positiveText)
                .onPositive(dialog12 -> {
                    // Sign out for re login
                    FirebaseAuth.getInstance().signOut();
                    dialog12.dismiss();
                    overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                    startActivity(new Intent(ChangePassword.this, LoginPage.class));
                })
                .show();

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
                    hideSoftKeyboard(ChangePassword.this);
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

}