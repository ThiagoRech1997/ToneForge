package com.thiagofernendorech.toneforge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import java.util.List;

/**
 * Fragment que implementa a interface de pedaleira real
 * Cada efeito é exibido como um pedal visual interativo
 */
public class PedalboardFragment extends Fragment implements PedalboardAdapter.PedalInteractionListener {
    
    private RecyclerView pedalboardRecyclerView;
    private PedalboardAdapter pedalboardAdapter;
    private List<PedalEffect> pedalEffects;
    
    private CardView pedalboardStatus;
    private TextView pipelineStatusText;
    private TextView activePedalsCount;
    private TextView cpuUsage;
    private TextView latencyInfo;
    private TextView presetInfo;
    
    private LinearLayout btnResetPedalboard;
    private LinearLayout btnSavePreset;
    private LinearLayout btnAddEffect;
    
    private LinearLayout signalChainContainer;
    
    // Gerenciadores
    private PresetManager presetManager;
    private TooltipManager tooltipManager;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedalboard, container, false);
        
        initializeViews(view);
        initializePedals();
        setupRecyclerView();
        setupButtons();
        setupStatusUpdates();
        
        return view;
    }
    
    private void initializeViews(View view) {
        pedalboardRecyclerView = view.findViewById(R.id.pedalboardRecyclerView);
        pedalboardStatus = view.findViewById(R.id.pedalboardStatusCard);
        pipelineStatusText = view.findViewById(R.id.pipelineStatusText);
        activePedalsCount = view.findViewById(R.id.activePedalsCount);
        cpuUsage = view.findViewById(R.id.cpuUsage);
        latencyInfo = view.findViewById(R.id.latencyInfo);
        presetInfo = view.findViewById(R.id.presetInfo);
        
        btnResetPedalboard = view.findViewById(R.id.btnResetPedalboard);
        btnSavePreset = view.findViewById(R.id.btnSavePreset);
        btnAddEffect = view.findViewById(R.id.btnAddEffect);
        
        signalChainContainer = view.findViewById(R.id.signalChainContainer);
    }
    
    private void initializePedals() {
        // Criar lista de pedais baseada nos efeitos existentes
        pedalEffects = PedalEffect.createDefaultPedalboard();
        
        // Inicializar gerenciadores
        presetManager = new PresetManager();
        tooltipManager = new TooltipManager();
    }
    
    private void setupRecyclerView() {
        // Configurar GridLayoutManager para layout tipo pedaleira
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        pedalboardRecyclerView.setLayoutManager(layoutManager);
        
        // Criar adaptador
        pedalboardAdapter = new PedalboardAdapter(getContext(), pedalEffects, this);
        pedalboardRecyclerView.setAdapter(pedalboardAdapter);
        
        // Configurar drag & drop
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, 
                                @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                pedalboardAdapter.onItemMove(fromPosition, toPosition);
                return true;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Não permitir swipe para deletar
            }
        });
        
        itemTouchHelper.attachToRecyclerView(pedalboardRecyclerView);
    }
    
    private void setupButtons() {
        btnResetPedalboard.setOnClickListener(v -> resetPedalboard());
        btnSavePreset.setOnClickListener(v -> saveCurrentPreset());
        btnAddEffect.setOnClickListener(v -> addNewEffect());
        
        // Configurar tooltips
        btnResetPedalboard.setOnLongClickListener(v -> {
            showTooltip("Reseta todos os pedais para configuração padrão");
            return true;
        });
        
        btnSavePreset.setOnLongClickListener(v -> {
            showTooltip("Salva a configuração atual como preset");
            return true;
        });
        

        
        btnAddEffect.setOnLongClickListener(v -> {
            showTooltip("Adiciona um novo efeito à pedaleira");
            return true;
        });
    }
    
    private void setupStatusUpdates() {
        // Configurar atualizações periódicas do status
        updatePedalboardStatus();
        updateSignalChain();
    }
    
    // === IMPLEMENTAÇÃO DA INTERFACE PedalInteractionListener ===
    
    @Override
    public void onPedalEnabled(String effectName, boolean enabled) {
        // Aplicar no AudioEngine
        switch (effectName) {
            case "Gain":
                AudioEngine.setGainEnabled(enabled);
                break;
            case "Overdrive":
            case "Distortion":
                AudioEngine.setDistortionEnabled(enabled);
                break;
            case "Delay":
                AudioEngine.setDelayEnabled(enabled);
                break;
            case "Reverb":
                AudioEngine.setReverbEnabled(enabled);
                break;
            case "Chorus":
                AudioEngine.setChorusEnabled(enabled);
                break;
            case "Flanger":
                AudioEngine.setFlangerEnabled(enabled);
                break;
            case "Phaser":
                AudioEngine.setPhaserEnabled(enabled);
                break;
            case "EQ":
                AudioEngine.setEQEnabled(enabled);
                break;
            case "Compressor":
                AudioEngine.setCompressorEnabled(enabled);
                break;
        }
        
        // Atualizar status
        updatePedalboardStatus();
        updateSignalChain();
        
        // Feedback visual
        Toast.makeText(getContext(), 
            effectName + (enabled ? " ativado" : " desativado"), 
            Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onPedalParameterChanged(String effectName, String parameter, float value) {
        // Aplicar parâmetro no AudioEngine
        switch (effectName) {
            case "Gain":
                if ("level".equals(parameter)) {
                    AudioEngine.setGainLevel(value);
                }
                break;
            case "Overdrive":
            case "Distortion":
                if ("drive".equals(parameter) || "dist".equals(parameter)) {
                    AudioEngine.setDistortionLevel(value);
                }
                break;
            case "Delay":
                if ("time".equals(parameter)) {
                    AudioEngine.setDelayTime(value);
                } else if ("feedback".equals(parameter)) {
                    AudioEngine.setDelayFeedback(value);
                } else if ("mix".equals(parameter)) {
                    AudioEngine.setDelayMix(value);
                }
                break;
            case "Reverb":
                if ("room".equals(parameter)) {
                    AudioEngine.setReverbRoomSize(value);
                } else if ("damping".equals(parameter)) {
                    AudioEngine.setReverbDamping(value);
                } else if ("mix".equals(parameter)) {
                    AudioEngine.setReverbMix(value);
                }
                break;
            case "Chorus":
                if ("depth".equals(parameter)) {
                    AudioEngine.setChorusDepth(value);
                } else if ("rate".equals(parameter)) {
                    AudioEngine.setChorusRate(value);
                } else if ("mix".equals(parameter)) {
                    AudioEngine.setChorusMix(value);
                }
                break;
            case "Flanger":
                if ("depth".equals(parameter)) {
                    AudioEngine.setFlangerDepth(value);
                } else if ("rate".equals(parameter)) {
                    AudioEngine.setFlangerRate(value);
                } else if ("feedback".equals(parameter)) {
                    AudioEngine.setFlangerFeedback(value);
                }
                break;
            case "Phaser":
                if ("depth".equals(parameter)) {
                    AudioEngine.setPhaserDepth(value);
                } else if ("rate".equals(parameter)) {
                    AudioEngine.setPhaserRate(value);
                } else if ("feedback".equals(parameter)) {
                    AudioEngine.setPhaserFeedback(value);
                }
                break;
            case "EQ":
                if ("freq".equals(parameter)) {
                    // Não há setEQFrequency, vamos usar setEQLow, setEQMid ou setEQHigh dependendo do valor
                    if (value < 0.33f) {
                        AudioEngine.setEQLow(value * 3.0f); // Mapear para frequências baixas
                    } else if (value < 0.66f) {
                        AudioEngine.setEQMid((value - 0.33f) * 3.0f); // Mapear para frequências médias
                    } else {
                        AudioEngine.setEQHigh((value - 0.66f) * 3.0f); // Mapear para frequências altas
                    }
                } else if ("gain".equals(parameter)) {
                    // Aplicar ganho em todas as bandas proporcionalmente
                    AudioEngine.setEQLow(value);
                    AudioEngine.setEQMid(value);
                    AudioEngine.setEQHigh(value);
                } else if ("q".equals(parameter)) {
                    // Q factor não está disponível, vamos usar o mix como aproximação
                    AudioEngine.setEQMix(value);
                }
                break;
            case "Compressor":
                if ("ratio".equals(parameter)) {
                    AudioEngine.setCompressorRatio(value);
                } else if ("attack".equals(parameter)) {
                    AudioEngine.setCompressorAttack(value);
                } else if ("release".equals(parameter)) {
                    AudioEngine.setCompressorRelease(value);
                }
                break;
        }
    }
    
    @Override
    public void onPedalOrderChanged(List<String> newOrder) {
        // Aplicar nova ordem no AudioEngine
        AudioEngine.setEffectOrder(newOrder.toArray(new String[0]));
        
        // Atualizar cadeia de sinal
        updateSignalChain();
        
        // Feedback
        Toast.makeText(getContext(), "Ordem dos efeitos alterada", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onPedalClicked(String effectName) {
        // Abrir tela de detalhes do pedal
        for (PedalEffect pedal : pedalEffects) {
            if (pedal.getName().equals(effectName)) {
                openPedalDetailActivity(pedal);
                break;
            }
        }
    }
    
    private void openPedalDetailActivity(PedalEffect pedal) {
        Intent intent = new Intent(getActivity(), PedalDetailActivity.class);
        intent.putExtra("pedal_type", pedal.getType());
        intent.putExtra("pedal_position", pedal.getPosition());
        startActivityForResult(intent, 1001);
    }
    
    @Override
    public void onPedalLongClicked(String effectName) {
        // Long click para toggle rápido
        for (PedalEffect pedal : pedalEffects) {
            if (pedal.getName().equals(effectName)) {
                boolean newState = !pedal.isEnabled();
                pedal.setEnabled(newState);
                onPedalEnabled(effectName, newState);
                pedalboardAdapter.notifyDataSetChanged();
                showTooltip("Pedal: " + effectName + (newState ? " ativado" : " desativado"));
                break;
            }
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            // Atualizar pedal com configurações modificadas
            PedalEffect updatedPedal = (PedalEffect) data.getSerializableExtra("updated_pedal");
            if (updatedPedal != null) {
                updatePedalConfiguration(updatedPedal);
            }
        }
    }
    
    private void updatePedalConfiguration(PedalEffect updatedPedal) {
        // Encontrar e atualizar o pedal na lista
        for (int i = 0; i < pedalEffects.size(); i++) {
            PedalEffect pedal = pedalEffects.get(i);
            if (pedal.getPosition() == updatedPedal.getPosition()) {
                pedalEffects.set(i, updatedPedal);
                onPedalEnabled(updatedPedal.getName(), updatedPedal.isEnabled());
                break;
            }
        }
        
        pedalboardAdapter.notifyDataSetChanged();
        updateSignalChain();
        updatePedalboardStatus();
    }
    
    // === MÉTODOS AUXILIARES ===
    
    private void resetPedalboard() {
        // Resetar todos os pedais
        for (PedalEffect pedal : pedalEffects) {
            pedal.setEnabled(true);
            pedal.setMainValue(0.5f);
            pedal.setSecondaryKnob1Value(0.5f);
            pedal.setSecondaryKnob2Value(0.5f);
        }
        
        // Resetar AudioEngine
        // AudioEngine.resetAllEffects(); // Método não existe
        // Resetar cada efeito individualmente
        AudioEngine.setGainEnabled(false);
        AudioEngine.setGainLevel(0.0f);
        AudioEngine.setDistortionEnabled(false);
        AudioEngine.setDistortionLevel(0.0f);
        AudioEngine.setDelayEnabled(false);
        AudioEngine.setDelayLevel(0.0f);
        AudioEngine.setReverbEnabled(false);
        AudioEngine.setReverbLevel(0.0f);
        AudioEngine.setChorusEnabled(false);
        AudioEngine.setFlangerEnabled(false);
        AudioEngine.setPhaserEnabled(false);
        AudioEngine.setEQEnabled(false);
        AudioEngine.setCompressorEnabled(false);
        
        // Atualizar interface
        pedalboardAdapter.notifyDataSetChanged();
        updatePedalboardStatus();
        updateSignalChain();
        
        Toast.makeText(getContext(), "Pedaleira resetada!", Toast.LENGTH_SHORT).show();
    }
    
    private void saveCurrentPreset() {
        // Implementar salvamento de preset
        Toast.makeText(getContext(), "Salvando preset...", Toast.LENGTH_SHORT).show();
        // TODO: Implementar diálogo de salvamento
    }
    
    private void loadPreset() {
        // Implementar carregamento de preset
        Toast.makeText(getContext(), "Carregando preset...", Toast.LENGTH_SHORT).show();
        // TODO: Implementar diálogo de carregamento
    }
    
    private void addNewEffect() {
        // Implementar adição de novo efeito
        Toast.makeText(getContext(), "Adicionando efeito...", Toast.LENGTH_SHORT).show();
        // TODO: Implementar diálogo de seleção de efeito
    }
    
    private void updatePedalboardStatus() {
        // Contar pedais ativos
        int activePedals = 0;
        for (PedalEffect pedal : pedalEffects) {
            if (pedal.isEnabled()) {
                activePedals++;
            }
        }
        
        // Atualizar contadores
        activePedalsCount.setText(String.valueOf(activePedals));
        
        // Simular CPU usage (TODO: implementar cálculo real)
        int cpuUsagePercent = activePedals * 5; // 5% por pedal ativo
        cpuUsage.setText(cpuUsagePercent + "%");
        
        // Simular latência (TODO: implementar cálculo real)
        int latencyMs = 2 + (activePedals / 2); // 2ms base + 0.5ms por pedal
        latencyInfo.setText(latencyMs + "ms");
        
        // Atualizar status geral
        if (AudioEngine.isAudioPipelineRunning()) {
            // Encontrar o primeiro TextView dentro do LinearLayout
            if (pedalboardStatus.getChildCount() > 0) {
                View firstChild = pedalboardStatus.getChildAt(1); // O segundo filho é o TextView
                if (firstChild instanceof TextView) {
                    TextView statusText = (TextView) firstChild;
                    statusText.setText("🟢 Pipeline Ativo - Processando em Tempo Real");
                    statusText.setTextColor(getResources().getColor(R.color.lava_green));
                }
            }
        } else {
            // Encontrar o primeiro TextView dentro do LinearLayout
            if (pedalboardStatus.getChildCount() > 0) {
                View firstChild = pedalboardStatus.getChildAt(1); // O segundo filho é o TextView
                if (firstChild instanceof TextView) {
                    TextView statusText = (TextView) firstChild;
                    statusText.setText("🔴 Pipeline Inativo");
                    statusText.setTextColor(getResources().getColor(R.color.lava_red));
                }
            }
        }
    }
    
    private void updateSignalChain() {
        // Limpar cadeia atual
        signalChainContainer.removeAllViews();
        
        // Adicionar cada efeito ativo na cadeia
        for (PedalEffect pedal : pedalEffects) {
            if (pedal.isEnabled()) {
                TextView chainItem = new TextView(getContext());
                chainItem.setText(pedal.getName());
                chainItem.setTextColor(getResources().getColor(R.color.white));
                chainItem.setTextSize(10);
                chainItem.setPadding(8, 4, 8, 4);
                chainItem.setBackgroundColor(pedal.getBackgroundColor());
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(4, 0, 4, 0);
                chainItem.setLayoutParams(params);
                
                signalChainContainer.addView(chainItem);
                
                // Adicionar seta se não for o último
                if (signalChainContainer.getChildCount() > 1) {
                    TextView arrow = new TextView(getContext());
                    arrow.setText("→");
                    arrow.setTextColor(getResources().getColor(R.color.white));
                    arrow.setPadding(8, 4, 8, 4);
                    signalChainContainer.addView(arrow, signalChainContainer.getChildCount() - 1);
                }
            }
        }
    }
    
    private void showTooltip(String message) {
        if (tooltipManager != null) {
            tooltipManager.showTooltip(getContext(), getView(), message);
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Iniciar pipeline de áudio
        AudioEngine.startAudioPipeline();
        updatePedalboardStatus();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Não parar pipeline para permitir background
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Limpar recursos
        AudioEngine.stopAudioPipeline();
    }
} 