package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FavoritePresetAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> presetNames;
    
    public FavoritePresetAdapter(Context context, ArrayList<String> presetNames) {
        super(context, android.R.layout.simple_spinner_item, presetNames);
        this.context = context;
        this.presetNames = presetNames;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }
    
    private View createItemView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        
        TextView textView = convertView.findViewById(android.R.id.text1);
        String presetName = presetNames.get(position);
        textView.setText(presetName);
        
        // Adicionar ícone de favorito se necessário
        if (FavoritesManager.isFavorite(context, presetName)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_favorite_filled, 0);
            textView.setCompoundDrawablePadding(8);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        
        return convertView;
    }
} 