package com.sikderithub.facebookvideodownloader.adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sikderithub.facebookvideodownloader.Database;
import com.sikderithub.facebookvideodownloader.R;
import com.sikderithub.facebookvideodownloader.models.FVideo;

import java.io.File;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private final ItemClickListener itemClickListener;
    private List<FVideo> videos;
    private final Context context;

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

    class ListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        TextView tvVideoTitle;
        TextView tvVideoState;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            tvVideoTitle = itemView.findViewById(R.id.tv_video_title);
            tvVideoState = itemView.findViewById(R.id.tv_video_state);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            FVideo video = videos.get(getAdapterPosition());
            itemClickListener.onItemClickListener(video);
        }

        @Override
        public boolean onLongClick(View v) {
            FVideo video = videos.get(getAdapterPosition());

            File file = new File(video.getFileUri());

            if (video.getState() == FVideo.COMPLETE) {
                if (file.exists()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Want to delete this video?")
                            .setMessage("This will delete video form your memory")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    boolean isDeleted = file.delete();
                                    if (isDeleted)
                                        Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show();
                                    Database.deleteAVideo(video.getDownloadId());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
                else {
                    Database.deleteAVideo((video.getDownloadId()));
                }
            }
            return true;
        }
    }

}
