package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TooltipManager {
    private static PopupWindow currentTooltip;
    
    public static void showTooltip(Context context, View anchorView, String tooltipText) {
        // Fechar tooltip anterior se existir
        if (currentTooltip != null && currentTooltip.isShowing()) {
            currentTooltip.dismiss();
        }
        
        // Criar o layout do tooltip
        LayoutInflater inflater = LayoutInflater.from(context);
        View tooltipView = inflater.inflate(R.layout.tooltip_layout, null);
        TextView tooltipTextView = tooltipView.findViewById(R.id.tooltipText);
        tooltipTextView.setText(tooltipText);
        
        // Criar o PopupWindow
        currentTooltip = new PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        
        // Configurar o PopupWindow
        currentTooltip.setBackgroundDrawable(null);
        currentTooltip.setElevation(10f);
        currentTooltip.setOutsideTouchable(true);
        currentTooltip.setFocusable(false);
        
        // Calcular posição
        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        
        int x = location[0] + (anchorView.getWidth() / 2);
        int y = location[1] - 20; // 20px acima do elemento
        
        // Mostrar o tooltip
        currentTooltip.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);
        
        // Auto-dismiss após 3 segundos
        anchorView.postDelayed(() -> {
            if (currentTooltip != null && currentTooltip.isShowing()) {
                currentTooltip.dismiss();
            }
        }, 3000);
    }
    
    public static void hideTooltip() {
        if (currentTooltip != null && currentTooltip.isShowing()) {
            currentTooltip.dismiss();
        }
    }
} 