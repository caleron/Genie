package de.teyzer.genie.model;

import java.io.Serializable;

/**
 * Stellt ein konkretes Produkt dar, etwa eine 12er Packung Eier von Penny
 */
public class Product implements Serializable {
    private int id;
    private String name;
    private String store;
    private FoodType foodType;
    private Double quantity;
    private String barCode;

    public Product(int id, String name, String store, FoodType foodType, Double quantity, String barCode) {
        this.id = id;
        updateProduct(name, store, foodType, quantity, barCode);
    }

    public void updateProduct(String name, String store, FoodType foodType, Double quantity, String barCode) {
        this.name = name;
        this.foodType = foodType;
        this.quantity = quantity;
        this.barCode = barCode;
        this.store = store;
    }


    public Double getQuantity() {
        return quantity;
    }

    public String getQuantityString() {
        if (quantity.intValue() - quantity == 0) {
            return String.valueOf(quantity.intValue());
        } else {
            return String.valueOf(quantity);
        }
    }

    public FoodType getFoodType() {
        return foodType;
    }

    public String getStore() {
        return store;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

}
