package Converters;

import java.util.ArrayList;

import Data.Head;
import Data.Rule;
import Data.Status;
import Resources.Movement;

public class MultiTape {
    private String input;
    private String output;
    private Head head;
    private ArrayList<String> states;
    private ArrayList<String> alphabet;
    private ArrayList<ParsedRule> inputRules;
    private int tapeNumber;
    private ArrayList<Status> statuses;
    private Status startStatus;
    private ArrayList<ParsedRule> acceptRules = new ArrayList<ParsedRule>();
    private ArrayList<ParsedRule> nonAcceptRules = new ArrayList<ParsedRule>();
    private Status controlStatusRight;
    private Status controlStatusLeft;
    private String startState;
    private ArrayList<Status> writerStatuses = new ArrayList<Status>();


    public MultiTape(String input) {
        this.input = input;
    }

    /**
     * Converts the input string to a Turing machine
     */
    public void convert() {
        output = "";
        parse();
        if(!check()){
            return;
        }
        statuses = new ArrayList<Status>();
        splitRules();
        createStatuses();
        setStarterStatus();
        createSetupRules();
        StringBuilder outputBuilder = new StringBuilder();
        for(Status status : statuses){
            outputBuilder.append(status.toString());
            outputBuilder.append("\n");
        }
        output = outputBuilder.toString();
    }

    private boolean check() {
        if(!output.equals("")) {
            return false;
        }
        if(states.isEmpty()) {
            output = "Empty input";
            return false;
        }
        tapeNumber = inputRules.get(0).getTapeLength();
        for (ParsedRule rule : inputRules) {
            if(!rule.validate()) {
                output = "Invalid rule";
                return false;
            }
            if(tapeNumber != rule.getTapeLength()) {
                output = "Invalid tape length";
                return false;
            }
        }
        if(tapeNumber == 0) {
            output = "No tapes";
            return false;
        }
        if(states.isEmpty()) {
            output = "No states";
            return false;
        }
        if(!states.contains(startState)) {
            output = "Start state not in states";
            return false;
        }
        return true;
    }

    /**
     * Parses the input string to get the states, alphabet and rules
     */
    private void parse(){
        Parser parser = new Parser(input);
        if(parser.isAccept()){
            states = parser.getStates();
            alphabet = parser.getAlphabet();
            inputRules = parser.getRules();
            startState = parser.getStartState();
        }else{
            output = "Problem with the line: " + parser.getBugged();
        }
    }
    
    /**
     * Creates the setup rules for the Turing machine
     * The setup rules are used to initialize the tapes
     */
    private void createSetupRules(){
        Status firstStatus = new Status("Setup1");
        Status startingStatus = new Status("Setup2");
        head = new Head(firstStatus);
        firstStatus.addRule(new Rule(" ", "*", Movement.RIGHT, startingStatus));
        for(String character : alphabet){
            firstStatus.addRule(new Rule(character, character + "*", Movement.RIGHT, startingStatus));
            startingStatus.addRule(new Rule(character, character, Movement.RIGHT, startingStatus));
        }
        ArrayList<Status> setupStatuses = new ArrayList<Status>();
        for(int i = 1; i < tapeNumber; i++){
            Status status = new Status("setup#" + i);
            setupStatuses.add(status);
            status = new Status("setup*" + i);
            setupStatuses.add(status);
        }
        startingStatus.addRule(new Rule(" ", " ", Movement.STAY, setupStatuses.get(0)));
        boolean isHashtag = true;
        for(int i = 0; i < setupStatuses.size() - 1; i++){
            if(isHashtag){
                setupStatuses.get(i).addRule(new Rule(" ", "#", Movement.RIGHT, setupStatuses.get(i + 1)));
                isHashtag = false;
            } else {
                setupStatuses.get(i).addRule(new Rule(" ", "*", Movement.RIGHT, setupStatuses.get(i + 1)));
                isHashtag = true;
            }
        }
        Status goBackStatus = new Status("goBackToStart");
        setupStatuses.get(setupStatuses.size()-1).addRule(new Rule(" ", "*", Movement.LEFT, goBackStatus));
        for(String character : alphabet){
            goBackStatus.addRule(new Rule(character, character, Movement.LEFT, goBackStatus));
            goBackStatus.addRule(new Rule(character+"*", character+"*", Movement.LEFT, goBackStatus));
        }
        Status startCheckStatus = new Status("startCheck");
        goBackStatus.addRule(new Rule("#", "#", Movement.LEFT, goBackStatus));
        goBackStatus.addRule(new Rule("*", "*", Movement.LEFT, goBackStatus));
        goBackStatus.addRule(new Rule(" ", " ", Movement.RIGHT, startCheckStatus));
        for(String character : alphabet){
            startCheckStatus.addRule(new Rule(character + "*", character + "*", Movement.STAY, startStatus));
        }
        startCheckStatus.addRule(new Rule("*", "*", Movement.STAY, startStatus));
        statuses.addAll(setupStatuses);
        statuses.add(goBackStatus);
        statuses.add(startCheckStatus);
        statuses.add(firstStatus);
        statuses.add(startingStatus);
    }

