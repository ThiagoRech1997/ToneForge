package com.thiagofernendorech.toneforge.ui.fragments.learning;

import android.content.Context;

public interface LearningContract {
    
    interface View {
        // UI Updates
        void updateOverallProgress(int progress, int total);
        void updateProgressText(String text);
        void updateScoreText(String text);
        void updateLevelText(String text);
        void updateExerciseProgress(int current, int total);
        void updateTheoryProgress(int current, int total);
        
        // Mode Switching
        void showExerciseMode();
        void showTheoryMode();
        void showPracticeMode();
        void showChallengeMode();
        void showMainMenu();
        
        // Exercise Management
        void setExerciseTitle(String title);
        void setExerciseDescription(String description);
        void setExerciseTimeLeft(int seconds);
        void showExerciseComplete();
        void showExerciseFailed();
        void showHint(String hint);
        
        // Theory Management
        void setTheoryTitle(String title);
        void setTheoryContent(String content);
        void enableTheoryNavigation(boolean canGoNext, boolean canGoPrevious);
        
        // Practice Management
        void setPracticeTitle(String title);
        void setPracticeDescription(String description);
        void setTempo(int bpm);
        void setMetronomeEnabled(boolean enabled);
        void setPracticeTimer(String time);
        void enablePracticeControls(boolean canStart, boolean canStop);
        
        // Challenge Management
        void setChallengeTitle(String title);
        void setChallengeDescription(String description);
        void setChallengeScore(String score);
        void showLeaderboard();
        
        // UI State
        void setExerciseActive(boolean active);
        void setPracticeActive(boolean active);
        void setChallengeActive(boolean active);
        
        // Navigation
        void enableMainButtons(boolean exercises, boolean theory, boolean practice, boolean challenge);
        
        // Feedback
        void showToast(String message);
        void showLoading(boolean show);
    }
    
    interface Presenter {
        // Lifecycle
        void attachView(View view);
        void detachView();
        
        // Main Menu Actions
        void onStartExerciseClicked();
        void onTheoryClicked();
        void onPracticeClicked();
        void onChallengeClicked();
        void onBackToMainClicked();
        
        // Exercise Management
        void onNextExerciseClicked();
        void onHintClicked();
        void onExerciseCompleted();
        void onExerciseFailed();
        void onExerciseTimeExpired();
        
        // Theory Management
        void onNextTheoryClicked();
        void onPreviousTheoryClicked();
        
        // Practice Management
        void onStartPracticeClicked();
        void onStopPracticeClicked();
        void onTempoChanged(int bpm);
        void onMetronomeToggled(boolean enabled);
        
        // Challenge Management
        void onStartChallengeClicked();
        void onViewLeaderboardClicked();
        
        // Data Management
        void loadProgress();
        void saveProgress();
        
        // Timer Management
        void startExerciseTimer();
        void stopExerciseTimer();
        void startPracticeTimer();
        void stopPracticeTimer();
        
        // UI Updates
        void updateUI();
        void updateExerciseUI();
        void updatePracticeUI();
    }
} 