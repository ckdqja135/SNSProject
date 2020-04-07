package com.example.snsproject.adpter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snsproject.R;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public class GallaryAdapter extends RecyclerView.Adapter<GallaryAdapter.GallaryViewHolder> {
    private ArrayList<String> mDataset;
    private Activity activity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class GallaryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;
        public GallaryViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public GallaryAdapter(Activity activity, ArrayList<String> myDataset) {
        mDataset = myDataset;
        this.activity = activity;
    }


    public GallaryAdapter(ArrayList<String> GallaryDataset) {
        mDataset = GallaryDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GallaryAdapter.GallaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallary, parent, false);

        final GallaryViewHolder gallaryViewHolder = new GallaryViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("profilePath", mDataset.get(gallaryViewHolder.getAdapterPosition()));
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            }
        });

        return gallaryViewHolder;
    }

    // 여기서 데이터가 하나하나 들어옴.
    @Override
    public void onBindViewHolder(@Nonnull final GallaryViewHolder holder, final int position) {
        CardView cardView = holder.cardView;
        ImageView imageView = holder.cardView.findViewById(R.id.imageView);
        Glide.with(activity).load(mDataset.get(position)).centerCrop().override(500).into(imageView); // 300사이즈의 이미지가 들어옴.
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

