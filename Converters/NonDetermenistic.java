package Converters;

import java.lang.reflect.Array;
import java.util.ArrayList;

import Data.Head;
import Data.Rule;
import Data.Status;
import Resources.Movement;

public class NonDetermenistic {
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
    private Status acceptStatus;
    private Status controlStatusRight;
    private Status controlStatusLeft;
    private String startState;
    private ArrayList<Status> writerStatuses = new ArrayList<Status>();
    private ArrayList<ArrayList<Integer>> nonDeterministicRuleGroup = new ArrayList<ArrayList<Integer>>();
    private Status controlReaderStatus;
    private Status controlWriterStatus;
    private Status copySearchStatus;


    public NonDetermenistic(String input) {
        this.input = input;
    }

    /**
     * Converts the input string to a Turing machine
     */
    public void convert() {
        parse();
        if(!check()){
            return;
        }
        statuses = new ArrayList<Status>();
        startStatus = new Status("start");
        statuses.add(startStatus);
        splitRules();
        searchNonDeterministicRuling();
        createStatuses();
        setStarterStatus();
        createSetupRules();
        head = new Head(startStatus);
        StringBuilder outputBuilder = new StringBuilder();
        for(Status status : statuses){
            outputBuilder.append(status.toString());
            outputBuilder.append("\n");
        }
        output = outputBuilder.toString();
    }

    private boolean check() {
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
        states = parser.getStates();
        alphabet = parser.getAlphabet();
        inputRules = parser.getRules();
        startState = parser.getStartState();
    }
    
