package com.thiagofernendorech.toneforge;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ExportDialog extends DialogFragment {
    
    public interface ExportDialogListener {
        void onExportSelected(LoopExportManager.ExportFormat format, String fileName);
    }
    
    private ExportDialogListener listener;
    
    public static ExportDialog newInstance() {
        return new ExportDialog();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ExportDialogListener) {
            listener = (ExportDialogListener) context;
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        // Inflar layout customizado
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_export, null);
        
        // Configurar views
        RadioGroup formatGroup = view.findViewById(R.id.formatGroup);
        EditText fileNameInput = view.findViewById(R.id.fileNameInput);
        Button exportButton = view.findViewById(R.id.exportButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        
        // Configurar nome padrão
        String defaultName = "loop_" + System.currentTimeMillis();
        fileNameInput.setText(defaultName);
        fileNameInput.setSelection(defaultName.length());
        
        // Configurar botão de exportar
        exportButton.setOnClickListener(v -> {
            String fileName = fileNameInput.getText().toString().trim();
            if (fileName.isEmpty()) {
                fileNameInput.setError("Digite um nome para o arquivo");
                return;
            }
            
            // Obter formato selecionado
            int selectedId = formatGroup.getCheckedRadioButtonId();
            LoopExportManager.ExportFormat format = LoopExportManager.ExportFormat.WAV; // padrão
            
            if (selectedId == R.id.radioWav) {
                format = LoopExportManager.ExportFormat.WAV;
            } else if (selectedId == R.id.radioMp3) {
                format = LoopExportManager.ExportFormat.MP3;
            } else if (selectedId == R.id.radioFlac) {
                format = LoopExportManager.ExportFormat.FLAC;
            }
            
            // Chamar callback
            if (listener != null) {
                listener.onExportSelected(format, fileName);
            }
            
            dismiss();
        });
        
        // Configurar botão cancelar
        cancelButton.setOnClickListener(v -> dismiss());
        
        builder.setView(view);
        builder.setTitle("Exportar Loop");
        
        return builder.create();
    }
} 