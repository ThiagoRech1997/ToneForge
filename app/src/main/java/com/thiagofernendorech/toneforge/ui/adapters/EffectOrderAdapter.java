package com.thiagofernendorech.toneforge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.List;

public class EffectOrderAdapter extends RecyclerView.Adapter<EffectOrderAdapter.ViewHolder> {
    private final List<String> effects;
    private final OnOrderChangedListener listener;

    public interface OnOrderChangedListener {
        void onOrderChanged(List<String> newOrder);
    }

    public EffectOrderAdapter(List<String> effects, OnOrderChangedListener listener) {
        this.effects = effects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(effects.get(position));
    }

    @Override
    public int getItemCount() {
        return effects.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= effects.size() || toPosition >= effects.size()) return;
        Collections.swap(effects, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        if (listener != null) listener.onOrderChanged(effects);
    }

    public List<String> getCurrentOrder() {
        return effects;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
} 