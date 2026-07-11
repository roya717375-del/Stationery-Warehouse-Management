package DAO;

import model.Item;
import java.sql.SQLException;
import java.util.List;

public interface ItemDAO {
    void addItem(Item item) throws SQLException;
    Item getItemById(String itemId) throws SQLException;
    List<Item> getAllItems() throws SQLException;
    List<Item> getItemsByCategory(String categoryId) throws SQLException;
    void updateItem(Item item) throws SQLException;
    void deleteItem(String itemId) throws SQLException;
    boolean itemCodeExists(String itemCode) throws SQLException;
    List<Item> searchItems(String id, String code, String name, String price) throws SQLException;
}