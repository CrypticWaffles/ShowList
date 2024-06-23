package com.prog.showlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ShowAdapter extends ArrayAdapter<Show> {
    public ShowAdapter(Context context, List<Show> shows) {
        super(context, 0, shows);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.show_list_item, parent, false);
        }

        Show show = getItem(position);

        ImageView showImage = convertView.findViewById(R.id.show_image);
        showImage.setImageResource(show.getImageResourceId());

        TextView showTitle = convertView.findViewById(R.id.show_title);
        showTitle.setText(show.getTitle());

        TextView showRating = convertView.findViewById(R.id.show_rating);
        showRating.setText(show.getRating());

        return convertView;
    }
}
