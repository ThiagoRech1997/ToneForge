package com.thiagofernendorech.toneforge.ui.fragments.learning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.thiagofernendorech.toneforge.R;

public class LearningFragmentRefactored extends Fragment implements LearningContract.View {
    
    private LearningContract.Presenter presenter;
    
    // UI Elements
    private TextView learningTitle;
    private TextView learningDescription;
    private ProgressBar overallProgress;
    private TextView progressText;
    private Button btnStartExercise;
    private Button btnTheory;
    private Button btnPractice;
    private Button btnChallenge;
    private LinearLayout exerciseContainer;
    private LinearLayout theoryContainer;
    private LinearLayout practiceContainer;
    private LinearLayout challengeContainer;
    // Exercícios
    private TextView exerciseTitle;
    private TextView exerciseDescription;
    private TextView exerciseProgress;
    private ProgressBar exerciseProgressBar;
    private Button btnNextExercise;
    private Button btnHint;
    private TextView scoreText;
    private TextView levelText;
    
    // Teoria Musical
    private TextView theoryTitle;
    private TextView theoryContent;
    private Button btnNextTheory;
    private Button btnPreviousTheory;
    private TextView theoryProgress;
    
    // Prática
    private TextView practiceTitle;
    private TextView practiceDescription;
    private SeekBar tempoSeekBar;
    private TextView tempoText;
    private Switch metronomeSwitch;
    private Button btnStartPractice;
    private Button btnStopPractice;
    private TextView practiceTimer;
    
    // Desafios
    private TextView challengeTitle;
    private TextView challengeDescription;
    private Button btnStartChallenge;
    private Button btnViewLeaderboard;
    private TextView challengeScore;
    
    // Botão voltar


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        presenter = new LearningPresenter(requireContext());
        presenter.attachView(this);
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Views principais
        learningTitle = view.findViewById(R.id.learningTitle);
        learningDescription = view.findViewById(R.id.learningDescription);
        overallProgress = view.findViewById(R.id.overallProgress);
        progressText = view.findViewById(R.id.progressText);
        
        // Botões principais
        btnStartExercise = view.findViewById(R.id.btnStartExercise);
        btnTheory = view.findViewById(R.id.btnTheory);
        btnPractice = view.findViewById(R.id.btnPractice);
        btnChallenge = view.findViewById(R.id.btnChallenge);
        
        // Containers
        exerciseContainer = view.findViewById(R.id.exerciseContainer);
        theoryContainer = view.findViewById(R.id.theoryContainer);
        practiceContainer = view.findViewById(R.id.practiceContainer);
        challengeContainer = view.findViewById(R.id.challengeContainer);
        
        // Exercícios
        exerciseTitle = view.findViewById(R.id.exerciseTitle);
        exerciseDescription = view.findViewById(R.id.exerciseDescription);
        exerciseProgress = view.findViewById(R.id.exerciseProgress);
        exerciseProgressBar = view.findViewById(R.id.exerciseProgressBar);
        btnNextExercise = view.findViewById(R.id.btnNextExercise);
        btnHint = view.findViewById(R.id.btnHint);
        scoreText = view.findViewById(R.id.scoreText);
        levelText = view.findViewById(R.id.levelText);
        
        // Teoria
        theoryTitle = view.findViewById(R.id.theoryTitle);
        theoryContent = view.findViewById(R.id.theoryContent);
        btnNextTheory = view.findViewById(R.id.btnNextTheory);
        btnPreviousTheory = view.findViewById(R.id.btnPreviousTheory);
        theoryProgress = view.findViewById(R.id.theoryProgress);
        
        // Prática
        practiceTitle = view.findViewById(R.id.practiceTitle);
        practiceDescription = view.findViewById(R.id.practiceDescription);
        tempoSeekBar = view.findViewById(R.id.tempoSeekBar);
        tempoText = view.findViewById(R.id.tempoText);
        metronomeSwitch = view.findViewById(R.id.metronomeSwitch);
        btnStartPractice = view.findViewById(R.id.btnStartPractice);
        btnStopPractice = view.findViewById(R.id.btnStopPractice);
        practiceTimer = view.findViewById(R.id.practiceTimer);
        
