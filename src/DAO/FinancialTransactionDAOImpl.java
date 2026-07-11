package DAO;

import model.FinancialTransaction;
import model.TransactionType;
import util.IdGenerator;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinancialTransactionDAOImpl implements FinancialTransactionDAO {
    private Connection connection;
    private CashBalanceDAO cashBalanceDAO;

    public FinancialTransactionDAOImpl(Connection connection) {
        this.connection = connection;
        this.cashBalanceDAO = new CashBalanceDAOImpl(connection);
    }

    @Override
    public synchronized void addTransaction(FinancialTransaction transaction) throws SQLException {
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
            transaction.setTransactionId(IdGenerator.generateTransactionId());
        }

        String sql = "INSERT INTO FinancialTransactions (transaction_id, permission_id, type, amount, transaction_date, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getPermissionId());
            pstmt.setString(3, transaction.getType().name());
            pstmt.setBigDecimal(4, transaction.getAmount());
            pstmt.setString(5, transaction.getTransactionDate().toString());
            pstmt.setString(6, transaction.getDescription());
            pstmt.executeUpdate();
        }

        if (transaction.getType() == TransactionType.PURCHASE) {
            cashBalanceDAO.subtractAmount(transaction.getAmount());
        } else if (transaction.getType() == TransactionType.SALE) {
            cashBalanceDAO.addAmount(transaction.getAmount());
        }
    }

    @Override
    public synchronized void deleteTransactionByPermissionId(String permissionId) throws SQLException {
        String getSql = "SELECT type, amount FROM FinancialTransactions WHERE permission_id = ?";
        TransactionType type = null;
        BigDecimal amount = BigDecimal.ZERO;
        try (PreparedStatement pstmt = connection.prepareStatement(getSql)) {
            pstmt.setString(1, permissionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                type = TransactionType.valueOf(rs.getString("type"));
                amount = rs.getBigDecimal("amount");
            }
        }

        if (type != null) {
            if (type == TransactionType.PURCHASE) {
                cashBalanceDAO.addAmount(amount);
            } else if (type == TransactionType.SALE) {
                cashBalanceDAO.subtractAmount(amount);
            }
        }

        String sql = "DELETE FROM FinancialTransactions WHERE permission_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, permissionId);
            pstmt.executeUpdate();
        }
    }


    @Override
    public synchronized List<FinancialTransaction> getTransactionsByPermission(String permissionId) throws SQLException {
        List<FinancialTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM FinancialTransactions WHERE permission_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, permissionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }



    @Override
    public synchronized List<FinancialTransaction> searchTransactions(String id, String permissionId, String type, String description) throws SQLException {
        List<FinancialTransaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM FinancialTransactions WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (id != null && !id.trim().isEmpty()) {
            sql.append(" AND transaction_id LIKE ?");
            params.add("%" + id.trim() + "%");
        }
        if (permissionId != null && !permissionId.trim().isEmpty()) {
            sql.append(" AND permission_id LIKE ?");
            params.add("%" + permissionId.trim() + "%");
        }
        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type.trim().toUpperCase());
        }
        if (description != null && !description.trim().isEmpty()) {
            sql.append(" AND description LIKE ?");
            params.add("%" + description.trim() + "%");
        }

        sql.append(" ORDER BY transaction_date DESC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    private FinancialTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        FinancialTransaction transaction = new FinancialTransaction(
                rs.getString("permission_id"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getBigDecimal("amount"),
                rs.getString("description")
        );
        transaction.setTransactionId(rs.getString("transaction_id"));
        transaction.setTransactionDate(LocalDate.parse(rs.getString("transaction_date")));
        return transaction;
    }
}