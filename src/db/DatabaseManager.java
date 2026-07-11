package db;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:warehouse.db";
    private static Connection connection = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initializeDatabase() {
        String sql =
                "CREATE TABLE IF NOT EXISTS Warehouses (" +
                        "warehouse_id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "address TEXT NOT NULL, " +
                        "capacity INTEGER NOT NULL" +
                        ");" +

                        "CREATE TABLE IF NOT EXISTS Categories (" +
                        "category_id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "parent_id TEXT" +
                        ");" +

                        "CREATE TABLE IF NOT EXISTS Items (" +
                        "item_id TEXT PRIMARY KEY, " +
                        "item_code TEXT NOT NULL UNIQUE, " +
                        "name TEXT NOT NULL, " +
                        "description TEXT, " +
                        "category_id TEXT NOT NULL, " +
                        "unit_price DECIMAL(15,2) DEFAULT 0" +
                        ");" +

                        "CREATE TABLE IF NOT EXISTS Permissions (" +
                        "permission_id TEXT PRIMARY KEY, " +
                        "type TEXT NOT NULL CHECK(type IN ('IN', 'OUT')), " +
                        "warehouse_id TEXT NOT NULL, " +
                        "item_id TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT, " +
                        "status TEXT NOT NULL CHECK(status IN ('ISSUED', 'DONE')), " +
                        "permission_date TEXT DEFAULT CURRENT_DATE, " +
                        "confirmed_date TEXT" +
                        ");" +

                        "CREATE TABLE IF NOT EXISTS Inventory (" +
                        "warehouse_id TEXT NOT NULL, " +
                        "item_id TEXT NOT NULL, " +
                        "real_stock INTEGER NOT NULL DEFAULT 0, " +
                        "reserved_stock INTEGER NOT NULL DEFAULT 0, " +
                        "incoming_stock INTEGER NOT NULL DEFAULT 0, " +
                        "last_updated TEXT DEFAULT CURRENT_DATE, " +
                        "PRIMARY KEY (warehouse_id, item_id)" +
                        ");" +

                        "CREATE TABLE IF NOT EXISTS CashBalance (" +
                        "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                        "balance DECIMAL(15,2) NOT NULL DEFAULT 0, " +
                        "last_updated TEXT DEFAULT CURRENT_DATE" +
                        ");" +

                        "CREATE TABLE IF NOT EXISTS FinancialTransactions (" +
                        "transaction_id TEXT PRIMARY KEY, " +
                        "permission_id TEXT NOT NULL, " +
                        "type TEXT NOT NULL CHECK(type IN ('PURCHASE', 'SALE')), " +
                        "amount DECIMAL(15,2) NOT NULL, " +
                        "transaction_date TEXT DEFAULT CURRENT_DATE, " +
                        "description TEXT" +
                        ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertSampleData() {
        if (!isDatabaseEmpty()) {
            System.out.println("Database already initialized, skipping sample data.");
            return;
        }

        try (Connection conn = getConnection()) {

            try (PreparedStatement p = conn.prepareStatement(
                    "INSERT OR IGNORE INTO CashBalance (id, balance) VALUES (1, 0)")) {
                p.executeUpdate();
            }

            String[][] categories = {
                    {"CAT-001", "Writing Instruments"},
                    {"CAT-002", "Notebooks & Paper"},
                    {"CAT-003", "Office Supplies"},
                    {"CAT-004", "Art Supplies"},
                    {"CAT-005", "Filing & Organization"}
            };
            try (PreparedStatement p = conn.prepareStatement(
                    "INSERT OR IGNORE INTO Categories (category_id, name) VALUES (?, ?)")) {
                for (String[] c : categories) {
                    p.setString(1, c[0]);
                    p.setString(2, c[1]);
                    p.executeUpdate();
                }
            }

            Object[][] warehouses = {
                    {"WH-001", "Central Warehouse", "Tehran, Enghelab Ave", 10000},
                    {"WH-002", "East Warehouse", "Tehran, Resalat", 6000},
                    {"WH-003", "West Warehouse", "Karaj, Azadi Sq", 5000}
            };
            try (PreparedStatement p = conn.prepareStatement(
                    "INSERT OR IGNORE INTO Warehouses (warehouse_id, name, address, capacity) VALUES (?, ?, ?, ?)")) {
                for (Object[] w : warehouses) {
                    p.setString(1, (String) w[0]);
                    p.setString(2, (String) w[1]);
                    p.setString(3, (String) w[2]);
                    p.setInt(4, (int) w[3]);
                    p.executeUpdate();
                }
            }

            Object[][] items = {
                    {"ITM-001", "PEN-001", "Ballpoint Pen (Blue)", "Box of 12", "CAT-001", 12000},
                    {"ITM-002", "PEN-002", "Gel Pen (Black)", "Box of 10", "CAT-001", 18000},
                    {"ITM-003", "PEN-003", "Mechanical Pencil", "0.5mm lead", "CAT-001", 15000},
                    {"ITM-004", "PEN-004", "Highlighter Set", "4 colors", "CAT-001", 28000},
                    {"ITM-005", "NTB-001", "A4 Notebook", "100 sheets", "CAT-002", 35000},
                    {"ITM-006", "NTB-002", "A5 Notebook", "60 sheets", "CAT-002", 22000},
                    {"ITM-007", "PAP-001", "A4 Copy Paper", "500 sheets", "CAT-002", 60000},
                    {"ITM-008", "OFF-001", "Heavy Duty Stapler", "Includes staples", "CAT-003", 45000},
                    {"ITM-009", "OFF-002", "Paper Clips", "Box of 100", "CAT-003", 9000},
                    {"ITM-010", "OFF-003", "Sticky Notes", "Pack of 6", "CAT-003", 20000},
                    {"ITM-011", "ART-001", "Colored Pencils", "Set of 24", "CAT-004", 55000},
                    {"ITM-012", "ART-002", "Watercolor Paint Set", "18 colors", "CAT-004", 75000},
                    {"ITM-013", "FIL-001", "Ring Binder A4", "3-ring, 5cm", "CAT-005", 32000}
            };
            Map<String, BigDecimal> priceByItem = new LinkedHashMap<>();
            try (PreparedStatement p = conn.prepareStatement(
                    "INSERT OR IGNORE INTO Items (item_id, item_code, name, description, category_id, unit_price) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (Object[] it : items) {
                    String itemId = (String) it[0];
                    BigDecimal price = BigDecimal.valueOf((int) it[5]);
                    p.setString(1, itemId);
                    p.setString(2, (String) it[1]);
                    p.setString(3, (String) it[2]);
                    p.setString(4, (String) it[3]);
                    p.setString(5, (String) it[4]);
                    p.setBigDecimal(6, price);
                    p.executeUpdate();
                    priceByItem.put(itemId, price);
                }
            }

            Object[][] permissions = {
                    {"PRM-001", "IN", "WH-001", "ITM-001", 300, "Initial Stock - Ballpoint Pens", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-002", "IN", "WH-001", "ITM-002", 200, "Initial Stock - Gel Pens", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-003", "IN", "WH-001", "ITM-003", 250, "Initial Stock - Mechanical Pencils", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-004", "IN", "WH-001", "ITM-004", 150, "Initial Stock - Highlighters", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-005", "IN", "WH-001", "ITM-005", 180, "Initial Stock - A4 Notebooks", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-006", "IN", "WH-001", "ITM-006", 140, "Initial Stock - A5 Notebooks", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-007", "IN", "WH-001", "ITM-007", 100, "Initial Stock - Copy Paper", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-008", "IN", "WH-001", "ITM-008", 80, "Initial Stock - Staplers", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-009", "IN", "WH-001", "ITM-009", 300, "Initial Stock - Paper Clips", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-010", "IN", "WH-001", "ITM-010", 220, "Initial Stock - Sticky Notes", "First shipment", "DONE", "2025-01-05", "2025-01-06"},
                    {"PRM-011", "IN", "WH-002", "ITM-001", 150, "Initial Stock - Ballpoint Pens", "East warehouse opening stock", "DONE", "2025-01-08", "2025-01-09"},
                    {"PRM-012", "IN", "WH-002", "ITM-005", 90, "Initial Stock - A4 Notebooks", "East warehouse opening stock", "DONE", "2025-01-08", "2025-01-09"},
                    {"PRM-013", "IN", "WH-002", "ITM-008", 40, "Initial Stock - Staplers", "East warehouse opening stock", "DONE", "2025-01-08", "2025-01-09"},
                    {"PRM-014", "IN", "WH-002", "ITM-011", 60, "Initial Stock - Colored Pencils", "East warehouse opening stock", "DONE", "2025-01-08", "2025-01-09"},
                    {"PRM-015", "IN", "WH-003", "ITM-006", 70, "Initial Stock - A5 Notebooks", "West warehouse opening stock", "DONE", "2025-01-10", "2025-01-11"},
                    {"PRM-016", "IN", "WH-003", "ITM-012", 35, "Initial Stock - Watercolor Sets", "West warehouse opening stock", "DONE", "2025-01-10", "2025-01-11"},
                    {"PRM-017", "IN", "WH-003", "ITM-013", 90, "Initial Stock - Ring Binders", "West warehouse opening stock", "DONE", "2025-01-10", "2025-01-11"},
                    {"PRM-018", "OUT", "WH-001", "ITM-001", 45, "Retail Order #1021", "Sold to retail partner", "DONE", "2025-01-15", "2025-01-16"},
                    {"PRM-019", "OUT", "WH-001", "ITM-003", 60, "School Supply Order", "Sold to local school", "DONE", "2025-01-18", "2025-01-19"},
                    {"PRM-020", "OUT", "WH-001", "ITM-005", 30, "Bookstore Restock", "Sold to bookstore", "DONE", "2025-01-20", "2025-01-21"},
                    {"PRM-021", "OUT", "WH-001", "ITM-009", 80, "Corporate Order", "Sold to company", "DONE", "2025-01-22", "2025-01-23"},
                    {"PRM-022", "OUT", "WH-001", "ITM-010", 50, "Office Supply Order", "Internal consumption", "DONE", "2025-01-25", "2025-01-26"},
                    {"PRM-023", "OUT", "WH-002", "ITM-001", 25, "Retail Order #1022", "Sold to retail partner", "DONE", "2025-01-28", "2025-01-29"},
                    {"PRM-024", "OUT", "WH-002", "ITM-008", 10, "Small Business Order", "Sold to small business", "DONE", "2025-02-01", "2025-02-02"},
                    {"PRM-025", "OUT", "WH-003", "ITM-006", 15, "Local Shop Order", "Sold to local shop", "DONE", "2025-02-03", "2025-02-04"},
                    {"PRM-026", "OUT", "WH-003", "ITM-013", 20, "Filing Supplies Order", "Sold to office", "DONE", "2025-02-05", "2025-02-06"},
                    {"PRM-027", "IN", "WH-001", "ITM-002", 100, "Restock - Gel Pens", "Second shipment", "DONE", "2025-02-08", "2025-02-09"},
                    {"PRM-028", "IN", "WH-001", "ITM-007", 50, "Restock - Copy Paper", "Second shipment", "DONE", "2025-02-10", "2025-02-11"},
                    {"PRM-029", "IN", "WH-001", "ITM-004", 80, "Restock Order - Highlighters", "Ordered from supplier, awaiting delivery", "ISSUED", "2025-02-18", null},
                    {"PRM-030", "OUT", "WH-001", "ITM-006", 25, "Pending Customer Pickup", "Reserved, not yet picked up", "ISSUED", "2025-02-19", null},
                    {"PRM-031", "OUT", "WH-002", "ITM-011", 15, "Pending Art School Order", "Awaiting customer confirmation", "ISSUED", "2025-02-20", null},
                    {"PRM-032", "IN", "WH-003", "ITM-012", 20, "Restock Order - Watercolor Sets", "New shipment ordered", "ISSUED", "2025-02-21", null}
            };

            Map<String, int[]> inventory = new LinkedHashMap<>();
            BigDecimal cashBalance = BigDecimal.valueOf(150_000_000);
            int trxCounter = 1;

            String insertPermSql = "INSERT INTO Permissions (permission_id, type, warehouse_id, item_id, quantity, title, description, status, permission_date, confirmed_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String insertTrxSql = "INSERT INTO FinancialTransactions (transaction_id, permission_id, type, amount, transaction_date, description) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement permStmt = conn.prepareStatement(insertPermSql);
                 PreparedStatement trxStmt = conn.prepareStatement(insertTrxSql)) {

                for (Object[] row : permissions) {
                    String id = (String) row[0];
                    String type = (String) row[1];
                    String warehouseId = (String) row[2];
                    String itemId = (String) row[3];
                    int quantity = (int) row[4];
                    String title = (String) row[5];
                    String description = (String) row[6];
                    String status = (String) row[7];
                    String permDate = (String) row[8];
                    String confirmedDate = (String) row[9];

                    permStmt.setString(1, id);
                    permStmt.setString(2, type);
                    permStmt.setString(3, warehouseId);
                    permStmt.setString(4, itemId);
                    permStmt.setInt(5, quantity);
                    permStmt.setString(6, title);
                    permStmt.setString(7, description);
                    permStmt.setString(8, status);
                    permStmt.setString(9, permDate);
                    if (confirmedDate == null) {
                        permStmt.setNull(10, Types.VARCHAR);
                    } else {
                        permStmt.setString(10, confirmedDate);
                    }
                    permStmt.executeUpdate();

                    boolean isIn = type.equals("IN");
                    BigDecimal amount = priceByItem.get(itemId).multiply(BigDecimal.valueOf(quantity));
                    String trxType = isIn ? "PURCHASE" : "SALE";
                    String trxDesc = (isIn ? "Purchase: " : "Sale: ") + title;

                    trxStmt.setString(1, String.format("TRX-%03d", trxCounter++));
                    trxStmt.setString(2, id);
                    trxStmt.setString(3, trxType);
                    trxStmt.setBigDecimal(4, amount);
                    trxStmt.setString(5, permDate);
                    trxStmt.setString(6, trxDesc);
                    trxStmt.executeUpdate();

                    cashBalance = isIn ? cashBalance.subtract(amount) : cashBalance.add(amount);

                    int[] inv = inventory.computeIfAbsent(warehouseId + "|" + itemId, k -> new int[3]);
                    boolean done = status.equals("DONE");
                    if (isIn) {
                        if (done) inv[0] += quantity; else inv[2] += quantity;
                    } else {
                        if (done) inv[0] -= quantity; else inv[1] += quantity;
                    }
                }
            }

            try (PreparedStatement invStmt = conn.prepareStatement(
                    "INSERT INTO Inventory (warehouse_id, item_id, real_stock, reserved_stock, incoming_stock) VALUES (?, ?, ?, ?, ?)")) {
                for (Map.Entry<String, int[]> e : inventory.entrySet()) {
                    String[] parts = e.getKey().split("\\|");
                    int[] v = e.getValue();
                    invStmt.setString(1, parts[0]);
                    invStmt.setString(2, parts[1]);
                    invStmt.setInt(3, v[0]);
                    invStmt.setInt(4, v[1]);
                    invStmt.setInt(5, v[2]);
                    invStmt.executeUpdate();
                }
            }

            try (PreparedStatement cashStmt = conn.prepareStatement(
                    "UPDATE CashBalance SET balance = ?, last_updated = ? WHERE id = 1")) {
                cashStmt.setBigDecimal(1, cashBalance);
                cashStmt.setString(2, "2025-02-21");
                cashStmt.executeUpdate();
            }

            System.out.println("Sample data inserted successfully! Final cash balance: " + cashBalance);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean isDatabaseEmpty() {
        String sql = "SELECT COUNT(*) FROM Warehouses";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setupDatabase() {
        initializeDatabase();
        insertSampleData();
    }
}