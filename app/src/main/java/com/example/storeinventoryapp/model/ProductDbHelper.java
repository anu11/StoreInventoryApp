package com.example.storeinventoryapp.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for Products table. Manages database creation and version management.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final String TAG = ProductDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "product_inventory.db";
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String TYPE_TEXT = " TEXT";
        final String TYPE_INTEGER = " INTEGER";
        final String TYPE_FLOAT = " REAL";
        final String TYPE_TIMESTAMP = " TIMESTAMP";
        final String NOT_NULL = " NOT NULL";
        final String SEPARATOR = ", ";

        String SQL_CREATE_TABLE =  "CREATE TABLE " + ProductEntity.ProductEntry.TABLE_NAME + " ("
                + ProductEntity.ProductEntry._ID + TYPE_INTEGER + " PRIMARY KEY AUTOINCREMENT" + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME + TYPE_TEXT + NOT_NULL + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE + TYPE_FLOAT + NOT_NULL + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY + TYPE_INTEGER + NOT_NULL + " DEFAULT 0" + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_PRODUCT_IMAGE + TYPE_TEXT + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME + TYPE_TEXT + NOT_NULL + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE + TYPE_TEXT + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL + TYPE_TEXT + NOT_NULL + SEPARATOR
                + ProductEntity.ProductEntry.COLUMN_LAST_UPDATED + TYPE_TIMESTAMP + NOT_NULL + " DEFAULT CURRENT_TIMESTAMP"
                + ")";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProductEntity.ProductEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
