package Converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import Data.Head;
import Data.Rule;
import Data.Status;
import Loader.DataWrapper;
import Loader.RuleMatrix;
import Resources.Movement;
import Resources.WrongTableException;
 
public class ConverterMultiTread {
    protected int threadCount = 0;
    protected ArrayList<Status> choosableStatuses = new ArrayList<>();
    protected ArrayList<Status> moveStarterStatuses = new ArrayList<>();
    protected ArrayList<Object[][]> tablesData = new ArrayList<>();
    protected int columnNumber;
    protected ArrayList<ArrayList<String>> columnData = new ArrayList<>();
    protected Status chooserStatus = new Status("Chooser");;
    protected Status moveToMostLeft = new Status("MoveToMostLeft");
    protected ArrayList<ArrayList<String>> combinations = new ArrayList<>();
    protected HashMap<String,RuleMatrix> ruleMap;
    protected ArrayList<Status> statuses = new ArrayList<>();
    protected Head head;
    /**
    * Creates a new ConverterMultiTread object.
    */
    public ConverterMultiTread() {}
    
    /**
     * Converts the given tables to a list of Status objects.
     * @param tables The tables to convert.
     * @param statusList The list of status names.
     * @param threadCount The number of threads to use.
     * @return The list of Status objects.
     * @throws WrongTableException If the tables are invalid.
    */
    public Head convert(HashMap<String,RuleMatrix> ruleMap) throws WrongTableException{
        threadCount = threadCount-1;
        threadCount = threadCount/3;
        this.ruleMap = ruleMap;
        this.threadCount = threadCount;
        getDataFromTables();
        if(isThereWrongMovement()){
            throw new WrongTableException("Wrong Movement.");
        }
        if(isThereEmptyFirstElement()){
            throw new WrongTableException("Empty First Element.");
        }
        fillBlank();
        if(checkDuplicationAllTable()){
            throw new WrongTableException("Duplication in the tables.");
        }
        columnData = getCharacterlist();
        createCharactercombinations();
        choosableStatuses = createBaseStatuses();
        statuses.addAll(createMoverStatuses());
        createTheChooserStatus();
        createMoveToMostLeftStatus();
        statuses.add(chooserStatus);
        statuses.add(moveToMostLeft);
        statuses.addAll(moveStarterStatuses);
        statuses.addAll(choosableStatuses);
        statuses.addAll(createConvertStatuses());
        head.setStatuses(statuses);
        return head;
    }

