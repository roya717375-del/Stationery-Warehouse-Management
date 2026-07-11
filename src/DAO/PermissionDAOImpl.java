package DAO;

import model.*;
import util.IdGenerator;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PermissionDAOImpl implements PermissionDAO {
    private Connection connection;
    private InventoryDAO inventoryDAO;
    private FinancialTransactionDAO transactionDAO;
    private CashBalanceDAO cashBalanceDAO;

    public PermissionDAOImpl(Connection connection) {
        this.connection = connection;
        this.inventoryDAO = new InventoryDAOImpl(connection);
        this.transactionDAO = new FinancialTransactionDAOImpl(connection);
        this.cashBalanceDAO = new CashBalanceDAOImpl(connection);
    }

    @Override
    public synchronized void addPermission(Permission permission) throws SQLException {
        if (permission.getPermissionId() == null || permission.getPermissionId().isEmpty()) {
            permission.setPermissionId(IdGenerator.generatePermissionId());
        }

        String warehouseId = permission.getWarehouseId();
        String itemId = permission.getItemId();
        int quantity = permission.getQuantity();
        PermissionType type = permission.getType();

        if (type == PermissionType.IN) {
            String capSql = "SELECT capacity FROM Warehouses WHERE warehouse_id = ?";
            int capacity = 0;
            try (PreparedStatement pstmt = connection.prepareStatement(capSql)) {
                pstmt.setString(1, warehouseId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    capacity = rs.getInt("capacity");
                } else {
                    throw new SQLException("Warehouse not found!");
                }
            }

            String totalSql = "SELECT SUM(real_stock + incoming_stock) FROM Inventory WHERE warehouse_id = ?";
            int totalCurrentStock = 0;
            try (PreparedStatement pstmt = connection.prepareStatement(totalSql)) {
                pstmt.setString(1, warehouseId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totalCurrentStock = rs.getInt(1);
                }
            }

            int totalAfterAdd = totalCurrentStock + quantity;

            if (totalAfterAdd > capacity) {
                throw new SQLException(String.format(
                        "Warehouse capacity exceeded! Capacity: %d, Current: %d, New: %d",
                        capacity, totalCurrentStock, quantity
                ));
            }

        } else {
            String stockSql = "SELECT real_stock, reserved_stock FROM Inventory WHERE warehouse_id = ? AND item_id = ?";
            int realStock = 0;
            int reservedStock = 0;
            boolean exists = false;
            try (PreparedStatement pstmt = connection.prepareStatement(stockSql)) {
                pstmt.setString(1, warehouseId);
                pstmt.setString(2, itemId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    realStock = rs.getInt("real_stock");
                    reservedStock = rs.getInt("reserved_stock");
                    exists = true;
                }
            }

            if (!exists) {
                throw new SQLException("Item does not exist in this warehouse!");
            }

            int availableStock = realStock - reservedStock;

            if (availableStock < quantity) {
                throw new SQLException(String.format(
                        "Insufficient stock! Available: %d, Requested: %d",
                        availableStock, quantity
                ));
            }
        }

        String priceSql = "SELECT unit_price FROM Items WHERE item_id = ?";
        BigDecimal unitPrice = BigDecimal.ZERO;
        try (PreparedStatement pstmt = connection.prepareStatement(priceSql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                unitPrice = rs.getBigDecimal("unit_price");
            }
        }

        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (type == PermissionType.IN) {
            CashBalance cb = cashBalanceDAO.getCurrentBalance();
            if (cb.getBalance().compareTo(totalAmount) < 0) {
                throw new SQLException(String.format(
                        "Insufficient cash balance! Balance: %,d, Required: %,d",
                        cb.getBalance().longValue(),
                        totalAmount.longValue()
                ));
            }
        }

        Inventory inventory = inventoryDAO.getInventory(warehouseId, itemId);

        if (type == PermissionType.IN) {
            if (inventory == null) {
                inventory = new Inventory(warehouseId, itemId);
                inventory.setIncomingStock(quantity);
                inventoryDAO.addInventory(inventory);
            } else {
                inventory.setIncomingStock(inventory.getIncomingStock() + quantity);
                inventoryDAO.updateInventory(inventory);
            }
        } else {
            if (inventory == null) {
                throw new SQLException("Inventory not found for this item!");
            }
            inventory.setReservedStock(inventory.getReservedStock() + quantity);
            inventoryDAO.updateInventory(inventory);
        }

        String sql = "INSERT INTO Permissions (permission_id, type, warehouse_id, item_id, quantity, title, description, status, permission_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, permission.getPermissionId());
            pstmt.setString(2, permission.getType().name());
            pstmt.setString(3, permission.getWarehouseId());
            pstmt.setString(4, permission.getItemId());
            pstmt.setInt(5, permission.getQuantity());
            pstmt.setString(6, permission.getTitle());
            pstmt.setString(7, permission.getDescription());
            pstmt.setString(8, permission.getStatus().name());
            pstmt.setString(9, permission.getPermissionDate());
            pstmt.executeUpdate();
        }

        TransactionType transType = (type == PermissionType.IN) ? TransactionType.PURCHASE : TransactionType.SALE;
        String desc = (type == PermissionType.IN) ? "Purchase: " + permission.getTitle() : "Sale: " + permission.getTitle();

        FinancialTransaction transaction = new FinancialTransaction(
                permission.getPermissionId(),
                transType,
                totalAmount,
                desc
        );
        transactionDAO.addTransaction(transaction);
    }

    @Override
    public synchronized void updatePermission(Permission permission) throws SQLException {
        if (permission.getStatus() != PermissionStatus.ISSUED) {
            throw new SQLException("Only pending (ISSUED) permissions can be updated!");
        }

        String oldSql = "SELECT type, quantity, item_id FROM Permissions WHERE permission_id = ?";
        PermissionType oldType = null;
        int oldQuantity = 0;
        String oldItemId = null;
        try (PreparedStatement pstmt = connection.prepareStatement(oldSql)) {
            pstmt.setString(1, permission.getPermissionId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                oldType = PermissionType.valueOf(rs.getString("type"));
                oldQuantity = rs.getInt("quantity");
                oldItemId = rs.getString("item_id");
            }
        }

        if (permission.getType() == PermissionType.IN) {
            String capSql = "SELECT capacity FROM Warehouses WHERE warehouse_id = ?";
            int capacity = 0;
            try (PreparedStatement pstmt = connection.prepareStatement(capSql)) {
                pstmt.setString(1, permission.getWarehouseId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    capacity = rs.getInt("capacity");
                } else {
                    throw new SQLException("Warehouse not found!");
                }
            }

            String totalSql = "SELECT SUM(real_stock + incoming_stock) FROM Inventory WHERE warehouse_id = ?";
            int totalCurrentStock = 0;
            try (PreparedStatement pstmt = connection.prepareStatement(totalSql)) {
                pstmt.setString(1, permission.getWarehouseId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totalCurrentStock = rs.getInt(1);
                }
            }

            int totalAfterAdd = totalCurrentStock + permission.getQuantity();

            if (totalAfterAdd > capacity) {
                throw new SQLException(String.format(
                        "Warehouse capacity exceeded! Capacity: %d, Total: %d",
                        capacity, totalAfterAdd
                ));
            }
        }

        if (permission.getType() == PermissionType.OUT) {
            Inventory inventory = inventoryDAO.getInventory(permission.getWarehouseId(), permission.getItemId());
            if (inventory == null) {
                throw new SQLException("Item does not exist in this warehouse!");
            }

            int realStock = inventory.getRealStock();
            int reservedStock = inventory.getReservedStock();

            int availableStock = realStock - reservedStock;

            if (availableStock < permission.getQuantity()) {
                throw new SQLException(String.format(
                        "Insufficient stock! Available: %d, Requested: %d",
                        availableStock, permission.getQuantity()
                ));
            }
        }

        String priceSql = "SELECT unit_price FROM Items WHERE item_id = ?";
        BigDecimal unitPrice = BigDecimal.ZERO;
        try (PreparedStatement pstmt = connection.prepareStatement(priceSql)) {
            pstmt.setString(1, permission.getItemId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                unitPrice = rs.getBigDecimal("unit_price");
            }
        }

        String oldPriceSql = "SELECT unit_price FROM Items WHERE item_id = ?";
        BigDecimal oldUnitPrice = BigDecimal.ZERO;
        try (PreparedStatement pstmt = connection.prepareStatement(oldPriceSql)) {
            pstmt.setString(1, oldItemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                oldUnitPrice = rs.getBigDecimal("unit_price");
            }
        }

        BigDecimal newTotal = unitPrice.multiply(BigDecimal.valueOf(permission.getQuantity()));
        BigDecimal oldTotal = oldUnitPrice.multiply(BigDecimal.valueOf(oldQuantity));

        CashBalance cb = cashBalanceDAO.getCurrentBalance();
        BigDecimal currentBalance = cb.getBalance();

        if (oldType == PermissionType.IN && permission.getType() == PermissionType.IN) {
            if (newTotal.compareTo(oldTotal) > 0) {
                BigDecimal extraCost = newTotal.subtract(oldTotal);
                if (currentBalance.compareTo(extraCost) < 0) {
                    throw new SQLException(String.format(
                            "Insufficient cash balance! Balance: %,d, Required: %,d",
                            currentBalance.longValue(),
                            extraCost.longValue()
                    ));
                }
            }
        } else if (oldType == PermissionType.OUT && permission.getType() == PermissionType.IN) {
            if (currentBalance.compareTo(newTotal) < 0) {
                throw new SQLException(String.format(
                        "Insufficient cash balance for this purchase! Balance: %,d, Required: %,d",
                        currentBalance.longValue(),
                        newTotal.longValue()
                ));
            }
        }

        Inventory oldInventory = inventoryDAO.getInventory(permission.getWarehouseId(), permission.getItemId());
        if (oldInventory != null) {
            if (oldType == PermissionType.IN) {
                oldInventory.setIncomingStock(oldInventory.getIncomingStock() - oldQuantity);
            } else {
                oldInventory.setReservedStock(oldInventory.getReservedStock() - oldQuantity);
            }
            inventoryDAO.updateInventory(oldInventory);
        }

        Inventory newInventory = inventoryDAO.getInventory(permission.getWarehouseId(), permission.getItemId());

        if (permission.getType() == PermissionType.IN) {
            if (newInventory == null) {
                newInventory = new Inventory(permission.getWarehouseId(), permission.getItemId());
                newInventory.setIncomingStock(permission.getQuantity());
                inventoryDAO.addInventory(newInventory);
            } else {
                newInventory.setIncomingStock(newInventory.getIncomingStock() + permission.getQuantity());
                inventoryDAO.updateInventory(newInventory);
            }
        } else {
            if (newInventory == null) {
                throw new SQLException("Inventory not found for this item!");
            }
            newInventory.setReservedStock(newInventory.getReservedStock() + permission.getQuantity());
            inventoryDAO.updateInventory(newInventory);
        }

        String sql = "UPDATE Permissions SET type = ?, warehouse_id = ?, item_id = ?, quantity = ?, title = ?, description = ? WHERE permission_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, permission.getType().name());
            pstmt.setString(2, permission.getWarehouseId());
            pstmt.setString(3, permission.getItemId());
            pstmt.setInt(4, permission.getQuantity());
            pstmt.setString(5, permission.getTitle());
            pstmt.setString(6, permission.getDescription());
            pstmt.setString(7, permission.getPermissionId());
            pstmt.executeUpdate();
        }

        transactionDAO.deleteTransactionByPermissionId(permission.getPermissionId());

        TransactionType transType = (permission.getType() == PermissionType.IN) ? TransactionType.PURCHASE : TransactionType.SALE;
        String desc = (permission.getType() == PermissionType.IN) ? "Purchase: " + permission.getTitle() : "Sale: " + permission.getTitle();

        FinancialTransaction transaction = new FinancialTransaction(
                permission.getPermissionId(),
                transType,
                newTotal,
                desc
        );
        transactionDAO.addTransaction(transaction);
    }

    @Override
    public synchronized void deletePermission(String permissionId) throws SQLException {
        Permission permission = getPermissionById(permissionId);
        if (permission == null) {
            throw new SQLException("Permission not found!");
        }
        if (permission.getStatus() == PermissionStatus.DONE) {
            throw new SQLException("Cannot delete completed (DONE) permissions!");
        }

        Inventory inventory = inventoryDAO.getInventory(permission.getWarehouseId(), permission.getItemId());
        if (inventory != null) {
            if (permission.getType() == PermissionType.IN) {
                inventory.setIncomingStock(inventory.getIncomingStock() - permission.getQuantity());
            } else {
                inventory.setReservedStock(inventory.getReservedStock() - permission.getQuantity());
            }
            inventoryDAO.updateInventory(inventory);
        }

        transactionDAO.deleteTransactionByPermissionId(permissionId);

        String sql = "DELETE FROM Permissions WHERE permission_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, permissionId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized Permission getPermissionById(String permissionId) throws SQLException {
        String sql = "SELECT * FROM Permissions WHERE permission_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, permissionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPermission(rs);
            }
        }
        return null;
    }

    @Override
    public synchronized void updatePermissionStatus(String permissionId, PermissionStatus status) throws SQLException {
        Permission permission = getPermissionById(permissionId);
        if (permission == null) {
            throw new SQLException("Permission not found!");
        }
        if (permission.getStatus() == PermissionStatus.DONE) {
            throw new SQLException("Permission already completed!");
        }

        Inventory inventory = inventoryDAO.getInventory(permission.getWarehouseId(), permission.getItemId());

        if (permission.getType() == PermissionType.IN) {
            if (inventory == null) {
                inventory = new Inventory(permission.getWarehouseId(), permission.getItemId());
                inventory.setRealStock(permission.getQuantity());
                inventory.setIncomingStock(0);
                inventoryDAO.addInventory(inventory);
            } else {
                inventory.setRealStock(inventory.getRealStock() + permission.getQuantity());
                inventory.setIncomingStock(inventory.getIncomingStock() - permission.getQuantity());
                inventoryDAO.updateInventory(inventory);
            }
        } else {
            if (inventory == null) {
                throw new SQLException("Inventory not found for this item!");
            }
            if (inventory.getRealStock() < permission.getQuantity()) {
                throw new SQLException("Insufficient stock!");
            }
            inventory.setRealStock(inventory.getRealStock() - permission.getQuantity());
            inventory.setReservedStock(inventory.getReservedStock() - permission.getQuantity());
            inventoryDAO.updateInventory(inventory);
        }

        String sql = "UPDATE Permissions SET status = ?, confirmed_date = ? WHERE permission_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            if (status == PermissionStatus.DONE) {
                pstmt.setString(2, LocalDate.now().toString());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            pstmt.setString(3, permissionId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized List<Permission> getPermissionsByWarehouse(String warehouseId) throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM Permissions WHERE warehouse_id = ? ORDER BY permission_id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                permissions.add(mapResultSetToPermission(rs));
            }
        }
        return permissions;
    }

    @Override
    public synchronized List<Permission> getPermissionsByWarehouseAndStatus(String warehouseId, PermissionStatus status) throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM Permissions WHERE warehouse_id = ? AND status = ? ORDER BY permission_id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            pstmt.setString(2, status.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                permissions.add(mapResultSetToPermission(rs));
            }
        }
        return permissions;
    }

    @Override
    public synchronized int getTotalQuantityByWarehouseAndItemAndType(String warehouseId, String itemId, PermissionType type, PermissionStatus status) throws SQLException {
        String sql = "SELECT SUM(quantity) FROM Permissions WHERE warehouse_id = ? AND item_id = ? AND type = ? AND status = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseId);
            pstmt.setString(2, itemId);
            pstmt.setString(3, type.name());
            pstmt.setString(4, status.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public synchronized List<Permission> searchPermissions(String warehouseId, String id, String type, String itemName, String date, PermissionStatus status) throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        List<Permission> allPermissions = getPermissionsByWarehouseAndStatus(warehouseId, status);

        for (Permission p : allPermissions) {
            Item item = null;
            try {
                item = new ItemDAOImpl(connection).getItemById(p.getItemId());
            } catch (SQLException e) {}

            boolean match = true;

            if (id != null && !id.trim().isEmpty() && !p.getPermissionId().toLowerCase().contains(id.toLowerCase())) {
                match = false;
            }
            if (type != null && !type.trim().isEmpty() && !p.getType().name().equalsIgnoreCase(type)) {
                match = false;
            }
            if (itemName != null && !itemName.trim().isEmpty() && (item == null || !item.getName().toLowerCase().contains(itemName.toLowerCase()))) {
                match = false;
            }
            if (date != null && !date.trim().isEmpty()) {
                String pDate = (status == PermissionStatus.ISSUED) ? p.getPermissionDate() : p.getConfirmedDate();
                if (pDate == null || !pDate.contains(date)) {
                    match = false;
                }
            }

            if (match) {
                permissions.add(p);
            }
        }

        return permissions;
    }

    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        Permission permission = new Permission(
                PermissionType.valueOf(rs.getString("type")),
                rs.getString("warehouse_id"),
                rs.getString("item_id"),
                rs.getInt("quantity"),
                rs.getString("title"),
                rs.getString("description")
        );
        permission.setPermissionId(rs.getString("permission_id"));
        permission.setStatus(PermissionStatus.valueOf(rs.getString("status")));
        permission.setPermissionDate(rs.getString("permission_date"));
        permission.setConfirmedDate(rs.getString("confirmed_date"));
        return permission;
    }
}