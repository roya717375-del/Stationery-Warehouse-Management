import db.DatabaseManager;
import gui.MainFrame;
import util.IdGenerator;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.setupDatabase();


            IdGenerator.syncWithDatabase(DatabaseManager.getConnection());

            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new MainFrame();
            });

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}