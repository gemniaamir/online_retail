package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.GeneralProductAdapter;
import com.codecanyon.onlinestore.models.CartModel;
import com.codecanyon.onlinestore.models.ProductModel;
import com.codecanyon.onlinestore.models.GeneralProduct;
import com.codecanyon.onlinestore.models.UserClass;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.ornach.nobobutton.NoboButton;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProductDetail extends AppCompatActivity implements RecyclerViewInterface{

    SharedPreferences mPrefs;
    ImageView productIcon;
    TextView productTitle, productPrice;
    RecyclerView recentProductRecycler;
    GeneralProductAdapter generalProductAdapter;
    ProductModel selectedProduct;

    ImageButton addQuantity, lessQuantity;
    TextView quantity, totalAmount, discount;
    TextView productDetail, companyName, productCategory, productWeight;
    NoboButton addToCart;
    RelativeLayout loadingView;
    LikeButton favourite;

    // Global Variable of Tiny DB
    TinyDB tinyDB;

    int quantityToAdd = 0;

    String userId = "";

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private String activeCartDocumentId = "";

    final private static String TAG = "ProductDetail";

    private final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";

    ArrayList<CartModel> cartList = new ArrayList<>();
    TextView cartCount;
    RelativeLayout cartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);
        loadingView = findViewById(R.id.loadingView);

        productDetail = findViewById(R.id.productDesc);
        productTitle = findViewById(R.id.productTitle);
        productPrice = findViewById(R.id.productPrice);
        productIcon = findViewById(R.id.productIcon);
        recentProductRecycler = findViewById(R.id.recentRecycler);
        cartCount = findViewById(R.id.cartCount);
        cartView = findViewById(R.id.cartView);

        productDetail = findViewById(R.id.productDetail);
        productWeight = findViewById(R.id.productWeight);
        companyName = findViewById(R.id.companyName);
        productCategory = findViewById(R.id.productCategory);

        addQuantity = findViewById(R.id.addQuantity);
        lessQuantity = findViewById(R.id.lessQuantity);
        quantity = findViewById(R.id.quantity);
        totalAmount = findViewById(R.id.totalAmount);
        addToCart = findViewById(R.id.addToCart);
        discount = findViewById(R.id.discount);
        favourite = findViewById(R.id.favourite);

        loadPreferenceAndView();

        quantityToAdd = Integer.parseInt(quantity.getText().toString());
        lessQuantity.setEnabled(false);

        // Recent List
        List<GeneralProduct> recentList = new ArrayList<>();
        recentList.add(new GeneralProduct("Rs. 230", "H&S Shampoo", "Bottle",
                "190 ml", R.drawable.shampoo_cat, ""));
        recentList.add(new GeneralProduct("Rs. 300", "Mitchell's Jam", "Jar",
                "1 kg",R.drawable.jam, ""));
        recentList.add(new GeneralProduct("Rs. 80", "Lux Soap", "Bar", "70 Gram",
                R.drawable.soap, ""));
        recentList.add(new GeneralProduct("Rs. 250", "Lipton Ice Tea", "Packet", "40 Sachets",
                R.drawable.icetea, ""));
        recentList.add(new GeneralProduct("Rs. 200", "Lipton Tea", "Packet", "20 Sachets",
                R.drawable.lipton, ""));
        recentList.add(new GeneralProduct("Rs. 199", "Lipton Green Tea", "Packet", "20 Sachets",
                R.drawable.greentea, ""));
        recentList.add(new GeneralProduct("Rs. 50", "Noodles", "Packet", "40 Gram",
                R.drawable.knorr, ""));
        recentList.add(new GeneralProduct("Rs. 85", "National Biryani", "Box", "65 Gram",
                R.drawable.biryani, ""));
        recentList.add(new GeneralProduct("Rs. 145", "Harpic Liquid", "Bottle", "150 ml",
                R.drawable.harpic, ""));

        setRecentProductRecyclerData(recentList);

        findViewById(R.id.cart).setOnClickListener(view -> {
            startActivity(new Intent(ProductDetail.this, CartList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });

        findViewById(R.id.gotoBack).setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        addQuantity.setOnClickListener(view -> {
            quantityToAdd += 1;
            quantity.setText(String.valueOf(quantityToAdd));

            if (quantityToAdd > 1) {
                lessQuantity.setEnabled(true);
            }
            updateTotalAmount(quantityToAdd);
        });

        lessQuantity.setOnClickListener(view -> {
            if (quantityToAdd > 1) {
                quantityToAdd -= 1;
                quantity.setText(String.valueOf(quantityToAdd));
            }

            if (quantityToAdd == 1) {
                lessQuantity.setEnabled(false);
            }
            updateTotalAmount(quantityToAdd);
        });

        addToCart.setOnClickListener(view -> {
            if (isNetworkAvailable()){
                loadingView.setVisibility(View.VISIBLE);
                collectionReference = db.collection("Cart List");

                // Creating cart model object
                CartModel cartModel = new CartModel(
                        selectedProduct.getProductName(),
                        Integer.parseInt(selectedProduct.getProductPrice())
                                - Integer.parseInt(selectedProduct.getProductDiscountAmt()),
                        Integer.parseInt(selectedProduct.getProductDiscountAmt()),
                        selectedProduct.getProductImage(),
                        selectedProduct.getCompanyName(),
                        Integer.parseInt(selectedProduct.getWeight()),
                        selectedProduct.getWeightUnit(),
                        quantityToAdd,
                        userId,
                        selectedProduct.getProductID(),
                        String.valueOf((Integer.parseInt(selectedProduct.getProductPrice())
                                - Integer.parseInt(selectedProduct.getProductDiscountAmt())) * quantityToAdd));

                if (activeCartDocumentId.equals("")) {
                    String randomId = getRandomCartId(13);
                    Map<String, Object> firstItem = new HashMap<>();
                    firstItem.put(cartModel.getProductID(), cartModel);
                    // Create a new cart id in user table
                    // Created a cart id (Document id) in Cart List (Collection) at Firebase Firestore
                    collectionReference.document(userId)
                            .collection("Cart List")
                            .document(randomId)
                            .set(firstItem)
                            .addOnSuccessListener(unused -> {
                                loadingView.setVisibility(View.GONE);

                                activeCartDocumentId = randomId;
                                // Save current cart id into Shared Preference,
                                // We will update cart id when user check out with current cart
                                // If user delete an item in cart we will delete it from Firestore also.
                                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                                prefsEditor.putString("ActiveCartDocumentID", activeCartDocumentId);
                                cartList.add(cartModel);
                                // Save items to newly created cart and active cart id
                                saveCartItemsToPreferences(cartList);
                                prefsEditor.apply();

                                TastyToast.makeText(ProductDetail.this, "Item added successfully",
                                        TastyToast.LENGTH_LONG, TastyToast.SUCCESS);

                                super.onBackPressed();
                                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);

                            }).addOnFailureListener(e -> {
                                loadingView.setVisibility(View.GONE);
                                TastyToast.makeText(ProductDetail.this, e.getMessage(),
                                        TastyToast.LENGTH_LONG, TastyToast.ERROR);
                            });
                } else {
                    // Save item into active cart id at shared preferences
                    addUpdateCartItemsToPreference(cartModel, activeCartDocumentId);
                }

            }else {
                TastyToast.makeText(ProductDetail.this, "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
            }
        });

        favourite.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        // Store product id into shared preference of favourites
                        ArrayList<String> favIds = returnFavIds();
                        favIds.add(selectedProduct.getProductID());
                        tinyDB.putListString("fav", favIds);
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        // Make it un-favourite inside shared preference
                        ArrayList<String> favIds = returnFavIds();
                        favIds.remove(favIds.indexOf(selectedProduct.getProductID()));
                        tinyDB.putListString("fav", favIds);
                    }
                });

    }

    private ArrayList<String> returnFavIds(){
        tinyDB = new TinyDB(ProductDetail.this);

        ArrayList<String> favIds;
        // Get from Tiny db
        tinyDB = new TinyDB(ProductDetail.this);
        favIds = tinyDB.getListString("fav");

        return favIds;
    }

    private void addUpdateCartItemsToPreference(CartModel cartModel, String activeCartDocumentId) {

        // Update cart list in shared preference
        // Check If item product id already exists
        Boolean exists = false;
        int itemPositionInCart = 0;

        cartModel.setCartID(activeCartDocumentId);

        for (int i = 0 ; i < cartList.size() ; i++){
            if (cartList.get(i).getProductID().equals(cartModel.getProductID())){
                exists = true;
                itemPositionInCart = i;
                break;
            }
        }

        if (exists){
            // Update item in cart list
            cartList.remove(itemPositionInCart);
            cartList.add(cartModel);
        }else {
            // Add item in cart list
            cartList.add(cartModel);
        }

        saveCartItemsToPreferences(cartList);
        TastyToast.makeText(ProductDetail.this, "Item added successfully to cart",
                TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void loadPreferenceAndView() {
        Gson gson = new Gson();

        String json = mPrefs.getString("SelectedProduct", "");
        String userJson = mPrefs.getString("User", "");

        selectedProduct = gson.fromJson(json, ProductModel.class);
        UserClass userClass = gson.fromJson(userJson, UserClass.class);
        userId = userClass.getUID();

        saveRecentProduct(selectedProduct.getProductID());

        productPrice.setText("Rs. " + (Integer.parseInt(selectedProduct.getProductPrice()) - Integer.parseInt(selectedProduct.getProductDiscountAmt())));
        productDetail.setText(selectedProduct.getDesc());
        productWeight.setText(selectedProduct.getWeight() + " " + selectedProduct.getWeightUnit());
        productTitle.setText(selectedProduct.getProductName());
        productCategory.setText(selectedProduct.getProductCat());
        companyName.setText(selectedProduct.getCompanyName());

        if (selectedProduct.getDiscount()) {
            discount.setVisibility(View.VISIBLE);
            discount.setText("Price: Rs. " + selectedProduct.getProductPrice());
            discount.setPaintFlags(discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        updateTotalAmount(1);

        Picasso.get().load(selectedProduct.getProductImage())
                .into(productIcon);

        if (returnFavIds().contains(selectedProduct.getProductID())){
            favourite.setLiked(true);
        }else {
            favourite.setLiked(false);
        }

        // Check if we have active cart id in our shared preferences data.
        activeCartDocumentId = mPrefs.getString("ActiveCartDocumentID", "");

        if (!activeCartDocumentId.equals("")){
            loadCartListFromPreference();
        }
    }

    private void saveCartItemsToPreferences(ArrayList<CartModel> cartList){
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartList);
        prefsEditor.putString("CartItems", json);
        prefsEditor.apply();
    }

    private void loadCartListFromPreference() {
        Gson gson = new Gson();

        String json = mPrefs.getString("CartItems", "");
        Type productType = new TypeToken<List<CartModel>>() {}.getType();
        cartList =  gson.fromJson(json, productType);

        countCartItems();
    }

    private void setRecentProductRecyclerData(List<GeneralProduct> list) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ProductDetail.this, LinearLayoutManager.HORIZONTAL, false);
        recentProductRecycler.setLayoutManager(layoutManager);
        generalProductAdapter = new GeneralProductAdapter(ProductDetail.this, list, this::onItemClick);
        recentProductRecycler.setAdapter(generalProductAdapter);
        recentProductRecycler.setVisibility(View.GONE);
    }

    // Calculate Amount w.r.t. to changes quantity
    private void updateTotalAmount(int quantity) {
        totalAmount.setText("Rs. " + (quantity * (
                Integer.parseInt(selectedProduct.getProductPrice()) -
                Integer.parseInt(selectedProduct.getProductDiscountAmt()))
        ));
    }

    private String getRandomCartId(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    @Override
    public void onItemClick(int position, String type, View view) {

    }

    private void countCartItems(){
        // Populate Cart List
        int cartItemsCount = 0;

        for (int i = 0 ; i < cartList.size() ; i++){
            cartItemsCount = cartItemsCount + cartList.get(i).getQuantity();
        }

        if (cartItemsCount == 0){
            cartView.setVisibility(View.GONE);
        }else {
            cartView.setVisibility(View.VISIBLE);
            cartCount.setText(String.valueOf(cartItemsCount));
        }
    }

    // save recent products from shared preferences
    private void saveRecentProduct(String productId){
        String recent = mPrefs.getString("Recent", "");
        String[] recentIds = recent.split(",");
        if (!Arrays.asList(recentIds).contains(productId)){
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            prefsEditor.putString("Recent", recent + productId + ",");
            prefsEditor.apply();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    // TODO: Modify 'YOU MAY LIKE' list
}