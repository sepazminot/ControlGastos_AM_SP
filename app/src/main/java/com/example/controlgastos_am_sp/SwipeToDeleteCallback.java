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

    public SwipeToDeleteCallback(Context context, TransactionAdapter adapter, Manager manager) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.manager = manager;
        this.activity = (AppCompatActivity) context;
        this.background = new ColorDrawable(Color.RED);
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        final Transactions deletedTransaction = adapter.removeItem(position);

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
                            adapter.addItem(position, deletedTransaction);
                            Toast.makeText(activity,
                                    "Error al eliminar. Inténtelo de nuevo.",
                                    Toast.LENGTH_LONG).show();
                        }
                        if (rowsAffected <= 0) {
                            adapter.notifyItemInserted(position);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        if (dX > 0) {
            background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) {
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);

        if (deleteIcon != null && dX != 0) {
            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = itemView.getBottom() - iconMargin;

            if (dX > 0) {
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            } else if (dX < 0) {
                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            }
            deleteIcon.draw(c);
        }
    }
}
