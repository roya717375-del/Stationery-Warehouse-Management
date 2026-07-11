package model;
import java.math.BigDecimal;

public class CashBalance {
    private BigDecimal balance;
    private String lastUpdated;

    public CashBalance() {
    }


    public CashBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    @Override
    public String toString() {
        return "CashBalance{" +
                ", balance=" + balance +
                '}';
    }
}