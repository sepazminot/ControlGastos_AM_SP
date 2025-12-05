package com.example.controlgastos_am_sp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    EditText txtname, txtpresupuesto, txtmoneda, txtfecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtname = findViewById(R.id.txtname);
        txtpresupuesto = findViewById(R.id.txtpresupuesto);
        txtmoneda = findViewById(R.id.txtmoneda);
        txtfecha = findViewById(R.id.txtfecha);
    }

    public void cmdGuardar_onclick(View v) {
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", txtname.getText().toString());
        editor.putString("presupuesto", txtpresupuesto.getText().toString());
        editor.putString("moneda", txtmoneda.getText().toString());
        editor.putString("fecha", txtfecha.getText().toString());
        editor.commit();
    }

    //Pantallas
    public void cmdDashboard_onClick(View v) {
        intent = new Intent(this, Activity_Dashboard.class);
        startActivity(intent);
    }

    public void cmdLista_onClick(View v) {
        intent = new Intent(this, Activity_List.class);
        startActivity(intent);
    }

    public void cmdForm_onClick(View v) {
        intent = new Intent(this, Activity_Form.class);
        startActivity(intent);
    }

    public void cmdStadistic_onclick(View v) {
        intent = new Intent(this, Activity_Stadistic.class);
        startActivity(intent);
    }

    public void cmdSetting_onClick(View v) {
        intent = new Intent(this, Activity_Setting.class);
        startActivity(intent);
    }
}