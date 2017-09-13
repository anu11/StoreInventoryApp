package com.example.storeinventoryapp.model;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * ProductEntity: schema for Products Database
 */

public class ProductEntity {

    private ProductEntity() {}

    public static final String CONTENT_AUTHORITY = "com.example.storeinventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    public static class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PRODUCT_NAME ="product_name";

        public final static String COLUMN_PRODUCT_PRICE ="product_price";

        public final static String COLUMN_PRODUCT_QUANTITY="product_quantity";

        public final static String COLUMN_PRODUCT_IMAGE ="product_image";

        public final static String COLUMN_SUPPLIER_NAME ="product_supplier_name";

        public final static String COLUMN_SUPPLIER_PHONE ="product_supplier_phone";

        public final static String COLUMN_SUPPLIER_EMAIL ="product_supplier_email";

        public final static String COLUMN_LAST_UPDATED ="product_last_updated";
    }
}
