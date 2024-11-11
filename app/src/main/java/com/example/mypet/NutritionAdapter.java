package com.example.mypet;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NutritionAdapter extends RecyclerView.Adapter<NutritionAdapter.MyViewHolder> {

    ArrayList<String> arrayList;
    Context context;

    public NutritionAdapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public NutritionAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.nutrition_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutritionAdapter.MyViewHolder holder, int position) {
        holder.textView.setText(arrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.newFeedback);
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                Intent intent = new Intent(context, NutritionDetails.class);
                String value = arrayList.get(position);
                intent.putExtra("key", value);
                context.startActivity(intent);
            });
        }
    }
}
