package Loader;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTable;

public class DataWrapper implements Serializable {
        private boolean boolValue;
        private int intValue;
        private ArrayList<String> names;
        private ArrayList<SerializableTable> tables;

        /**
         * Constructor for DataWrapper
         * @param boolValue
         * @param intValue
         * @param jTableHashMap
         */
        public DataWrapper(boolean boolValue, int intValue, HashMap<String, JTable> jTableHashMap) {
            this.boolValue = boolValue;
            this.intValue = intValue;
            this.tables = new ArrayList<>();
            this.names = new ArrayList<>();
            jTableHashMap.forEach((key, value) -> this.tables.add(new SerializableTable(value)));
            jTableHashMap.forEach((key, value) -> this.names.add(key));
        }

        /**
         * Returns the boolean value
         * @return
         */
        public boolean isBoolValue() {
            return boolValue;
        }

        /**
         * Returns the int value
         * @return
         */
        public int getIntValue() {
            return intValue;
        }

        /**
         * Returns the tables as a HashMap
         * @return
         */
        public HashMap<String, JTable> getTables() {
            HashMap<String, JTable> jTableHashMap = new HashMap<>();
            for(int i = this.tables.size()-1; i >= 0; i--){
                jTableHashMap.put(this.names.get(i), this.tables.get(i).toJTable());
            }
            return jTableHashMap;
        }

        /**
         * Returns the string representation of the DataWrapper
         */
        public String toString(){
            StringBuilder returningValue = new StringBuilder();
            returningValue.append("boolValue: ").append(this.boolValue).append("\nintValue: ").append(this.intValue).append("\n");
            for(int i = 0; i < this.tables.size(); i++){
                returningValue.append("Table ").append(this.names.get(i)).append(":\n").append(this.tables.get(i).toString()).append("\n");
            }
            return returningValue.toString();
        }
}