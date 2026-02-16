package com.thiagofernendorech.toneforge.infrastructure.ui;

import android.os.SystemClock;
import android.view.View;

/**
 * Click listener que previne toques duplos impondo um intervalo mínimo
 * entre cliques consecutivos.
 */
public class DebounceClickListener implements View.OnClickListener {

    private static final long DEFAULT_DEBOUNCE_MS = 500;

    private final long debounceMs;
    private final View.OnClickListener delegate;
    private long lastClickTime = 0;

    public DebounceClickListener(View.OnClickListener delegate) {
        this(DEFAULT_DEBOUNCE_MS, delegate);
    }

    public DebounceClickListener(long debounceMs, View.OnClickListener delegate) {
        this.debounceMs = debounceMs;
        this.delegate = delegate;
    }

    @Override
    public void onClick(View v) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastClickTime >= debounceMs) {
            lastClickTime = now;
            delegate.onClick(v);
        }
    }
}
