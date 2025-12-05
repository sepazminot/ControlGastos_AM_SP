package com.example.controlgastos_am_sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Manager {

    private DB database;
    private SQLiteDatabase db;
    private static final String TABLE_NAME = "transactions";
    private static final String TABLE_C = "categories";
    private static final String KEY_ID = "id";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE = "date";
    private static final String KEY_PAYMENT_METHOD = "payment_method";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String TAG = "API_CALL";

    public Manager(Context context, String nombreDB, int nVersion) {
        database = new DB(context, nombreDB, null, nVersion);
    }

    private void openWriteDB() {
        if (db == null || !db.isOpen() || db.isReadOnly()) {
            db = database.getWritableDatabase();
        }
    }

    private void openReadDB() {
        if (db == null || !db.isOpen()) {
            db = database.getReadableDatabase();
        }
    }

    public long Create(Transactions transaction) {
        openWriteDB();
        ContentValues values = new ContentValues();
        values.put(KEY_AMOUNT, transaction.amount);
        values.put(KEY_CATEGORY, transaction.category);
        values.put(KEY_DESCRIPTION, transaction.description);
        values.put(KEY_DATE, transaction.date);
        values.put(KEY_PAYMENT_METHOD, transaction.paymentMethod);
        values.put(KEY_CREATED_AT, transaction.createdAt);

        long newId = db.insert(TABLE_NAME, null, values);
        db.close();

        return newId;
    }

    public List<Transactions> ReadAll() {
        List<Transactions> transactionList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        openReadDB();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Transactions transaction = new Transactions();
                transaction.id = cursor.getInt(0);
                transaction.amount = cursor.getFloat(1);
                transaction.category = cursor.getInt(2);
                transaction.description = cursor.getString(3);
                transaction.date = cursor.getInt(4);
                transaction.paymentMethod = cursor.getString(5);
                transaction.createdAt = cursor.getInt(6);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactionList;
    }

    public Transactions ReadById(int id) {
        openReadDB();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{KEY_ID, KEY_AMOUNT, KEY_CATEGORY, KEY_DESCRIPTION, KEY_DATE, KEY_PAYMENT_METHOD, KEY_CREATED_AT},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Transactions transaction = new Transactions();
            transaction.id = cursor.getInt(0);
            transaction.amount = cursor.getFloat(1);
            transaction.category = cursor.getInt(2);
            transaction.description = cursor.getString(3);
            transaction.date = cursor.getInt(4);
            transaction.paymentMethod = cursor.getString(5);
            transaction.createdAt = cursor.getInt(6);
            cursor.close();
            return transaction;
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return null;
    }

    public int Update(Transactions transaction) {
        openWriteDB();

        ContentValues values = new ContentValues();
        values.put(KEY_AMOUNT, transaction.amount);
        values.put(KEY_CATEGORY, transaction.category);
        values.put(KEY_DESCRIPTION, transaction.description);
        values.put(KEY_DATE, transaction.date);
        values.put(KEY_PAYMENT_METHOD, transaction.paymentMethod);
//        values.put(KEY_CREATED_AT, transaction.createdAt);

        int rowsAffected = db.update(
                TABLE_NAME,
                values,
                KEY_ID + " = ?",
                new String[]{String.valueOf(transaction.id)}
        );
        db.close();
        return rowsAffected;
    }

    public int Delete(int id) {
        openWriteDB();
        int rowsDeleted = db.delete(
                TABLE_NAME,
                KEY_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        db.close();
        return rowsDeleted;
    }

    public void fetchRates(String baseCurrency, String currency, ConversionRateCallback callback) {

        ApiService service = RetrofitClient.getExchangeService();
        Call<ExchangeRates> call = service.getLatestRates(baseCurrency);
        call.enqueue(new Callback<ExchangeRates>() {
            @Override
            public void onResponse(Call<ExchangeRates> call, Response<ExchangeRates> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExchangeRates ratesResponse = response.body();

                    // Comprobamos si la moneda destino está en el mapa de tasas
                    if (ratesResponse.getRates() != null && ratesResponse.getRates().containsKey(currency)) {

                        Double rate = ratesResponse.getRates().get(currency);

                        // 1. LLAMADA DE ÉXITO: Devolvemos el valor a través de la interfaz
                        callback.onSuccess(rate);

                    } else {
                        // 2. LLAMADA DE FALLO: La moneda destino no se encontró en la respuesta
                        callback.onFailure("Tasa para " + currency + " no encontrada en la respuesta del servidor.");
                    }

                } else {
                    Log.e(TAG, "Respuesta fallida: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ExchangeRates> call, Throwable t) {
                Log.e(TAG, "Error de red o conexión: " + t.getMessage(), t);
            }
        });
    }

    public List<Transactions> getFilteredAndSortedTransactions(String transactionType,
                                                               Integer categoryId,
                                                               Long startDate,
                                                               Long endDate) {

        List<Transactions> transactionList = new ArrayList<>();
        openReadDB();

        // 1. Construcción dinámica de la cláusula WHERE y Argumentos
        StringBuilder whereClause = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        // 2. Filtrar por TIPO de Transacción ("Ingreso" o "Gasto")
        // Este filtro usa el campo 'type' de la tabla 'categories' (TABLE_C)
        if (transactionType != null && !transactionType.isEmpty() &&
                (transactionType.equals("Ingreso") || transactionType.equals("Gasto"))) {
            whereClause.append("C.").append("type").append(" = ?");
            selectionArgs.add(transactionType);
        }

        // 3. Filtrar por ID de Categoría
        if (categoryId != null && categoryId > 0) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("T.").append(KEY_CATEGORY).append(" = ?");
            selectionArgs.add(String.valueOf(categoryId));
        }

        // 4. Filtrar por Rango de Fechas (Timestamp)
        if (startDate != null && startDate > 0) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("T.").append(KEY_DATE).append(" >= ?");
            selectionArgs.add(String.valueOf(startDate));
        }

        if (endDate != null && endDate > 0) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("T.").append(KEY_DATE).append(" <= ?");
            selectionArgs.add(String.valueOf(endDate));
        }

        // 5. Construir la Consulta Completa con JOIN
        String SELECT_QUERY = "SELECT T.*, C.name, C.type "  // Seleccionar columnas de transaccion y nombre/tipo de categoría
                + "FROM " + TABLE_NAME + " T "
                + "JOIN " + TABLE_C + " C ON T." + KEY_CATEGORY + " = C.id";

        if (whereClause.length() > 0) {
            SELECT_QUERY += " WHERE " + whereClause.toString();
        }

        SELECT_QUERY += " ORDER BY T." + KEY_DATE + " DESC";

        Cursor cursor = null;
        // El tipo de transacción (Ingreso/Gasto) se obtiene haciendo un JOIN con la tabla categories.
        // Por simplicidad, este método solo filtra por ID de categoría y rango de fechas.
        // Si quieres filtrar por tipo (Ingreso/Gasto), necesitarás implementar el JOIN aquí.

        try {
            cursor = db.rawQuery(SELECT_QUERY, selectionArgs.toArray(new String[0]));
            if (cursor.moveToFirst()) {
                do {
                    // Mapear los datos del Cursor a un objeto Transaction
                    Transactions transaction = new Transactions();
                    transaction.id = cursor.getInt(0);
                    transaction.amount = cursor.getFloat(1);
                    transaction.category = cursor.getInt(2);
                    transaction.description = cursor.getString(3);
                    transaction.date = cursor.getInt(4);
                    transaction.paymentMethod = cursor.getString(5);
                    transaction.createdAt = cursor.getInt(6);
                    transactionList.add(transaction);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("Manager", "Error al obtener transacciones filtradas: ", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return transactionList;
    }

    // ***************************************************************
    // 1. MÉTODOS AUXILIARES PARA FECHAS
    // ***************************************************************

    /**
     * Calcula los timestamps (en segundos) de inicio y fin del mes actual.
     *
     * @return Arreglo de long: [timestampInicio, timestampFin]
     */
    private long[] getStartAndEndOfCurrentMonth(int mes, int anio) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.YEAR, anio);
        calendar.set(Calendar.MONTH, mes - 1); // mes - 1
        // --- 1. Inicio del Mes (00:00:00 del día 1) ---
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis() / 1000L; // Convertir a segundos

        // --- 2. Fin del Mes (23:59:59 del último día) ---
        calendar.add(Calendar.MONTH, 1); // Avanza al primer día del siguiente mes
        calendar.add(Calendar.SECOND, -1); // Retrocede un segundo (último segundo del mes actual)
        long endOfMonth = calendar.getTimeInMillis() / 1000L; // Convertir a segundos

        return new long[]{startOfMonth, endOfMonth};
    }

    // ***************************************************************
    // 2. MÉTODOS DE CONSULTA PARA EL DASHBOARD
    // ***************************************************************

    /**
     * Obtiene la suma total de montos para el tipo de transacción especificado
     * (GASTO o INGRESO) para el mes actual.
     *
     * @param transactionType El tipo de categoría ('GASTO' o 'INGRESO').
     * @return La suma total de montos como double.
     */
    private double getMonthlyTotalByType(String transactionType, int mes, int anio) {
        openReadDB();
        double total = 0.0;

        // Obtener los límites del mes actual
        long[] monthLimits = getStartAndEndOfCurrentMonth(mes, anio);
        long startTimestamp = monthLimits[0];
        long endTimestamp = monthLimits[1];

        // Consulta SQL con JOIN y filtrado por fecha y tipo de categoría
        // El SUM(T.amount) es el resultado que queremos.
        String QUERY = "SELECT SUM(T." + KEY_AMOUNT + ") "
                + "FROM " + TABLE_NAME + " T "
                + "JOIN " + TABLE_C + " C "
                + "ON T." + KEY_CATEGORY + " = C.id "
                + "WHERE C.type = ? " // Filtro por tipo de categoría
                + "AND T." + KEY_DATE + " BETWEEN ? AND ?"; // Filtro por rango de fecha

        // Usamos String.valueOf() para los timestamps al pasarlos como argumentos
        String[] selectionArgs = new String[]{
                transactionType,
                String.valueOf(startTimestamp),
                String.valueOf(endTimestamp)
        };

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(QUERY, selectionArgs);

            if (cursor.moveToFirst()) {
                // El resultado de SUM() es la columna 0
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e("ManagerDB", "Error al obtener el total mensual para " + transactionType, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return total;
    }

    /**
     * Obtiene la suma total de INGRESOS para el mes actual.
     */
    public double getMonthlyIncome(int mes, int anio) {
        // Suponemos que la columna 'type' tiene el valor 'INGRESO'
        return getMonthlyTotalByType("Ingreso", mes, anio);
    }

    /**
     * Obtiene la suma total de GASTOS para el mes actual.
     */
    public double getMonthlyExpenses(int mes, int anio) {
        // Suponemos que la columna 'type' tiene el valor 'GASTO'
        // NOTA: Si en tu base de datos los gastos se guardan como valores positivos
        // y quieres que el resultado sea negativo para el balance, puedes negarlo aquí:
        return getMonthlyTotalByType("Gasto", mes, anio);
        //return -getMonthlyTotalByType("Gasto", mes,anio); // Opcional, si prefieres devolver un número negativo
    }

    // ***************************************************************
    // 2. MÉTODOS DE CONSULTA PARA EL DASHBOARD Y ESTADÍSTICAS (EXISTENTES Y NUEVOS)
    // ***************************************************************


    /**
     * Obtiene la suma total de GASTOS para el mes y año dados.
     */
    public double getTotalExpenseAmount(int mes, int anio) {
        // Devuelve el valor POSITIVO para usarlo en cálculos de promedio/porcentaje
        return getMonthlyTotalByType("Gasto", mes, anio);
    }

    // ***************************************************************
    // NUEVOS MÉTODOS PARA ESTADÍSTICAS
    // ***************************************************************

    /**
     * Obtiene el listado de gastos totales por cada categoría para el mes y año dados.
     *
     * @param mes  El mes (1-12).
     * @param anio El año.
     * @return Lista de objetos CategorySummary con nombre y monto.
     */
    public List<CategorySummary> getCategoryExpenseSummary(int mes, int anio) {
        List<CategorySummary> summaryList = new ArrayList<>();
        openReadDB();

        long[] monthLimits = getStartAndEndOfCurrentMonth(mes, anio);
        long startTimestamp = monthLimits[0];
        long endTimestamp = monthLimits[1];

        // Consulta SQL: SUMAR el monto agrupado por el nombre de la categoría,
        // filtrado por tipo 'Gasto' y el rango de fecha.
        String QUERY = "SELECT C.name, SUM(T." + KEY_AMOUNT + ") "
                + "FROM " + TABLE_NAME + " T "
                + "JOIN " + TABLE_C + " C "
                + "ON T." + KEY_CATEGORY + " = C.id "
                + "WHERE C.type = 'Gasto' "
                + "AND T." + KEY_DATE + " BETWEEN ? AND ? "
                + "GROUP BY C.name"
                + " HAVING SUM(T." + KEY_AMOUNT + ") > 0 " // Solo categorías con gasto > 0
                + "ORDER BY SUM(T." + KEY_AMOUNT + ") DESC"; // Ordenar de mayor a menor gasto

        String[] selectionArgs = new String[]{
                String.valueOf(startTimestamp),
                String.valueOf(endTimestamp)
        };

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(QUERY, selectionArgs);

            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                double amount = cursor.getDouble(1);
                summaryList.add(new CategorySummary(name, amount));
            }
        } catch (Exception e) {
            Log.e("ManagerDB", "Error al obtener el resumen de gastos por categoría", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return summaryList;
    }

    /**
     * Calcula el promedio de gasto diario para un mes y año dados.
     *
     * @param mes  El mes (1-12).
     * @param anio El año.
     * @return El promedio de gasto diario como double.
     */
    public double calculateDailyAverage(int mes, int anio) {
        double totalExpenses = getTotalExpenseAmount(mes, anio);

        if (totalExpenses == 0.0) {
            return 0.0;
        }

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        // Establecer el mes y año
        calendar.set(Calendar.YEAR, anio);
        calendar.set(Calendar.MONTH, mes - 1);

        // Obtener el número total de días en ese mes.
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // NOTA: Para un cálculo más preciso, podrías dividir por el número de días que han transcurrido
        // hasta la fecha actual si estás analizando el mes actual. Pero, para el promedio del mes completo:
        return totalExpenses / daysInMonth;
    }

    // --- NUEVO MÉTODO AUXILIAR PARA OBTENER CATEGORÍAS FILTRADAS ---

    /**
     * Obtiene todas las categorías de un tipo específico (ej: "Ingreso" o "Gasto").
     *
     * @param type El tipo de categoría.
     * @return Un mapa (ID, Nombre) de las categorías.
     */
    public Map<Integer, String> getCategoriesByType(String type) {
        Map<Integer, String> categoryMap = new HashMap<>();
        openReadDB();

        String SELECT_QUERY = "SELECT id,name "
                + " FROM " + TABLE_C
                + " WHERE type = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{type});

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                categoryMap.put(id, name);
            }
        } catch (Exception e) {
            Log.e("Manager", "Error al obtener categorías por tipo: ", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return categoryMap;
    }

    public Map<Integer, String> getCategories() {
        Map<Integer, String> categoryMap = new HashMap<>();
        openReadDB();

        String SELECT_QUERY = "SELECT id,name "
                + " FROM " + TABLE_C;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_QUERY, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                categoryMap.put(id, name);
            }
        } catch (Exception e) {
            Log.e("Manager", "Error al obtener categorías: ", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return categoryMap;
    }
}
