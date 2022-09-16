package com.codecanyon.onlinestore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.models.CartModel;

import java.util.List;

public class CartListProductAdapter extends RecyclerView.Adapter<CartListProductAdapter.ViewHolder> {

    Context context;
    List<CartModel> cartList;

    public CartListProductAdapter(Context context, List<CartModel> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.place_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.price.setText(
                cartList.get(position).getQuantity() + " x " +
                        (cartList.get(position).getPriceAfterDiscount()) + " Rs.");
        holder.title.setText(
                cartList.get(position).getProductName() + " -  "+
                cartList.get(position).getWeight() + " " +
                cartList.get(position).getWeightUnit());
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            price = itemView.findViewById(R.id.price);
            title = itemView.findViewById(R.id.title);

        }
    }
}
