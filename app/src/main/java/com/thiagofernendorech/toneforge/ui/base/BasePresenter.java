package com.thiagofernendorech.toneforge.ui.base;

import java.lang.ref.WeakReference;

/**
 * Classe base para todos os presenters da aplicação
 * Gerencia o ciclo de vida da view e evita memory leaks
 * @param <V> tipo da view que será controlada
 */
public abstract class BasePresenter<V extends BaseView> {
    
    private WeakReference<V> viewRef;
    
    /**
     * Anexa uma view ao presenter
     * @param view view a ser anexada
     */
    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
    }
    
    /**
     * Desanexa a view do presenter
     */
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }
    
    /**
     * Obtém a view atual
     * @return view atual ou null se não estiver anexada
     */
    protected V getView() {
        return viewRef == null ? null : viewRef.get();
    }
    
    /**
     * Verifica se a view está anexada e ativa
     * @return true se a view está anexada e ativa
     */
    protected boolean isViewAttached() {
        V view = getView();
        return view != null && view.isViewActive();
    }
    
    /**
     * Executa uma operação na view se ela estiver anexada
     * @param viewAction ação a ser executada
     */
    protected void ifViewAttached(ViewAction<V> viewAction) {
        V view = getView();
        if (view != null && view.isViewActive()) {
            viewAction.execute(view);
        }
    }
    
    /**
     * Chamado quando a view é destruída
     * Subclasses podem sobrescrever para limpeza
     */
    protected void onViewDestroyed() {
        // Implementação padrão vazia
    }
    
    /**
     * Interface funcional para ações na view
     */
    public interface ViewAction<V> {
        void execute(V view);
    }
} 