package com.codecanyon.onlinestore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.GeneralProductAdapter;
import com.codecanyon.onlinestore.models.GeneralProduct;
import com.codecanyon.onlinestore.models.ProductModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProductResults extends AppCompatActivity implements RecyclerViewInterface {

    GeneralProductAdapter generalProductAdapter;
    RecyclerView resultsRecycler;
    ImageButton back;
    ImageView gotoCart;
    SharedPreferences mPrefs;
    TextView subtitle, itemsFound;

    // Firestore DB and Reference
    ArrayList<ProductModel> totalProducts = new ArrayList<>();
    ArrayList<GeneralProduct> filteredProducts = new ArrayList<>();
    String keyword = "";
    String category = "";
    Boolean isFromCategory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_result);

        resultsRecycler = findViewById(R.id.resultRecycler);
        back = findViewById(R.id.backButton);
        gotoCart = findViewById(R.id.gotoCart);
        subtitle = findViewById(R.id.subtitle);
        itemsFound = findViewById(R.id.itemsFound);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);
        keyword = getIntent().getExtras().getString("keyword");
        isFromCategory = getIntent().getExtras().getBoolean("IsFromCat", false);
        category = getIntent().getExtras().getString("Category", "");

        if (isFromCategory) {
            // Load data based on category
            subtitle.setText("\'" + category + "\' " + subtitle.getText().toString().trim());
            loadPreference(false, category);
        } else {
            // Load data based on keyword
            subtitle.setText("\'" + keyword + "\' " + subtitle.getText().toString().trim());
            loadPreference(true, keyword);
        }

        back.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        gotoCart.setOnClickListener(view -> {
            startActivity(new Intent(ProductResults.this, CartList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });
    }

    private void setRecyclerData(List<GeneralProduct> list) {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        resultsRecycler.setLayoutManager(layoutManager);
        resultsRecycler.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(16), true));
        resultsRecycler.setItemAnimator(new DefaultItemAnimator());
        generalProductAdapter = new GeneralProductAdapter(ProductResults.this, list, this::onItemClick);
        resultsRecycler.setAdapter(generalProductAdapter);
    }

    @Override
    public void onItemClick(int position, String type, View View) {
        ProductModel product = new ProductModel();

        for (int i = 0; i < totalProducts.size(); i++) {
            if (filteredProducts.get(position).getId()
                    .equals(totalProducts.get(i).getProductID())) {
                product = totalProducts.get(i);
                break;
            }
        }

        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(product);
        prefsEditor.putString("SelectedProduct", json);
        prefsEditor.apply();

        startActivity(new Intent(ProductResults.this, ProductDetail.class));
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

    private void loadPreference(Boolean isKeyword, String filterString) {
        Gson gson = new Gson();

        String json = mPrefs.getString("TotalProducts", "");
        Type productType = new TypeToken<List<ProductModel>>() {
        }.getType();

        totalProducts = gson.fromJson(json, productType);

        for (int i = 0; i < totalProducts.size(); i++) {
            if (isKeyword ?
                    totalProducts.get(i).getProductName().equals(filterString) :
                    totalProducts.get(i).getProductCat().equals(filterString)) {

                GeneralProduct product = new GeneralProduct(
                        "Rs. " + totalProducts.get(i).getProductPrice(),
                        totalProducts.get(i).getProductName(),
                        totalProducts.get(i).getWeight() + " " + totalProducts.get(i).getWeightUnit(),
                        totalProducts.get(i).getWeight(),
                        0, totalProducts.get(i).getProductImage());
                product.setId(totalProducts.get(i).getProductID());
                filteredProducts.add(product);
            }
        }
        itemsFound.setText("(" + filteredProducts.size() + ")" + " Items found");
        setRecyclerData(filteredProducts);
    }

}