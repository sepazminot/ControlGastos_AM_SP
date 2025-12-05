package com.example.controlgastos_am_sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DB extends SQLiteOpenHelper {
    public DB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table if not exists categories(id integer primary key autoincrement," +
                "name text(20)," +
                "type text(20)," +
                "icon text," +
                "color text)");

        db.execSQL("Create table if not exists transactions(id integer primary key autoincrement," +
                "amount real," +
                "category integer," +
                "description text(50)," +
                "date integer," +
                "payment_method text(25)," +
                "created_at intger," +
                "foreign key(category) references categories(id))");

        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertDefaultCategories(SQLiteDatabase db) {
        String[][] categories = new String[][]{
                {"Alimentación", "Gasto", "ic_food", "#FF5733"},
                {"Transporte", "Gasto", "ic_car", "#33FF57"},
                {"Educación", "Gasto", "ic_book", "#3357FF"},
                {"Entretenimiento", "Gasto", "ic_movie", "#FF33A1"},
                {"Salud", "Gasto", "ic_health", "#33FFF6"},
                {"Otros", "Gasto", "ic_expense_other", "#FFCC33"},

                // Ingresos
                {"Salario", "Ingreso", "ic_salary", "#1E8449"},
                {"Freelance", "Ingreso", "ic_freelance", "#2ECC71"},
                {"Beca", "Ingreso", "ic_scholarship", "#5DADE2"},
                {"Otros", "Ingreso", "ic_income_other", "#F4D03F"}
        };

        for (String[] cat : categories) {
            ContentValues values = new ContentValues();
            values.put("name", cat[0]);
            values.put("type", cat[1]);
            values.put("icon", cat[2]);
            values.put("color", cat[3]);

            db.insert("categories", null, values);
        }
    }
}
