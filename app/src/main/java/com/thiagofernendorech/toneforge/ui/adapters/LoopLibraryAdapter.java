package com.thiagofernendorech.toneforge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoopLibraryAdapter extends RecyclerView.Adapter<LoopLibraryAdapter.LoopViewHolder> {
    
    private List<LoopLibraryManager.LoopInfo> loops = new ArrayList<>();
    private OnLoopActionListener actionListener;
    
    public interface OnLoopActionListener {
        void onLoopSelected(LoopLibraryManager.LoopInfo loop);
        void onLoopDeleted(LoopLibraryManager.LoopInfo loop);
        void onLoopRenamed(LoopLibraryManager.LoopInfo loop);
    }
    
    public LoopLibraryAdapter(OnLoopActionListener listener) {
        this.actionListener = listener;
    }
    
    public void updateLoops(List<LoopLibraryManager.LoopInfo> newLoops) {
        this.loops = newLoops;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public LoopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.loop_library_item, parent, false);
        return new LoopViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LoopViewHolder holder, int position) {
        LoopLibraryManager.LoopInfo loop = loops.get(position);
        holder.bind(loop);
    }
    
    @Override
    public int getItemCount() {
        return loops.size();
    }
    
    class LoopViewHolder extends RecyclerView.ViewHolder {
        private TextView loopName;
        private TextView loopDuration;
        private TextView loopDate;
        private TextView loopSize;
        private ImageButton deleteButton;
        private ImageButton renameButton;
        
        LoopViewHolder(@NonNull View itemView) {
            super(itemView);
            loopName = itemView.findViewById(R.id.loopName);
            loopDuration = itemView.findViewById(R.id.loopDuration);
            loopDate = itemView.findViewById(R.id.loopDate);
            loopSize = itemView.findViewById(R.id.loopSize);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            renameButton = itemView.findViewById(R.id.renameButton);
            
            // Configurar click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && actionListener != null) {
                    actionListener.onLoopSelected(loops.get(position));
                }
            });
            
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && actionListener != null) {
                    actionListener.onLoopDeleted(loops.get(position));
                }
            });
            
            renameButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && actionListener != null) {
                    actionListener.onLoopRenamed(loops.get(position));
                }
            });
        }
        
        void bind(LoopLibraryManager.LoopInfo loop) {
            loopName.setText(loop.displayName);
            loopDuration.setText(loop.durationFormatted);
            
            // Formatar data
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(loop.creationTime));
            loopDate.setText(dateStr);
            
            // Formatar tamanho do arquivo
            String sizeStr = formatFileSize(loop.fileSize);
            loopSize.setText(sizeStr);
        }
        
        private String formatFileSize(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            } else if (bytes < 1024 * 1024) {
                return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
            } else {
                return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
            }
        }
    }
} 