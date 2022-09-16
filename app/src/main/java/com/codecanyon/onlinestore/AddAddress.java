package com.codecanyon.onlinestore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.models.AddressModel;
import com.codecanyon.onlinestore.models.UserClass;
import com.omarshehe.forminputkotlin.FormInputSpinner;
import com.omarshehe.forminputkotlin.FormInputText;
import com.ornach.nobobutton.NoboButton;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.Date;

public class AddAddress extends AppCompatActivity {

    ImageView backButton;
    RelativeLayout loadingView;
    FormInputSpinner typeSpinner;
    TextView latitude, longitude, gLocation;
    FormInputText street, houseNo, nearby, areaName;
    AppCompatButton gotoMap;
    NoboButton addAddress;
    RelativeLayout parentView;

    UserClass userClass;
    SharedPreferences mPrefs;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        backButton = findViewById(R.id.backButton);
        loadingView = findViewById(R.id.loadingView);
        typeSpinner = findViewById(R.id.addressType);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        gLocation = findViewById(R.id.gLocation);
        street = findViewById(R.id.street);
        houseNo = findViewById(R.id.houseNo);
        areaName = findViewById(R.id.areaName);
        nearby = findViewById(R.id.nearby);
        gotoMap = findViewById(R.id.gotoMap);
        addAddress = findViewById(R.id.addAddress);
        loadingView = findViewById(R.id.loadingView);
        parentView = findViewById(R.id.parentView);

        setupUI(parentView);

        typeSpinner.setValue("Home");

        backButton.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        addAddress.setOnClickListener(view -> {
            if (street.getValue().equals("")
                    || houseNo.getValue().equals("")
                    || areaName.getValue().equals("")
                    || nearby.getValue().equals("")
                    || latitude.getText().toString().equals("")
                    || longitude.getText().toString().equals("")
                    || gLocation.getText().toString().equals("")){
                TastyToast.makeText(AddAddress.this, "Fill all fields",
                        TastyToast.LENGTH_LONG, TastyToast.ERROR);
            }else {
                addAddressToFireStore(new AddressModel(
                        typeSpinner.getValue(),
                        street.getValue(),
                        houseNo.getValue(),
                        nearby.getValue(),
                        new Timestamp(new Date()),
                        new GeoPoint(Double.parseDouble(latitude.getText().toString()
                                ), Double.parseDouble(longitude.getText().toString())),
                        areaName.getValue()));
                loadingView.setVisibility(View.VISIBLE);
            }
        });

        gotoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(AddAddress.this, MapActivity.class), 200);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            }
        });

        loadPreference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200){
            if(resultCode == Activity.RESULT_OK){
                latitude.setText(data.getStringExtra("Lat"));
                longitude.setText(data.getStringExtra("Lon"));
                gLocation.setText(data.getStringExtra("Loc"));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // Write your code if there's no result
                TastyToast.makeText(AddAddress.this, "No address selected", TastyToast.LENGTH_LONG, TastyToast.WARNING);
            }
        }
    }

    private void loadPreference() {
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");
        userClass = gson.fromJson(userJson, UserClass.class);
    }

    private void addAddressToFireStore(AddressModel address) {
        if (isNetworkAvailable()){
            collectionReference = db.collection("Addresses");

            collectionReference
                    .document(userClass.getUID())
                    .collection("Address List")
                    .add(address)
                    .addOnSuccessListener(unused -> {
                        loadingView.setVisibility(View.GONE);
                        TastyToast.makeText(AddAddress.this, "Address added successfully",
                                TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                        super.onBackPressed();
                        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }).addOnFailureListener(e -> {
                        loadingView.setVisibility(View.GONE);
                        TastyToast.makeText(AddAddress.this, e.getMessage(),
                                TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    });
        }else {
            TastyToast.makeText(AddAddress.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
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
                    hideSoftKeyboard(AddAddress.this);
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

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}