package com.codecanyon.onlinestore.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.RecyclerViewInterface;
import com.codecanyon.onlinestore.models.CartModel;
import com.ornach.nobobutton.NoboButton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.ViewHolder> {

    Context context;
    List<CartModel> cartList;
    private final RecyclerViewInterface recyclerViewInterface;


    public CartProductAdapter(Context context, List<CartModel> cartList,
                              RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.cartList = cartList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.bag_list_item, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(cartList.get(position).getProductImageURL())
                .into(holder.icon);
        holder.discount.setPaintFlags(holder.discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        holder.price.setText("Rs. " + (cartList.get(position).getPriceAfterDiscount()));
        holder.productName.setText(cartList.get(position).getProductName());

        holder.count.setText(String.valueOf(cartList.get(position).getQuantity()));
        holder.weight.setText("Weight: " + cartList.get(position).getWeight()
                + " " + cartList.get(position).getWeightUnit());
        holder.companyName.setText(cartList.get(position).getCompanyName());
        holder.discount.setText("Rs. " + (cartList.get(position).getPriceAfterDiscount() + cartList.get(position).getDiscountAmount()));
        holder.discount.setPaintFlags(holder.discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView count;
        NoboButton add;
        NoboButton less;
        TextView weight;
        TextView companyName;
        TextView discount;
        TextView price;
        TextView productName;
        ImageView icon;

        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            count = itemView.findViewById(R.id.count);
            add = itemView.findViewById(R.id.add);
            less = itemView.findViewById(R.id.less);
            weight = itemView.findViewById(R.id.weight);
            companyName = itemView.findViewById(R.id.companyName);
            discount = itemView.findViewById(R.id.discount);

            price = itemView.findViewById(R.id.price);

            productName = itemView.findViewById(R.id.productName);

            icon = itemView.findViewById(R.id.shoeImage);

            itemView.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position, "Cart", itemView);
                    }
                }
            });

            add.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position, "Cart", add);
                    }
                }
            });

            less.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position, "Cart", less);
                    }
                }
            });

        }
    }
}
