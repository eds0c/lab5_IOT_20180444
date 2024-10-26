package com.example.lab5_iot_20180444.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_iot_20180444.databinding.ItemFoodBinding;
import com.example.lab5_iot_20180444.entity.FoodItem;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder>{
    private List<FoodItem> foodList;
    private OnFoodItemDeleteListener deleteListener;

    public interface OnFoodItemDeleteListener {
        void onFoodItemDeleted(int position, int calorias);
    }

    public FoodAdapter(List<FoodItem> foodList, OnFoodItemDeleteListener listener) {
        this.foodList = foodList;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //uutilizando View Binding para inflar el layout
        ItemFoodBinding binding = ItemFoodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem foodItem = foodList.get(position);
        holder.binding.tvNombreComida.setText(foodItem.getNombre());

        holder.binding.tvCaloriasComida.setText(foodItem.getCalorias() + " kcal");

        holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteListener != null) {
                    deleteListener.onFoodItemDeleted(holder.getAdapterPosition(), foodItem.getCalorias());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    private int calcularCaloriasAcumuladasHasta(int position) {
        int totalCalorias = 0;
        for (int i = 0; i <= position; i++) {
            totalCalorias += foodList.get(i).getCalorias();
        }
        return totalCalorias;
    }

    public void removeItem(int position) {
        foodList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, foodList.size());
    }

    //viewHolder usando View Binding
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemFoodBinding binding;

        public FoodViewHolder(ItemFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
