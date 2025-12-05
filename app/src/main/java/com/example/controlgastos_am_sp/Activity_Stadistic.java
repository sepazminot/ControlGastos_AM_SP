package com.example.controlgastos_am_sp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Activity_Stadistic extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Manager manager;
    private TextView tvDailyAverage;
    private RecyclerView rvCategoryList;
    // TextView tvChartPlaceholder; // Si decides usar un placeholder simple para el gráfico

    // Mes y Año de análisis (1-based month)
    private int currentMonth = -1;
    private int currentYear = -1;

    // NOTA: Para el gráfico (Sección 1), necesitarás integrar una librería
    // como MPAndroidChart o AChartEngine en tu proyecto y luego inicializarla aquí.

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stadistic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDailyAverage = findViewById(R.id.tv_daily_average);
        rvCategoryList = findViewById(R.id.category_list_container);

        // Configurar RecyclerView
        rvCategoryList.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar Manager y SharedPreferences
        manager = new Manager(this, "transaccion.db", 1);

        sharedPreferences = getSharedPreferences("datos", Context.MODE_PRIVATE);

        // Cargar y mostrar datos
        loadStatisticsData();
    }

    private void loadStatisticsData() {

        // 1. OBTENER MES Y AÑO DE FILTRO (Reutilizando la lógica de Dashboard)
        String dateString = sharedPreferences.getString("fecha", "");

        Calendar cal = Calendar.getInstance();

        int targetMonth0Based = cal.get(Calendar.MONTH); // 0-based
        int targetYear = cal.get(Calendar.YEAR);

        if (!dateString.isEmpty()) {
            // Ajusta este formato "dd/MM/yyyy" al formato EXACTO en que guardas tu fecha
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date date = parser.parse(dateString);
                cal.setTime(date);
                targetMonth0Based = cal.get(Calendar.MONTH);
                targetYear = cal.get(Calendar.YEAR);
            } catch (ParseException e) {
                Log.e("Estadisticas", "Error al parsear la fecha: " + dateString, e);
            }
        }

        // Convertir a 1-based para el Manager
        currentMonth = targetMonth0Based + 1;
        currentYear = targetYear;

        // 2. OBTENER DATOS DEL MANAGER

        // 2a. Obtener el total de gastos del mes (Necesario para porcentajes)
        double totalExpenses = manager.getTotalExpenseAmount(currentMonth, currentYear);

        // 2b. Obtener el promedio diario
        double dailyAverage = manager.calculateDailyAverage(currentMonth, currentYear);
        tvDailyAverage.setText(String.format(Locale.getDefault(), "$ %.2f", dailyAverage));

        // 2c. Obtener el resumen de gastos por categoría
        List<CategorySummary> categorySummaries = manager.getCategoryExpenseSummary(currentMonth, currentYear);

        // 3. POBLAR LA LISTA DE CATEGORÍAS
        CategorySummaryAdapter adapter = new CategorySummaryAdapter(this, categorySummaries, totalExpenses);
        rvCategoryList.setAdapter(adapter);

        // 4. GENERAR GRÁFICO (Lógica de Librería iría aquí)
        // La generación de gráficos es compleja y requiere librerías externas.
        // Aquí iría el código para alimentar el objeto Chart con 'categorySummaries'.
    }

    public void cmdSetting_onClick(View v) {
        Intent intent = new Intent(this, Activity_Setting.class);
        startActivity(intent);
    }
}