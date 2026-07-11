package DAO;

import model.Inventory;
import java.sql.SQLException;
import java.util.List;

public interface InventoryDAO {
    void addInventory(Inventory inventory) throws SQLException;
    Inventory getInventory(String warehouseId, String itemId) throws SQLException;
    List<Inventory> getInventoryByWarehouse(String warehouseId) throws SQLException;
    List<Inventory> getInventoryByItem(String itemId) throws SQLException;
    List<Inventory> getAllInventories() throws SQLException;
    void updateInventory(Inventory inventory) throws SQLException;
    void deleteInventory(String warehouseId, String itemId) throws SQLException;
    boolean inventoryExists(String warehouseId, String itemId) throws SQLException;
    Inventory getInventoryByWarehouseAndItem(String warehouseId, String itemId) throws SQLException;
    List<Inventory> searchInventory(String itemId, String itemName, String realStock, String reserved, String incoming) throws SQLException;
}