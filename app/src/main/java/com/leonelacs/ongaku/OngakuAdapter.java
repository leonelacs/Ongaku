package com.leonelacs.ongaku;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class OngakuAdapter extends RecyclerView.Adapter<OngakuAdapter.ViewHolder> {
    private List<Ongaku> ongakus;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Ongaku ongaku);
    }

    private class MyOnClickListener implements View.OnClickListener {
        private int position;
        private Ongaku ongaku;

        public MyOnClickListener(int position, Ongaku ongaku) {
            this.position = position;
            this.ongaku = ongaku;
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(view, position, ongaku);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCover;
        TextView musicTitle;
        TextView musicArtist;
        View searchView;

        public ViewHolder(View view) {
            super(view);
            searchView = view;
            albumCover = (ImageView) view.findViewById(R.id.album_cover);
            musicTitle = (TextView) view.findViewById(R.id.music_title);
            musicArtist = (TextView) view.findViewById(R.id.music_artist);
        }
    }

    public OngakuAdapter(List<Ongaku> ongakuList) {
        ongakus = ongakuList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ongaku_square, parent, false);
        ViewHolder holder = new ViewHolder(view);

//        holder.searchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int position = holder.getAdapterPosition();
//                Ongaku ongakuBeClicked = ongakus.get(position);
//            }
//        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ongaku ongaku = ongakus.get(position);
        int adapterPosition = holder.getAdapterPosition();
        if (ongaku.getCover() != null) {
            holder.albumCover.setImageBitmap(ongaku.getCover());
        }
        else {
            holder.albumCover.setImageResource(R.drawable.default_cover_alpha_400);
        }
        holder.musicTitle.setText(ongaku.getTitle());
        holder.musicArtist.setText(ongaku.getArtist());
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new MyOnClickListener(position, ongakus.get(adapterPosition)));
        }
    }

    @Override
    public int getItemCount() {
        return ongakus.size();
    }
}