    /**
     * Splits the rules into accept and non-accept rules
     */
    private void splitRules(){
        for(ParsedRule rule : inputRules){
            if(rule.isAccept()){
                acceptRules.add(rule);
            } else {
                nonAcceptRules.add(rule);
            }
        }
    }

    /**
     * Creates the pushing rules for the Turing machine
     * @param movement The movement of the head
     */
    private ArrayList<Status> createPushingRules(Movement movement){
        ArrayList<Status> pushingStatuses = new ArrayList<Status>();
        ArrayList<Status> taggedPushingStatuses = new ArrayList<Status>();
        String statusName = "";
        if(movement == Movement.RIGHT){
            statusName = "RightPush";
        } else {
            statusName = "LeftPush";
        }
        for(String character : alphabet){
            Status status = new Status(statusName + "#" + character);
            pushingStatuses.add(status);
            status = new Status(statusName + "*" + character);
            taggedPushingStatuses.add(status);
        }
        for(int i = 0; i < alphabet.size(); i++){
            for(int j = 0; j < alphabet.size(); j++){
                pushingStatuses.get(i).addRule(new Rule(alphabet.get(j), alphabet.get(i), movement, pushingStatuses.get(j)));
                taggedPushingStatuses.get(i).addRule(new Rule(alphabet.get(j), alphabet.get(i)+"*", movement, pushingStatuses.get(j)));
                pushingStatuses.get(i).addRule(new Rule(alphabet.get(j)+"*", alphabet.get(i), movement, taggedPushingStatuses.get(j)));
            }
        }
        Status hashtagPush = new Status(statusName + "#");
        Status taggedPush = new Status(statusName + "*");
        for(int i = 0; i < alphabet.size(); i++){
            hashtagPush.addRule(new Rule(alphabet.get(i), "#", movement, pushingStatuses.get(i)));
            taggedPush.addRule(new Rule(alphabet.get(i)+"*", "*", movement, taggedPushingStatuses.get(i)));
            pushingStatuses.get(i).addRule(new Rule("#", alphabet.get(i), movement, hashtagPush));
            pushingStatuses.get(i).addRule(new Rule("*", alphabet.get(i), movement, taggedPush));
            taggedPushingStatuses.get(i).addRule(new Rule("*", alphabet.get(i)+"*", movement, taggedPush));
            taggedPushingStatuses.get(i).addRule(new Rule("#", alphabet.get(i)+"*", movement, hashtagPush));
            if(movement == Movement.RIGHT){
                pushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i), Movement.LEFT, controlStatusRight));
                taggedPushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i)+"*", Movement.LEFT, controlStatusRight));
            } else {
                pushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i), Movement.RIGHT, controlStatusLeft));
                taggedPushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i)+"*", Movement.RIGHT, controlStatusLeft));
            }
        }
        hashtagPush.addRule(new Rule("#", "#", movement, hashtagPush));
        hashtagPush.addRule(new Rule("*", "#", movement, taggedPush));
        taggedPush.addRule(new Rule("*", "*", movement, taggedPush));
        taggedPush.addRule(new Rule("#", "*", movement, hashtagPush));
        if(movement == Movement.RIGHT){
            hashtagPush.addRule(new Rule(" ", "#", Movement.LEFT, controlStatusRight));
            taggedPush.addRule(new Rule(" ", "*", Movement.LEFT, controlStatusRight));
        } else {
            hashtagPush.addRule(new Rule(" ", "#", Movement.RIGHT, controlStatusLeft));
            taggedPush.addRule(new Rule(" ", "*", Movement.RIGHT, controlStatusLeft));
        }
        ArrayList<Status> returnStatuses = new ArrayList<Status>();
        returnStatuses.add(hashtagPush);
        returnStatuses.add(taggedPush);
        returnStatuses.addAll(pushingStatuses);
        returnStatuses.addAll(taggedPushingStatuses);
        statuses.addAll(returnStatuses);
        return returnStatuses;
    }

    /**
     * Creates the reading statuses for the Turing machine
     * @return The reading statuses
     */
    private ArrayList<Status> createReadingStatuses(){
        ArrayList<Status> readingStatuses = new ArrayList<Status>();
        ArrayList<String> statusName = new ArrayList<String>();
        ArrayList<String> statusNameInprogressTape = new ArrayList<String>();
        ArrayList<String> statusNameLastTape = new ArrayList<String>();
        statusNameLastTape.add("");
        for(int i = 0; i < tapeNumber; i++){
            for(String character : alphabet){
                for(String lastName : statusNameLastTape){
                    statusNameInprogressTape.add(lastName + "#" + character);
                }
            }
            statusNameLastTape = statusNameInprogressTape;
            statusNameInprogressTape = new ArrayList<String>();
            if(i != tapeNumber-1){
                statusName.addAll(statusNameLastTape);
            }
        }
        for(String status : states){
            for(String name : statusName){
                Status readingStatus = new Status("read#" + status + name);                
                readingStatuses.add(readingStatus);
            }
            Status readingStatus = new Status("read#" + status);
            readingStatuses.add(readingStatus);
        }

        for(Status status : readingStatuses){
            for(String character : alphabet){
                status.addRule(new Rule(character, character, Movement.RIGHT, status));
            }
            status.addRule(new Rule("#", "#", Movement.RIGHT, status));
        }
        
        for(String status : states){
            for(String name : statusNameLastTape){
                Status readingStatus = new Status("read#" + status + name);                
                readingStatuses.add(readingStatus);
            }
            Status readingStatus = new Status("read#" + status);
            readingStatuses.add(readingStatus);
        }
        for(Status status : readingStatuses){
            for(String character : alphabet){
                for(Status nextStatus : readingStatuses){
                    if(nextStatus.getName().equals(status.getName() + "#" + character)){
                        status.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, nextStatus));
                        if(character.equals("_")){
                            status.addRule(new Rule("*", "*", Movement.RIGHT, nextStatus));
                        }
                    }
                }
            }
        }
        statuses.addAll(readingStatuses);
        return readingStatuses;
    }

    /**
     * Creates the writing statuses for the Turing machine
     * @return The writing statuses
     */
    private ArrayList<Status> createWritingStatuses(){
        ArrayList<Status> writingStatuses = new ArrayList<Status>();
        for(int i = 0; i <inputRules.size(); i++){
            for(int j = 0; j < tapeNumber; j++){
                Status status = new Status("write#"+ inputRules.get(i).getState() + "#rule" + i + "#tape" + j);
                Status writerStatus = new Status("write#" + inputRules.get(i).getState() + "#rule" + i + "#tape" + j + "#writer");
                if(inputRules.get(i).isAccept()){
                    status.setAccept();
                }else if(inputRules.get(i).getRead()[j].equals(" ") || inputRules.get(i).getRead()[j].equals("_")){
                    if(inputRules.get(i).getWrite()[j].equals(" ")){
                        status.addRule(new Rule("*", "*", convertMovement(inputRules.get(i).getMove()[j]), writerStatus));
                    } else {
                        status.addRule(new Rule("*", inputRules.get(i).getWrite()[j], convertMovement(inputRules.get(i).getMove()[j]), writerStatus));
                    }
                }else{
                    if(inputRules.get(i).getWrite()[j].equals(" ")){
                        status.addRule(new Rule(inputRules.get(i).getRead()[j]+"*", "*", convertMovement(inputRules.get(i).getMove()[j]), writerStatus));
                    } else {
                        status.addRule(new Rule(inputRules.get(i).getRead()[j]+"*", inputRules.get(i).getWrite()[j], convertMovement(inputRules.get(i).getMove()[j]), writerStatus));
                    }
                }
                writingStatuses.add(status);
                writerStatuses.add(writerStatus);
            }
        }
        for(Status status : writingStatuses){
            for(String character : alphabet){
                status.addRule(new Rule(character, character, Movement.RIGHT, status));
            }
            status.addRule(new Rule("#", "#", Movement.RIGHT, status));
        }
        int j = 0;
        for(int i = 0; i < writerStatuses.size(); i++){
            if(j == tapeNumber-1){
                j = 0;
            }else{
                j++;
                for(String character : alphabet){
                    writerStatuses.get(i).addRule(new Rule(character, character+"*", Movement.RIGHT, writingStatuses.get(i+1)));
                }
            }
        }
        statuses.addAll(writerStatuses);
        statuses.addAll(writingStatuses);
        return writingStatuses;
    }

    /**
     * Creates the control status for the Turing machine
     */
    private void createControlStatuses(ArrayList<Status> writingStatuses){
        int counter = 0;
        int ruleCounter = 0;
        for(Status status : writerStatuses){
            if(counter < tapeNumber-1){
                controlStatusLeft.addRule(new Rule(status.getName(), "*", Movement.RIGHT, writingStatuses.get(counter+1)));
                controlStatusRight.addRule(new Rule(status.getName(), "*", Movement.RIGHT, writingStatuses.get(counter+1)));
            } else {
                controlStatusLeft.addRule(createRule(status.getName(), "*", Movement.LEFT, "goToStartRead#"+inputRules.get(ruleCounter).getState()));
                controlStatusRight.addRule(createRule(status.getName(), "*", Movement.LEFT, "goToStartRead#"+inputRules.get(ruleCounter).getState()));
            }
            counter++;
        }
        for(String character : alphabet){
            controlStatusLeft.addRule(new Rule(character, character, Movement.LEFT, controlStatusLeft));
            controlStatusRight.addRule(new Rule(character, character, Movement.RIGHT, controlStatusRight));
        }
        controlStatusLeft.addRule(new Rule("#", "#", Movement.LEFT, controlStatusLeft));
        controlStatusRight.addRule(new Rule("#", "#", Movement.RIGHT, controlStatusRight));
        controlStatusLeft.addRule(new Rule("*", "*", Movement.LEFT, controlStatusLeft));
        controlStatusRight.addRule(new Rule("*", "*", Movement.RIGHT, controlStatusRight));
        statuses.add(controlStatusLeft);
        statuses.add(controlStatusRight);
    }

    private void setStarterStatus(){
        startStatus = searchStatus("read#"+startState);
    }

    private ArrayList<Status> createGoToStartReaderStatuses(){
        ArrayList<Status> goToStartStatuses = new ArrayList<Status>();
        for(String state : states){
            Status status = new Status("goToStartRead#" + state);
            goToStartStatuses.add(status);
        }
        for(Status status : goToStartStatuses){
            for(String character : alphabet){
                status.addRule(new Rule(character, character, Movement.LEFT, status));
                status.addRule(new Rule(character+"*", character+"*", Movement.LEFT, status));
            }
            status.addRule(new Rule("#", "#", Movement.LEFT, status));
            status.addRule(new Rule("*", "*", Movement.LEFT, status));
        }
        statuses.addAll(goToStartStatuses);
        return goToStartStatuses;
    }

    private ArrayList<Status> createGoToStartWriterStatuses(){
        ArrayList<Status> goToStartStatuses = new ArrayList<Status>();
        for(int i = 0; i < inputRules.size(); i++){
            Status status = new Status("goToStartWrite#" + i);
            goToStartStatuses.add(status);
        }
        for(Status status : goToStartStatuses){
            for(String character : alphabet){
                status.addRule(new Rule(character, character, Movement.LEFT, status));
                status.addRule(new Rule(character+"*", character+"*", Movement.LEFT, status));
            }
            status.addRule(new Rule("#", "#", Movement.LEFT, status));
            status.addRule(new Rule("*", "*", Movement.LEFT, status));
        }
        statuses.addAll(goToStartStatuses);
        return goToStartStatuses;
    }

    private void createStatuses(){
        controlStatusRight = new Status("ControlRight");
        controlStatusLeft = new Status("ControlLeft");
        ArrayList<Status> readingStatuses = createReadingStatuses();
        ArrayList<Status> writingStatuses = createWritingStatuses();
        ArrayList<Status> pushingStatusesRight = createPushingRules(Movement.RIGHT);        
        ArrayList<Status> pushingStatusesLeft = createPushingRules(Movement.LEFT);
        ArrayList<Status> goToStartReaderStatuses = createGoToStartReaderStatuses();
        ArrayList<Status> goToStartWriterStatuses = createGoToStartWriterStatuses();

        for (int i = 0; i < inputRules.size(); i++){
            StringBuilder statusName = new StringBuilder("read#");
            statusName.append(inputRules.get(i).getState());
            for(String read : inputRules.get(i).getRead()){
                statusName.append("#");
                statusName.append(read);
            }
            Status readingStatus = searchStatus(statusName.toString(), readingStatuses);
            for(String character : alphabet){
                readingStatus.addRule(new Rule(character, character, Movement.LEFT, goToStartWriterStatuses.get(i)));
            }
            readingStatus.addRule(new Rule("#", "#", Movement.LEFT, goToStartWriterStatuses.get(i)));
            readingStatus.addRule(new Rule(" ", " ", Movement.LEFT, goToStartWriterStatuses.get(i)));
            goToStartWriterStatuses.get(i).addRule(createRule(" ", " ", Movement.RIGHT, "write#"+inputRules.get(i).getState()+"#rule"+i+"#tape0", writingStatuses));
        }

        int counter = 0;
        for(int i = 0; i <inputRules.size(); i++){
            if(!inputRules.get(i).isAccept()){
                for(int j = 0; j < tapeNumber; j++){
                    if(j == tapeNumber-1){
                        writerStatuses.get(counter).addRule(createRule(" ", "*", Movement.LEFT, "goToStartRead#"+inputRules.get(i).getStateGoTo(), goToStartReaderStatuses));
                        for(String character : alphabet){
                            writerStatuses.get(counter).addRule(createRule(character, character+"*", Movement.LEFT, "goToStartRead#"+inputRules.get(i).getStateGoTo(), goToStartReaderStatuses));
                        }
                    }
                    if(inputRules.get(i).getMove()[j].equals(">")){
                        writerStatuses.get(counter).addRule(new Rule("#", writerStatuses.get(i).getName(), Movement.RIGHT, pushingStatusesRight.get(0)));
                    } else if(inputRules.get(i).getMove()[j].equals("<")){
                        writerStatuses.get(counter).addRule(new Rule("#", writerStatuses.get(i).getName(), Movement.RIGHT, pushingStatusesLeft.get(0)));
                    }
                    counter++;
                }
            }
        }

        for(int i = 0; i < states.size(); i++){
            goToStartReaderStatuses.get(i).addRule(createRule(" ", " ", Movement.RIGHT, "read#"+states.get(i), readingStatuses));
        }

        createControlStatuses(writingStatuses);
    }

    /**
     * Converts the movement string to a Movement object
     * @param move The movement string
     * @return The Movement object
     */
    private Movement convertMovement(String move){
        if(move.equals(">")){
            return Movement.RIGHT;
        } else if(move.equals("<")){
            return Movement.LEFT;
        } else {
            return Movement.STAY;
        }
    }

    private Rule createRule(String read, String write, Movement move, String nextState){
        for(Status status : statuses){
            if(status.getName().equals(nextState)){
                return new Rule(read, write, move, status);
            }
        }
        System.out.println("State not found " + nextState);
        return null;
    }

    private Status searchStatus(String name){
        for(Status status : statuses){
            if(status.getName().equals(name)){
                return status;
            }
        }
        System.out.println("Status not found " + name);
        return null;
    }

    private Status searchStatus(String name, ArrayList<Status> statusList){
        for(Status status : statusList){
            if(status.getName().equals(name)){
                return status;
            }
        }
        System.out.println("Status not found " + name);
        return null;
    }

    private Rule createRule(String read, String write, Movement move, String nextState, ArrayList<Status> possibleStates){
        for(Status status : possibleStates){
            if(status.getName().equals(nextState)){
                return new Rule(read, write, move, status);
            }
        }
        System.out.println("State not found " + nextState);
        return null;
    }

    public Head getHead() {
        return head;
    }

    public String getOutput() {
        return output;
    }
}

//writerstatuses handle empty character on the tape