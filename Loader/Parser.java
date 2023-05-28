package Loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JTable;

public class Parser {

    public Parser(){
    }

    public static void save(String filename, HashMap<String, JTable> data) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filename + ".tgt");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(convertToSerializableJTable(data));
            objectOut.close();
            fileOut.close();
            System.out.println("Data saved successfully to " + filename + ".tgt");
        } catch (Exception e) {
            System.out.println("An error occurred while saving the data: " + e.getMessage());
        }
    }

    public static HashMap<String, JTable> load(String filename) {
        HashMap<String, SerializableJTable> convertedData = null;

        try {
            FileInputStream fileIn = new FileInputStream(filename + ".tgt");
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            convertedData = (HashMap<String, SerializableJTable>) objectIn.readObject();
            objectIn.close();
            fileIn.close();
            System.out.println("Data loaded successfully from " + filename);
        } catch (Exception e) {
            System.out.println("An error occurred while loading the data: " + e.getMessage());
        }

        return convertToJTable(convertedData);
    }

    public static HashMap<String, SerializableJTable> convertToSerializableJTable(HashMap<String, JTable> originalMap) {
        HashMap<String, SerializableJTable> convertedMap = new HashMap<>();
        for (String key : originalMap.keySet()) {
            JTable originalTable = originalMap.get(key);
            SerializableJTable serializableTable = new SerializableJTable();
            convertedMap.put(key, serializableTable);
        }
        return convertedMap;
    }

    public static HashMap<String, JTable> convertToJTable(HashMap<String, SerializableJTable> convertedMap) {
        HashMap<String, JTable> originalMap = new HashMap<>();
        for (String key : convertedMap.keySet()) {
            SerializableJTable serializableTable = convertedMap.get(key);
            JTable originalTable = new JTable();
            // Copy relevant properties or data from the SerializableJTable to the original JTable
            // For example, you might copy column names, cell values, or other table properties.

            originalMap.put(key, originalTable);
        }
        return originalMap;
    }

}
