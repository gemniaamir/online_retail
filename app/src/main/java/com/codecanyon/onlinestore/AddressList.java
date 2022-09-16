package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.AddressAdapter;
import com.codecanyon.onlinestore.models.AddressModel;
import com.codecanyon.onlinestore.models.UserClass;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;

public class AddressList extends AppCompatActivity implements RecyclerViewInterface {

    ImageView backButton;
    RecyclerView addressRecycler;
    RelativeLayout loadingView, noAddress;
    FloatingActionButton addAddress;
    TextView itemFound;

    UserClass userClass;
    SharedPreferences mPrefs;
    Boolean isFirstTime = true;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    AddressAdapter adapter;
    ArrayList<AddressModel> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        noAddress = findViewById(R.id.noAddressView);
        backButton = findViewById(R.id.backButton);
        addressRecycler = findViewById(R.id.addressRecycler);
        loadingView = findViewById(R.id.loadingView);
        addAddress = findViewById(R.id.addAddress);
        itemFound = findViewById(R.id.itemFound);

        backButton.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        addAddress.setOnClickListener(view -> {
            startActivity(new Intent(AddressList.this, AddAddress.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });

        loadPreference();
    }

    private void loadPreference() {
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");
        userClass = gson.fromJson(userJson, UserClass.class);

        loadAddressesFromFireStore(userClass.getUID());
    }

    private void loadAddressesFromFireStore(String userId) {
        if (isNetworkAvailable()){
            collectionReference = db.collection("Addresses");
            loadingView.setVisibility(View.VISIBLE);

            collectionReference.document(userId)
                    .collection("Address List")
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        addresses = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            AddressModel addressModel = documentSnapshot.toObject(AddressModel.class);
                            addressModel.setAddressId(documentSnapshot.getId());
                            addresses.add(addressModel);
                        }

                        loadingView.setVisibility(View.GONE);

                        if (addresses.size() > 0) {
                            noAddress.setVisibility(View.GONE);
                            itemFound.setText(addresses.size() + " Items found");
                            adapter = new AddressAdapter(AddressList.this, addresses, AddressList.this::onItemClick);
                            setAddressRecyclerData(adapter);
                        } else {
                            noAddress.setVisibility(View.VISIBLE);
                        }

                    }).addOnFailureListener(e -> {
                        loadingView.setVisibility(View.GONE);
                        TastyToast.makeText(AddressList.this, e.getMessage(),
                                TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    });
        }else {
            TastyToast.makeText(AddressList.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
        }
    }

    private void setAddressRecyclerData(AddressAdapter adapter) {
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(AddressList.this, LinearLayoutManager.VERTICAL, false);
        addressRecycler.setLayoutManager(layoutManager);
        addressRecycler.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, String type, View view) {
        if (type.equals("delete")) {
            deleteAddress(addresses.get(position).getAddressId(), position);
        }
        if (type.equals("edit")) {

        }
        if (type.equals("view")) {

        }
    }

    private void deleteAddress(String id, int position) {
        if (isNetworkAvailable()){
            collectionReference
                    .document(userClass.getUID())
                    .collection("Address List")
                    .document(id)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        loadingView.setVisibility(View.GONE);
                        addresses.remove(position);
                        adapter.notifyDataSetChanged();
                        itemFound.setText(addresses.size() + " Items found");

                        TastyToast.makeText(AddressList.this, "Item deleted successfully",
                                TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        if (addresses.size() == 0){
                            noAddress.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(e -> {
                        loadingView.setVisibility(View.GONE);
                        TastyToast.makeText(AddressList.this, e.getMessage(),
                                TastyToast.LENGTH_LONG, TastyToast.ERROR);

                    });
        }else {
            TastyToast.makeText(AddressList.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTime){
            isFirstTime = false;
        }else {
            loadAddressesFromFireStore(userClass.getUID());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}