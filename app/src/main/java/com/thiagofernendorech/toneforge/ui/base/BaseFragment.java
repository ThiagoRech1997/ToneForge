package com.thiagofernendorech.toneforge.ui.base;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.thiagofernendorech.toneforge.MainActivity;
import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.infrastructure.ui.DebounceClickListener;

/**
 * Fragment base que implementa funcionalidades comuns
 * Gerencia o ciclo de vida do presenter e fornece métodos utilitários
 * @param <P> tipo do presenter
 */
public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements BaseView {
    
    protected P presenter;
    private boolean isViewActive = false;
    private View loadingOverlay;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = createPresenter();
        if (presenter != null) {
            presenter.attachView(this);
        }
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewActive = true;
        onViewReady();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewActive = false;
        if (presenter != null) {
            presenter.onViewDestroyed();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }
    
    /**
     * Cria o presenter para este fragment
     * @return presenter criado
     */
    protected abstract P createPresenter();
    
    /**
     * Chamado quando a view está pronta
     * Subclasses podem sobrescrever para inicialização
     */
    protected void onViewReady() {
        // Implementação padrão vazia
    }
    
    /**
     * Atualiza o título do header
     * @param title título a ser exibido
     */
    protected void updateHeaderTitle(String title) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeaderTitle(title);
        }
    }
    
    /**
     * Carrega um fragment
     * @param fragment fragment a ser carregado
     */
    protected void loadFragment(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(fragment);
        }
    }
    
    // Implementações da interface BaseView
    
    @Override
    public void showLoading() {
        if (getView() == null || loadingOverlay != null) return;

        ViewGroup root = (ViewGroup) getView();

        // ScrollView only accepts one child, so add overlay to its child instead
        ViewGroup targetContainer = root;
        if (root instanceof ScrollView && root.getChildCount() > 0) {
            View child = root.getChildAt(0);
            if (child instanceof ViewGroup) {
                targetContainer = (ViewGroup) child;
            }
        }

        FrameLayout overlay = new FrameLayout(requireContext());
        overlay.setBackgroundColor(0x80000000);
        overlay.setClickable(true);

        ProgressBar progressBar = new ProgressBar(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        overlay.addView(progressBar, params);

        targetContainer.addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        loadingOverlay = overlay;
    }

    @Override
    public void hideLoading() {
        if (loadingOverlay != null && loadingOverlay.getParent() != null) {
            ((ViewGroup) loadingOverlay.getParent()).removeView(loadingOverlay);
            loadingOverlay = null;
        }
    }

    @Override
    public void showError(String message) {
        if (getView() != null) {
            Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.accent_red));
            snackbar.show();
        } else if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean isViewActive() {
        return isViewActive && !isDetached() && getActivity() != null;
    }

    /**
     * Cria um click listener com debounce para prevenir toques duplos
     */
    protected View.OnClickListener debounced(View.OnClickListener listener) {
        return new DebounceClickListener(listener);
    }
} 