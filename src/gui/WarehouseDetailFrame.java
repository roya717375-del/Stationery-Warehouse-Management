package gui;

import model.*;
import DAO.*;
import db.DatabaseManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDetailFrame extends JFrame {
    private Connection connection;
    private WarehouseDAO warehouseDAO;
    private CategoryDAO categoryDAO;
    private ItemDAO itemDAO;
    private PermissionDAO permissionDAO;
    private InventoryDAO inventoryDAO;
    private FinancialTransactionDAO transactionDAO;
    private CashBalanceDAO cashBalanceDAO;

    private Warehouse warehouse;
    private JFrame parent;

    private JList<String> categoryList;
    private DefaultListModel<String> categoryListModel;
    private JTextField catIdSearch;
    private JTextField catNameSearch;
    private JTextField catParentSearch;

    private JTable pendingTable;
    private DefaultTableModel pendingTableModel;
    private JTable doneTable;
    private DefaultTableModel doneTableModel;

    private JTextField pendingIdSearch;
    private JTextField pendingTypeSearch;
    private JTextField pendingItemSearch;
    private JTextField pendingDateSearch;

    private JTextField doneIdSearch;
    private JTextField doneTypeSearch;
    private JTextField doneItemSearch;
    private JTextField doneDateSearch;

    private JTable transactionTable;
    private DefaultTableModel transactionTableModel;
    private JTextField transIdSearch;
    private JTextField transPermSearch;
    private JTextField transTypeSearch;
    private JTextField transDescSearch;

    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private JTextField invItemSearch;
    private JTextField invNameSearch;
    private JTextField invRealSearch;
    private JTextField invReservedSearch;
    private JTextField invIncomingSearch;

    private JLabel cashBalanceLabel;

    public WarehouseDetailFrame(JFrame parent, Warehouse warehouse) {
        this.parent = parent;
        this.warehouse = warehouse;
        initDatabase();
        initUI();
        setTitle("Warehouse: " + warehouse.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(parent);
        loadCategories();
        loadPendingPermissions();
        loadDonePermissions();
        refreshTransactions();
        loadInventory();
        updateCashBalance();
    }

    private void initDatabase() {
        try {
            connection = DatabaseManager.getConnection();
            warehouseDAO = new WarehouseDAOImpl(connection);
            categoryDAO = new CategoryDAOImpl(connection);
            itemDAO = new ItemDAOImpl(connection);
            permissionDAO = new PermissionDAOImpl(connection);
            inventoryDAO = new InventoryDAOImpl(connection);
            transactionDAO = new FinancialTransactionDAOImpl(connection);
            cashBalanceDAO = new CashBalanceDAOImpl(connection);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Categories", createCategoryPanel());
        tabbedPane.addTab("Permissions", createPermissionPanel());
        tabbedPane.addTab("Financial Transactions", createFinancialPanel());
        tabbedPane.addTab("Inventory", createInventoryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back to Warehouses");
        backBtn.addActionListener(e -> dispose());
        bottomPanel.add(backBtn);

        JButton refreshAllBtn = new JButton("Refresh All");
        refreshAllBtn.addActionListener(e -> {
            loadCategories();
            loadPendingPermissions();
            loadDonePermissions();
            refreshTransactions();
            loadInventory();
            updateCashBalance();
        });
        bottomPanel.add(refreshAllBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Warehouse Information"));
        panel.setBackground(new Color(240, 248, 255));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.setBackground(new Color(240, 248, 255));
        row1.add(new JLabel("ID:"));
        row1.add(new JLabel(warehouse.getWarehouseId()));
        row1.add(Box.createHorizontalStrut(40));
        row1.add(new JLabel("Name:"));
        row1.add(new JLabel(warehouse.getName()));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setBackground(new Color(240, 248, 255));
        row2.add(new JLabel("Address:"));
        row2.add(new JLabel(warehouse.getAddress() != null ? warehouse.getAddress() : "No Address"));
        row2.add(Box.createHorizontalStrut(40));
        row2.add(new JLabel("Capacity:"));
        row2.add(new JLabel(String.valueOf(warehouse.getCapacity())));

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3.setBackground(new Color(240, 248, 255));
        row3.add(new JLabel("Cash Balance:"));
        cashBalanceLabel = new JLabel("Loading...");
        cashBalanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cashBalanceLabel.setForeground(new Color(0, 100, 0));
        row3.add(cashBalanceLabel);

        panel.add(row1);
        panel.add(row2);
        panel.add(row3);

        return panel;
    }

    private void updateCashBalance() {
        try {
            CashBalance cb = cashBalanceDAO.getCurrentBalance();
            cashBalanceLabel.setText(String.format("%,d IRR", cb.getBalance().longValue()));
        } catch (SQLException e) {
            cashBalanceLabel.setText("Error loading balance");
        }
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        listPanel.setPreferredSize(new Dimension(0, 0));

        JPanel searchPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("ID:"));
        catIdSearch = new JTextField();
        catIdSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchCategories();
            }
        });
        searchPanel.add(catIdSearch);

        searchPanel.add(new JLabel("Name:"));
        catNameSearch = new JTextField();
        catNameSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchCategories();
            }
        });
        searchPanel.add(catNameSearch);

        searchPanel.add(new JLabel("Parent ID:"));
        catParentSearch = new JTextField();
        catParentSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchCategories();
            }
        });
        searchPanel.add(catParentSearch);

        listPanel.add(searchPanel, BorderLayout.NORTH);

        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        categoryList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openCategoryDetail();
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(categoryList);
        listScroll.setPreferredSize(new Dimension(600, 400));
        listPanel.add(listScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("+ Add");
        addBtn.addActionListener(e -> showAddCategoryDialog());
        buttonPanel.add(addBtn);

        JButton editBtn = new JButton("✎ Edit");
        editBtn.addActionListener(e -> editCategory());
        buttonPanel.add(editBtn);

        JButton deleteBtn = new JButton("× Delete");
        deleteBtn.addActionListener(e -> deleteCategory());
        buttonPanel.add(deleteBtn);

        JLabel instruction = new JLabel("Double-click a category to view its items", SwingConstants.CENTER);
        instruction.setFont(new Font("Arial", Font.ITALIC, 12));
        instruction.setForeground(Color.BLUE);
        buttonPanel.add(instruction);

        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(listPanel, BorderLayout.CENTER);

        return panel;
    }

    private void searchCategories() {
        categoryListModel.clear();
        try {
            List<Category> categories = categoryDAO.searchCategories(
                    catIdSearch.getText(),
                    catNameSearch.getText(),
                    catParentSearch.getText()
            );
            for (Category c : categories) {
                categoryListModel.addElement(c.getCategoryId() + " - " + c.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadCategories() {
        catIdSearch.setText("");
        catNameSearch.setText("");
        catParentSearch.setText("");
        categoryListModel.clear();
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            for (Category c : categories) {
                categoryListModel.addElement(c.getCategoryId() + " - " + c.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private void openCategoryDetail() {
        int index = categoryList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Select a category first!");
            return;
        }

        String selected = categoryListModel.get(index);
        String categoryId = selected.split(" - ")[0];

        try {
            Category category = categoryDAO.getCategoryById(categoryId);
            if (category != null) {
                CategoryDetailFrame detailFrame = new CategoryDetailFrame(this, category, warehouse.getWarehouseId());
                detailFrame.setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showAddCategoryDialog() {
        CategoryDialog dialog = new CategoryDialog(this, categoryDAO);
        dialog.setVisible(true);
        loadCategories();
    }

    private void editCategory() {
        int index = categoryList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Select a category first!");
            return;
        }
        try {
            String selected = categoryListModel.get(index);
            String id = selected.split(" - ")[0];
            Category c = categoryDAO.getCategoryById(id);
            if (c != null) {
                CategoryDialog dialog = new CategoryDialog(this, categoryDAO, c);
                dialog.setVisible(true);
                loadCategories();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        int index = categoryList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Select a category first!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this category?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String selected = categoryListModel.get(index);
                String id = selected.split(" - ")[0];
                categoryDAO.deleteCategory(id);
                loadCategories();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private JPanel createPermissionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane permissionTabbedPane = new JTabbedPane();
        permissionTabbedPane.addTab("Pending (ISSUED)", createPendingPermissionPanel());
        permissionTabbedPane.addTab("Done (DONE)", createDonePermissionPanel());

        panel.add(permissionTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPendingPermissionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("ID:"));
        pendingIdSearch = new JTextField();
        pendingIdSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchPendingPermissions();
            }
        });
        searchPanel.add(pendingIdSearch);

        searchPanel.add(new JLabel("Type (IN/OUT):"));
        pendingTypeSearch = new JTextField();
        pendingTypeSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchPendingPermissions();
            }
        });
        searchPanel.add(pendingTypeSearch);

        searchPanel.add(new JLabel("Item Name:"));
        pendingItemSearch = new JTextField();
        pendingItemSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchPendingPermissions();
            }
        });
        searchPanel.add(pendingItemSearch);

        searchPanel.add(new JLabel("Permission Date:"));
        pendingDateSearch = new JTextField();
        pendingDateSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchPendingPermissions();
            }
        });
        searchPanel.add(pendingDateSearch);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Type", "Item", "Quantity", "Title", "Status", "Permission Date"};
        pendingTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pendingTable = new JTable(pendingTableModel);
        pendingTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(pendingTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn = new JButton("+ Add Permission");
        addBtn.addActionListener(e -> showAddPermissionDialog());
        buttonPanel.add(addBtn);

        JButton editBtn = new JButton("✎ Edit");
        editBtn.addActionListener(e -> editPendingPermission());
        buttonPanel.add(editBtn);

        JButton confirmBtn = new JButton("✓ Confirm");
        confirmBtn.setBackground(new Color(0, 150, 0));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.addActionListener(e -> confirmPendingPermission());
        buttonPanel.add(confirmBtn);

        JButton deleteBtn = new JButton("× Delete");
        deleteBtn.addActionListener(e -> deletePendingPermission());
        buttonPanel.add(deleteBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDonePermissionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("ID:"));
        doneIdSearch = new JTextField();
        doneIdSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchDonePermissions();
            }
        });
        searchPanel.add(doneIdSearch);

        searchPanel.add(new JLabel("Type (IN/OUT):"));
        doneTypeSearch = new JTextField();
        doneTypeSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchDonePermissions();
            }
        });
        searchPanel.add(doneTypeSearch);

        searchPanel.add(new JLabel("Item Name:"));
        doneItemSearch = new JTextField();
        doneItemSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchDonePermissions();
            }
        });
        searchPanel.add(doneItemSearch);

        searchPanel.add(new JLabel("Confirmed Date:"));
        doneDateSearch = new JTextField();
        doneDateSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchDonePermissions();
            }
        });
        searchPanel.add(doneDateSearch);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Type", "Item", "Quantity", "Title", "Status", "Permission Date", "Confirmed Date"};
        doneTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        doneTable = new JTable(doneTableModel);
        doneTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(doneTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("Completed permissions cannot be edited or deleted", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void searchPendingPermissions() {
        String id = pendingIdSearch.getText().trim();
        String type = pendingTypeSearch.getText().trim();
        String itemName = pendingItemSearch.getText().trim();
        String date = pendingDateSearch.getText().trim();

        pendingTableModel.setRowCount(0);
        if (warehouse == null) return;

        try {
            List<Permission> permissions = permissionDAO.searchPermissions(
                    warehouse.getWarehouseId(), id, type, itemName, date, PermissionStatus.ISSUED
            );
            for (Permission p : permissions) {
                Item item = itemDAO.getItemById(p.getItemId());
                pendingTableModel.addRow(new Object[]{
                        p.getPermissionId(),
                        p.getType().name(),
                        item != null ? item.getName() : "Unknown",
                        p.getQuantity(),
                        p.getTitle(),
                        p.getStatus().name(),
                        p.getPermissionDate()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }

    private void loadPendingPermissions() {
        pendingTableModel.setRowCount(0);
        if (warehouse == null) return;
        try {
            List<Permission> permissions = permissionDAO.getPermissionsByWarehouseAndStatus(warehouse.getWarehouseId(), PermissionStatus.ISSUED);
            for (Permission p : permissions) {
                Item item = itemDAO.getItemById(p.getItemId());
                pendingTableModel.addRow(new Object[]{
                        p.getPermissionId(),
                        p.getType().name(),
                        item != null ? item.getName() : "Unknown",
                        p.getQuantity(),
                        p.getTitle(),
                        p.getStatus().name(),
                        p.getPermissionDate()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading pending permissions: " + e.getMessage());
        }
    }

    private void editPendingPermission() {
        int row = pendingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a permission first!");
            return;
        }

        try {
            String permissionId = (String) pendingTableModel.getValueAt(row, 0);
            String status = (String) pendingTableModel.getValueAt(row, 5);

            if (!"ISSUED".equals(status)) {
                JOptionPane.showMessageDialog(this, "Only pending (ISSUED) permissions can be edited!");
                return;
            }

            Permission permission = permissionDAO.getPermissionById(permissionId);
            if (permission != null) {
                PermissionEditDialog dialog = new PermissionEditDialog(
                        this, permissionDAO, warehouseDAO, itemDAO, inventoryDAO, permission
                );
                dialog.setVisible(true);
                loadPendingPermissions();
                loadDonePermissions();
                loadInventory();
                updateCashBalance();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deletePendingPermission() {
        int row = pendingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a permission first!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this pending permission?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String permissionId = (String) pendingTableModel.getValueAt(row, 0);
                permissionDAO.deletePermission(permissionId);
                loadPendingPermissions();
                loadInventory();
                updateCashBalance();
                JOptionPane.showMessageDialog(this, "Permission deleted!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void confirmPendingPermission() {
        int row = pendingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a permission to confirm!");
            return;
        }

        String permissionId = (String) pendingTableModel.getValueAt(row, 0);
        String status = (String) pendingTableModel.getValueAt(row, 5);

        if (!"ISSUED".equals(status)) {
            JOptionPane.showMessageDialog(this, "This permission is already processed!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Confirm this permission?", "Confirm Permission", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                permissionDAO.updatePermissionStatus(permissionId, PermissionStatus.DONE);
                loadPendingPermissions();
                loadDonePermissions();
                loadInventory();
                updateCashBalance();
                JOptionPane.showMessageDialog(this, "Permission confirmed successfully!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void showAddPermissionDialog() {
        PermissionDialog dialog = new PermissionDialog(this, permissionDAO, warehouseDAO, itemDAO, inventoryDAO);
        dialog.setVisible(true);
        loadPendingPermissions();
        loadInventory();
        updateCashBalance();
    }

    private void searchDonePermissions() {
        String id = doneIdSearch.getText().trim();
        String type = doneTypeSearch.getText().trim();
        String itemName = doneItemSearch.getText().trim();
        String date = doneDateSearch.getText().trim();

        doneTableModel.setRowCount(0);
        if (warehouse == null) return;

        try {
            List<Permission> permissions = permissionDAO.searchPermissions(
                    warehouse.getWarehouseId(), id, type, itemName, date, PermissionStatus.DONE
            );
            for (Permission p : permissions) {
                Item item = itemDAO.getItemById(p.getItemId());
                doneTableModel.addRow(new Object[]{
                        p.getPermissionId(),
                        p.getType().name(),
                        item != null ? item.getName() : "Unknown",
                        p.getQuantity(),
                        p.getTitle(),
                        p.getStatus().name(),
                        p.getPermissionDate(),
                        p.getConfirmedDate() != null ? p.getConfirmedDate() : "N/A"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }

    private void loadDonePermissions() {
        doneTableModel.setRowCount(0);
        if (warehouse == null) return;
        try {
            List<Permission> permissions = permissionDAO.getPermissionsByWarehouseAndStatus(warehouse.getWarehouseId(), PermissionStatus.DONE);
            for (Permission p : permissions) {
                Item item = itemDAO.getItemById(p.getItemId());
                doneTableModel.addRow(new Object[]{
                        p.getPermissionId(),
                        p.getType().name(),
                        item != null ? item.getName() : "Unknown",
                        p.getQuantity(),
                        p.getTitle(),
                        p.getStatus().name(),
                        p.getPermissionDate(),
                        p.getConfirmedDate() != null ? p.getConfirmedDate() : "N/A"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading done permissions: " + e.getMessage());
        }
    }

    private JPanel createFinancialPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("Transaction ID:"));
        transIdSearch = new JTextField();
        transIdSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchTransactions();
            }
        });
        searchPanel.add(transIdSearch);

        searchPanel.add(new JLabel("Permission ID:"));
        transPermSearch = new JTextField();
        transPermSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchTransactions();
            }
        });
        searchPanel.add(transPermSearch);

        searchPanel.add(new JLabel("Type:"));
        transTypeSearch = new JTextField();
        transTypeSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchTransactions();
            }
        });
        searchPanel.add(transTypeSearch);

        searchPanel.add(new JLabel("Description:"));
        transDescSearch = new JTextField();
        transDescSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchTransactions();
            }
        });
        searchPanel.add(transDescSearch);

        topPanel.add(searchPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Permission", "Type", "Amount", "Date", "Description"};
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void searchTransactions() {
        String id = transIdSearch.getText().trim();
        String permissionId = transPermSearch.getText().trim();
        String type = transTypeSearch.getText().trim();
        String description = transDescSearch.getText().trim();

        if (id.isEmpty() && permissionId.isEmpty() && type.isEmpty() && description.isEmpty()) {
            refreshTransactions();
            return;
        }

        transactionTableModel.setRowCount(0);
        try {
            List<FinancialTransaction> transactions = transactionDAO.searchTransactions(id, permissionId, type, description);
            for (FinancialTransaction t : transactions) {
                transactionTableModel.addRow(new Object[]{
                        t.getTransactionId(),
                        t.getPermissionId(),
                        t.getType().name(),
                        t.getAmount(),
                        t.getTransactionDate(),
                        t.getDescription() != null ? t.getDescription() : "-"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }

    private void refreshTransactions() {
        transIdSearch.setText("");
        transPermSearch.setText("");
        transTypeSearch.setText("");
        transDescSearch.setText("");
        transactionTableModel.setRowCount(0);
        if (warehouse == null) return;
        try {
            List<Permission> permissions = permissionDAO.getPermissionsByWarehouse(warehouse.getWarehouseId());
            List<FinancialTransaction> allTransactions = new ArrayList<>();
            for (Permission p : permissions) {
                allTransactions.addAll(transactionDAO.getTransactionsByPermission(p.getPermissionId()));
            }
            for (FinancialTransaction t : allTransactions) {
                transactionTableModel.addRow(new Object[]{
                        t.getTransactionId(),
                        t.getPermissionId(),
                        t.getType().name(),
                        t.getAmount(),
                        t.getTransactionDate(),
                        t.getDescription() != null ? t.getDescription() : "-"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("Item ID:"));
        invItemSearch = new JTextField();
        invItemSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchInventory();
            }
        });
        searchPanel.add(invItemSearch);

        searchPanel.add(new JLabel("Item Name:"));
        invNameSearch = new JTextField();
        invNameSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchInventory();
            }
        });
        searchPanel.add(invNameSearch);

        searchPanel.add(new JLabel("Real Stock:"));
        invRealSearch = new JTextField();
        invRealSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchInventory();
            }
        });
        searchPanel.add(invRealSearch);

        searchPanel.add(new JLabel("Reserved:"));
        invReservedSearch = new JTextField();
        invReservedSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchInventory();
            }
        });
        searchPanel.add(invReservedSearch);

        searchPanel.add(new JLabel("Incoming:"));
        invIncomingSearch = new JTextField();
        invIncomingSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchInventory();
            }
        });
        searchPanel.add(invIncomingSearch);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"Item ID", "Item Name", "Real Stock", "Reserved", "Incoming", "Available", "Last Updated"};
        inventoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("Inventory is view-only. Use Permissions to manage stock.", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void searchInventory() {
        String itemId = invItemSearch.getText().trim();
        String itemName = invNameSearch.getText().trim();
        String realStock = invRealSearch.getText().trim();
        String reserved = invReservedSearch.getText().trim();
        String incoming = invIncomingSearch.getText().trim();

        inventoryTableModel.setRowCount(0);
        if (warehouse == null) return;

        try {
            List<Inventory> inventories = inventoryDAO.searchInventory(itemId, itemName, realStock, reserved, incoming);
            for (Inventory inv : inventories) {
                Item item = itemDAO.getItemById(inv.getItemId());
                if (item == null) continue;
                inventoryTableModel.addRow(new Object[]{
                        inv.getItemId(),
                        item.getName(),
                        inv.getRealStock(),
                        inv.getReservedStock(),
                        inv.getIncomingStock(),
                        inv.getAvailableStock(),
                        inv.getLastUpdated() != null ? inv.getLastUpdated() : "N/A"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching inventory: " + e.getMessage());
        }
    }

    private void loadInventory() {
        invItemSearch.setText("");
        invNameSearch.setText("");
        invRealSearch.setText("");
        invReservedSearch.setText("");
        invIncomingSearch.setText("");
        inventoryTableModel.setRowCount(0);
        if (warehouse == null) return;
        try {
            List<Inventory> inventories = inventoryDAO.getInventoryByWarehouse(warehouse.getWarehouseId());
            for (Inventory inv : inventories) {
                Item item = itemDAO.getItemById(inv.getItemId());
                inventoryTableModel.addRow(new Object[]{
                        inv.getItemId(),
                        item != null ? item.getName() : "Unknown",
                        inv.getRealStock(),
                        inv.getReservedStock(),
                        inv.getIncomingStock(),
                        inv.getAvailableStock(),
                        inv.getLastUpdated() != null ? inv.getLastUpdated() : "N/A"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage());
        }
    }
}