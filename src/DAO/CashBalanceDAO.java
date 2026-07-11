package DAO;

import model.CashBalance;
import java.math.BigDecimal;
import java.sql.SQLException;

public interface CashBalanceDAO {
    CashBalance getCurrentBalance() throws SQLException;
    void updateBalance(BigDecimal newBalance) throws SQLException;
    void addAmount(BigDecimal amount) throws SQLException;
    void subtractAmount(BigDecimal amount) throws SQLException;
}