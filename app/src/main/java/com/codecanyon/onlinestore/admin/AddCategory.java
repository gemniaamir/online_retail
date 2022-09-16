package com.codecanyon.onlinestore.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.codecanyon.onlinestore.AdminPage;
import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.models.CategoryClass;
import com.codecanyon.onlinestore.models.ProductModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddCategory extends AppCompatActivity {

    private String placeHolderImage = "https://firebasestorage.googleapis.com/v0/b/onlinestorecodecanyon.appspot.com/o/ProductImages%2Fcrisps.png?alt=media&token=a579306e-ad00-47d2-9420-530a93f72528";
    EditText categoryName;
    RelativeLayout parentView;
    AppCompatButton addCategory;
    private static final String TAG = "AddCategory";
    ImageView back;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    RelativeLayout loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        categoryName = findViewById(R.id.catName);
        parentView = findViewById(R.id.parentView);
        addCategory = findViewById(R.id.addCategory);
        back = findViewById(R.id.back);
        loadingView = findViewById(R.id.loadingView);

        setupUI(parentView);

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (categoryName.getText().toString().trim().equals("")){
                    Toast.makeText(AddCategory.this, "Please mention Category name", Toast.LENGTH_LONG).show();
                }else {
                    saveCategory();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddCategory.super.onBackPressed();
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
                    hideSoftKeyboard(AddCategory.this);
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

    private void saveCategory() {
        loadingView.setVisibility(View.VISIBLE);
        collectionReference = db.collection("Product Categories");

        collectionReference.add(new CategoryClass(categoryName.getText().toString().trim(), placeHolderImage))
                .addOnSuccessListener(documentReference -> {
                    loadingView.setVisibility(View.GONE);
                    Log.d(TAG, documentReference.getId());
                    Toast.makeText(AddCategory.this, "Category added successfully", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, AdminPage.class));
                    overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                }).addOnFailureListener(e -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(AddCategory.this, "Category couldn't added", Toast.LENGTH_LONG).show();
                });
    }

}