        // Desafios
        challengeTitle = view.findViewById(R.id.challengeTitle);
        challengeDescription = view.findViewById(R.id.challengeDescription);
        btnStartChallenge = view.findViewById(R.id.btnStartChallenge);
        btnViewLeaderboard = view.findViewById(R.id.btnViewLeaderboard);
        challengeScore = view.findViewById(R.id.challengeScore);
    }
    
    private void setupClickListeners() {
        // Botões principais
        btnStartExercise.setOnClickListener(v -> presenter.onStartExerciseClicked());
        btnTheory.setOnClickListener(v -> presenter.onTheoryClicked());
        btnPractice.setOnClickListener(v -> presenter.onPracticeClicked());
        btnChallenge.setOnClickListener(v -> presenter.onChallengeClicked());
        
        // Exercícios
        btnNextExercise.setOnClickListener(v -> presenter.onNextExerciseClicked());
        btnHint.setOnClickListener(v -> presenter.onHintClicked());
        
        // Teoria
        btnNextTheory.setOnClickListener(v -> presenter.onNextTheoryClicked());
        btnPreviousTheory.setOnClickListener(v -> presenter.onPreviousTheoryClicked());
        
        // Prática
        btnStartPractice.setOnClickListener(v -> presenter.onStartPracticeClicked());
        btnStopPractice.setOnClickListener(v -> presenter.onStopPracticeClicked());
        tempoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    presenter.onTempoChanged(progress + 60); // 60-180 BPM
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        metronomeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> 
            presenter.onMetronomeToggled(isChecked));
        
        // Desafios
        btnStartChallenge.setOnClickListener(v -> presenter.onStartChallengeClicked());
        btnViewLeaderboard.setOnClickListener(v -> presenter.onViewLeaderboardClicked());
    }
    
    // UI Updates
    @Override
    public void updateOverallProgress(int progress, int total) {
        overallProgress.setProgress(progress);
    }
    
    @Override
    public void updateProgressText(String text) {
        progressText.setText(text);
    }
    
    @Override
    public void updateScoreText(String text) {
        scoreText.setText(text);
    }
    
    @Override
    public void updateLevelText(String text) {
        levelText.setText(text);
    }
    
    @Override
    public void updateExerciseProgress(int current, int total) {
        exerciseProgress.setText(current + "/" + total);
        exerciseProgressBar.setProgress((current * 100) / total);
    }
    
    @Override
    public void updateTheoryProgress(int current, int total) {
        theoryProgress.setText(current + "/" + total);
    }
    
    // Mode Switching
    @Override
    public void showExerciseMode() {
        exerciseContainer.setVisibility(View.VISIBLE);
        theoryContainer.setVisibility(View.GONE);
        practiceContainer.setVisibility(View.GONE);
        challengeContainer.setVisibility(View.GONE);
    }
    
    @Override
    public void showTheoryMode() {
        exerciseContainer.setVisibility(View.GONE);
        theoryContainer.setVisibility(View.VISIBLE);
        practiceContainer.setVisibility(View.GONE);
        challengeContainer.setVisibility(View.GONE);
    }
    
    @Override
    public void showPracticeMode() {
        exerciseContainer.setVisibility(View.GONE);
        theoryContainer.setVisibility(View.GONE);
        practiceContainer.setVisibility(View.VISIBLE);
        challengeContainer.setVisibility(View.GONE);
    }
    
    @Override
    public void showChallengeMode() {
        exerciseContainer.setVisibility(View.GONE);
        theoryContainer.setVisibility(View.GONE);
        practiceContainer.setVisibility(View.GONE);
        challengeContainer.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void showMainMenu() {
        exerciseContainer.setVisibility(View.GONE);
        theoryContainer.setVisibility(View.GONE);
        practiceContainer.setVisibility(View.GONE);
        challengeContainer.setVisibility(View.GONE);
    }
    
    // Exercise Management
    @Override
    public void setExerciseTitle(String title) {
        exerciseTitle.setText(title);
    }
    
    @Override
    public void setExerciseDescription(String description) {
        exerciseDescription.setText(description);
    }
    
    @Override
    public void setExerciseTimeLeft(int seconds) {
        // Timer não implementado no layout atual
        // exerciseTimer.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
    }
    
    @Override
    public void showExerciseComplete() {
        Toast.makeText(requireContext(), "Exercício completado!", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void showExerciseFailed() {
        Toast.makeText(requireContext(), "Exercício falhou!", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void showHint(String hint) {
        Toast.makeText(requireContext(), "Dica: " + hint, Toast.LENGTH_LONG).show();
    }
    
    // Theory Management
    @Override
    public void setTheoryTitle(String title) {
        theoryTitle.setText(title);
    }
    
    @Override
    public void setTheoryContent(String content) {
        theoryContent.setText(content);
    }
    
    @Override
    public void enableTheoryNavigation(boolean canGoNext, boolean canGoPrevious) {
        btnNextTheory.setEnabled(canGoNext);
        btnPreviousTheory.setEnabled(canGoPrevious);
    }
    
    // Practice Management
    @Override
    public void setPracticeTitle(String title) {
        practiceTitle.setText(title);
    }
    
    @Override
    public void setPracticeDescription(String description) {
        practiceDescription.setText(description);
    }
    
    @Override
    public void setTempo(int bpm) {
        tempoText.setText(bpm + " BPM");
        tempoSeekBar.setProgress(bpm - 60);
    }
    
    @Override
    public void setMetronomeEnabled(boolean enabled) {
        metronomeSwitch.setChecked(enabled);
    }
    
    @Override
    public void setPracticeTimer(String time) {
        practiceTimer.setText(time);
    }
    
    @Override
    public void enablePracticeControls(boolean canStart, boolean canStop) {
        btnStartPractice.setEnabled(canStart);
        btnStopPractice.setEnabled(canStop);
    }
    
    // Challenge Management
    @Override
    public void setChallengeTitle(String title) {
        challengeTitle.setText(title);
    }
    
    @Override
    public void setChallengeDescription(String description) {
        challengeDescription.setText(description);
    }
    
    @Override
    public void setChallengeScore(String score) {
        challengeScore.setText(score);
    }
    
    @Override
    public void showLeaderboard() {
        Toast.makeText(requireContext(), "Tabela de classificação em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
    
    // UI State
    @Override
    public void setExerciseActive(boolean active) {
        btnNextExercise.setEnabled(active);
        btnHint.setEnabled(active);
    }
    
    @Override
    public void setPracticeActive(boolean active) {
        // Implementação específica se necessário
    }
    
    @Override
    public void setChallengeActive(boolean active) {
        btnStartChallenge.setEnabled(!active);
    }
    
    // Navigation
    @Override
    public void enableMainButtons(boolean exercises, boolean theory, boolean practice, boolean challenge) {
        btnStartExercise.setEnabled(exercises);
        btnTheory.setEnabled(theory);
        btnPractice.setEnabled(practice);
        btnChallenge.setEnabled(challenge);
    }
    
    // Feedback
    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void showLoading(boolean show) {
        // Implementação se necessário
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
} 