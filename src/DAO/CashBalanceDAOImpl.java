package DAO;

import model.CashBalance;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class CashBalanceDAOImpl implements CashBalanceDAO {
    private Connection connection;

    public CashBalanceDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized CashBalance getCurrentBalance() throws SQLException {
        String sql = "SELECT balance, last_updated FROM CashBalance LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                CashBalance cb = new CashBalance();
                cb.setBalance(rs.getBigDecimal("balance"));
                cb.setLastUpdated(rs.getString("last_updated"));
                return cb;
            }
        }

        CashBalance initial = new CashBalance(BigDecimal.ZERO);
        String insertSql = "INSERT INTO CashBalance (balance, last_updated) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            pstmt.setBigDecimal(1, initial.getBalance());
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.executeUpdate();
        }
        return initial;
    }

    @Override
    public synchronized void updateBalance(BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE CashBalance SET balance = ?, last_updated = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.executeUpdate();
        }
    }

    @Override
    public synchronized void addAmount(BigDecimal amount) throws SQLException {
        CashBalance current = getCurrentBalance();
        BigDecimal newBalance = current.getBalance().add(amount);
        updateBalance(newBalance);
    }

    @Override
    public synchronized void subtractAmount(BigDecimal amount) throws SQLException {
        CashBalance current = getCurrentBalance();
        BigDecimal newBalance = current.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Insufficient cash balance!");
        }
        updateBalance(newBalance);
    }
}