    /**
     * Creates an ArrayList of String objects from the given table column.
     * @param table The table to get the data from.
     * @param columnIndex The index of the column to get.
     * @return The column data.
     */
    protected ArrayList<String> getColumnAsArrayList(Object[][] data, int columnIndex) {
        ArrayList<String> columnsData = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            columnsData.add((String) data[i][columnIndex]);
        }
        return columnsData;
    }

    /**
     * Removes duplicates from the given list.
     * @param list The list to remove duplicates from.
     * @return The list without duplicates.
     */
    protected ArrayList<String>  removeDuplicates(ArrayList<String> list) {
        HashSet<String> set = new HashSet<>(list);
        ArrayList<String> newList = new ArrayList<>(set);
        return newList;
    }

    /**
     * Gets the character list from the tables.
     * @return The character list.
     */
    protected ArrayList<ArrayList<String>> getCharacterlist() {
        ArrayList<ArrayList<String>> columnsDataSplitted = new ArrayList<>();
        ArrayList<String> columnsData = new ArrayList<>();
        for (int i = 1; i < columnNumber; i+=3) {
            for (int j = 0; j < tablesData.size(); j++) {
                columnsData.addAll(getColumnAsArrayList(tablesData.get(j), i));
                columnsData.addAll(getColumnAsArrayList(tablesData.get(j), i+1));
            }
            columnsData.add("Blank");
            columnsData = removeDuplicates(columnsData);
            columnsDataSplitted.add(columnsData);
            columnsData = new ArrayList<>();
        }
        return columnsDataSplitted;
    }

    /**
     * Creates the statuses of the tables converted to single line.
     * @return The base statuses.
     */
    protected ArrayList<Status> createBaseStatuses(){
        ArrayList<Status> statuses = new ArrayList<>();
        Status status;
        Rule rule = null;
        StringBuilder write = new StringBuilder();
        StringBuilder sign = new StringBuilder();
        StringBuilder direction = new StringBuilder();
        for (int i = 0; i < statusList.size(); i++) {
            status = new Status(statusList.get(i));
            statuses.add(status);
        }
        for (int i = 0; i < statusList.size(); i++) {
            for(int j = 0; j < tablesData.get(i).length; j++){
                sign = new StringBuilder();
                write = new StringBuilder();
                direction = new StringBuilder();
                for (int k = 1; k < columnNumber; k+=3) {
                    sign.append((String)tablesData.get(i)[j][k]);
                    sign.append("#");
                    write.append((String)tablesData.get(i)[j][k+1]);
                    write.append("#");
                    direction.append(tablesData.get(i)[j][k+2]);
                    direction.append("#");
                }
                sign.append(statuses.get(i).getName());
                sign.append("#");
                write.append((String)tablesData.get(i)[j][0]);
                write.append("#");
                write.append(direction.toString());
                for(int l = 0; l < statuses.size(); l++){
                    if(statuses.get(l).getName().equals(tablesData.get(i)[j][0])){
                        rule = new Rule(sign.toString(), write.toString(), Movement.STAY, chooserStatus);
                    }
                }
                if(rule != null){
                    statuses.get(i).addRule(rule);
                }
            }
        }
        return statuses;
    }

    protected ArrayList<Status> createConvertStatuses(){
        ArrayList<Status> convertStatuses = new ArrayList<>();
        Status convertStatus = new Status("Convert");
        Status goToStart = new Status("GoToStart");
        ArrayList<String> blanks = new ArrayList<>();
        Status starStatus = new Status("ConverterStart");
        for(int i = 0; i < threadCount; i++){
            blanks.add("Blank");
        }
        goToStart.addRule(new Rule("Blank", "Blank", Movement.RIGHT, chooserStatus));
        convertStatus.addRule(new Rule("Blank", "Blank", Movement.LEFT, goToStart));
        starStatus.addRule(new Rule("Blank", createCombinationString(blanks), Movement.LEFT, goToStart));
        for(int i = 0; i < columnData.get(0).size(); i++){
            if(columnData.get(0).get(i).equals("Blank")){
                continue;
            }else{
                starStatus.addRule(new Rule(columnData.get(0).get(i), createCombinationStringWithEditing(blanks, 0, i) + "Start#", Movement.RIGHT, convertStatus));
                convertStatus.addRule(new Rule(columnData.get(0).get(i), createCombinationStringWithEditing(blanks, 0, i), Movement.RIGHT, convertStatus));
                goToStart.addRule(new Rule(createCombinationStringWithEditing(blanks, 0, i), createCombinationStringWithEditing(blanks, 0, i), Movement.LEFT, goToStart));
                goToStart.addRule(new Rule(createCombinationStringWithEditing(blanks, 0, i) + "Start#", createCombinationStringWithEditing(blanks, 0, i) + "Start#", Movement.LEFT, goToStart));    
            }
        }
        convertStatuses.add(convertStatus);
        convertStatuses.add(goToStart);
        convertStatuses.add(starStatus);
        head = new Head(starStatus);
        return convertStatuses;
    }

    /**
     * Creates the chooser status.
     */
    protected void createTheChooserStatus(){
        StringBuilder write = new StringBuilder();
        StringBuilder sign = new StringBuilder();
        ArrayList<String> moveCombinations = new ArrayList<>();
        for(int i = 0; i < choosableStatuses.size(); i++){
            for(int j = 0; j < combinations.size(); j++){
                write = new StringBuilder();
                write.append(createCombinationString(combinations.get(j)));
                write.append(choosableStatuses.get(i).getName());
                write.append("#");

                sign = new StringBuilder();
                sign.append(createCombinationString(combinations.get(j)));
                sign.append(choosableStatuses.get(i).getName());
                sign.append("#");

                chooserStatus.addRule(new Rule(sign.toString(), write.toString(), Movement.STAY, choosableStatuses.get(i)));

                for(int k = 0; k < threadCount; k++){
                    moveCombinations = createMovementCombinations(k);
                    if(k == 0){
                        moveCombinations.add("");
                    }
                    for(int l = 0; l < moveCombinations.size(); l++){
                        chooserStatus.addRule(new Rule(sign.toString() + moveCombinations.get(l) + "Right#", write.toString() + moveCombinations.get(l), Movement.RIGHT, moveStarterStatuses.get(k*2)));
                        chooserStatus.addRule(new Rule(sign.toString() + moveCombinations.get(l) + "Stay#", write.toString() + moveCombinations.get(l), Movement.STAY, chooserStatus));
                        chooserStatus.addRule(new Rule(sign.toString() + moveCombinations.get(l) + "Left#", write.toString() + moveCombinations.get(l), Movement.LEFT, moveStarterStatuses.get(k*2+1)));
                    }
                }
            }
        }
        for(int i = 0;i<combinations.size();i++){
            chooserStatus.addRule(new Rule(createCombinationString(combinations.get(i)), createCombinationString(combinations.get(i)), Movement.RIGHT, chooserStatus));
        }
    }

    /**
     * Creates the status which moves to the most left, and when reaches it, changes to the chooser status.
     */
    protected void createMoveToMostLeftStatus(){
        ArrayList<String> moveCombinations = new ArrayList<>();
        for(int i = 0; i < combinations.size(); i++){
            moveToMostLeft.addRule(new Rule(createCombinationString(combinations.get(i)), createCombinationString(combinations.get(i)), Movement.LEFT, moveToMostLeft));
            for(int j = 0; j < choosableStatuses.size(); j++){
                moveToMostLeft.addRule(new Rule(createCombinationString(combinations.get(i)) + choosableStatuses.get(j).getName() + "#", createCombinationString(combinations.get(i)) + choosableStatuses.get(j).getName() + "#", Movement.LEFT, moveToMostLeft));
                for(int k = 0; k <= threadCount;k++){
                    moveCombinations = createMovementCombinations(k);
                    for(String moveCombination : moveCombinations){
                        moveToMostLeft.addRule(new Rule(createCombinationString(combinations.get(i)) + choosableStatuses.get(j).getName() + "#" + moveCombination, createCombinationString(combinations.get(i)) + choosableStatuses.get(j).getName() + "#" + moveCombination, Movement.LEFT, moveToMostLeft));
                    }                    
                }
            }
        }
        moveToMostLeft.addRule(new Rule("Blank", "Blank", Movement.RIGHT, chooserStatus));
    }
    
    /**
     * Creates all the possible combinations of the characters on each line.
     */
    protected void createCharactercombinations(){
        int totalCombinations = 1;
        int counter1 = 0;
        int counter2 = 0;
        for(int i = 0; i<columnData.size();i++){
            totalCombinations *= columnData.get(i).size();
        }
        for(int i = 0; i<totalCombinations;i++){
            combinations.add(new ArrayList<String>());
        }
        for(int i = 0; i<columnData.size();i++){
            totalCombinations /= columnData.get(i).size();
            for(int j = 0; j<combinations.size();j++){
                combinations.get(j).add(columnData.get(i).get(counter2));
                counter1++;
                if(counter1 == totalCombinations){
                    counter1 = 0;
                    counter2++;
                    if(counter2 == columnData.get(i).size()){
                        counter2 = 0;
                    }
                }
            }
        }
    }

    /**
     * Creates the statuses which moves the lines to left and right.
     */
    protected ArrayList<Status> createMoverStatuses(){
        ArrayList<Status> moverStatuses = new ArrayList<>();
        ArrayList<ArrayList<Status>> moverStatusesSplittedRight = new ArrayList<>();
        ArrayList<Status> moverStatusesSplitted2Right = new ArrayList<>();
        ArrayList<ArrayList<Status>> moverStatusesSplittedLeft = new ArrayList<>();
        ArrayList<Status> moverStatusesSplitted2Left = new ArrayList<>();
        Status statusRight;
        Status statusLeft;
        ArrayList<String> blanks = new ArrayList<>();
        for(int i = 0; i < threadCount; i++){
            blanks.add("Blank");
        }
        for(int i = 0; i < columnData.size(); i++){
            moverStatusesSplitted2Right = new ArrayList<>();
            moverStatusesSplitted2Left = new ArrayList<>();
            for(int j = 0; j < columnData.get(i).size(); j++){
                statusRight = new Status("WriterLine"+i+"RightCharacter"+columnData.get(i).get(j));
                statusLeft = new Status("WriterLine"+i+"LeftCharacter"+columnData.get(i).get(j));
                if(columnData.get(i).get(j).equals("Blank")){
                    statusRight.addRule(new Rule("Blank", "Blank", Movement.RIGHT, chooserStatus));
                    statusLeft.addRule(new Rule("Blank", "Blank", Movement.LEFT, moveToMostLeft));
                }else{
                    statusRight.addRule(new Rule("Blank", createCombinationStringWithEditing(blanks, i, j), Movement.RIGHT, chooserStatus));
                    statusLeft.addRule(new Rule("Blank", createCombinationStringWithEditing(blanks, i, j), Movement.LEFT, moveToMostLeft));
                }
                moverStatusesSplitted2Right.add(statusRight);
                moverStatusesSplitted2Left.add(statusLeft);

            }
            moverStatusesSplittedRight.add(moverStatusesSplitted2Right);
            moverStatusesSplittedLeft.add(moverStatusesSplitted2Left);
        }
        
        ArrayList<String> moveCombinations = new ArrayList<>();
        String read;
        String write;
        int characterIndex = 0;
        for(int i = 0; i < threadCount; i++){
            for(int j = 0; j < columnData.get(i).size(); j++){
                for(int k = 0; k < combinations.size(); k++){
                    for(int l = 0; l < columnData.get(i).size(); l++){
                        if(columnData.get(i).get(l).equals(combinations.get(k).get(i))){
                            characterIndex = l;
                        }
                    }
                    if(!combinations.get(k).get(i).equals("Blank")){
                        read = createCombinationString(combinations.get(k));
                        write = createCombinationStringWithEditing(combinations.get(k), i, j);
                        moverStatusesSplittedRight.get(i).get(j).addRule(new Rule(read, write, Movement.RIGHT, moverStatusesSplittedRight.get(i).get(characterIndex)));
                        moverStatusesSplittedLeft.get(i).get(j).addRule(new Rule(read, write, Movement.LEFT, moverStatusesSplittedLeft.get(i).get(characterIndex)));
                        moveCombinations = createMovementCombinations(i);
                        if(i == 0){
                            moveCombinations.add("");
                        }
                        for(int l = 0;l<choosableStatuses.size();l++){
                            for(int m = 0;m < moveCombinations.size();m++){
                                moverStatusesSplittedRight.get(i).get(j).addRule(new Rule(read + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), write + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), Movement.RIGHT, moverStatusesSplittedRight.get(i).get(characterIndex)));
                                moverStatusesSplittedLeft.get(i).get(j).addRule(new Rule(read + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), write  + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), Movement.LEFT, moverStatusesSplittedLeft.get(i).get(characterIndex)));
                            }
                        }
                    }else{
                        read = createCombinationString(combinations.get(k));
                        write = createCombinationStringWithEditing(combinations.get(k), i, j);
                        moverStatusesSplittedRight.get(i).get(j).addRule(new Rule(read, write, Movement.RIGHT, moveToMostLeft));
                        moverStatusesSplittedLeft.get(i).get(j).addRule(new Rule(read, write, Movement.LEFT, chooserStatus));
                        moveCombinations = createMovementCombinations(i);
                        if(i == 0){
                            moveCombinations.add("");
                        }
                        for(int l = 0;l<choosableStatuses.size();l++){
                            for(int m = 0;m < moveCombinations.size();m++){
                                moverStatusesSplittedRight.get(i).get(j).addRule(new Rule(read + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), write + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), Movement.RIGHT, moveToMostLeft));
                                moverStatusesSplittedLeft.get(i).get(j).addRule(new Rule(read + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), write  + choosableStatuses.get(l).getName() + "#" + moveCombinations.get(m), Movement.LEFT, chooserStatus));
                            }
                        }
                    }
                }
            }
        }
        moverStatuses.addAll(moverStatusesSplitted2Left);
        moverStatuses.addAll(moverStatusesSplitted2Right);
        ArrayList<Status> moveToSide = createMoveToSide();
        for(int i = 0; i < threadCount; i++){
            for(int j = 0; j < moverStatuses.size(); j++){
                for(int k = 0; k < combinations.size();k++){
                    if(moverStatuses.get(j).getName().equals("WriterLine"+i+"LeftCharacterBlank")){
                        moveToSide.get(i*2).addRule(new Rule("Blank", "Blank", Movement.LEFT, moverStatuses.get(j)));
                        if(!combinations.get(k).get(i).equals("Blank")){
                            moveToSide.get(i*2).addRule(new Rule(createCombinationString(combinations.get(i)), createCombinationString(combinations.get(i)), Movement.LEFT, moverStatuses.get(j)));    
                        }
                    }
                    if(moverStatuses.get(j).getName().equals("WriterLine"+i+"RightCharacterBlank")){
                        moveToSide.get((i*2)+1).addRule(new Rule("Blank", "Blank", Movement.RIGHT, moverStatuses.get(j)));
                        if(!combinations.get(k).get(i).equals("Blank")){
                            moveToSide.get((i*2)+1).addRule(new Rule(createCombinationString(combinations.get(i)), createCombinationString(combinations.get(i)), Movement.RIGHT, moverStatuses.get(j)));   
                        }
                    }
                }
            }
        }
        moverStatuses.addAll(moveToSide);
        return moverStatuses;
    }

    /**
     * Creates the statuses which moves the left most and right most remembering which line it is.
     * @return  The statuses which moves the left most and right most.
     */
    protected ArrayList<Status> createMoveToSide(){
        ArrayList<Status> moverStatuses = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Status status1;
        Status status2;
        for(int line = 0; line<columnData.size(); line++){
            sb = new StringBuilder();
            sb.append("Mover");
            sb.append(line);
            sb.append("Right");
            status1 = new Status(sb.toString());
            sb = new StringBuilder();
            sb.append("Mover");
            sb.append(line);
            sb.append("Left");
            status2 = new Status(sb.toString());
            for(int i = 0; i < combinations.size();i++){
                if(!combinations.get(i).get(line).equals("Blank")){
                    status1.addRule(new Rule(createCombinationString(combinations.get(i)), createCombinationString(combinations.get(i)), Movement.RIGHT, status1));
                    status2.addRule(new Rule(createCombinationString(combinations.get(i)), createCombinationString(combinations.get(i)), Movement.LEFT, status2));    
                }
            }
            moverStatuses.add(status1);
            moverStatuses.add(status2);
        }
        moveStarterStatuses.addAll(moverStatuses);
        return moverStatuses;
    }

    /**
     * Creates all posible combinations of Right, Left and Stay.
     * @param line The number of lines, which is the number of Right, Left and Stay can be in the combination.
     * @return The combinations.
     */
    protected ArrayList<String> createMovementCombinations(int line){
        ArrayList<String> moveCombinations = new ArrayList<>();
        ArrayList<StringBuilder> combinationHolder = new ArrayList<>();
        ArrayList<StringBuilder> combinationBuilderR = new ArrayList<>();
        ArrayList<StringBuilder> combinationBuilderS = new ArrayList<>();
        ArrayList<StringBuilder> combinationBuilderL = new ArrayList<>();
        combinationBuilderR.add(new StringBuilder());
        combinationBuilderS.add(new StringBuilder());
        combinationBuilderL.add(new StringBuilder());
        for(int i = 0; i < line; i++){
            for(int j = 0; j<combinationBuilderR.size(); j++){
                combinationBuilderR.get(j).append("Right#");
                combinationBuilderS.get(j).append("Stay#");
                combinationBuilderL.get(j).append("Left#");
            }
            combinationHolder.addAll(combinationBuilderR);
            combinationHolder.addAll(combinationBuilderS);
            combinationHolder.addAll(combinationBuilderL);
            for(StringBuilder sb : combinationHolder){
                combinationBuilderR.add(sb);
                combinationBuilderS.add(sb);
                combinationBuilderL.add(sb);
            }
        }
        for(int i = 0; i < combinationHolder.size(); i++){
            moveCombinations.add(combinationHolder.get(i).toString());
        }
        return moveCombinations;
    }

    /**
     * Creates a combination string with editing the provided line to be the provided character.
     * @param combination The combination to edit.
     * @param line The line to edit.
     * @param character The character to be set in the line.
     * @return The edited combination.
     */
    protected String createCombinationStringWithEditing(ArrayList<String> combination, int line, int character){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < combination.size(); i++){
            if(i == line){
                sb.append(columnData.get(line).get(character));
                sb.append("#");
            }else{
                sb.append(combination.get(i));
                sb.append("#");
            }
        }
        return sb.toString();
    }

    /**
     * Creates a combination string from the given combination.
     * @param combination The combination to create the string from.
     * @return The combination string.
     */
    protected String createCombinationString(ArrayList<String> combination){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < combination.size(); i++){
            sb.append(combination.get(i));
            sb.append("#");
        }
        return sb.toString();
    }

    /**
     * Checks if there is a duplication in the tables.
     * @return True if there is a duplication, false otherwise.
     */
    protected boolean checkDuplicationAllTable(){
        for(int i = 0; i < tables.size(); i++){
            if(checkDuplication(tables.get(i))){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is a duplication in the given table.
     * @param table The table to check.
     * @return True if there is a duplication, false otherwise.
     */
    protected boolean checkDuplication(JTable table){
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        Set<String> uniqueRows = new HashSet<>();
        ArrayList<Integer> columns = new ArrayList<>();
        for(int i = 1; i < table.getColumnCount(); i+=3){
            columns.add(i);
        }

        for (int row = 0; row < rowCount; row++) {
            StringBuilder rowBuilder = new StringBuilder();
            boolean duplicateFound = true;

            for (int column : columns) {
                Object cellValue = model.getValueAt(row, column);
                rowBuilder.append(cellValue).append("|");

                if (!uniqueRows.contains(rowBuilder.toString())) {
                    duplicateFound = false;
                }
            }

            if (duplicateFound) {
                return true;
            }

            uniqueRows.add(rowBuilder.toString());
        }

        return false;
    }

    /**
     * Checks if there is an empty first element in the tables.
     * @return True if there is an empty first element, false otherwise.
     */
    protected boolean isThereEmptyFirstElement(){
        for (JTable table : tables) {
            if (isThereARowsWithEmptyFirstElement(table)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an empty first element in the given table.
     * @param table The table to check.
     * @return True if there is an empty first element, false otherwise.
     */
    protected boolean isThereARowsWithEmptyFirstElement(JTable table) {
        int rowCount = table.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            Object value = table.getValueAt(i, 0);

            if (value == null || value.toString().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if there is a non valid movement in the tables.
     * @return True if there is a wrong movement, false otherwise.
     */
    protected boolean isThereWrongMovement(){
        for(int i = 0; i < tablesData.size(); i++){
            for(int j = 4; j < columnNumber; j+=3){
                if(checkRowsWithInvalidElementAtColumnX(tablesData.get(i), j)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if there is a non valid movement in the given table.
     * @param table The table to check.
     * @param columnX The column to check.
     * @return True if there is a wrong movement, false otherwise.
     */
    protected boolean checkRowsWithInvalidElementAtColumnX(Object[][] table, int columnX) {
        for (int row = 0; row > table.length; row++) {
            Object cellValue = table[row][columnX];
            if (cellValue == null || cellValue.toString().trim().isEmpty()) {
                return true;
            } else if (columnX > 1 && isValidMovement(cellValue.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given movement is valid.
     * @param movement The movement to check.
     * @return True if the movement is valid, false otherwise.
     */
    protected static boolean isValidMovement(String movement) {
        String[] validMovements = {"Right", "Left", "Stay"};
        for (String validMovement : validMovements) {
            if (!validMovement.equalsIgnoreCase(movement)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Fills the blank cells with "Blank".
     */
    protected void fillBlank(){
        for (JTable table : tables) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int rowCount = model.getRowCount();
            int columnCount = model.getColumnCount();

            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    Object cellValue = model.getValueAt(row, column);
                    if (cellValue == null || cellValue.toString().trim().isEmpty()) {
                        model.setValueAt("Blank", row, column);
                    }
                }
            }
        }
    }

    protected void getDataFromTables(){
        for(int i = 0; i < tables.size(); i++){
            tablesData.add(getTableData(tables.get(i)));
        }
    }

    protected Object[][] getTableData (JTable table) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        TableModel dtm = table.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        Object[][] tableData = new Object[nRow][nCol];
        for (int i = 0 ; i < nRow ; i++){
            for (int j = 0 ; j < nCol ; j++){
                tableData[i][j] = dtm.getValueAt(i,j);
            }
        }
        return tableData;
    }
}
