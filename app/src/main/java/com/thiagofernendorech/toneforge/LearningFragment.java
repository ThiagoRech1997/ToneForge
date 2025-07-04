package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class LearningFragment extends Fragment {
    
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning, container, false);
        
        initializeViews(view);
        loadProgress();
        setupClickListeners();
        updateUI();
        
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
        
        // Inicializar SharedPreferences
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }
    
    private void setupClickListeners() {
        // Botões principais
        btnStartExercise.setOnClickListener(v -> showExerciseMode());
        btnTheory.setOnClickListener(v -> showTheoryMode());
        btnPractice.setOnClickListener(v -> showPracticeMode());
        btnChallenge.setOnClickListener(v -> showChallengeMode());
        
        // Exercícios
        btnNextExercise.setOnClickListener(v -> startNextExercise());
        btnHint.setOnClickListener(v -> showHint());
        
        // Teoria
        btnNextTheory.setOnClickListener(v -> nextTheoryPage());
        btnPreviousTheory.setOnClickListener(v -> previousTheoryPage());
        
        // Prática
        btnStartPractice.setOnClickListener(v -> startPractice());
        btnStopPractice.setOnClickListener(v -> stopPractice());
        tempoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateTempo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Desafios
        btnStartChallenge.setOnClickListener(v -> startChallenge());
        btnViewLeaderboard.setOnClickListener(v -> showLeaderboard());
    }
    
    private void showExerciseMode() {
        exerciseContainer.setVisibility(View.VISIBLE);
        theoryContainer.setVisibility(View.GONE);
        practiceContainer.setVisibility(View.GONE);
        
        if (!isExerciseActive) {
            startNextExercise();
        }
    }
    
    private void showTheoryMode() {
        exerciseContainer.setVisibility(View.GONE);
        theoryContainer.setVisibility(View.VISIBLE);
        practiceContainer.setVisibility(View.GONE);
        
        updateTheoryPage();
    }
    
    private void showPracticeMode() {
        exerciseContainer.setVisibility(View.GONE);
        theoryContainer.setVisibility(View.GONE);
        practiceContainer.setVisibility(View.VISIBLE);
        
        updatePracticeUI();
    }
    
    private void showChallengeMode() {
        // Implementar modo de desafio
        Toast.makeText(getContext(), "Modo Desafio em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
    
    private void startNextExercise() {
        if (completedExercises >= exercises.length) {
            // Todos os exercícios completados
            showExerciseComplete();
            return;
        }
        
        Exercise exercise = exercises[completedExercises];
        exerciseTitle.setText(exercise.title);
        exerciseDescription.setText(exercise.description);
        
        isExerciseActive = true;
        exerciseTimeLeft = 60;
        
        // Iniciar timer
        startExerciseTimer();
        
        // Configurar exercício específico
        setupSpecificExercise(exercise);
        
        updateExerciseUI();
    }
    
    private void setupSpecificExercise(Exercise exercise) {
        switch (exercise.type) {
            case "tuner":
                setupTunerExercise();
                break;
            case "intervals":
                setupIntervalsExercise();
                break;
            case "chords":
                setupChordsExercise();
                break;
            case "scales":
                setupScalesExercise();
                break;
            case "rhythm":
                setupRhythmExercise();
                break;
            case "effects":
                setupEffectsExercise();
                break;
            case "looper":
                setupLooperExercise();
                break;
            case "composition":
                setupCompositionExercise();
                break;
        }
    }
    
    private void setupTunerExercise() {
        // Exercício de afinamento
        exerciseDescription.setText("Toque a corda E6 (mi baixo) e afine até que o afinador mostre E");
        
        // Iniciar afinador
        AudioEngine.startTuner();
        
        // Verificar se está afinado
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (AudioEngine.isTunerActive()) {
                float freq = AudioEngine.getDetectedFrequency();
                if (Math.abs(freq - 82.41f) < 2.0f) { // E2 = 82.41 Hz
                    completeExercise();
                }
            }
        }, 5000); // Verificar após 5 segundos
    }
    
    private void setupIntervalsExercise() {
        // Exercício de intervalos
        String[] intervals = {"uníssono", "segunda", "terça", "quarta", "quinta", "sexta", "sétima", "oitava"};
        String randomInterval = intervals[new Random().nextInt(intervals.length)];
        
        exerciseDescription.setText("Identifique o intervalo: " + randomInterval);
        
        // Aqui você implementaria a lógica de detecção de intervalos
    }
    
    private void setupChordsExercise() {
        // Exercício de acordes
        String[] chords = {"Dó maior", "Ré maior", "Mi maior", "Fá maior", "Sol maior", "Lá maior", "Si maior"};
        String randomChord = chords[new Random().nextInt(chords.length)];
        
        exerciseDescription.setText("Toque o acorde: " + randomChord);
    }
    
    private void setupScalesExercise() {
        // Exercício de escalas
        exerciseDescription.setText("Toque a escala de Dó maior: Dó-Ré-Mi-Fá-Sol-Lá-Si-Dó");
    }
    
    private void setupRhythmExercise() {
        // Exercício de ritmo
        exerciseDescription.setText("Mantenha o tempo com o metrônomo em 120 BPM");
        
        // Iniciar metrônomo
        AudioEngine.startMetronome(120);
        AudioEngine.setMetronomeVolume(0.5f);
    }
    
    private void setupEffectsExercise() {
        // Exercício de efeitos
        exerciseDescription.setText("Aplique distorção com nível 50% e reverb com nível 30%");
        
        // Configurar efeitos
        AudioEngine.setDistortionEnabled(true);
        AudioEngine.setDistortionLevel(0.5f);
        AudioEngine.setReverbEnabled(true);
        AudioEngine.setReverbLevel(0.3f);
    }
    
    private void setupLooperExercise() {
        // Exercício de looper
        exerciseDescription.setText("Grave um loop de 4 batidas e reproduza");
        
        // Configurar looper
        AudioEngine.setLooperBPM(120);
        AudioEngine.setLooperSyncEnabled(true);
    }
    
    private void setupCompositionExercise() {
        // Exercício de composição
        exerciseDescription.setText("Crie uma progressão de acordes: I-IV-V-I");
    }
    
    private void startExerciseTimer() {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        
        exerciseTimer = new Timer();
        exerciseTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                exerciseTimeLeft--;
                
                if (exerciseTimeLeft <= 0) {
                    // Tempo esgotado
                    runOnUiThread(() -> {
                        exerciseTimeExpired();
                    });
                    exerciseTimer.cancel();
                } else {
                    runOnUiThread(() -> {
                        updateExerciseTimer();
                    });
                }
            }
        }, 1000, 1000);
    }
    
    private void updateExerciseTimer() {
        exerciseProgress.setText("Tempo: " + exerciseTimeLeft + "s");
        exerciseProgressBar.setProgress((60 - exerciseTimeLeft) * 100 / 60);
    }
    
    private void exerciseTimeExpired() {
        Toast.makeText(getContext(), "Tempo esgotado! Tente novamente.", Toast.LENGTH_SHORT).show();
        isExerciseActive = false;
        exerciseProgress.setText("Tempo esgotado");
    }
    
    private void completeExercise() {
        if (!isExerciseActive) return;
        
        Exercise exercise = exercises[completedExercises];
        currentScore += exercise.points;
        completedExercises++;
        
        // Verificar se subiu de nível
        if (completedExercises % 3 == 0) {
            currentLevel++;
            Toast.makeText(getContext(), "Parabéns! Você subiu para o nível " + currentLevel, Toast.LENGTH_LONG).show();
        }
        
        Toast.makeText(getContext(), "Exercício completo! +" + exercise.points + " pontos", Toast.LENGTH_SHORT).show();
        
        isExerciseActive = false;
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        
        // Parar efeitos específicos
        stopExerciseEffects();
        
        saveProgress();
        updateUI();
        
        // Próximo exercício após 2 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startNextExercise();
        }, 2000);
    }
    
    private void stopExerciseEffects() {
        AudioEngine.stopTuner();
        AudioEngine.stopMetronome();
        AudioEngine.setDistortionEnabled(false);
        AudioEngine.setReverbEnabled(false);
        AudioEngine.stopLooperPlayback();
        AudioEngine.stopLooperRecording();
    }
    
    private void showHint() {
        Exercise exercise = exercises[completedExercises];
        String hint = getHintForExercise(exercise);
        Toast.makeText(getContext(), "Dica: " + hint, Toast.LENGTH_LONG).show();
    }
    
    private String getHintForExercise(Exercise exercise) {
        switch (exercise.type) {
            case "tuner":
                return "Toque a corda E6 (a mais grossa) e ajuste até o ponteiro ficar no centro";
            case "intervals":
                return "Conte as notas entre as duas notas para identificar o intervalo";
            case "chords":
                return "Use os dedos indicador, médio e anelar para formar o acorde";
            case "scales":
                return "Toque as notas em sequência, uma por vez";
            case "rhythm":
                return "Bata o pé no tempo com o metrônomo";
            case "effects":
                return "Use os controles de distorção e reverb na tela de efeitos";
            case "looper":
                return "Pressione gravar, toque por 4 batidas, depois pressione parar";
            case "composition":
                return "Use os acordes Dó, Fá, Sol e Dó novamente";
            default:
                return "Leia as instruções cuidadosamente";
        }
    }
    
    private void showExerciseComplete() {
        exerciseTitle.setText("Parabéns!");
        exerciseDescription.setText("Você completou todos os exercícios básicos!");
        exerciseProgress.setText("Nível " + currentLevel + " - " + currentScore + " pontos");
        btnNextExercise.setText("Reiniciar");
        btnNextExercise.setOnClickListener(v -> {
            completedExercises = 0;
            startNextExercise();
        });
    }
    
    private void updateTheoryPage() {
        if (currentTheoryPage >= theoryPages.length) {
            currentTheoryPage = 0;
        }
        
        TheoryPage page = theoryPages[currentTheoryPage];
        theoryTitle.setText(page.title);
        theoryContent.setText(page.content);
        theoryProgress.setText((currentTheoryPage + 1) + " / " + theoryPages.length);
        
        // Salvar progresso
        prefs.edit().putInt(KEY_THEORY_PAGE, currentTheoryPage).apply();
    }
    
    private void nextTheoryPage() {
        currentTheoryPage++;
        if (currentTheoryPage >= theoryPages.length) {
            currentTheoryPage = 0;
        }
        updateTheoryPage();
    }
    
    private void previousTheoryPage() {
        currentTheoryPage--;
        if (currentTheoryPage < 0) {
            currentTheoryPage = theoryPages.length - 1;
        }
        updateTheoryPage();
    }
    
    private void startPractice() {
        isPracticeActive = true;
        btnStartPractice.setEnabled(false);
        btnStopPractice.setEnabled(true);
        
        // Iniciar metrônomo se ativado
        if (metronomeSwitch.isChecked()) {
            int tempo = tempoSeekBar.getProgress() + 60; // 60-180 BPM
            AudioEngine.startMetronome(tempo);
            AudioEngine.setMetronomeVolume(0.3f);
        }
        
        // Iniciar timer de prática
        startPracticeTimer();
    }
    
    private void stopPractice() {
        isPracticeActive = false;
        btnStartPractice.setEnabled(true);
        btnStopPractice.setEnabled(false);
        
        // Parar metrônomo
        AudioEngine.stopMetronome();
        
        // Parar timer
        stopPracticeTimer();
    }
    
    private void updateTempo(int progress) {
        int tempo = progress + 60; // 60-180 BPM
        tempoText.setText("Tempo: " + tempo + " BPM");
        
        if (isPracticeActive && metronomeSwitch.isChecked()) {
            AudioEngine.startMetronome(tempo);
        }
    }
    
    private void startPracticeTimer() {
        // Implementar timer de prática
    }
    
    private void stopPracticeTimer() {
        // Parar timer de prática
    }
    
    private void startChallenge() {
        // Implementar desafio
        Toast.makeText(getContext(), "Desafio iniciado!", Toast.LENGTH_SHORT).show();
    }
    
    private void showLeaderboard() {
        // Implementar leaderboard
        Toast.makeText(getContext(), "Leaderboard em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
    
    private void loadProgress() {
        currentLevel = prefs.getInt(KEY_LEVEL, 1);
        currentScore = prefs.getInt(KEY_SCORE, 0);
        completedExercises = prefs.getInt(KEY_EXERCISES_COMPLETED, 0);
        currentTheoryPage = prefs.getInt(KEY_THEORY_PAGE, 0);
    }
    
    private void saveProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LEVEL, currentLevel);
        editor.putInt(KEY_SCORE, currentScore);
        editor.putInt(KEY_EXERCISES_COMPLETED, completedExercises);
        editor.apply();
    }
    
    private void updateUI() {
        // Atualizar progresso geral
        int progressPercent = (completedExercises * 100) / exercises.length;
        overallProgress.setProgress(progressPercent);
        progressText.setText("Nível " + currentLevel + " - " + currentScore + " pontos");
        
        // Atualizar informações de exercício
        scoreText.setText("Pontuação: " + currentScore);
        levelText.setText("Nível: " + currentLevel);
        
        // Atualizar interface de exercício
        updateExerciseUI();
    }
    
    private void updateExerciseUI() {
        if (isExerciseActive) {
            exerciseProgress.setText("Tempo: " + exerciseTimeLeft + "s");
            exerciseProgressBar.setProgress((60 - exerciseTimeLeft) * 100 / 60);
        } else {
            exerciseProgress.setText("Pronto para começar");
            exerciseProgressBar.setProgress(0);
        }
    }
    
    private void updatePracticeUI() {
        tempoSeekBar.setProgress(60); // 120 BPM padrão
        tempoText.setText("Tempo: 120 BPM");
        metronomeSwitch.setChecked(true);
        practiceTimer.setText("00:00");
    }
    
    private void runOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(runnable);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Parar timers e efeitos
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        
        stopExerciseEffects();
        AudioEngine.stopMetronome();
        
        // Salvar progresso
        saveProgress();
    }
} 