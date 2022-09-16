package com.codecanyon.onlinestore.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.RecyclerViewInterface;
import com.codecanyon.onlinestore.models.ProductModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DiscountedProductAdapter extends RecyclerView.Adapter<DiscountedProductAdapter.DiscountedProductViewHolder> {

    Context context;
    List<ProductModel> discountedProductsList;
    public String[] mColors = {"#EBF3FF","#F5B48C","#E66167","#CBCFF2", "#A7D9DB", "#CACDF3"};
    private final RecyclerViewInterface recyclerViewInterface;

    public DiscountedProductAdapter(Context context,
                                    List<ProductModel> discountedProductsList,
                                    RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.discountedProductsList = discountedProductsList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public DiscountedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.discounted_row_items, parent, false);
        return new DiscountedProductViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountedProductViewHolder holder, int position) {
        Drawable backgroundDrawable = holder.background.getBackground();
        backgroundDrawable.setTint(Color.parseColor(mColors[position % 6]));
        holder.background.setBackground(backgroundDrawable);
        holder.title.setText(discountedProductsList.get(position).getProductName());
        holder.discount.setText( "Rs."+ (discountedProductsList.get(position).getProductDiscountAmt()) + " off");
        Picasso.get().load(discountedProductsList.get(position).getProductImage())
                .into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return discountedProductsList.size();
    }

    public static class DiscountedProductViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout background;
        ImageView icon;
        TextView title, discount;

        public DiscountedProductViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            background = itemView.findViewById(R.id.background);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            discount = itemView.findViewById(R.id.discount);

            itemView.setOnClickListener(view -> {
                if (recyclerViewInterface != null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(position, "Discount", itemView);
                    }
                }
            });

        }
    }
}
