package com.thiagofernendorech.toneforge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;
import org.json.JSONException;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.text.TextUtils;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
import android.widget.LinearLayout;

public class EffectsFragment extends Fragment {
    private static final int EXPORT_PRESET_REQUEST = 1001;
    private static final int IMPORT_PRESET_REQUEST = 1002;
    
    private TextView statusText;
    private ArrayAdapter<String> presetAdapter;
    private ArrayList<EffectPreset> presetList;
    private Spinner presetSpinner;
    private EditText presetNameEdit;
    private Button savePresetButton, deletePresetButton;
    private EffectOrderAdapter effectOrderAdapter;
    private RecyclerView effectsOrderRecycler;
    private ArrayList<String> effectOrder;
    private boolean showFavoritesOnly = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effects, container, false);

        statusText = view.findViewById(R.id.effectsStatusText);

        // Verificar permissão de áudio
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            Toast.makeText(getContext(), "Permissão de microfone necessária para os efeitos", Toast.LENGTH_LONG).show();
            statusText.setText("Aguardando permissão de microfone...");
            statusText.setTextColor(getResources().getColor(R.color.yellow));
        } else {
            // Iniciar pipeline de áudio em tempo real
            AudioEngine.startAudioPipeline();
            statusText.setText("Pipeline de áudio ativo - Efeitos funcionando!");
            statusText.setTextColor(getResources().getColor(R.color.green));
            Toast.makeText(getContext(), "Pipeline de áudio iniciado! Fale no microfone para ouvir os efeitos.", Toast.LENGTH_LONG).show();
        }

        // Ganho
        Switch switchGain = view.findViewById(R.id.switchGain);
        SeekBar seekGain = view.findViewById(R.id.seekGain);
        switchGain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setGainEnabled(isChecked);
            AudioStateManager.updateEffectState("gain", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            updateStatus();
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setGainLevel(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Tooltips para Ganho
        seekGain.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_gain));
            return true;
        });
        
        switchGain.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_gain_enable));
            return true;
        });

        // Distorção
        Switch switchDistortion = view.findViewById(R.id.switchDistortion);
        SeekBar seekDistortion = view.findViewById(R.id.seekDistortion);
        switchDistortion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setDistortionEnabled(isChecked);
            AudioStateManager.updateEffectState("distortion", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            updateStatus();
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekDistortion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setDistortionLevel(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Tooltips para Distorção
        seekDistortion.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_distortion));
            return true;
        });
        
        switchDistortion.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_distortion_enable));
            return true;
        });

        // Delay
        Switch switchDelay = view.findViewById(R.id.switchDelay);
        SeekBar seekDelayTime = view.findViewById(R.id.seekDelayTime);
        Switch switchDelaySyncBPM = view.findViewById(R.id.switchDelaySyncBPM);
        SeekBar seekDelayBPM = view.findViewById(R.id.seekDelayBPM);
        SeekBar seekDelayFeedback = view.findViewById(R.id.seekDelayFeedback);
        SeekBar seekDelayMix = view.findViewById(R.id.seekDelayMix);
        
        switchDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setDelayEnabled(isChecked);
            AudioStateManager.updateEffectState("delay", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            updateStatus();
            if (getView() != null) updateBypassIndicators(getView());
        });
        
        seekDelayTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress; // 0-1000 ms
                AudioEngine.setDelayTime(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        switchDelaySyncBPM.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setDelaySyncBPM(isChecked);
        });
        
        seekDelayBPM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = 60 + progress; // 60-260 BPM
                AudioEngine.setDelayBPM(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        seekDelayFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setDelayFeedback(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        seekDelayMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setDelayMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Tooltips para Delay
        seekDelayTime.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_delay_time));
            return true;
        });
        
        switchDelay.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_delay_enable));
            return true;
        });
        
        switchDelaySyncBPM.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_delay_sync_bpm));
            return true;
        });
        
        seekDelayBPM.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_delay_bpm));
            return true;
        });
        
        seekDelayFeedback.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_delay_feedback));
            return true;
        });
        
        seekDelayMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_delay_mix));
            return true;
        });

        // Reverb
        Switch switchReverb = view.findViewById(R.id.switchReverb);
        SeekBar seekReverb = view.findViewById(R.id.seekReverb);
        switchReverb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setReverbEnabled(isChecked);
            AudioStateManager.updateEffectState("reverb", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            updateStatus();
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekReverb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setReverbLevel(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Room Size do Reverb
        SeekBar seekReverbRoomSize = view.findViewById(R.id.seekReverbRoomSize);
        seekReverbRoomSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setReverbRoomSize(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Damping do Reverb
        SeekBar seekReverbDamping = view.findViewById(R.id.seekReverbDamping);
        seekReverbDamping.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setReverbDamping(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Tipo de Distorção
        Spinner spinnerDistortionType = view.findViewById(R.id.spinnerDistortionType);
        spinnerDistortionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                AudioEngine.setDistortionType(position); // 0=Soft, 1=Hard, 2=Fuzz, 3=Overdrive
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        // Mix Distorção
        SeekBar seekDistortionMix = view.findViewById(R.id.seekDistortionMix);
        seekDistortionMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setDistortionMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // Mix Reverb
        SeekBar seekReverbMix = view.findViewById(R.id.seekReverbMix);
        seekReverbMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setReverbMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Tooltips para Reverb
        seekReverb.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reverb));
            return true;
        });
        
        switchReverb.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reverb_enable));
            return true;
        });
        
        seekReverbRoomSize.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reverb_room_size));
            return true;
        });
        
        seekReverbDamping.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reverb_damping));
            return true;
        });
        
        seekReverbMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reverb_mix));
            return true;
        });

        // Chorus
        Switch switchChorus = view.findViewById(R.id.switchChorus);
        SeekBar seekChorusDepth = view.findViewById(R.id.seekChorusDepth);
        SeekBar seekChorusRate = view.findViewById(R.id.seekChorusRate);
        SeekBar seekChorusMix = view.findViewById(R.id.seekChorusMix);
        switchChorus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setChorusEnabled(isChecked);
            AudioStateManager.updateEffectState("chorus", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekChorusDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 0-40ms (0.0-0.04s)
                float value = progress / 2500.0f; // 0.0-0.04
                AudioEngine.setChorusDepth(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekChorusRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 0-5Hz
                float value = progress / 20.0f; // 0.0-5.0
                AudioEngine.setChorusRate(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekChorusMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setChorusMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Flanger
        Switch switchFlanger = view.findViewById(R.id.switchFlanger);
        SeekBar seekFlangerDepth = view.findViewById(R.id.seekFlangerDepth);
        SeekBar seekFlangerRate = view.findViewById(R.id.seekFlangerRate);
        SeekBar seekFlangerFeedback = view.findViewById(R.id.seekFlangerFeedback);
        SeekBar seekFlangerMix = view.findViewById(R.id.seekFlangerMix);
        switchFlanger.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setFlangerEnabled(isChecked);
            AudioStateManager.updateEffectState("flanger", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekFlangerDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 0-10ms (0.0-0.01s)
                float value = progress / 10000.0f; // 0.0-0.01
                AudioEngine.setFlangerDepth(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekFlangerRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 0-5Hz
                float value = progress / 20.0f; // 0.0-5.0
                AudioEngine.setFlangerRate(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekFlangerFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setFlangerFeedback(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekFlangerMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setFlangerMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Phaser
        Switch switchPhaser = view.findViewById(R.id.switchPhaser);
        SeekBar seekPhaserDepth = view.findViewById(R.id.seekPhaserDepth);
        SeekBar seekPhaserRate = view.findViewById(R.id.seekPhaserRate);
        SeekBar seekPhaserFeedback = view.findViewById(R.id.seekPhaserFeedback);
        SeekBar seekPhaserMix = view.findViewById(R.id.seekPhaserMix);
        switchPhaser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setPhaserEnabled(isChecked);
            AudioStateManager.updateEffectState("phaser", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekPhaserDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 0-1.0 (profundidade da modulação)
                float value = progress / 100.0f;
                AudioEngine.setPhaserDepth(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekPhaserRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 0-5Hz
                float value = progress / 20.0f; // 0.0-5.0
                AudioEngine.setPhaserRate(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekPhaserFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setPhaserFeedback(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekPhaserMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setPhaserMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Equalizer (EQ)
        Switch switchEQ = view.findViewById(R.id.switchEQ);
        SeekBar seekEQLow = view.findViewById(R.id.seekEQLow);
        SeekBar seekEQMid = view.findViewById(R.id.seekEQMid);
        SeekBar seekEQHigh = view.findViewById(R.id.seekEQHigh);
        SeekBar seekEQMix = view.findViewById(R.id.seekEQMix);
        switchEQ.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setEQEnabled(isChecked);
            AudioStateManager.updateEffectState("eq", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekEQLow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> -12dB a +12dB
                float value = (progress - 50) / 50.0f * 12.0f; // -12 a +12 dB
                AudioEngine.setEQLow(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekEQMid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> -12dB a +12dB
                float value = (progress - 50) / 50.0f * 12.0f; // -12 a +12 dB
                AudioEngine.setEQMid(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekEQHigh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> -12dB a +12dB
                float value = (progress - 50) / 50.0f * 12.0f; // -12 a +12 dB
                AudioEngine.setEQHigh(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekEQMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setEQMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Compressor
        Switch switchCompressor = view.findViewById(R.id.switchCompressor);
        SeekBar seekCompressorThreshold = view.findViewById(R.id.seekCompressorThreshold);
        SeekBar seekCompressorRatio = view.findViewById(R.id.seekCompressorRatio);
        SeekBar seekCompressorAttack = view.findViewById(R.id.seekCompressorAttack);
        SeekBar seekCompressorRelease = view.findViewById(R.id.seekCompressorRelease);
        SeekBar seekCompressorMix = view.findViewById(R.id.seekCompressorMix);
        switchCompressor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setCompressorEnabled(isChecked);
            AudioStateManager.updateEffectState("compressor", isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            if (getView() != null) updateBypassIndicators(getView());
        });
        seekCompressorThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> -60dB a 0dB
                float value = (progress / 100.0f) * 60.0f - 60.0f; // -60 a 0 dB
                AudioEngine.setCompressorThreshold(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekCompressorRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 1:1 a 20:1
                float value = 1.0f + (progress / 100.0f) * 19.0f; // 1.0 a 20.0
                AudioEngine.setCompressorRatio(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekCompressorAttack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 0.1ms a 100ms
                float value = 0.1f + (progress / 100.0f) * 99.9f; // 0.1 a 100 ms
                AudioEngine.setCompressorAttack(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekCompressorRelease.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0-100 -> 10ms a 1000ms
                float value = 10.0f + (progress / 100.0f) * 990.0f; // 10 a 1000 ms
                AudioEngine.setCompressorRelease(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekCompressorMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 100.0f;
                AudioEngine.setCompressorMix(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // --- Presets UI ---
        presetSpinner = view.findViewById(R.id.presetSpinner);
        presetNameEdit = view.findViewById(R.id.presetNameEdit);
        savePresetButton = view.findViewById(R.id.savePresetButton);
        deletePresetButton = view.findViewById(R.id.deletePresetButton);

        presetList = loadPresets();
        ArrayList<String> presetNames = new ArrayList<>();
        for (EffectPreset p : presetList) presetNames.add(p.name);
        presetAdapter = new FavoritePresetAdapter(getContext(), presetNames);
        presetSpinner.setAdapter(presetAdapter);

        savePresetButton.setOnClickListener(v -> {
            String name = presetNameEdit.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(), "Digite um nome para o preset", Toast.LENGTH_SHORT).show();
                return;
            }
            EffectPreset preset = getCurrentPreset(name);
            // Remover preset antigo com mesmo nome
            for (EffectPreset p : new ArrayList<>(presetList)) {
                if (p.name.equals(name)) deletePreset(p);
            }
            savePreset(preset);
            atualizarListaPresets();
            Toast.makeText(getContext(), "Preset salvo!", Toast.LENGTH_SHORT).show();
        });

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position >= 0 && position < presetList.size()) {
                    applyPreset(presetList.get(position));
                }
                updateFavoriteButton();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        deletePresetButton.setOnClickListener(v -> {
            String selectedPreset = presetSpinner.getSelectedItem().toString();
            for (EffectPreset p : presetList) {
                if (p.name.equals(selectedPreset)) {
                    deletePreset(p);
                    break;
                }
            }
            atualizarListaPresets();
        });

        // --- Ordem dos Efeitos ---
        effectsOrderRecycler = view.findViewById(R.id.effectsOrderRecycler);
        effectOrder = loadEffectOrderFromPrefs();
        if (effectOrder == null) {
            effectOrder = new ArrayList<>();
            effectOrder.add("Ganho");
            effectOrder.add("Distorção");
            effectOrder.add("Chorus");
            effectOrder.add("Flanger");
            effectOrder.add("Phaser");
            effectOrder.add("EQ");
            effectOrder.add("Compressor");
            effectOrder.add("Delay");
            effectOrder.add("Reverb");
        }
        updateChainView();
        effectOrderAdapter = new EffectOrderAdapter(effectOrder, newOrder -> {
            effectOrder = new ArrayList<>(newOrder);
            saveEffectOrderToPrefs(effectOrder);
            // Enviar ordem para o C++
            AudioEngine.setEffectOrder(effectOrder.toArray(new String[0]));
            updateChainView();
        });
        effectsOrderRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        effectsOrderRecycler.setAdapter(effectOrderAdapter);
        // Drag-and-drop
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                effectOrderAdapter.onItemMove(from, to);
                return true;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(effectsOrderRecycler);

        // --- Oversampling ---
        Switch switchOversampling = view.findViewById(R.id.switchOversampling);
        Spinner spinnerOversamplingFactor = view.findViewById(R.id.spinnerOversamplingFactor);
        TextView oversamplingQualityText = view.findViewById(R.id.oversamplingQualityText);
        
        // Configurar spinner de fator de oversampling
        String[] oversamplingFactors = {
            getString(R.string.oversampling_1x),
            getString(R.string.oversampling_2x),
            getString(R.string.oversampling_4x),
            getString(R.string.oversampling_8x)
        };
        ArrayAdapter<String> oversamplingAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, oversamplingFactors);
        oversamplingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOversamplingFactor.setAdapter(oversamplingAdapter);
        
        // Carregar configurações salvas
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean oversamplingEnabled = prefs.getBoolean("oversampling_enabled", true);
        int oversamplingFactor = prefs.getInt("oversampling_factor", 2);
        
        switchOversampling.setChecked(oversamplingEnabled);
        spinnerOversamplingFactor.setSelection(oversamplingFactor - 1); // 1x = posição 0
        
        // Aplicar configurações
        AudioEngine.setOversamplingEnabled(oversamplingEnabled);
        AudioEngine.setOversamplingFactor(oversamplingFactor);
        updateOversamplingQualityText(oversamplingQualityText, oversamplingEnabled, oversamplingFactor);
        
        // Listeners
        switchOversampling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setOversamplingEnabled(isChecked);
            AudioStateManager.getInstance(requireContext()).setOversamplingEnabled(isChecked);
            AudioBackgroundService.updateNotification(requireContext());
            prefs.edit().putBoolean("oversampling_enabled", isChecked).apply();
            updateOversamplingQualityText(oversamplingQualityText, isChecked, oversamplingFactor);
        });
        
        spinnerOversamplingFactor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                int factor = position + 1; // 1x, 2x, 4x, 8x
                AudioEngine.setOversamplingFactor(factor);
                AudioStateManager.getInstance(requireContext()).setOversamplingFactor(factor);
                AudioBackgroundService.updateNotification(requireContext());
                prefs.edit().putInt("oversampling_factor", factor).apply();
                updateOversamplingQualityText(oversamplingQualityText, oversamplingEnabled, factor);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Tipo de Reverb
        Spinner spinnerReverbType = view.findViewById(R.id.spinnerReverbType);
        spinnerReverbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                AudioEngine.setReverbType(position); // 0=Hall, 1=Plate, 2=Spring
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Botão Reset All
        Button btnResetAll = view.findViewById(R.id.btnResetAll);
        btnResetAll.setOnClickListener(v -> resetAllEffects());
        
        // Botões de reset individuais
        Button btnResetGain = view.findViewById(R.id.btnResetGain);
        btnResetGain.setOnClickListener(v -> resetGain());
        
        Button btnResetDistortion = view.findViewById(R.id.btnResetDistortion);
        btnResetDistortion.setOnClickListener(v -> resetDistortion());
        
        Button btnResetDelay = view.findViewById(R.id.btnResetDelay);
        btnResetDelay.setOnClickListener(v -> resetDelay());
        
        Button btnResetReverb = view.findViewById(R.id.btnResetReverb);
        btnResetReverb.setOnClickListener(v -> resetReverb());
        
        Button btnResetChorus = view.findViewById(R.id.btnResetChorus);
        btnResetChorus.setOnClickListener(v -> resetChorus());
        
        Button btnResetFlanger = view.findViewById(R.id.btnResetFlanger);
        btnResetFlanger.setOnClickListener(v -> resetFlanger());
        
        Button btnResetPhaser = view.findViewById(R.id.btnResetPhaser);
        btnResetPhaser.setOnClickListener(v -> resetPhaser());
        
        Button btnResetEQ = view.findViewById(R.id.btnResetEQ);
        btnResetEQ.setOnClickListener(v -> resetEQ());
        
        Button btnResetCompressor = view.findViewById(R.id.btnResetCompressor);
        btnResetCompressor.setOnClickListener(v -> resetCompressor());

        // Botões de Exportar/Importar Presets
        Button btnExportPreset = view.findViewById(R.id.btnExportPreset);
        Button btnImportPreset = view.findViewById(R.id.btnImportPreset);
        
        btnExportPreset.setOnClickListener(v -> showExportPresetDialog());
        btnImportPreset.setOnClickListener(v -> selectPresetFile());

        // Botão de Filtro de Favoritos
        Button btnFavoritesFilter = view.findViewById(R.id.btnFavoritesFilter);
        btnFavoritesFilter.setOnClickListener(v -> toggleFavoritesFilter());

        // Tooltips para todos os controles restantes
        setupTooltips(view);

        updateBypassIndicators(view);

        // Botão de Favorito
        Button favoritePresetButton = view.findViewById(R.id.favoritePresetButton);
        favoritePresetButton.setOnClickListener(v -> toggleFavoritePreset());

        return view;
    }

    private void updateStatus() {
        if (statusText != null) {
            if (AudioEngine.isAudioPipelineRunning()) {
                statusText.setText("Pipeline ativo - Efeitos aplicados em tempo real");
                statusText.setTextColor(getResources().getColor(R.color.green));
            } else {
                statusText.setText("Pipeline inativo");
                statusText.setTextColor(getResources().getColor(R.color.red));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Parar pipeline de áudio quando sair da tela de efeitos
        AudioEngine.stopAudioPipeline();
    }

    // Classe para armazenar um preset de efeitos
    private static class EffectPreset {
        String name;
        float gain, distortion, delay, delayFeedback, reverb, reverbRoomSize, reverbDamping;
        boolean gainEnabled, distortionEnabled, delayEnabled, reverbEnabled;
        EffectPreset(String name, float gain, boolean gainEnabled, float distortion, boolean distortionEnabled, float delay, float delayFeedback, boolean delayEnabled, float reverb, float reverbRoomSize, float reverbDamping, boolean reverbEnabled) {
            this.name = name;
            this.gain = gain;
            this.gainEnabled = gainEnabled;
            this.distortion = distortion;
            this.distortionEnabled = distortionEnabled;
            this.delay = delay;
            this.delayFeedback = delayFeedback;
            this.delayEnabled = delayEnabled;
            this.reverb = reverb;
            this.reverbRoomSize = reverbRoomSize;
            this.reverbDamping = reverbDamping;
            this.reverbEnabled = reverbEnabled;
        }
        JSONObject toJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("gain", gain);
            obj.put("gainEnabled", gainEnabled);
            obj.put("distortion", distortion);
            obj.put("distortionEnabled", distortionEnabled);
            obj.put("delay", delay);
            obj.put("delayFeedback", delayFeedback);
            obj.put("delayEnabled", delayEnabled);
            obj.put("reverb", reverb);
            obj.put("reverbRoomSize", reverbRoomSize);
            obj.put("reverbDamping", reverbDamping);
            obj.put("reverbEnabled", reverbEnabled);
            return obj;
        }
        static EffectPreset fromJson(JSONObject obj) throws JSONException {
            return new EffectPreset(
                obj.getString("name"),
                (float)obj.getDouble("gain"),
                obj.getBoolean("gainEnabled"),
                (float)obj.getDouble("distortion"),
                obj.getBoolean("distortionEnabled"),
                (float)obj.getDouble("delay"),
                (float)obj.getDouble("delayFeedback"),
                obj.getBoolean("delayEnabled"),
                (float)obj.getDouble("reverb"),
                (float)obj.getDouble("reverbRoomSize"),
                (float)obj.getDouble("reverbDamping"),
                obj.getBoolean("reverbEnabled")
            );
        }
    }

    // Utilitários para SharedPreferences
    private void savePreset(EffectPreset preset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> presetSet = new HashSet<>(prefs.getStringSet("effect_presets", new HashSet<>()));
        try {
            JSONObject obj = preset.toJson();
            obj.put("order", new org.json.JSONArray(effectOrder));
            presetSet.add(obj.toString());
        } catch (JSONException e) { e.printStackTrace(); }
        prefs.edit().putStringSet("effect_presets", presetSet).apply();
        saveEffectOrderToPrefs(effectOrder);
    }

    private ArrayList<EffectPreset> loadPresets() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> presetSet = prefs.getStringSet("effect_presets", new HashSet<>());
        ArrayList<EffectPreset> list = new ArrayList<>();
        for (String json : presetSet) {
            try {
                list.add(EffectPreset.fromJson(new JSONObject(json)));
            } catch (JSONException e) { e.printStackTrace(); }
        }
        return list;
    }

    private void deletePreset(EffectPreset preset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> presetSet = new HashSet<>(prefs.getStringSet("effect_presets", new HashSet<>()));
        try {
            presetSet.remove(preset.toJson().toString());
        } catch (JSONException e) { e.printStackTrace(); }
        prefs.edit().putStringSet("effect_presets", presetSet).apply();
    }

    // Métodos para integração futura com a interface
    private EffectPreset getCurrentPreset(String name) {
        // Obter valores atuais dos controles e switches
        View view = getView();
        if (view == null) return null;
        float gain = ((SeekBar)view.findViewById(R.id.seekGain)).getProgress() / 100.0f;
        boolean gainEnabled = ((Switch)view.findViewById(R.id.switchGain)).isChecked();
        float distortion = ((SeekBar)view.findViewById(R.id.seekDistortion)).getProgress() / 100.0f;
        boolean distortionEnabled = ((Switch)view.findViewById(R.id.switchDistortion)).isChecked();
        float delay = ((SeekBar)view.findViewById(R.id.seekDelayTime)).getProgress(); // 0-1000 ms
        float delayFeedback = ((SeekBar)view.findViewById(R.id.seekDelayFeedback)).getProgress() / 100.0f;
        boolean delayEnabled = ((Switch)view.findViewById(R.id.switchDelay)).isChecked();
        float reverb = ((SeekBar)view.findViewById(R.id.seekReverb)).getProgress() / 100.0f;
        float reverbRoomSize = ((SeekBar)view.findViewById(R.id.seekReverbRoomSize)).getProgress() / 100.0f;
        float reverbDamping = ((SeekBar)view.findViewById(R.id.seekReverbDamping)).getProgress() / 100.0f;
        boolean reverbEnabled = ((Switch)view.findViewById(R.id.switchReverb)).isChecked();
        return new EffectPreset(name, gain, gainEnabled, distortion, distortionEnabled, delay, delayFeedback, delayEnabled, reverb, reverbRoomSize, reverbDamping, reverbEnabled);
    }

    private void applyPreset(EffectPreset preset) {
        View view = getView();
        if (view == null) return;
        ((SeekBar)view.findViewById(R.id.seekGain)).setProgress((int)(preset.gain * 100));
        ((Switch)view.findViewById(R.id.switchGain)).setChecked(preset.gainEnabled);
        ((SeekBar)view.findViewById(R.id.seekDistortion)).setProgress((int)(preset.distortion * 100));
        ((Switch)view.findViewById(R.id.switchDistortion)).setChecked(preset.distortionEnabled);
        ((SeekBar)view.findViewById(R.id.seekDelayTime)).setProgress((int)preset.delay);
        ((SeekBar)view.findViewById(R.id.seekDelayFeedback)).setProgress((int)(preset.delayFeedback * 100));
        ((Switch)view.findViewById(R.id.switchDelay)).setChecked(preset.delayEnabled);
        ((SeekBar)view.findViewById(R.id.seekReverb)).setProgress((int)(preset.reverb * 100));
        ((SeekBar)view.findViewById(R.id.seekReverbRoomSize)).setProgress((int)(preset.reverbRoomSize * 100));
        ((SeekBar)view.findViewById(R.id.seekReverbDamping)).setProgress((int)(preset.reverbDamping * 100));
        ((Switch)view.findViewById(R.id.switchReverb)).setChecked(preset.reverbEnabled);
        // Restaurar ordem se existir
        try {
            JSONObject obj = preset.toJson();
            if (obj.has("order")) {
                org.json.JSONArray arr = obj.getJSONArray("order");
                effectOrder.clear();
                for (int i = 0; i < arr.length(); i++) effectOrder.add(arr.getString(i));
                effectOrderAdapter.notifyDataSetChanged();
                saveEffectOrderToPrefs(effectOrder);
                AudioEngine.setEffectOrder(effectOrder.toArray(new String[0]));
            }
        } catch (JSONException e) { /* ignora */ }
    }

    private void atualizarListaPresets() {
        if (showFavoritesOnly) {
            presetList = loadFavoritePresets();
        } else {
            presetList = loadPresets();
        }
        
        ArrayList<String> presetNames = new ArrayList<>();
        for (EffectPreset p : presetList) presetNames.add(p.name);
        presetAdapter.clear();
        presetAdapter.addAll(presetNames);
        presetAdapter.notifyDataSetChanged();
    }
    
    private ArrayList<EffectPreset> loadFavoritePresets() {
        ArrayList<String> favoriteNames = FavoritesManager.getFavoritePresets(getContext());
        ArrayList<EffectPreset> favoritePresets = new ArrayList<>();
        
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> presetSet = prefs.getStringSet("effect_presets", new HashSet<>());
        
        for (String json : presetSet) {
            try {
                EffectPreset preset = EffectPreset.fromJson(new JSONObject(json));
                if (favoriteNames.contains(preset.name)) {
                    favoritePresets.add(preset);
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }
        
        return favoritePresets;
    }

    // Persistência da ordem dos efeitos
    private void saveEffectOrderToPrefs(ArrayList<String> order) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("effect_order", new org.json.JSONArray(order).toString()).apply();
    }
    private ArrayList<String> loadEffectOrderFromPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = prefs.getString("effect_order", null);
        if (json == null) return null;
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) list.add(arr.getString(i));
            return list;
        } catch (Exception e) { return null; }
    }

    private void updateBypassIndicators(View view) {
        // Ganho
        updateEffectIndicator(
            view.findViewById(R.id.gainContainer),
            view.findViewById(R.id.gainIndicator),
            ((Switch)view.findViewById(R.id.switchGain)).isChecked()
        );
        
        // Distorção
        updateEffectIndicator(
            view.findViewById(R.id.distortionContainer),
            view.findViewById(R.id.distortionIndicator),
            ((Switch)view.findViewById(R.id.switchDistortion)).isChecked()
        );
        
        // Delay
        updateEffectIndicator(
            view.findViewById(R.id.delayContainer),
            view.findViewById(R.id.delayIndicator),
            ((Switch)view.findViewById(R.id.switchDelay)).isChecked()
        );
        
        // Reverb
        updateEffectIndicator(
            view.findViewById(R.id.reverbContainer),
            view.findViewById(R.id.reverbIndicator),
            ((Switch)view.findViewById(R.id.switchReverb)).isChecked()
        );
        
        // Chorus
        updateEffectIndicator(
            view.findViewById(R.id.chorusContainer),
            view.findViewById(R.id.chorusIndicator),
            ((Switch)view.findViewById(R.id.switchChorus)).isChecked()
        );
        
        // Flanger
        updateEffectIndicator(
            view.findViewById(R.id.flangerContainer),
            view.findViewById(R.id.flangerIndicator),
            ((Switch)view.findViewById(R.id.switchFlanger)).isChecked()
        );
        
        // Phaser
        updateEffectIndicator(
            view.findViewById(R.id.phaserContainer),
            view.findViewById(R.id.phaserIndicator),
            ((Switch)view.findViewById(R.id.switchPhaser)).isChecked()
        );
        
        // EQ
        updateEffectIndicator(
            view.findViewById(R.id.eqContainer),
            view.findViewById(R.id.eqIndicator),
            ((Switch)view.findViewById(R.id.switchEQ)).isChecked()
        );
        
        // Compressor
        updateEffectIndicator(
            view.findViewById(R.id.compressorContainer),
            view.findViewById(R.id.compressorIndicator),
            ((Switch)view.findViewById(R.id.switchCompressor)).isChecked()
        );
    }
    
    private void updateEffectIndicator(View container, ImageView indicator, boolean enabled) {
        if (enabled) {
            container.setBackgroundResource(R.color.effect_enabled_bg);
            indicator.setImageResource(R.drawable.effect_indicator_enabled);
            // Encontrar o TextView dentro do container
            for (int i = 0; i < ((ViewGroup) container).getChildCount(); i++) {
                View child = ((ViewGroup) container).getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(getResources().getColor(R.color.effect_enabled));
                    break;
                }
            }
        } else {
            container.setBackgroundResource(R.color.effect_disabled_bg);
            indicator.setImageResource(R.drawable.effect_indicator_disabled);
            // Encontrar o TextView dentro do container
            for (int i = 0; i < ((ViewGroup) container).getChildCount(); i++) {
                View child = ((ViewGroup) container).getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(getResources().getColor(R.color.effect_disabled));
                    break;
                }
            }
        }
    }

    private void resetAllEffects() {
        resetGain();
        resetDistortion();
        resetDelay();
        resetReverb();
        resetChorus();
        resetFlanger();
        resetPhaser();
        resetEQ();
        resetCompressor();
        
        // Reset da ordem dos efeitos
        effectOrder.clear();
        effectOrder.add("Ganho");
        effectOrder.add("Distorção");
        effectOrder.add("Chorus");
        effectOrder.add("Flanger");
        effectOrder.add("Phaser");
        effectOrder.add("EQ");
        effectOrder.add("Compressor");
        effectOrder.add("Delay");
        effectOrder.add("Reverb");
        effectOrderAdapter.notifyDataSetChanged();
        saveEffectOrderToPrefs(effectOrder);
        AudioEngine.setEffectOrder(effectOrder.toArray(new String[0]));
        
        updateBypassIndicators(getView());
        
        // Feedback visual
        Toast.makeText(getContext(), "Todos os efeitos foram resetados!", Toast.LENGTH_SHORT).show();
    }

    private void resetGain() {
        View view = getView();
        if (view == null) return;
        
        SeekBar seekGain = view.findViewById(R.id.seekGain);
        Switch switchGain = view.findViewById(R.id.switchGain);
        
        seekGain.setProgress(50); // 50% = 0.5
        switchGain.setChecked(true);
        
        AudioEngine.setGainLevel(0.5f);
        AudioEngine.setGainEnabled(true);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Ganho resetado!", Toast.LENGTH_SHORT).show();
    }

    private void resetDistortion() {
        View view = getView();
        if (view == null) return;
        
        SeekBar seekDistortion = view.findViewById(R.id.seekDistortion);
        Switch switchDistortion = view.findViewById(R.id.switchDistortion);
        Spinner spinnerDistortionType = view.findViewById(R.id.spinnerDistortionType);
        SeekBar seekDistortionMix = view.findViewById(R.id.seekDistortionMix);
        
        seekDistortion.setProgress(30); // 30%
        switchDistortion.setChecked(true);
        spinnerDistortionType.setSelection(0); // Soft Clip
        seekDistortionMix.setProgress(100); // 100%
        
        AudioEngine.setDistortionLevel(0.3f);
        AudioEngine.setDistortionEnabled(true);
        AudioEngine.setDistortionType(0);
        AudioEngine.setDistortionMix(1.0f);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Distorção resetada!", Toast.LENGTH_SHORT).show();
    }

    private void resetDelay() {
        View view = getView();
        if (view == null) return;
        
        SeekBar seekDelayTime = view.findViewById(R.id.seekDelayTime);
        Switch switchDelay = view.findViewById(R.id.switchDelay);
        Switch switchDelaySyncBPM = view.findViewById(R.id.switchDelaySyncBPM);
        SeekBar seekDelayBPM = view.findViewById(R.id.seekDelayBPM);
        SeekBar seekDelayFeedback = view.findViewById(R.id.seekDelayFeedback);
        SeekBar seekDelayMix = view.findViewById(R.id.seekDelayMix);
        
        seekDelayTime.setProgress(200); // 200ms
        switchDelay.setChecked(true);
        switchDelaySyncBPM.setChecked(false);
        seekDelayBPM.setProgress(60); // 120 BPM
        seekDelayFeedback.setProgress(50); // 50%
        seekDelayMix.setProgress(100); // 100%
        
        AudioEngine.setDelayTime(200.0f);
        AudioEngine.setDelayEnabled(true);
        AudioEngine.setDelaySyncBPM(false);
        AudioEngine.setDelayBPM(120);
        AudioEngine.setDelayFeedback(0.5f);
        AudioEngine.setDelayMix(1.0f);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Delay resetado!", Toast.LENGTH_SHORT).show();
    }

    private void resetReverb() {
        View view = getView();
        if (view == null) return;
        
        SeekBar seekReverb = view.findViewById(R.id.seekReverb);
        Switch switchReverb = view.findViewById(R.id.switchReverb);
        SeekBar seekReverbRoomSize = view.findViewById(R.id.seekReverbRoomSize);
        SeekBar seekReverbDamping = view.findViewById(R.id.seekReverbDamping);
        SeekBar seekReverbMix = view.findViewById(R.id.seekReverbMix);
        Spinner spinnerReverbType = view.findViewById(R.id.spinnerReverbType);
        
        seekReverb.setProgress(40); // 40%
        switchReverb.setChecked(true);
        seekReverbRoomSize.setProgress(50); // 50%
        seekReverbDamping.setProgress(50); // 50%
        seekReverbMix.setProgress(100); // 100%
        spinnerReverbType.setSelection(0); // Hall
        
        AudioEngine.setReverbLevel(0.4f);
        AudioEngine.setReverbEnabled(true);
        AudioEngine.setReverbRoomSize(0.5f);
        AudioEngine.setReverbDamping(0.5f);
        AudioEngine.setReverbMix(1.0f);
        AudioEngine.setReverbType(0);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Reverb resetado!", Toast.LENGTH_SHORT).show();
    }

    private void resetChorus() {
        View view = getView();
        if (view == null) return;
        
        Switch switchChorus = view.findViewById(R.id.switchChorus);
        SeekBar seekChorusDepth = view.findViewById(R.id.seekChorusDepth);
        SeekBar seekChorusRate = view.findViewById(R.id.seekChorusRate);
        SeekBar seekChorusMix = view.findViewById(R.id.seekChorusMix);
        
        switchChorus.setChecked(false);
        seekChorusDepth.setProgress(20); // 20%
        seekChorusRate.setProgress(10); // 10%
        seekChorusMix.setProgress(50); // 50%
        
        AudioEngine.setChorusEnabled(false);
        AudioEngine.setChorusDepth(0.008f);
        AudioEngine.setChorusRate(0.5f);
        AudioEngine.setChorusMix(0.5f);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Chorus resetado!", Toast.LENGTH_SHORT).show();
    }

    private void resetFlanger() {
        View view = getView();
        if (view == null) return;
        
        Switch switchFlanger = view.findViewById(R.id.switchFlanger);
        SeekBar seekFlangerDepth = view.findViewById(R.id.seekFlangerDepth);
        SeekBar seekFlangerRate = view.findViewById(R.id.seekFlangerRate);
        SeekBar seekFlangerFeedback = view.findViewById(R.id.seekFlangerFeedback);
        SeekBar seekFlangerMix = view.findViewById(R.id.seekFlangerMix);
        
        switchFlanger.setChecked(false);
        seekFlangerDepth.setProgress(3); // 3%
        seekFlangerRate.setProgress(3); // 3%
        seekFlangerFeedback.setProgress(50); // 50%
        seekFlangerMix.setProgress(50); // 50%
        
        AudioEngine.setFlangerEnabled(false);
        AudioEngine.setFlangerDepth(0.0003f);
        AudioEngine.setFlangerRate(0.15f);
        AudioEngine.setFlangerFeedback(0.5f);
        AudioEngine.setFlangerMix(0.5f);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Flanger resetado!", Toast.LENGTH_SHORT).show();
    }

    private void resetPhaser() {
        View view = getView();
        if (view == null) return;
        
        Switch switchPhaser = view.findViewById(R.id.switchPhaser);
        SeekBar seekPhaserDepth = view.findViewById(R.id.seekPhaserDepth);
        SeekBar seekPhaserRate = view.findViewById(R.id.seekPhaserRate);
        SeekBar seekPhaserFeedback = view.findViewById(R.id.seekPhaserFeedback);
        SeekBar seekPhaserMix = view.findViewById(R.id.seekPhaserMix);
        
        switchPhaser.setChecked(false);
        seekPhaserDepth.setProgress(80); // 80%
        seekPhaserRate.setProgress(10); // 10%
        seekPhaserFeedback.setProgress(60); // 60%
        seekPhaserMix.setProgress(50); // 50%
        
        AudioEngine.setPhaserEnabled(false);
        AudioEngine.setPhaserDepth(0.8f);
        AudioEngine.setPhaserRate(0.5f);
        AudioEngine.setPhaserFeedback(0.6f);
        AudioEngine.setPhaserMix(0.5f);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Phaser resetado!", Toast.LENGTH_SHORT).show();
    }

    private void resetEQ() {
        View view = getView();
        if (view == null) return;
        
        Switch switchEQ = view.findViewById(R.id.switchEQ);
        SeekBar seekEQLow = view.findViewById(R.id.seekEQLow);
        SeekBar seekEQMid = view.findViewById(R.id.seekEQMid);
        SeekBar seekEQHigh = view.findViewById(R.id.seekEQHigh);
        SeekBar seekEQMix = view.findViewById(R.id.seekEQMix);
        
        switchEQ.setChecked(false);
        seekEQLow.setProgress(50); // 0dB
        seekEQMid.setProgress(50); // 0dB
        seekEQHigh.setProgress(50); // 0dB
        seekEQMix.setProgress(100); // 100%
        
        AudioEngine.setEQEnabled(false);
        AudioEngine.setEQLow(0.0f);
        AudioEngine.setEQMid(0.0f);
        AudioEngine.setEQHigh(0.0f);
        AudioEngine.setEQMix(1.0f);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "EQ resetado!", Toast.LENGTH_SHORT).show();
    }

    private void resetCompressor() {
        View view = getView();
        if (view == null) return;
        
        Switch switchCompressor = view.findViewById(R.id.switchCompressor);
        SeekBar seekCompressorThreshold = view.findViewById(R.id.seekCompressorThreshold);
        SeekBar seekCompressorRatio = view.findViewById(R.id.seekCompressorRatio);
        SeekBar seekCompressorAttack = view.findViewById(R.id.seekCompressorAttack);
        SeekBar seekCompressorRelease = view.findViewById(R.id.seekCompressorRelease);
        SeekBar seekCompressorMix = view.findViewById(R.id.seekCompressorMix);
        
        switchCompressor.setChecked(false);
        seekCompressorThreshold.setProgress(67); // -20dB
        seekCompressorRatio.setProgress(16); // 4:1
        seekCompressorAttack.setProgress(10); // 10ms
        seekCompressorRelease.setProgress(9); // 100ms
        seekCompressorMix.setProgress(100); // 100%
        
        AudioEngine.setCompressorEnabled(false);
        AudioEngine.setCompressorThreshold(-20.0f);
        AudioEngine.setCompressorRatio(4.0f);
        AudioEngine.setCompressorAttack(10.0f);
        AudioEngine.setCompressorRelease(100.0f);
        AudioEngine.setCompressorMix(1.0f);
        updateBypassIndicators(view);
        
        // Feedback visual
        Toast.makeText(getContext(), "Compressor resetado!", Toast.LENGTH_SHORT).show();
    }

    // Tooltips para todos os controles restantes
    private void setupTooltips(View view) {
        // Tooltips para Chorus
        SeekBar seekChorusDepth = view.findViewById(R.id.seekChorusDepth);
        SeekBar seekChorusRate = view.findViewById(R.id.seekChorusRate);
        SeekBar seekChorusMix = view.findViewById(R.id.seekChorusMix);
        Switch switchChorus = view.findViewById(R.id.switchChorus);
        Button btnResetChorus = view.findViewById(R.id.btnResetChorus);
        
        seekChorusDepth.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_chorus_depth));
            return true;
        });
        
        seekChorusRate.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_chorus_rate));
            return true;
        });
        
        seekChorusMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_chorus_mix));
            return true;
        });
        
        switchChorus.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_chorus_enable));
            return true;
        });
        
        btnResetChorus.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        // Tooltips para Flanger
        SeekBar seekFlangerDepth = view.findViewById(R.id.seekFlangerDepth);
        SeekBar seekFlangerRate = view.findViewById(R.id.seekFlangerRate);
        SeekBar seekFlangerFeedback = view.findViewById(R.id.seekFlangerFeedback);
        SeekBar seekFlangerMix = view.findViewById(R.id.seekFlangerMix);
        Switch switchFlanger = view.findViewById(R.id.switchFlanger);
        Button btnResetFlanger = view.findViewById(R.id.btnResetFlanger);
        
        seekFlangerDepth.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_flanger_depth));
            return true;
        });
        
        seekFlangerRate.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_flanger_rate));
            return true;
        });
        
        seekFlangerFeedback.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_flanger_feedback));
            return true;
        });
        
        seekFlangerMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_flanger_mix));
            return true;
        });
        
        switchFlanger.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_flanger_enable));
            return true;
        });
        
        btnResetFlanger.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        // Tooltips para Phaser
        SeekBar seekPhaserDepth = view.findViewById(R.id.seekPhaserDepth);
        SeekBar seekPhaserRate = view.findViewById(R.id.seekPhaserRate);
        SeekBar seekPhaserFeedback = view.findViewById(R.id.seekPhaserFeedback);
        SeekBar seekPhaserMix = view.findViewById(R.id.seekPhaserMix);
        Switch switchPhaser = view.findViewById(R.id.switchPhaser);
        Button btnResetPhaser = view.findViewById(R.id.btnResetPhaser);
        
        seekPhaserDepth.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_phaser_depth));
            return true;
        });
        
        seekPhaserRate.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_phaser_rate));
            return true;
        });
        
        seekPhaserFeedback.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_phaser_feedback));
            return true;
        });
        
        seekPhaserMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_phaser_mix));
            return true;
        });
        
        switchPhaser.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_phaser_enable));
            return true;
        });
        
        btnResetPhaser.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        // Tooltips para EQ
        SeekBar seekEQLow = view.findViewById(R.id.seekEQLow);
        SeekBar seekEQMid = view.findViewById(R.id.seekEQMid);
        SeekBar seekEQHigh = view.findViewById(R.id.seekEQHigh);
        SeekBar seekEQMix = view.findViewById(R.id.seekEQMix);
        Switch switchEQ = view.findViewById(R.id.switchEQ);
        Button btnResetEQ = view.findViewById(R.id.btnResetEQ);
        
        seekEQLow.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_eq_low));
            return true;
        });
        
        seekEQMid.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_eq_mid));
            return true;
        });
        
        seekEQHigh.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_eq_high));
            return true;
        });
        
        seekEQMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_eq_mix));
            return true;
        });
        
        switchEQ.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_eq_enable));
            return true;
        });
        
        btnResetEQ.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        // Tooltips para Compressor
        SeekBar seekCompressorThreshold = view.findViewById(R.id.seekCompressorThreshold);
        SeekBar seekCompressorRatio = view.findViewById(R.id.seekCompressorRatio);
        SeekBar seekCompressorAttack = view.findViewById(R.id.seekCompressorAttack);
        SeekBar seekCompressorRelease = view.findViewById(R.id.seekCompressorRelease);
        SeekBar seekCompressorMix = view.findViewById(R.id.seekCompressorMix);
        Switch switchCompressor = view.findViewById(R.id.switchCompressor);
        Button btnResetCompressor = view.findViewById(R.id.btnResetCompressor);
        
        seekCompressorThreshold.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_compressor_threshold));
            return true;
        });
        
        seekCompressorRatio.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_compressor_ratio));
            return true;
        });
        
        seekCompressorAttack.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_compressor_attack));
            return true;
        });
        
        seekCompressorRelease.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_compressor_release));
            return true;
        });
        
        seekCompressorMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_compressor_mix));
            return true;
        });
        
        switchCompressor.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_compressor_enable));
            return true;
        });
        
        btnResetCompressor.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        // Tooltips para controles adicionais
        SeekBar seekDistortionMix = view.findViewById(R.id.seekDistortionMix);
        Spinner spinnerDistortionType = view.findViewById(R.id.spinnerDistortionType);
        Spinner spinnerReverbType = view.findViewById(R.id.spinnerReverbType);
        Button btnResetAll = view.findViewById(R.id.btnResetAll);
        Button btnResetGain = view.findViewById(R.id.btnResetGain);
        Button btnResetDistortion = view.findViewById(R.id.btnResetDistortion);
        Button btnResetDelay = view.findViewById(R.id.btnResetDelay);
        Button btnResetReverb = view.findViewById(R.id.btnResetReverb);
        
        seekDistortionMix.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_distortion_mix));
            return true;
        });
        
        spinnerDistortionType.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_distortion_type));
            return true;
        });
        
        spinnerReverbType.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reverb_type));
            return true;
        });
        
        btnResetAll.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset_all));
            return true;
        });
        
        btnResetGain.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        btnResetDistortion.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        btnResetDelay.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        btnResetReverb.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_reset));
            return true;
        });
        
        // Tooltips para Exportar/Importar
        Button btnExportPreset = view.findViewById(R.id.btnExportPreset);
        Button btnImportPreset = view.findViewById(R.id.btnImportPreset);
        
        btnExportPreset.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_export_preset));
            return true;
        });
        
        btnImportPreset.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_import_preset));
            return true;
        });
        
        // Tooltips para Favoritos
        Button btnFavoritesFilter = view.findViewById(R.id.btnFavoritesFilter);
        Button favoritePresetButton = view.findViewById(R.id.favoritePresetButton);
        
        btnFavoritesFilter.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_favorites_filter));
            return true;
        });
        
        favoritePresetButton.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_favorite_button));
            return true;
        });
        
        // Tooltips para Oversampling
        Switch switchOversampling = view.findViewById(R.id.switchOversampling);
        Spinner spinnerOversamplingFactor = view.findViewById(R.id.spinnerOversamplingFactor);
        
        switchOversampling.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_oversampling_enabled));
            return true;
        });
        
        spinnerOversamplingFactor.setOnLongClickListener(v -> {
            TooltipManager.showTooltip(getContext(), v, getString(R.string.tooltip_oversampling_factor));
            return true;
        });
    }
    
    private void showExportPresetDialog() {
        ArrayList<String> presetNames = PresetManager.getPresetNames(getContext());
        
        if (presetNames.isEmpty()) {
            Toast.makeText(getContext(), "Nenhum preset salvo para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] names = presetNames.toArray(new String[0]);
        
        new AlertDialog.Builder(getContext())
            .setTitle("Exportar Preset")
            .setItems(names, (dialog, which) -> {
                String selectedPreset = names[which];
                exportPreset(selectedPreset);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void exportPreset(String presetName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, presetName + ".json");
        
        // Salvar nome do preset temporariamente
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("temp_export_preset", presetName).apply();
        
        startActivityForResult(intent, EXPORT_PRESET_REQUEST);
    }
    
    private void selectPresetFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, IMPORT_PRESET_REQUEST);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            
            if (requestCode == EXPORT_PRESET_REQUEST) {
                // Exportar preset
                SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
                String presetName = prefs.getString("temp_export_preset", "");
                
                if (!presetName.isEmpty()) {
                    boolean success = PresetManager.savePresetToFile(getContext(), presetName, uri);
                    if (success) {
                        Toast.makeText(getContext(), getString(R.string.preset_exported), Toast.LENGTH_SHORT).show();
                        
                        // Compartilhar arquivo
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("application/json");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Preset ToneForge: " + presetName);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Compartilhando preset do ToneForge: " + presetName);
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_preset)));
                    } else {
                        Toast.makeText(getContext(), getString(R.string.preset_export_failed), Toast.LENGTH_SHORT).show();
                    }
                }
                
            } else if (requestCode == IMPORT_PRESET_REQUEST) {
                // Importar preset
                boolean success = PresetManager.importPreset(getContext(), uri);
                if (success) {
                    Toast.makeText(getContext(), getString(R.string.preset_imported), Toast.LENGTH_SHORT).show();
                    // Atualizar lista de presets
                    atualizarListaPresets();
                } else {
                    Toast.makeText(getContext(), getString(R.string.preset_import_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void toggleFavoritesFilter() {
        showFavoritesOnly = !showFavoritesOnly;
        atualizarListaPresets();
        updateFavoritesFilterButton();
    }
    
    private void updateFavoritesFilterButton() {
        View view = getView();
        if (view == null) return;
        
        Button btnFavoritesFilter = view.findViewById(R.id.btnFavoritesFilter);
        if (showFavoritesOnly) {
            btnFavoritesFilter.setText(getString(R.string.show_all_presets));
        } else {
            btnFavoritesFilter.setText(getString(R.string.show_favorites_only));
        }
    }

    private void toggleFavoritePreset() {
        if (presetSpinner.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Selecione um preset primeiro", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String selectedPreset = presetSpinner.getSelectedItem().toString();
        FavoritesManager.toggleFavorite(getContext(), selectedPreset);
        
        boolean isFavorite = FavoritesManager.isFavorite(getContext(), selectedPreset);
        if (isFavorite) {
            Toast.makeText(getContext(), getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
        }
        
        // Atualizar interface
        atualizarListaPresets();
        updateFavoriteButton();
    }
    
    private void updateFavoriteButton() {
        View view = getView();
        if (view == null) return;
        
        Button favoritePresetButton = view.findViewById(R.id.favoritePresetButton);
        if (presetSpinner.getSelectedItem() == null) {
            favoritePresetButton.setText("❤");
            return;
        }
        
        String selectedPreset = presetSpinner.getSelectedItem().toString();
        boolean isFavorite = FavoritesManager.isFavorite(getContext(), selectedPreset);
        
        if (isFavorite) {
            favoritePresetButton.setText("❤");
            favoritePresetButton.setTextColor(getResources().getColor(R.color.red));
        } else {
            favoritePresetButton.setText("🤍");
            favoritePresetButton.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void updateChainView() {
        View view = getView();
        if (view == null) return;
        LinearLayout chainContainer = view.findViewById(R.id.chainContainer);
        if (chainContainer == null) return;
        chainContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String effect : effectOrder) {
            View block = inflater.inflate(R.layout.chain_effect_block, chainContainer, false);
            ImageView icon = block.findViewById(R.id.effectIcon);
            TextView name = block.findViewById(R.id.effectName);
            View status = block.findViewById(R.id.effectStatus);

            name.setText(effect);
            // Ícone por efeito
            switch (effect) {
                case "Ganho":
                    icon.setImageResource(R.drawable.ic_volume_up);
                    status.setBackgroundResource(isEffectEnabled("Ganho") ? R.color.green : R.color.red);
                    break;
                case "Distorção":
                    icon.setImageResource(R.drawable.ic_launcher_foreground);
                    status.setBackgroundResource(isEffectEnabled("Distorção") ? R.color.green : R.color.red);
                    break;
                case "Chorus":
                    icon.setImageResource(R.drawable.ic_waves);
                    status.setBackgroundResource(isEffectEnabled("Chorus") ? R.color.green : R.color.red);
                    break;
                case "Flanger":
                    icon.setImageResource(R.drawable.ic_rotate_left);
                    status.setBackgroundResource(isEffectEnabled("Flanger") ? R.color.green : R.color.red);
                    break;
                case "Phaser":
                    icon.setImageResource(R.drawable.ic_target);
                    status.setBackgroundResource(isEffectEnabled("Phaser") ? R.color.green : R.color.red);
                    break;
                case "EQ":
                    icon.setImageResource(R.drawable.ic_graphic_eq);
                    status.setBackgroundResource(isEffectEnabled("EQ") ? R.color.green : R.color.red);
                    break;
                case "Compressor":
                    icon.setImageResource(R.drawable.ic_mic);
                    status.setBackgroundResource(isEffectEnabled("Compressor") ? R.color.green : R.color.red);
                    break;
                case "Delay":
                    icon.setImageResource(R.drawable.ic_av_timer);
                    status.setBackgroundResource(isEffectEnabled("Delay") ? R.color.green : R.color.red);
                    break;
                case "Reverb":
                    icon.setImageResource(R.drawable.ic_waves);
                    status.setBackgroundResource(isEffectEnabled("Reverb") ? R.color.green : R.color.red);
                    break;
                default:
                    icon.setImageResource(R.drawable.ic_launcher_foreground);
                    status.setBackgroundResource(R.color.light_gray);
            }
            chainContainer.addView(block);
        }
    }

    // Método auxiliar para saber se o efeito está ativado
    private boolean isEffectEnabled(String effect) {
        View view = getView();
        if (view == null) return false;
        switch (effect) {
            case "Ganho":
                return ((Switch)view.findViewById(R.id.switchGain)).isChecked();
            case "Distorção":
                return ((Switch)view.findViewById(R.id.switchDistortion)).isChecked();
            case "Chorus":
                return ((Switch)view.findViewById(R.id.switchChorus)).isChecked();
            case "Flanger":
                return ((Switch)view.findViewById(R.id.switchFlanger)).isChecked();
            case "Phaser":
                return ((Switch)view.findViewById(R.id.switchPhaser)).isChecked();
            case "EQ":
                return ((Switch)view.findViewById(R.id.switchEQ)).isChecked();
            case "Compressor":
                return ((Switch)view.findViewById(R.id.switchCompressor)).isChecked();
            case "Delay":
                return ((Switch)view.findViewById(R.id.switchDelay)).isChecked();
            case "Reverb":
                return ((Switch)view.findViewById(R.id.switchReverb)).isChecked();
            default:
                return false;
        }
    }

    private void updateOversamplingQualityText(TextView textView, boolean enabled, int factor) {
        if (!enabled) {
            textView.setText("Desativado - Performance Máxima");
            textView.setTextColor(getResources().getColor(R.color.red));
        } else {
            String quality;
            int color;
            switch (factor) {
                case 1:
                    quality = "Sem Oversampling - Performance";
                    color = R.color.yellow;
                    break;
                case 2:
                    quality = "Qualidade Boa - Performance Média";
                    color = R.color.green;
                    break;
                case 4:
                    quality = "Qualidade Alta - Performance Baixa";
                    color = R.color.green;
                    break;
                case 8:
                    quality = "Qualidade Máxima - Performance Mínima";
                    color = R.color.green;
                    break;
                default:
                    quality = "Qualidade Padrão";
                    color = R.color.white;
            }
            textView.setText(quality);
            textView.setTextColor(getResources().getColor(color));
        }
    }
} 