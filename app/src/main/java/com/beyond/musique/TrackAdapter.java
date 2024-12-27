package com.beyond.musique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter class for displaying tracks in a RecyclerView.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private Context context;
    private List<Track> trackList;

    /**
     * Constructor for TrackAdapter.
     * @param context The context in which the adapter is used.
     * @param trackList The list of tracks to display.
     */
    public TrackAdapter(Context context, List<Track> trackList) {
        this.context = context;
        this.trackList = trackList;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new TrackViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_track, parent, false);
        return new TrackViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.trackName.setText((position + 1) + ". " + track.getName());
        holder.trackDuration.setText(formatDuration(track.getDuration()));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return trackList.size();
    }

    /**
     * ViewHolder class for track items.
     */
    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView trackName;
        TextView trackDuration;

        /**
         * Constructor for TrackViewHolder.
         * @param itemView The view of the track item.
         */
        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackName);
            trackDuration = itemView.findViewById(R.id.trackDuration);
        }
    }

    /**
     * Formats the duration from seconds to a "minutes:seconds" format.
     * @param durationInSeconds The duration in seconds.
     * @return The formatted duration string.
     */
    private String formatDuration(String durationInSeconds) {
        int seconds = Integer.parseInt(durationInSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}