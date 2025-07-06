package com.thiagofernendorech.toneforge.ui.fragments.recorder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thiagofernendorech.toneforge.R;

/**
 * Fragment refatorado para o gravador de áudio seguindo o padrão MVP
 * Responsável apenas pela interface do usuário
 */
public class RecorderFragmentRefactored extends Fragment implements RecorderContract.View {

    private RecorderContract.Presenter presenter;
    
    private Button recordButton;
    private Button playButton;
    private TextView timerText;
    private TextView listPlaceholder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorder, container, false);
        
        initializeViews(view);
        setupClickListeners();
        initializePresenter();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.thiagofernendorech.toneforge.MainActivity) {
            ((com.thiagofernendorech.toneforge.MainActivity) getActivity()).updateHeaderTitle("Gravador");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.cleanup();
        }
    }

    @Override
    public void updateRecordButtonState(boolean isRecording) {
        if (recordButton != null) {
            if (isRecording) {
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            } else {
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
            }
        }
    }

    @Override
    public void updatePlayButtonState(boolean isPlaying) {
        if (playButton != null) {
            if (isPlaying) {
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent_blue));
            } else {
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            }
        }
    }

    @Override
    public void updateTimer(String timeString) {
        if (timerText != null) {
            timerText.setText(timeString);
        }
    }

    @Override
    public void updateRecordingsList(String[] recordings) {
        if (listPlaceholder != null) {
            if (recordings != null && recordings.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < recordings.length; i++) {
                    sb.append("• ").append(recordings[i]);
                    if (i < recordings.length - 1) {
                        sb.append("\n");
                    }
                }
                listPlaceholder.setText(sb.toString());
            } else {
                showNoRecordingsPlaceholder();
            }
        }
    }

    @Override
    public void showNoRecordingsPlaceholder() {
        if (listPlaceholder != null) {
            listPlaceholder.setText("(Nenhuma gravação ainda)");
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean isViewActive() {
        return isAdded() && getActivity() != null && !isDetached();
    }

    @Override
    public void showLoading() {
        // Implementação básica - pode ser expandida com ProgressBar
        if (getContext() != null) {
            Toast.makeText(getContext(), "Carregando...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void hideLoading() {
        // Implementação básica - pode ser expandida com ProgressBar
    }

    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Inicializa as views do fragment
     */
    private void initializeViews(View view) {
        recordButton = view.findViewById(R.id.recorderRecordButton);
        playButton = view.findViewById(R.id.recorderPlayButton);
        timerText = view.findViewById(R.id.recorderTimer);
        listPlaceholder = view.findViewById(R.id.recorderListPlaceholder);
    }

    /**
     * Configura os listeners de clique dos botões
     */
    private void setupClickListeners() {
        if (recordButton != null) {
            recordButton.setOnClickListener(v -> {
                if (presenter != null) {
                    presenter.toggleRecording();
                }
            });
        }

        if (playButton != null) {
            playButton.setOnClickListener(v -> {
                if (presenter != null) {
                    presenter.togglePlayback();
                }
            });
        }
    }

    /**
     * Inicializa o presenter
     */
    private void initializePresenter() {
        presenter = new RecorderPresenter(this);
        presenter.initialize();
    }
} 