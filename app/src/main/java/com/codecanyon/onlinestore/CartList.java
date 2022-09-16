package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.CartProductAdapter;
import com.codecanyon.onlinestore.models.CartModel;
import com.codecanyon.onlinestore.models.UserClass;
import com.ornach.nobobutton.NoboButton;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartList extends AppCompatActivity implements RecyclerViewInterface{

    // Database Related Variables
    CartProductAdapter adapter;
    ArrayList<CartModel> cartList = new ArrayList<>();
    RecyclerView cartRecyclerView;
    RelativeLayout rootView;
    TextView total, totalPrice;
    ImageView backButton;

    ImageView noCart;
    SharedPreferences mPrefs;

    final private static String TAG = "CartList";

    String activeCartDocumentID = "";
    String userId = "";

    RelativeLayout loadingView;
    NoboButton checkOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        backButton = findViewById(R.id.backButton);
        loadingView = findViewById(R.id.loadingView);
        total = findViewById(R.id.total);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        rootView = findViewById(R.id.rootView);
        total = findViewById(R.id.total);
        totalPrice = findViewById(R.id.totalAmount);
        noCart = findViewById(R.id.noCart);
        checkOut = findViewById(R.id.checkOut);

        loadPreferenceAndView();

        // Load Cart List against this user and cart id from FireStore DB
        if (!activeCartDocumentID.equals("")){
            loadCartListFromPreference();
        }

        findViewById(R.id.gotoBack).setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        checkOut.setOnClickListener(view -> {
            saveCartItemsToPreferences();
            // Save Cart with active cart id into FireStore DB
            startActivity(new Intent(CartList.this, PlaceOrder.class));
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });
    }

    private void loadCartListFromPreference() {

        Gson gson = new Gson();

        String json = mPrefs.getString("CartItems", "");
        Type productType = new TypeToken<List<CartModel>>() {}.getType();
        cartList =  gson.fromJson(json, productType);

        adapter = new CartProductAdapter(CartList.this, cartList, this::onItemClick);
        setCartRecyclerData(adapter);

        if (cartList.size() > 0){
            calculateCartValue();
        }
    }

    private void calculateCartValue(){
        // Calculating total cart amount
        Integer totalAmount = 0;
        int quantity = 0;
        for (int j = 0 ; j < cartList.size() ; j++){
            totalAmount = totalAmount + (cartList.get(j).getPriceAfterDiscount() * cartList.get(j).getQuantity());
            quantity = quantity + cartList.get(j).getQuantity();
        }

        totalPrice.setText( "Rs. " + (new DecimalFormat("0.00").format(totalAmount)));
        total.setText("Total " + quantity + " items");

        if (quantity == 0){
            noCart.setVisibility(View.VISIBLE);
        }else {
            noCart.setVisibility(View.GONE);
        }
    }

    private void setCartRecyclerData(CartProductAdapter adapter) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CartList.this, LinearLayoutManager.VERTICAL, false);
        cartRecyclerView.setLayoutManager(layoutManager);
        cartRecyclerView.setAdapter(adapter);
    }

    private void loadPreferenceAndView() {
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");
        UserClass userClass = gson.fromJson(userJson, UserClass.class);
        userId = userClass.getUID();

        activeCartDocumentID = mPrefs.getString("ActiveCartDocumentID", "");
    }

    @Override
    public void onItemClick(int position, String type, View view) {
        if (view.getId() == R.id.add){
            cartList.get(position).setQuantity(cartList.get(position).getQuantity() + 1);
        }
        if (view.getId() == R.id.less){
            if (cartList.get(position).getQuantity() > 1){
                cartList.get(position).setQuantity(cartList.get(position).getQuantity() - 1);
            }else {
                cartList.remove(position);
            }
        }

        adapter.notifyDataSetChanged();
        saveCartItemsToPreferences();
        calculateCartValue();
    }

    private void saveCartItemsToPreferences(){
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartList);
        prefsEditor.putString("CartItems", json);
        prefsEditor.apply();
    }

}