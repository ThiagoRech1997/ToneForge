package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LoopLibraryFragment extends Fragment implements LoopLibraryAdapter.OnLoopActionListener {
    
    private RecyclerView loopsRecyclerView;
    private View emptyStateText;
    private LoopLibraryAdapter adapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loop_library, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadLibrary();
        
        return view;
    }
    
    private void initializeViews(View view) {
        loopsRecyclerView = view.findViewById(R.id.loopsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
    }
    
    private void setupRecyclerView() {
        adapter = new LoopLibraryAdapter(this);
        loopsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loopsRecyclerView.setAdapter(adapter);
    }
    
    private void loadLibrary() {
        LoopLibraryManager.loadLibrary(getContext(), loops -> {
            if (loops.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                adapter.updateLoops(loops);
            }
        });
    }
    
    private void showEmptyState() {
        emptyStateText.setVisibility(View.VISIBLE);
        loopsRecyclerView.setVisibility(View.GONE);
    }
    
    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
        loopsRecyclerView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onLoopSelected(LoopLibraryManager.LoopInfo loop) {
        // Mostrar opções para o loop selecionado
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(loop.displayName)
            .setItems(new String[]{"Carregar no Looper", "Compartilhar", "Cancelar"}, (dialog, which) -> {
                switch (which) {
                    case 0: // Carregar
                        loadLoop(loop);
                        break;
                    case 1: // Compartilhar
                        shareLoop(loop);
                        break;
                    case 2: // Cancelar
                        break;
                }
            })
            .show();
    }
    
    private void loadLoop(LoopLibraryManager.LoopInfo loop) {
        LoopLoadUtil.loadLoopFromFile(getContext(), loop.fileName, (success, fileName) -> {
            if (success) {
                Toast.makeText(getContext(), "Loop carregado: " + loop.displayName, Toast.LENGTH_SHORT).show();
                
                // Navegar de volta para o looper
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            } else {
                Toast.makeText(getContext(), "Erro ao carregar loop!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void shareLoop(LoopLibraryManager.LoopInfo loop) {
        LoopShareUtil.shareLoopFromLibrary(getContext(), loop.fileName);
    }
    
    @Override
    public void onLoopDeleted(LoopLibraryManager.LoopInfo loop) {
        // Mostrar diálogo de confirmação
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Excluir Loop")
            .setMessage("Tem certeza que deseja excluir '" + loop.displayName + "'?")
            .setPositiveButton("Excluir", (dialog, which) -> {
                deleteLoop(loop);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    @Override
    public void onLoopRenamed(LoopLibraryManager.LoopInfo loop) {
        // Mostrar diálogo para renomear
        EditText input = new EditText(getContext());
        input.setText(loop.displayName);
        input.setSelection(loop.displayName.length());
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Renomear Loop")
            .setView(input)
            .setPositiveButton("Renomear", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    renameLoop(loop, newName);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void deleteLoop(LoopLibraryManager.LoopInfo loop) {
        LoopLibraryManager.deleteLoop(getContext(), loop.fileName, (success, fileName) -> {
            if (success) {
                Toast.makeText(getContext(), "Loop excluído: " + loop.displayName, Toast.LENGTH_SHORT).show();
                loadLibrary(); // Recarregar lista
            } else {
                Toast.makeText(getContext(), "Erro ao excluir loop!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void renameLoop(LoopLibraryManager.LoopInfo loop, String newName) {
        LoopLibraryManager.renameLoop(getContext(), loop.fileName, newName, (success, oldName, newFileName) -> {
            if (success) {
                Toast.makeText(getContext(), "Loop renomeado: " + newName, Toast.LENGTH_SHORT).show();
                loadLibrary(); // Recarregar lista
            } else {
                Toast.makeText(getContext(), "Erro ao renomear loop!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Recarregar biblioteca quando voltar para este fragment
        loadLibrary();
    }
} 