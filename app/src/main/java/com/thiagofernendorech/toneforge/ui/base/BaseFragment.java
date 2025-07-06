package com.thiagofernendorech.toneforge.ui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.thiagofernendorech.toneforge.MainActivity;

/**
 * Fragment base que implementa funcionalidades comuns
 * Gerencia o ciclo de vida do presenter e fornece métodos utilitários
 * @param <P> tipo do presenter
 */
public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements BaseView {
    
    protected P presenter;
    private boolean isViewActive = false;
    
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
        // Implementação padrão - pode ser sobrescrita
    }
    
    @Override
    public void hideLoading() {
        // Implementação padrão - pode ser sobrescrita
    }
    
    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Erro: " + message, Toast.LENGTH_LONG).show();
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
} 