package Loader;

import java.util.ArrayList;

import javax.swing.JTable;

public class RuleMatrix {
    private ArrayList<String[]> matrix;

    /**
     * Constructor for RuleMatrix
     */
    public RuleMatrix() {
        this.matrix = new ArrayList<>();
    }

    /**
     * Adds a row to the matrix
     * @param row
     */
    public void addRow(String[] row){
        this.matrix.add(row);
    }

    /**
     * Returns the matrix
     * @return
     */
    public ArrayList<String[]> getMatrix() {
        return matrix;
    }

    public void addRow(String stateToMove, String[] symbolToRead, String[] symbolToWrite, String[] direction){
        ArrayList<String> row = new ArrayList<>();
        row.add(stateToMove);
        for(int i = 0; i < symbolToRead.length; i++){
            row.add(symbolToRead[i]);
            row.add(symbolToWrite[i]);
            row.add(direction[i]);
        }
    }

    public JTable toJTable(){
        String[] columnNames = new String[this.matrix.get(0).length];
        for(int i = 0; i < columnNames.length; i++){
            columnNames[i] = "Tape " + (i+1);
        }
        String[][] data = new String[this.matrix.size()][this.matrix.get(0).length];
        for(int i = 0; i < data.length; i++){
            data[i] = this.matrix.get(i);
        }
        return new JTable(data, columnNames);
    }
}
