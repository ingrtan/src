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

        public DataWrapper(boolean boolValue, int intValue, HashMap<String, JTable> jTableHashMap) {
            this.boolValue = boolValue;
            this.intValue = intValue;
            this.tables = new ArrayList<>();
            this.names = new ArrayList<>();
            jTableHashMap.forEach((key, value) -> this.tables.add(new SerializableTable(value)));
            jTableHashMap.forEach((key, value) -> this.names.add(key));
        }

        public boolean isBoolValue() {
            return boolValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public HashMap<String, JTable> getTables() {
            HashMap<String, JTable> jTableHashMap = new HashMap<>();
            for(int i = this.tables.size()-1; i >= 0; i--){
                jTableHashMap.put(this.names.get(i), this.tables.get(i).toJTable());
            }
            return jTableHashMap;
        }

        public String toString(){
            return "boolValue: " + this.boolValue + "\nintValue: " + this.intValue + "\n" + this.tables.toString() + "\n" + this.names.toString();
        }
}