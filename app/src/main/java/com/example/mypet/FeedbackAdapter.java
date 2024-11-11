package com.example.mypet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.MyViewHolder> {

    ArrayList<FeedbackItem> arrayList;
    Context context;

    public FeedbackAdapter(ArrayList<FeedbackItem> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public FeedbackAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.feedback_list, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdapter.MyViewHolder holder, int position) {
        FeedbackItem feedbackItem = arrayList.get(position);
        holder.textView.setText(feedbackItem.getFeedbackText());

        // Load image using Picasso from Firebase Storage URL
        if (feedbackItem.getFeedbackImageUri() != null) {
            Picasso.with(context).load(feedbackItem.getFeedbackImageUri()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.icons8_camera_50); // Default image if no image URL is available
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.newFeedback);
            imageView = itemView.findViewById(R.id.feedbackPic);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                String feedbackKey = arrayList.get(position).getFeedbackKey();
                Intent intent = new Intent(context, FeedbackDetails.class);
                intent.putExtra("key", feedbackKey);
                context.startActivity(intent);
            });
        }
    }

}
