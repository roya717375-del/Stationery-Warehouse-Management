package gui;

import model.*;
import DAO.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionDialog extends JDialog {
    private PermissionDAO permissionDAO;
    private WarehouseDAO warehouseDAO;
    private ItemDAO itemDAO;
    private InventoryDAO inventoryDAO;

    private JComboBox<String> typeCombo;
    private JComboBox<String> warehouseCombo;
    private JComboBox<String> itemCombo;
    private JTextField quantityField;
    private JTextField titleField;
    private JTextArea descriptionArea;

    public PermissionDialog(Frame owner, PermissionDAO permissionDAO,
                            WarehouseDAO warehouseDAO, ItemDAO itemDAO,
                            InventoryDAO inventoryDAO) {
        super(owner, "New Permission", true);
        this.permissionDAO = permissionDAO;
        this.warehouseDAO = warehouseDAO;
        this.itemDAO = itemDAO;
        this.inventoryDAO = inventoryDAO;

        initComponents();
        loadData();
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

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[]{"IN", "OUT"});
        formPanel.add(typeCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Warehouse:"), gbc);
        gbc.gridx = 1;
        warehouseCombo = new JComboBox<>();
        warehouseCombo.setPreferredSize(new Dimension(300, 25));
        formPanel.add(warehouseCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Item:"), gbc);
        gbc.gridx = 1;
        itemCombo = new JComboBox<>();
        itemCombo.setPreferredSize(new Dimension(300, 25));
        formPanel.add(itemCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField();
        quantityField.setPreferredSize(new Dimension(300, 25));
        formPanel.add(quantityField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(300, 25));
        formPanel.add(titleField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
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
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> savePermission());
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
            warehouseCombo.addItem("-- Select Warehouse --");
            List<Warehouse> warehouses = warehouseDAO.getAllWarehouses();
            for (Warehouse w : warehouses) {
                warehouseCombo.addItem(w.getWarehouseId() + " - " + w.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading warehouses: " + e.getMessage());
        }

        try {
            itemCombo.removeAllItems();
            itemCombo.addItem("-- Select Item --");
            List<Item> items = itemDAO.getAllItems();
            for (Item item : items) {
                itemCombo.addItem(item.getItemId() + " - " + item.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }
    }

    private void savePermission() {
        try {
            String typeStr = (String) typeCombo.getSelectedItem();
            PermissionType type = PermissionType.valueOf(typeStr);

            String warehouseKey = (String) warehouseCombo.getSelectedItem();
            if (warehouseKey == null || warehouseKey.equals("-- Select Warehouse --")) {
                JOptionPane.showMessageDialog(this, "Please select a warehouse!");
                return;
            }
            String warehouseId = warehouseKey.split(" - ")[0];

            String itemKey = (String) itemCombo.getSelectedItem();
            if (itemKey == null || itemKey.equals("-- Select Item --")) {
                JOptionPane.showMessageDialog(this, "Please select an item!");
                return;
            }
            String itemId = itemKey.split(" - ")[0];

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

            Permission permission = new Permission(type, warehouseId, itemId, quantity, title, description);
            permissionDAO.addPermission(permission);

            JOptionPane.showMessageDialog(this,
                    "Permission added successfully!\nID: " + permission.getPermissionId(),
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