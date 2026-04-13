package com.thiagofernendorech.toneforge.ui.debug;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.thiagofernendorech.toneforge.AudioEngine;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;

/**
 * Fase 0 — Activity de debug para comparar a latência do pipeline legado
 * (AudioRecord/AudioTrack via PipelineManager) contra o novo pipeline C++
 * baseado em Oboe. Gate crítico para decidir se a migração segue para Fase 1.
 *
 * Lançar via:
 *   adb shell am start -n com.thiagofernendorech.toneforge/com.thiagofernendorech.toneforge.ui.debug.DebugBenchmarkActivity
 */
public class DebugBenchmarkActivity extends AppCompatActivity {

    private static final String TAG = "DebugBenchmark";
    private static final int REQ_RECORD_AUDIO = 42;
    private static final long POLL_INTERVAL_MS = 250L;

    private AudioRepository repo;

    private RadioButton rbLegacy;
    private RadioButton rbCpp;
    private Button btnStartStop;
    private Button btnToggleEffects;
    private TextView statsView;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long runStartMs = 0L;
    private boolean effectsEngaged = false;

    private final Runnable pollStats = new Runnable() {
        @Override
        public void run() {
            updateStats();
            mainHandler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = AudioRepository.getInstance(this);
        setContentView(buildUi());
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainHandler.post(pollStats);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainHandler.removeCallbacks(pollStats);
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("ToneForge — Oboe Benchmark (Fase 0)");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18f);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        TextView hint = new TextView(this);
        hint.setText("1) Escolha o backend\n2) (Opcional) Engate todos os efeitos\n3) Start/Stop\n4) Compare a latência entre os dois modos");
        hint.setTextColor(Color.LTGRAY);
        hint.setTextSize(12f);
        hint.setPadding(0, 0, 0, dp(16));
        root.addView(hint);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);

        rbLegacy = new RadioButton(this);
        rbLegacy.setText("Java legado");
        rbLegacy.setTextColor(Color.WHITE);
        rbLegacy.setChecked(!repo.isUsingCppPipeline());

        rbCpp = new RadioButton(this);
        rbCpp.setText("C++ (Oboe)");
        rbCpp.setTextColor(Color.WHITE);
        rbCpp.setChecked(repo.isUsingCppPipeline());

        group.addView(rbLegacy);
        group.addView(rbCpp);
        group.setOnCheckedChangeListener((g, id) -> {
            boolean useCpp = (id == rbCpp.getId());
            boolean ok = repo.setUseCppPipeline(useCpp);
            if (!ok) {
                Toast.makeText(this, "Pare o pipeline antes de trocar o backend", Toast.LENGTH_SHORT).show();
                // Revert visual state
                rbLegacy.setChecked(!repo.isUsingCppPipeline());
                rbCpp.setChecked(repo.isUsingCppPipeline());
            }
            updateStats();
        });
        root.addView(group);

        btnStartStop = new Button(this);
        btnStartStop.setText("START");
        btnStartStop.setOnClickListener(v -> toggleStartStop());
        root.addView(btnStartStop);

        btnToggleEffects = new Button(this);
        btnToggleEffects.setText("Engajar TODOS os efeitos");
        btnToggleEffects.setOnClickListener(v -> toggleAllEffects());
        root.addView(btnToggleEffects);

        statsView = new TextView(this);
        statsView.setTextColor(Color.GREEN);
        statsView.setTypeface(android.graphics.Typeface.MONOSPACE);
        statsView.setTextSize(13f);
        statsView.setPadding(0, dp(24), 0, 0);
        statsView.setGravity(Gravity.START);
        root.addView(statsView);

        return root;
    }

    private void toggleStartStop() {
        if (repo.isAudioPipelineRunning()) {
            repo.stopAudioPipeline();
            btnStartStop.setText("START");
            logFinalReport();
            runStartMs = 0L;
        } else {
            if (!ensureRecordPermission()) return;
            boolean started = repo.startAudioPipeline();
            if (started) {
                btnStartStop.setText("STOP");
                runStartMs = System.currentTimeMillis();
            } else {
                Toast.makeText(this, "Falha ao iniciar pipeline — ver logcat", Toast.LENGTH_LONG).show();
            }
        }
        updateStats();
    }

    private void toggleAllEffects() {
        effectsEngaged = !effectsEngaged;
        repo.setGainEnabled(effectsEngaged);
        repo.setDistortionEnabled(effectsEngaged);
        repo.setDelayEnabled(effectsEngaged);
        repo.setReverbEnabled(effectsEngaged);
        repo.setChorusEnabled(effectsEngaged);
        repo.setFlangerEnabled(effectsEngaged);
        repo.setPhaserEnabled(effectsEngaged);
        repo.setEQEnabled(effectsEngaged);
        repo.setCompressorEnabled(effectsEngaged);
        btnToggleEffects.setText(effectsEngaged ? "Desengajar efeitos" : "Engajar TODOS os efeitos");
    }

    private boolean ensureRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_RECORD_AUDIO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toggleStartStop();
        } else {
            Toast.makeText(this, "Permissão de microfone é obrigatória", Toast.LENGTH_LONG).show();
        }
    }

    private void updateStats() {
        boolean running = repo.isAudioPipelineRunning();
        boolean cpp = repo.isUsingCppPipeline();

        StringBuilder sb = new StringBuilder();
        sb.append("backend      : ").append(cpp ? "C++ / Oboe" : "Java legado (AudioRecord)").append('\n');
        sb.append("running      : ").append(running).append('\n');
        if (cpp && running) {
            sb.append("sample rate  : ").append(AudioEngine.getCppPipelineSampleRate()).append(" Hz\n");
            sb.append("latency rt   : ").append(String.format("%.2f", AudioEngine.getCppPipelineLatencyMs())).append(" ms\n");
            sb.append("xruns        : ").append(AudioEngine.getCppPipelineXrunCount()).append('\n');
        } else if (!cpp && running) {
            sb.append("(pipeline legado — telemetria via adb logcat -s PipelineManager:*)\n");
        }
        sb.append("effects on   : ").append(effectsEngaged).append('\n');
        if (runStartMs > 0) {
            long elapsed = (System.currentTimeMillis() - runStartMs) / 1000L;
            sb.append("uptime       : ").append(elapsed).append(" s\n");
        }
        statsView.setText(sb.toString());
    }

    private void logFinalReport() {
        long elapsed = runStartMs > 0 ? (System.currentTimeMillis() - runStartMs) / 1000L : 0;
        boolean cpp = repo.isUsingCppPipeline();
        Log.i(TAG, "==== BENCHMARK REPORT ====");
        Log.i(TAG, "backend: " + (cpp ? "C++/Oboe" : "Java legado"));
        Log.i(TAG, "uptime_s: " + elapsed);
        if (cpp) {
            Log.i(TAG, "last_latency_ms: " + AudioEngine.getCppPipelineLatencyMs());
            Log.i(TAG, "total_xruns: " + AudioEngine.getCppPipelineXrunCount());
            Log.i(TAG, "sample_rate: " + AudioEngine.getCppPipelineSampleRate());
        }
        Log.i(TAG, "effects_engaged: " + effectsEngaged);
        Log.i(TAG, "==========================");
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
