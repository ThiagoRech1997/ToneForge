package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;

public class WaveformView extends View {
    
    private Paint waveformPaint;
    private Paint gridPaint;
    private Paint cursorPaint;
    private Paint backgroundPaint;
    private Paint textPaint;
    
    private float[] waveformData;
    private int waveformLength;
    private float currentPosition = 0.0f; // 0.0 a 1.0
    private float playheadPosition = 0.0f; // 0.0 a 1.0
    
    // Seleção de trechos para edição
    private float selectionStart = 0.0f; // 0.0 a 1.0
    private float selectionEnd = 0.0f; // 0.0 a 1.0
    private boolean isSelecting = false;
    private boolean showSelection = false;
    
    private boolean isPlaying = false;
    private boolean showGrid = true;
    private boolean showPlayhead = true;
    private boolean useRMS = true; // Usar RMS ou forma de onda direta
    private boolean editMode = false; // Modo de edição ativo
    private boolean showTimeGrid = false; // Grade de tempo musical
    private int gridBPM = 120; // BPM para a grade de tempo
    
    // Zoom functionality
    private float zoomLevel = 1.0f; // 1.0 = normal, 2.0 = 2x zoom, etc.
    private float zoomCenter = 0.5f; // Center of zoom (0.0 to 1.0)
    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 10.0f;
    
    // Sistema de Marcadores
    private List<Float> markers = new ArrayList<>();
    private Paint markerPaint;
    
    private OnWaveformClickListener listener;
    private OnWaveformSelectionListener selectionListener;
    
    public interface OnWaveformClickListener {
        void onWaveformClick(float position);
    }
    
    public interface OnWaveformSelectionListener {
        void onSelectionChanged(float start, float end);
    }
    
    public WaveformView(Context context) {
        super(context);
        init();
    }
    
    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WaveformView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Paint para a forma de onda
        waveformPaint = new Paint();
        waveformPaint.setColor(Color.parseColor("#4CAF50")); // Verde
        waveformPaint.setStyle(Paint.Style.FILL);
        waveformPaint.setAntiAlias(true);
        
        // Paint para a grade
        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#666666"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAlpha(100);
        
        // Paint para o cursor/playhead
        cursorPaint = new Paint();
        cursorPaint.setColor(Color.parseColor("#FF5722")); // Laranja
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setStrokeWidth(3f);
        cursorPaint.setAntiAlias(true);
        
        // Paint para o fundo
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#1A1A1A")); // Cinza escuro
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        // Paint para texto
        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#FFFFFF"));
        textPaint.setTextSize(12f);
        textPaint.setAntiAlias(true);
        
        // Paint para marcadores
        markerPaint = new Paint();
        markerPaint.setColor(Color.parseColor("#FF9800")); // Laranja
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(2f);
        markerPaint.setAntiAlias(true);
        
