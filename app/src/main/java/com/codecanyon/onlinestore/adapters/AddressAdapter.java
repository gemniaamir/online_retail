package com.codecanyon.onlinestore.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.RecyclerViewInterface;
import com.codecanyon.onlinestore.models.AddressModel;
import com.ornach.nobobutton.NoboButton;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    Context context;
    List<AddressModel> addresses;
    RecyclerViewInterface recyclerViewInterface;
    public String[] mColors = {"#804080","#F5B48C","#E66167","#CBCFF2", "#A7D9DB", "#CACDF3"};


    public AddressAdapter(Context context, List<AddressModel> cartList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.addresses = cartList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.address_list_item, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (addresses.get(position).getAddressType().equals("Home")){
            holder.icon.setBackground(context.getDrawable(R.drawable.house_icon_2));
        }else if (addresses.get(position).getAddressType().equals("Office")){
            holder.icon.setBackground(context.getDrawable(R.drawable.office_icon));
        } else {
            holder.icon.setBackground(context.getDrawable(R.drawable.circle_wh));
        }

        holder.streetHouseNearby.setText("House # " + addresses.get(position).getHouseNo()
                + ", Street # " + addresses.get(position).getStreet()
                + "Near " + addresses.get(position).getNearBy());
        holder.areaName.setText(addresses.get(position).getAreaName());
        holder.icon.setText(String.valueOf(addresses.get(position).getAreaName().charAt(0)));

        holder.icon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(mColors[position % 6])));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon;
        TextView streetHouseNearby, areaName;
        NoboButton delete;

        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            icon = itemView.findViewById(R.id.icon);
            streetHouseNearby = itemView.findViewById(R.id.houseStreetNearby);
            areaName = itemView.findViewById(R.id.areaName);
            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position, "view", itemView);
                    }
                }
            });

            delete.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position, "delete", delete);
                    }
                }
            });

        }
    }
}
