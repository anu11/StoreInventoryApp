package com.example.storeinventoryapp.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.storeinventoryapp.R;

/**
 * Inventory App.
 */

public class ProductProvider extends ContentProvider {
    public static final String LOG = ProductProvider.class.getSimpleName();
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private ProductDbHelper mDbHelper;
    private static final String TAG_NAME = "name";
    private static final String TAG_PRICE = "price";
    private static final String TAG_STOCK = "stock";
    private static final String TAG_SUPPLIER = "supplier";
    private static final String TAG_EMAIL = "email";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /** Static initializer - run the first time anything is called from this class */
    static {
        sUriMatcher.addURI(ProductEntity.CONTENT_AUTHORITY, ProductEntity.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductEntity.CONTENT_AUTHORITY, ProductEntity.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase sqLiteDBReadable = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int matchCode = sUriMatcher.match(uri);
        switch (matchCode) {
            case PRODUCTS:
                cursor = sqLiteDBReadable.query(ProductEntity.ProductEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                selection = ProductEntity.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = sqLiteDBReadable.query(ProductEntity.ProductEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id;
        String columnsToValidate = TAG_NAME + "|" + TAG_PRICE + "|" + "|"
                + TAG_STOCK + "|" + TAG_SUPPLIER + "|" + TAG_EMAIL;
        boolean isValidInput = validateInput(contentValues, columnsToValidate);
        if (!isValidInput) {
            Log.e(LOG, (getContext().getString(R.string.error_insert, uri)));
            return null;
        }
        SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();
        id = sqLiteDBWritable.insert(ProductEntity.ProductEntry.TABLE_NAME,
                null, contentValues);

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * This method updates products
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int matchCode = sUriMatcher.match(uri);
        switch (matchCode) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);

            case PRODUCT_ID:
                selection = ProductEntity.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }
    }


    /**
     * This method validates input data for updates and applies the updates
     */
    public int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        String columnsToValidate = null;
        StringBuilder stringBuilder = new StringBuilder();
        final String SEPARATOR = "|";

        if (contentValues.size() == 0) {
            return 0;
        }

        if (contentValues.containsKey(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME)) {
            stringBuilder.append(TAG_NAME);
        } else if (contentValues.containsKey(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            stringBuilder.append(SEPARATOR).append(TAG_PRICE);
        } else if (contentValues.containsKey(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            stringBuilder.append(SEPARATOR).append(TAG_STOCK);
        } else if (contentValues.containsKey(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME)) {
            stringBuilder.append(SEPARATOR).append(TAG_SUPPLIER);
        } else if (contentValues.containsKey(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL)) {
            stringBuilder.append(SEPARATOR).append(TAG_EMAIL);
        }

        columnsToValidate = stringBuilder.toString();
        boolean isValidInput = validateInput(contentValues, columnsToValidate);

        if (!isValidInput) {
            return 0;
        }
        SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();
        int rowsUpdated = sqLiteDBWritable.update(ProductEntity.ProductEntry.TABLE_NAME,
                contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    /**
     * This method validates the inputs used in INSERT and UPDATE queries
     */
    public boolean validateInput(ContentValues values, String columns) {
        String[] columnArgs = columns.split("|");
        String productName = null;
        Double productPrice = null;
        Integer productStock = null;
        String supplier = null;
        String supplierEmail = null;

        for (int i = 0; i < columnArgs.length; i++) {

            if (columnArgs[i].equals(TAG_NAME)) {
                // Check if Product Name is not null
                productName = values.getAsString(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME);
                if (productName == null || productName.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_name));
                }
            } else if (columnArgs[i].equals(TAG_PRICE)) {
                // Check if Price is provided
                productPrice = values.getAsDouble(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE);
                if (productPrice == null || productPrice < 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_price));
                }
            } else if (columnArgs[i].equals(TAG_STOCK)) {
                productStock = values.getAsInteger(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY);
                if (productStock == null || productStock < 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_stock));
                }
            } else if (columnArgs[i].equals(TAG_SUPPLIER)) {
                supplier = values.getAsString(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME);
                if (supplier == null || supplier.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_supplier));
                }
            } else if (columnArgs[i].equals(TAG_EMAIL)) {
                supplierEmail = values.getAsString(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL);
                if (supplierEmail == null || supplierEmail.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_supplier_email));
                }
            }
        }
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                rowsDeleted = sqLiteDBWritable.delete(ProductEntity.ProductEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            case PRODUCT_ID:
                selection = ProductEntity.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqLiteDBWritable.delete(ProductEntity.ProductEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntity.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntity.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }
    }
}
