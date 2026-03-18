package com.prog.queued;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TvMazeShow show);
    }

    private final List<TvMazeShow> results;
    private OnItemClickListener onItemClickListener;

    public SearchResultAdapter(List<TvMazeShow> results) {
        this.results = results;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TvMazeShow show = results.get(position);

        holder.title.setText(show.getName());
        holder.rating.setText(show.getRating() > 0
                ? show.getRating() + "/10"
                : "No rating");
        holder.summary.setText(show.getSummary().isEmpty()
                ? "No description available."
                : show.getSummary());

        if (show.getImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(show.getImageUrl())
                    .placeholder(R.drawable.default_image)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.default_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(show);
        });
    }

    @Override
    public int getItemCount() { return results.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, rating, summary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.result_image);
            title = itemView.findViewById(R.id.result_title);
            rating = itemView.findViewById(R.id.result_rating);
            summary = itemView.findViewById(R.id.result_summary);
        }
    }
}
