package com.thiagofernendorech.toneforge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.thiagofernendorech.toneforge.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ToneForge";
    
    // Permissões necessárias
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO
    };

    // Configurações de áudio
    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    // Componentes de áudio
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private boolean isRecording = false;
    private Thread audioThread;

    // Interface
    private ActivityMainBinding binding;
    private Handler mainHandler;

    // Used to load the 'toneforge' library on application startup.
    static {
        System.loadLibrary("toneforge");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainHandler = new Handler(Looper.getMainLooper());
        
        // Inicializar motor de áudio
        initAudioEngine();
        
        // Configurar interface
        setupUI();
        
        // Verificar permissões
        checkPermissions();
    }

    private void setupUI() {
        // Configurar título
        binding.titleText.setText(stringFromJNI());
        
        // Configurar botões de áudio
        binding.btnStartAudio.setOnClickListener(v -> startAudio());
        binding.btnStopAudio.setOnClickListener(v -> stopAudio());
        
        // Configurar SeekBars
        setupSeekBars();
    }

    private void setupSeekBars() {
        // Ganho
        binding.seekbarGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float gain = progress / 100.0f;
                setGain(gain);
                binding.textGainValue.setText(String.format(Locale.getDefault(), "Ganho: %.1fx", gain));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Distorção
        binding.seekbarDistortion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float distortion = progress / 100.0f;
                setDistortion(distortion);
                binding.textDistortionValue.setText(String.format(Locale.getDefault(), "Distorção: %d%%", progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Delay - Tempo
        binding.seekbarDelayTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float delayTime = progress / 1000.0f; // Converter para segundos
                setDelay(delayTime, 0.5f);
                binding.textDelayTimeValue.setText(String.format(Locale.getDefault(), "Tempo: %dms", progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Reverb - Tamanho da sala
        binding.seekbarReverbRoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float roomSize = progress / 100.0f;
                setReverb(roomSize, 0.5f);
                binding.textReverbRoomValue.setText(String.format(Locale.getDefault(), "Tamanho: %d%%", progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
            binding.btnStartAudio.setEnabled(false);
        } else {
            updateStatus("Pronto para iniciar");
            binding.btnStartAudio.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateStatus("Permissão concedida - Pronto para iniciar");
                binding.btnStartAudio.setEnabled(true);
            } else {
                updateStatus("Permissão de áudio necessária!");
                binding.btnStartAudio.setEnabled(false);
            }
        }
    }

    private void startAudio() {
        if (isRecording) {
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            updateStatus("Permissão de áudio não concedida!");
            return;
        }

        try {
            // Inicializar AudioRecord
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            );

            // Inicializar AudioTrack
            audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(new AudioFormat.Builder()
                    .setEncoding(AUDIO_FORMAT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(BUFFER_SIZE)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

            audioRecord.startRecording();
            audioTrack.play();
            isRecording = true;

            // Iniciar thread de processamento
            audioThread = new Thread(this::audioProcessingLoop);
            audioThread.start();

            updateStatus("🎵 Áudio ativo - Processando em tempo real");
            binding.btnStartAudio.setEnabled(false);
            binding.btnStopAudio.setEnabled(true);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar áudio: " + e.getMessage());
            updateStatus("❌ Erro ao iniciar áudio: " + e.getMessage());
        }
    }

    private void stopAudio() {
        if (!isRecording) {
            return;
        }

        isRecording = false;

        if (audioThread != null) {
            try {
                audioThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Erro ao parar thread: " + e.getMessage());
            }
        }

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }

        updateStatus("⏹️ Áudio parado");
        binding.btnStartAudio.setEnabled(true);
        binding.btnStopAudio.setEnabled(false);
    }

    private void audioProcessingLoop() {
        float[] inputBuffer = new float[BUFFER_SIZE / 4]; // 4 bytes por float
        float[] outputBuffer = new float[BUFFER_SIZE / 4];

        while (isRecording) {
            try {
                // Ler áudio de entrada
                int bytesRead = audioRecord.read(inputBuffer, 0, inputBuffer.length, AudioRecord.READ_BLOCKING);
                
                if (bytesRead > 0) {
                    // Processar buffer através do motor de áudio
                    processBuffer(inputBuffer, outputBuffer, bytesRead);
                    
                    // Reproduzir áudio processado
                    audioTrack.write(outputBuffer, 0, bytesRead, AudioTrack.WRITE_BLOCKING);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro no loop de áudio: " + e.getMessage());
                break;
            }
        }
    }

    private void updateStatus(String status) {
        mainHandler.post(() -> {
            binding.statusText.setText("Status: " + status);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
        cleanupAudioEngine();
    }

    // Métodos JNI
    public native String stringFromJNI();
    public native void setGain(float gain);
    public native void setDistortion(float amount);
    public native void setDelay(float time, float feedback);
    public native void setReverb(float roomSize, float damping);
    public native float processSample(float input);
    public native void processBuffer(float[] input, float[] output, int numSamples);
    public native void initAudioEngine();
    public native void cleanupAudioEngine();
}