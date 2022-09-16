package com.codecanyon.onlinestore.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.CartList;
import com.codecanyon.onlinestore.GlobalVariable;
import com.codecanyon.onlinestore.ProductDetail;
import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.RecyclerViewInterface;
import com.codecanyon.onlinestore.TinyDB;
import com.codecanyon.onlinestore.adapters.FavProductAdapter;
import com.codecanyon.onlinestore.databinding.FragmentHomeBinding;
import com.codecanyon.onlinestore.models.GeneralProduct;
import com.codecanyon.onlinestore.models.ProductModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements RecyclerViewInterface {

    private FragmentHomeBinding binding;

    FavProductAdapter favProductAdapter;
    RecyclerView resultsRecycler;
    ImageView gotoCart;
    SharedPreferences mPrefs;

    // Firestore DB and Reference
    ArrayList<ProductModel> totalProducts = new ArrayList<>();
    ArrayList<GeneralProduct> favouriteProducts = new ArrayList<>();
    RelativeLayout noFavouriteView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        resultsRecycler = root.findViewById(R.id.resultRecycler);
        gotoCart = root.findViewById(R.id.gotoCart);
        noFavouriteView = root.findViewById(R.id.noFavouriteView);

        mPrefs = getActivity().getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        loadPreference();

        gotoCart.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), CartList.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setRecyclerData(List<GeneralProduct> list) {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        resultsRecycler.setLayoutManager(layoutManager);
        resultsRecycler.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(16), true));
        resultsRecycler.setItemAnimator(new DefaultItemAnimator());
        favProductAdapter = new FavProductAdapter(getContext(), list, this::onItemClick);
        resultsRecycler.setAdapter(favProductAdapter);
    }

    @Override
    public void onItemClick(int position, String type, View View) {
        if (type.equals("Remove")){
            TinyDB tinyDB = new TinyDB(getContext());
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

            startActivity(new Intent(getContext(), ProductDetail.class));

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
        TinyDB tinyDB = new TinyDB(getContext());
        favIds = tinyDB.getListString("fav");

        return favIds;
    }

}