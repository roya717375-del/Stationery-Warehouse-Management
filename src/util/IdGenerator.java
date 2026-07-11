package util;

import java.sql.*;

public class IdGenerator {
    private static int warehouseCounter = 1;
    private static int categoryCounter = 1;
    private static int itemCounter = 1;
    private static int permissionCounter = 1;
    private static int transactionCounter = 1;

    public static synchronized void syncWithDatabase(Connection conn) throws SQLException {
        warehouseCounter = getMaxId(conn, "Warehouses", "warehouse_id", "WH-") + 1;
        categoryCounter = getMaxId(conn, "Categories", "category_id", "CAT-") + 1;
        itemCounter = getMaxId(conn, "Items", "item_id", "ITM-") + 1;
        permissionCounter = getMaxId(conn, "Permissions", "permission_id", "PRM-") + 1;
        transactionCounter = getMaxId(conn, "FinancialTransactions", "transaction_id", "TRX-") + 1;
    }

    private static int getMaxId(Connection conn, String table, String column, String prefix) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTR(" + column + ", LENGTH(?) + 1) AS INTEGER)) FROM " + table;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public static synchronized String generateWarehouseId() {
        return "WH-" + String.format("%03d", warehouseCounter++);
    }

    public static synchronized String generateCategoryId() {
        return "CAT-" + String.format("%03d", categoryCounter++);
    }

    public static synchronized String generateItemId() {
        return "ITM-" + String.format("%03d", itemCounter++);
    }

    public static synchronized String generatePermissionId() {
        return "PRM-" + String.format("%03d", permissionCounter++);
    }

    public static synchronized String generateTransactionId() {
        return "TRX-" + String.format("%03d", transactionCounter++);
    }

    public static void resetCounters() {
        warehouseCounter = 1;
        categoryCounter = 1;
        itemCounter = 1;
        permissionCounter = 1;
        transactionCounter = 1;
    }
}