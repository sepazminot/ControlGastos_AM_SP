package com.example.controlgastos_am_sp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Activity_Setting extends AppCompatActivity {

    EditText txtname, txtpresupuesto, txtmoneda, txtfecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtname = findViewById(R.id.txtname2);
        txtpresupuesto = findViewById(R.id.txtpresupuesto2);
        txtmoneda = findViewById(R.id.txtmoneda2);
        txtfecha = findViewById(R.id.txtfecha2);

        Leer();
    }

    public void Leer() {
        String name, presupuesto, moneda, fecha;
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        name = preferences.getString("name", "");
        presupuesto = preferences.getString("presupuesto", "");
        moneda = preferences.getString("moneda", "");
        fecha = preferences.getString("fecha", "");
        txtname.setText(name);
        txtpresupuesto.setText(presupuesto);
        txtmoneda.setText(moneda);
        txtfecha.setText(fecha);
    }

    public void cmdActualizar_onclick(View v) {
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", txtname.getText().toString());
        editor.putString("presupuesto", txtpresupuesto.getText().toString());
        editor.putString("moneda", txtmoneda.getText().toString());
        editor.putString("fecha", txtfecha.getText().toString());
        editor.commit();
    }

    public void cmdRestablecer_onclick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Restablecimiento");
        builder.setMessage("¿Estás seguro de que quieres borrar todos los campos del formulario?");
        builder.setCancelable(true);
        builder.setPositiveButton("Sí, Restablecer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                txtname.setText("");
                txtpresupuesto.setText("");
                txtmoneda.setText("");
                txtfecha.setText("");
                Toast.makeText(Activity_Setting.this, "Campos restablecidos", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}