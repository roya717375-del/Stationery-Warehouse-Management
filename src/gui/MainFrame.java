package gui;

import model.*;
import DAO.*;
import db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MainFrame extends JFrame {
    private Connection connection;
    private WarehouseDAO warehouseDAO;
    private CashBalanceDAO cashBalanceDAO;

    private JList<String> warehouseList;
    private DefaultListModel<String> warehouseListModel;
    private JTextField whIdSearch, whNameSearch, whAddressSearch, whCapacitySearch;
    private JTextArea warehouseDetailsArea;
    private JLabel cashBalanceLabel;

    public MainFrame() {
        initDatabase();
        initUI();
        setTitle("Warehouse Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setVisible(true);
        loadWarehouses();
        updateCashBalance();
    }

    private void initDatabase() {
        try {
            connection = DatabaseManager.getConnection();
            warehouseDAO = new WarehouseDAOImpl(connection);
            cashBalanceDAO = new CashBalanceDAOImpl(connection);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Warehouses"));

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("ID:"));
        whIdSearch = new JTextField();
        whIdSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchWarehouses();
            }
        });
        searchPanel.add(whIdSearch);

        searchPanel.add(new JLabel("Name:"));
        whNameSearch = new JTextField();
        whNameSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchWarehouses();
            }
        });
        searchPanel.add(whNameSearch);

        searchPanel.add(new JLabel("Address:"));
        whAddressSearch = new JTextField();
        whAddressSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchWarehouses();
            }
        });
        searchPanel.add(whAddressSearch);

        searchPanel.add(new JLabel("Capacity:"));
        whCapacitySearch = new JTextField();
        whCapacitySearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchWarehouses();
            }
        });
        searchPanel.add(whCapacitySearch);

        listPanel.add(searchPanel, BorderLayout.NORTH);

        warehouseListModel = new DefaultListModel<>();
        warehouseList = new JList<>(warehouseListModel);
        warehouseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        warehouseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showWarehouseDetails();
            }
        });

        warehouseList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openWarehouseDetail();
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(warehouseList);
        listScroll.setPreferredSize(new Dimension(400, 350));
        listPanel.add(listScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("+ Add");
        addBtn.addActionListener(e -> showAddWarehouseDialog());
        buttonPanel.add(addBtn);

        JButton editBtn = new JButton("✎ Edit");
        editBtn.addActionListener(e -> editWarehouse());
        buttonPanel.add(editBtn);

        JButton deleteBtn = new JButton("× Delete");
        deleteBtn.addActionListener(e -> deleteWarehouse());
        buttonPanel.add(deleteBtn);

        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(listPanel, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Warehouse Details"));
        detailsPanel.setPreferredSize(new Dimension(400, 180));

        warehouseDetailsArea = new JTextArea();
        warehouseDetailsArea.setEditable(false);
        warehouseDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailsScroll = new JScrollPane(warehouseDetailsArea);
        detailsPanel.add(detailsScroll, BorderLayout.CENTER);

        JPanel cashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cashPanel.setBorder(BorderFactory.createTitledBorder("Cash Balance"));
        cashBalanceLabel = new JLabel("Loading...");
        cashBalanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cashPanel.add(cashBalanceLabel);
        detailsPanel.add(cashPanel, BorderLayout.SOUTH);

        mainPanel.add(detailsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        JLabel instruction = new JLabel("Click a warehouse to view details, double-click to open", SwingConstants.CENTER);
        instruction.setFont(new Font("Arial", Font.ITALIC, 12));
        add(instruction, BorderLayout.SOUTH);
    }

    private void searchWarehouses() {
        warehouseListModel.clear();
        try {
            List<Warehouse> warehouses = warehouseDAO.searchWarehouses(
                    whIdSearch.getText(),
                    whNameSearch.getText(),
                    whAddressSearch.getText(),
                    whCapacitySearch.getText()
            );
            for (Warehouse w : warehouses) {
                warehouseListModel.addElement(w.getWarehouseId() + " - " + w.getName());
            }
            if (warehouseListModel.size() > 0) {
                warehouseList.setSelectedIndex(0);
                showWarehouseDetails();
            } else {
                warehouseDetailsArea.setText("No warehouses found");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }

    private void loadWarehouses() {
        whIdSearch.setText("");
        whNameSearch.setText("");
        whAddressSearch.setText("");
        whCapacitySearch.setText("");
        warehouseListModel.clear();
        try {
            List<Warehouse> warehouses = warehouseDAO.getAllWarehouses();
            for (Warehouse w : warehouses) {
                warehouseListModel.addElement(w.getWarehouseId() + " - " + w.getName());
            }
            if (warehouseListModel.size() > 0) {
                warehouseList.setSelectedIndex(0);
                showWarehouseDetails();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading warehouses: " + e.getMessage());
        }
    }

    private void showWarehouseDetails() {
        int index = warehouseList.getSelectedIndex();
        if (index == -1) {
            warehouseDetailsArea.setText("Select a warehouse to view details");
            return;
        }

        String selected = warehouseListModel.get(index);
        String id = selected.split(" - ")[0];

        try {
            Warehouse w = warehouseDAO.getWarehouseById(id);
            if (w != null) {
                warehouseDetailsArea.setText(String.format(

                                "═══════════════════════════════════════\n" +
                                "  ID          : %s\n" +
                                "  Name        : %s\n" +
                                "  Address     : %s\n" +
                                "  Capacity    : %d\n" +
                                "═══════════════════════════════════════",
                        w.getWarehouseId(),
                        w.getName(),
                        w.getAddress() != null && !w.getAddress().isEmpty() ? w.getAddress() : "No Address",
                        w.getCapacity()
                ));
            }
        } catch (SQLException e) {
            warehouseDetailsArea.setText("Error loading warehouse details: " + e.getMessage());
        }
    }

    private void openWarehouseDetail() {
        int index = warehouseList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Please select a warehouse first!");
            return;
        }

        String selected = warehouseListModel.get(index);
        String warehouseId = selected.split(" - ")[0];

        try {
            Warehouse warehouse = warehouseDAO.getWarehouseById(warehouseId);
            if (warehouse != null) {
                WarehouseDetailFrame detailFrame = new WarehouseDetailFrame(this, warehouse);
                detailFrame.setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error opening warehouse: " + e.getMessage());
        }
    }

    private void showAddWarehouseDialog() {
        WarehouseDialog dialog = new WarehouseDialog(this, warehouseDAO);
        dialog.setVisible(true);
        loadWarehouses();
        updateCashBalance();
    }

    private void editWarehouse() {
        int index = warehouseList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Select a warehouse first!");
            return;
        }
        try {
            String selected = warehouseListModel.get(index);
            String id = selected.split(" - ")[0];
            Warehouse w = warehouseDAO.getWarehouseById(id);
            if (w != null) {
                WarehouseDialog dialog = new WarehouseDialog(this, warehouseDAO, w);
                dialog.setVisible(true);
                loadWarehouses();
                updateCashBalance();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error editing warehouse: " + e.getMessage());
        }
    }

    private void deleteWarehouse() {
        int index = warehouseList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Select a warehouse first!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this warehouse?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String selected = warehouseListModel.get(index);
                String id = selected.split(" - ")[0];
                warehouseDAO.deleteWarehouse(id);
                loadWarehouses();
                updateCashBalance();
                warehouseDetailsArea.setText("");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting warehouse: " + e.getMessage());
            }
        }
    }

    private void updateCashBalance() {
        try {
            CashBalance cb = cashBalanceDAO.getCurrentBalance();
            cashBalanceLabel.setText(String.format("%,d IRR", cb.getBalance().longValue()));
        } catch (SQLException e) {
            cashBalanceLabel.setText("Error loading balance");
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateCashBalance();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame();
        });
    }
}