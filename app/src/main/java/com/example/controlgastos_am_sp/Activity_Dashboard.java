package com.example.controlgastos_am_sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Activity_Dashboard extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Manager manager; // Tu gestor de base de datos
    private double currentBudgetLimit = 0.0;

    // UI components
    private TextView tvTotalIncome, tvTotalExpenses, tvBalance, tvBudgetSpent, tvBudgetLimit, tvAlertStatus, tvBudgetPercentage;
    private ProgressBar budgetProgressBar;
    private String presupuesto, fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpenses = findViewById(R.id.tv_total_expenses);
        tvBalance = findViewById(R.id.tv_balance);
        tvBudgetSpent = findViewById(R.id.tv_budget_spent);
        tvBudgetLimit = findViewById(R.id.tv_budget_limit);
        tvAlertStatus = findViewById(R.id.tv_alert_status);
        tvBudgetPercentage = findViewById(R.id.tv_budget_percentage);
        budgetProgressBar = findViewById(R.id.budget_progress_bar);
        // 2. Inicializar Manager y SharedPreferences
        manager = new Manager(this, "transaccion.db", 1);

        sharedPreferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        presupuesto = sharedPreferences.getString("presupuesto", "0");
        loadDashboardData();
    }

    private void loadDashboardData() {

        // *******************************************************************
        // LÓGICA CLAVE: Obtener el Mes y Año de la cadena de fecha guardada
        // *******************************************************************
        String dateString = sharedPreferences.getString("fecha", "");

        Calendar cal = Calendar.getInstance(); // Por defecto, usa el mes y año actual
        int targetMonth = cal.get(Calendar.MONTH);
        Log.e("Mes", "Error al parsear la fecha: " +targetMonth, null);
        int targetYear = cal.get(Calendar.YEAR);
        Log.e("Anio", "Error al parsear la fecha: " +targetYear, null);


        // A. Parsear la cadena si existe
        // NOTA IMPORTANTE: Ajusta el formato "yyyy/MM/dd" al formato EXACTO
        // en que guardas la fecha en SharedPreferences (ej: "dd-MM-yyyy" o "yyyy-MM-dd")
        if (!dateString.isEmpty()) {
            // Ejemplo: Suponiendo que la fecha guardada es "YYYY/MM/DD"
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date date = parser.parse(dateString);
                cal.setTime(date);
                targetMonth = cal.get(Calendar.MONTH); // 0-based
                Log.e("Mes", "Error al parsear la fecha: " +targetMonth, null);
                targetYear = cal.get(Calendar.YEAR);
                Log.e("Anio", "Error al parsear la fecha: " +targetYear, null);

            } catch (ParseException e) {
                Log.e("Dashboard", "Error al parsear la fecha: " + dateString, e);
                // Si hay error, se sigue usando el mes y año actual (valor por defecto de 'cal')
            }
        }

        // B. Cargar el presupuesto (clave "monthly_budget" guardada como float)
        float presupuestoFloat = Float.parseFloat(presupuesto);
        Log.e("Presupuesto", "Error al parsear la fecha: " + presupuestoFloat, null);
        tvBudgetLimit.setText(String.format(Locale.getDefault(), "Meta: $ %.2f", presupuestoFloat));

        // C. Obtener los datos del resumen del mes llamando al Manager con el Mes y Año
        double income = manager.getMonthlyIncome(targetMonth+1, targetYear);
        double expenses = manager.getMonthlyExpenses(targetMonth+1, targetYear);
        double balance = income - expenses; // Asumiendo que expenses devuelve un valor positivo

        // Actualizar UI del Resumen
        tvTotalIncome.setText(String.format(Locale.getDefault(), "$ %.2f", income));
        // Usamos Math.abs() para asegurar que los gastos se muestren como positivos en el UI
        tvTotalExpenses.setText(String.format(Locale.getDefault(), "$ %.2f", Math.abs(expenses)));
        tvBalance.setText(String.format(Locale.getDefault(), "$ %.2f", balance));

        // D. Calcular y mostrar progreso del presupuesto
        calculateBudgetProgress(expenses, presupuestoFloat);
    }

    private void calculateBudgetProgress(double expenses, double budgetLimit) {
        tvBudgetSpent.setText(String.format("Gastado: $ %.2f", expenses));

        if (budgetLimit > 0) {
            int percentage = (int) ((expenses / budgetLimit) * 100);

            budgetProgressBar.setProgress(Math.min(percentage, 100)); // Máximo 100% en la barra
            tvBudgetPercentage.setText(String.format("%d%% utilizado", percentage));

            // D. Lógica de Alertas
            if (percentage >= 100) {
                tvAlertStatus.setText("¡Alerta! Has superado el presupuesto.");
                tvAlertStatus.setTextColor(getResources().getColor(R.color.expense_red, null));
            } else if (percentage >= 80) {
                tvAlertStatus.setText("¡Advertencia! Has superado el 80% de tu presupuesto.");
                tvAlertStatus.setTextColor(getResources().getColor(R.color.warning_orange, null));
            } else {
                tvAlertStatus.setText("El presupuesto está bajo control.");
                tvAlertStatus.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            }
        } else {
            // Manejo si el presupuesto es 0 o no está configurado
            budgetProgressBar.setProgress(0);
            tvBudgetPercentage.setText("Presupuesto no configurado");
            tvAlertStatus.setText("Define un presupuesto para recibir alertas.");
        }
    }
}