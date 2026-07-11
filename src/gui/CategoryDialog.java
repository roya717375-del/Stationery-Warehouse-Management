package gui;

import model.Category;
import DAO.CategoryDAO;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class CategoryDialog extends JDialog {
    private CategoryDAO categoryDAO;
    private JTextField nameField;
    private JTextField parentIdField;
    private boolean isEdit;
    private String categoryId;

    public CategoryDialog(Frame owner, CategoryDAO categoryDAO) {
        super(owner, "New Category", true);
        this.categoryDAO = categoryDAO;
        this.isEdit = false;
        initComponents();
        setSize(400, 250);
        setLocationRelativeTo(owner);
    }

    public CategoryDialog(Frame owner, CategoryDAO categoryDAO, Category category) {
        super(owner, "Edit Category", true);
        this.categoryDAO = categoryDAO;
        this.isEdit = true;
        this.categoryId = category.getCategoryId();
        initComponents();
        loadCategoryData(category);
        setSize(400, 250);
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
        formPanel.add(new JLabel("Parent ID:"), gbc);
        gbc.gridx = 1;
        parentIdField = new JTextField(20);
        formPanel.add(parentIdField, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(isEdit ? "Update" : "Save");
        saveButton.addActionListener(e -> saveCategory());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCategoryData(Category category) {
        nameField.setText(category.getName());
        if (category.getParentId() != null) {
            parentIdField.setText(category.getParentId());
        }
    }

    private void saveCategory() {
        try {
            String name = nameField.getText().trim();
            String parentId = parentIdField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                return;
            }

            Category category;
            if (parentId.isEmpty()) {
                category = new Category(name);
            } else {
                category = new Category(name, parentId);
            }

            if (isEdit) {
                category.setCategoryId(categoryId);
                categoryDAO.updateCategory(category);
                JOptionPane.showMessageDialog(this, "Category updated!");
            } else {
                categoryDAO.addCategory(category);
                JOptionPane.showMessageDialog(this, "Category added!");
            }

            dispose();

        } catch (SQLException e) {
            if (e.getMessage().contains("already exists")) {
                JOptionPane.showMessageDialog(this, "Category name already exists!");
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }
        }
    }
}