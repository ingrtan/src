package Loader;

import javax.swing.JTable;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Parser {

    public void save(String fileName, boolean boolValue, int intValue, HashMap<String, JTable> tables) {
        
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            DataWrapper data = new DataWrapper(boolValue, intValue, tables);
            objectOut.writeObject(data);
            objectOut.close();
            fileOut.close();
            System.out.println("Data saved successfully to " + fileName);
        } catch (Exception e) {
            System.out.println("An error occurred while saving the data: " + e.getMessage());
        }
    }

    public DataWrapper load(String fileName) {
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            System.out.println("1");
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            System.out.println("2");
            DataWrapper object = (DataWrapper)objectIn.readObject();
            System.out.println("3");
            objectIn.close();
            fileIn.close();
            System.out.println("Data loaded successfully from " + fileName);
            System.out.println("Data: " + ((DataWrapper)object).toString());
            return (DataWrapper) object;
        } catch (Exception e) {
            System.out.println("An error occurred while loading the data: " + e.getMessage());
            return null;
        }
    }
}
