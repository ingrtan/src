package Loader;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SerializableTable {
    private Object[][] data;
    private Object[] columnNames;

    // Construct from a JTable
    public SerializableTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        int columnCount = model.getColumnCount();
        data = new Object[rowCount][columnCount];
        columnNames = new Object[columnCount];

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                data[i][j] = model.getValueAt(i, j);
            }
        }

        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = model.getColumnName(i);
        }
    }

    public JTable toJTable() {
        return new JTable(new DefaultTableModel(data, columnNames));
    }
}