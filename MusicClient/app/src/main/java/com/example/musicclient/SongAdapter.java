package com.example.musicclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private ArrayList<String> mp3s;
    private ArrayList<String> titles;
    private ArrayList<String> authors;
    private ArrayList<Bitmap> images;
    public MediaPlayer mPlayer;

    public SongAdapter(ArrayList<String> titles,
                       ArrayList<String> mp3s,
                       ArrayList<String> authors,
                       ArrayList<Bitmap> images){

        this.titles = titles;
        this.mp3s = mp3s;
        this.authors = authors;
        this.images = images;
        mPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View listView = inflater.inflate(R.layout.song_item, parent, false);
        return new ViewHolder(listView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.author.setText(authors.get(position));
        holder.title.setText(titles.get(position));
        holder.image.setImageBitmap(images.get(position));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView title;
        public TextView author;
        public ImageView image;
        public View listView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            author = (TextView) itemView.findViewById(R.id.author);
            image = (ImageView) itemView.findViewById(R.id.imageView);

            this.listView = itemView;
            listView.setOnClickListener(this); //set short click listener
        }

        // for handling onclick events for recycler view items
        @Override
        public void onClick(View v) {
            // setting up media player
            mPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            try {
                // if media player already reset it for next song
                if(mPlayer.isPlaying()){
                    mPlayer.stop();
                    mPlayer.reset();
                }
                // get the mp3 url and set it to media player for playing
                mPlayer.setDataSource(mp3s.get(getAdapterPosition()));
                mPlayer.prepare();
                mPlayer.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
