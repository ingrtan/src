package Loader;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Parser {

    public void save(String fileName, boolean boolValue, int intValue, HashMap<String, JTable> tables) {
        try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(Files.newOutputStream(Paths.get(fileName))))) {
            encoder.writeObject(new DataWrapper(boolValue, intValue, tables));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataWrapper load(String fileName) {
        try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(Files.newInputStream(Paths.get(fileName))))) {
            return (DataWrapper) decoder.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
