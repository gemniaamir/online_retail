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
import com.codecanyon.onlinestore.models.GeneralProduct;
import com.ornach.nobobutton.NoboButton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavProductAdapter extends RecyclerView.Adapter<FavProductAdapter.ViewHolder> {

    Context context;
    List<GeneralProduct> generalProductList;
    public String[] mColors = {"#A8E6CF","#DCEDC1","#FFD3B6","#FF8B94", "#FF8B94", "#C1C1C1"};
    private final RecyclerViewInterface recyclerViewInterface;


    public FavProductAdapter(Context context, List<GeneralProduct> generalProductList , RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.generalProductList = generalProductList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fav_row_items, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable backgroundDrawable = holder.background.getBackground();
        backgroundDrawable.setTint(Color.parseColor(mColors[position % 6]));
        holder.background.setBackground(backgroundDrawable);
        holder.title.setText(generalProductList.get(position).getTitle());
        holder.price.setText(generalProductList.get(position).getPrice());
        holder.unit.setText(generalProductList.get(position).getUnit());
        holder.weight.setText(generalProductList.get(position).getWeight());
        if (generalProductList.get(position).getBackgroundColor() != 0){
            holder.icon.setImageResource(generalProductList.get(position).getBackgroundColor());
        }
        if (!generalProductList.get(position).getBackgroundImage().equals("")){
            Picasso.get().load(generalProductList.get(position).getBackgroundImage())
                    .into(holder.icon);
        }

    }

    @Override
    public int getItemCount() {
        return generalProductList.size();
    }

    public static class ViewHolder extends  RecyclerView.ViewHolder{

        RelativeLayout background, rootView;
        ImageView icon, addCart;
        TextView title, price, unit, weight;
        NoboButton remove;

        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            background = itemView.findViewById(R.id.background);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            unit = itemView.findViewById(R.id.unit);
            addCart = itemView.findViewById(R.id.addCart);
            rootView = itemView.findViewById(R.id.rootView);
            weight = itemView.findViewById(R.id.weight);
            remove = itemView.findViewById(R.id.remove);

            itemView.setOnClickListener(view -> {
                if (recyclerViewInterface != null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(position, "Fav", itemView);
                    }
                }
            });

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(position, "Remove", remove);
                        }
                    }
                }
            });
        }
    }
}
