package Loader;

import java.io.Serializable;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SerializableTable implements Serializable{
    private Object[][] data;
    private Object[] columnNames;

    /**
     * Constructor for SerializableTable
     * @param table
     */
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

    /**
     * Returns the SerializableTable as a JTable
     * @return
     */
    public JTable toJTable() {
        return new JTable(new DefaultTableModel(data, columnNames));
    }

    /**
     * Returns the string representation of the SerializableTable
     */
    public String toString(){
        return "data: " + this.data.toString() + "\ncolumnNames: " + this.columnNames.toString();
    }
}