package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private static final String FAVORITES_KEY = "favorite_presets";
    
    public static void addToFavorites(Context context, String presetName) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favorites = new HashSet<>(prefs.getStringSet(FAVORITES_KEY, new HashSet<>()));
        favorites.add(presetName);
        prefs.edit().putStringSet(FAVORITES_KEY, favorites).apply();
    }
    
    public static void removeFromFavorites(Context context, String presetName) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favorites = new HashSet<>(prefs.getStringSet(FAVORITES_KEY, new HashSet<>()));
        favorites.remove(presetName);
        prefs.edit().putStringSet(FAVORITES_KEY, favorites).apply();
    }
    
    public static boolean isFavorite(Context context, String presetName) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        return favorites.contains(presetName);
    }
    
    public static ArrayList<String> getFavorites(Context context) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        return new ArrayList<>(favorites);
    }
    
    public static ArrayList<String> getFavoritePresets(Context context) {
        ArrayList<String> allPresets = PresetManager.getPresetNames(context);
        ArrayList<String> favorites = getFavorites(context);
        ArrayList<String> favoritePresets = new ArrayList<>();
        
        for (String preset : allPresets) {
            if (favorites.contains(preset)) {
                favoritePresets.add(preset);
            }
        }
        
        return favoritePresets;
    }
    
    public static void toggleFavorite(Context context, String presetName) {
        if (isFavorite(context, presetName)) {
            removeFromFavorites(context, presetName);
        } else {
            addToFavorites(context, presetName);
        }
    }
    
    public static int getFavoritesCount(Context context) {
        return getFavorites(context).size();
    }
} 