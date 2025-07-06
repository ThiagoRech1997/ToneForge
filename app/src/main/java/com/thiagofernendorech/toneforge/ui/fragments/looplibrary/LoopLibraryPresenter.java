package com.thiagofernendorech.toneforge.ui.fragments.looplibrary;

import android.content.Context;
import android.util.Log;

import com.thiagofernendorech.toneforge.LoopLibraryManager;
import com.thiagofernendorech.toneforge.LoopLoadUtil;
import com.thiagofernendorech.toneforge.LoopShareUtil;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;

import java.util.List;

/**
 * Presenter para o LoopLibraryFragment
 * Gerencia a lógica de negócio da biblioteca de loops
 */
public class LoopLibraryPresenter extends BasePresenter<LoopLibraryContract.View> implements LoopLibraryContract.Presenter {

    private Context context;

    public LoopLibraryPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void loadLibrary() {
        if (!isViewAttached()) return;

        Log.d("LoopLibraryPresenter", "Iniciando carregamento da biblioteca...");
        
        LoopLibraryManager.loadLibrary(context, loops -> {
            Log.d("LoopLibraryPresenter", "Callback recebido com " + loops.size() + " loops");
            
            if (!isViewAttached()) return;
            
            LoopLibraryContract.View view = getView();
            if (loops.isEmpty()) {
                Log.d("LoopLibraryPresenter", "Nenhum loop encontrado - mostrando estado vazio");
                view.showEmptyState();
            } else {
                Log.d("LoopLibraryPresenter", "Loops encontrados: " + loops.size());
                view.hideEmptyState();
                view.updateLoopsList(loops);
            }
        });
    }

    @Override
    public void loadLoop(LoopLibraryManager.LoopInfo loop) {
        if (!isViewAttached()) return;

        LoopLoadUtil.loadLoopFromFile(context, loop.fileName, (success, fileName) -> {
            if (!isViewAttached()) return;
            
            LoopLibraryContract.View view = getView();
            if (success) {
                view.showSuccessMessage("Loop carregado: " + loop.displayName);
                view.navigateBack();
            } else {
                view.showErrorMessage("Erro ao carregar loop!");
            }
        });
    }

    @Override
    public void deleteLoop(LoopLibraryManager.LoopInfo loop) {
        if (!isViewAttached()) return;

        LoopLibraryManager.deleteLoop(context, loop.fileName, (success, fileName) -> {
            if (!isViewAttached()) return;
            
            LoopLibraryContract.View view = getView();
            if (success) {
                view.showSuccessMessage("Loop excluído: " + loop.displayName);
                loadLibrary(); // Recarregar lista
            } else {
                view.showErrorMessage("Erro ao excluir loop!");
            }
        });
    }

    @Override
    public void renameLoop(LoopLibraryManager.LoopInfo loop, String newName) {
        if (!isViewAttached()) return;

        LoopLibraryManager.renameLoop(context, loop.fileName, newName, (success, oldName, newFileName) -> {
            if (!isViewAttached()) return;
            
            LoopLibraryContract.View view = getView();
            if (success) {
                view.showSuccessMessage("Loop renomeado: " + newName);
                loadLibrary(); // Recarregar lista
            } else {
                view.showErrorMessage("Erro ao renomear loop!");
            }
        });
    }

    @Override
    public void shareLoop(LoopLibraryManager.LoopInfo loop) {
        if (!isViewAttached()) return;
        
        LoopLibraryContract.View view = getView();
        view.shareLoop(loop);
    }

    @Override
    public void onResume() {
        if (!isViewAttached()) return;
        
        // Recarregar biblioteca quando voltar para este fragment
        loadLibrary();
    }
} 