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

                    if (ratesResponse.getRates() != null && ratesResponse.getRates().containsKey(currency)) {
                        Double rate = ratesResponse.getRates().get(currency);
                        callback.onSuccess(rate);

                    } else {
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

        StringBuilder whereClause = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (transactionType != null && !transactionType.isEmpty() &&
                (transactionType.equals("Ingreso") || transactionType.equals("Gasto"))) {
            whereClause.append("C.").append("type").append(" = ?");
            selectionArgs.add(transactionType);
        }

        if (categoryId != null && categoryId > 0) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("T.").append(KEY_CATEGORY).append(" = ?");
            selectionArgs.add(String.valueOf(categoryId));
        }

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

        String SELECT_QUERY = "SELECT T.*, C.name, C.type "
                + "FROM " + TABLE_NAME + " T "
                + "JOIN " + TABLE_C + " C ON T." + KEY_CATEGORY + " = C.id";

        if (whereClause.length() > 0) {
            SELECT_QUERY += " WHERE " + whereClause.toString();
        }

        SELECT_QUERY += " ORDER BY T." + KEY_DATE + " DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_QUERY, selectionArgs.toArray(new String[0]));
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
        } catch (Exception e) {
            Log.e("Manager", "Error al obtener transacciones filtradas: ", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return transactionList;
    }

    private long[] getStartAndEndOfCurrentMonth(int mes, int anio) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.YEAR, anio);
        calendar.set(Calendar.MONTH, mes - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis() / 1000L;

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        long endOfMonth = calendar.getTimeInMillis() / 1000L;

        return new long[]{startOfMonth, endOfMonth};
    }

    private double getMonthlyTotalByType(String transactionType, int mes, int anio) {
        openReadDB();
        double total = 0.0;

        long[] monthLimits = getStartAndEndOfCurrentMonth(mes, anio);
        long startTimestamp = monthLimits[0];
        long endTimestamp = monthLimits[1];

        String QUERY = "SELECT SUM(T." + KEY_AMOUNT + ") "
                + "FROM " + TABLE_NAME + " T "
                + "JOIN " + TABLE_C + " C "
                + "ON T." + KEY_CATEGORY + " = C.id "
                + "WHERE C.type = ? "
                + "AND T." + KEY_DATE + " BETWEEN ? AND ?";

        String[] selectionArgs = new String[]{
                transactionType,
                String.valueOf(startTimestamp),
                String.valueOf(endTimestamp)
        };

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(QUERY, selectionArgs);

            if (cursor.moveToFirst()) {
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

    public double getMonthlyIncome(int mes, int anio) {
        return getMonthlyTotalByType("Ingreso", mes, anio);
    }

    public double getMonthlyExpenses(int mes, int anio) {
        return getMonthlyTotalByType("Gasto", mes, anio);
    }

    public double getTotalExpenseAmount(int mes, int anio) {
        return getMonthlyTotalByType("Gasto", mes, anio);
    }

    public List<CategorySummary> getCategoryExpenseSummary(int mes, int anio) {
        List<CategorySummary> summaryList = new ArrayList<>();
        openReadDB();

        long[] monthLimits = getStartAndEndOfCurrentMonth(mes, anio);
        long startTimestamp = monthLimits[0];
        long endTimestamp = monthLimits[1];
        String QUERY = "SELECT C.name, SUM(T." + KEY_AMOUNT + ") "
                + "FROM " + TABLE_NAME + " T "
                + "JOIN " + TABLE_C + " C "
                + "ON T." + KEY_CATEGORY + " = C.id "
                + "WHERE C.type = 'Gasto' "
                + "AND T." + KEY_DATE + " BETWEEN ? AND ? "
                + "GROUP BY C.name"
                + " HAVING SUM(T." + KEY_AMOUNT + ") > 0 "
                + "ORDER BY SUM(T." + KEY_AMOUNT + ") DESC";

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

    public double calculateDailyAverage(int mes, int anio) {
        double totalExpenses = getTotalExpenseAmount(mes, anio);
        if (totalExpenses == 0.0) {
            return 0.0;
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.YEAR, anio);
        calendar.set(Calendar.MONTH, mes - 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return totalExpenses / daysInMonth;
    }

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
