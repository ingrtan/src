package Loader;

import javax.swing.JTable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {

    /**
     * Saves the data to the given file
     * @param fileName
     * @param boolValue
     * @param intValue
     * @param tables
     */
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

    /**
     * Loads the data from the given file
     * @param fileName
     * @return
     */
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
            return (DataWrapper) object;
        } catch (Exception e) {
            System.out.println("An error occurred while loading the data: " + e.getMessage());
            return null;
        }
    }

    private Rules[] textToRules (String text){
        String[] lines = text.split("\n");
        Integer numberOfTapes = Integer.parseInt(lines[0].split(":")[1]);
        ArrayList<Rules> rules = new ArrayList<>();
        for(int i = 1; i < lines.length; i+=numberOfTapes+3){
            String state = lines[i];
            String stateToMove = lines[i+1];
            String[] symbolToRead = new String[numberOfTapes];
            String[] symbolToWrite = new String[numberOfTapes];
            String[] direction = new String[numberOfTapes];
            for(int j = 0; j < numberOfTapes; j++){
                symbolToRead[j] = lines[i+2+j].split(";")[0];
                symbolToWrite[j] = lines[i+2+j].split(";")[1];
                direction[j] = lines[i+2+j].split(";")[2];
            }
            rules.add(new Rules(state, stateToMove, symbolToRead, symbolToWrite, direction));
        }
        return rules.toArray(new Rules[rules.size()]);
    }

    public HashMap<String, JTable> textToJTableHashMap(String text){
        Rules[] rules = textToRules(text);
        HashMap<String, RuleMatrix> ruleMatrixHashMap = new HashMap<>();
        for(int i = 0; i < rules.length; i++){
            if(!ruleMatrixHashMap.containsKey(rules[i].getState())){
                ruleMatrixHashMap.put(rules[i].getState(), new RuleMatrix());
            }
            ruleMatrixHashMap.get(rules[i].getState()).addRow(rules[i].getStateToMove(), rules[i].getSymbolToRead(), rules[i].getSymbolToWrite(), rules[i].getDirection());
        }
        HashMap<String, JTable> jTableHashMap = new HashMap<>();
        ruleMatrixHashMap.forEach((key, value) -> jTableHashMap.put(key, value.toJTable()));
        return jTableHashMap;
    }
}
