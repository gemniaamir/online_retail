package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.CartListProductAdapter;
import com.codecanyon.onlinestore.adapters.OrderHistoryAdapter;
import com.codecanyon.onlinestore.models.CartModel;
import com.codecanyon.onlinestore.models.PlacedOrderDetails;
import com.codecanyon.onlinestore.models.UserClass;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrderHistory extends AppCompatActivity implements RecyclerViewInterface {

    ImageView backButton;
    RecyclerView addressRecycler;
    RelativeLayout loadingView, noHistoryView;
    TextView itemFound;

    UserClass userClass;
    SharedPreferences mPrefs;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    OrderHistoryAdapter adapter;
    ArrayList<PlacedOrderDetails> orders;

    private BottomSheetBehavior mBottomSheetBehavior;
    View bottomSheet;
    ImageView cross;
    RecyclerView cartRecyclerView;
    ArrayList<CartModel> cartList = new ArrayList<>();
    TextView orderId, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        noHistoryView = findViewById(R.id.noHistoryView);
        backButton = findViewById(R.id.backButton);
        addressRecycler = findViewById(R.id.addressRecycler);
        loadingView = findViewById(R.id.loadingView);
        itemFound = findViewById(R.id.itemFound);
        bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        cross = findViewById(R.id.cross);
        orderId = findViewById(R.id.orderID);
        title = findViewById(R.id.title);

        cross.setOnClickListener(view -> mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        backButton.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        loadPreference();
    }

    private void loadPreference() {
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");
        userClass = gson.fromJson(userJson, UserClass.class);

        loadOrdersFromFireStore(userClass.getUID());
    }

    private void loadOrdersFromFireStore(String userId) {
        if (isNetworkAvailable()){
            collectionReference = db.collection("Orders Received");
            loadingView.setVisibility(View.VISIBLE);

            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                orders = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    PlacedOrderDetails orderDetail = documentSnapshot.toObject(PlacedOrderDetails.class);
                    if (userId.equals(orderDetail.getUserID())) {
                        orderDetail.setID(documentSnapshot.getId());
                        orders.add(orderDetail);
                    }
                }

                loadingView.setVisibility(View.GONE);

                if (orders.size() > 0) {
                    noHistoryView.setVisibility(View.GONE);
                    itemFound.setText(orders.size() + " Items found");
                    adapter = new OrderHistoryAdapter(OrderHistory.this, orders, OrderHistory.this::onItemClick);
                    setOrdersRecyclerData(adapter);
                } else {
                    noHistoryView.setVisibility(View.VISIBLE);
                }

            }).addOnFailureListener(e -> {
                loadingView.setVisibility(View.GONE);
                TastyToast.makeText(OrderHistory.this, e.getMessage(),
                        TastyToast.LENGTH_LONG, TastyToast.ERROR);
            });
        }else {
            TastyToast.makeText(OrderHistory.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
        }
    }

    private void setOrdersRecyclerData(OrderHistoryAdapter adapter) {
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(OrderHistory.this, LinearLayoutManager.VERTICAL, false);
        addressRecycler.setLayoutManager(layoutManager);
        addressRecycler.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, String type, View view) {
        if (type.equals("view")) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            orderId.setText(orders.get(position).getID());
            // Get order details from FireStore DB by sending cart id and user id
            collectionReference = db.collection("Cart List");
            cartList.clear();
            collectionReference.document(userClass.getUID())
                    .collection("Cart List")
                    .document(orders.get(position).getCartID())
                    .get().addOnSuccessListener(documentSnapshot -> {
                        // Get total added items from Firestore Online DB
                        Map<String, Object> list = new HashMap<>(); // e.g. Key: 0bbQsKQKQjAmfpXRwm6p , Value : Cart Item Entry
                        Set<String> keys = new HashSet<>(); // Key of Cart Item Entry e.g. 0bbQsKQKQjAmfpXRwm6p
                        // e.g. List of Key Value Pairs of type -->
                        // Key: 0bbQsKQKQjAmfpXRwm6p
                        // Value: {Cart Entry Items} can be multiple items
                        list = documentSnapshot.getData();
                        // list of all keys e.g. 0bbQsKQKQjAmfpXRwm6p
                        keys = list.keySet();

                        final ObjectMapper mapper = new ObjectMapper();

                        // Extract CartModel class from values of list
                        for (String key : keys) {
                            final CartModel pojo = mapper.convertValue(list.get(key), CartModel.class);
                            pojo.setCartID(orders.get(position).getCartID());
                            cartList.add(pojo);
                        }

                        if (cartList.size() > 0){
                            setCartItemsRecycler();
                        }else {
                            TastyToast.makeText(OrderHistory.this, "Nothing found", TastyToast.LENGTH_LONG, TastyToast.INFO).show();
                        }

                    }).addOnFailureListener(e -> {
                        TastyToast.makeText(OrderHistory.this, e.getMessage(), TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
                    });
        }
    }

    private void setCartItemsRecycler() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(OrderHistory.this,
                LinearLayoutManager.VERTICAL,
                false);
        cartRecyclerView.setLayoutManager(layoutManager);
        cartRecyclerView.setAdapter( new CartListProductAdapter(OrderHistory.this, cartList));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}