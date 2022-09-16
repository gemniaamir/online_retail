package com.codecanyon.onlinestore.ui.dashboard;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.CartList;
import com.codecanyon.onlinestore.GlobalVariable;
import com.codecanyon.onlinestore.MoreCategory;
import com.codecanyon.onlinestore.ProductDetail;
import com.codecanyon.onlinestore.ProductResults;
import com.codecanyon.onlinestore.RecyclerViewInterface;
import com.codecanyon.onlinestore.adapters.CategoryProductAdapter;
import com.codecanyon.onlinestore.adapters.GeneralProductAdapter;
import com.codecanyon.onlinestore.models.CartModel;
import com.codecanyon.onlinestore.models.CategoryClass;
import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.adapters.DiscountedProductAdapter;
import com.codecanyon.onlinestore.databinding.FragmentDashboardBinding;
import com.codecanyon.onlinestore.models.ProductModel;
import com.codecanyon.onlinestore.models.GeneralProduct;
import com.codecanyon.onlinestore.models.UserClass;
import com.sdsmdg.tastytoast.TastyToast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment implements RecyclerViewInterface {

    private FragmentDashboardBinding binding;

    RecyclerView discountedRecycler, categoriesRecycler, recentProductRecycler;
    DiscountedProductAdapter discountedProductAdapter;
    CategoryProductAdapter categoryProductAdapter;
    GeneralProductAdapter generalProductAdapter;

    RelativeLayout parentView, loadingView;
    TextView seeMore;
    AutoCompleteTextView search;
    ImageView cart;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    // Discounted products from Firestore DB with isDiscount == true
    ArrayList<ProductModel> discountedProducts;
    ArrayList<ProductModel> totalProducts = new ArrayList<>();
    // Recent products from Shared Preferences
    List<GeneralProduct> recentList;
    // Categories List from FireStore DB
    ArrayList<CategoryClass> categoryList;

    SharedPreferences mPrefs;
    String[] recentIds;
    Boolean isFirstTime = true;

    String activeCartDocumentID = "";
    String userId;
    ArrayList<CartModel> cartList = new ArrayList<>();
    TextView cartCount;
    RelativeLayout cartView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mPrefs = getActivity().getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        search = root.findViewById(R.id.searchBar);
        discountedRecycler = root.findViewById(R.id.discountedRecycler);
        categoriesRecycler = root.findViewById(R.id.categoriesRecycler);
        recentProductRecycler = root.findViewById(R.id.recentRecycler);
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        cartCount = root.findViewById(R.id.cartCount);
        cartView = root.findViewById(R.id.cartView);

        seeMore = root.findViewById(R.id.textView5);
        cart = root.findViewById(R.id.cart);

        setupUI(parentView);

        // Discounted Item List
        loadProductList();

        // Category List
        loadCategoryList();

        seeMore.setOnClickListener(view -> {
            saveTotalProductsToPreferences();

            startActivity(new Intent(getContext(), MoreCategory.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });

        cartView.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), CartList.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });

        // Load preferences for getting active card it and user id
        // We will get our active cart from FireStore db
        loadPreferenceAndCart();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstTime) {
            isFirstTime = false;
        } else {
            // Recent List
            loadRecentProduct();

            activeCartDocumentID = mPrefs.getString("ActiveCartDocumentID", "");

            if (!activeCartDocumentID.equals("")){
                loadCartListFromPreference();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadCategoryList() {
        if (isNetworkAvailable()){
            // initiate document and collection references
            collectionReference = db.collection("Product Categories");
            categoryList = new ArrayList<>();

            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    CategoryClass category = documentSnapshot.toObject(CategoryClass.class);
                    category.setCatID(documentSnapshot.getId());
                    categoryList.add(category);
                }
                setCategoryRecyclerData(categoryList);

            }).addOnFailureListener(e ->
                    TastyToast.makeText(getContext(), "Products not found", TastyToast.LENGTH_LONG, TastyToast.INFO).show());
        }else {
            TastyToast.makeText(getContext(), "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
        }
    }

    private void loadProductList() {
        if (isNetworkAvailable()){
            // initiate document and collection references
            collectionReference = db.collection("Products");
            discountedProducts = new ArrayList<>();

            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    ProductModel product = documentSnapshot.toObject(ProductModel.class);
                    product.setProductID(documentSnapshot.getId());
                    if (product.getDiscount()) {
                        discountedProducts.add(product);
                    }

                    totalProducts.add(product);
                }

                // save total products for first time
                saveTotalProductsToPreferences();

                setDiscountedRecyclerData(discountedProducts);

                // Load all product title names into auto complete search bar
                ArrayList<String> names = new ArrayList<>();

                for (int i = 0; i < totalProducts.size(); i++) {
                    if (!names.contains(totalProducts.get(i).getProductName())) {
                        names.add(totalProducts.get(i).getProductName());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, names.toArray(new String[names.size()]));

                search.setThreshold(1);
                search.setAdapter(adapter);

                search.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
                    saveTotalProductsToPreferences();

                    startActivity(new Intent(getContext(), ProductResults.class).putExtra("keyword",
                            String.valueOf(adapterView.getAdapter().getItem(i))));
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                });

                // Recent List
                loadRecentProduct();

            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Products not found", Toast.LENGTH_LONG).show());
        }else {
            TastyToast.makeText(getContext(), "Internet not available", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
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
                    hideSoftKeyboard(getActivity());
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

    private void setDiscountedRecyclerData(List<ProductModel> list) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        discountedRecycler.setLayoutManager(layoutManager);
        discountedProductAdapter = new DiscountedProductAdapter(getContext(), list, this::onItemClick);
        discountedRecycler.setAdapter(discountedProductAdapter);
    }

    private void setCategoryRecyclerData(List<CategoryClass> list) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        categoriesRecycler.setLayoutManager(layoutManager);
        categoryProductAdapter = new CategoryProductAdapter(getContext(), list, this::onItemClick);
        categoriesRecycler.setAdapter(categoryProductAdapter);
    }

    private void setRecentProductRecyclerData(List<GeneralProduct> list) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recentProductRecycler.setLayoutManager(layoutManager);
        generalProductAdapter = new GeneralProductAdapter(getContext(), list, this::onItemClick);
        recentProductRecycler.setAdapter(generalProductAdapter);
    }

    @Override
    public void onItemClick(int position, String type, View view) {
        if (type.equals("Category")) {

            saveTotalProductsToPreferences();

            startActivity(new Intent(getContext(), ProductResults.class)
                    .putExtra("Category", categoryList.get(position).getCatName())
                    .putExtra("IsFromCat", true));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        } else {
            ProductModel product = new ProductModel();

            if (type.equals("Discount")) {
                product = discountedProducts.get(position);
            }

            if (type.equals("Recent")) {
                for (int i = 0; i < totalProducts.size(); i++) {
                    if (recentList.get(position).getId().equals(totalProducts.get(i).getProductID())) {
                        product = totalProducts.get(i);
                        break;
                    }
                }
            }

            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(product);
            prefsEditor.putString("SelectedProduct", json);
            prefsEditor.apply();

            startActivity(new Intent(getContext(), ProductDetail.class));
        }
    }

    // save recent products from shared preferences
    private void loadRecentProduct() {
        String recent = mPrefs.getString("Recent", "");
        recentIds = recent.split(",");
        int length = recentIds.length < 6 ? recentIds.length : 5;
        if (recentIds.length > 0) {
            recentList = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < totalProducts.size(); j++) {
                    if (recentIds[i].replace(",", "").equals(totalProducts.get(j).getProductID())) {
                        GeneralProduct generalProduct = new GeneralProduct(
                                "Rs. " + totalProducts.get(j).getProductPrice(),
                                totalProducts.get(j).getProductName(),
                                totalProducts.get(j).getWeight() + " " + totalProducts.get(j).getWeightUnit(),
                                totalProducts.get(j).getWeight(),
                                0, totalProducts.get(j).getProductImage());
                        generalProduct.setId(totalProducts.get(j).getProductID());
                        recentList.add(generalProduct);
                        break;
                    }
                }
            }
            if (recentList.size() > 0) {
                setRecentProductRecyclerData(recentList);
            }
        }
    }

    private void saveTotalProductsToPreferences() {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(totalProducts);
        prefsEditor.putString("TotalProducts", json);
        prefsEditor.apply();
    }

    private void loadPreferenceAndCart() {
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");
        UserClass userClass = gson.fromJson(userJson, UserClass.class);
        userId = userClass.getUID();

        // check weather we have an active cart or not
        // TODO Add: Check active cart id for a specific user
        activeCartDocumentID = mPrefs.getString("ActiveCartDocumentID", "");

        if (activeCartDocumentID.equals("")){
            // There is no active cart id in shared preferences
        }else {
            // If active cart exists, loading our cart from shared preference
            loadCartListFromPreference();
        }

    }

    private void saveCartItemsToPreferences(){
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

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    // TODO: minus items above 10 from recent list
}