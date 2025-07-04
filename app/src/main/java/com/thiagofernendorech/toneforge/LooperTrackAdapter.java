package com.thiagofernendorech.toneforge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LooperTrackAdapter extends RecyclerView.Adapter<LooperTrackAdapter.TrackViewHolder> {
    
    private List<LooperTrack> tracks = new ArrayList<>();
    private OnTrackActionListener listener;
    
    public interface OnTrackActionListener {
        void onTrackVolumeChanged(int trackIndex, float volume);
        void onTrackMuteToggled(int trackIndex, boolean muted);
        void onTrackSoloToggled(int trackIndex, boolean soloed);
        void onTrackDeleted(int trackIndex);
    }
    
    public static class LooperTrack {
        public int id;
        public String name;
        public float volume = 1.0f;
        public boolean muted = false;
        public boolean soloed = false;
        public int length = 0; // em samples
        
        public LooperTrack(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    
    public LooperTrackAdapter(OnTrackActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.looper_track_item, parent, false);
        return new TrackViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        LooperTrack track = tracks.get(position);
        holder.bind(track, position);
    }
    
    @Override
    public int getItemCount() {
        return tracks.size();
    }
    
    public void addTrack(LooperTrack track) {
        tracks.add(track);
        notifyItemInserted(tracks.size() - 1);
    }
    
    public void removeTrack(int position) {
        if (position >= 0 && position < tracks.size()) {
            tracks.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void updateTrack(int position, LooperTrack track) {
        if (position >= 0 && position < tracks.size()) {
            tracks.set(position, track);
            notifyItemChanged(position);
        }
    }
    
    public List<LooperTrack> getTracks() {
        return tracks;
    }
    
    public void clearTracks() {
        tracks.clear();
        notifyDataSetChanged();
    }
    
    class TrackViewHolder extends RecyclerView.ViewHolder {
        private TextView trackNumber;
        private TextView trackName;
        private SeekBar trackVolume;
        private Button trackMuteButton;
        private Button trackSoloButton;
        private Button trackDeleteButton;
        
        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackNumber = itemView.findViewById(R.id.trackNumber);
            trackName = itemView.findViewById(R.id.trackName);
            trackVolume = itemView.findViewById(R.id.trackVolume);
            trackMuteButton = itemView.findViewById(R.id.trackMuteButton);
            trackSoloButton = itemView.findViewById(R.id.trackSoloButton);
            trackDeleteButton = itemView.findViewById(R.id.trackDeleteButton);
        }
        
        public void bind(LooperTrack track, int position) {
            trackNumber.setText(String.valueOf(track.id));
            trackName.setText(track.name);
            
            // Configurar volume
            trackVolume.setProgress((int)(track.volume * 100));
            trackVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && listener != null) {
                        listener.onTrackVolumeChanged(position, progress / 100.0f);
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            
            // Configurar botão mute
            updateMuteButton(track.muted);
            trackMuteButton.setOnClickListener(v -> {
                track.muted = !track.muted;
                updateMuteButton(track.muted);
                if (listener != null) {
                    listener.onTrackMuteToggled(position, track.muted);
                }
            });
            
            // Configurar botão solo
            updateSoloButton(track.soloed);
            trackSoloButton.setOnClickListener(v -> {
                track.soloed = !track.soloed;
                updateSoloButton(track.soloed);
                if (listener != null) {
                    listener.onTrackSoloToggled(position, track.soloed);
                }
            });
            
            // Configurar botão deletar
            trackDeleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackDeleted(position);
                }
            });
        }
        
        private void updateMuteButton(boolean muted) {
            if (muted) {
                trackMuteButton.setBackgroundTintList(itemView.getContext()
                        .getColorStateList(R.color.accent_red));
            } else {
                trackMuteButton.setBackgroundTintList(itemView.getContext()
                        .getColorStateList(R.color.green));
            }
        }
        
        private void updateSoloButton(boolean soloed) {
            if (soloed) {
                trackSoloButton.setBackgroundTintList(itemView.getContext()
                        .getColorStateList(R.color.accent_blue));
            } else {
                trackSoloButton.setBackgroundTintList(itemView.getContext()
                        .getColorStateList(R.color.dark_gray));
            }
        }
    }
} 