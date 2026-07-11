package model;
public class Inventory {
    private String warehouseId;
    private String itemId;
    private int realStock;
    private int reservedStock;
    private int incomingStock;
    private String lastUpdated;

    public Inventory() {}

    public Inventory(String warehouseId, String itemId) {
        this.warehouseId = warehouseId;
        this.itemId = itemId;
        this.realStock = 0;
        this.reservedStock = 0;
        this.incomingStock = 0;
    }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public int getRealStock() { return realStock; }
    public void setRealStock(int realStock) { this.realStock = realStock; }
    public int getReservedStock() { return reservedStock; }
    public void setReservedStock(int reservedStock) { this.reservedStock = reservedStock; }
    public int getIncomingStock() { return incomingStock; }
    public void setIncomingStock(int incomingStock) { this.incomingStock = incomingStock; }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getAvailableStock() {
        return realStock - reservedStock;
    }

    public int getTotalStock() {
        return realStock + incomingStock;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "warehouseId='" + warehouseId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", realStock=" + realStock +
                ", reservedStock=" + reservedStock +
                ", incomingStock=" + incomingStock +
                ", lastupdated=" + lastUpdated +
                '}';
    }
}