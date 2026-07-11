package DAO;

import model.Category;
import util.IdGenerator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {
    private Connection connection;

    public CategoryDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized void addCategory(Category category) throws SQLException {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new SQLException("Category name cannot be empty");
        }

        String checkSql = "SELECT COUNT(*) FROM Categories WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setString(1, category.getName());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Category name already exists: " + category.getName());
            }
        }

        if (category.getCategoryId() == null || category.getCategoryId().isEmpty()) {
            category.setCategoryId(IdGenerator.generateCategoryId());
        }

        String sql = "INSERT INTO Categories (category_id, name, parent_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryId());
            pstmt.setString(2, category.getName());
            pstmt.setString(3, category.getParentId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized Category getCategoryById(String categoryId) throws SQLException {
        String sql = "SELECT * FROM Categories WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Category category = new Category(rs.getString("name"));
                category.setCategoryId(rs.getString("category_id"));
                category.setParentId(rs.getString("parent_id"));
                return category;
            }
        }
        return null;
    }

    @Override
    public synchronized List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories ORDER BY category_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category category = new Category(rs.getString("name"));
                category.setCategoryId(rs.getString("category_id"));
                category.setParentId(rs.getString("parent_id"));
                categories.add(category);
            }
        }
        return categories;
    }

    @Override
    public synchronized List<Category> getSubCategories(String parentId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories WHERE parent_id = ? ORDER BY category_id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, parentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Category category = new Category(rs.getString("name"));
                category.setCategoryId(rs.getString("category_id"));
                category.setParentId(rs.getString("parent_id"));
                categories.add(category);
            }
        }
        return categories;
    }

    @Override
    public synchronized void updateCategory(Category category) throws SQLException {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new SQLException("Category name cannot be empty");
        }

        String checkSql = "SELECT COUNT(*) FROM Categories WHERE name = ? AND category_id != ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getCategoryId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Category name already exists: " + category.getName());
            }
        }

        String sql = "UPDATE Categories SET name = ?, parent_id = ? WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getParentId());
            pstmt.setString(3, category.getCategoryId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized void deleteCategory(String categoryId) throws SQLException {
        List<Category> subCategories = getSubCategories(categoryId);
        if (!subCategories.isEmpty()) {
            throw new SQLException("Cannot delete a category with subcategories.");
        }

        String checkItemSql = "SELECT COUNT(*) FROM Items WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkItemSql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete a category with items.");
            }
        }

        String sql = "DELETE FROM Categories WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized boolean categoryExists(String categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Categories WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public synchronized int getItemCountInCategory(String categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Items WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public synchronized List<Category> searchCategories(String id, String name, String parentId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Categories WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (id != null && !id.trim().isEmpty()) {
            sql.append(" AND category_id LIKE ?");
            params.add("%" + id.trim() + "%");
        }
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        if (parentId != null && !parentId.trim().isEmpty()) {
            sql.append(" AND parent_id LIKE ?");
            params.add("%" + parentId.trim() + "%");
        }

        sql.append(" ORDER BY category_id");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Category category = new Category(rs.getString("name"));
                category.setCategoryId(rs.getString("category_id"));
                category.setParentId(rs.getString("parent_id"));
                categories.add(category);
            }
        }
        return categories;
    }
}