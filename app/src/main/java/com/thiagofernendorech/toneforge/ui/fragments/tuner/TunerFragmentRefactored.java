package com.thiagofernendorech.toneforge.ui.fragments.tuner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;

/**
 * TunerFragment refatorado usando arquitetura MVP
 * Versão limpa e modular do fragment original
 */
public class TunerFragmentRefactored extends BaseFragment<TunerPresenter> implements TunerContract.View {
    
    // === UI COMPONENTS ===
    
    // Status e controles principais
    private TextView tunerNote;
    private TextView tunerFreq;
    private TextView tunerCents;
    private Button tunerToggleButton;
    private TextView tunerStatusText;
    
    // Notas de referência
    private Button btnNoteE;
    private Button btnNoteA;
    private Button btnNoteD;
    private Button btnNoteG;
    private Button btnNoteB;
    private Button btnNoteE2;
    
    // Indicadores visuais
    private ProgressBar tuningIndicator;
    private TextView accuracyText;
    private TextView inputLevelText;
    
    // Controles de calibração
    private SeekBar calibrationSeekBar;
    private TextView calibrationText;
    private Button resetCalibrationButton;
    
    // Controles de sensibilidade
    private SeekBar sensitivitySeekBar;
    private TextView sensitivityText;
    private SeekBar thresholdSeekBar;
    private TextView thresholdText;
    
    // Temperamento
    private Spinner temperamentSpinner;
    private Button temperamentButton;
    
    // Configurações
    private Button settingsButton;
    private Button helpButton;
    
    @Override
    protected TunerPresenter createPresenter() {
        AudioRepository audioRepository = AudioRepository.getInstance(requireContext());
        return new TunerPresenter(requireContext(), audioRepository);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tuner, container, false);
        
        initializeViews(view);
        setupClickListeners();
        setupSeekBarListeners();
        setupSpinnerListeners();
        
