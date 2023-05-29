package Loader;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class SerializableTableModel implements Serializable {
    private Vector<Vector<Object>> data;
    private Vector<Object> columnNames;

    public SerializableTableModel(DefaultTableModel model) {
        this.data = model.getDataVector();
        int columnCount = model.getColumnCount();
        for(int i = 0; i < columnCount; i++) {
            columnNames.add(model.getColumnName(i));
        }
    }

    public DefaultTableModel createTableModel() {
        return new DefaultTableModel(data, columnNames);
    }
}