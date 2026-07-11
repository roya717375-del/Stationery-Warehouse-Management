package model;
import util.IdGenerator;
public class Category {
    private String categoryId;
    private String name;
    private String parentId;

    public Category() {
    }

    public Category(String name) {
        this.categoryId = IdGenerator.generateCategoryId();
        this.name = name;
    }

    public Category(String name, String parentId) {
        this.categoryId=IdGenerator.generateCategoryId();
        this.name = name;
        this.parentId = parentId;

    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }


    @Override
    public String toString() {
        return "Category{" +
                "categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", parentId='" + parentId +
                '}';
    }
}