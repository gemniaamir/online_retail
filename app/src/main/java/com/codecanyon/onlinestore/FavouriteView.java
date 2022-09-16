package com.codecanyon.onlinestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.adapters.FavProductAdapter;
import com.codecanyon.onlinestore.models.GeneralProduct;
import com.codecanyon.onlinestore.models.ProductModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavouriteView extends AppCompatActivity implements RecyclerViewInterface {

    FavProductAdapter favProductAdapter;
    RecyclerView resultsRecycler;
    ImageButton back;
    ImageView gotoCart;
    SharedPreferences mPrefs;

    // Firestore DB and Reference
    ArrayList<ProductModel> totalProducts = new ArrayList<>();
    ArrayList<GeneralProduct> favouriteProducts = new ArrayList<>();
    RelativeLayout noFavouriteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_view);

        resultsRecycler = findViewById(R.id.resultRecycler);
        back = findViewById(R.id.backButton);
        gotoCart = findViewById(R.id.gotoCart);
        noFavouriteView = findViewById(R.id.noFavouriteView);

        mPrefs = getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        loadPreference();

        back.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        gotoCart.setOnClickListener(view -> {
            startActivity(new Intent(FavouriteView.this, CartList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });
    }

    private void setRecyclerData(List<GeneralProduct> list) {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        resultsRecycler.setLayoutManager(layoutManager);
        resultsRecycler.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(16), true));
        resultsRecycler.setItemAnimator(new DefaultItemAnimator());
        favProductAdapter = new FavProductAdapter(FavouriteView.this, list, this::onItemClick);
        resultsRecycler.setAdapter(favProductAdapter);
    }

    @Override
    public void onItemClick(int position, String type, View View) {
        if (type.equals("Remove")){
            TinyDB tinyDB = new TinyDB(FavouriteView.this);
            // Make it un-favourite inside shared preference
            ArrayList<String> favIds = returnFavIds();
            favIds.remove(favouriteProducts.get(position).getId());
            tinyDB.putListString("fav", favIds);

            favouriteProducts.remove(position);
            favProductAdapter.notifyDataSetChanged();

            if (favouriteProducts.size() == 0){
                noFavouriteView.setVisibility(View.VISIBLE);
            }

        }else {
            ProductModel product = new ProductModel();

            for (int i = 0; i < totalProducts.size(); i++) {
                if (favouriteProducts.get(position).getId()
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

            startActivity(new Intent(FavouriteView.this, ProductDetail.class));

        }
    }

    // now we need some item decoration class for manage spacing

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

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

    private void loadPreference() {
        Gson gson = new Gson();

        String json = mPrefs.getString("TotalProducts", "");
        Type productType = new TypeToken<List<ProductModel>>() {}.getType();

        totalProducts = gson.fromJson(json, productType);

        ArrayList<String> favIDs = returnFavIds();

        for (int i = 0; i < totalProducts.size(); i++) {
            if ( favIDs.contains(totalProducts.get(i).getProductID())){
                GeneralProduct product = new GeneralProduct(
                        "Rs. " + totalProducts.get(i).getProductPrice(),
                        totalProducts.get(i).getProductName(),
                        totalProducts.get(i).getWeight() + " " + totalProducts.get(i).getWeightUnit(),
                        totalProducts.get(i).getWeight(),
                        0, totalProducts.get(i).getProductImage());
                product.setId(totalProducts.get(i).getProductID());
                favouriteProducts.add(product);
            }
        }

        if (favIDs.size() == 0){
            noFavouriteView.setVisibility(View.VISIBLE);
        }

        setRecyclerData(favouriteProducts);
    }

    private ArrayList<String> returnFavIds(){
        ArrayList<String> favIds;
        // Get from Tiny db
        TinyDB tinyDB = new TinyDB(FavouriteView.this);
        favIds = tinyDB.getListString("fav");

        return favIds;
    }

}