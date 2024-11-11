package com.example.mypet;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private ArrayList<Pet> petList;
    private Context context;

    public PetAdapter(ArrayList<Pet> petList, Context context) {
        this.petList = petList;
        this.context = context;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);

        Picasso.with(context).load(pet.getImageUrl()).into(holder.petImageButton);

        holder.petImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditPetActivity.class);
                intent.putExtra("petId", pet.getPetId());
                intent.putExtra("petName", pet.getName());
                intent.putExtra("petGender", pet.getGender());
                intent.putExtra("petAge", pet.getAge());
                intent.putExtra("petBreed", pet.getBreed());
                intent.putExtra("petImageUrl", pet.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {

        public ImageButton petImageButton;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImageButton = itemView.findViewById(R.id.petImageButton);
        }
    }
}
