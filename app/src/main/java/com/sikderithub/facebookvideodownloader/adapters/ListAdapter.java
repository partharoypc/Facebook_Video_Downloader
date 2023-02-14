package com.sikderithub.facebookvideodownloader.adapters;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sikderithub.facebookvideodownloader.R;
import com.sikderithub.facebookvideodownloader.models.FVideo;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private List<FVideo> videos;
    private Context context;
    private final ItemClickListener itemClickListener;

    public ListAdapter(Context context, ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.video_item_layout, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        FVideo video = videos.get(position);

        holder.tvVideoTitle.setText(video.getFileName());

        switch (video.getState()) {
            case FVideo.DOWNLOADING:
                holder.tvVideoState.setText("Downloading...");
                break;
            case FVideo.PROCESSING:
                holder.tvVideoState.setText("Processing...");
                break;
            case FVideo.COMPLETE:
                holder.tvVideoState.setText("Complete");
        }
    }

    @Override
    public int getItemCount() {
        if (videos == null)
            return 0;
        return videos.size();
    }

    public void setVideos(List<FVideo> fVideos) {
        videos = fVideos;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(FVideo video);
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvVideoTitle;
        TextView tvVideoState;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            tvVideoTitle = itemView.findViewById(R.id.tv_video_title);
            tvVideoState = itemView.findViewById(R.id.tv_video_state);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            FVideo video = videos.get(getAdapterPosition());
            itemClickListener.onItemClickListener(video);
        }
    }

}
