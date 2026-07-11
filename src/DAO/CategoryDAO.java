package DAO;

import model.Category;
import java.sql.SQLException;
import java.util.List;

public interface CategoryDAO {
    void addCategory(Category category) throws SQLException;
    Category getCategoryById(String categoryId) throws SQLException;
    List<Category> getAllCategories() throws SQLException;
    List<Category> getSubCategories(String parentId) throws SQLException;
    void updateCategory(Category category) throws SQLException;
    void deleteCategory(String categoryId) throws SQLException;
    boolean categoryExists(String categoryId) throws SQLException;
    int getItemCountInCategory(String categoryId) throws SQLException;
    List<Category> searchCategories(String id, String name, String parentId) throws SQLException;
}