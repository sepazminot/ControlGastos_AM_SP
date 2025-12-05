package com.example.controlgastos_am_sp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

// Asume que R existe y contiene los IDs de tu proyecto
// import com.example.controlgastos_am_sp.R;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder> {

    private final List<CategorySummary> summaries;
    private final double totalExpenses; // Gasto total del mes
    private final Context context;

    public CategorySummaryAdapter(Context context, List<CategorySummary> summaries, double totalExpenses) {
        this.context = context;
        this.summaries = summaries;
        this.totalExpenses = totalExpenses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout item_category_summary.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategorySummary summary = summaries.get(position);

        holder.tvCategoryName.setText(summary.getCategoryName());
        holder.tvCategoryAmount.setText(String.format(Locale.getDefault(), "$ %.2f", summary.getTotalAmount()));

        // Calcular Porcentaje
        int percentage = 0;
        if (totalExpenses > 0) {
            percentage = (int) ((summary.getTotalAmount() / totalExpenses) * 100);
        }

        holder.tvCategoryPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
        holder.progressBar.setProgress(percentage);
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCategoryName;
        final TextView tvCategoryPercentage;
        final TextView tvCategoryAmount;
        final ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            // Referenciar los IDs del item_category_summary.xml
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryPercentage = itemView.findViewById(R.id.tv_category_percentage);
            tvCategoryAmount = itemView.findViewById(R.id.tv_category_amount);
            progressBar = itemView.findViewById(R.id.category_progress_bar);
        }
    }
}
