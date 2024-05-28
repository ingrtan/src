package Converters;

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
    private Status controlStatusRight;
    private Status controlStatusLeft;
    private String startState;
    private ArrayList<Status> writerStatuses = new ArrayList<Status>();
    private ArrayList<Integer> nonDeterministicRules = new ArrayList<Integer>();
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
        output = "";
        parse();
        if(!check()){
            return;
        }
        statuses = new ArrayList<Status>();
        splitRules();
        searchNonDeterministicRuling();
        createStatuses();
        setStarterStatus();
        createSetupRules();
        StringBuilder outputBuilder = new StringBuilder();
        for(Status status : statuses){
            outputBuilder.append(status.toString());
            outputBuilder.append("\n");
        }
        output = outputBuilder.toString();
        head.setStatuses(statuses);
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
        goBackStatus.addRule(new Rule(" ", "&", Movement.RIGHT, startCheckStatus));
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
            taggedPushingStatuses.get(i).addRule(new Rule("&", alphabet.get(i)+"*", movement, andPush));
            if(movement == Movement.RIGHT){
                pushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i), Movement.LEFT, controlStatusRight));
                taggedPushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i)+"*", Movement.LEFT, controlStatusRight));
            } else {
                pushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i), Movement.RIGHT, controlStatusLeft));
                taggedPushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i)+"*", Movement.RIGHT, controlStatusLeft));
            }
            for(int j = 0; j < separatorStatusesState.size(); j++){
                pushingStatuses.get(i).addRule(new Rule("&" + states.get(j), alphabet.get(i), movement, separatorStatusesState.get(j)));
                taggedPushingStatuses.get(i).addRule(new Rule("&" + states.get(j), alphabet.get(i), movement, separatorStatusesState.get(j)));
            }
            for(int j = 0; j < separatorStatusesTag.size(); j++){
                pushingStatuses.get(i).addRule(new Rule("&" + j, alphabet.get(i), movement, separatorStatusesTag.get(j)));
                taggedPushingStatuses.get(i).addRule(new Rule("&" + j, alphabet.get(i), movement, separatorStatusesTag.get(j)));
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
        for(int j = 0; j < separatorStatusesState.size(); j++){
            hashtagPush.addRule(new Rule("&" + states.get(j), "#", movement, separatorStatusesState.get(j)));
            taggedPush.addRule(new Rule("&" + states.get(j), "*", movement, separatorStatusesState.get(j)));
        }
        for(int j = 0; j < separatorStatusesTag.size(); j++){
            hashtagPush.addRule(new Rule("&" + j, "#", movement, separatorStatusesTag.get(j)));
            taggedPush.addRule(new Rule("&" + j, "*", movement, separatorStatusesTag.get(j)));
        }
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
                controlStatusLeft.addRule(new Rule(writerStatuses.get(i).getName(), "*", Movement.RIGHT, writingStatuses.get(i+1)));
                controlStatusRight.addRule(new Rule(writerStatuses.get(i).getName(), "*", Movement.RIGHT, writingStatuses.get(i+1)));
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
        for(String character : alphabet){
            controlStatusLeft.addRule(new Rule(character, character, Movement.RIGHT, controlStatusLeft));
            controlStatusLeft.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, controlStatusLeft));
            controlStatusRight.addRule(new Rule(character, character, Movement.LEFT, controlStatusRight));
            controlStatusRight.addRule(new Rule(character+"*", character+"*", Movement.LEFT, controlStatusRight));
        }
        controlStatusLeft.addRule(new Rule("#", "#", Movement.RIGHT, controlStatusLeft));
        controlStatusRight.addRule(new Rule("#", "#", Movement.LEFT, controlStatusRight));
        controlStatusLeft.addRule(new Rule("*", "*", Movement.RIGHT, controlStatusLeft));
        controlStatusRight.addRule(new Rule("*", "*", Movement.LEFT, controlStatusRight));
        controlStatusLeft.addRule(new Rule("&", "&", Movement.RIGHT, controlStatusLeft));
        controlStatusRight.addRule(new Rule("&", "&", Movement.LEFT, controlStatusRight));
        for(String state : states){
            controlStatusLeft.addRule(new Rule("&"+state, "&"+state, Movement.RIGHT, controlStatusLeft));
            controlStatusRight.addRule(new Rule("&"+state, "&"+state, Movement.LEFT, controlStatusRight));
        }
        for(int i = 0; i < inputRules.size(); i++){
            controlStatusLeft.addRule(new Rule("&"+i, "&"+i, Movement.RIGHT, controlStatusLeft));
            controlStatusRight.addRule(new Rule("&"+i, "&"+i, Movement.LEFT, controlStatusRight));
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

    private ArrayList<Status> createGoToStartWriterStatusesND(){
        ArrayList<Status> goToStartStatuses = new ArrayList<Status>();
        for(int i = 0; i < nonDeterministicRuleGroup.size(); i++){
            StringBuilder statusName = new StringBuilder("");
            for(int j = 0; j < nonDeterministicRuleGroup.get(i).size(); j++){
                statusName.append("&");
                statusName.append(nonDeterministicRuleGroup.get(i).get(j));
            }
            Status status = new Status("goToStartWrite"+statusName.toString());
            status.addRule(new Rule("&", statusName.toString(), Movement.RIGHT, controlReaderStatus));
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
        ArrayList<Status> goToStartWriterStatusesND = createGoToStartWriterStatusesND();
        createCopyStatuses();

        for (int i = 0; i < inputRules.size(); i++){
            StringBuilder statusName = new StringBuilder("read#");
            statusName.append(inputRules.get(i).getState());
            for(String read : inputRules.get(i).getRead()){
                statusName.append("#");
                statusName.append(read);
            }
            if(!nonDeterministicRules.contains(i)){
                Status readingStatus = searchStatus(statusName.toString(), readingStatuses);
                for(String character : alphabet){
                    readingStatus.addRule(new Rule(character, character, Movement.LEFT, goToStartWriterStatuses.get(i)));
                }
                for(String status : states){
                    readingStatus.addRule(new Rule("&"+status, "&"+status, Movement.LEFT, goToStartWriterStatuses.get(i)));
                }
                readingStatus.addRule(new Rule("&", "&", Movement.LEFT, goToStartWriterStatuses.get(i)));
                readingStatus.addRule(new Rule("#", "#", Movement.LEFT, goToStartWriterStatuses.get(i)));
                readingStatus.addRule(new Rule(" ", " ", Movement.LEFT, goToStartWriterStatuses.get(i)));
            }
            controlWriterStatus.addRule(createRule("&"+i, "&", Movement.RIGHT, "write#"+inputRules.get(i).getState()+"#rule"+i+"#tape0", writingStatuses));
        }
        for(int i = 0; i < nonDeterministicRuleGroup.size(); i++){
            ArrayList<Integer> group = nonDeterministicRuleGroup.get(i);
            StringBuilder statusName = new StringBuilder("read#");
            statusName.append(inputRules.get(group.get(0)).getState());
            for(String read : inputRules.get(group.get(0)).getRead()){
                statusName.append("#");
                statusName.append(read);
            }
            Status readingStatus = searchStatus(statusName.toString(), readingStatuses);
            readingStatus.addRule(new Rule(" ", " ", Movement.LEFT, goToStartWriterStatusesND.get(i)));
            for(String character : alphabet){
                readingStatus.addRule(new Rule(character, character, Movement.LEFT, goToStartWriterStatusesND.get(i)));
            }
            for(String status : states){
                readingStatus.addRule(new Rule("&"+status, "&"+status, Movement.LEFT, goToStartWriterStatusesND.get(i)));
            }
            readingStatus.addRule(new Rule("&", "&", Movement.LEFT, goToStartWriterStatusesND.get(i)));

        }

        int counter = 0;
        for(int i = 0; i <inputRules.size(); i++){
            if(!inputRules.get(i).isAccept()){
                for(int j = 0; j < tapeNumber; j++){
                    if(j == tapeNumber-1){
                        writerStatuses.get(counter).addRule(createRule(" ", "*", Movement.LEFT, "goToStartRead#"+inputRules.get(i).getStateGoTo(), goToStartReaderStatuses));
                        controlStatusLeft.addRule(createRule(writerStatuses.get(counter).getName(), "*", Movement.LEFT, "goToStartRead#"+inputRules.get(i).getStateGoTo(), goToStartReaderStatuses));
                        controlStatusRight.addRule(createRule(writerStatuses.get(counter).getName(), "*", Movement.LEFT, "goToStartRead#"+inputRules.get(i).getStateGoTo(), goToStartReaderStatuses));
                        for(String character : alphabet){
                            writerStatuses.get(counter).addRule(createRule(character, character+"*", Movement.LEFT, "goToStartRead#"+inputRules.get(i).getStateGoTo(), goToStartReaderStatuses));
                        }
                        for(int k = 0; k < inputRules.size(); k++){
                            if(inputRules.get(i).getMove()[j].equals(">")){
                                writerStatuses.get(counter).addRule(createRule("&"+k, writerStatuses.get(i).getName(), Movement.RIGHT, "RightPush&"+k, pushingStatusesRight));
                            }
                        }
                    }
                    if(j == 0){
                        for(int k = 0; k < states.size(); k++){
                            if(inputRules.get(i).getMove()[j].equals("<")){
                                writerStatuses.get(counter).addRule(createRule("&"+k, writerStatuses.get(counter).getName(), Movement.LEFT, "LeftPush&"+states.get(k), pushingStatusesLeft));
                            }
                        }
                    }
                    if(inputRules.get(i).getMove()[j].equals(">")){
                        writerStatuses.get(counter).addRule(new Rule("#", writerStatuses.get(counter).getName(), Movement.RIGHT, pushingStatusesRight.get(0)));
                    } else if(inputRules.get(i).getMove()[j].equals("<")){
                        writerStatuses.get(counter).addRule(new Rule("#", writerStatuses.get(counter).getName(), Movement.LEFT, pushingStatusesLeft.get(0)));
                    }
                    counter++;
                }
            }else{
                counter += tapeNumber;
            }
        }

        for(int i = 0; i < states.size(); i++){
            controlReaderStatus.addRule(createRule("&"+i, "&", Movement.RIGHT, "read#"+states.get(i), readingStatuses));
        }

        setupNDControl(readingStatuses);
        cleanupRuling(readingStatuses);
        createControlStatuses(writingStatuses);
    }

    private void setupNDControl(ArrayList<Status> readingStatuses){
        Status goToRightControl = new Status("goToRightControl");
        for(String character : alphabet){
            goToRightControl.addRule(new Rule(character, character, Movement.LEFT, goToRightControl));
            goToRightControl.addRule(new Rule(character+"*", character+"*", Movement.LEFT, goToRightControl));
            controlReaderStatus.addRule(new Rule(character, character, Movement.RIGHT, controlReaderStatus));
            controlReaderStatus.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, controlReaderStatus));
            controlWriterStatus.addRule(new Rule(character, character, Movement.RIGHT, controlWriterStatus));
            controlWriterStatus.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, controlWriterStatus));
        }
        goToRightControl.addRule(new Rule("#", "#", Movement.LEFT, goToRightControl));
        goToRightControl.addRule(new Rule("*", "*", Movement.LEFT, goToRightControl));
        goToRightControl.addRule(new Rule("&", "&", Movement.LEFT, goToRightControl));
        controlReaderStatus.addRule(new Rule("#", "#", Movement.RIGHT, controlReaderStatus));
        controlReaderStatus.addRule(new Rule("*", "*", Movement.RIGHT, controlReaderStatus));
        controlWriterStatus.addRule(new Rule("#", "#", Movement.RIGHT, controlWriterStatus));
        controlWriterStatus.addRule(new Rule("*", "*", Movement.RIGHT, controlWriterStatus));
        controlReaderStatus.addRule(new Rule("&", "&", Movement.RIGHT, controlReaderStatus));
        controlWriterStatus.addRule(new Rule("&", "&", Movement.RIGHT, controlWriterStatus));
        for(String state: states){
            goToRightControl.addRule(new Rule("&"+state, "&"+state, Movement.LEFT, goToRightControl));
        }
        controlReaderStatus.addRule(new Rule(" ", " ", Movement.LEFT, copySearchStatus));
        controlWriterStatus.addRule(new Rule(" ", " ", Movement.LEFT, goToRightControl));
        goToRightControl.addRule(new Rule(" ", " ", Movement.RIGHT, controlReaderStatus));
        copySearchStatus.addRule(new Rule(" ", " ", Movement.RIGHT, controlWriterStatus));
        for(String status : states){
            controlReaderStatus.addRule(createRule("&"+status, "&", Movement.RIGHT, "read#"+status ,readingStatuses));
        }
        statuses.add(goToRightControl);
    }

    private void cleanupRuling(ArrayList<Status> statuses){
        ArrayList<String> statusNames = new ArrayList<String>();
        for(ParsedRule rule : inputRules){
            StringBuilder statusName = new StringBuilder("read#");
            statusName.append(rule.getState());
            for(String read : rule.getRead()){
                statusName.append("#");
                statusName.append(read);
            }
            statusNames.add(statusName.toString());
        }
        for(Status status : statuses){
            if(!statusNames.contains(status.getName())){
                for(String character : alphabet){
                    status.addRule(new Rule(character,character,Movement.RIGHT,controlReaderStatus));
                }
                status.addRule(new Rule(" "," ",Movement.STAY,controlReaderStatus));
                status.addRule(new Rule("&","&",Movement.RIGHT,controlReaderStatus));
                for(String state : states){
                    status.addRule(new Rule("&"+state,"&"+state,Movement.STAY,controlReaderStatus));
                }
            }
        }
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

    private void createCopyStatuses(){
        Status copyControlStatus = new Status("copyControl");
        Status copyControlStatus2 = new Status("copyControl2");
        ArrayList<Status> copyStatuses = new ArrayList<Status>();
        for(String character : alphabet){
            Status status = new Status("copy#" + character);
            status.addRule(new Rule(" ", character, Movement.LEFT, copyControlStatus));
            copyControlStatus2.addRule(new Rule(character, character+"$", Movement.RIGHT, status));
            copyStatuses.add(status);
            status = new Status("copy#" + character + "*");
            status.addRule(new Rule(" ", character+"*", Movement.LEFT, copyControlStatus));
            copyControlStatus2.addRule(new Rule(character+"*", character+"*$", Movement.RIGHT, status));
            copyStatuses.add(status);
        }
        Status state = new Status("copy#");
        state.addRule(new Rule(" ", "#", Movement.LEFT, copyControlStatus));
        copyControlStatus2.addRule(new Rule("#", "#$", Movement.RIGHT, state));
        copyStatuses.add(state);
        state = new Status("copy#*");
        state.addRule(new Rule(" ", "*", Movement.LEFT, copyControlStatus));
        copyControlStatus2.addRule(new Rule("*", "*$", Movement.RIGHT, state));
        copyStatuses.add(state);
        for(String character : alphabet){
            copyControlStatus.addRule(new Rule(character+"$", character, Movement.RIGHT, copyControlStatus2));
            copyControlStatus.addRule(new Rule(character+"*$", character+"*", Movement.RIGHT, copyControlStatus2));
            copyControlStatus.addRule(new Rule(character, character, Movement.LEFT, copyControlStatus));
            copyControlStatus.addRule(new Rule(character+"*", character+"*", Movement.LEFT, copyControlStatus));
        }
        copyControlStatus.addRule(new Rule("#$", "#", Movement.RIGHT, copyControlStatus2));
        copyControlStatus.addRule(new Rule("*$", "*", Movement.RIGHT, copyControlStatus2));
        copyControlStatus.addRule(new Rule("#", "#", Movement.LEFT, copyControlStatus));
        copyControlStatus.addRule(new Rule("*", "*", Movement.LEFT, copyControlStatus));
        copyControlStatus.addRule(new Rule("&", "&", Movement.LEFT, copyControlStatus));
        for(int i = 0; i < inputRules.size(); i++){
            copyControlStatus.addRule(new Rule("&"+i, "&"+i, Movement.LEFT, copyControlStatus));
        }
        setupCopySearchStatus(copyControlStatus, copyControlStatus2, copyStatuses);
        statuses.add(copyControlStatus);
        statuses.add(copyControlStatus2);
    }

    private void setupCopySearchStatus(Status copyControlStatus, Status copyControlStatus2, ArrayList<Status> copyStatuses){
        copySearchStatus = new Status("copySearch");
        for(String character : alphabet){
            copySearchStatus.addRule(new Rule(character, character, Movement.LEFT, copySearchStatus));
            copySearchStatus.addRule(new Rule(character+"*", character+"*", Movement.LEFT, copySearchStatus));
        }
        copySearchStatus.addRule(new Rule("#", "#", Movement.LEFT, copySearchStatus));
        copySearchStatus.addRule(new Rule("*", "*", Movement.LEFT, copySearchStatus));
        copySearchStatus.addRule(new Rule("&", "&", Movement.LEFT, copySearchStatus));
        for(int i = 0; i < inputRules.size(); i++){
            copySearchStatus.addRule(new Rule("&"+i, "&"+i, Movement.LEFT, copySearchStatus));
        }
        StringBuilder alphabetBuilder = new StringBuilder();
        for(ArrayList<Integer> group : nonDeterministicRuleGroup){
            alphabetBuilder = new StringBuilder();
            for(int i = 0; i < group.size(); i++){
                if(i != 0){
                    Status status = new Status("copy#&" + group.get(i));
                    copySearchStatus.addRule(new Rule(alphabetBuilder.toString()+"&"+group.get(i), alphabetBuilder.toString()+"$", Movement.RIGHT, status));
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
            status.addRule(new Rule("&", "&", Movement.RIGHT, status));
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
        nonDeterministicRules = ruleIndexes;
        nonDeterministicRuleGroup = ruleIndexGroups;
    }

    public Head getHead() {
        return head;
    }

    public String getOutput() {
        return output;
    }

}

//create cleanup for read if there is no rule