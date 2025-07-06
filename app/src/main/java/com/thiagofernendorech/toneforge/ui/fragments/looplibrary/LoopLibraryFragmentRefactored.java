package com.thiagofernendorech.toneforge.ui.fragments.looplibrary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thiagofernendorech.toneforge.LoopLibraryAdapter;
import com.thiagofernendorech.toneforge.LoopLibraryManager;
import com.thiagofernendorech.toneforge.LoopShareUtil;
import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;

import java.util.List;

/**
 * Fragment refatorado para biblioteca de loops
 * Implementa a arquitetura MVP para melhor separação de responsabilidades
 */
public class LoopLibraryFragmentRefactored extends BaseFragment<LoopLibraryPresenter> 
        implements LoopLibraryContract.View, LoopLibraryAdapter.OnLoopActionListener {

    @Override
    protected LoopLibraryPresenter createPresenter() {
        return new LoopLibraryPresenter(requireContext());
    }

    private RecyclerView loopsRecyclerView;
    private View emptyStateText;
    private LoopLibraryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loop_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        
        // Carregar biblioteca
        presenter.loadLibrary();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.thiagofernendorech.toneforge.MainActivity) {
            ((com.thiagofernendorech.toneforge.MainActivity) getActivity()).updateHeaderTitle("Biblioteca de Loops");
        }
        if (presenter != null) {
            presenter.onResume();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initializeViews(View view) {
        loopsRecyclerView = view.findViewById(R.id.loopsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
    }

    /**
     * Configura o RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new LoopLibraryAdapter(this);
        loopsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loopsRecyclerView.setAdapter(adapter);
    }

    // ===== IMPLEMENTAÇÃO DA INTERFACE VIEW =====

    @Override
    public void updateLoopsList(List<LoopLibraryManager.LoopInfo> loops) {
        if (adapter != null) {
            adapter.updateLoops(loops);
        }
    }

    @Override
    public void showEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.VISIBLE);
        }
        if (loopsRecyclerView != null) {
            loopsRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }
        if (loopsRecyclerView != null) {
            loopsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showLoopOptionsDialog(LoopLibraryManager.LoopInfo loop) {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
            .setTitle(loop.displayName)
            .setItems(new String[]{"Carregar no Looper", "Compartilhar", "Cancelar"}, (dialog, which) -> {
                switch (which) {
                    case 0: // Carregar
                        if (presenter != null) presenter.loadLoop(loop);
                        break;
                    case 1: // Compartilhar
                        if (presenter != null) presenter.shareLoop(loop);
                        break;
                    case 2: // Cancelar
                        break;
                }
            })
            .show();
    }

    @Override
    public void showDeleteConfirmationDialog(LoopLibraryManager.LoopInfo loop) {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
            .setTitle("Excluir Loop")
            .setMessage("Tem certeza que deseja excluir '" + loop.displayName + "'?")
            .setPositiveButton("Excluir", (dialog, which) -> {
                if (presenter != null) presenter.deleteLoop(loop);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override
    public void showRenameDialog(LoopLibraryManager.LoopInfo loop) {
        if (getContext() == null) return;

        EditText input = new EditText(getContext());
        input.setText(loop.displayName);
        input.setSelection(loop.displayName.length());

        new AlertDialog.Builder(requireContext())
            .setTitle("Renomear Loop")
            .setView(input)
            .setPositiveButton("Renomear", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty() && presenter != null) {
                    presenter.renameLoop(loop, newName);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override
    public void showSuccessMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showErrorMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void navigateBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void shareLoop(LoopLibraryManager.LoopInfo loop) {
        LoopShareUtil.shareLoopFromLibrary(requireContext(), loop.fileName);
    }

    // ===== IMPLEMENTAÇÃO DO LOOP LIBRARY ADAPTER LISTENER =====

    @Override
    public void onLoopSelected(LoopLibraryManager.LoopInfo loop) {
        showLoopOptionsDialog(loop);
    }

    @Override
    public void onLoopDeleted(LoopLibraryManager.LoopInfo loop) {
        showDeleteConfirmationDialog(loop);
    }

    @Override
    public void onLoopRenamed(LoopLibraryManager.LoopInfo loop) {
        showRenameDialog(loop);
    }

    @Override
    public boolean isViewActive() {
        return isAdded() && getActivity() != null && !isDetached();
    }

    @Override
    public void showLoading() {
        // Implementação básica - pode ser expandida com ProgressBar
        if (getContext() != null) {
            Toast.makeText(getContext(), "Carregando...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void hideLoading() {
        // Implementação básica - pode ser expandida com ProgressBar
    }

    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
} 