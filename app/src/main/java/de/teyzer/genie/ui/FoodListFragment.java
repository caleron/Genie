package de.teyzer.genie.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.teyzer.genie.R;
import de.teyzer.genie.data.DataManager;
import de.teyzer.genie.data.DataProvider;
import de.teyzer.genie.model.FoodType;
import de.teyzer.genie.model.Product;
import de.teyzer.genie.scanner.IntentIntegrator;
import de.teyzer.genie.scanner.IntentResult;

public class FoodListFragment extends Fragment {
    public static final String FRAGMENT_TAG = "food_list";

    public static final int REQUEST_NEW_FOOD_TYPE = 0;
    public static final int REQUEST_EDIT_FOOD_TYPE = 1;
    public static final int REQUEST_EDIT_FOOD_QUANTITY = 2;
    public static final int REQUEST_NEW_PRODUCT = 3;
    public static final int REQUEST_EDIT_PRODUCT = 4;

    @Bind(R.id.food_list)
    RecyclerView foodList;
    @Bind(R.id.food_list_fab)
    FloatingActionButton foodListFab;

    private DataProvider mListener;
    private FoodAdapter mAdapter;
    private FoodListFragment foodListFragment;

    private boolean nextScanForEdit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.food_list_fragment, container, false);

        ButterKnife.bind(this, root);

        //Liste initialisieren
        RecyclerView.LayoutManager mListLayoutManager = new LinearLayoutManager(getActivity());
        foodList.setLayoutManager(mListLayoutManager);

        mAdapter = new FoodAdapter();
        foodList.setAdapter(mAdapter);

        //damit der Adapter darauf zugreifen kann
        foodListFragment = this;

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //Wird einmal beim laden der Activity ausgeführt
        inflater.inflate(R.menu.food_list, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DataProvider) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DataProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Wird beim Klick auf ein Button in der Actionbar ausgelöst
        int id = item.getItemId();

        if (id == R.id.food_list_menu_add_food_type) {
            Intent intent = new Intent(getActivity(), NewFoodTypeActivity.class);
            intent.putExtra(NewFoodTypeActivity.REQUEST_ACTION, NewFoodTypeActivity.REQUEST_ACTION_NEW);

            startActivityForResult(intent, REQUEST_NEW_FOOD_TYPE);
        } else if (id == R.id.food_list_menu_edit_product) {
            startCaptureMode(true);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Startet den Kamera-Barcode-Scan-Modus
     *
     * @param editMode Ob das nächste gescannte Produkt bearbeitet werden soll
     */
    private void startCaptureMode(boolean editMode) {
        //barcode reader starten
        nextScanForEdit = editMode;

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Wird ausgelöst, wenn eine via startActivityForResult gestartete Activity fertig ist

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            handleScanResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_EDIT_FOOD_TYPE || requestCode == REQUEST_NEW_FOOD_TYPE) {
            handleFoodTypeResult(requestCode, data);
        } else if (requestCode == REQUEST_EDIT_FOOD_QUANTITY) {
            handleFoodQuantityResult(data);
        } else if (requestCode == REQUEST_NEW_PRODUCT) {
            handleProductResult(requestCode, data);
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Verarbeitet das Scanergebnis, also fügt
     *
     * @param requestCode AnfrageCode
     * @param resultCode  Ergebniscode
     * @param data        Ergebnis
     */
    private void handleScanResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            DataManager dataManager = mListener.getDataManager();

            if (nextScanForEdit) {
                Product product = dataManager.getProductWithBarcode(scanResult.getContents());
                if (product == null) {
                    Toast.makeText(getActivity(), "Produkt nicht vorhanden", Toast.LENGTH_LONG);
                } else {
                    //Aktivität zum bearbeiten starten
                    Intent intent = new Intent(getActivity(), NewProductActivity.class);
                    intent.putExtra(NewProductActivity.REQUEST_ACTION, NewProductActivity.REQUEST_ACTION_EDIT);
                    intent.putExtra(NewProductActivity.PARAM_BARCODE, scanResult.getContents());

                    FoodType[] foodTypes = mListener.getDataManager().getFoodTypeArray();
                    intent.putExtra(NewProductActivity.PARAM_FOOD_TYPES, foodTypes);
                    intent.putExtra(NewProductActivity.PARAM_PRODUCT, product);

                    startActivityForResult(intent, REQUEST_EDIT_PRODUCT);
                }
                nextScanForEdit = false;
                return;
            }

            FoodType scannedFoodType = dataManager.productScanned(scanResult.getContents(), getActivity());
            if (scannedFoodType == null) {
                //Produkt nicht vorhanden
                //Aktivität zum erstellen starten
                Intent intent = new Intent(getActivity(), NewProductActivity.class);
                intent.putExtra(NewProductActivity.REQUEST_ACTION, NewProductActivity.REQUEST_ACTION_NEW);
                intent.putExtra(NewProductActivity.PARAM_BARCODE, scanResult.getContents());

                FoodType[] foodTypes = mListener.getDataManager().getFoodTypeArray();
                intent.putExtra(NewProductActivity.PARAM_FOOD_TYPES, foodTypes);

                startActivityForResult(intent, REQUEST_NEW_PRODUCT);
            } else {
                //Produkt vorhanden und erfolgreich hinzugefügt
                String alertText = scannedFoodType.getLastAddedQuantityString()
                        + " " + scannedFoodType.getName() + " hinzugefügt";

                Toast.makeText(getActivity(), alertText, Toast.LENGTH_SHORT).show();
                //nächstes Scannen starten
                startCaptureMode(false);
            }
        }
    }

    private void handleFoodTypeResult(int requestCode, Intent data) {

        DataManager manager = mListener.getDataManager();
        String name = data.getStringExtra(NewFoodTypeActivity.RESULT_NAME);
        Double commonPackSize = data.getDoubleExtra(NewFoodTypeActivity.RESULT_PACK_SIZE, 1.0);
        String preferredMeal = data.getStringExtra(NewFoodTypeActivity.RESULT_PREFERRED_MEAL);
        String quantityType = data.getStringExtra(NewFoodTypeActivity.RESULT_QUANTITY_TYPE);
        Double currentQuantity = data.getDoubleExtra(NewFoodTypeActivity.RESULT_CURRENT_QUANTITY, 1.0);

        if (requestCode == REQUEST_NEW_FOOD_TYPE) {
            manager.addFoodType(name, "", currentQuantity, quantityType, preferredMeal, commonPackSize);
        } else {
            int id = data.getIntExtra(NewFoodTypeActivity.RESULT_ID, -1);
            manager.updateFoodType(id, name, "", currentQuantity, quantityType, preferredMeal, commonPackSize);
        }
    }

    private void handleFoodQuantityResult(Intent data) {
        int id = data.getIntExtra(EditQuantityDialogFragment.RESULT_ID, -1);

        Double quantity = data.getDoubleExtra(EditQuantityDialogFragment.RESULT_QUANTITY, 0.0);
        DataManager dataManager = mListener.getDataManager();
        dataManager.updateFoodQuantity(id, quantity);
    }

    private void handleProductResult(int requestCode, Intent data) {
        DataManager manager = mListener.getDataManager();
        String name = data.getStringExtra(NewProductActivity.RESULT_NAME);
        String store = data.getStringExtra(NewProductActivity.RESULT_STORE);
        Double packSize = data.getDoubleExtra(NewProductActivity.RESULT_PACK_SIZE, 0.0);
        FoodType foodType = data.getParcelableExtra(NewProductActivity.RESULT_FOOD_TYPE);
        String barcode = data.getStringExtra(NewProductActivity.RESULT_BARCODE);

        if (requestCode == REQUEST_NEW_PRODUCT) {
            Product product = manager.addProduct(name, store, foodType, packSize, barcode);

            mListener.getDataManager().addProductToQuantity(product);

            String alertText = FoodType.getQuantityString(product.getQuantity())
                    + " " + product.getFoodType().getQuantityTypeString(getActivity())
                    + " " + product.getFoodType().getName() + " hinzugefügt";

            Toast.makeText(getActivity(), alertText, Toast.LENGTH_SHORT).show();

            startCaptureMode(false);
        } else {
            int id = data.getIntExtra(NewProductActivity.RESULT_ID, 0);
            manager.updateProduct(id, name, store, foodType, packSize, barcode);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.food_list_fab)
    public void onClick() {
        startCaptureMode(false);
    }

    /**
     * Adapter zum Darstellen der Nahrungsmitteltypen in der Liste
     */
    private class FoodAdapter extends RecyclerView.Adapter<ViewHolder> {
        DataManager dataManager;

        /**
         * Erstellt einen neuen FoodAdapter und holt den Datenmanager von der Activity
         */
        public FoodAdapter() {
            dataManager = mListener.getDataManager();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Wird so oft ausgeführt, wie ViewHolder auf den Bildschirm passen
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_list_item, parent, false);
            return new ViewHolder(v, dataManager);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //Bindet einen neuen Nahrungsmitteltyp an einen bestehenden ViewHolder
            FoodType food = dataManager.getFoodTypes().valueAt(position);
            holder.bindFood(food);
        }

        @Override
        public int getItemCount() {
            return dataManager.getFoodTypes().size();
        }
    }

    /**
     * Verwaltet einen Eintrag in der RecyclerView
     */
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View itemView;
        public TextView titleText;
        public Button addCommonPackButton;
        private FoodType foodType;
        private DataManager dataManager;

        /**
         * Erstellt einen neuen ViewHolder, der die Views einer Zeile in der RecyclerView verwaltet
         * und für Events darauf zuständig ist
         *
         * @param itemView    Die View der Zeile
         * @param dataManager Der Datenmanager, um auf Events reagieren zu können
         */
        public ViewHolder(View itemView, DataManager dataManager) {
            super(itemView);

            titleText = (TextView) itemView.findViewById(R.id.titleText);
            addCommonPackButton = (Button) itemView.findViewById(R.id.add_common_pack_button);
            this.itemView = itemView;
            this.dataManager = dataManager;

            titleText.setOnClickListener(this);
            titleText.setOnLongClickListener(this);
            addCommonPackButton.setOnClickListener(this);
        }

        /**
         * Atualisiert die View
         *
         * @param foodType Der Nahrungsmitteltyp, der repräsentiert werden soll
         */
        public void bindFood(FoodType foodType) {
            this.foodType = foodType;

            String quantityType = foodType.getQuantityType();
            String unit = foodType.getQuantityTypeString(getActivity());

            Double quantity = foodType.getQuantity();
            if (quantity <= 0) {
                titleText.setText(foodType.getName() + " (leer)");
            } else {
                if (quantityType.equals("count")) {
                    //Essen wird als Anzahl gemessen
                    titleText.setText(foodType.getQuantityString() + " " + foodType.getName());
                } else {
                    titleText.setText(foodType.getName() + " " + foodType.getQuantityString() + " " + unit);
                }
            }
        }

        /**
         * Wird beim Klick auf TitleTextView oder Button ausgelöst
         *
         * @param v Button oder TitleTextView
         */
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.add_common_pack_button) {
                dataManager.addCommonPackSizeToFoodQuantity(foodType.getId());
                mAdapter.notifyDataSetChanged();
            } else if (id == R.id.titleText) {
                EditQuantityDialogFragment dialog = new EditQuantityDialogFragment();
                dialog.setTargetFragment(foodListFragment, REQUEST_EDIT_FOOD_QUANTITY);

                Bundle args = new Bundle();
                args.putInt(EditQuantityDialogFragment.STATE_FOOD_TYPE_ID, foodType.getId());
                args.putString(EditQuantityDialogFragment.STATE_NAME, foodType.getName());
                args.putString(EditQuantityDialogFragment.STATE_UNIT, foodType.getQuantityTypeString(getActivity()));
                args.putDouble(EditQuantityDialogFragment.STATE_QUANTITY, foodType.getQuantity());
                args.putString(EditQuantityDialogFragment.STATE_QUANTITY_TYPE, foodType.getQuantityType());
                args.putDouble(EditQuantityDialogFragment.STATE_COMMON_PACK_SIZE, foodType.getCommonPackSize());

                dialog.setArguments(args);
                dialog.show(getFragmentManager(), EditQuantityDialogFragment.FRAGMENT_TAG);
            }
        }

        /**
         * Wird beim gedrückt halten des Eintrags ausgelöst und öffnet den Nahrungsmitteltyp zum bearbeiten
         *
         * @param v Die View
         * @return true, wenn das Event abgefangen wurde
         */
        @Override
        public boolean onLongClick(View v) {
            Intent intent = new Intent(getActivity(), NewFoodTypeActivity.class);

            intent.putExtra(NewFoodTypeActivity.REQUEST_ACTION, NewFoodTypeActivity.REQUEST_ACTION_EDIT);
            intent.putExtra(NewFoodTypeActivity.RESULT_ID, foodType.getId());
            intent.putExtra(NewFoodTypeActivity.RESULT_NAME, foodType.getName());
            intent.putExtra(NewFoodTypeActivity.RESULT_PACK_SIZE, foodType.getCommonPackSize());
            intent.putExtra(NewFoodTypeActivity.RESULT_PREFERRED_MEAL, foodType.getPreferredMeal());
            intent.putExtra(NewFoodTypeActivity.RESULT_QUANTITY_TYPE, foodType.getQuantityType());
            intent.putExtra(NewFoodTypeActivity.RESULT_CURRENT_QUANTITY, foodType.getQuantity());

            startActivityForResult(intent, REQUEST_EDIT_FOOD_TYPE);
            return true;
        }
    }
}
