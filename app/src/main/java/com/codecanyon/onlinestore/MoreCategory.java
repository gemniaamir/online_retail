package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.CategoryProductAdapter;
import com.codecanyon.onlinestore.models.CategoryClass;
import com.codecanyon.onlinestore.models.ProductModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MoreCategory extends AppCompatActivity implements RecyclerViewInterface{

    CategoryProductAdapter categoryProductAdapter;
    RecyclerView categoriesRecycler;
    ImageButton back;
    ImageView gotoCart;

    // Firestore DB and Reference
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    ArrayList<ProductModel> totalProducts;
    ArrayList<CategoryClass> list;
    SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_category);
        categoriesRecycler = findViewById(R.id.categoriesRecycler);
        back = findViewById(R.id.backButton);
        gotoCart = findViewById(R.id.gotoCart);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        // Category List
        loadCategoryList();
        // Load total products from shared preferences
        loadTotalProductsFromSharedPreference();

        back.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
        });

        gotoCart.setOnClickListener(view -> {
            startActivity(new Intent(MoreCategory.this, CartList.class));
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });
    }

    private void loadCategoryList() {
        if (isNetworkAvailable()){
            // initiate document and collection references
            collectionReference = db.collection("Product Categories");
            list = new ArrayList<>();

            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    CategoryClass category = documentSnapshot.toObject(CategoryClass.class);
                    category.setCatID(documentSnapshot.getId());
                    list.add(category);
                }
                setCategoryRecyclerData(list);

            }).addOnFailureListener(e -> Toast.makeText(MoreCategory.this, "Products not found", Toast.LENGTH_LONG).show());
        }
    }

    private void setCategoryRecyclerData(List<CategoryClass> list) {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        categoriesRecycler.setLayoutManager(layoutManager);
        categoriesRecycler.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(16), true));
        categoriesRecycler.setItemAnimator(new DefaultItemAnimator());
        categoryProductAdapter = new CategoryProductAdapter(MoreCategory.this, list, this::onItemClick);
        categoriesRecycler.setAdapter(categoryProductAdapter);
    }

    @Override
    public void onItemClick(int position, String type, View View) {

        saveTotalProductsToPreferences();

        startActivity(new Intent(this, ProductResults.class)
                .putExtra("IsFromCat", true)
                .putExtra("Category", list.get(position).getCatName()));
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
    }

    // now we need some item decoration class for manage spacing

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void loadTotalProductsFromSharedPreference(){
        Gson gson = new Gson();

        String json = mPrefs.getString("TotalProducts", "");
        Type productType = new TypeToken<List<ProductModel>>() {}.getType();

        totalProducts =  gson.fromJson(json, productType);
    }

    private void saveTotalProductsToPreferences(){
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(totalProducts);
        prefsEditor.putString("TotalProducts", json);
        prefsEditor.apply();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}