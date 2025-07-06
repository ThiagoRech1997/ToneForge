package com.thiagofernendorech.toneforge.ui.fragments.learning;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class LearningPresenter implements LearningContract.Presenter {
    
    private LearningContract.View view;
    private Context context;
    private Handler mainHandler;
    
    // Estado
    private int currentLevel = 1;
    private int currentScore = 0;
    private int totalExercises = 0;
    private int completedExercises = 0;
    private int currentTheoryPage = 0;
    private boolean isExerciseActive = false;
    private boolean isPracticeActive = false;
    private boolean isChallengeActive = false;
    
    // Timer para exercícios
    private Timer exerciseTimer;
    private int exerciseTimeLeft = 60; // 60 segundos
    
    // Dados de progresso
    private SharedPreferences prefs;
    private static final String KEY_LEVEL = "learning_level";
    private static final String KEY_SCORE = "learning_score";
    private static final String KEY_EXERCISES_COMPLETED = "exercises_completed";
    private static final String KEY_THEORY_PAGE = "theory_page";
    
    // Exercícios disponíveis
    private Exercise[] exercises = {
        new Exercise("Afinamento Básico", "Afinar a corda E6 (mi baixo)", 10, "tuner"),
        new Exercise("Intervalos", "Identificar intervalos musicais", 15, "intervals"),
        new Exercise("Acordes Maiores", "Tocar acordes maiores básicos", 20, "chords"),
        new Exercise("Escalas", "Tocar escala de Dó maior", 25, "scales"),
        new Exercise("Ritmo", "Manter o tempo com metrônomo", 15, "rhythm"),
        new Exercise("Efeitos", "Aplicar efeitos corretamente", 20, "effects"),
        new Exercise("Looper", "Criar um loop básico", 30, "looper"),
        new Exercise("Composição", "Criar uma progressão de acordes", 40, "composition")
    };
    
    // Teoria musical
    private TheoryPage[] theoryPages = {
        new TheoryPage("Notas Musicais", "As notas musicais são: Dó, Ré, Mi, Fá, Sol, Lá, Si. Cada nota tem uma frequência específica e pode ser representada em diferentes oitavas."),
        new TheoryPage("Intervalos", "Intervalos são a distância entre duas notas. Os intervalos básicos são: uníssono, segunda, terça, quarta, quinta, sexta, sétima e oitava."),
        new TheoryPage("Acordes", "Acordes são grupos de três ou mais notas tocadas simultaneamente. Os acordes básicos são: maior, menor, diminuto e aumentado."),
        new TheoryPage("Escalas", "Escalas são sequências de notas em ordem ascendente ou descendente. A escala mais comum é a escala maior, que segue o padrão: tom-tom-semitom-tom-tom-tom-semitom."),
        new TheoryPage("Ritmo", "Ritmo é a organização temporal dos sons. Elementos básicos: pulso, tempo, compasso e acentuação."),
        new TheoryPage("Efeitos", "Efeitos de guitarra modificam o som original. Principais: distorção, reverb, delay, chorus, flanger, phaser, compressor e equalizador."),
        new TheoryPage("Looper", "Looper permite gravar e reproduzir loops em tempo real. Útil para prática solo e composição."),
        new TheoryPage("Composição", "Composição é a arte de criar música. Elementos: melodia, harmonia, ritmo e forma.")
    };
    
    private Exercise currentExercise;
    private int currentExerciseIndex = 0;
    private int practiceTempo = 120;
    private boolean metronomeEnabled = false;
    private Timer practiceTimer;
    private int practiceTimeElapsed = 0;
    
    public LearningPresenter(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.totalExercises = exercises.length;
    }
    
    @Override
    public void attachView(LearningContract.View view) {
        this.view = view;
        loadProgress();
        updateUI();
    }
    
    @Override
    public void detachView() {
        this.view = null;
        stopExerciseTimer();
        stopPracticeTimer();
    }
    
    // Main Menu Actions
    @Override
    public void onStartExerciseClicked() {
        if (view != null) {
            view.showExerciseMode();
            startNextExercise();
        }
    }
    
    @Override
    public void onTheoryClicked() {
        if (view != null) {
            view.showTheoryMode();
            updateTheoryPage();
        }
    }
    
    @Override
    public void onPracticeClicked() {
        if (view != null) {
            view.showPracticeMode();
            updatePracticeUI();
        }
    }
    
    @Override
    public void onChallengeClicked() {
        if (view != null) {
            view.showChallengeMode();
            updateChallengeUI();
        }
    }
    
    @Override
    public void onBackToMainClicked() {
        if (view != null) {
            view.showMainMenu();
            stopExerciseTimer();
            stopPracticeTimer();
            isExerciseActive = false;
            isPracticeActive = false;
            isChallengeActive = false;
            updateUI();
        }
    }
    
    // Exercise Management
    @Override
    public void onNextExerciseClicked() {
        if (currentExerciseIndex < exercises.length - 1) {
            currentExerciseIndex++;
            startNextExercise();
        } else {
            // Todos os exercícios completados
            if (view != null) {
                view.showToast("Parabéns! Você completou todos os exercícios!");
                view.showMainMenu();
            }
        }
    }
    
    @Override
    public void onHintClicked() {
        if (currentExercise != null && view != null) {
            String hint = getHintForExercise(currentExercise);
            view.showHint(hint);
        }
    }
    
    @Override
    public void onExerciseCompleted() {
        if (currentExercise != null) {
            currentScore += currentExercise.points;
            completedExercises++;
            
            // Verificar se subiu de nível
            if (completedExercises % 3 == 0) {
                currentLevel++;
            }
            
            saveProgress();
            
            if (view != null) {
                view.showExerciseComplete();
                view.updateScoreText("Pontuação: " + currentScore);
                view.updateLevelText("Nível: " + currentLevel);
            }
        }
    }
    
    @Override
    public void onExerciseFailed() {
        if (view != null) {
            view.showExerciseFailed();
        }
    }
    
    @Override
    public void onExerciseTimeExpired() {
        if (view != null) {
            view.showExerciseFailed();
            view.showToast("Tempo esgotado! Tente novamente.");
        }
    }
    
    // Theory Management
    @Override
    public void onNextTheoryClicked() {
        if (currentTheoryPage < theoryPages.length - 1) {
            currentTheoryPage++;
            updateTheoryPage();
        }
    }
    
    @Override
    public void onPreviousTheoryClicked() {
        if (currentTheoryPage > 0) {
            currentTheoryPage--;
            updateTheoryPage();
        }
    }
    
    // Practice Management
    @Override
    public void onStartPracticeClicked() {
        isPracticeActive = true;
        practiceTimeElapsed = 0;
        startPracticeTimer();
        
        if (view != null) {
            view.enablePracticeControls(false, true);
            view.setPracticeActive(true);
        }
    }
    
    @Override
    public void onStopPracticeClicked() {
        isPracticeActive = false;
        stopPracticeTimer();
        
        if (view != null) {
            view.enablePracticeControls(true, false);
            view.setPracticeActive(false);
            view.setPracticeTimer("00:00");
        }
    }
    
    @Override
    public void onTempoChanged(int bpm) {
        this.practiceTempo = bpm;
        if (view != null) {
            view.setTempo(bpm);
        }
    }
    
    @Override
    public void onMetronomeToggled(boolean enabled) {
        this.metronomeEnabled = enabled;
        if (view != null) {
            view.setMetronomeEnabled(enabled);
        }
    }
    
    // Challenge Management
    @Override
    public void onStartChallengeClicked() {
        isChallengeActive = true;
        if (view != null) {
            view.setChallengeActive(true);
            view.showToast("Desafio iniciado! Boa sorte!");
        }
    }
    
    @Override
    public void onViewLeaderboardClicked() {
        if (view != null) {
            view.showLeaderboard();
        }
    }
    
    // Data Management
    @Override
    public void loadProgress() {
        currentLevel = prefs.getInt(KEY_LEVEL, 1);
        currentScore = prefs.getInt(KEY_SCORE, 0);
        completedExercises = prefs.getInt(KEY_EXERCISES_COMPLETED, 0);
        currentTheoryPage = prefs.getInt(KEY_THEORY_PAGE, 0);
    }
    
    @Override
    public void saveProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LEVEL, currentLevel);
        editor.putInt(KEY_SCORE, currentScore);
        editor.putInt(KEY_EXERCISES_COMPLETED, completedExercises);
        editor.putInt(KEY_THEORY_PAGE, currentTheoryPage);
        editor.apply();
    }
    
    // Timer Management
    @Override
    public void startExerciseTimer() {
        stopExerciseTimer();
        exerciseTimeLeft = 60;
        exerciseTimer = new Timer();
        exerciseTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(() -> {
                    exerciseTimeLeft--;
                    if (view != null) {
                        view.setExerciseTimeLeft(exerciseTimeLeft);
                    }
                    
                    if (exerciseTimeLeft <= 0) {
                        stopExerciseTimer();
                        onExerciseTimeExpired();
                    }
                });
            }
        }, 0, 1000);
    }
    
    @Override
    public void stopExerciseTimer() {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }
    }
    
    @Override
    public void startPracticeTimer() {
        stopPracticeTimer();
        practiceTimer = new Timer();
        practiceTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(() -> {
                    practiceTimeElapsed++;
                    if (view != null) {
                        int minutes = practiceTimeElapsed / 60;
                        int seconds = practiceTimeElapsed % 60;
                        String time = String.format("%02d:%02d", minutes, seconds);
                        view.setPracticeTimer(time);
                    }
                });
            }
        }, 0, 1000);
    }
    
    @Override
    public void stopPracticeTimer() {
        if (practiceTimer != null) {
            practiceTimer.cancel();
            practiceTimer = null;
        }
    }
    
    // UI Updates
    @Override
    public void updateUI() {
        if (view != null) {
            int progress = (completedExercises * 100) / totalExercises;
            view.updateOverallProgress(progress, 100);
            view.updateProgressText(completedExercises + "/" + totalExercises + " exercícios");
            view.updateScoreText("Pontuação: " + currentScore);
            view.updateLevelText("Nível: " + currentLevel);
            view.enableMainButtons(true, true, true, true);
        }
    }
    
    @Override
    public void updateExerciseUI() {
        if (view != null && currentExercise != null) {
            view.setExerciseTitle(currentExercise.title);
            view.setExerciseDescription(currentExercise.description);
            view.updateExerciseProgress(currentExerciseIndex + 1, totalExercises);
            view.setExerciseActive(isExerciseActive);
        }
    }
    
    @Override
    public void updatePracticeUI() {
        if (view != null) {
            view.setPracticeTitle("Prática Livre");
            view.setPracticeDescription("Pratique suas habilidades musicais");
            view.setTempo(practiceTempo);
            view.setMetronomeEnabled(metronomeEnabled);
            view.enablePracticeControls(!isPracticeActive, isPracticeActive);
            view.setPracticeActive(isPracticeActive);
        }
    }
    
    private void updateTheoryPage() {
        if (view != null && currentTheoryPage < theoryPages.length) {
            TheoryPage page = theoryPages[currentTheoryPage];
            view.setTheoryTitle(page.title);
            view.setTheoryContent(page.content);
            view.updateTheoryProgress(currentTheoryPage + 1, theoryPages.length);
            view.enableTheoryNavigation(
                currentTheoryPage < theoryPages.length - 1,
                currentTheoryPage > 0
            );
        }
    }
    
    private void updateChallengeUI() {
        if (view != null) {
            view.setChallengeTitle("Desafio Musical");
            view.setChallengeDescription("Complete desafios para ganhar pontos extras");
            view.setChallengeScore("Pontuação: " + currentScore);
            view.setChallengeActive(isChallengeActive);
        }
    }
    
    private void startNextExercise() {
        if (currentExerciseIndex < exercises.length) {
            currentExercise = exercises[currentExerciseIndex];
            isExerciseActive = true;
            startExerciseTimer();
            updateExerciseUI();
            
            if (view != null) {
                view.showToast("Exercício iniciado: " + currentExercise.title);
            }
        }
    }
    
    private String getHintForExercise(Exercise exercise) {
        switch (exercise.type) {
            case "tuner":
                return "Use o afinador para encontrar a nota E6 (82.41 Hz)";
            case "intervals":
                return "Ouça atentamente a diferença entre as notas";
            case "chords":
                return "Posicione os dedos corretamente no braço da guitarra";
            case "scales":
                return "Toque as notas em sequência: Dó, Ré, Mi, Fá, Sol, Lá, Si, Dó";
            case "rhythm":
                return "Siga o metrônomo e mantenha o tempo constante";
            case "effects":
                return "Experimente diferentes configurações de efeitos";
            case "looper":
                return "Grave um riff simples e reproduza em loop";
            case "composition":
                return "Crie uma progressão de 4 acordes";
            default:
                return "Pratique e seja paciente!";
        }
    }
    
    // Classes auxiliares
    private static class Exercise {
        String title;
        String description;
        int points;
        String type;
        
        Exercise(String title, String description, int points, String type) {
            this.title = title;
            this.description = description;
            this.points = points;
            this.type = type;
        }
    }
    
    private static class TheoryPage {
        String title;
        String content;
        
        TheoryPage(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
} 