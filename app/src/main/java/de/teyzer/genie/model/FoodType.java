package de.teyzer.genie.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.List;

import de.teyzer.genie.R;

/**
 * Stellt ein Nahrungsmitteltyp dar, etwa Eier oder Mehl
 */
public class FoodType implements Parcelable {
    public static final List<String> QUANTITY_TYPES;
    public static final List<String> PREFERRED_MEALS;

    static {
        QUANTITY_TYPES = Arrays.asList("count", "weight", "volume", "pack", "can");
        PREFERRED_MEALS = Arrays.asList("breakfast", "lunch", "dinner", "snack", "other");
    }

    private final int id;
    private String name;
    /**
     * Etwa Kühl- oder Tiefkühlware
     */
    private String category;
    /**
     * Anzahl Einheiten vom Produkt
     */
    private Double quantity;
    /**
     * Einheit, in der die Anzahl gemessen wird
     */
    private String quantityType;
    /**
     * Mahlzeit, zu der der Nahrungsmitteltyp bevorzugt gegessen wird (etwa Nutella zum Frühstück)
     */
    private String preferredMeal;

    /**
     * Übliche Verpackungsgröße
     */
    private Double commonPackSize;

    private String lastAddedQuantityString;

    public FoodType(int id, String name, String category, Double quantity, String quantityType, String preferredMeal, Double commonPackSize) {
        this.id = id;
        updateData(name, category, quantity, quantityType, preferredMeal, commonPackSize);
    }

    private FoodType(Parcel parcel) {
        this.id = parcel.readInt();
        this.name = parcel.readString();
        this.category = parcel.readString();
        this.quantity = parcel.readDouble();
        this.preferredMeal = parcel.readString();
        this.quantityType = parcel.readString();
        this.commonPackSize = parcel.readDouble();
    }

    public void updateData(String name, String category, Double quantity, String quantityType, String preferredMeal, Double commonPackSize) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.preferredMeal = preferredMeal;
        this.quantityType = quantityType;
        this.commonPackSize = commonPackSize;
    }

    public double getCommonPackSize() {
        return commonPackSize;
    }

    public void addCommonPackSize() {
        setQuantity(getQuantity() + getCommonPackSize());
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getQuantityWithUnitString(Context context) {
        return getQuantityString() + " " + getQuantityTypeString(context);
    }

    public void setLastAddedQuantity(double quantity, Context context) {
        lastAddedQuantityString = getQuantityString(quantity) + " " + getQuantityTypeString(context);
    }

    public String getLastAddedQuantityString() {
        return lastAddedQuantityString;
    }

    public String getQuantityString() {
        return getQuantityString(quantity);
    }

    public static String getQuantityString(Double quantity) {
        if (quantity.intValue() - quantity == 0) {
            return String.valueOf(quantity.intValue());
        } else {
            return String.valueOf(quantity);
        }
    }

    public String getQuantityType() {
        return quantityType;
    }

    public String getQuantityTypeString(Context context) {
        String[] units = context.getResources().getStringArray(R.array.quantity_units);
        int position = QUANTITY_TYPES.indexOf(getQuantityType());
        return units[position];
    }

    public String getPreferredMeal() {
        return preferredMeal;
    }

    public String getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeDouble(quantity);
        dest.writeString(preferredMeal);
        dest.writeString(quantityType);
        dest.writeDouble(commonPackSize);
    }

    public static final Parcelable.Creator<FoodType> CREATOR =
            new Parcelable.Creator<FoodType>() {

                @Override
                public FoodType createFromParcel(Parcel source) {
                    return new FoodType(source);
                }

                @Override
                public FoodType[] newArray(int size) {
                    return new FoodType[size];
                }
            };
}
