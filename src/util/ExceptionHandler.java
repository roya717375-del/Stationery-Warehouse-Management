package util;

import Exception.*;
import javax.swing.*;
import java.awt.*;

public class ExceptionHandler {

    public static void handleException(Exception e, Component parent) {
        String title = "Error";
        int messageType = JOptionPane.ERROR_MESSAGE;
        String message = e.getMessage();

        if (e instanceof CapacityExceededException) {
            title = "Capacity Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof InsufficientStockException) {
            title = "Stock Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof InsufficientCashException) {
            title = "Cash Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof ItemNotFoundException) {
            title = "Item Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof WarehouseNotFoundException) {
            title = "Warehouse Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof PermissionNotFoundException) {
            title = "Permission Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof InvalidPermissionException) {
            title = "Permission Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof DatabaseException) {
            title = "Database Error";
            messageType = JOptionPane.ERROR_MESSAGE;
        } else if (e instanceof java.sql.SQLException) {
            title = "Database Connection Error";
            messageType = JOptionPane.ERROR_MESSAGE;
        }

        if (message == null || message.isEmpty()) {
            message = "An unexpected error occurred.";
        }

        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }

    public static void handleException(Exception e) {
        String title = "Error";
        int messageType = JOptionPane.ERROR_MESSAGE;
        String message = e.getMessage();

        if (e instanceof CapacityExceededException) {
            title = "Capacity Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof InsufficientStockException) {
            title = "Stock Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof InsufficientCashException) {
            title = "Cash Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof ItemNotFoundException) {
            title = "Item Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof WarehouseNotFoundException) {
            title = "Warehouse Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof PermissionNotFoundException) {
            title = "Permission Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof InvalidPermissionException) {
            title = "Permission Error";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (e instanceof DatabaseException) {
            title = "Database Error";
            messageType = JOptionPane.ERROR_MESSAGE;
        } else if (e instanceof java.sql.SQLException) {
            title = "Database Connection Error";
            messageType = JOptionPane.ERROR_MESSAGE;
        }

        if (message == null || message.isEmpty()) {
            message = "An unexpected error occurred.";
        }

        JOptionPane.showMessageDialog(null, message, title, messageType);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}