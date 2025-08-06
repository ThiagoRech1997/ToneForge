package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.List;

/**
 * Adaptador para a pedaleira que exibe efeitos como pedais reais
 * Cada pedal tem visual autêntico com knobs, LEDs e switches
 */
public class PedalboardAdapter extends RecyclerView.Adapter<PedalboardAdapter.PedalViewHolder> {
    
    private final Context context;
    private final List<PedalEffect> pedals;
    private final PedalInteractionListener listener;
    
    /**
     * Interface para interações com os pedais
     */
    public interface PedalInteractionListener {
        void onPedalEnabled(String effectName, boolean enabled);
        void onPedalParameterChanged(String effectName, String parameter, float value);
        void onPedalOrderChanged(List<String> newOrder);
        void onPedalClicked(String effectName);
        void onPedalLongClicked(String effectName);
    }
    
    public PedalboardAdapter(Context context, List<PedalEffect> pedals, PedalInteractionListener listener) {
        this.context = context;
        this.pedals = pedals;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PedalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pedal_effect_item, parent, false);
        return new PedalViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PedalViewHolder holder, int position) {
        PedalEffect pedal = pedals.get(position);
        holder.bind(pedal, listener);
    }
    
    @Override
    public int getItemCount() {
        return pedals.size();
    }
    
    /**
     * Move pedal de uma posição para outra
     */
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 || 
            fromPosition >= pedals.size() || toPosition >= pedals.size()) {
            return;
        }
        
        Collections.swap(pedals, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        
        // Notificar mudança na ordem
        if (listener != null) {
            List<String> newOrder = new java.util.ArrayList<>();
            for (PedalEffect pedal : pedals) {
                newOrder.add(pedal.getName());
            }
            listener.onPedalOrderChanged(newOrder);
        }
    }
    
    /**
     * Atualiza o estado de um pedal específico
     */
    public void updatePedal(String effectName, boolean enabled, float mainValue) {
        for (int i = 0; i < pedals.size(); i++) {
            PedalEffect pedal = pedals.get(i);
            if (pedal.getName().equals(effectName)) {
                pedal.setEnabled(enabled);
                pedal.setMainValue(mainValue);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    /**
     * ViewHolder para cada pedal
     */
    public static class PedalViewHolder extends RecyclerView.ViewHolder {
        
        private final ImageView pedalLed;
        private final TextView pedalName;
        private final TextView knobLabel;
        private final SeekBar pedalMainKnob;
        private final LinearLayout secondaryKnobsContainer;
        private final SeekBar pedalSecondaryKnob1;
        private final SeekBar pedalSecondaryKnob2;
        private final Switch pedalSwitch;
        private final ImageView pedalIcon;
        private final View dragHandle;
        
        public PedalViewHolder(@NonNull View itemView) {
            super(itemView);
            
            pedalLed = itemView.findViewById(R.id.pedalLed);
            pedalName = itemView.findViewById(R.id.pedalName);
            knobLabel = itemView.findViewById(R.id.knobLabel);
            pedalMainKnob = itemView.findViewById(R.id.pedalMainKnob);
            secondaryKnobsContainer = itemView.findViewById(R.id.secondaryKnobsContainer);
            pedalSecondaryKnob1 = itemView.findViewById(R.id.pedalSecondaryKnob1);
            pedalSecondaryKnob2 = itemView.findViewById(R.id.pedalSecondaryKnob2);
            pedalSwitch = itemView.findViewById(R.id.pedalSwitch);
            pedalIcon = itemView.findViewById(R.id.pedalIcon);
            dragHandle = itemView.findViewById(R.id.dragHandle);
        }
        
        public void bind(PedalEffect pedal, PedalInteractionListener listener) {
            // Configurar nome e visual
            pedalName.setText(pedal.getName().toUpperCase());
            
            // Configurar LED baseado no estado
            pedalLed.setImageResource(pedal.isEnabled() ? 
                R.drawable.ic_led_on : R.drawable.ic_led_off);
            
            // Configurar switch
            pedalSwitch.setChecked(pedal.isEnabled());
            pedalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onPedalEnabled(pedal.getName(), isChecked);
                }
                // Atualizar LED imediatamente
                pedalLed.setImageResource(isChecked ? 
                    R.drawable.ic_led_on : R.drawable.ic_led_off);
            });
            
            // Configurar knob principal
            knobLabel.setText(pedal.getMainKnobLabel());
            pedalMainKnob.setProgress((int) (pedal.getMainValue() * 100));
            pedalMainKnob.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && listener != null) {
                        float value = progress / 100.0f;
                        listener.onPedalParameterChanged(pedal.getName(), 
                            pedal.getMainKnobLabel().toLowerCase(), value);
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            
            // Configurar knobs secundários se existirem
            if (pedal.hasSecondaryKnobs()) {
                secondaryKnobsContainer.setVisibility(View.VISIBLE);
                
                if (pedal.getSecondaryKnob1Value() != null) {
                    pedalSecondaryKnob1.setProgress((int) (pedal.getSecondaryKnob1Value() * 100));
                    pedalSecondaryKnob1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser && listener != null) {
                                float value = progress / 100.0f;
                                listener.onPedalParameterChanged(pedal.getName(), 
                                    pedal.getSecondaryKnob1Label().toLowerCase(), value);
                            }
                        }
                        
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}
                        
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }
                
                if (pedal.getSecondaryKnob2Value() != null) {
                    pedalSecondaryKnob2.setProgress((int) (pedal.getSecondaryKnob2Value() * 100));
                    pedalSecondaryKnob2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser && listener != null) {
                                float value = progress / 100.0f;
                                listener.onPedalParameterChanged(pedal.getName(), 
                                    pedal.getSecondaryKnob2Label().toLowerCase(), value);
                            }
                        }
                        
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}
                        
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }
            } else {
                secondaryKnobsContainer.setVisibility(View.GONE);
            }
            
            // Configurar ícone do efeito
            pedalIcon.setImageResource(pedal.getIconResourceId());
            
            // Configurar cores baseadas no tipo de efeito
            setupPedalColors(pedal);
            
            // Configurar cliques
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPedalClicked(pedal.getName());
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onPedalLongClicked(pedal.getName());
                }
                return true;
            });
        }
        
        private void setupPedalColors(PedalEffect pedal) {
            int backgroundColor = pedal.getBackgroundColor();
            if (backgroundColor != 0) {
                itemView.findViewById(R.id.pedalCard).setBackgroundColor(backgroundColor);
            }
        }
    }
} 