package com.thiagofernendorech.toneforge.ui.widgets;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.thiagofernendorech.toneforge.R;

/**
 * HilavaAppButton - Botão de aplicativo no estilo iOS/HILAVA
 *
 * Características:
 * - Ícone com gradiente de cor
 * - Animações de toque (press/release)
 * - Sombra colorida
 * - Suporte a estados (normal, pressed, disabled)
 * - Responsivo para diferentes densidades de tela
 *
 * Uso em XML:
 * <com.thiagofernendorech.toneforge.ui.widgets.HilavaAppButton
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:hilava_color="@color/hilava_tuner"
 *     app:hilava_icon="@drawable/ic_target"
 *     app:hilava_label="Afinador" />
 */
public class HilavaAppButton extends View {

    // Constantes de design
    private static final float ICON_SIZE_RATIO = 0.45f;
    private static final int CORNER_RADIUS_DP = 18;
    private static final int SHADOW_RADIUS_DP = 6;
    private static final float PRESSED_SCALE = 0.94f;
    private static final int ANIMATION_DURATION = 100;
    private static final int DEFAULT_BUTTON_SIZE_DP = 68;
    private static final int LABEL_HEIGHT_DP = 28;
    private static final int LABEL_TEXT_SIZE_SP = 13;

    // Cores padrão dos apps (compatíveis com HILAVA)
    public static final int COLOR_TUNER = 0xFFFF9500;
    public static final int COLOR_TEMPO = 0xFF5856D6;
    public static final int COLOR_RECORDER = 0xFFFF3B30;
    public static final int COLOR_EFFECTS = 0xFF007AFF;
    public static final int COLOR_PRACTICE = 0xFF34C759;
    public static final int COLOR_LOOPS = 0xFFAF52DE;
    public static final int COLOR_SETTINGS = 0xFF8E8E93;
    public static final int COLOR_LIBRARY = 0xFF00C7BE;

    // Atributos
    private int primaryColor;
    private int secondaryColor;
    private String label;
    private Drawable iconDrawable;

    // Paint objects
    private Paint backgroundPaint;
    private Paint shadowPaint;
    private Paint labelPaint;

    // Dimensões
    private float density;
    private float buttonSize;
    private float cornerRadius;
    private float iconSize;
    private float labelHeight;

    // Rects
    private RectF buttonRect;
    private RectF iconRect;

    // Animações
    private AnimatorSet pressAnimator;
    private AnimatorSet releaseAnimator;

    public HilavaAppButton(Context context) {
        super(context);
        init(context, null);
    }

    public HilavaAppButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HilavaAppButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        density = context.getResources().getDisplayMetrics().density;

        // Valores padrão
        primaryColor = COLOR_EFFECTS;
        secondaryColor = adjustColorBrightness(primaryColor, -40);
        label = "";

