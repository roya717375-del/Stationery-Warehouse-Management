package gui;

import model.Item;
import model.Category;
import DAO.ItemDAO;
import DAO.CategoryDAO;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ItemDialog extends JDialog {
    private ItemDAO itemDAO;
    private CategoryDAO categoryDAO;
    private JTextField codeField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo;
    private JTextField priceField;
    private boolean isEdit;
    private String itemId;

    public ItemDialog(Frame owner, ItemDAO itemDAO, CategoryDAO categoryDAO) {
        super(owner, "New Item", true);
        this.itemDAO = itemDAO;
        this.categoryDAO = categoryDAO;
        this.isEdit = false;
        initComponents();
        loadCategories();
        setSize(500, 450);
        setLocationRelativeTo(owner);
    }

    public ItemDialog(Frame owner, ItemDAO itemDAO, CategoryDAO categoryDAO, Item item) {
        super(owner, "Edit Item", true);
        this.itemDAO = itemDAO;
        this.categoryDAO = categoryDAO;
        this.isEdit = true;
        this.itemId = item.getItemId();
        initComponents();
        loadCategories();
        loadItemData(item);
        setSize(500, 450);
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
        formPanel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1;
        codeField = new JTextField(20);
        formPanel.add(codeField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryCombo = new JComboBox<>();
        categoryCombo.setPreferredSize(new Dimension(300, 25));
        formPanel.add(categoryCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(20);
        formPanel.add(priceField, gbc);
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
        JButton saveButton = new JButton(isEdit ? "Update" : "Save");
        saveButton.addActionListener(e -> saveItem());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        try {
            categoryCombo.removeAllItems();
            categoryCombo.addItem("-- Select Category --");
            List<Category> categories = categoryDAO.getAllCategories();
            for (Category cat : categories) {
                categoryCombo.addItem(cat.getCategoryId() + " - " + cat.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private void loadItemData(Item item) {
        codeField.setText(item.getItemCode());
        nameField.setText(item.getName());
        priceField.setText(item.getUnitPrice().toString());
        descriptionArea.setText(item.getDescription());
        if (item.getCategoryId() != null) {
            categoryCombo.setSelectedItem(item.getCategoryId() + " - " + item.getCategoryId());
        }
    }

    private void saveItem() {
        try {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String categoryKey = (String) categoryCombo.getSelectedItem();
            String priceStr = priceField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter item code!");
                return;
            }

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter item name!");
                return;
            }

            if (categoryKey == null || categoryKey.equals("-- Select Category --")) {
                JOptionPane.showMessageDialog(this, "Please select a category!");
                return;
            }

            if (priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter unit price!");
                return;
            }

            String categoryId = categoryKey.split(" - ")[0];
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than 0!");
                return;
            }

            Item item = new Item(code, name, categoryId, price);
            item.setDescription(description);

            if (isEdit) {
                item.setItemId(itemId);
                itemDAO.updateItem(item);
                JOptionPane.showMessageDialog(this, "Item updated!");
            } else {
                if (itemDAO.itemCodeExists(code)) {
                    JOptionPane.showMessageDialog(this, "Item code already exists!");
                    return;
                }
                itemDAO.addItem(item);
                JOptionPane.showMessageDialog(this, "Item added!\nID: " + item.getItemId());
            }

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for price!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}