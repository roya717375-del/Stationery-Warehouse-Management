package model;


import util.IdGenerator;

import java.math.BigDecimal;

public class Item {
    private String itemId;
    private String itemCode;
    private String name;
    private String description;
    private String categoryId;
    private BigDecimal unitPrice;

    public Item() {
    }

    public Item(String itemCode, String name, String categoryId, BigDecimal unitPrice) {
        this.itemId = IdGenerator.generateItemId();
        this.itemCode = itemCode;
        this.name = name;
        this.categoryId = categoryId;
        this.unitPrice = unitPrice;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }


    @Override
    public String toString() {
        return "Item{" +
                "itemId='" + itemId + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", unitPrice=" + unitPrice +
                '}';
    }
}