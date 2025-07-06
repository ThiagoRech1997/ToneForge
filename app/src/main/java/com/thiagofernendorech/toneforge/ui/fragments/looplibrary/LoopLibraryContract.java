package com.thiagofernendorech.toneforge.ui.fragments.looplibrary;

import com.thiagofernendorech.toneforge.ui.base.BaseView;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.LoopLibraryManager;
import java.util.List;

/**
 * Contrato MVP para o LoopLibraryFragment
 * Define as interfaces View e Presenter para gerenciar a biblioteca de loops
 */
public interface LoopLibraryContract {

    /**
     * Interface View para o LoopLibraryFragment
     * Define os métodos que a View deve implementar
     */
    interface View extends BaseView {
        
        /**
         * Atualiza a lista de loops na interface
         */
        void updateLoopsList(List<LoopLibraryManager.LoopInfo> loops);
        
        /**
         * Mostra o estado vazio quando não há loops
         */
        void showEmptyState();
        
        /**
         * Esconde o estado vazio quando há loops
         */
        void hideEmptyState();
        
        /**
         * Mostra diálogo de opções para um loop selecionado
         */
        void showLoopOptionsDialog(LoopLibraryManager.LoopInfo loop);
        
        /**
         * Mostra diálogo de confirmação para deletar loop
         */
        void showDeleteConfirmationDialog(LoopLibraryManager.LoopInfo loop);
        
        /**
         * Mostra diálogo para renomear loop
         */
        void showRenameDialog(LoopLibraryManager.LoopInfo loop);
        
        /**
         * Mostra mensagem de sucesso
         */
        void showSuccessMessage(String message);
        
        /**
         * Mostra mensagem de erro
         */
        void showErrorMessage(String message);
        
        /**
         * Navega de volta para o fragment anterior
         */
        void navigateBack();
        
        /**
         * Compartilha um loop
         */
        void shareLoop(LoopLibraryManager.LoopInfo loop);
    }

    /**
     * Interface Presenter para o LoopLibraryFragment
     * Define os métodos que o Presenter deve implementar
     */
    interface Presenter {
        
        /**
         * Carrega a biblioteca de loops
         */
        void loadLibrary();
        
        /**
         * Carrega um loop no looper
         */
        void loadLoop(LoopLibraryManager.LoopInfo loop);
        
        /**
         * Deleta um loop
         */
        void deleteLoop(LoopLibraryManager.LoopInfo loop);
        
        /**
         * Renomeia um loop
         */
        void renameLoop(LoopLibraryManager.LoopInfo loop, String newName);
        
        /**
         * Compartilha um loop
         */
        void shareLoop(LoopLibraryManager.LoopInfo loop);
        
        /**
         * Atualiza a biblioteca quando o fragment é retomado
         */
        void onResume();
    }
} 