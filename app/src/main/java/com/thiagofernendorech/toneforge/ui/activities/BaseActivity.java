package com.thiagofernendorech.toneforge.ui.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.thiagofernendorech.toneforge.LogManager;
import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import com.thiagofernendorech.toneforge.infrastructure.adapters.PermissionManagerAdapter;
import java.util.List;

/**
 * Activity base que centraliza funcionalidades comuns
 * Reduz duplicação de código e melhora manutenibilidade
 */
public abstract class BaseActivity extends AppCompatActivity 
    implements PermissionInterface.PermissionCallback {
    
    private static final String TAG = "BaseActivity";
    private PermissionInterface permissionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initializeBaseComponents();
        configureFullscreenMode();
        hideActionBar();
    }
    
    /**
     * Inicializa componentes base da aplicação
     */
    private void initializeBaseComponents() {
        LogManager.getInstance(this);
        permissionManager = new PermissionManagerAdapter(this);
        LogManager.d(TAG, "Componentes base inicializados");
    }
    
    /**
     * Configura modo fullscreen
     */
    private void configureFullscreenMode() {
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        LogManager.d(TAG, "Modo fullscreen configurado");
    }
    
    /**
     * Esconde a action bar
     */
    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
    
    /**
     * Carrega um fragment na activity com animação padrão (fade)
     * @param fragment fragment a ser carregado
     */
    public void loadFragment(Fragment fragment) {
        loadFragment(fragment, TransitionType.FADE);
    }

    /**
     * Carrega um fragment na activity com tipo de animação específico
     * @param fragment fragment a ser carregado
     * @param transitionType tipo de transição a ser aplicada
     */
    public void loadFragment(Fragment fragment, TransitionType transitionType) {
        if (isDestroyed() || isFinishing()) {
            LogManager.w(TAG, "Tentativa de carregar fragment em activity destruída");
            return;
        }

        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Aplicar animação baseada no tipo de transição
            applyTransitionAnimation(transaction, transitionType);

            transaction.replace(getFragmentContainerId(), fragment);

            // Adicionar à pilha apenas se necessário
            if (shouldAddToBackStack(fragment)) {
                transaction.addToBackStack(null);
            }

            transaction.commit();
            LogManager.d(TAG, "Fragment carregado: " + fragment.getClass().getSimpleName());

        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao carregar fragment", e);
        }
    }

    /**
     * Aplica animação de transição à transação de fragment
     * @param transaction transação do fragment
     * @param transitionType tipo de transição
     */
    private void applyTransitionAnimation(FragmentTransaction transaction, TransitionType transitionType) {
        switch (transitionType) {
            case SLIDE_RIGHT:
                // Entrar da direita, sair pela esquerda (navegação para frente)
                transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                );
                break;
            case SLIDE_LEFT:
                // Entrar da esquerda, sair pela direita (navegação para trás)
                transaction.setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                );
                break;
            case FADE:
                // Fade in/out suave
                transaction.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.fade_out
                );
                break;
            case NONE:
            default:
                // Sem animação
                break;
        }
    }

    /**
     * Tipos de transição disponíveis para fragments
     */
    public enum TransitionType {
        /** Slide entrando pela direita (navegação para frente) */
        SLIDE_RIGHT,
        /** Slide entrando pela esquerda (navegação para trás) */
        SLIDE_LEFT,
        /** Fade in/out suave */
        FADE,
        /** Sem animação */
        NONE
    }
    
    /**
     * Verifica e solicita permissões necessárias
     */
    protected void checkAndRequestPermissions() {
        if (!permissionManager.hasAllRequiredPermissions()) {
            requestPermissions();
        } else {
            onPermissionsGranted();
        }
    }
    
    /**
     * Solicita permissões necessárias
     * Implementação específica deve ser fornecida pela subclasse
     */
    protected abstract void requestPermissions();
    
    /**
     * Obtém o ID do container de fragments
     * Implementação específica deve ser fornecida pela subclasse
     */
    protected abstract int getFragmentContainerId();
    
    /**
     * Determina se o fragment deve ser adicionado à pilha de navegação
     * Implementação padrão: adicionar todos exceto fragment inicial
     */
    protected boolean shouldAddToBackStack(Fragment fragment) {
        return true; // Subclasses podem sobrescrever
    }
    
    /**
     * Trata resultado de solicitação de permissões
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (allGranted) {
            onPermissionsGranted();
        } else {
            List<String> deniedPermissions = permissionManager.getDeniedPermissions();
            onPermissionsDenied(deniedPermissions);
        }
    }
    
    // Implementações padrão dos callbacks de permissão
    // Subclasses podem sobrescrever conforme necessário
    
    @Override
    public void onPermissionsGranted() {
        LogManager.i(TAG, "Todas as permissões concedidas");
    }
    
    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        LogManager.w(TAG, "Permissões negadas: " + deniedPermissions);
    }
    
    @Override
    public void onPermissionExplanationNeeded(List<String> permissions) {
        LogManager.i(TAG, "Explicação de permissões necessária: " + permissions);
    }
    
    /**
     * Obtém o gerenciador de permissões
     */
    protected PermissionInterface getPermissionManager() {
        return permissionManager;
    }
} 