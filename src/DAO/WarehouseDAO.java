package DAO;

import model.Warehouse;
import java.sql.SQLException;
import java.util.List;

public interface WarehouseDAO {
    void addWarehouse(Warehouse warehouse) throws SQLException;
    Warehouse getWarehouseById(String warehouseId) throws SQLException;
    List<Warehouse> getAllWarehouses() throws SQLException;
    void updateWarehouse(Warehouse warehouse) throws SQLException;
    void deleteWarehouse(String warehouseId) throws SQLException;
    List<Warehouse> searchWarehouses(String id, String name, String address, String capacity) throws SQLException;
}