package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.CartListProductAdapter;
import com.codecanyon.onlinestore.models.AddressModel;
import com.codecanyon.onlinestore.models.CartModel;
import com.codecanyon.onlinestore.models.PlacedOrderDetails;
import com.codecanyon.onlinestore.models.UserClass;
import com.ornach.nobobutton.NoboButton;
import com.sdsmdg.tastytoast.TastyToast;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceOrder extends AppCompatActivity {

    RecyclerView cartRecyclerView;
    ArrayList<CartModel> cartList;
    TextView totalDiscountAmount, totalGrossAmount, totalNetAmount, deliveryCharges;
    NoboButton placeOrder;
    SharedPreferences mPrefs;

    AppCompatSpinner deliverySpinner;
    RelativeLayout loadingView;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    UserClass userClass;
    String activeCartDocumentID = "";
    ArrayList<AddressModel> addresses = new ArrayList<>();
    AppCompatRadioButton grocerySwitch, codSwitch;
    String selectedPaymentType = "COD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);
        deliverySpinner = findViewById(R.id.deliverySpinner);
        totalDiscountAmount = findViewById(R.id.totalDiscountAmount);
        totalGrossAmount = findViewById(R.id.totalGrossAmount);
        totalNetAmount = findViewById(R.id.totalAmount);
        placeOrder = findViewById(R.id.placeOrder);
        loadingView = findViewById(R.id.loadingView);
        grocerySwitch = findViewById(R.id.groceryCardButton);
        codSwitch = findViewById(R.id.codButton);
        deliveryCharges = findViewById(R.id.deliveryCharges);

        // Load cart from shared preferences
        loadCartList();

        findViewById(R.id.backButton).setOnClickListener(view -> {
            PlaceOrder.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        placeOrder.setOnClickListener(view -> {
            if (isNetworkAvailable()){
                if (addresses.size() == 0){
                    TastyToast.makeText(PlaceOrder.this, "Please add delivery address from Profile",
                            TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
                }else {
                    addCartListToFireStore(activeCartDocumentID);
                }
            }else {
                TastyToast.makeText(PlaceOrder.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
            }
        });

        grocerySwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked){
                selectedPaymentType = "CARD";
            }else {
                selectedPaymentType = "COD";
            }
        });

        codSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked){
                selectedPaymentType = "COD";
            }else {
                selectedPaymentType = "CARD";
            }
        });
    }

    private void loadAmounts(){
        int netTotal = 0, grossTotal = 0, discountTotal = 0;

        for (int i = 0 ; i < cartList.size() ; i++){
            discountTotal = discountTotal + (cartList.get(i).getDiscountAmount() * cartList.get(i).getQuantity());
            netTotal = netTotal + cartList.get(i).getPriceAfterDiscount() * cartList.get(i).getQuantity();
            grossTotal = grossTotal + cartList.get(i).getQuantity() *
                    (cartList.get(i).getDiscountAmount() + cartList.get(i).getPriceAfterDiscount());
        }

        totalDiscountAmount.setText(discountTotal + " Rs.");
        totalGrossAmount.setText(grossTotal + " Rs.");
        totalNetAmount.setText(netTotal + " Rs.");
    }

    private void setupDeliverySpinner(ArrayList<AddressModel> array) {
        if (array.size() > 0) {
            deliverySpinner.setEnabled(true);
            ArrayList<String> name = new ArrayList<>();

            for (int i = 0; i < array.size(); i++) {
                name.add(array.get(i).getStreet() + ", " + array.get(i).getHouseNo() + ", " + array.get(i).getNearBy() );
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(PlaceOrder.this, android.R.layout.simple_spinner_item, name);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deliverySpinner.setAdapter(adapter);
        } else {
            deliverySpinner.setEnabled(false);
        }
    }

    private void loadCartList() {
        Gson gson = new Gson();

        String json = mPrefs.getString("CartItems", "");
        Type productType = new TypeToken<List<CartModel>>() {}.getType();
        cartList =  gson.fromJson(json, productType);
        String userJson = mPrefs.getString("User", "");
        userClass = gson.fromJson(userJson, UserClass.class);
        activeCartDocumentID = mPrefs.getString("ActiveCartDocumentID", "");

        setCartItemsRecycler();
        loadAmounts();
        loadAddresses(userClass.getUID());
    }

    private void setCartItemsRecycler() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PlaceOrder.this,
                LinearLayoutManager.VERTICAL,
                false);
        cartRecyclerView.setLayoutManager(layoutManager);
        cartRecyclerView.setAdapter( new CartListProductAdapter(PlaceOrder.this, cartList));
    }

    private void loadAddresses(String userId){
        if (isNetworkAvailable()){
            db.collection("Addresses")
                    .document(userId)
                    .collection("Address List")
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            addresses.add(documentSnapshot.toObject(AddressModel.class));
                        }
                        if (addresses.size() > 0){
                            setupDeliverySpinner(addresses);
                        }
                    }).addOnFailureListener(e -> TastyToast.makeText(PlaceOrder.this, e.getMessage(), TastyToast.LENGTH_LONG, TastyToast.ERROR));
        }
    }

    private void addCartListToFireStore(String activeCartDocumentId) {
        loadingView.setVisibility(View.VISIBLE);
        collectionReference = db.collection("Cart List");

        Map<String, Object> cartItemsMap = new HashMap<>();

        for (int i = 0 ; i < cartList.size() ; i++){
            cartItemsMap.put(cartList.get(i).getProductID(), cartList.get(i));
        }

        collectionReference
                    .document(userClass.getUID())
                    .collection("Cart List")
                    .document(activeCartDocumentId)
                    .update(cartItemsMap)
                    .addOnSuccessListener(unused -> {
                        processOrder(activeCartDocumentId);
                    }).addOnFailureListener(e -> {
                        Log.d("Place Order", e.getMessage());
                        loadingView.setVisibility(View.GONE);
                        TastyToast.makeText(PlaceOrder.this, e.getMessage(),
                                TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                    });
    }

    private void processOrder(String cartID){
        loadingView.setVisibility(View.VISIBLE);
        collectionReference = db.collection("Orders Received");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        PlacedOrderDetails orderDetails = new PlacedOrderDetails(cartID, userClass.getUID(),
                deliverySpinner.getSelectedItem().toString()
                , selectedPaymentType
                , totalGrossAmount.getText().toString().replace(" Rs.", "")
                , totalDiscountAmount.getText().toString().replace(" Rs.", "")
                , deliveryCharges.getText().toString().replace(" Rs.", "")
                , totalNetAmount.getText().toString().replace(" Rs.", "")
                , format.format(new Date()));

        collectionReference.add(orderDetails).addOnSuccessListener(documentReference -> {
            loadingView.setVisibility(View.GONE);
            TastyToast.makeText(PlaceOrder.this, "Order placed successfully",
                    TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
            // Cleaning up active cart id
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            prefsEditor.putString("ActiveCartDocumentID", "");
            prefsEditor.remove("CartItems");
            prefsEditor.apply();
            startActivity(new Intent(PlaceOrder.this, Home.class));
        }).addOnFailureListener(e -> TastyToast.makeText(PlaceOrder.this, e.getMessage(),
                TastyToast.LENGTH_LONG, TastyToast.SUCCESS));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}