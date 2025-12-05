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
    private Spinner filterTypeSpinner;
    private Spinner filterCategorySpinner;
    private Button filterDateButton;
    private String currentTypeFilter = null;
    private Integer currentCategoryIdFilter = null;
    private Long currentStartDateFilter = null;
    private Long currentEndDateFilter = null;
    private TextView tvStartDateFilter;
    private TextView tvEndDateFilter;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private List<Integer> categoryIdList = new ArrayList<>();
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

        List<Transactions> initialList = manager.getFilteredAndSortedTransactions(null, null, null, null);
        adapter = new TransactionAdapter(initialList, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback swipeHandler = new SwipeToDeleteCallback(this, adapter, manager);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        filterTypeSpinner = findViewById(R.id.spinner_type_filter);
        filterCategorySpinner = findViewById(R.id.spinner_category_filter);
        filterDateButton = findViewById(R.id.btn_filter_apply);
        tvStartDateFilter = findViewById(R.id.tv_start_date_filter);
        tvEndDateFilter = findViewById(R.id.tv_end_date_filter);

        setupFilterSpinners();
        setupDatePickers();

        filterDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFilters();
            }
        });
    }

    private void setupFilterSpinners() {
        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();

                if (selectedType.equals("Todos")) {
                    currentTypeFilter = null;
                } else {
                    currentTypeFilter = selectedType;
                }
                loadCategorySpinner(currentTypeFilter);
                applyCurrentFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentTypeFilter = null;
                loadCategorySpinner(null);
                applyCurrentFilters();
            }
        });

        filterCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    currentCategoryIdFilter = null;
                } else {
                    currentCategoryIdFilter = categoryIdList.get(position - 1);
                }

                applyCurrentFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCategoryIdFilter = null;
                applyCurrentFilters();
            }
        });
        loadCategorySpinner(null);
    }

    private void loadCategorySpinner(String type) {
        categoryIdList.clear();
        categoryNameMap.clear();
        List<String> spinnerNames = new ArrayList<>();
        spinnerNames.add("Todas las categorías");
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
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerNames
        );
        filterCategorySpinner.setAdapter(categoryAdapter);
        currentCategoryIdFilter = null;
    }

    private void applyCurrentFilters() {
        Log.d("Filtro", "Aplicando filtros: Tipo=" + currentTypeFilter + ", CatID=" + currentCategoryIdFilter);
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
        currentTypeFilter = null;
        currentCategoryIdFilter = null;
        currentStartDateFilter = null;
        currentEndDateFilter = null;
        filterTypeSpinner.setSelection(0);
        tvStartDateFilter.setText("Fecha Inicio");
        tvEndDateFilter.setText("Fecha Fin");
        applyCurrentFilters();
        Toast.makeText(this, "Filtros limpiados. Mostrando todas las transacciones.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionClick(Transactions transaction) {
        Toast.makeText(this, "Abriendo edición para ID: " + transaction.id, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Activity_Form.class);
        intent.putExtra("TRANSACTION_ID", transaction.id);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyCurrentFilters();
    }

    private void setupDatePickers() {
        tvStartDateFilter.setText("Fecha Inicio");
        tvEndDateFilter.setText("Fecha Fin");

        tvStartDateFilter.setOnClickListener(v -> showDatePickerDialog(tvStartDateFilter, true));
        tvEndDateFilter.setOnClickListener(v -> showDatePickerDialog(tvEndDateFilter, false));
    }

    private void showDatePickerDialog(final TextView dateTextView, final boolean isStartDate) {
        final Calendar c = Calendar.getInstance();
        Long currentTimestamp = isStartDate ? currentStartDateFilter : currentEndDateFilter;
        if (currentTimestamp != null) {
            c.setTimeInMillis(currentTimestamp * 1000);
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    if (isStartDate) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                        selectedDate.set(Calendar.MINUTE, 0);
                        selectedDate.set(Calendar.SECOND, 0);
                        selectedDate.set(Calendar.MILLISECOND, 0);
                        currentStartDateFilter = selectedDate.getTimeInMillis() / 1000;
                        if (currentEndDateFilter != null && currentStartDateFilter > currentEndDateFilter) {
                            currentEndDateFilter = null;
                            tvEndDateFilter.setText("Fecha Fin");
                        }
                    } else {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
                        selectedDate.set(Calendar.MINUTE, 59);
                        selectedDate.set(Calendar.SECOND, 59);
                        selectedDate.set(Calendar.MILLISECOND, 999);
                        currentEndDateFilter = selectedDate.getTimeInMillis() / 1000;
                        if (currentStartDateFilter != null && currentEndDateFilter < currentStartDateFilter) {
                            currentStartDateFilter = null;
                            tvStartDateFilter.setText("Fecha Inicio");
                        }
                    }
                    dateTextView.setText(dateFormat.format(selectedDate.getTime()));
                    applyCurrentFilters();

                }, year, month, day);
        datePickerDialog.show();
    }
}