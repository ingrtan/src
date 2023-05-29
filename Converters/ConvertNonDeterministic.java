package Converters;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JTable;
import Data.Head;
import Data.Rule;
import Data.Status;
import Resources.Movement;
import Resources.WrongTableException;

public class ConvertNonDeterministic extends ConverterMultiTread{
    private HashSet<String> allPosibleCombinations = new HashSet<String>();
    ArrayList<String> everyPosibleCombinations = new ArrayList<>();
    private Status copyManager = new Status("CopyManager");
    private Status goLeftCopy = new Status("LeftSearch");
    private ArrayList<Status> copyStatuses = new ArrayList<>();
    private Status searchActiveFromRight = new Status("SearchActiveFromRight"); 
    private Status searchActiveFromLeft = new Status("SearchActiveFromLeft");
    private ArrayList<Status> movers = new ArrayList<>();
    private ArrayList<Status> pushers = new ArrayList<>();
    private Status chooserStatus = new Status("Chooser");
    private Status moveToMostLeft = new Status("MoveToMostLeft");
    private Status startNewThread = new Status("StartNewThread");
    ArrayList<Status> choosableStatuses = new ArrayList<>();
    private ArrayList<Status> copyEditingStatuses = new ArrayList<>();
    private Status clearerRight = new Status("ClearerRight");
    private Status clearerRightCheck = new Status("ClearerRightCheck");
    private Status clearerLeft = new Status("ClearerLeft");
    private Status resetStatus = new Status("ResetStatus");
    private Status checkStatus = new Status("CheckStatus");

    public ConvertNonDeterministic(){
    }

    @Override
    public Head convert(ArrayList<JTable> tables, ArrayList<String> statusList, int threadCount) throws WrongTableException{
        this.tables = tables;
        this.statusList = statusList;
        this.threadCount = threadCount;
        columnNumber = tables.get(0).getColumnCount();
        super.getDataFromTables();
        if(isThereWrongMovement()){
            throw new WrongTableException("Wrong Movement.");
        }
        if(isThereEmptyFirstElement()){
            throw new WrongTableException("Empty First Element.");
        }
        super.fillBlank();
        super.chooserStatus = this.chooserStatus;
        columnData = getCharacterlist();
        createCharactercombinations();
        createAllposibleCombinations();
        choosableStatuses = createBaseStatuses();
        movers = super.createMoverStatuses();
        createPushers();
        createCopyStatuses();
        createMoveToMostLeftStatus();
        createSearchRules();
        addMoversToChooser();
        startNewThread();
        createClearers();
        createResetStatus();
        statuses.add(chooserStatus);
        statuses.add(moveToMostLeft);
        statuses.addAll(moveStarterStatuses);
        statuses.addAll(choosableStatuses);
        statuses.addAll(copyStatuses);
        statuses.addAll(copyEditingStatuses);
        statuses.add(copyManager);
        statuses.add(goLeftCopy);
        statuses.add(searchActiveFromRight);
        statuses.add(searchActiveFromLeft);
        statuses.addAll(pushers);
        statuses.addAll(movers);
        statuses.add(startNewThread);
        statuses.add(clearerRight);
        statuses.add(clearerRightCheck);
        statuses.add(clearerLeft);
        statuses.add(resetStatus);
        statuses.addAll(createConvertStatuses());
        head.setStatuses(statuses);
        return head;
    }

