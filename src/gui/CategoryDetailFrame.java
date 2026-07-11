package gui;

import model.*;
import DAO.*;
import db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CategoryDetailFrame extends JFrame {
    private Connection connection;
    private CategoryDAO categoryDAO;
    private ItemDAO itemDAO;
    private InventoryDAO inventoryDAO;
    private WarehouseDAO warehouseDAO;

    private Category category;
    private String warehouseId;
    private JFrame parent;

    private JList<String> itemList;
    private DefaultListModel<String> itemListModel;
    private JTextField itemIdSearch, itemCodeSearch, itemNameSearch, itemPriceSearch;
    private JTextArea itemDetailsArea;
    private JTextArea inventoryArea;

    public CategoryDetailFrame(JFrame parent, Category category, String warehouseId) {
        this.parent = parent;
        this.category = category;
        this.warehouseId = warehouseId;
        initDatabase();
        initUI();
        setTitle("Category: " + category.getName() + " - Warehouse: " + warehouseId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(parent);
        loadItems();
    }

    private void initDatabase() {
        try {
            connection = DatabaseManager.getConnection();
            categoryDAO = new CategoryDAOImpl(connection);
            itemDAO = new ItemDAOImpl(connection);
            inventoryDAO = new InventoryDAOImpl(connection);
            warehouseDAO = new WarehouseDAOImpl(connection);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createTitledBorder("Items in this Category"));

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("ID:"));
        itemIdSearch = new JTextField();
        itemIdSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchItems();
            }
        });
        searchPanel.add(itemIdSearch);

        searchPanel.add(new JLabel("Code:"));
        itemCodeSearch = new JTextField();
        itemCodeSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchItems();
            }
        });
        searchPanel.add(itemCodeSearch);

        searchPanel.add(new JLabel("Name:"));
        itemNameSearch = new JTextField();
        itemNameSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchItems();
            }
        });
        searchPanel.add(itemNameSearch);

        searchPanel.add(new JLabel("Unit Price:"));
        itemPriceSearch = new JTextField();
        itemPriceSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchItems();
            }
        });
        searchPanel.add(itemPriceSearch);

        itemPanel.add(searchPanel, BorderLayout.NORTH);

        itemListModel = new DefaultListModel<>();
        itemList = new JList<>(itemListModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onItemSelected();
            }
        });

        JScrollPane listScroll = new JScrollPane(itemList);
        listScroll.setPreferredSize(new Dimension(400, 300));
        itemPanel.add(listScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("+ Add");
        addBtn.addActionListener(e -> showAddItemDialog());
        buttonPanel.add(addBtn);

        JButton editBtn = new JButton("✎ Edit");
        editBtn.addActionListener(e -> editItem());
        buttonPanel.add(editBtn);

        JButton deleteBtn = new JButton("× Delete");
        deleteBtn.addActionListener(e -> deleteItem());
        buttonPanel.add(deleteBtn);

        JLabel instruction = new JLabel("Select an item to view inventory in this warehouse");
        instruction.setFont(new Font("Arial", Font.ITALIC, 11));
        instruction.setForeground(Color.BLUE);
        buttonPanel.add(instruction);

        itemPanel.add(buttonPanel, BorderLayout.SOUTH);

        centerPanel.add(itemPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        rightPanel.setPreferredSize(new Dimension(450, 0));

        itemDetailsArea = new JTextArea();
        itemDetailsArea.setEditable(false);
        itemDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        itemDetailsArea.setBackground(new Color(255, 255, 240));
        JScrollPane itemScroll = new JScrollPane(itemDetailsArea);
        itemScroll.setBorder(BorderFactory.createTitledBorder("Item Details"));
        rightPanel.add(itemScroll);

        inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        inventoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inventoryArea.setBackground(new Color(240, 255, 240));
        JScrollPane invScroll = new JScrollPane(inventoryArea);
        invScroll.setBorder(BorderFactory.createTitledBorder("Inventory in This Warehouse"));
        rightPanel.add(invScroll);

        centerPanel.add(rightPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back to Warehouse");
        backBtn.addActionListener(e -> dispose());
        bottomPanel.add(backBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            loadItems();
            itemDetailsArea.setText("");
            inventoryArea.setText("");
        });
        bottomPanel.add(refreshBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        itemDetailsArea.setText("Select an item to view details");
        inventoryArea.setText("Inventory will appear here for the selected warehouse");
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Category Information"));
        panel.setBackground(new Color(240, 248, 255));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.setBackground(new Color(240, 248, 255));
        row1.add(new JLabel("ID:"));
        row1.add(new JLabel(category.getCategoryId()));
        row1.add(Box.createHorizontalStrut(40));
        row1.add(new JLabel("Name:"));
        row1.add(new JLabel(category.getName()));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setBackground(new Color(240, 248, 255));
        row2.add(new JLabel("Parent ID:"));
        row2.add(new JLabel(category.getParentId() != null ? category.getParentId() : "None"));
        row2.add(Box.createHorizontalStrut(40));
        row2.add(new JLabel("Warehouse:"));
        try {
            Warehouse w = warehouseDAO.getWarehouseById(warehouseId);
            row2.add(new JLabel(w != null ? w.getName() : warehouseId));
        } catch (SQLException e) {
            row2.add(new JLabel(warehouseId));
        }

        panel.add(row1);
        panel.add(row2);

        return panel;
    }

    private void searchItems() {
        String id = itemIdSearch.getText().trim();
        String code = itemCodeSearch.getText().trim();
        String name = itemNameSearch.getText().trim();
        String price = itemPriceSearch.getText().trim();

        itemListModel.clear();
        try {
            List<Item> items = itemDAO.searchItems(id, code, name, price);
            for (Item i : items) {
                if (i.getCategoryId().equals(category.getCategoryId())) {
                    itemListModel.addElement(i.getItemId() + " - " + i.getName() + " (" + i.getItemCode() + ")");
                }
            }
            if (itemListModel.size() > 0) {
                itemList.setSelectedIndex(0);
            } else {
                itemDetailsArea.setText("No items found");
                inventoryArea.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }

    private void loadItems() {
        itemIdSearch.setText("");
        itemCodeSearch.setText("");
        itemNameSearch.setText("");
        itemPriceSearch.setText("");
        itemListModel.clear();
        try {
            List<Item> items = itemDAO.getItemsByCategory(category.getCategoryId());
            for (Item i : items) {
                itemListModel.addElement(i.getItemId() + " - " + i.getName() + " (" + i.getItemCode() + ")");
            }
            if (itemListModel.size() > 0) {
                itemList.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }
    }

    private void onItemSelected() {
        int index = itemList.getSelectedIndex();
        if (index == -1) {
            itemDetailsArea.setText("Select an item to view details");
            inventoryArea.setText("");
            return;
        }

        String selected = itemListModel.get(index);
        String itemId = selected.split(" - ")[0];

        try {
            Item item = itemDAO.getItemById(itemId);
            if (item != null) {
                String details = String.format(
                        "═══════════════════════════════════════\n" +
                                "  ITEM DETAILS\n" +
                                "═══════════════════════════════════════\n" +
                                "  ID          : %s\n" +
                                "  Code        : %s\n" +
                                "  Name        : %s\n" +
                                "  Description : %s\n" +
                                "  Category    : %s\n" +
                                "  Unit Price  : %,d IRR\n" +
                                "═══════════════════════════════════════",
                        item.getItemId(),
                        item.getItemCode(),
                        item.getName(),
                        item.getDescription() != null ? item.getDescription() : "No description",
                        item.getCategoryId(),
                        item.getUnitPrice().longValue()
                );
                itemDetailsArea.setText(details);
                loadInventory(itemId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading item: " + e.getMessage());
        }
    }

    private void loadInventory(String itemId) {
        try {
            Inventory inv = inventoryDAO.getInventoryByWarehouseAndItem(warehouseId, itemId);

            StringBuilder sb = new StringBuilder();
            if (inv == null) {
                sb.append("═══════════════════════════════════════\n");
                sb.append("  No inventory found for this item\n");
                sb.append("  in the selected warehouse.\n");
                sb.append("═══════════════════════════════════════");
            } else {
                Warehouse warehouse = warehouseDAO.getWarehouseById(warehouseId);
                sb.append("═══════════════════════════════════════════════════════════\n");
                sb.append("  INVENTORY INFORMATION\n");
                sb.append("═══════════════════════════════════════════════════════════\n\n");
                sb.append(String.format(
                        "  Warehouse   : %s\n" +
                                "  Warehouse ID: %s\n" +
                                "  ─────────────────────────────────────\n" +
                                "  Real Stock  : %d\n" +
                                "  Reserved    : %d\n" +
                                "  Incoming    : %d\n" +
                                "  Available   : %d\n" +
                                "  ─────────────────────────────────────\n" +
                                "  Last Updated: %s\n" +
                                "═══════════════════════════════════════════════════════════",
                        warehouse != null ? warehouse.getName() : warehouseId,
                        warehouseId,
                        inv.getRealStock(),
                        inv.getReservedStock(),
                        inv.getIncomingStock(),
                        inv.getAvailableStock(),
                        inv.getLastUpdated() != null ? inv.getLastUpdated() : "N/A"
                ));
            }
            inventoryArea.setText(sb.toString());
        } catch (SQLException e) {
            inventoryArea.setText("Error loading inventory: " + e.getMessage());
        }
    }

    private void showAddItemDialog() {
        ItemDialog dialog = new ItemDialog(this, itemDAO, categoryDAO);
        dialog.setVisible(true);
        loadItems();
    }

    private void editItem() {
        int index = itemList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first!");
            return;
        }
        try {
            String selected = itemListModel.get(index);
            String id = selected.split(" - ")[0];
            Item item = itemDAO.getItemById(id);
            if (item != null) {
                ItemDialog dialog = new ItemDialog(this, itemDAO, categoryDAO, item);
                dialog.setVisible(true);
                loadItems();
                onItemSelected();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteItem() {
        int index = itemList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String selected = itemListModel.get(index);
                String id = selected.split(" - ")[0];
                itemDAO.deleteItem(id);
                loadItems();
                itemDetailsArea.setText("Item deleted");
                inventoryArea.setText("");
            } catch (SQLException e) {
                if (e.getMessage().contains("inventory")) {
                    JOptionPane.showMessageDialog(this, "Cannot delete: Item exists in inventory!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        }
    }
}