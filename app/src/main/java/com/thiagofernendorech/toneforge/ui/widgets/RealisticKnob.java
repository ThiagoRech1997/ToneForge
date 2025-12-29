package com.thiagofernendorech.toneforge.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RealisticKnob extends View {
    private Paint knobPaint;
    private Paint indicatorPaint;
    private Paint shadowPaint;
    private Paint highlightPaint;
    
    private float progress = 0.5f; // 0.0 to 1.0
    private float knobRadius;
    private float centerX, centerY;
    
    private OnKnobChangeListener listener;
    private boolean isDragging = false;
    private float lastAngle = 0;
    
    // Configurações do knob
    private static final float START_ANGLE = 135f; // Graus
    private static final float SWEEP_ANGLE = 270f; // Graus
    private static final float INDICATOR_LENGTH = 0.7f; // Fração do raio
    
    public interface OnKnobChangeListener {
        void onKnobChanged(float progress, float value);
    }
    
    public RealisticKnob(Context context) {
        super(context);
        init();
    }
    
    public RealisticKnob(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Paint para o corpo do knob
        knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Paint para o indicador
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(Color.WHITE);
        indicatorPaint.setStrokeWidth(4f);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Paint para sombra
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(100);
        
        // Paint para highlight
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.WHITE);
        highlightPaint.setAlpha(80);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        centerX = w / 2f;
        centerY = h / 2f;
        knobRadius = Math.min(w, h) / 2f - 8; // Margem para sombra
        
        // Criar gradiente radial para efeito metálico
        RadialGradient gradient = new RadialGradient(
            centerX, centerY - knobRadius * 0.3f, knobRadius,
            new int[]{Color.parseColor("#606060"), 
                     Color.parseColor("#404040"), 
                     Color.parseColor("#202020")},
            new float[]{0f, 0.7f, 1f},
            Shader.TileMode.CLAMP
        );
        knobPaint.setShader(gradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Desenhar sombra
        canvas.drawCircle(centerX + 3, centerY + 3, knobRadius, shadowPaint);
        
        // Desenhar corpo do knob
        canvas.drawCircle(centerX, centerY, knobRadius, knobPaint);
        
        // Desenhar highlight superior
        float highlightRadius = knobRadius * 0.3f;
        canvas.drawCircle(centerX - knobRadius * 0.2f, 
                         centerY - knobRadius * 0.2f, 
                         highlightRadius, highlightPaint);
        
        // Desenhar indicador de posição
        drawIndicator(canvas);
        
        // Desenhar marcações (opcional)
        drawTicks(canvas);
    }
    
    private void drawIndicator(Canvas canvas) {
        // Calcular ângulo baseado no progresso
        float angle = START_ANGLE + (progress * SWEEP_ANGLE);
        double radians = Math.toRadians(angle);
        
        // Posição do indicador
        float startX = centerX + (float)(Math.cos(radians) * knobRadius * 0.3f);
        float startY = centerY + (float)(Math.sin(radians) * knobRadius * 0.3f);
        float endX = centerX + (float)(Math.cos(radians) * knobRadius * INDICATOR_LENGTH);
        float endY = centerY + (float)(Math.sin(radians) * knobRadius * INDICATOR_LENGTH);
        
        canvas.drawLine(startX, startY, endX, endY, indicatorPaint);
        
        // Ponto central
        Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.parseColor("#808080"));
        canvas.drawCircle(centerX, centerY, 3f, centerPaint);
    }
    
    private void drawTicks(Canvas canvas) {
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#404040"));
        tickPaint.setStrokeWidth(2f);
        
        // Desenhar 5 marcações
        for (int i = 0; i <= 4; i++) {
            float tickProgress = i / 4f;
            float angle = START_ANGLE + (tickProgress * SWEEP_ANGLE);
            double radians = Math.toRadians(angle);
            
            float startRadius = knobRadius + 4;
            float endRadius = knobRadius + 12;
            
            float startX = centerX + (float)(Math.cos(radians) * startRadius);
            float startY = centerY + (float)(Math.sin(radians) * startRadius);
            float endX = centerX + (float)(Math.cos(radians) * endRadius);
            float endY = centerY + (float)(Math.sin(radians) * endRadius);
            
            canvas.drawLine(startX, startY, endX, endY, tickPaint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = true;
                lastAngle = getAngleFromPoint(event.getX(), event.getY());
                return true;
                
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float currentAngle = getAngleFromPoint(event.getX(), event.getY());
                    float deltaAngle = currentAngle - lastAngle;
                    
                    // Normalizar diferença de ângulo
                    if (deltaAngle > 180) deltaAngle -= 360;
                    if (deltaAngle < -180) deltaAngle += 360;
                    
                    // Aplicar mudança no progresso
                    float deltaProgress = deltaAngle / SWEEP_ANGLE;
                    setProgress(progress + deltaProgress);
                    
                    lastAngle = currentAngle;
                }
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }
    
    private float getAngleFromPoint(float x, float y) {
        float deltaX = x - centerX;
        float deltaY = y - centerY;
        return (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
    }
    
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
        
        if (listener != null) {
            listener.onKnobChanged(this.progress, this.progress * 100f);
        }
    }
    
    public float getProgress() {
        return progress;
    }
    
    public void setOnKnobChangeListener(OnKnobChangeListener listener) {
        this.listener = listener;
    }
} 