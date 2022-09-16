package com.codecanyon.onlinestore.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.codecanyon.onlinestore.enums.Currencies;
import com.codecanyon.onlinestore.enums.PackingTypes;
import com.codecanyon.onlinestore.enums.ProductUnit;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.codecanyon.onlinestore.AdminPage;
import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.helpingclasses.NumberInputFilter;
import com.codecanyon.onlinestore.models.CategoryClass;
import com.codecanyon.onlinestore.models.DiscountCurrency;
import com.codecanyon.onlinestore.models.PackingModel;
import com.codecanyon.onlinestore.models.ProductModel;
import com.codecanyon.onlinestore.models.ProductUnitModel;

import java.util.ArrayList;

public class AddProduct extends AppCompatActivity {

    RelativeLayout parentView, loadingView;

    EditText productName, discountAmount, productPrice, productWeight, productDesc, companyName;
    SwitchCompat isFeatured, isDiscount;
    AppCompatSpinner category, currency, packing, productUnit;
    AppCompatButton addProduct;
    private static final String TAG = "AddProduct";
    ImageView back;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private String placeHolderImage = "https://firebasestorage.googleapis.com/v0/b/onlinestorecodecanyon.appspot.com/o/ProductImages%2Fseeds.png?alt=media&token=bba64167-e749-4d72-8514-2ace983e9322";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        parentView = findViewById(R.id.parentView);
        productWeight = findViewById(R.id.productWeight);
        productName = findViewById(R.id.productName);
        category = findViewById(R.id.category);
        category.setEnabled(false);
        discountAmount = findViewById(R.id.discountAmount);
        discountAmount.setFilters(new InputFilter[]{new NumberInputFilter(2)});
        currency = findViewById(R.id.discountCurrency);
        isFeatured = findViewById(R.id.featuredSwitch);
        isDiscount = findViewById(R.id.discountSwitch);
        productPrice = findViewById(R.id.productPrice);
        productPrice.setFilters(new InputFilter[]{new NumberInputFilter(2)});
        packing = findViewById(R.id.packingType);
        productDesc = findViewById(R.id.productDesc);
        productUnit = findViewById(R.id.productUnit);
        companyName = findViewById(R.id.companyName);
        addProduct = findViewById(R.id.addProduct);
        loadingView = findViewById(R.id.loadingView);
        back = findViewById(R.id.back);

        setupUI(parentView);
        loadAllCategories();
        loadAllCurrency();
        loadAllPackingType();
        loadAllProductUnits();

        addProduct.setOnClickListener(view -> {
            if (checkAllNecessaryFields()) {
                saveProduct();
            } else {
                Toast.makeText(AddProduct.this, "Fill all fields", Toast.LENGTH_LONG).show();
            }
        });

        back.setOnClickListener(view -> onBackPressed());

    }

    private void saveProduct() {
        loadingView.setVisibility(View.VISIBLE);
        collectionReference = db.collection("Products");

        collectionReference.add(new ProductModel(
                        isDiscount.isChecked(),
                        isFeatured.isChecked(),
                        category.getSelectedItem().toString(),
                        discountAmount.getText().toString().trim(),
                        currency.getSelectedItem().toString(),
                        placeHolderImage,
                        productName.getText().toString().trim(),
                        packing.getSelectedItem().toString(),
                        productPrice.getText().toString().trim(),
                        productUnit.getSelectedItem().toString(),
                        productWeight.getText().toString().trim(),
                        productDesc.getText().toString().trim().equals("")? "-":productDesc.getText().toString().trim(),
                        companyName.getText().toString().trim().equals("")?"-":companyName.getText().toString().trim()))
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, documentReference.getId());
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(AddProduct.this, "Product added successfully", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, AdminPage.class));
                    overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                }).addOnFailureListener(e -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(AddProduct.this, "Product couldn't added", Toast.LENGTH_LONG).show();
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
                    hideSoftKeyboard(AddProduct.this);
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

    private void setupCategorySpinner(ArrayList<CategoryClass> array) {
        if (array.size() > 0) {
            category.setEnabled(true);
            ArrayList<String> name = new ArrayList<>();

            for (int i = 0; i < array.size(); i++) {
                name.add(array.get(i).getCatName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddProduct.this, android.R.layout.simple_spinner_item, name);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            category.setAdapter(adapter);
        } else {
            category.setEnabled(false);
        }
    }

    private void loadAllCategories() {
        // initiate document and collection references
        collectionReference = db.collection("Product Categories");
        ArrayList<CategoryClass> list = new ArrayList<>();

        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                CategoryClass category = documentSnapshot.toObject(CategoryClass.class);
                category.setCatID(documentSnapshot.getId());
                list.add(category);
            }
            setupCategorySpinner(list);

        }).addOnFailureListener(e -> Toast.makeText(AddProduct.this, "Categories not found", Toast.LENGTH_LONG).show());
    }

    private void loadAllCurrency() {
        ArrayAdapter<Currencies> adapter = new ArrayAdapter<>(AddProduct.this,
                android.R.layout.simple_spinner_item, Currencies.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(adapter);
    }

    private void loadAllPackingType() {
        ArrayAdapter<PackingTypes> adapter = new ArrayAdapter<>(AddProduct.this,
                android.R.layout.simple_spinner_item, PackingTypes.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        packing.setAdapter(adapter);
    }

    private void loadAllProductUnits() {
        ArrayAdapter<ProductUnit> adapter = new ArrayAdapter<>(AddProduct.this,
                android.R.layout.simple_spinner_item, ProductUnit.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productUnit.setAdapter(adapter);
    }

    private Boolean checkAllNecessaryFields() {
        if (productName.getText().toString().trim().equals("")
                || productPrice.getText().toString().trim().equals("")
                || productWeight.getText().toString().trim().equals("")) {
            return false;
        } else {
            return true;
        }
    }

}