        // Ler atributos XML se disponível
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HilavaAppButton);
            try {
                primaryColor = a.getColor(R.styleable.HilavaAppButton_hilava_color, COLOR_EFFECTS);
                secondaryColor = adjustColorBrightness(primaryColor, -40);
                label = a.getString(R.styleable.HilavaAppButton_hilava_label);
                if (label == null) label = "";

                int iconResId = a.getResourceId(R.styleable.HilavaAppButton_hilava_icon, 0);
                if (iconResId != 0) {
                    iconDrawable = ContextCompat.getDrawable(context, iconResId);
                    if (iconDrawable != null) {
                        // Usar mutate() para não afetar outras instâncias do drawable
                        iconDrawable = iconDrawable.mutate();
                        iconDrawable.setTint(Color.WHITE);
                        iconDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }
            } finally {
                a.recycle();
            }
        }

        // Inicializar paints
        initPaints();

        // Inicializar rects
        buttonRect = new RectF();
        iconRect = new RectF();

        // Habilitar clique
        setClickable(true);
        setFocusable(true);

        // Preparar animações
        setupAnimations();
    }

    private void initPaints() {
        // Paint do background
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Paint da sombra
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);

        // Paint do label
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(spToPx(LABEL_TEXT_SIZE_SP));
    }

    private void setupAnimations() {
        // Animação de press
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, PRESSED_SCALE);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, PRESSED_SCALE);
        pressAnimator = new AnimatorSet();
        pressAnimator.playTogether(scaleDownX, scaleDownY);
        pressAnimator.setDuration(ANIMATION_DURATION);
        pressAnimator.setInterpolator(new DecelerateInterpolator());

        // Animação de release
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", PRESSED_SCALE, 1.0f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", PRESSED_SCALE, 1.0f);
        releaseAnimator = new AnimatorSet();
        releaseAnimator.playTogether(scaleUpX, scaleUpY);
        releaseAnimator.setDuration(ANIMATION_DURATION);
        releaseAnimator.setInterpolator(new OvershootInterpolator(1.5f));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) dpToPx(DEFAULT_BUTTON_SIZE_DP);
        int desiredHeight = (int) dpToPx(DEFAULT_BUTTON_SIZE_DP + LABEL_HEIGHT_DP); // Botão + label

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calcular dimensões
        labelHeight = dpToPx(LABEL_HEIGHT_DP);
        buttonSize = Math.min(w, h - labelHeight);
        cornerRadius = dpToPx(CORNER_RADIUS_DP);
        iconSize = buttonSize * ICON_SIZE_RATIO;

        // Centralizar o botão horizontalmente
        float buttonLeft = (w - buttonSize) / 2f;
        float buttonTop = 0;
        buttonRect.set(buttonLeft, buttonTop, buttonLeft + buttonSize, buttonTop + buttonSize);

        // Área do ícone - centralizado no botão
        float iconLeft = buttonRect.centerX() - iconSize / 2f;
        float iconTop = buttonRect.centerY() - iconSize / 2f;
        iconRect.set(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);

        // Atualizar gradiente do background
        updateGradient();
        updateShadow();
    }

    private void updateGradient() {
        LinearGradient gradient = new LinearGradient(
                buttonRect.left, buttonRect.top,
                buttonRect.right, buttonRect.bottom,
                primaryColor, secondaryColor,
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(gradient);
    }

    private void updateShadow() {
        // Sombra simples sem blur para evitar tremido
        shadowPaint.setColor(primaryColor);
        shadowPaint.setAlpha(40);
        // Removido setShadowLayer para manter hardware acceleration e evitar ícones tremidos
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Desenhar sombra (retângulo deslocado para baixo)
        float shadowOffset = dpToPx(3);
        RectF shadowRect = new RectF(
                buttonRect.left + dpToPx(2),
                buttonRect.top + shadowOffset,
                buttonRect.right - dpToPx(2),
                buttonRect.bottom + shadowOffset
        );
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint);

        // Desenhar background com gradiente
        canvas.drawRoundRect(buttonRect, cornerRadius, cornerRadius, backgroundPaint);

        // Desenhar ícone
        if (iconDrawable != null) {
            iconDrawable.setBounds(
                    (int) iconRect.left, (int) iconRect.top,
                    (int) iconRect.right, (int) iconRect.bottom
            );
            iconDrawable.draw(canvas);
        }

        // Desenhar label
        if (label != null && !label.isEmpty()) {
            // Posicionar label abaixo do botão, centralizado
            float labelY = buttonRect.bottom + dpToPx(16);
            canvas.drawText(label, getWidth() / 2f, labelY, labelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (releaseAnimator.isRunning()) {
                    releaseAnimator.cancel();
                }
                pressAnimator.start();
                return true;

            case MotionEvent.ACTION_UP:
                if (pressAnimator.isRunning()) {
                    pressAnimator.cancel();
                }
                releaseAnimator.start();

                // Verificar se o toque está dentro do botão
                if (buttonRect.contains(event.getX(), event.getY())) {
                    performClick();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                if (pressAnimator.isRunning()) {
                    pressAnimator.cancel();
                }
                releaseAnimator.start();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // ==================== Métodos públicos ====================

    /**
     * Define a cor primária do botão
     * @param color Cor em formato ARGB
     */
    public void setPrimaryColor(int color) {
        this.primaryColor = color;
        this.secondaryColor = adjustColorBrightness(color, -40);
        if (buttonRect.width() > 0) {
            updateGradient();
            updateShadow();
        }
        invalidate();
    }

    /**
     * Define o label do botão
     * @param label Texto do label
     */
    public void setLabel(String label) {
        this.label = label != null ? label : "";
        invalidate();
    }

    /**
     * Define o ícone por resource ID
     * @param resId Resource ID do drawable
     */
    public void setIconResource(int resId) {
        this.iconDrawable = ContextCompat.getDrawable(getContext(), resId);
        if (iconDrawable != null) {
            iconDrawable = iconDrawable.mutate();
            iconDrawable.setTint(Color.WHITE);
            iconDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        invalidate();
    }

    /**
     * Define o ícone diretamente
     * @param drawable Drawable do ícone
     */
    public void setIconDrawable(Drawable drawable) {
        this.iconDrawable = drawable;
        if (iconDrawable != null) {
            iconDrawable = iconDrawable.mutate();
            iconDrawable.setTint(Color.WHITE);
            iconDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        invalidate();
    }

    /**
     * Retorna a cor primária atual
     */
    public int getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Retorna o label atual
     */
    public String getLabel() {
        return label;
    }

    // ==================== Utilitários ====================

    /**
     * Ajusta o brilho de uma cor
     * @param color Cor original
     * @param amount Quantidade para ajustar (-255 a 255)
     * @return Cor ajustada
     */
    private int adjustColorBrightness(int color, int amount) {
        int r = Math.max(0, Math.min(255, Color.red(color) + amount));
        int g = Math.max(0, Math.min(255, Color.green(color) + amount));
        int b = Math.max(0, Math.min(255, Color.blue(color) + amount));
        return Color.argb(Color.alpha(color), r, g, b);
    }

    private float dpToPx(float dp) {
        return dp * density;
    }

    private float spToPx(float sp) {
        return sp * getContext().getResources().getDisplayMetrics().scaledDensity;
    }
}
