package com.example.storeinventoryapp.viewcontroller;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.storeinventoryapp.R;
import com.example.storeinventoryapp.StoreInventoryUtil;
import com.example.storeinventoryapp.model.ProductEntity;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int PRODUCT_LOADER = 1;

    private TextView mTextProduct;
    private TextView mTextProductPrice;
    private TextView mTextProductStock;
    private TextView mTextSupplier;
    private TextView mTextSupplierPhone;
    private TextView mTextSupplierEmail;
    private ImageButton mButtonPhone;
    private ImageButton mButtonEmail;
    private ImageButton mButtonDecrease;
    private ImageButton mButtonIncrease;
    private ImageView mImageProduct;
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize all UI components
        mTextProduct = (TextView) findViewById(R.id.text_product_name);
        mTextProductPrice = (TextView) findViewById(R.id.text_product_price);
        mTextProductStock = (TextView) findViewById(R.id.text_stock);
        mTextSupplier = (TextView) findViewById(R.id.text_supplier_name);
        mTextSupplierPhone = (TextView) findViewById(R.id.text_supplier_phone);
        mTextSupplierEmail = (TextView) findViewById(R.id.text_supplier_email);
        mButtonEmail = (ImageButton) findViewById(R.id.button_email);
        mButtonPhone = (ImageButton) findViewById(R.id.button_phone);
        mButtonIncrease = (ImageButton) findViewById(R.id.button_increase);
        mButtonDecrease = (ImageButton) findViewById(R.id.button_decrease);
        mImageProduct = (ImageView) findViewById(R.id.image_product);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri != null) {
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,                       // Parent activity context
                mCurrentProductUri,         // Table to query
                null,                       // Projection
                null,                       // Selection clause
                null,                       // Selection arguments
                null                        // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            DatabaseUtils.dumpCursor(cursor);

            int productColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE);
            int stockColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the respective column index
            final String product = cursor.getString(productColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            final int productQuantity = cursor.getInt(stockColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            final String phone = cursor.getString(phoneColumnIndex);
            final String email = cursor.getString(emailColumnIndex);
            final String imageString = cursor.getString(imageColumnIndex);

            mTextProduct.setText(product);
            mTextProductPrice.setText("$ " + String.format("%.02f", price));
            mTextProductStock.setText(Integer.toString(productQuantity));
            mTextSupplier.setText(supplier);
            mTextSupplierEmail.setText(email);

            if (!TextUtils.isEmpty(phone)) {
                mTextSupplierPhone.setText(phone);
            } else {
                mButtonPhone.setVisibility(View.GONE);
            }
            // Display image attached to the product
            ViewTreeObserver viewTreeObserver = mImageProduct.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageProduct.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageProduct.setImageBitmap(StoreInventoryUtil.getBitmapFromUri(
                            DetailActivity.this.getApplicationContext(), Uri.parse(imageString),
                            mImageProduct.getWidth(), mImageProduct.getHeight()));
                }
            });

            mButtonDecrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustStock(mCurrentProductUri, (productQuantity - 1));
                }
            });

            mButtonIncrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustStock(mCurrentProductUri, (productQuantity + 1));
                }
            });

            mButtonEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    orderByEmail(email);
                }
            });

            if (mButtonPhone.getVisibility() == View.VISIBLE) {
                mButtonPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        orderByPhone(phone);
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // No action here
    }

    /**
     * Method to increase / decrease product stock
     *
     */
    private int adjustStock(Uri itemUri, int newStockCount) {
        if (newStockCount < 0) {
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY, newStockCount);
        int numRowsUpdated = getContentResolver().update(itemUri, values, null, null);
        return numRowsUpdated;
    }
    /**
     * Method to send email to supplier
     */
    public void orderByEmail(String emailAddress) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    /**
     * Method to call supplier
     */
    public void orderByPhone(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    /**
     * Inflate the menu options from the res/menu/menu_delete.xml file.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Method to handle actions when individual menu item is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_edit:
                editProduct();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deleteProduct();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to launch EditorActivity with the product URI
     */
    public void editProduct() {
        Intent intent = new Intent(DetailActivity.this, ProductEditorActivity.class);
        intent.setData(mCurrentProductUri);
        startActivity(intent);
    }

    /**
     * Method to delete the product
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.error_delete_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful
                Toast.makeText(this, getString(R.string.confirm_delete_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
