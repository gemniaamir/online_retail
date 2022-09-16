package com.codecanyon.onlinestore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.RecyclerViewInterface;
import com.codecanyon.onlinestore.models.PlacedOrderDetails;
import com.ornach.nobobutton.NoboButton;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    Context context;
    List<PlacedOrderDetails> orders;
    RecyclerViewInterface recyclerViewInterface;


    public OrderHistoryAdapter(Context context, List<PlacedOrderDetails> orders, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.orders = orders;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.order_list_item, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.paymentType.setText("Payment Type: "+ orders.get(position).getPaymentType());
        holder.orderId.setText(orders.get(position).getID());
        holder.orderDate.setText("Placed On: "+ orders.get(position).getOrderDateTime());
        holder.netAmount.setText("Order Amount: "+ orders.get(position).getNetAmountTotal() + " Rs.");
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, netAmount, paymentType, orderDate;
        NoboButton view;

        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            orderId = itemView.findViewById(R.id.orderID);
            netAmount = itemView.findViewById(R.id.totalAmount);
            orderDate = itemView.findViewById(R.id.orderDate);
            paymentType = itemView.findViewById(R.id.paymentType);
            view = itemView.findViewById(R.id.view);

            view.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position, "view", view);
                    }
                }
            });
        }
    }
}
