package com.example.controlgastos_am_sp;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Activity_List extends AppCompatActivity implements OnTransactionClickListener {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private Manager manager;

    // Elementos de UI para filtrado
    private Spinner filterTypeSpinner; // Tipo: Ingreso/Gasto
    private Spinner filterCategorySpinner; // Categoría
    private Button filterDateButton; // Para aplicar el rango de fechas (o abrir un diálogo)

    // Variables para almacenar los filtros seleccionados
    private String currentTypeFilter = null;
    private Integer currentCategoryIdFilter = null;
    private Long currentStartDateFilter = null;
    private Long currentEndDateFilter = null;
    private TextView tvStartDateFilter; // Nuevo campo de fecha inicio
    private TextView tvEndDateFilter;   // Nuevo campo de fecha fin
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Mapeo para traducir la posición del Spinner a la ID de la categoría
    private List<Integer> categoryIdList = new ArrayList<>();
    // Mapeo de nombres de categorías a IDs
    private Map<String, Integer> categoryNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        manager = new Manager(this, "transaccion.db", 1);
        recyclerView = findViewById(R.id.rv_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cargar datos iniciales
        List<Transactions> initialList = manager.getFilteredAndSortedTransactions(null, null, null, null);
        adapter = new TransactionAdapter(initialList, this);
        recyclerView.setAdapter(adapter);

        // 3. Implementar Swipe para Eliminar
        ItemTouchHelper.Callback swipeHandler = new SwipeToDeleteCallback(this, adapter, manager);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // 4. Inicializar UI de Filtros
        filterTypeSpinner = findViewById(R.id.spinner_type_filter);
        filterCategorySpinner = findViewById(R.id.spinner_category_filter);
        filterDateButton = findViewById(R.id.btn_filter_apply);
        tvStartDateFilter = findViewById(R.id.tv_start_date_filter); // Inicializar nuevo campo
        tvEndDateFilter = findViewById(R.id.tv_end_date_filter);     // Inicializar nuevo campo

        setupFilterSpinners();
        setupDatePickers();

        // 5. Listener para aplicar filtros (Botón principal)
        filterDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NOTA: Aquí se podría abrir un DateRangePicker, por ahora solo aplicamos filtros de Spinners

                // Si el texto del botón es "Filtrar por Fecha/Aplicar", asumimos que también aplica los Spinners
                clearAllFilters();
            }
        });
    }


    // --- MÉTODOS DE FILTRADO ---

    /**
     * Configura los listeners iniciales para los Spinners.
     */
    private void setupFilterSpinners() {
        // Listener para el filtro de TIPO (Ingreso/Gasto)
        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el tipo de transacción seleccionado (ej: "Ingreso", "Gasto" o "Todos")
                String selectedType = parent.getItemAtPosition(position).toString();

                if (selectedType.equals("Todos")) { // Asumo que "Todos" es la primera opción
                    currentTypeFilter = null;
                } else {
                    currentTypeFilter = selectedType;
                }

                // 1. Recargar el Spinner de Categorías según el tipo
                loadCategorySpinner(currentTypeFilter);

                // 2. Aplicar los filtros inmediatamente
                applyCurrentFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentTypeFilter = null;
                loadCategorySpinner(null);
                applyCurrentFilters();
            }
        });

        // Listener para el filtro de CATEGORÍA
        filterCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el ID de la categoría basado en la posición del Spinner
                // La posición 0 es siempre "Todas las categorías" (ID = null)

                if (position == 0) {
                    currentCategoryIdFilter = null;
                } else {
                    // Restamos 1 porque la posición 0 es "Todas las categorías"
                    currentCategoryIdFilter = categoryIdList.get(position - 1);
                }

                // Aplicar los filtros inmediatamente después de seleccionar la categoría
                applyCurrentFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCategoryIdFilter = null;
                applyCurrentFilters();
            }
        });

        // Cargar la lista de categorías inicial (si el tipo inicial es "Todos")
        loadCategorySpinner(null);
    }

    /**
     * Carga el Spinner de categorías basándose en el tipo de transacción seleccionado.
     *
     * @param type "Ingreso", "Gasto" o null (para todos).
     */
    private void loadCategorySpinner(String type) {

        // Limpiar las listas de mapeo
        categoryIdList.clear();
        categoryNameMap.clear();
        List<String> spinnerNames = new ArrayList<>();

        // Opción predeterminada
        spinnerNames.add("Todas las categorías");

        // Obtener categorías filtradas del Manager
        Map<Integer, String> categories;
        if (type == null) {
        } else {
            categories = manager.getCategoriesByType(type);

            for (Map.Entry<Integer, String> entry : categories.entrySet()) {
                categoryIdList.add(entry.getKey());
                categoryNameMap.put(entry.getValue(), entry.getKey());
                spinnerNames.add(entry.getValue());
            }
        }

        // Asignar el nuevo adaptador al Spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerNames
        );
        filterCategorySpinner.setAdapter(categoryAdapter);

        // Reiniciar el filtro de categoría al recargar la lista
        currentCategoryIdFilter = null;
    }

    /**
     * Método principal para aplicar los filtros del estado actual (Spinners y Fechas)
     * y recargar el RecyclerView.
     */
    private void applyCurrentFilters() {
        Log.d("Filtro", "Aplicando filtros: Tipo=" + currentTypeFilter + ", CatID=" + currentCategoryIdFilter);

        // Llamar a la función principal de filtrado
        List<Transactions> filteredList = manager.getFilteredAndSortedTransactions(
                currentTypeFilter,
                currentCategoryIdFilter,
                currentStartDateFilter,
                currentEndDateFilter
        );

        adapter.updateData(filteredList);
        Toast.makeText(this, "Filtros aplicados. Mostrando " + filteredList.size() + " transacciones.", Toast.LENGTH_SHORT).show();
    }

    private void clearAllFilters() {
        // 1. Resetear variables de estado
        currentTypeFilter = null;
        currentCategoryIdFilter = null;
        currentStartDateFilter = null;
        currentEndDateFilter = null;

        // 2. Resetear UI (Spinners)
        // Esto automáticamente llama a loadCategorySpinner y applyCurrentFilters
        filterTypeSpinner.setSelection(0);

        // 3. Resetear UI (Fechas)
        tvStartDateFilter.setText("Fecha Inicio");
        tvEndDateFilter.setText("Fecha Fin");

        // 4. Aplicar la lista sin filtros
        applyCurrentFilters();
        Toast.makeText(this, "Filtros limpiados. Mostrando todas las transacciones.", Toast.LENGTH_SHORT).show();
    }

    // Implementación del Click para Editar
    @Override
    public void onTransactionClick(Transactions transaction) {
        Toast.makeText(this, "Abriendo edición para ID: " + transaction.id, Toast.LENGTH_SHORT).show();

        // Lógica para iniciar la Activity de Edición
        Intent intent = new Intent(this, Activity_Form.class); // Asumimos que tienes una Activity EditTransactionActivity
        intent.putExtra("TRANSACTION_ID", transaction.id);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar la lista, manteniendo los filtros que estén activos en la UI.
        // Llamamos a applyCurrentFilters() para que lea el estado actual de los Spinners.
        applyCurrentFilters();
    }

    /**
     * Configura los click listeners para los TextViews de fecha.
     */
    private void setupDatePickers() {
        // Establecer texto inicial
        tvStartDateFilter.setText("Fecha Inicio");
        tvEndDateFilter.setText("Fecha Fin");

        tvStartDateFilter.setOnClickListener(v -> showDatePickerDialog(tvStartDateFilter, true));
        tvEndDateFilter.setOnClickListener(v -> showDatePickerDialog(tvEndDateFilter, false));
    }

    /**
     * Muestra el diálogo de selección de fecha y actualiza el estado y el TextView.
     *
     * @param dateTextView El TextView a actualizar.
     * @param isStartDate  Si es la fecha de inicio (true) o la fecha de fin (false).
     */
    private void showDatePickerDialog(final TextView dateTextView, final boolean isStartDate) {
        final Calendar c = Calendar.getInstance();

        // Usar la fecha actualmente seleccionada si existe
        Long currentTimestamp = isStartDate ? currentStartDateFilter : currentEndDateFilter;
        if (currentTimestamp != null) {
            c.setTimeInMillis(currentTimestamp * 1000); // Convertir segundos a milisegundos
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // NOTA IMPORTANTE: Para startDate queremos el inicio del día (00:00:00)
                    // Para endDate queremos el final del día (23:59:59)
                    if (isStartDate) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                        selectedDate.set(Calendar.MINUTE, 0);
                        selectedDate.set(Calendar.SECOND, 0);
                        selectedDate.set(Calendar.MILLISECOND, 0);
                        currentStartDateFilter = selectedDate.getTimeInMillis() / 1000; // Guardar en segundos

                        // Si la fecha de inicio es posterior a la de fin, ajustamos la de fin
                        if (currentEndDateFilter != null && currentStartDateFilter > currentEndDateFilter) {
                            currentEndDateFilter = null;
                            tvEndDateFilter.setText("Fecha Fin");
                        }
                    } else {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
                        selectedDate.set(Calendar.MINUTE, 59);
                        selectedDate.set(Calendar.SECOND, 59);
                        selectedDate.set(Calendar.MILLISECOND, 999);
                        currentEndDateFilter = selectedDate.getTimeInMillis() / 1000; // Guardar en segundos

                        // Si la fecha de fin es anterior a la de inicio, ajustamos la de inicio
                        if (currentStartDateFilter != null && currentEndDateFilter < currentStartDateFilter) {
                            currentStartDateFilter = null;
                            tvStartDateFilter.setText("Fecha Inicio");
                        }
                    }

                    // Actualizar el TextView con el formato legible
                    dateTextView.setText(dateFormat.format(selectedDate.getTime()));

                    // Aplicar filtros automáticamente después de la selección
                    applyCurrentFilters();

                }, year, month, day);
        datePickerDialog.show();
    }
}