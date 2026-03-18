package com.prog.queued;

import android.graphics.Color;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ShowViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Show show);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Show show, View anchorView);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Show show);
    }

    private final List<Show> shows;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;

    public ShowAdapter(List<Show> shows) {
        this.shows = shows;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.onFavoriteClickListener = listener;
    }

    @NonNull
    @Override
    public ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.show_list_item, parent, false);
        return new ShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowViewHolder holder, int position) {
        Show show = shows.get(position);

        if (show.getImageUrl() != null && !show.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(show.getImageUrl())
                    .placeholder(R.drawable.default_image)
                    .into(holder.showImage);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(show.getImageResourceId())
                    .into(holder.showImage);
        }
        holder.showTitle.setText(show.getTitle());
        holder.showRating.setText(show.getRating());
        holder.showStatus.setText(show.getStatus());

        holder.showFavorite.setText(show.isFavorite() ? "★" : "☆");
        holder.showFavorite.setTextColor(show.isFavorite()
                ? Color.parseColor("#FFC107")
                : Color.parseColor("#AAAAAA"));

        switch (show.getStatus()) {
            case "Watching":
                holder.showStatus.setTextColor(Color.parseColor("#1565C0"));
                break;
            case "Completed":
                holder.showStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "Dropped":
                holder.showStatus.setTextColor(Color.parseColor("#C62828"));
                break;
            default:
                holder.showStatus.setTextColor(Color.parseColor("#757575"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(show);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(show, v);
                return true;
            }
            return false;
        });

        holder.showFavorite.setOnClickListener(v -> {
            if (onFavoriteClickListener != null) onFavoriteClickListener.onFavoriteClick(show);
        });
    }

    @Override
    public int getItemCount() {
        return shows.size();
    }

    static class ShowViewHolder extends RecyclerView.ViewHolder {
        ImageView showImage;
        TextView showTitle;
        TextView showStatus;
        TextView showRating;
        TextView showFavorite;

        ShowViewHolder(@NonNull View itemView) {
            super(itemView);
            showImage = itemView.findViewById(R.id.show_image);
            showTitle = itemView.findViewById(R.id.show_title);
            showStatus = itemView.findViewById(R.id.show_status);
            showRating = itemView.findViewById(R.id.show_rating);
            showFavorite = itemView.findViewById(R.id.show_favorite);
        }
    }
}
