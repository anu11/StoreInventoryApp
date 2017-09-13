package com.example.storeinventoryapp.viewcontroller;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.storeinventoryapp.R;
import com.example.storeinventoryapp.StoreInventoryUtil;
import com.example.storeinventoryapp.model.ProductEntity;


public class ProductEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = ProductEditorActivity.class.getSimpleName();
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int EXISTING_ITEM_LOADER = 0;
    private static final String IMAGE_URI = "IMAGE_URI";
    private EditText mEditTextProduct;
    private EditText mEditTextProductPrice;
    private EditText mEditTextProductStock;
    private EditText mEditTextSupplier;
    private EditText mEditTextSupplierPhone;
    private EditText mEditTextSupplierEmail;
    private Button mButtonAddImage;
    private ImageView mProductImage;
    private Button mFillTestData;

    private Uri mCurrentProductUri;
    private Uri mImageUri;

    private String product;
    private String strPrice;
    private Double productPrice;
    private String strStock;
    private int productStock;
    private String supplier;
    private String supplierPhone;
    private String supplierEmail;
    private String imagePath;


    // Boolean flag that keeps track of whether the item has been edited (true) or not (false)
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Initialize all UI components
        initializeUIElements();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // check if adding a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Check if CurrentProductUri is null  or not
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.title_add));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.title_edit));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
            mButtonAddImage.setText(getString(R.string.label_change_image));
        }
        mButtonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonImageClick();
            }
        });
        mFillTestData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTestData();
            }
        });
    }

    /**
     * This method initializes all components used in the Activity
     */
    public void initializeUIElements() {
        mEditTextProduct = (EditText) findViewById(R.id.edit_product_name);
        mEditTextProductPrice = (EditText) findViewById(R.id.edit_product_price);
        mEditTextProductStock = (EditText) findViewById(R.id.edit_product_stock);
        mEditTextSupplier = (EditText) findViewById(R.id.edit_supplier_name);
        mEditTextSupplierEmail = (EditText) findViewById(R.id.edit_supplier_email);
        mEditTextSupplierPhone = (EditText) findViewById(R.id.edit_supplier_phone);

        mButtonAddImage = (Button) findViewById(R.id.button_add_image);
        mProductImage = (ImageView) findViewById(R.id.image_product);

        mFillTestData = (Button) findViewById(R.id.button_test_add_data);
        // Setup OnTouchListeners on all the input fields, to determine if the user
        // has touched or modified them
        mEditTextProduct.setOnTouchListener(mTouchListener);
        mEditTextProductPrice.setOnTouchListener(mTouchListener);
        mEditTextProductStock.setOnTouchListener(mTouchListener);
        mEditTextSupplier.setOnTouchListener(mTouchListener);
        mEditTextSupplierEmail.setOnTouchListener(mTouchListener);
        mEditTextSupplierPhone.setOnTouchListener(mTouchListener);
    }




    /**
     * Method to select a picture from device's media storage
     */
    private void fillTestData() {
        mEditTextProduct.setText("Amazon Echo");
        mEditTextProductPrice.setText("40");
        mEditTextProductStock.setText("40");
        mEditTextSupplier.setText("Amazon");
        mEditTextSupplierEmail.setText("echo@amazon.com");
        mEditTextSupplierPhone.setText("4157703453");
    }


    /**
     * Method to select a picture from device's storage
     */
    private void buttonImageClick() {
        Intent imageIntent;

        if (Build.VERSION.SDK_INT < 19) {
            imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            imageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            imageIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        imageIntent.setType("image/*");
        if (imageIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(imageIntent, getString(R.string.action_select_picture)), IMAGE_REQUEST_CODE);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all store attributes, define a projection that contains
        // all columns from the store table
        String[] projection = {
                ProductEntity.ProductEntry._ID,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE,
                ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
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
            final int productStock = cursor.getInt(stockColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            final String phone = cursor.getString(phoneColumnIndex);
            final String email = cursor.getString(emailColumnIndex);
            String imageString = cursor.getString(imageColumnIndex);

            strPrice = Double.toString(price);
            strStock = Integer.toString(productStock);

            mEditTextProduct.setText(product);
            mEditTextSupplier.setText(supplier);
            mEditTextSupplierEmail.setText(email);
            mEditTextSupplierPhone.setText(phone);
            mEditTextProductPrice.setText(String.format("$%.2f", price));

            mEditTextProductStock.setText(String.valueOf(productStock));
            mProductImage.setImageBitmap(StoreInventoryUtil.getBitmapFromUri(
                    ProductEditorActivity.this.getApplicationContext(), Uri.parse(imageString),
                    mProductImage.getWidth(), mProductImage.getHeight()));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditTextProduct.setText("");
        mEditTextProductPrice.setText("");
        mEditTextProductStock.setText("");
        mEditTextSupplier.setText("");
        mEditTextSupplierEmail.setText("");
        mEditTextSupplierPhone.setText("");
    }

    /**
     * Method to set selected image to ImageView holder if request is successful
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            try {
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                mProductImage.setImageBitmap(StoreInventoryUtil.getBitmapFromUri(
                        ProductEditorActivity.this.getApplicationContext(), mImageUri,
                        mProductImage.getWidth(), mProductImage.getHeight()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inflate the menu options from the res/menu/menu_editor.xml file.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Method to handle actions when individual menu item is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mCurrentProductUri == null) {
                    addProduct();
                } else {
                    updateProduct();
                }
                return true;

            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                if (!mItemHasChanged && !hasEntry()) {
                    finish();
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                finish();
                                //NavUtils.navigateUpFromSameTask(ProductEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with navigating up to parent activity
        if (!mItemHasChanged && !hasEntry()) {
            onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        finish();
                    }
                };

        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener - click listener action to take when user confirms discarding changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes);
        builder.setPositiveButton(R.string.action_yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog and continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method to check if any entry has been made. This is to handle situation when motion event
     * is not detected, yet entries have been made (e.g. using emulator and typing using keyboard)
     */
    public boolean hasEntry() {
        boolean hasInput = false;

        if (!TextUtils.isEmpty(mEditTextProduct.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextProductPrice.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextProductStock.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextSupplier.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextSupplierEmail.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextSupplierPhone.getText().toString()) ||
                (mProductImage.getDrawable() != null)) {
            hasInput = true;
        }
        return hasInput;
    }

    /**
     * Method to add a new product to database
     */
    public void addProduct() {

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.

        if (getEditorInputs()) {
            ContentValues values = new ContentValues();
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME, product);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY, productStock);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_IMAGE, imagePath);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME, supplier);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);

            mCurrentProductUri = getContentResolver().insert(ProductEntity.ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            // If the row ID is -1, then there was an error with insertion.
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            if (mCurrentProductUri == null) {
                Toast.makeText(this, getString(R.string.error_insert_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.confirm_insert_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Method to update an existing product
     */
    public void updateProduct() {

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.

        if (getEditorInputs()) {
            ContentValues values = new ContentValues();
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME, product);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY, productStock);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME, supplier);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);

            int numRowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Display error message in Log if product stock fails to update
            if (!(numRowsUpdated > 0)) {
                Toast.makeText(this, getString(R.string.error_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.confirm_update_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Method to get editor inputs and validate them
     */
    public boolean getEditorInputs() {

        product = mEditTextProduct.getText().toString().trim();
        strPrice = mEditTextProductPrice.getText().toString().trim();
        strStock = mEditTextProductStock.getText().toString().trim();
        supplier = mEditTextSupplier.getText().toString().trim();
        supplierEmail = mEditTextSupplierEmail.getText().toString().trim();
        supplierPhone = mEditTextSupplierPhone.getText().toString().trim();

        if (TextUtils.isEmpty(product)) {
            mEditTextProduct.requestFocusFromTouch();
            Toast.makeText(this,getString(R.string.error_product), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(strPrice)) {
            mEditTextProductPrice.requestFocusFromTouch();
            Toast.makeText(this,getString(R.string.error_price), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(strStock)) {
            mEditTextProductStock.requestFocusFromTouch();
            Toast.makeText(this,getString(R.string.error_quantity), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(supplier)) {
            mEditTextSupplier.requestFocusFromTouch();
            Toast.makeText(this,getString(R.string.error_supplier), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check supplier email
        if (TextUtils.isEmpty(supplierEmail) || (!Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches())) {
            mEditTextSupplierEmail.requestFocusFromTouch();
            Toast.makeText(this,getString(R.string.error_email), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if image selected
        if (mCurrentProductUri == null) {
            if (mImageUri == null) {
                Toast.makeText(this,getString(R.string.error_no_image), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                imagePath = mImageUri.toString();
            }
        }

        strPrice = strPrice.replace("$", "");
        productPrice = Double.valueOf(strPrice);
        productStock = Integer.valueOf(strStock);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(IMAGE_URI, mImageUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(IMAGE_URI) &&
                !savedInstanceState.getString(IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(IMAGE_URI));
        }
    }
}
