package com.example.storeinventoryapp.viewmodel;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.storeinventoryapp.R;
import com.example.storeinventoryapp.model.ProductEntity;

import static android.content.ContentValues.TAG;

public class ProductCursorAdapter extends CursorAdapter {

    private static Context mContext;

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    /**
     * This class describes the view items to create a list item
     */
    public static class ProductViewHolder {
        TextView textViewProductName;
        TextView textViewPrice;
        TextView textViewStock;
        ImageButton buttonSale;

        public ProductViewHolder(View itemView) {
            textViewProductName = (TextView) itemView.findViewById(R.id.text_product_name);
            textViewPrice = (TextView) itemView.findViewById(R.id.text_product_price);
            textViewStock = (TextView) itemView.findViewById(R.id.text_product_stock);
            buttonSale = (ImageButton) itemView.findViewById(R.id.button_sale);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent ,false);
        ProductViewHolder holder = new ProductViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ProductViewHolder holder = (ProductViewHolder)view.getTag();
        final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntity.ProductEntry._ID));
        String productName = cursor.getString(cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME));
        Double price = cursor.getDouble(cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE));
        final int stock = cursor.getInt(cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY));

        holder.textViewProductName.setText(productName);
        holder.textViewPrice.setText(mContext.getString(R.string.display_product_price, price.doubleValue()));
        holder.textViewStock.setText(mContext.getString(R.string.display_product_stock, stock));

        holder.buttonSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(ProductEntity.ProductEntry.CONTENT_URI, productId);
                adjustStock(context, productUri, stock);
            }
        });
    }

    private void adjustStock(Context context, Uri productUri, int currentStock) {
        int newStock = (currentStock >= 1) ? currentStock - 1 : 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY, newStock);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (!(numRowsUpdated > 0)) {
            Log.e(TAG, context.getString(R.string.error_stock_update));
        }
    }
}
