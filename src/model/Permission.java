package model;
import util.IdGenerator;

import java.time.LocalDate;


public class Permission {
    private String permissionId;
    private PermissionType type;
    private String warehouseId;
    private String itemId;
    private int quantity;
    private String title;
    private String description;
    private PermissionStatus status;
    private String permissionDate;
    private String confirmedDate;

    public Permission() {}

    public Permission(PermissionType type, String warehouseId, String itemId,
                      int quantity, String title, String description) {
        this.permissionId = IdGenerator.generatePermissionId();
        this.type = type;
        this.warehouseId = warehouseId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.title = title;
        this.description = description;
        this.status = PermissionStatus.ISSUED;
        this.permissionDate = LocalDate.now().toString();
    }

    public String getPermissionId() { return permissionId; }
    public void setPermissionId(String permissionId) { this.permissionId = permissionId; }
    public PermissionType getType() { return type; }
    public void setType(PermissionType type) { this.type = type; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PermissionStatus getStatus() { return status; }
    public void setStatus(PermissionStatus status) { this.status = status; }

    public String getPermissionDate() {
        return permissionDate;
    }

    public void setPermissionDate(String permissionDate) {
        this.permissionDate = permissionDate;
    }

    public String getConfirmedDate() {
        return confirmedDate;
    }

    public void setConfirmedDate(String confirmedDate) {
        this.confirmedDate = confirmedDate;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "permissionId='" + permissionId + '\'' +
                ", type=" + type +
                ", warehouseId='" + warehouseId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", quantity=" + quantity +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}

