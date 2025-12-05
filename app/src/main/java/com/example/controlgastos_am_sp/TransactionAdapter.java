package com.example.controlgastos_am_sp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlgastos_am_sp.Transactions;
import com.example.controlgastos_am_sp.R; // Asegúrate de tener este import

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transactions> transactions;
    private final OnTransactionClickListener clickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public TransactionAdapter(List<Transactions> transactions, OnTransactionClickListener clickListener) {
        this.transactions = transactions;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Necesitas un layout llamado R.layout.item_transaction para cada fila
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        final Transactions transaction = transactions.get(position);

        // Convertir el 'long' de la fecha a un String legible
        long timestampInMillis = transaction.date * 1000L;
        String dateString = dateFormat.format(new Date(timestampInMillis));

        holder.tvAmount.setText(String.format(Locale.getDefault(), "$ %.2f", transaction.amount));
        holder.tvDescription.setText(transaction.description != null ? transaction.description : "Sin descripción");
        holder.tvDate.setText(dateString);

        // Muestra el ID de categoría, en una app real buscarías el nombre
        holder.tvCategory.setText("Cat ID: " + transaction.category);

        // Estilo del Monto: Rojo para gastos, Verde para ingresos (asumiendo que los gastos son negativos)
        int colorResId = (transaction.amount < 0) ? R.color.expense_red : R.color.income_green;
        // NOTA: Usar ContextCompat.getColor() para compatibilidad
        holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(colorResId, null));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    // Método para manejar la eliminación después del swipe
    public Transactions removeItem(int position) {
        Transactions item = transactions.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void addItem(int position, Transactions item) {
        transactions.add(position, item);
        notifyItemInserted(position);
    }

    // Método para actualizar la lista de transacciones (usado en el filtrado y onResume)
    public void updateData(List<Transactions> newTransactions) {
        transactions.clear();
        transactions.addAll(newTransactions);
        notifyDataSetChanged();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAmount;
        final TextView tvCategory;
        final TextView tvDescription;
        final TextView tvDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);

            // Manejar el CLICK para la edición
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onTransactionClick(transactions.get(position));
                    }
                }
            });
        }
    }
}
