package DAO;

import model.FinancialTransaction;
import model.TransactionType;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface FinancialTransactionDAO {
    void addTransaction(FinancialTransaction transaction) throws SQLException;
    void deleteTransactionByPermissionId(String permissionId) throws SQLException;

    List<FinancialTransaction> getTransactionsByPermission(String permissionId) throws SQLException;

    List<FinancialTransaction> searchTransactions(String id, String permissionId, String type, String description) throws SQLException;
}