        // Dados de exemplo para teste
        setWaveformData(new float[1000]);
    }
    
    public void setWaveformData(float[] data) {
        this.waveformData = data;
        this.waveformLength = data != null ? data.length : 0;
        invalidate();
    }
    
    public void setCurrentPosition(float position) {
        this.currentPosition = Math.max(0.0f, Math.min(1.0f, position));
        invalidate();
    }
    
    public void setPlayheadPosition(float position) {
        this.playheadPosition = Math.max(0.0f, Math.min(1.0f, position));
        invalidate();
    }
    
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        invalidate();
    }
    
    public void setShowGrid(boolean show) {
        this.showGrid = show;
        invalidate();
    }
    
    public void setShowPlayhead(boolean show) {
        this.showPlayhead = show;
        invalidate();
    }
    
    public void setUseRMS(boolean useRMS) {
        this.useRMS = useRMS;
        invalidate();
    }
    
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        invalidate();
    }
    
    public void setShowTimeGrid(boolean show) {
        this.showTimeGrid = show;
        invalidate();
    }
    
    public void setGridBPM(int bpm) {
        this.gridBPM = bpm;
        invalidate();
    }
    
    public void setSelection(float start, float end) {
        this.selectionStart = Math.max(0.0f, Math.min(1.0f, start));
        this.selectionEnd = Math.max(0.0f, Math.min(1.0f, end));
        this.showSelection = true;
        invalidate();
    }
    
    public void clearSelection() {
        this.showSelection = false;
        this.selectionStart = 0.0f;
        this.selectionEnd = 0.0f;
        invalidate();
    }
    
    public float getSelectionStart() {
        return selectionStart;
    }
    
    public float getSelectionEnd() {
        return selectionEnd;
    }
    
    public boolean hasSelection() {
        return showSelection;
    }
    
    // Zoom methods
    public void zoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel = Math.min(MAX_ZOOM, zoomLevel * 1.5f);
            invalidate();
        }
    }
    
    public void zoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel = Math.max(MIN_ZOOM, zoomLevel / 1.5f);
            invalidate();
        }
    }
    
    public void resetZoom() {
        zoomLevel = 1.0f;
        zoomCenter = 0.5f;
        invalidate();
    }
    
    public float getZoomLevel() {
        return zoomLevel;
    }
    
    public void setOnWaveformClickListener(OnWaveformClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnWaveformSelectionListener(OnWaveformSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public void setMarkers(List<Float> markers) {
        this.markers = markers != null ? new ArrayList<>(markers) : new ArrayList<>();
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) return;
        
        // Desenhar fundo
        canvas.drawRect(0, 0, width, height, backgroundPaint);
        
        // Desenhar grade
        if (showGrid) {
            drawGrid(canvas, width, height);
        }
        
        // Desenhar forma de onda
        if (waveformData != null && waveformLength > 0) {
            drawWaveform(canvas, width, height);
        }
        
        // Desenhar marcadores
        drawMarkers(canvas, width, height);
        
        // Desenhar seleção
        if (showSelection && editMode) {
            drawSelection(canvas, width, height);
        }
        
        // Desenhar playhead
        if (showPlayhead && isPlaying) {
            drawPlayhead(canvas, width, height);
        }
        
        // Desenhar informações
        drawInfo(canvas, width, height);
    }
    
    private void drawGrid(Canvas canvas, int width, int height) {
        if (showTimeGrid && gridBPM > 0 && waveformLength > 0) {
            // Grade de tempo musical baseada no BPM
            drawTimeGrid(canvas, width, height);
        } else {
            // Grade padrão
            drawStandardGrid(canvas, width, height);
        }
    }
    
    private void drawStandardGrid(Canvas canvas, int width, int height) {
        // Linhas verticais (tempo)
        int numVerticalLines = 10;
        for (int i = 0; i <= numVerticalLines; i++) {
            float x = (float) i / numVerticalLines * width;
            canvas.drawLine(x, 0, x, height, gridPaint);
        }
        
        // Linhas horizontais (amplitude)
        int numHorizontalLines = 4;
        for (int i = 0; i <= numHorizontalLines; i++) {
            float y = (float) i / numHorizontalLines * height;
            canvas.drawLine(0, y, width, y, gridPaint);
        }
    }
    
    private void drawTimeGrid(Canvas canvas, int width, int height) {
        // Calcular duração do loop em segundos
        float loopDurationSeconds = (float) waveformLength / 48000.0f; // Assumindo 48kHz
        
        // Calcular duração de uma batida em segundos
        float beatDurationSeconds = 60.0f / gridBPM;
        
        // Calcular quantas batidas cabem no loop
        int totalBeats = (int) (loopDurationSeconds / beatDurationSeconds);
        
        // Desenhar linhas verticais para cada batida
        Paint beatPaint = new Paint(gridPaint);
        beatPaint.setColor(Color.parseColor("#FF9800")); // Laranja para batidas
        beatPaint.setStrokeWidth(2f);
        
        Paint measurePaint = new Paint(gridPaint);
        measurePaint.setColor(Color.parseColor("#4CAF50")); // Verde para compassos
        measurePaint.setStrokeWidth(3f);
        
        for (int beat = 0; beat <= totalBeats; beat++) {
            float beatTime = beat * beatDurationSeconds;
            float beatPosition = beatTime / loopDurationSeconds;
            float x = beatPosition * width;
            
            if (x <= width) {
                // Linha da batida
                canvas.drawLine(x, 0, x, height, beatPaint);
                
                // Número da batida a cada 4 batidas (compasso 4/4)
                if (beat % 4 == 0) {
                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.parseColor("#FFFFFF"));
                    textPaint.setTextSize(10f);
                    textPaint.setAntiAlias(true);
                    
                    String measureText = String.valueOf(beat / 4 + 1);
                    canvas.drawText(measureText, x + 2, 12, textPaint);
                    
                    // Linha mais grossa para compassos
                    canvas.drawLine(x, 0, x, height, measurePaint);
                }
            }
        }
        
        // Linhas horizontais (amplitude)
        int numHorizontalLines = 4;
        for (int i = 0; i <= numHorizontalLines; i++) {
            float y = (float) i / numHorizontalLines * height;
            canvas.drawLine(0, y, width, y, gridPaint);
        }
    }
    
    private void drawWaveform(Canvas canvas, int width, int height) {
        if (waveformData == null || waveformLength == 0) return;
        
        float centerY = height / 2f;
        float scaleY = height / 2f * 0.8f; // 80% da altura
        
        // Apply zoom transformation
        canvas.save();
        if (zoomLevel > 1.0f) {
            // Calculate zoom parameters
            float zoomWidth = width / zoomLevel;
            float zoomOffset = (zoomCenter - 0.5f) * (width - zoomWidth);
            
            // Apply zoom transformation
            canvas.translate(zoomOffset, 0);
            canvas.scale(zoomLevel, 1.0f);
            
            // Adjust width for zoomed drawing
            width = (int) zoomWidth;
        }
        
        Path waveformPath = new Path();
        waveformPath.moveTo(0, centerY);
        
        if (useRMS) {
            // Usar RMS (Root Mean Square) para melhor visualização
            float[] rmsData = getRMSData(width);
            
            for (int i = 0; i < width; i++) {
                float amplitude = rmsData[i];
                float x = i;
                float y = centerY - amplitude * scaleY;
                
                if (i == 0) {
                    waveformPath.moveTo(x, y);
                } else {
                    waveformPath.lineTo(x, y);
                }
            }
        } else {
            // Usar forma de onda direta (amostras individuais)
            int samplesPerPixel = Math.max(1, waveformLength / width);
            
            for (int i = 0; i < width; i++) {
                int dataIndex = i * samplesPerPixel;
                if (dataIndex >= waveformLength) break;
                
                float amplitude = waveformData[dataIndex];
                float x = i;
                float y = centerY - amplitude * scaleY;
                
                if (i == 0) {
                    waveformPath.moveTo(x, y);
                } else {
                    waveformPath.lineTo(x, y);
                }
            }
        }
        
        // Desenhar a forma de onda
        canvas.drawPath(waveformPath, waveformPaint);
        
        // Restore canvas transformation
        canvas.restore();
        
        // Desenhar linha central (after restore to avoid zoom)
        Paint centerLinePaint = new Paint();
        centerLinePaint.setColor(Color.parseColor("#444444"));
        centerLinePaint.setStyle(Paint.Style.STROKE);
        centerLinePaint.setStrokeWidth(1f);
        canvas.drawLine(0, centerY, getWidth(), centerY, centerLinePaint);
    }
    
    private void drawPlayhead(Canvas canvas, int width, int height) {
        float x = playheadPosition * width;
        
        // Linha vertical do playhead
        canvas.drawLine(x, 0, x, height, cursorPaint);
        
        // Triângulo no topo
        Path triangle = new Path();
        triangle.moveTo(x - 8, 0);
        triangle.lineTo(x + 8, 0);
        triangle.lineTo(x, 16);
        triangle.close();
        
        Paint trianglePaint = new Paint();
        trianglePaint.setColor(Color.parseColor("#FF5722"));
        trianglePaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(triangle, trianglePaint);
    }
    
    private void drawInfo(Canvas canvas, int width, int height) {
        String info = String.format("Samples: %d", waveformLength);
        canvas.drawText(info, 10, height - 10, textPaint);
        
        if (isPlaying) {
            String position = String.format("Pos: %.1f%%", playheadPosition * 100);
            canvas.drawText(position, width - 80, height - 10, textPaint);
        }
        
        // Show zoom level if zoomed
        if (zoomLevel > 1.0f) {
            String zoomInfo = String.format("Zoom: %.1fx", zoomLevel);
            canvas.drawText(zoomInfo, width / 2 - 30, height - 10, textPaint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float position = x / getWidth();
        position = Math.max(0.0f, Math.min(1.0f, position));
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (editMode) {
                    // Iniciar seleção
                    isSelecting = true;
                    selectionStart = position;
                    selectionEnd = position;
                    showSelection = true;
                    invalidate();
                } else if (listener != null) {
                    listener.onWaveformClick(position);
                }
                return true;
                
            case MotionEvent.ACTION_MOVE:
                if (editMode && isSelecting) {
                    // Atualizar seleção
                    selectionEnd = position;
                    invalidate();
                    
                    if (selectionListener != null) {
                        selectionListener.onSelectionChanged(selectionStart, selectionEnd);
                    }
                }
                return true;
                
            case MotionEvent.ACTION_UP:
                if (editMode && isSelecting) {
                    // Finalizar seleção
                    isSelecting = false;
                    if (Math.abs(selectionEnd - selectionStart) < 0.01f) {
                        // Seleção muito pequena, limpar
                        clearSelection();
                    }
                }
                return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    // Método para atualizar dados do looper
    public void updateFromLooper() {
        // Obter dados do mix do looper
        float[] mix = AudioEngine.getLooperMix();
        if (mix != null && mix.length > 0) {
            setWaveformData(mix);
        }
    }
    
    // Método para obter dados de amplitude RMS (Root Mean Square)
    public float[] getRMSData(int numPoints) {
        if (waveformData == null || waveformLength == 0) {
            return new float[numPoints];
        }
        
        float[] rmsData = new float[numPoints];
        int samplesPerPoint = Math.max(1, waveformLength / numPoints);
        
        // Normalizar os dados para melhor visualização
        float maxAmplitude = 0.0f;
        for (int i = 0; i < waveformLength; i++) {
            maxAmplitude = Math.max(maxAmplitude, Math.abs(waveformData[i]));
        }
        
        // Se não há amplitude, retornar array vazio
        if (maxAmplitude == 0.0f) {
            return rmsData;
        }
        
        for (int i = 0; i < numPoints; i++) {
            int startIndex = i * samplesPerPoint;
            int endIndex = Math.min(startIndex + samplesPerPoint, waveformLength);
            
            float sum = 0.0f;
            int count = 0;
            
            for (int j = startIndex; j < endIndex; j++) {
                sum += waveformData[j] * waveformData[j];
                count++;
            }
            
            if (count > 0) {
                float rms = (float) Math.sqrt(sum / count);
                // Normalizar pela amplitude máxima
                rmsData[i] = rms / maxAmplitude;
            }
        }
        
        return rmsData;
    }
    
    private void drawMarkers(Canvas canvas, int width, int height) {
        if (markers.isEmpty()) return;
        
        for (int i = 0; i < markers.size(); i++) {
            float markerPosition = markers.get(i);
            float x = markerPosition * width;
            
            // Linha vertical do marcador
            canvas.drawLine(x, 0, x, height, markerPaint);
            
            // Triângulo na parte inferior
            Path triangle = new Path();
            triangle.moveTo(x - 6, height);
            triangle.lineTo(x + 6, height);
            triangle.lineTo(x, height - 12);
            triangle.close();
            
            Paint trianglePaint = new Paint();
            trianglePaint.setColor(Color.parseColor("#FF9800"));
            trianglePaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(triangle, trianglePaint);
            
            // Número do marcador
            String markerNumber = String.valueOf(i + 1);
            Paint numberPaint = new Paint();
            numberPaint.setColor(Color.parseColor("#FFFFFF"));
            numberPaint.setTextSize(10f);
            numberPaint.setAntiAlias(true);
            
            float textWidth = numberPaint.measureText(markerNumber);
            canvas.drawText(markerNumber, x - textWidth/2, height - 16, numberPaint);
        }
    }
    
    private void drawSelection(Canvas canvas, int width, int height) {
        if (!showSelection) return;
        
        float startX = selectionStart * width;
        float endX = selectionEnd * width;
        
        // Garantir que startX <= endX
        if (startX > endX) {
            float temp = startX;
            startX = endX;
            endX = temp;
        }
        
        // Desenhar área de seleção
        Paint selectionPaint = new Paint();
        selectionPaint.setColor(Color.parseColor("#4CAF50"));
        selectionPaint.setAlpha(50);
        selectionPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawRect(startX, 0, endX, height, selectionPaint);
        
        // Desenhar bordas da seleção
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#4CAF50"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        
        canvas.drawLine(startX, 0, startX, height, borderPaint);
        canvas.drawLine(endX, 0, endX, height, borderPaint);
    }
} 