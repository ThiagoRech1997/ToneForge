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

public class EffectsFragment extends Fragment {
    private TextView statusText;
    private ArrayAdapter<String> presetAdapter;
    private ArrayList<EffectPreset> presetList;
    private Spinner presetSpinner;
    private EditText presetNameEdit;
    private Button savePresetButton, deletePresetButton;
    private EffectOrderAdapter effectOrderAdapter;
    private RecyclerView effectsOrderRecycler;
    private ArrayList<String> effectOrder;

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
            updateStatus();
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

        // Distorção
        Switch switchDistortion = view.findViewById(R.id.switchDistortion);
        SeekBar seekDistortion = view.findViewById(R.id.seekDistortion);
        switchDistortion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setDistortionEnabled(isChecked);
            updateStatus();
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

        // Delay
        Switch switchDelay = view.findViewById(R.id.switchDelay);
        SeekBar seekDelayTime = view.findViewById(R.id.seekDelayTime);
        Switch switchDelaySyncBPM = view.findViewById(R.id.switchDelaySyncBPM);
        SeekBar seekDelayBPM = view.findViewById(R.id.seekDelayBPM);
        SeekBar seekDelayFeedback = view.findViewById(R.id.seekDelayFeedback);
        SeekBar seekDelayMix = view.findViewById(R.id.seekDelayMix);
        
        switchDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setDelayEnabled(isChecked);
            updateStatus();
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

        // Reverb
        Switch switchReverb = view.findViewById(R.id.switchReverb);
        SeekBar seekReverb = view.findViewById(R.id.seekReverb);
        switchReverb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setReverbEnabled(isChecked);
            updateStatus();
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

        // Chorus
        Switch switchChorus = view.findViewById(R.id.switchChorus);
        SeekBar seekChorusDepth = view.findViewById(R.id.seekChorusDepth);
        SeekBar seekChorusRate = view.findViewById(R.id.seekChorusRate);
        SeekBar seekChorusMix = view.findViewById(R.id.seekChorusMix);
        switchChorus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setChorusEnabled(isChecked);
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
        presetAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, presetNames);
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

        presetSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < presetList.size()) {
                    applyPreset(presetList.get(position));
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        deletePresetButton.setOnClickListener(v -> {
            int pos = presetSpinner.getSelectedItemPosition();
            if (pos >= 0 && pos < presetList.size()) {
                deletePreset(presetList.get(pos));
                atualizarListaPresets();
                Toast.makeText(getContext(), "Preset excluído!", Toast.LENGTH_SHORT).show();
            }
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
        effectOrderAdapter = new EffectOrderAdapter(effectOrder, newOrder -> {
            effectOrder = new ArrayList<>(newOrder);
            saveEffectOrderToPrefs(effectOrder);
            // Enviar ordem para o C++
            AudioEngine.setEffectOrder(effectOrder.toArray(new String[0]));
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

        // Tipo de Reverb
        Spinner spinnerReverbType = view.findViewById(R.id.spinnerReverbType);
        spinnerReverbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                AudioEngine.setReverbType(position); // 0=Hall, 1=Plate, 2=Spring
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

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
        presetList = loadPresets();
        ArrayList<String> presetNames = new ArrayList<>();
        for (EffectPreset p : presetList) presetNames.add(p.name);
        presetAdapter.clear();
        presetAdapter.addAll(presetNames);
        presetAdapter.notifyDataSetChanged();
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
} 