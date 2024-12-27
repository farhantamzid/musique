package com.beyond.musique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter class for displaying reviews in a RecyclerView.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context context;
    private List<Review> reviewList;

    /**
     * Constructor for ReviewAdapter.
     * @param context The context in which the adapter is used.
     * @param reviewList The list of reviews to display.
     */
    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ReviewViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.tvReviewerName.setText(review.getUserName());
        holder.ratingBarReview.setRating(review.getRating());
        holder.tvReviewText.setText(review.getReviewText());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    /**
     * ViewHolder class for review items.
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName;
        RatingBar ratingBarReview;
        TextView tvReviewText;

        /**
         * Constructor for ReviewViewHolder.
         * @param itemView The view of the review item.
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
        }
    }
}