    /**
     * Creates the setup rules for the Turing machine
     * The setup rules are used to initialize the tapes
     */
    private void createSetupRules(){
        Status firstStatus = new Status("Setup1");
        Status startingStatus = new Status("Setup2");
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
        setupStatuses.get(setupStatuses.size()-1).addRule(new Rule(" ", "#", Movement.LEFT, goBackStatus));
        for(String character : alphabet){
            goBackStatus.addRule(new Rule(character, character, Movement.LEFT, goBackStatus));
        }
        Status startCheckStatus = new Status("startCheck");
        goBackStatus.addRule(new Rule("#", "#", Movement.LEFT, goBackStatus));
        goBackStatus.addRule(new Rule("*", "*", Movement.LEFT, goBackStatus));
        goBackStatus.addRule(new Rule(" ", "&"+startState, Movement.STAY, startCheckStatus));
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
        Status andPush = new Status(statusName + "&");
        ArrayList<Status> separatorStatusesState = new ArrayList<Status>();
        ArrayList<Status> separatorStatusesTag = new ArrayList<Status>();
        for(int i = 0; i<states.size(); i++){
            Status status = new Status(statusName + "&" + states.get(i));
            for(int j = 0; j < alphabet.size(); j++){
                status.addRule(new Rule(alphabet.get(j), "&" + states.get(i), movement, pushingStatuses.get(j)));
                status.addRule(new Rule(alphabet.get(j)+"*", "&" + states.get(i), movement, taggedPushingStatuses.get(j)));
            }
            status.addRule(new Rule("#", "&" + states.get(i), movement, hashtagPush));
            status.addRule(new Rule("*", "&" + states.get(i), movement, taggedPush));
            separatorStatusesState.add(status);
        }
        for(int i = 0; i < inputRules.size(); i++){
            Status status = new Status(statusName + "&" + i);
            for(int j = 0; j < alphabet.size(); j++){
                status.addRule(new Rule(alphabet.get(j), "&" + i, movement, pushingStatuses.get(j)));
                status.addRule(new Rule(alphabet.get(j)+"*", "&" + i, movement, taggedPushingStatuses.get(j)));
            }
            status.addRule(new Rule("#", "&" + i, movement, hashtagPush));
            status.addRule(new Rule("*", "&" + i, movement, taggedPush));
            separatorStatusesTag.add(status);
        }
        for(int i = 0; i < alphabet.size(); i++){
            hashtagPush.addRule(new Rule(alphabet.get(i), "#", movement, pushingStatuses.get(i)));
            taggedPush.addRule(new Rule(alphabet.get(i)+"*", "*", movement, taggedPushingStatuses.get(i)));
            pushingStatuses.get(i).addRule(new Rule("#", alphabet.get(i), movement, hashtagPush));
            pushingStatuses.get(i).addRule(new Rule("*", alphabet.get(i), movement, taggedPush));
            pushingStatuses.get(i).addRule(new Rule("&", alphabet.get(i), movement, andPush));
            taggedPushingStatuses.get(i).addRule(new Rule("*", alphabet.get(i)+"*", movement, taggedPush));
            taggedPushingStatuses.get(i).addRule(new Rule("#", alphabet.get(i)+"*", movement, hashtagPush));
            if(movement == Movement.RIGHT){
                pushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i), Movement.LEFT, controlStatusRight));
                taggedPushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i)+"*", Movement.LEFT, controlStatusRight));
            } else {
                pushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i), Movement.RIGHT, controlStatusLeft));
                taggedPushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i)+"*", Movement.RIGHT, controlStatusLeft));
            }
            for(int j = 0; j < separatorStatusesState.size(); j++){
                pushingStatuses.get(i).addRule(new Rule(alphabet.get(j), "&" + states.get(j), movement, separatorStatusesState.get(j)));
                taggedPushingStatuses.get(i).addRule(new Rule(alphabet.get(j), "&" + states.get(j), movement, separatorStatusesState.get(j)));
            }
            for(int j = 0; j < separatorStatusesTag.size(); j++){
                pushingStatuses.get(i).addRule(new Rule(alphabet.get(j), "&" + i, movement, separatorStatusesTag.get(j)));
                taggedPushingStatuses.get(i).addRule(new Rule(alphabet.get(j), "&" + i, movement, separatorStatusesTag.get(j)));
            }
        }
        hashtagPush.addRule(new Rule("#", "#", movement, hashtagPush));
        hashtagPush.addRule(new Rule("*", "#", movement, taggedPush));
        hashtagPush.addRule(new Rule("&", "#", movement, andPush));
        taggedPush.addRule(new Rule("*", "*", movement, taggedPush));
        taggedPush.addRule(new Rule("#", "*", movement, hashtagPush));
        taggedPush.addRule(new Rule("&", "*", movement, andPush));
        andPush.addRule(new Rule("&", "&", movement, andPush));
        andPush.addRule(new Rule("#", "&", movement, hashtagPush));
        andPush.addRule(new Rule("*", "&", movement, taggedPush));
        if(movement == Movement.RIGHT){
            hashtagPush.addRule(new Rule(" ", "#", Movement.LEFT, controlStatusRight));
            taggedPush.addRule(new Rule(" ", "*", Movement.LEFT, controlStatusRight));
            andPush.addRule(new Rule(" ", "&", Movement.LEFT, controlStatusRight));
        } else {
            hashtagPush.addRule(new Rule(" ", "#", Movement.RIGHT, controlStatusLeft));
            taggedPush.addRule(new Rule(" ", "*", Movement.RIGHT, controlStatusLeft));
            andPush.addRule(new Rule(" ", "&", Movement.RIGHT, controlStatusLeft));
        }
        ArrayList<Status> returnStatuses = new ArrayList<Status>();
        returnStatuses.add(hashtagPush);
        returnStatuses.add(taggedPush);
        returnStatuses.add(andPush);
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
            statusName.addAll(statusNameLastTape);
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
                for(Status nextStatus : readingStatuses){
                    if(nextStatus.getName().equals(status.getName() + "#" + character)){
                        status.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, nextStatus));
                        if(character.equals("_")){
                            status.addRule(new Rule("*", "*", Movement.RIGHT, nextStatus));
                        }
                    }
                }
            }
            status.addRule(new Rule("#", "#", Movement.RIGHT, status));
        }
        statuses.addAll(readingStatuses);
        return readingStatuses;
    }

    /**
     * Creates the moving statuses for the Turing machine
     * @return The moving statuses
     */
    private ArrayList<Status> createMovingStatuses(){
        ArrayList<Status> movingStatuses = new ArrayList<Status>();
        for(String state : states){
            Status status = new Status("move#" + state);
            movingStatuses.add(status);
        }
        return movingStatuses;
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
                }else if(inputRules.get(i).getRead()[j].equals(" ")){
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
            status.addRule(new Rule("&", "&"+state, Movement.RIGHT, controlWriterStatus));
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
            status.addRule(new Rule("&", "&"+i, Movement.RIGHT, controlReaderStatus));
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
        controlReaderStatus = new Status("controlReader");
        controlWriterStatus = new Status("controlWriter");
        ArrayList<Status> readingStatuses = createReadingStatuses();
        ArrayList<Status> writingStatuses = createWritingStatuses();
        ArrayList<Status> pushingStatusesRight = createPushingRules(Movement.RIGHT);        
        ArrayList<Status> pushingStatusesLeft = createPushingRules(Movement.LEFT);
        ArrayList<Status> goToStartReaderStatuses = createGoToStartReaderStatuses();
        ArrayList<Status> goToStartWriterStatuses = createGoToStartWriterStatuses();
        ArrayList<Status> copyStatuses = createCopyStatuses();

        for (int i = 0; i < inputRules.size(); i++){
            StringBuilder statusName = new StringBuilder("read#");
            statusName.append(inputRules.get(i).getState());
            for(String read : inputRules.get(i).getRead()){
                statusName.append("#");
                statusName.append(read);
            }
            searchStatus(statusName.toString(), readingStatuses).addRule(new Rule(" ", " ", Movement.LEFT, goToStartWriterStatuses.get(i)));
            //goToStartWriterStatuses.get(i).addRule(createRule(" ", " ", Movement.RIGHT, "write#"+inputRules.get(i).getState()+"#rule"+i+"#tape0", writingStatuses));
            controlWriterStatus.addRule(createRule("&"+1, "&", Movement.RIGHT, "write#"+inputRules.get(i).getState()+"#rule"+i+"#tape0", writingStatuses));
        }

        int counter = 0;
        for(int i = 0; i <inputRules.size(); i++){
            for(int j = 0; j < tapeNumber; j++){
                if(j == tapeNumber-1){
                    writerStatuses.get(counter).addRule(createRule(" ", " ", Movement.LEFT, "goToStartRead#"+inputRules.get(i).getState(), goToStartReaderStatuses));
                } 
                if(inputRules.get(i).getMove()[j].equals(">")){
                    writerStatuses.get(counter).addRule(new Rule("#", writerStatuses.get(i).getName(), Movement.RIGHT, pushingStatusesRight.get(0)));
                } else if(inputRules.get(i).getMove()[j].equals("<")){
                    writerStatuses.get(counter).addRule(new Rule("#", writerStatuses.get(i).getName(), Movement.RIGHT, pushingStatusesLeft.get(0)));
                }
                counter++;
            }
        }

        for(int i = 0; i < states.size(); i++){
            //goToStartReaderStatuses.get(i).addRule(createRule(" ", " ", Movement.RIGHT, "read#"+states.get(i), readingStatuses));
            controlReaderStatus.addRule(createRule("&"+i, "&", Movement.RIGHT, "read#"+states.get(i), readingStatuses));
        }

        Status goToRightControl = new Status("goToRightControl");
        for(String character : alphabet){
            goToRightControl.addRule(new Rule(character, character, Movement.LEFT, goToRightControl));
            goToRightControl.addRule(new Rule(character+"*", character+"*", Movement.LEFT, goToRightControl));
        }
        goToRightControl.addRule(new Rule("#", "#", Movement.LEFT, goToRightControl));
        goToRightControl.addRule(new Rule("*", "*", Movement.LEFT, goToRightControl));
        for(String state: states){
            goToRightControl.addRule(new Rule("&"+state, "&"+state, Movement.LEFT, goToRightControl));
        }
        controlReaderStatus.addRule(new Rule(" ", " ", Movement.LEFT, copySearchStatus));
        controlWriterStatus.addRule(new Rule(" ", " ", Movement.LEFT, goToRightControl));
        goToRightControl.addRule(new Rule(" ", " ", Movement.RIGHT, controlReaderStatus));

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

    private ArrayList<Status> createCopyStatuses(){
        Status copyControlStatus = new Status("copyControl");
        Status copyControlStatus2 = new Status("copyControl2");
        ArrayList<Status> copyStatuses = new ArrayList<Status>();
        ArrayList<String> alphabetExtended = new ArrayList<String>();
        alphabetExtended.addAll(alphabet);
        alphabetExtended.add("#");
        alphabetExtended.add("*");
        for(String character : alphabetExtended){
            Status status = new Status("copy#" + character);
            status.addRule(new Rule(" ", character, Movement.RIGHT, copyControlStatus));
            copyControlStatus2.addRule(new Rule(character, character+"$", Movement.RIGHT, status));
            copyStatuses.add(status);
            status = new Status("copy#" + character + "*");
            status.addRule(new Rule(" ", character+"*", Movement.RIGHT, copyControlStatus));
            copyControlStatus2.addRule(new Rule(character+"*", character+"*$", Movement.RIGHT, status));
            copyStatuses.add(status);
        }
        for(Status status : copyStatuses){
            for(String character : alphabet){
                status.addRule(new Rule(character, character, Movement.RIGHT, status));
                status.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, status));
            }
            status.addRule(new Rule("#", "#", Movement.RIGHT, status));
            status.addRule(new Rule("*", "*", Movement.RIGHT, status));
        }
        for(String character : alphabet){
            copyControlStatus.addRule(new Rule(character+"$", character, Movement.RIGHT, copyControlStatus2));
            copyControlStatus.addRule(new Rule(character+"*$", character+"*", Movement.RIGHT, copyControlStatus2));
        }
        setupCopySearchStatus(copyControlStatus, copyControlStatus2, copyStatuses);
        statuses.addAll(copyStatuses);
        statuses.add(copyControlStatus);
        statuses.add(copyControlStatus2);
        return copyStatuses;
    }

    private void setupCopySearchStatus(Status copyControlStatus, Status copyControlStatus2, ArrayList<Status> copyStatuses){
        copySearchStatus = new Status("copySearch");
        for(String character : alphabet){
            copySearchStatus.addRule(new Rule(character, character, Movement.LEFT, copySearchStatus));
            copySearchStatus.addRule(new Rule(character+"*", character+"*", Movement.LEFT, copySearchStatus));
        }
        copySearchStatus.addRule(new Rule("#", "#", Movement.LEFT, copySearchStatus));
        copySearchStatus.addRule(new Rule("*", "*", Movement.LEFT, copySearchStatus));
        for(int i = 0; i < inputRules.size(); i++){
            copySearchStatus.addRule(new Rule("&"+i, "&"+i, Movement.LEFT, copySearchStatus));
        }
        StringBuilder alphabetBuilder = new StringBuilder();
        for(ArrayList<Integer> group : nonDeterministicRuleGroup){
            alphabetBuilder = new StringBuilder();
            for(int i = 0; i < group.size(); i++){
                if(i != 0){
                    Status status = new Status("copy#&" + group.get(i));
                    copySearchStatus.addRule(new Rule(alphabetBuilder.toString()+group.get(i), alphabetBuilder.toString()+"$", Movement.RIGHT, status));
                    copyControlStatus.addRule(new Rule(alphabetBuilder.toString()+"$", alphabetBuilder.toString(), Movement.RIGHT, copyControlStatus2));
                    status.addRule(new Rule(" ", "&"+group.get(i), Movement.LEFT, copyControlStatus));
                    copyStatuses.add(status);
                }
                alphabetBuilder.append("&");
                alphabetBuilder.append(group.get(i));
            }
        }
        for(Status status : copyStatuses){
            for(String character : alphabet){
                status.addRule(new Rule(character, character, Movement.RIGHT, status));
                status.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, status));
            }
            status.addRule(new Rule("#", "#", Movement.RIGHT, status));
            status.addRule(new Rule("*", "*", Movement.RIGHT, status));
            for(int i = 0; i < inputRules.size(); i++){
                status.addRule(new Rule("&"+i, "&"+i, Movement.RIGHT, status));
            }
        }
        for(int i = 0; i < inputRules.size(); i++){
            copyControlStatus2.addRule(new Rule("&"+i,"&"+i, Movement.LEFT, copySearchStatus));
        }
        statuses.addAll(copyStatuses);
        statuses.add(copySearchStatus);
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

    private void searchNonDeterministicRuling(){
        ArrayList<Integer> ruleIndexes = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> ruleIndexGroups = new ArrayList<ArrayList<Integer>>();
        boolean isSame = false;
        for(int i = 0; i < inputRules.size(); i++){
            ArrayList<Integer> group = new ArrayList<Integer>();           
            for(int j = 0; j < inputRules.size(); j++){
                if(i != j){
                    for(int k = 0 ; k < inputRules.get(i).getTapeLength(); k++){
                        if(inputRules.get(i).getRead()[k].equals(inputRules.get(j).getRead()[k])){
                            isSame = true;
                        } else {
                            isSame = false;
                            break;
                        }
                    }
                    if(inputRules.get(i).getState().equals(inputRules.get(j).getState())&&isSame){
                        if(!ruleIndexes.contains(i)){
                            ruleIndexes.add(i);
                            group.add(i);
                        }
                        if(!ruleIndexes.contains(j)){
                            ruleIndexes.add(j);
                            group.add(j);
                        }
                        isSame = false;
                    }
                }
            }
            if(!group.isEmpty()){
                ruleIndexGroups.add(group);
            }
        }
        nonDeterministicRuleGroup = ruleIndexGroups;
    }

    private ArrayList<String> nonDetermenisticAlphabeth(){
        ArrayList<String> newAlphabet = new ArrayList<String>();
        StringBuilder alphabetBuilder = new StringBuilder();
        for(ArrayList<Integer> group : nonDeterministicRuleGroup){
            alphabetBuilder = new StringBuilder();
            for(int i = 0; i < group.size(); i++){
                alphabetBuilder.append("&");
                alphabetBuilder.append(group.get(i));
                if(i != 0){
                    newAlphabet.add(alphabetBuilder.toString());
                }
            }
        }
        return newAlphabet;
    }

    public Head getHead() {
        return head;
    }

    public String getOutput() {
        return output;
    }

}

//Writer control logic, reader control logic, chek if all statuses are added