package de.teyzer.genie.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.Serializable;
import java.util.Arrays;

import de.teyzer.genie.R;
import de.teyzer.genie.model.FoodType;
import de.teyzer.genie.model.Product;

public class NewProductActivity extends AppCompatActivity {
    public static final String REQUEST_ACTION = "type";
    public static final String REQUEST_ACTION_NEW = "new";
    public static final String REQUEST_ACTION_EDIT = "edit";

    public static final String PARAM_BARCODE = "barcode";
    public static final String PARAM_FOOD_TYPES = "food_types";
    public static final String PARAM_PRODUCT = "product";

    public static final String RESULT_ID = "id";
    public static final String RESULT_NAME = "name";
    public static final String RESULT_STORE = "store";
    public static final String RESULT_FOOD_TYPE = "food_type";
    public static final String RESULT_PACK_SIZE = "pack_size";
    public static final String RESULT_BARCODE = "barcode";

    private FoodType[] foodTypes;
    private Product product;
    private String barcode;

    private boolean editMode = false;

    private Spinner foodTypeSpinner;
    private EditText nameBox;
    private EditText storeBox;
    private EditText quantityBox;
    private TextView unitLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //X-Button anstelle vom Icon zum Abbrechen
        actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);

        foodTypeSpinner = (Spinner) findViewById(R.id.food_type_spinner);
        nameBox = (EditText) findViewById(R.id.name_text_box);
        storeBox = (EditText) findViewById(R.id.store_text_box);
        quantityBox = (EditText) findViewById(R.id.new_product_pack_size);
        unitLabel = (TextView) findViewById(R.id.new_product_unit);

        Intent intent = getIntent();
        String action = intent.getStringExtra(REQUEST_ACTION);
        Parcelable[] arr = intent.getParcelableArrayExtra(PARAM_FOOD_TYPES);
        foodTypes = Arrays.copyOfRange(arr, 0, arr.length, FoodType[].class);

        if (action.equals(REQUEST_ACTION_EDIT)) {
            editMode = true;
            setTitle("Produkt bearbeiten");
            product = (Product) intent.getSerializableExtra(PARAM_PRODUCT);

            nameBox.setText(product.getName());
            storeBox.setText(product.getStore());
            quantityBox.setText(product.getQuantityString());
            unitLabel.setText(product.getFoodType().getQuantityTypeString(this));

            int position = 0;
            int foodTypeId = product.getFoodType().getId();
            for (int i = 0; i < foodTypes.length; i++) {
                FoodType foodType = foodTypes[i];
                if (foodType.getId() == foodTypeId) {
                    position = i;
                    break;
                }
            }
            foodTypeSpinner.setSelection(position);

        } else {
            editMode = false;
            setTitle("Neues Produkt");
        }
        barcode = intent.getStringExtra(PARAM_BARCODE);

        String[] foodTypeNames = new String[foodTypes.length];
        for (int i = 0; i < foodTypes.length; i++) {
            foodTypeNames[i] = foodTypes[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, foodTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        foodTypeSpinner.setAdapter(adapter);

        foodTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                unitLabel.setText(foodTypes[position].getQuantityTypeString(getBaseContext()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_product, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveAndFinish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveAndFinish() {
        String name = nameBox.getText().toString();
        String store = storeBox.getText().toString();
        int foodTypePosition = foodTypeSpinner.getSelectedItemPosition();
        FoodType foodType = foodTypes[foodTypePosition];
        Double packSize = 1.0;
        try {
            packSize = Double.parseDouble(quantityBox.getText().toString());
        } catch (Exception ex) {

        }

        Intent result = new Intent();

        if (editMode) {
            result.putExtra(RESULT_ID, product.getId());
        }
        result.putExtra(RESULT_NAME, name);
        result.putExtra(RESULT_STORE, store);
        result.putExtra(RESULT_FOOD_TYPE, foodType);
        result.putExtra(RESULT_PACK_SIZE, packSize);
        result.putExtra(RESULT_BARCODE, barcode);
        setResult(RESULT_OK, result);
        finish();
    }
}
