package com.example.controlgastos_am_sp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Activity_Form extends AppCompatActivity {

    EditText txtmonto, txtdescripcion, txtfecha, txtmetodo, txtdivisa;
    private Spinner spinnerCategory;
    TextView txtresultado;
    Manager manager;
    Transactions tr;
    private int transactionId = -1;
    private SharedPreferences sharedPreferences;
    String moneda;

    private Integer selectedCategoryId = null;

    private List<Integer> categoryIdList = new ArrayList<>();
    private Map<Integer, String> categoryIdToTypeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        manager = new Manager(this, "transaccion.db", 1);

        txtmonto = findViewById(R.id.txtmonto);
        txtdescripcion = findViewById(R.id.txtdescripcion);
        txtfecha = findViewById(R.id.txtfechat);
        txtmetodo = findViewById(R.id.txtmetodo);
        txtdivisa = findViewById(R.id.txtdivisa);
        txtresultado = findViewById(R.id.txtresultado);
        spinnerCategory = findViewById(R.id.spinner_category_form);

        setupCategorySpinner();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            transactionId = extras.getInt("TRANSACTION_ID", -1);

            if (transactionId != -1) {
                Toast.makeText(this, "Modo EDICIÓN: Cargando transacción ID: " + transactionId, Toast.LENGTH_LONG).show();
                loadTransactionData(transactionId);
            } else {
                Toast.makeText(this, "Modo CREACIÓN: Nueva Transacción", Toast.LENGTH_LONG).show();
            }
        }

        sharedPreferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        moneda = sharedPreferences.getString("moneda", "usd");
    }

    public void cmdCrear_onclick(View v) {

        if (txtmonto.getText().toString().isEmpty() || txtdescripcion.getText().toString().isEmpty() || selectedCategoryId == null) {
            Toast.makeText(this, "Por favor, completa Monto, Descripción y selecciona una Categoría.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategoryId == null || !categoryIdList.contains(selectedCategoryId)) {
            Toast.makeText(this, "Categoría seleccionada inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        String montoStr = txtmonto.getText().toString();
        double amount = 0.0;
        try {
            amount = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(v.getContext(), "Error: Monto inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        int categoryId;
        try {
            categoryId = selectedCategoryId;
        } catch (NumberFormatException e) {
            Toast.makeText(v.getContext(), "Error: ID de Categoría inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        String description = txtdescripcion.getText().toString();

        String dateString = txtfecha.getText().toString().trim();
        int timestampInSeconds = convertDateStringToTimestamp(dateString);

        String paymentMethod = txtmetodo.getText().toString();
        long createdAt = new Date().getTime();
        tr = new Transactions(
                amount,
                categoryId,
                description,
                timestampInSeconds,
                paymentMethod,
                createdAt
        );
        long idInsertado = manager.Create(tr);

        if (idInsertado != -1) {
            Toast.makeText(v.getContext(), "Transacción creada con ID: " + idInsertado, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(v.getContext(), "Error al crear la transacción.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadTransactionData(int id) {
        Transactions transaction = manager.ReadById(id);

        if (transaction != null) {
            txtmonto.setText(String.valueOf(transaction.amount));
            int categoryToSelectId = transaction.category;

            int position = categoryIdList.indexOf(categoryToSelectId);

            if (position != -1) {
                spinnerCategory.setSelection(position);
                selectedCategoryId = categoryToSelectId;
            } else {
                Log.w("Form", "Categoría ID " + categoryToSelectId + " no encontrada en el Spinner.");
                Toast.makeText(this, "Advertencia: Categoría no encontrada.", Toast.LENGTH_SHORT).show();
            }
            txtdescripcion.setText(transaction.description);
            String formattedDate = formatDateFromTimestamp(transaction.date);
            txtfecha.setText(formattedDate);
            txtmetodo.setText(transaction.paymentMethod);
        } else {
            Toast.makeText(this, "Error: Transacción no encontrada.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String formatDateFromTimestamp(long timestampInSeconds) {
        long timestampInMillis = timestampInSeconds * 1000L;
        Date date = new Date(timestampInMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    private int convertDateStringToTimestamp(String dateString) {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date date = parser.parse(dateString);
            long milliseconds = date.getTime();
            return (int) (milliseconds / 1000L);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error en el formato de fecha(dd/MM/yyyy). Usando fecha actual.", Toast.LENGTH_SHORT).show();
            return (int) (System.currentTimeMillis() / 1000L);
        }
    }

    public void cmdActualizar_onclick(View v) {
        String montoStr = txtmonto.getText().toString();
        double amount = 0.0;
        try {
            amount = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(v.getContext(), "Error: Monto inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        int categoryId;
        try {
            categoryId = selectedCategoryId;
        } catch (NumberFormatException e) {
            Toast.makeText(v.getContext(), "Error: ID de Categoría inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        String description = txtdescripcion.getText().toString();

        String dateString = txtfecha.getText().toString().trim();
        int timestampInSeconds = convertDateStringToTimestamp(dateString);

        String paymentMethod = txtmetodo.getText().toString();
        tr = new Transactions(
                amount,
                categoryId,
                description,
                timestampInSeconds,
                paymentMethod
        );
        tr.id = this.transactionId;
        int rowsAffected = manager.Update(tr);

        if (rowsAffected > 0) {
            Toast.makeText(v.getContext(), "Transacción actualizada", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Activity_List.class);
            startActivity(intent);
        } else {
            Toast.makeText(v.getContext(), "Error al actualizar la transacción.", Toast.LENGTH_LONG).show();
        }
    }

    public void cmdConversionProcess_onclick(View v) {

        String target = txtdivisa.getText().toString();

        manager.fetchRates(moneda, target, new ConversionRateCallback() {
            @Override
            public void onSuccess(Double rate) {
                Log.d("API", "Tasa de " + moneda + " a " + target + " recibida: " + rate);
                double monto = Double.parseDouble(txtmonto.getText().toString());
                double total = monto * rate;
                String resultado = monto + " " + moneda + " = " + total + " " + target;
                txtresultado.setText(resultado);

            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e("API", "Fallo al obtener la tasa: " + errorMsg);
                Toast.makeText(Activity_Form.this, "Fallo al obtener la tasa", Toast.LENGTH_SHORT).show();
            }
        });
        Log.i("API", "La solicitud de tasas ha sido enviada (fetchRates ha terminado de ejecutarse).");
    }

    private void setupCategorySpinner() {
        loadAllCategories();

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < categoryIdList.size()) {
                    selectedCategoryId = categoryIdList.get(position);
                    Log.d("Form", "Categoría seleccionada ID: " + selectedCategoryId +
                            " (Tipo: " + categoryIdToTypeMap.get(selectedCategoryId) + ")");
                } else {
                    selectedCategoryId = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null;
            }
        });

        if (categoryIdList.size() > 0) {
            spinnerCategory.setSelection(0);
        }
    }

    private void loadAllCategories() {
        categoryIdList.clear();
        List<String> spinnerNames = new ArrayList<>();
        Map<Integer, String> allCategories = manager.getCategories();

        if (allCategories != null) {
            for (Map.Entry<Integer, String> entry : allCategories.entrySet()) {
                Integer id = entry.getKey();
                String name = entry.getValue();

                categoryIdList.add(id);
                spinnerNames.add(name);
            }
        }

        if (spinnerNames.isEmpty()) {
            spinnerNames.add("No hay categorías disponibles");
            selectedCategoryId = null;
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerNames
        );
        spinnerCategory.setAdapter(categoryAdapter);

        if (!categoryIdList.isEmpty()) {
            selectedCategoryId = categoryIdList.get(0);
        }
    }
}