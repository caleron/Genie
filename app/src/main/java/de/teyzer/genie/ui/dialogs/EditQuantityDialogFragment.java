package de.teyzer.genie.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.teyzer.genie.R;

public class EditQuantityDialogFragment extends DialogFragment {
    public static final String FRAGMENT_TAG = "edit_food_type_quantity_dialog";

    public static final String RESULT_ID = "id";
    public static final String RESULT_QUANTITY = "quantity";

    public static final String STATE_QUANTITY = "quantity";
    public static final String STATE_NAME = "name";
    public static final String STATE_UNIT = "unit";
    public static final String STATE_FOOD_TYPE_ID = "food_type_id";
    public static final String STATE_QUANTITY_TYPE = "quantity_type";
    public static final String STATE_COMMON_PACK_SIZE = "pack_size";

    private ArrayList<String> numberPickerValues;

    private int foodTypeId;
    private String name;
    private Double quantity;
    private String unit;
    private String quantityType;
    private Double commonPackSize;

    @Bind(R.id.edit_quantity_dialog_title)
    TextView titleLabel;
    @Bind(R.id.edit_quantity_dialog_number_picker)
    NumberPicker numberPicker;
    @Bind(R.id.edit_quantity_dialog_unit)
    TextView unitLabel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.dialog_edit_quantity, null);
        ButterKnife.bind(this, rootView);

        builder.setView(rootView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendResult(false);
                    }
                }).setNeutralButton(R.string.empty, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sendResult(true);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
            }
        });

        Bundle args = getArguments();
        if (savedInstanceState != null) {
            quantity = savedInstanceState.getDouble(STATE_QUANTITY);
        } else {
            quantity = args.getDouble(STATE_QUANTITY);
        }
        name = args.getString(STATE_NAME);
        foodTypeId = args.getInt(STATE_FOOD_TYPE_ID);
        unit = args.getString(STATE_UNIT);
        quantityType = args.getString(STATE_QUANTITY_TYPE);
        commonPackSize = args.getDouble(STATE_COMMON_PACK_SIZE);

        //Numberpicker mit Werten ausstatten
        //je nach Einheit sollten die Schritte mindestens 1/10 der üblichen Packungsgröße sein
        //und das Maximum bei 10-20 Packungen, aber mindestens das Doppelte vom aktuellen Wert

        numberPickerValues = generateArray();

        numberPicker.setDisplayedValues(numberPickerValues.toArray(new String[numberPickerValues.size()]));
        numberPicker.setMaxValue(numberPickerValues.size() - 1);
        numberPicker.setValue(numberPickerValues.indexOf(stringValue(quantity)));

        titleLabel.setText(getString(R.string.edit_quantity_title, name));
        unitLabel.setText(unit);

        return builder.create();
    }

    private void sendResult(boolean setAsEmpty) {
        Intent result = new Intent();
        Double quantity;
        if (setAsEmpty) {
            quantity = 0.0;
        } else {
            quantity = Double.parseDouble(numberPickerValues.get(numberPicker.getValue()));
        }

        result.putExtra(RESULT_ID, foodTypeId);
        result.putExtra(RESULT_QUANTITY, quantity);

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
    }

    private String stringValue(Double d) {
        if (d - d.intValue() == 0) {
            return String.valueOf(d.intValue());
        } else {
            return String.valueOf(d);
        }
    }

    /**
     * Generiert eine String-ArrayList mit den angegebenen Parametern und fügt immer ein Vielfaches
     * der Packungsgröße ein, wenn der eigentliche Wert nur 10% davon abweicht
     *
     * @return ArrayList mit den gegebenen Wert
     */
    private ArrayList<String> generateArray() {

        Double maxValue = commonPackSize * 15,
                step = 1.0;

        ArrayList<Double> list = new ArrayList<>();

        if (quantity * 2 > maxValue) {
            maxValue = quantity * 2;
        }

        switch (quantityType) {
            case "count":
                step = Math.ceil(commonPackSize * 0.1);
                break;
            case "weight":
                step = Math.ceil(commonPackSize * 0.2);
                break;
            case "volume":
                step = commonPackSize * 0.5;
                break;
            case "pack":
                step = Math.ceil(commonPackSize * 0.2);
                break;
            case "can":
                step = commonPackSize * 0.5;
                break;
        }

        int divider = 10;
        while (commonPackSize % step != 0) {
            step = commonPackSize / divider;
            divider--;
        }

        int count = ((Double) (maxValue / step)).intValue();
        for (int i = 0; i <= count; i++) {
            Double value = i * step;
            list.add(value);
        }

        if (!list.contains(quantity)) {
            list.add(quantity);
        }

        Collections.sort(list);
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(stringValue(list.get(i)));
        }
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(STATE_QUANTITY, numberPicker.getValue());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
