package gui;

import model.Warehouse;
import DAO.WarehouseDAO;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class WarehouseDialog extends JDialog {
    private WarehouseDAO warehouseDAO;
    private JTextField nameField;
    private JTextField addressField;
    private JTextField capacityField;
    private boolean isEdit;
    private String warehouseId;

    public WarehouseDialog(Frame owner, WarehouseDAO warehouseDAO) {
        super(owner, "New Warehouse", true);
        this.warehouseDAO = warehouseDAO;
        this.isEdit = false;
        initComponents();
        setSize(400, 300);
        setLocationRelativeTo(owner);
    }

    public WarehouseDialog(Frame owner, WarehouseDAO warehouseDAO, Warehouse warehouse) {
        super(owner, "Edit Warehouse", true);
        this.warehouseDAO = warehouseDAO;
        this.isEdit = true;
        this.warehouseId = warehouse.getWarehouseId();
        initComponents();
        loadWarehouseData(warehouse);
        setSize(400, 300);
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
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressField = new JTextField(20);
        formPanel.add(addressField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        capacityField = new JTextField(20);
        formPanel.add(capacityField, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(isEdit ? "Update" : "Save");
        saveButton.addActionListener(e -> saveWarehouse());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadWarehouseData(Warehouse warehouse) {
        nameField.setText(warehouse.getName());
        addressField.setText(warehouse.getAddress());
        capacityField.setText(String.valueOf(warehouse.getCapacity()));
    }

    private void saveWarehouse() {
        try {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String capacityStr = capacityField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                return;
            }

            if (capacityStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Capacity cannot be empty!");
                return;
            }

            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                JOptionPane.showMessageDialog(this, "Capacity must be greater than 0!");
                return;
            }

            if (address.isEmpty()) address = null;

            Warehouse warehouse = new Warehouse(name, address, capacity);

            if (isEdit) {
                warehouse.setWarehouseId(warehouseId);
                warehouseDAO.updateWarehouse(warehouse);
                JOptionPane.showMessageDialog(this, "Warehouse updated successfully!");
            } else {
                warehouseDAO.addWarehouse(warehouse);
                JOptionPane.showMessageDialog(this, "Warehouse added successfully!\nID: " + warehouse.getWarehouseId());
            }

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for capacity!");
        } catch (SQLException e) {
            if (e.getMessage().contains("already exists")) {
                JOptionPane.showMessageDialog(this, "Warehouse name already exists!");
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }
        }
    }
}