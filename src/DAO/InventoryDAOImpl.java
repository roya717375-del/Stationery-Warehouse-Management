package DAO;

import model.Inventory;
import model.Item;
import model.Warehouse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAOImpl implements InventoryDAO {
    private Connection connection;

    public InventoryDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized void addInventory(Inventory inventory) throws SQLException {
        if (inventory.getWarehouseId() == null || inventory.getWarehouseId().trim().isEmpty()) {
            throw new SQLException("Warehouse ID cannot be empty");
        }
        if (inventory.getItemId() == null || inventory.getItemId().trim().isEmpty()) {
            throw new SQLException("Item ID cannot be empty");
        }

        String checkSql = "SELECT COUNT(*) FROM Inventory WHERE warehouse_id = ? AND item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setString(1, inventory.getWarehouseId());
            pstmt.setString(2, inventory.getItemId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Inventory already exists for this warehouse and item!");
            }
        }

        String sql = "INSERT INTO Inventory (warehouse_id, item_id, real_stock, reserved_stock, incoming_stock) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, inventory.getWarehouseId());
            pstmt.setString(2, inventory.getItemId());
            pstmt.setInt(3, inventory.getRealStock());
            pstmt.setInt(4, inventory.getReservedStock());
            pstmt.setInt(5, inventory.getIncomingStock());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized Inventory getInventory(String warehouseId, String itemId) throws SQLException {
        String sql = "SELECT * FROM Inventory WHERE warehouse_id = ? AND item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            pstmt.setString(2, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToInventory(rs);
            }
        }
        return null;
    }

    @Override
    public synchronized List<Inventory> getInventoryByWarehouse(String warehouseId) throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT * FROM Inventory WHERE warehouse_id = ? ORDER BY item_id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                inventories.add(mapResultSetToInventory(rs));
            }
        }
        return inventories;
    }

    @Override
    public synchronized List<Inventory> getInventoryByItem(String itemId) throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT * FROM Inventory WHERE item_id = ? ORDER BY warehouse_id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                inventories.add(mapResultSetToInventory(rs));
            }
        }
        return inventories;
    }

    @Override
    public synchronized List<Inventory> getAllInventories() throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT * FROM Inventory ORDER BY warehouse_id, item_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                inventories.add(mapResultSetToInventory(rs));
            }
        }
        return inventories;
    }

    @Override
    public synchronized void updateInventory(Inventory inventory) throws SQLException {
        String sql = "UPDATE Inventory SET real_stock = ?, reserved_stock = ?, incoming_stock = ?, last_updated = CURRENT_DATE WHERE warehouse_id = ? AND item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, inventory.getRealStock());
            pstmt.setInt(2, inventory.getReservedStock());
            pstmt.setInt(3, inventory.getIncomingStock());
            pstmt.setString(4, inventory.getWarehouseId());
            pstmt.setString(5, inventory.getItemId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized void deleteInventory(String warehouseId, String itemId) throws SQLException {
        String sql = "DELETE FROM Inventory WHERE warehouse_id = ? AND item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            pstmt.setString(2, itemId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized boolean inventoryExists(String warehouseId, String itemId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Inventory WHERE warehouse_id = ? AND item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            pstmt.setString(2, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public synchronized Inventory getInventoryByWarehouseAndItem(String warehouseId, String itemId) throws SQLException {
        String sql = "SELECT * FROM Inventory WHERE warehouse_id = ? AND item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            pstmt.setString(2, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToInventory(rs);
            }
        }
        return null;
    }

    @Override
    public synchronized List<Inventory> searchInventory(String itemId, String itemName, String realStock, String reserved, String incoming) throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT i.* FROM Inventory i ");
        sql.append("JOIN Items it ON i.item_id = it.item_id ");
        sql.append("WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (itemId != null && !itemId.trim().isEmpty()) {
            sql.append(" AND i.item_id LIKE ?");
            params.add("%" + itemId.trim() + "%");
        }
        if (itemName != null && !itemName.trim().isEmpty()) {
            sql.append(" AND it.name LIKE ?");
            params.add("%" + itemName.trim() + "%");
        }
        if (realStock != null && !realStock.trim().isEmpty()) {
            try {
                int rs = Integer.parseInt(realStock.trim());
                sql.append(" AND i.real_stock = ?");
                params.add(String.valueOf(rs));
            } catch (NumberFormatException e) {}
        }
        if (reserved != null && !reserved.trim().isEmpty()) {
            try {
                int rs = Integer.parseInt(reserved.trim());
                sql.append(" AND i.reserved_stock = ?");
                params.add(String.valueOf(rs));
            } catch (NumberFormatException e) {}
        }
        if (incoming != null && !incoming.trim().isEmpty()) {
            try {
                int inc = Integer.parseInt(incoming.trim());
                sql.append(" AND i.incoming_stock = ?");
                params.add(String.valueOf(inc));
            } catch (NumberFormatException e) {}
        }

        sql.append(" ORDER BY i.warehouse_id, i.item_id");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                inventories.add(mapResultSetToInventory(rs));
            }
        }
        return inventories;
    }

    private Inventory mapResultSetToInventory(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setWarehouseId(rs.getString("warehouse_id"));
        inventory.setItemId(rs.getString("item_id"));
        inventory.setRealStock(rs.getInt("real_stock"));
        inventory.setReservedStock(rs.getInt("reserved_stock"));
        inventory.setIncomingStock(rs.getInt("incoming_stock"));
        inventory.setLastUpdated(rs.getString("last_updated"));
        return inventory;
    }
}