    @Override
    protected ArrayList<Status> createBaseStatuses(){
        boolean isDuplicate = false;
        ArrayList<Integer> counter = new ArrayList<>();
        ArrayList<Integer> duplicates = new ArrayList<>();
        ArrayList<Status> statuses = new ArrayList<>();
        for(String name: statusList){
            Status status = new Status(name);
            statuses.add(status);
        }
        choosableStatuses.addAll(statuses);
        Object[][] data;
        for(int i = 0; i < tablesData.size();i++){
            data = tablesData.get(i);
            for(int j = 0; j < data.length; j++){
                if(counter.contains(j)){
                    System.out.println("continue1");
                    continue;
                }
                duplicates.add(j);
                for(int k = j+1; k < data.length; k++){
                    if(counter.contains(k)){
                        System.out.println("continue1");
                        continue;
                    }
                    if(getRowString(data[j]).equals(getRowString(data[k]))){
                        isDuplicate = true;
                        counter.add(k);
                        duplicates.add(k);
                        System.out.println("break1");
                        break;
                    }
                }
                if(isDuplicate){
                    createNonDeterministicRule(data, statusList.get(i), duplicates, statuses.get(i));
                    isDuplicate = false;
                }else{
                    System.out.println("createBaseRule " + statusList.get(i));
                    createBaseRule(data[j], statusList.get(i), statuses.get(i));
                }
                duplicates.clear();
            }
            counter.clear();
        }
        HashSet<String> usedCombinatioons = new HashSet<>();
        StringBuilder sign = new StringBuilder();
        for(int i = 0; i < tablesData.size();i++){
            data = tablesData.get(i);
            usedCombinatioons =  getWrite(data);
            for(int j = 0; j < combinations.size();j++){
                sign = new StringBuilder();
                sign.append(createCombinationString(combinations.get(j)));
                sign.append(statusList.get(i));
                sign.append("#");
                if(!usedCombinatioons.contains(sign.toString())){
                    statuses.get(i).addRule(new Rule(sign.toString(), "X", Movement.STAY, chooserStatus));
                }
            }
        }
        return statuses;
    }

    private HashSet<String> getWrite(Object[][] data){
        HashSet<String> write = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for(Object[] row : data){
            for(int i = 1; i < row.length;i+=3){
                sb.append(row[i]);
            }
            write.add(sb.toString());
            sb = new StringBuilder();
        }
        return write;
    }

