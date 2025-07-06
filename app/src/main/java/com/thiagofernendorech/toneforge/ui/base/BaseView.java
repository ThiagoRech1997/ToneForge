package com.thiagofernendorech.toneforge.ui.base;

/**
 * Interface base para todas as views da aplicação
 * Define métodos comuns para gerenciamento de estado da UI
 */
public interface BaseView {
    /**
     * Mostra indicador de carregamento
     */
    void showLoading();
    
    /**
     * Esconde indicador de carregamento
     */
    void hideLoading();
    
    /**
     * Mostra mensagem de erro
     * @param message mensagem a ser exibida
     */
    void showError(String message);
    
    /**
     * Mostra mensagem de sucesso
     * @param message mensagem a ser exibida
     */
    void showSuccess(String message);
    
    /**
     * Mostra toast com mensagem
     * @param message mensagem a ser exibida
     */
    void showMessage(String message);
    
    /**
     * Verifica se a view está ativa
     * @return true se a view está ativa
     */
    boolean isViewActive();
} 