        return view;
    }
    
    @Override
    protected void onViewReady() {
        super.onViewReady();
        if (presenter != null) {
            presenter.onViewStarted();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.onViewResumed();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.onViewPaused();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onViewDestroyed();
        }
    }
    
    // === INICIALIZAÇÃO ===
    
    private void initializeViews(View view) {
        // Status e controles principais
        tunerNote = view.findViewById(R.id.tunerNote);
        tunerFreq = view.findViewById(R.id.tunerFreq);
        tunerCents = view.findViewById(R.id.tunerCents);
        tunerToggleButton = view.findViewById(R.id.tunerToggleButton);
        // tunerStatusText = view.findViewById(R.id.tunerStatusText); // ID não existe no layout
        
        // Notas de referência
        btnNoteE = view.findViewById(R.id.btnNoteE);
        btnNoteA = view.findViewById(R.id.btnNoteA);
        btnNoteD = view.findViewById(R.id.btnNoteD);
        btnNoteG = view.findViewById(R.id.btnNoteG);
        btnNoteB = view.findViewById(R.id.btnNoteB);
        btnNoteE2 = view.findViewById(R.id.btnNoteE2);
        
        // Indicadores visuais - IDs não existem no layout atual
        // tuningIndicator = view.findViewById(R.id.tuningIndicator);
        // accuracyText = view.findViewById(R.id.accuracyText);
        // inputLevelText = view.findViewById(R.id.inputLevelText);
        
        // Controles de calibração - IDs não existem no layout atual
        // calibrationSeekBar = view.findViewById(R.id.calibrationSeekBar);
        // calibrationText = view.findViewById(R.id.calibrationText);
        // resetCalibrationButton = view.findViewById(R.id.resetCalibrationButton);
        
        // Controles de sensibilidade - IDs não existem no layout atual
        // sensitivitySeekBar = view.findViewById(R.id.sensitivitySeekBar);
        // sensitivityText = view.findViewById(R.id.sensitivityText);
        // thresholdSeekBar = view.findViewById(R.id.thresholdSeekBar);
        // thresholdText = view.findViewById(R.id.thresholdText);
        
        // Temperamento - IDs não existem no layout atual
        // temperamentSpinner = view.findViewById(R.id.temperamentSpinner);
        // temperamentButton = view.findViewById(R.id.temperamentButton);
        
        // Configurações - IDs não existem no layout atual
        // settingsButton = view.findViewById(R.id.settingsButton);
        // helpButton = view.findViewById(R.id.helpButton);
    }
    
    private void setupClickListeners() {
        // Botão principal de toggle
        tunerToggleButton.setOnClickListener(v -> {
            if (presenter != null) presenter.toggleTuner();
        });
        
        // Notas de referência
        btnNoteE.setOnClickListener(v -> {
            if (presenter != null) presenter.setReferenceNote("E");
        });
        
        btnNoteA.setOnClickListener(v -> {
            if (presenter != null) presenter.setReferenceNote("A");
        });
        
        btnNoteD.setOnClickListener(v -> {
            if (presenter != null) presenter.setReferenceNote("D");
        });
        
        btnNoteG.setOnClickListener(v -> {
            if (presenter != null) presenter.setReferenceNote("G");
        });
        
        btnNoteB.setOnClickListener(v -> {
            if (presenter != null) presenter.setReferenceNote("B");
        });
        
        btnNoteE2.setOnClickListener(v -> {
            if (presenter != null) presenter.setReferenceNote("E");
        });
        
        // Controles de calibração - comentado pois os IDs não existem no layout
        // resetCalibrationButton.setOnClickListener(v -> {
        //     if (presenter != null) presenter.resetCalibration();
        // });
        
        // Temperamento - comentado pois os IDs não existem no layout
        // temperamentButton.setOnClickListener(v -> {
        //     if (presenter != null) presenter.openTemperamentSelector();
        // });
        
        // Configurações - comentado pois os IDs não existem no layout
        // settingsButton.setOnClickListener(v -> {
        //     if (presenter != null) presenter.openSettings();
        // });
        
        // helpButton.setOnClickListener(v -> {
        //     if (presenter != null) presenter.openHelp();
        // });
    }
    
    private void setupSeekBarListeners() {
        // Comentado pois os IDs não existem no layout atual
        // // Calibração
        // calibrationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        //     @Override
        //     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //         if (fromUser && presenter != null) {
        //             int offset = progress - 50; // -50 a +50 cents
        //             presenter.setCalibrationOffset(offset);
        //         }
        //     }
        //     
        //     @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        //     @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        // });
        // 
        // // Sensibilidade
        // sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        //     @Override
        //     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //         if (fromUser && presenter != null) {
        //             float sensitivity = progress / 100.0f; // 0.0 a 1.0
        //             presenter.setSensitivity(sensitivity);
        //         }
        //     }
        //     
        //     @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        //     @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        // });
        // 
        // // Threshold
        // thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        //     @Override
        //     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //         if (fromUser && presenter != null) {
        //                     float threshold = -60.0f + (progress / 100.0f) * 40.0f; // -60 a -20 dB
        //                     presenter.setDetectionThreshold(threshold);
        //                 }
        //             }
        //             
        //             @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        //             @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        //         });
    }
    
    private void setupSpinnerListeners() {
        // Comentado pois os IDs não existem no layout atual
        // temperamentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //     @Override
        //     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //         if (presenter != null) {
        //             String temperament = parent.getItemAtPosition(position).toString();
        //             presenter.setTemperament(temperament);
        //         }
        //     }
        //     
        //     @Override
        //     public void onNothingSelected(AdapterView<?> parent) {}
        // });
    }
    
    // === Implementação da interface TunerContract.View ===
    
    @Override
    public void updateTunerStatus(boolean isActive, String statusText) {
        // tunerStatusText não existe no layout atual
        // if (tunerStatusText != null) {
        //     tunerStatusText.setText(statusText);
        // }
        
        if (tunerToggleButton != null) {
            tunerToggleButton.setText(isActive ? "Parar" : "Iniciar");
        }
    }
    
    @Override
    public void updateDetectedNote(String note, int octave) {
        if (tunerNote != null) {
            if (note.isEmpty() || "-".equals(note)) {
                tunerNote.setText("-");
                tunerNote.setTextColor(getResources().getColor(R.color.lava_text_primary));
            } else {
                tunerNote.setText(note);
                // Cor será definida pelo updateCentsDeviation
            }
        }
    }
    
    @Override
    public void updateDetectedFrequency(float frequency) {
        if (tunerFreq != null) {
            if (frequency > 0) {
                tunerFreq.setText(String.format("Frequência: %.1f Hz", frequency));
            } else {
                tunerFreq.setText("");
            }
        }
    }
    
    @Override
    public void updateCentsDeviation(int cents) {
        if (tunerCents != null) {
            if (cents != 0) {
                tunerCents.setText(String.format("%s%d cents", (cents > 0 ? "+" : ""), cents));
                
                // Definir cor baseada na precisão
                int absCents = Math.abs(cents);
                int colorRes;
                
                if (absCents <= 5) {
                    colorRes = R.color.lava_green;
                } else if (absCents <= 15) {
                    colorRes = R.color.lava_yellow;
                } else {
                    colorRes = R.color.lava_red;
                }
                
                tunerCents.setTextColor(getResources().getColor(colorRes));
                if (tunerNote != null) {
                    tunerNote.setTextColor(getResources().getColor(colorRes));
                }
            } else {
                tunerCents.setText("");
                if (tunerNote != null) {
                    tunerNote.setTextColor(getResources().getColor(R.color.lava_text_primary));
                }
            }
        }
    }
    
    @Override
    public void updateTuningAccuracy(float accuracy) {
        // accuracyText não existe no layout atual
        // if (accuracyText != null) {
        //     if (accuracy > 0) {
        //         String accuracyStr;
        //         if (accuracy >= 0.9f) {
        //             accuracyStr = "Perfeito";
        //         } else if (accuracy >= 0.7f) {
        //             accuracyStr = "Bom";
        //         } else if (accuracy >= 0.3f) {
        //             accuracyStr = "Regular";
        //         } else {
        //             accuracyStr = "Ruim";
        //         }
        //         accuracyText.setText("Precisão: " + accuracyStr);
        //     } else {
        //         accuracyText.setText("");
        //     }
        // }
    }
    
    @Override
    public void updateReferenceNote(String note) {
        // Atualizar botões de referência
        updateReferenceNoteButtons(note);
    }
    
    @Override
    public void updateReferenceFrequency(float frequency) {
        // Implementação futura se necessário
    }
    
    @Override
    public void updateCalibrationOffset(int offset) {
        // Controles de calibração não existem no layout atual
        // if (calibrationSeekBar != null) {
        //     calibrationSeekBar.setProgress(offset + 50); // Converter de -50/+50 para 0/100
        // }
        // 
        // if (calibrationText != null) {
        //     calibrationText.setText(String.format("Calibração: %s%d cents", 
        //         (offset > 0 ? "+" : ""), offset));
        // }
    }
    
    @Override
    public void showCalibrationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Calibração")
            .setMessage("Use o slider para ajustar a calibração do afinador")
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void updateTemperament(String temperament) {
        // temperamentSpinner não existe no layout atual
        // if (temperamentSpinner != null) {
        //     // Encontrar e selecionar o temperamento no spinner
        //     ArrayAdapter adapter = (ArrayAdapter) temperamentSpinner.getAdapter();
        //     if (adapter != null) {
        //         for (int i = 0; i < adapter.getCount(); i++) {
        //             if (temperament.equals(adapter.getItem(i))) {
        //                 temperamentSpinner.setSelection(i);
        //                 break;
        //             }
        //         }
        //     }
        // }
    }
    
    @Override
    public void showTemperamentSelector() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Selecionar Temperamento")
            .setItems(new String[]{"Equal Temperament", "Just Intonation", "Pythagorean"}, 
                (dialog, which) -> {
                    String[] temperaments = {"Equal Temperament", "Just Intonation", "Pythagorean"};
                    if (presenter != null) {
                        presenter.setTemperament(temperaments[which]);
                    }
                })
            .show();
    }
    
    @Override
    public void updateTemperamentList(String[] temperaments) {
        // temperamentSpinner não existe no layout atual
        // if (temperamentSpinner != null) {
        //     ArrayAdapter<String> adapter = new ArrayAdapter<>(
        //         requireContext(), 
        //         android.R.layout.simple_spinner_item, 
        //         temperaments
        //     );
        //     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //     temperamentSpinner.setAdapter(adapter);
        // }
    }
    
    @Override
    public void updateReferenceNoteButtons(String selectedNote) {
        // Resetar todos os botões
        resetNoteButton(btnNoteE);
        resetNoteButton(btnNoteA);
        resetNoteButton(btnNoteD);
        resetNoteButton(btnNoteG);
        resetNoteButton(btnNoteB);
        resetNoteButton(btnNoteE2);
        
        // Destacar o botão selecionado
        if ("E".equals(selectedNote)) {
            highlightNoteButton(btnNoteE);
            highlightNoteButton(btnNoteE2);
        } else if ("A".equals(selectedNote)) {
            highlightNoteButton(btnNoteA);
        } else if ("D".equals(selectedNote)) {
            highlightNoteButton(btnNoteD);
        } else if ("G".equals(selectedNote)) {
            highlightNoteButton(btnNoteG);
        } else if ("B".equals(selectedNote)) {
            highlightNoteButton(btnNoteB);
        }
    }
    
    @Override
    public void selectReferenceNote(String note) {
        updateReferenceNoteButtons(note);
    }
    
    @Override
    public void updateSensitivity(float sensitivity) {
        // Controles de sensibilidade não existem no layout atual
        // if (sensitivitySeekBar != null) {
        //     sensitivitySeekBar.setProgress((int) (sensitivity * 100));
        // }
        // 
        // if (sensitivityText != null) {
        //     sensitivityText.setText(String.format("Sensibilidade: %.0f%%", sensitivity * 100));
        // }
    }
    
    @Override
    public void updateDetectionThreshold(float threshold) {
        // Controles de threshold não existem no layout atual
        // if (thresholdSeekBar != null) {
        //     int progress = (int) ((threshold + 60.0f) / 40.0f * 100);
        //     thresholdSeekBar.setProgress(progress);
        // }
        // 
        // if (thresholdText != null) {
        //     thresholdText.setText(String.format("Threshold: %.0f dB", threshold));
        // }
    }
    
    @Override
    public void updateTuningIndicator(float position) {
        // tuningIndicator não existe no layout atual
        // if (tuningIndicator != null) {
        //     // Converter posição (-1.0 a 1.0) para progresso (0 a 100)
        //     int progress = (int) ((position + 1.0f) / 2.0f * 100);
        //     tuningIndicator.setProgress(progress);
        // }
    }
    
    @Override
    public void updateFrequencyHistory(float[] frequencies) {
        // Implementação futura para gráfico de histórico
    }
    
    @Override
    public void updateFrequencySpectrum(float[] spectrum) {
        // Implementação futura para espectro de frequências
    }
    
    @Override
    public void updateInputLevel(float level) {
        // inputLevelText não existe no layout atual
        // if (inputLevelText != null) {
        //     inputLevelText.setText(String.format("Entrada: %.1f dB", level));
        // }
    }
    
    @Override
    public void updateMicrophoneStatus(boolean isActive) {
        // Implementação futura para indicador de microfone
    }
    
    @Override
    public void requestAudioPermission() {
        // Implementação já feita no presenter
    }
    
    @Override
    public void showPermissionError() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Permissão Necessária")
            .setMessage("O afinador precisa de permissão para acessar o microfone")
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void showMicrophoneError() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Erro de Microfone")
            .setMessage("Não foi possível acessar o microfone")
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void showSettingsDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Configurações do Afinador")
            .setMessage("Configurações avançadas do afinador")
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void showHelpDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Ajuda do Afinador")
            .setMessage("Como usar o afinador:\n\n1. Toque em 'Iniciar'\n2. Toque uma corda\n3. Ajuste até ficar verde")
            .setPositiveButton("OK", null)
            .show();
    }
    
    // === Métodos privados ===
    
    private void resetNoteButton(Button button) {
        if (button != null) {
            button.setBackgroundTintList(getResources().getColorStateList(R.color.lava_button_bg));
            button.setTextColor(getResources().getColor(R.color.lava_text_primary));
        }
    }
    
    private void highlightNoteButton(Button button) {
        if (button != null) {
            button.setBackgroundTintList(getResources().getColorStateList(R.color.lava_green));
            button.setTextColor(getResources().getColor(R.color.white));
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (presenter != null) {
            presenter.handlePermissionResult(requestCode, permissions, grantResults);
        }
    }
} 