package model;
import util.IdGenerator;
import java.time.LocalDate;
import java.util.List;

public class Warehouse {
    private String warehouseId;
    private String name;
    private String address;
    private int capacity;



    public Warehouse(String name, String address, int capacity) {
        this.warehouseId = IdGenerator.generateWarehouseId();
        this.name = name;
        this.address = address;
        this.capacity = capacity;

    }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return "Warehouse{" +
                "warehouseId='" + warehouseId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}