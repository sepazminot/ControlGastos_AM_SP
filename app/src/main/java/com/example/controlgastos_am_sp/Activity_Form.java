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

    EditText txtmonto, txtcategoria, txtdescripcion, txtfecha, txtmetodo, txtdivisa;
    private Spinner spinnerCategory;
    TextView txtresultado;
    Manager manager;
    Transactions tr;
    private int transactionId = -1;
    private SharedPreferences sharedPreferences;
    String moneda;
    private Button btnCrear;

    private Integer selectedCategoryId = null;

    // Mapeo para traducir la posición del Spinner a la ID de la categoría
    private List<Integer> categoryIdList = new ArrayList<>();
    // Mapeo para guardar el tipo (Ingreso/Gasto) de cada ID de categoría
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
        btnCrear = findViewById(R.id.btncrear);

        setupCategorySpinner();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            // Asumimos que el ID de la transacción es de tipo Long
            transactionId = extras.getInt("TRANSACTION_ID", -1);

            if (transactionId != -1) {
//                Toast.makeText(this, "Modo EDICIÓN: Cargando transacción ID: " + transactionId, Toast.LENGTH_LONG).show();
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
            // Corregido: Usar el ID de la categoría para seleccionar el elemento correcto del Spinner
            int categoryToSelectId = transaction.category;

            // Encontrar la posición del ID de la categoría en la lista de IDs
            int position = categoryIdList.indexOf(categoryToSelectId);

            if (position != -1) {
                // Seleccionar el ítem correcto en el Spinner
                spinnerCategory.setSelection(position);
                // También actualizar selectedCategoryId para que los listeners lo tengan
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
        // Paso clave: Convertir segundos (int) a milisegundos (long)
        // La clase Date requiere milisegundos para ser inicializada.
        long timestampInMillis = timestampInSeconds * 1000L;

        // Crear el objeto Date
        Date date = new Date(timestampInMillis);

        // Formatear la fecha para la visualización (puedes cambiar "dd/MM/yyyy" al formato que desees)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        return sdf.format(date);
    }

    private int convertDateStringToTimestamp(String dateString) {
        // Define el formato exacto que el usuario está utilizando: AAAA/MM/D
        // Si usas día de 1 dígito (1) o 2 dígitos (01), usar 'd' o 'dd' respectivamente.
        // Si tu formato es exactamente "2025/12/1", usa "yyyy/MM/d"
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            // 1. Parsear la cadena a un objeto Date
            Date date = parser.parse(dateString);

            // 2. Obtener los milisegundos desde la época Unix
            long milliseconds = date.getTime();

            // 3. Convertir a segundos (int), que es el formato de tu base de datos
            // NOTA: Asegúrate que tu campo 'date' en la clase Transactions es 'int'
            return (int) (milliseconds / 1000L);

        } catch (ParseException e) {
            // Si el usuario introduce un formato incorrecto (ej: "hola"),
            // muestra un error y usa la fecha actual como valor predeterminado (fall-back).
            e.printStackTrace();
            Toast.makeText(this, "Error en el formato de fecha(dd/MM/yyyy). Usando fecha actual.", Toast.LENGTH_SHORT).show();

            // Devolver la fecha actual en segundos como contingencia
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
                // ESTE CÓDIGO SE EJECUTA CUANDO LA TASA LLEGA CORRECTAMENTE
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
        // La ejecución del programa continúa aquí, ANTES de que onSuccess o onFailure se llamen.
        Log.i("API", "La solicitud de tasas ha sido enviada (fetchRates ha terminado de ejecutarse).");
    }

    /**
     * Configura el listener para el Spinner de Categoría y carga todas las categorías.
     */
    private void setupCategorySpinner() {
        // Cargar todas las categorías al inicio
        loadAllCategories();

        // Listener para el Spinner de CATEGORÍA
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El 'position' ahora mapea directamente a categoryIdList
                if (position >= 0 && position < categoryIdList.size()) {
                    // Guarda el ID de la categoría seleccionada
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

        // Disparar la selección inicial si hay datos
        if (categoryIdList.size() > 0) {
            spinnerCategory.setSelection(0);
        }
    }

    /**
     * Carga el Spinner con todas las categorías (Ingreso y Gasto).
     */
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

        // Establecer la ID seleccionada inicial si existen categorías
        if (!categoryIdList.isEmpty()) {
            selectedCategoryId = categoryIdList.get(0);
        }
    }
}