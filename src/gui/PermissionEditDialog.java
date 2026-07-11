package gui;

import model.*;
import DAO.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionEditDialog extends JDialog {
    private PermissionDAO permissionDAO;
    private WarehouseDAO warehouseDAO;
    private ItemDAO itemDAO;
    private InventoryDAO inventoryDAO;
    private Permission permission;

    private JComboBox<String> typeCombo;
    private JComboBox<String> warehouseCombo;
    private JComboBox<String> itemCombo;
    private JTextField quantityField;
    private JTextField titleField;
    private JTextArea descriptionArea;

    private Map<String, String> warehouseMap;
    private Map<String, String> itemMap;

    public PermissionEditDialog(Frame owner, PermissionDAO permissionDAO,
                                WarehouseDAO warehouseDAO, ItemDAO itemDAO,
                                InventoryDAO inventoryDAO, Permission permission) {
        super(owner, "Edit Permission", true);
        this.permissionDAO = permissionDAO;
        this.warehouseDAO = warehouseDAO;
        this.itemDAO = itemDAO;
        this.inventoryDAO = inventoryDAO;
        this.permission = permission;
        this.warehouseMap = new HashMap<>();
        this.itemMap = new HashMap<>();

        initComponents();
        loadData();
        loadPermissionData();
        setSize(500, 500);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[]{"IN", "OUT"});
        formPanel.add(typeCombo, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Warehouse:"), gbc);
        gbc.gridx = 1;
        warehouseCombo = new JComboBox<>();
        warehouseCombo.setPreferredSize(new Dimension(300, 25));
        formPanel.add(warehouseCombo, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Item:"), gbc);
        gbc.gridx = 1;
        itemCombo = new JComboBox<>();
        itemCombo.setPreferredSize(new Dimension(300, 25));
        formPanel.add(itemCombo, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField();
        quantityField.setPreferredSize(new Dimension(300, 25));
        formPanel.add(quantityField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(300, 25));
        formPanel.add(titleField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(300, 80));
        formPanel.add(scrollPane, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Update");
        saveButton.addActionListener(e -> updatePermission());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            warehouseCombo.removeAllItems();
            List<Warehouse> warehouses = warehouseDAO.getAllWarehouses();
            for (Warehouse w : warehouses) {
                warehouseCombo.addItem(w.getWarehouseId() + " - " + w.getName());
                warehouseMap.put(w.getWarehouseId() + " - " + w.getName(), w.getWarehouseId());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading warehouses: " + e.getMessage());
        }

        try {
            itemCombo.removeAllItems();
            List<Item> items = itemDAO.getAllItems();
            for (Item item : items) {
                itemCombo.addItem(item.getItemId() + " - " + item.getName());
                itemMap.put(item.getItemId() + " - " + item.getName(), item.getItemId());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }
    }

    private void loadPermissionData() {
        typeCombo.setSelectedItem(permission.getType().name());

        try {
            Warehouse w = warehouseDAO.getWarehouseById(permission.getWarehouseId());
            if (w != null) {
                warehouseCombo.setSelectedItem(w.getWarehouseId() + " - " + w.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Item item = itemDAO.getItemById(permission.getItemId());
            if (item != null) {
                itemCombo.setSelectedItem(item.getItemId() + " - " + item.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        quantityField.setText(String.valueOf(permission.getQuantity()));
        titleField.setText(permission.getTitle());
        descriptionArea.setText(permission.getDescription());
    }

    private void updatePermission() {
        try {
            String typeStr = (String) typeCombo.getSelectedItem();
            PermissionType type = PermissionType.valueOf(typeStr);

            String warehouseKey = (String) warehouseCombo.getSelectedItem();
            if (warehouseKey == null) {
                JOptionPane.showMessageDialog(this, "Please select a warehouse!");
                return;
            }
            String warehouseId = warehouseMap.get(warehouseKey);

            String itemKey = (String) itemCombo.getSelectedItem();
            if (itemKey == null) {
                JOptionPane.showMessageDialog(this, "Please select an item!");
                return;
            }
            String itemId = itemMap.get(itemKey);

            String quantityStr = quantityField.getText().trim();
            if (quantityStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter quantity!");
                return;
            }
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!");
                return;
            }

            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter title!");
                return;
            }

            String description = descriptionArea.getText().trim();

            // بررسی ظرفیت برای IN
            if (type == PermissionType.IN) {
                Warehouse warehouse = warehouseDAO.getWarehouseById(warehouseId);
                if (warehouse == null) {
                    JOptionPane.showMessageDialog(this, "Warehouse not found!");
                    return;
                }

                Inventory inventory = inventoryDAO.getInventory(warehouseId, itemId);
                int realStock = (inventory != null) ? inventory.getRealStock() : 0;
                int incomingStock = (inventory != null) ? inventory.getIncomingStock() : 0;
                int totalCurrent = realStock + incomingStock;

                int pendingIncoming = permissionDAO.getTotalQuantityByWarehouseAndItemAndType(
                        warehouseId, itemId, PermissionType.IN, PermissionStatus.ISSUED
                );
                pendingIncoming -= permission.getQuantity();

                int totalAfterAdd = totalCurrent + pendingIncoming + quantity;

                if (totalAfterAdd > warehouse.getCapacity()) {
                    JOptionPane.showMessageDialog(this,
                            String.format(
                                    "Warehouse capacity exceeded!\nCapacity: %d\nTotal: %d",
                                    warehouse.getCapacity(),
                                    totalAfterAdd
                            ),
                            "Capacity Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }

            // بررسی موجودی برای OUT
            if (type == PermissionType.OUT) {
                Inventory inventory = inventoryDAO.getInventory(warehouseId, itemId);
                if (inventory == null) {
                    JOptionPane.showMessageDialog(this,
                            "Item does not exist in this warehouse!",
                            "Stock Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                int realStock = inventory.getRealStock();
                int reservedStock = inventory.getReservedStock();

                int pendingOutgoing = permissionDAO.getTotalQuantityByWarehouseAndItemAndType(
                        warehouseId, itemId, PermissionType.OUT, PermissionStatus.ISSUED
                );
                pendingOutgoing -= permission.getQuantity();

                int availableStock = realStock - reservedStock - pendingOutgoing;

                if (availableStock < quantity) {
                    JOptionPane.showMessageDialog(this,
                            String.format(
                                    "Insufficient stock!\nAvailable: %d\nRequested: %d",
                                    availableStock,
                                    quantity
                            ),
                            "Stock Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }

            // به‌روزرسانی
            permission.setType(type);
            permission.setWarehouseId(warehouseId);
            permission.setItemId(itemId);
            permission.setQuantity(quantity);
            permission.setTitle(title);
            permission.setDescription(description);

            permissionDAO.updatePermission(permission);

            JOptionPane.showMessageDialog(this,
                    "Permission updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}