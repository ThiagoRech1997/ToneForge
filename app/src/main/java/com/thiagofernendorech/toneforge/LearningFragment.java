package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
import android.widget.TextView;

public class LearningFragment extends Fragment {
    private int currentTip = 0;
    private final String[] tips = {
            "Dica: Use o afinador para garantir o melhor som!",
            "Dica: Experimente diferentes combinações de efeitos.",
            "Dica: Grave seus loops para praticar improvisação.",
            "Dica: Use o metrônomo para melhorar seu tempo."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning, container, false);

        TextView tipText = view.findViewById(R.id.learningTip);
        Button nextTipButton = view.findViewById(R.id.learningNextTipButton);

        tipText.setText(tips[currentTip]);

        nextTipButton.setOnClickListener(v -> {
            currentTip = (currentTip + 1) % tips.length;
            tipText.setText(tips[currentTip]);
        });

        // TODO: Adicionar conteúdo multimídia na área learningContentArea futuramente

        return view;
    }
} 