    private String getRowString(Object[] row){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < row.length;i+=3){
            sb.append(row[i]);
        } 
        return sb.toString();
    }
    
    @Override
    protected void createMoveToMostLeftStatus(){
        for(String stringy : allPosibleCombinations){
            if(stringy.equals("#X") || stringy.equals("Blank")){
                moveToMostLeft.addRule(new Rule(stringy, stringy, Movement.RIGHT, chooserStatus));
            }else{
                moveToMostLeft.addRule(new Rule(stringy, stringy, Movement.LEFT, moveToMostLeft));
            }
        }
    } 
    
    private void createBaseRule(Object[] row, String statusName, Status status){
        StringBuilder read = new StringBuilder();
        StringBuilder write = new StringBuilder();
        StringBuilder direction = new StringBuilder();
        for(int i = 1; i < row.length;i+=3){
            read.append(row[i]);
            read.append("#");
            write.append(row[i+1]);
            write.append("#");
            direction.append(row[i+2]);
            direction.append("#");
        }
        chooserStatus.addRule(new Rule(read.toString() + statusName + "#", read.toString() + row[0] + "#", Movement.STAY, status));
        write.append(row[0]);
        write.append("#");
        write.append(direction.toString());
        status.addRule(new Rule(read.toString() + statusName + "#",write.toString(),Movement.RIGHT,chooserStatus));
    }

    private void createNonDeterministicRule(Object[][] table, String currentStage, ArrayList<Integer> duplicates, Status stage){
        StringBuilder read = new StringBuilder();
        StringBuilder write = new StringBuilder();
        StringBuilder direction = new StringBuilder();
        
        for(int i = 1; i < table[duplicates.get(0)].length;i+=3){
            read.append(table[duplicates.get(0)][i]);
            read.append("#");
        }
        read.append(currentStage);
        read.append("#");
        for(int i = 1;i < duplicates.size();i++){
            copyEditingStatuses.add(new Status(currentStage + "CopyTableLine" + duplicates.get(i)));
            for(String stringy : allPosibleCombinations){
                copyEditingStatuses.get(i-1).addRule(new Rule(stringy, stringy, Movement.RIGHT, copyEditingStatuses.get(i-1)));
            }
        }
        for(int i = 1; i < table[duplicates.get(0)].length;i+=3){
            write.append(table[duplicates.get(0)][i+1]);
            write.append("#");
            direction.append(table[duplicates.get(0)][i+2]);
            direction.append("#");
        }
        write.append(table[duplicates.get(0)][0]);
        write.append("#");
        write.append(direction.toString());
        chooserStatus.addRule(new Rule(read.toString(), read.toString() + "NeedToCopy#", Movement.RIGHT, startNewThread));
        copyManager.addRule(new Rule(read.toString() + "NeedToCopy#", read.toString(), Movement.RIGHT, copyEditingStatuses.get(0)));
        allPosibleCombinations.add(write.toString() + "NeedToCopy#");
        read = new StringBuilder();
        read.append(write.toString());
        write = new StringBuilder();
        for(int i = 1; i < duplicates.size(); i++){
            for(int j = 1; j < table[duplicates.get(i)].length;j+=3){
                write.append(table[duplicates.get(i)][j+1]);
                write.append("#");
                direction.append(table[duplicates.get(i)][j+2]);
                direction.append("#");
            }
            allPosibleCombinations.add(write.toString() + "NeedToCopy#");
            copyManager.addRule(new Rule(read.toString() + "NeedToCopy#", read.toString(), Movement.RIGHT, copyEditingStatuses.get(i-1)));
            if( i == duplicates.size() - 1){
                copyEditingStatuses.get(i-1).addRule(new Rule("Blank", write.toString() + table[duplicates.get(i)][0] + "#", Movement.RIGHT, goLeftCopy));
                allPosibleCombinations.add(write.toString() + table[duplicates.get(i)][0] + "#");
                chooserStatus.addRule(new Rule(write.toString() + table[duplicates.get(i)][0] + "#", write.toString() + table[duplicates.get(i)][0] + "#", Movement.STAY, searchStatusByName((String)table[duplicates.get(i)][0])));
            }else{
                copyEditingStatuses.get(i-1).addRule(new Rule("Blank", write.toString() + table[duplicates.get(i)][0] + "NeedToCopy#", Movement.RIGHT, goLeftCopy));
                copyManager.addRule(new Rule(write.toString() + table[duplicates.get(i)][0] + "#NeedToCopy#", write.toString() + table[duplicates.get(i)][0] + "#", Movement.RIGHT, copyEditingStatuses.get(i)));
                chooserStatus.addRule(new Rule(write.toString() + table[duplicates.get(i)][0] + "#NeedToCopy#", write.toString() + table[duplicates.get(i)][0] + "#NeedToCopy", Movement.STAY, startNewThread));
                allPosibleCombinations.add(write.toString() + table[duplicates.get(i)][0] + "#NeedToCopy#");
            }
        }
    }

    private Status searchStatusByName(String name){
        for(Status status : choosableStatuses){
            if(status.getName().equals(name)){
                return status;
            }
        }
        return null;
    }

    private void createPushers(){
        ArrayList<Status> pushersLeft = new ArrayList<>();
        ArrayList<Status> pushersRight = new ArrayList<>();
        
        for(int j = 0; j < everyPosibleCombinations.size(); j++){
            pushersLeft.add(new Status("PusherLeftLineCombination" + everyPosibleCombinations.get(j)));
            pushersRight.add(new Status("PusherRightCombination" + everyPosibleCombinations.get(j)));
            if(everyPosibleCombinations.get(j).equals("#")){
                pusherStarterRight(pushersRight.get(pushersRight.size()-1));
            }
            if(everyPosibleCombinations.get(j).equals("#X")){
                pusherStarterLeft(pushersLeft.get(pushersLeft.size()-1));
            }
            pushersLeft.get(j).addRule(new Rule("Blank", everyPosibleCombinations.get(j), Movement.RIGHT, searchActiveFromRight));
            pushersRight.get(j).addRule(new Rule("Blank", everyPosibleCombinations.get(j), Movement.LEFT, searchActiveFromLeft));
            pushersLeft.get(j).addRule(new Rule("X", everyPosibleCombinations.get(j), Movement.RIGHT, searchActiveFromRight));
            pushersRight.get(j).addRule(new Rule("X", everyPosibleCombinations.get(j), Movement.LEFT, searchActiveFromLeft));
        }
        pushers.addAll(pushersLeft);
        pushers.addAll(pushersRight);
        for(int j = 0; j < everyPosibleCombinations.size(); j++){
            for(int k = 0; k < everyPosibleCombinations.size(); k++){
                pushersLeft.get(j).addRule(new Rule(everyPosibleCombinations.get(k), everyPosibleCombinations.get(j), Movement.RIGHT, pushersLeft.get(k)));
                pushersRight.get(j).addRule(new Rule(everyPosibleCombinations.get(k), everyPosibleCombinations.get(j), Movement.RIGHT, pushersRight.get(k)));
            }
        }
    }

    private void createCopyStatuses(){
        Status copyStatus;
        for(String stringy : allPosibleCombinations){
            copyStatus = new Status("copy" + stringy);
            copyManager.addRule(new Rule(stringy, stringy + "Copy#", Movement.RIGHT, copyStatus));
            for(String stringy2 : allPosibleCombinations){
                copyStatus.addRule(new Rule(stringy2, stringy2, Movement.RIGHT, copyStatus));
            }
            copyStatus.addRule(new Rule("Blank", stringy, Movement.LEFT, goLeftCopy));
            goLeftCopy.addRule(new Rule(stringy, stringy, Movement.LEFT, goLeftCopy));
            goLeftCopy.addRule(new Rule(stringy + "Copy", stringy, Movement.RIGHT, copyManager));
            copyStatuses.add(copyStatus);
        }
        goLeftCopy.addRule(new Rule("#X", "#X", Movement.RIGHT, copyManager));
        copyManager.addRule(new Rule("#", "#X", Movement.RIGHT, chooserStatus));
        
    }

    private void createAllposibleCombinations(){
        ArrayList<String> movementCombinations = new ArrayList<>();
        for(int i = 0; i < combinations.size(); i++){
            allPosibleCombinations.add(createCombinationString(combinations.get(i)));
            for(int j = i+1; j < statusList.size(); j++){
                allPosibleCombinations.add(createCombinationString(combinations.get(i)) + statusList.get(j) + "#");
                for(int k = 1; k < threadCount; k++){
                    movementCombinations = createMovementCombinations(k);
                    for(String stringy : movementCombinations){
                        allPosibleCombinations.add(createCombinationString(combinations.get(i)) + statusList.get(j) + "#" + stringy);
                    }
                }
            }
        }
        allPosibleCombinations.add("#");
        allPosibleCombinations.add("#X");
        everyPosibleCombinations.addAll(allPosibleCombinations);
    }

    private void pusherStarterRight(Status pusher){
        ArrayList<String> blanks = new ArrayList<>();
        for(int i = 0; i < threadCount; i++){
            blanks.add("Blank");
        }
        String sign;
        StringBuilder write = new StringBuilder();
        for(Status moverStatus: movers){
            for(int i = 0; i < threadCount; i++){
                if(moverStatus.getName().contains("WriterLine" + i + "RightCharacter")){
                    sign = moverStatus.getName().substring(("WriterLine" + i + "RightCharacter").length());
                    for(int j = 0; j < threadCount; j++){
                        if(j == i){
                            write.append(sign);
                        }else{
                            write.append("Blank");
                        }
                        write.append("#");
                    }
                    moverStatus.addRule(new Rule("#", write.toString(), Movement.RIGHT, pusher));
                }
            }
        }
    }

    private void pusherStarterLeft(Status pusher){
        ArrayList<String> blanks = new ArrayList<>();
        for(int i = 0; i < threadCount; i++){
            blanks.add("Blank");
        }
        String sign;
        StringBuilder write = new StringBuilder();
        for(Status moverStatus: movers){
            for(int i = 0; i < threadCount; i++){
                if(moverStatus.getName().contains("WriterLine" + i + "LeftCharacter")){
                    sign = moverStatus.getName().substring(("WriterLine" + i + "LeftCharacter").length());
                    for(int j = 0; j < threadCount; j++){
                        if(j == i){
                            write.append(sign);
                        }else{
                            write.append("Blank");
                        }
                        write.append("#");
                    }
                    moverStatus.addRule(new Rule("#X", write.toString(), Movement.LEFT, pusher));
                }
            }
        }

    }

    private void createSearchRules(){
        for(String stringy : allPosibleCombinations){
            if(stringy.contains("#")){
                searchActiveFromLeft.addRule(new Rule(stringy, stringy, Movement.STAY, chooserStatus));
            }else{
                searchActiveFromLeft.addRule(new Rule(stringy, stringy, Movement.RIGHT, searchActiveFromLeft));
            }
            if(stringy.contains("#X")){
                searchActiveFromRight.addRule(new Rule(stringy, stringy, Movement.STAY, chooserStatus));
            }else{
                searchActiveFromRight.addRule(new Rule(stringy, stringy, Movement.LEFT, searchActiveFromRight));
            }
        }
    }

    private void startNewThread(){
        for(String stringy : allPosibleCombinations){
            startNewThread.addRule(new Rule(stringy, stringy, Movement.RIGHT, startNewThread));
        }
        startNewThread.addRule(new Rule("Blank", "#", Movement.LEFT, goLeftCopy));
    }

    private void createClearers(){
        for(String stringy : allPosibleCombinations){
            if(stringy.equals("#") || stringy.equals("#X")){
                clearerRight.addRule(new Rule(stringy, stringy, Movement.STAY, chooserStatus));
                clearerRightCheck.addRule(new Rule(stringy, stringy, Movement.STAY, checkStatus));
                clearerLeft.addRule(new Rule(stringy, stringy, Movement.STAY, resetStatus));
            }else{
                clearerRight.addRule(new Rule(stringy, "X", Movement.RIGHT, clearerRight));
                clearerRightCheck.addRule(new Rule(stringy, "X", Movement.RIGHT, clearerRightCheck));
                clearerLeft.addRule(new Rule(stringy, "X", Movement.LEFT, clearerLeft));
            }
        }
        resetStatus.addRule(new Rule("X", "X", Movement.LEFT, clearerLeft));
        checkStatus.addRule(new Rule("X", "X", Movement.RIGHT, clearerRightCheck));
        chooserStatus.addRule(new Rule("X", "X", Movement.RIGHT, clearerRight));
    }

    private void createResetStatus(){
        for(String stringy : allPosibleCombinations){
            
            if(stringy.equals("#") || stringy.equals("X")){
                checkStatus.addRule(new Rule(stringy, stringy, Movement.RIGHT, checkStatus));
                resetStatus.addRule(new Rule(stringy, stringy, Movement.LEFT, resetStatus));
            }else{
                if(stringy.equals("#X")){
                    checkStatus.addRule(new Rule(stringy, stringy, Movement.RIGHT, checkStatus));
                    resetStatus.addRule(new Rule(stringy, "#", Movement.LEFT, resetStatus));
                }else{
                    checkStatus.addRule(new Rule(stringy, stringy, Movement.RIGHT, chooserStatus));
                    resetStatus.addRule(new Rule(stringy, stringy, Movement.LEFT, resetStatus));
                }
            }
        }
        chooserStatus.addRule(new Rule("Blank", "Blank", Movement.LEFT, resetStatus));
        resetStatus.addRule(new Rule("Blank", "Blank", Movement.RIGHT, checkStatus));
    }

    private void addMoversToChooser(){
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
                for(int k = 0; k < threadCount; k++){
                    moveCombinations = createMovementCombinations(k);
                    if(k == 0){
                        moveCombinations.add("");
                    }
                    for(int l = 0; l < moveCombinations.size(); l++){
                        chooserStatus.addRule(new Rule(sign.toString() + moveCombinations.get(l) + "Right#", write.toString() + moveCombinations.get(l), Movement.LEFT, moveStarterStatuses.get(k*2+1)));
                        chooserStatus.addRule(new Rule(sign.toString() + moveCombinations.get(l) + "Stay#", write.toString() + moveCombinations.get(l), Movement.STAY, chooserStatus));
                        chooserStatus.addRule(new Rule(sign.toString() + moveCombinations.get(l) + "Left#", write.toString() + moveCombinations.get(l), Movement.RIGHT, moveStarterStatuses.get(k*2)));
                    }
                }
            }
        }
        for(int i = 0;i<combinations.size();i++){
            chooserStatus.addRule(new Rule(createCombinationString(combinations.get(i)), createCombinationString(combinations.get(i)), Movement.RIGHT, chooserStatus));
        }
    }
}
