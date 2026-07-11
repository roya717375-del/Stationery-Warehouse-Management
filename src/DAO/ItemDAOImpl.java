package DAO;

import model.Item;
import util.IdGenerator;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl implements ItemDAO {
    private Connection connection;
    private CategoryDAO categoryDAO;

    public ItemDAOImpl(Connection connection) {
        this.connection = connection;
        this.categoryDAO = new CategoryDAOImpl(connection);
    }

    @Override
    public synchronized void addItem(Item item) throws SQLException {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new SQLException("Item name cannot be empty");
        }
        if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
            throw new SQLException("Item code cannot be empty");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Unit price must be greater than 0");
        }
        if (item.getCategoryId() == null || item.getCategoryId().trim().isEmpty()) {
            throw new SQLException("Category ID cannot be empty");
        }
        if (!categoryDAO.categoryExists(item.getCategoryId())) {
            throw new SQLException("Category does not exist: " + item.getCategoryId());
        }

        String checkCodeSql = "SELECT COUNT(*) FROM Items WHERE item_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkCodeSql)) {
            pstmt.setString(1, item.getItemCode());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Item code already exists: " + item.getItemCode());
            }
        }

        if (item.getItemId() == null || item.getItemId().isEmpty()) {
            item.setItemId(IdGenerator.generateItemId());
        }

        String sql = "INSERT INTO Items (item_id, item_code, name, description, category_id, unit_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemId());
            pstmt.setString(2, item.getItemCode());
            pstmt.setString(3, item.getName());
            pstmt.setString(4, item.getDescription());
            pstmt.setString(5, item.getCategoryId());
            pstmt.setBigDecimal(6, item.getUnitPrice());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized Item getItemById(String itemId) throws SQLException {
        String sql = "SELECT * FROM Items WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToItem(rs);
            }
        }
        return null;
    }

    @Override
    public synchronized List<Item> getAllItems() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM Items ORDER BY item_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    @Override
    public synchronized List<Item> getItemsByCategory(String categoryId) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM Items WHERE category_id = ? ORDER BY item_id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    @Override
    public synchronized void updateItem(Item item) throws SQLException {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new SQLException("Item name cannot be empty");
        }
        if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
            throw new SQLException("Item code cannot be empty");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Unit price must be greater than 0");
        }
        if (item.getCategoryId() == null || item.getCategoryId().trim().isEmpty()) {
            throw new SQLException("Category ID cannot be empty");
        }
        if (!categoryDAO.categoryExists(item.getCategoryId())) {
            throw new SQLException("Category does not exist: " + item.getCategoryId());
        }

        String checkCodeSql = "SELECT COUNT(*) FROM Items WHERE item_code = ? AND item_id != ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkCodeSql)) {
            pstmt.setString(1, item.getItemCode());
            pstmt.setString(2, item.getItemId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Item code already exists: " + item.getItemCode());
            }
        }

        String sql = "UPDATE Items SET item_code = ?, name = ?, description = ?, category_id = ?, unit_price = ? WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemCode());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getCategoryId());
            pstmt.setBigDecimal(5, item.getUnitPrice());
            pstmt.setString(6, item.getItemId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized void deleteItem(String itemId) throws SQLException {
        String checkInventorySql = "SELECT COUNT(*) FROM Inventory WHERE item_id = ? AND (real_stock != 0 OR reserved_stock != 0 OR incoming_stock != 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(checkInventorySql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Item has non-zero inventory! Cannot delete.");
            }
        }

        String checkPermissionSql = "SELECT COUNT(*) FROM Permissions WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkPermissionSql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Item has permissions! Cannot delete.");
            }
        }

        String deleteZeroInventorySql = "DELETE FROM Inventory WHERE item_id = ? AND real_stock = 0 AND reserved_stock = 0 AND incoming_stock = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteZeroInventorySql)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        }

        String sql = "DELETE FROM Items WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized boolean itemCodeExists(String itemCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Items WHERE item_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public synchronized List<Item> searchItems(String id, String code, String name, String price) throws SQLException {
        List<Item> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Items WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (id != null && !id.trim().isEmpty()) {
            sql.append(" AND item_id LIKE ?");
            params.add("%" + id.trim() + "%");
        }
        if (code != null && !code.trim().isEmpty()) {
            sql.append(" AND item_code LIKE ?");
            params.add("%" + code.trim() + "%");
        }
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        if (price != null && !price.trim().isEmpty()) {
            try {
                int p = Integer.parseInt(price.trim());
                sql.append(" AND unit_price = ?");
                params.add(String.valueOf(p));
            } catch (NumberFormatException e) {
            }
        }

        sql.append(" ORDER BY item_id");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = new Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getString("category_id"),
                        rs.getBigDecimal("unit_price")
                );
                item.setItemId(rs.getString("item_id"));
                item.setDescription(rs.getString("description"));
                items.add(item);
            }
        }
        return items;
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item(
                rs.getString("item_code"),
                rs.getString("name"),
                rs.getString("category_id"),
                rs.getBigDecimal("unit_price")
        );
        item.setItemId(rs.getString("item_id"));
        item.setDescription(rs.getString("description"));
        return item;
    }
}