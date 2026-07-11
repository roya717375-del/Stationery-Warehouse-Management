package DAO;

import model.Warehouse;
import util.IdGenerator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAOImpl implements WarehouseDAO {
    private Connection connection;

    public WarehouseDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized void addWarehouse(Warehouse warehouse) throws SQLException {
        if (warehouse.getName() == null || warehouse.getName().trim().isEmpty()) {
            throw new SQLException("Warehouse name cannot be empty");
        }
        if (warehouse.getCapacity() <= 0) {
            throw new SQLException("Capacity must be greater than 0");
        }

        String checkSql = "SELECT COUNT(*) FROM Warehouses WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setString(1, warehouse.getName());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Warehouse name already exists: " + warehouse.getName());
            }
        }

        if (warehouse.getWarehouseId() == null || warehouse.getWarehouseId().isEmpty()) {
            warehouse.setWarehouseId(IdGenerator.generateWarehouseId());
        }

        String address = warehouse.getAddress();
        if (address == null) address = "";

        String sql = "INSERT INTO Warehouses (warehouse_id, name, address, capacity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouse.getWarehouseId());
            pstmt.setString(2, warehouse.getName());
            pstmt.setString(3, address);
            pstmt.setInt(4, warehouse.getCapacity());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized Warehouse getWarehouseById(String warehouseId) throws SQLException {
        String sql = "SELECT * FROM Warehouses WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Warehouse warehouse = new Warehouse(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getInt("capacity")
                );
                warehouse.setWarehouseId(rs.getString("warehouse_id"));
                return warehouse;
            }
        }
        return null;
    }



    @Override
    public synchronized List<Warehouse> getAllWarehouses() throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT * FROM Warehouses ORDER BY warehouse_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Warehouse warehouse = new Warehouse(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getInt("capacity")
                );
                warehouse.setWarehouseId(rs.getString("warehouse_id"));
                warehouses.add(warehouse);
            }
        }
        return warehouses;
    }

    @Override
    public synchronized void updateWarehouse(Warehouse warehouse) throws SQLException {
        if (warehouse.getName() == null || warehouse.getName().trim().isEmpty()) {
            throw new SQLException("Warehouse name cannot be empty");
        }
        if (warehouse.getCapacity() <= 0) {
            throw new SQLException("Capacity must be greater than 0");
        }

        String checkSql = "SELECT COUNT(*) FROM Warehouses WHERE name = ? AND warehouse_id != ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setString(1, warehouse.getName());
            pstmt.setString(2, warehouse.getWarehouseId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Warehouse name already exists: " + warehouse.getName());
            }
        }

        String sql = "UPDATE Warehouses SET name = ?, address = ?, capacity = ? WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouse.getName());
            pstmt.setString(2, warehouse.getAddress());
            pstmt.setInt(3, warehouse.getCapacity());
            pstmt.setString(4, warehouse.getWarehouseId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized void deleteWarehouse(String warehouseId) throws SQLException {
        String checkInventorySql = "SELECT COUNT(*) FROM Inventory WHERE warehouse_id = ? AND (real_stock != 0 OR reserved_stock != 0 OR incoming_stock != 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(checkInventorySql)) {
            pstmt.setString(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Warehouse has non-zero inventory! Cannot delete.");
            }
        }

        String deleteZeroInventorySql = "DELETE FROM Inventory WHERE warehouse_id = ? AND real_stock = 0 AND reserved_stock = 0 AND incoming_stock = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteZeroInventorySql)) {
            pstmt.setString(1, warehouseId);
            pstmt.executeUpdate();
        }

        String getPermissionsSql = "SELECT permission_id FROM Permissions WHERE warehouse_id = ?";
        List<String> permissionIds = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(getPermissionsSql)) {
            pstmt.setString(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                permissionIds.add(rs.getString("permission_id"));
            }
        }

        for (String permissionId : permissionIds) {
            String deleteTransSql = "DELETE FROM FinancialTransactions WHERE permission_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteTransSql)) {
                pstmt.setString(1, permissionId);
                pstmt.executeUpdate();
            }
        }

        String deletePermSql = "DELETE FROM Permissions WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deletePermSql)) {
            pstmt.setString(1, warehouseId);
            pstmt.executeUpdate();
        }

        String sql = "DELETE FROM Warehouses WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized List<Warehouse> searchWarehouses(String id, String name, String address, String capacity) throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Warehouses WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (id != null && !id.trim().isEmpty()) {
            sql.append(" AND warehouse_id LIKE ?");
            params.add("%" + id.trim() + "%");
        }
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        if (address != null && !address.trim().isEmpty()) {
            sql.append(" AND address LIKE ?");
            params.add("%" + address.trim() + "%");
        }
        if (capacity != null && !capacity.trim().isEmpty()) {
            try {
                int cap = Integer.parseInt(capacity.trim());
                sql.append(" AND capacity = ?");
                params.add(String.valueOf(cap));
            } catch (NumberFormatException e) {
            }
        }

        sql.append(" ORDER BY warehouse_id");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Warehouse warehouse = new Warehouse(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getInt("capacity")
                );
                warehouse.setWarehouseId(rs.getString("warehouse_id"));
                warehouses.add(warehouse);
            }
        }
        return warehouses;
    }
}