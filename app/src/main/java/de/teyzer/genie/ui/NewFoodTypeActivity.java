package de.teyzer.genie.ui;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import de.teyzer.genie.R;
import de.teyzer.genie.model.FoodType;

public class NewFoodTypeActivity extends AppCompatActivity {
    public static final String REQUEST_ACTION = "type";
    public static final String REQUEST_ACTION_NEW = "new";
    public static final String REQUEST_ACTION_EDIT = "edit";

    public static final String RESULT_ID = "id";
    public static final String RESULT_NAME = "name";
    public static final String RESULT_QUANTITY_TYPE = "quantity_type";
    public static final String RESULT_PREFERRED_MEAL = "preferred_meal";
    public static final String RESULT_PACK_SIZE = "pack_size";
    public static final String RESULT_CURRENT_QUANTITY = "current_quantity";

    int editFoodTypeId;
    boolean editMode = false;

    Spinner quantityTypeSpinner;
    Spinner preferredMealSpinner;
    TextView packSizeUnitLabel;
    TextView currentQuantityUnitLabel;
    EditText nameBox;
    EditText packSizeBox;
    EditText currentQuantityBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_food_type);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //X-Button anstelle vom Icon zum Abbrechen
        actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);

        //Dropdown-listen setzen
        quantityTypeSpinner = (Spinner) findViewById(R.id.quantity_type_spinner);
        preferredMealSpinner = (Spinner) findViewById(R.id.preferred_meal_spinner);
        packSizeUnitLabel = (TextView) findViewById(R.id.pack_size_unit_label);
        currentQuantityUnitLabel = (TextView) findViewById(R.id.current_quantity_unit_label);
        nameBox = (EditText) findViewById(R.id.name_text_box);
        packSizeBox = (EditText) findViewById(R.id.pack_size_box);
        currentQuantityBox = (EditText) findViewById(R.id.current_quantity_box);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.quantity_units, android.R.layout.simple_spinner_item);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantityTypeSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.preferred_meals, android.R.layout.simple_spinner_item);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        preferredMealSpinner.setAdapter(adapter2);

        //Label bei üblicher Packungsgröße updaten
        quantityTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    //Anzahl ist ausgewählt
                    packSizeUnitLabel.setText("");
                    currentQuantityUnitLabel.setText("");
                } else {
                    String[] units = getResources().getStringArray(R.array.quantity_units);
                    packSizeUnitLabel.setText(units[position]);
                    currentQuantityUnitLabel.setText(units[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent intent = getIntent();
        String action = intent.getStringExtra(REQUEST_ACTION);
        switch (action) {
            case REQUEST_ACTION_EDIT:
                setTitle("Essen bearbeiten");

                editFoodTypeId = intent.getIntExtra(RESULT_ID, 0);
                String name = intent.getStringExtra(RESULT_NAME);
                Double commonPackSize = intent.getDoubleExtra(RESULT_PACK_SIZE, 1.0);
                String preferredMeal = intent.getStringExtra(RESULT_PREFERRED_MEAL);
                String quantityType = intent.getStringExtra(RESULT_QUANTITY_TYPE);
                Double currentQuantity = intent.getDoubleExtra(RESULT_CURRENT_QUANTITY, 1.0);

                nameBox.setText(name);
                packSizeBox.setText(FoodType.getQuantityString(commonPackSize));
                currentQuantityBox.setText(FoodType.getQuantityString(currentQuantity));
                quantityTypeSpinner.setSelection(FoodType.QUANTITY_TYPES.indexOf(quantityType));
                preferredMealSpinner.setSelection(FoodType.PREFERRED_MEALS.indexOf(preferredMeal));

                editMode = true;
                break;
            default:
                setTitle("Neues Essen");
                editMode = false;
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_food_type, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveAndFinish();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        Log.d("id", String.valueOf(id));
        Log.d("toString", item.toString());
        return super.onOptionsItemSelected(item);
    }

    public void saveAndFinish() {
        //TODO testen
        String name = nameBox.getText().toString();
        Double packSize = 1.0;
        try {
            packSize = Double.parseDouble(packSizeBox.getText().toString());
        } catch (Exception ex) {

        }
        Double currentQuantity = 0.0;
        try {
            currentQuantity = Double.parseDouble(currentQuantityBox.getText().toString());
        } catch (Exception ex) {

        }
        String quantityType = FoodType.QUANTITY_TYPES.get(quantityTypeSpinner.getSelectedItemPosition());
        String preferredMeal = FoodType.PREFERRED_MEALS.get(preferredMealSpinner.getSelectedItemPosition());

        Intent result = new Intent();
        if (editMode) {
            result.putExtra(RESULT_ID, editFoodTypeId);
        }
        result.putExtra(RESULT_NAME, name);
        result.putExtra(RESULT_PACK_SIZE, packSize);
        result.putExtra(RESULT_PREFERRED_MEAL, preferredMeal);
        result.putExtra(RESULT_QUANTITY_TYPE, quantityType);
        result.putExtra(RESULT_CURRENT_QUANTITY, currentQuantity);

        setResult(RESULT_OK, result);
        finish();
    }
}
