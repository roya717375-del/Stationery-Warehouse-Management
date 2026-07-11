package DAO;

import model.Permission;
import model.PermissionStatus;
import model.PermissionType;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface PermissionDAO {
    void addPermission(Permission permission) throws SQLException;
    Permission getPermissionById(String permissionId) throws SQLException;
    List<Permission> getPermissionsByWarehouse(String warehouseId) throws SQLException;
    List<Permission> getPermissionsByWarehouseAndStatus(String warehouseId, PermissionStatus status) throws SQLException;
    void updatePermissionStatus(String permissionId, PermissionStatus status) throws SQLException;
    void updatePermission(Permission permission) throws SQLException;
    void deletePermission(String permissionId) throws SQLException;
    int getTotalQuantityByWarehouseAndItemAndType(String warehouseId, String itemId, PermissionType type, PermissionStatus status) throws SQLException;
    List<Permission> searchPermissions(String warehouseId, String id, String type, String itemName, String date, PermissionStatus status) throws SQLException;
}