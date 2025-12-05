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
    private Manager manager;
    private TextView tvTotalIncome, tvTotalExpenses, tvBalance, tvBudgetSpent, tvBudgetLimit, tvAlertStatus, tvBudgetPercentage;
    private ProgressBar budgetProgressBar;
    private String presupuesto;

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
        manager = new Manager(this, "transaccion.db", 1);

        sharedPreferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        presupuesto = sharedPreferences.getString("presupuesto", "0");
        loadDashboardData();
    }

    private void loadDashboardData() {

        String dateString = sharedPreferences.getString("fecha", "");

        Calendar cal = Calendar.getInstance();
        int targetMonth = cal.get(Calendar.MONTH);
        Log.e("Mes", "Error al parsear la fecha: " + targetMonth, null);
        int targetYear = cal.get(Calendar.YEAR);
        Log.e("Anio", "Error al parsear la fecha: " + targetYear, null);

        if (!dateString.isEmpty()) {
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date date = parser.parse(dateString);
                cal.setTime(date);
                targetMonth = cal.get(Calendar.MONTH);
                Log.e("Mes", "Error al parsear la fecha: " + targetMonth, null);
                targetYear = cal.get(Calendar.YEAR);
                Log.e("Anio", "Error al parsear la fecha: " + targetYear, null);

            } catch (ParseException e) {
                Log.e("Dashboard", "Error al parsear la fecha: " + dateString, e);
            }
        }

        float presupuestoFloat = Float.parseFloat(presupuesto);
        Log.e("Presupuesto", "Error al parsear la fecha: " + presupuestoFloat, null);
        tvBudgetLimit.setText(String.format(Locale.getDefault(), "Meta: $ %.2f", presupuestoFloat));

        double income = manager.getMonthlyIncome(targetMonth + 1, targetYear);
        double expenses = manager.getMonthlyExpenses(targetMonth + 1, targetYear);
        double balance = income - expenses;

        tvTotalIncome.setText(String.format(Locale.getDefault(), "$ %.2f", income));
        tvTotalExpenses.setText(String.format(Locale.getDefault(), "$ %.2f", Math.abs(expenses)));
        tvBalance.setText(String.format(Locale.getDefault(), "$ %.2f", balance));

        calculateBudgetProgress(expenses, presupuestoFloat);
    }

    private void calculateBudgetProgress(double expenses, double budgetLimit) {
        tvBudgetSpent.setText(String.format("Gastado: $ %.2f", expenses));

        if (budgetLimit > 0) {
            int percentage = (int) ((expenses / budgetLimit) * 100);

            budgetProgressBar.setProgress(Math.min(percentage, 100));
            tvBudgetPercentage.setText(String.format("%d%% utilizado", percentage));

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
            budgetProgressBar.setProgress(0);
            tvBudgetPercentage.setText("Presupuesto no configurado");
            tvAlertStatus.setText("Define un presupuesto para recibir alertas.");
        }
    }
}