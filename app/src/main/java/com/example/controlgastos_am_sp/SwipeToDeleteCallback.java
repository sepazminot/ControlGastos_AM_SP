package com.example.controlgastos_am_sp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final TransactionAdapter adapter;
    private final Manager manager;
    private final Drawable deleteIcon;
    private final ColorDrawable background;
    private final AppCompatActivity activity;

    // Asumimos que R.drawable.ic_delete_white_24dp existe y es blanco
    public SwipeToDeleteCallback(Context context, TransactionAdapter adapter, Manager manager) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT); // Permitir deslizar a izquierda y derecha
        this.adapter = adapter;
        this.manager = manager;
        this.activity = (AppCompatActivity) context;
        this.background = new ColorDrawable(Color.RED);
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // No soportamos arrastrar y soltar
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        final Transactions deletedTransaction = adapter.removeItem(position); // Remover de la lista visible

        // Ejecutar la eliminación en la base de datos en un hilo separado para no bloquear la UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                int rowsAffected = manager.Delete(deletedTransaction.id);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rowsAffected > 0) {
                            Toast.makeText(activity,
                                    "Transacción eliminada: " + deletedTransaction.description,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Si la eliminación falló en la DB, re-insertar el ítem en la lista visual
                            adapter.addItem(position, deletedTransaction);
                            Toast.makeText(activity,
                                    "Error al eliminar. Inténtelo de nuevo.",
                                    Toast.LENGTH_LONG).show();
                        }
                        // Nota: La notificación al adapter (removeItem) ya ocurrió,
                        // pero es bueno llamar a notifyDataSetChanged() si re-insertamos.
                        if (rowsAffected <= 0) {
                            adapter.notifyItemInserted(position);
                        }
                    }
                });
            }
        }).start();
    }

    // Lógica para dibujar el fondo rojo y el ícono de eliminar mientras se desliza
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20; // Para que no tape las esquinas redondeadas

        if (dX > 0) { // Deslizando hacia la derecha (opcional, mismo color)
            background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { // Deslizando hacia la izquierda (eliminación)
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            background.setBounds(0, 0, 0, 0); // Ocultar el fondo
        }
        background.draw(c);

        // Lógica para dibujar el ícono (simplificada)
        if (deleteIcon != null && dX != 0) {
            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = itemView.getBottom() - iconMargin;

            if (dX > 0) { // Icono a la izquierda
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            } else if (dX < 0) { // Icono a la derecha
                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            }
            deleteIcon.draw(c);
        }
    }
}
