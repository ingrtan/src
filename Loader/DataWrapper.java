package Loader;

import java.io.Serializable;
import java.util.HashMap;
import javax.swing.JTable;

public class DataWrapper implements Serializable {
        private boolean boolValue;
        private int intValue;
        private HashMap<String, SerializableTable> tables;

        public DataWrapper(boolean boolValue, int intValue, HashMap<String, JTable> jTableHashMap) {
            this.boolValue = boolValue;
            this.intValue = intValue;
            this.tables = new HashMap<>();
            jTableHashMap.forEach((key, value) -> this.tables.put(key, new SerializableTable(value)));
        }

        public boolean isBoolValue() {
            return boolValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public HashMap<String, JTable> getTables() {
            HashMap<String, JTable> jTableHashMap = new HashMap<>();
            this.tables.forEach((key, value) -> jTableHashMap.put(key, value.toJTable()));
            return jTableHashMap;
        }
}