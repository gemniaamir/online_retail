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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.models.UserClass;
import com.ornach.nobobutton.NoboButton;
import com.sdsmdg.tastytoast.TastyToast;

public class LoginPage extends AppCompatActivity {

    NoboButton login, registerNow;
    ImageButton google, facebook;
    TextInputEditText password, email;

    private FirebaseAuth mAuth;
    private final String TAG = "LoginPage";
    UserClass loggedInUser = new UserClass();

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    SharedPreferences mPrefs;

    RelativeLayout loadingView, parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        login = findViewById(R.id.login);
        registerNow = findViewById(R.id.registerNow);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        loadingView = findViewById(R.id.loadingView);
        parentView = findViewById(R.id.parentView);

        setupUI(parentView);

        login.setOnClickListener(view -> {
            hideSoftKeyboard(LoginPage.this);

            if (email.getText().toString().trim().equals("")
                    || password.getText().toString().trim().equals("")){
                TastyToast.makeText(getApplicationContext()
                        , "Please provide credentials",
                        TastyToast.LENGTH_LONG,
                        TastyToast.WARNING);
            }else {
                if (email.getText().toString().trim().equals("Admin") && password.getText().toString().trim().equals("123456")){
                    // Goto Admin page
                    startActivity(new Intent(LoginPage.this, AdminPage.class));
                }else {
                    loadingView.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                            .addOnCompleteListener(this, task -> {
                                loadingView.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (isNetworkAvailable()){
                                        getUserDetailFromFireStoreDB(user.getUid(), "SignIn");
                                    }else {
                                        TastyToast.makeText(LoginPage.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    TastyToast.makeText(getApplicationContext(),
                                            task.getException().getMessage(),
                                            TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            });
                }
            }
        });

        facebook.setOnClickListener(view -> {

        });

        google.setOnClickListener(view -> {

        });

        registerNow.setOnClickListener(view -> {
            startActivity(new Intent(this, RegistrationPage.class));
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
                loadingView.setVisibility(View.VISIBLE);
                if (isNetworkAvailable()){
                    getUserDetailFromFireStoreDB(currentUser.getUid(), "");
                }else {
                    TastyToast.makeText(LoginPage.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
                }
        }
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void getUserDetailFromFireStoreDB(String uid, String from){
        // TODO After deleting an account from Firebase Authentication account is still logged in application.
        // We are checking if account still exists in FireStore User table. If not then we will take this user no longer exists.
        // This is not a proper way. It will use FireStore Read operation unnecessarily.
        // Find a proper solution regarding deletion of a user and automatically SignOut from application.

        collectionReference = db.collection("Registered Users");
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

                loadingView.setVisibility(View.GONE);
                // There is a user already logged in. We can advance to Home Activity
                startActivity(new Intent(this, Home.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            }else {
                if (from.contains("SignIn")){
                    TastyToast.makeText(getApplicationContext(),
                            "User info not found",
                            TastyToast.LENGTH_LONG, TastyToast.ERROR);

                }
            }
        });
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
                    hideSoftKeyboard(LoginPage.this);
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

    // TODO: Add facebook and google login
}