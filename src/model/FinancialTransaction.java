package model;

import util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialTransaction {
    private String transactionId;
    private String permissionId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;

    public FinancialTransaction() {
    }

    public FinancialTransaction(String permissionId, TransactionType type, BigDecimal amount, String description) {
        this.transactionId = IdGenerator.generateTransactionId();
        this.permissionId = permissionId;
        this.type = type;
        this.amount = amount;
        this.transactionDate = LocalDate.now();
        this.description=description;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getPermissionId() { return permissionId; }
    public void setPermissionId(String permissionId) { this.permissionId = permissionId; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "FinancialTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", permissionId='" + permissionId + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", transactionDate=" + transactionDate +
                ", description=" + description +
                '}';
    }
}

