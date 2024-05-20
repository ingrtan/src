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
    private Status acceptStatus;
    private Status controlStatus;


    public MultiTape(String input) {
        this.input = input;
    }

    /**
     * Converts the input string to a Turing machine
     */
    public void convert() {
        parse();
        if(states.isEmpty()) {
            output = "Empty input";
            return;
        }
        tapeNumber = inputRules.get(0).getTapeLength();
        for (ParsedRule rule : inputRules) {
            if(!rule.validate()) {
                output = "Invalid rule";
                return;
            }
            if(tapeNumber != rule.getTapeLength()) {
                output = "Invalid tape length";
                return;
            }
        }
        statuses = new ArrayList<Status>();
        startStatus = new Status("start");
        statuses.add(startStatus);
        createSetupRules();
        splitRules();
        head = new Head(startStatus);
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("tapes:" + tapeNumber);
        for(Status status : statuses){
            outputBuilder.append(status.toString());
            outputBuilder.append("\n");
        }
        output = outputBuilder.toString();
    }

    /**
     * Parses the input string to get the states, alphabet and rules
     */
    private void parse(){
        Parser parser = new Parser(input);
        states = parser.getStates();
        alphabet = parser.getAlphabet();
        inputRules = parser.getRules();
    }
    
    /**
     * Creates the setup rules for the Turing machine
     * The setup rules are used to initialize the tapes
     */
    private void createSetupRules(){
        Status startingStatus = new Status("startSetup");
        for(String character : alphabet){
            startingStatus.addRule(new Rule(character, character, Movement.RIGHT, startingStatus));
        }
        ArrayList<Status> setupStatuses = new ArrayList<Status>();
        setupStatuses.add(startingStatus);
        for(int i = 1; i < tapeNumber; i++){
            Status status = new Status("setup#" + i);
            setupStatuses.add(status);
            status = new Status("setup*" + i);
            setupStatuses.add(status);
        }
        boolean isHashtag = true;
        for(int i = 0; i < tapeNumber - 1; i++){
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
        goBackStatus.addRule(new Rule("#", "#", Movement.LEFT, setupStatuses.get(0)));
        goBackStatus.addRule(new Rule("*", "*", Movement.LEFT, goBackStatus));
        goBackStatus.addRule(new Rule(" ", " ", Movement.RIGHT, startCheckStatus));
        for(String character : alphabet){
            startCheckStatus.addRule(new Rule(character, character, Movement.STAY, startStatus));
        }
        startCheckStatus.addRule(new Rule(" ", "*", Movement.STAY, startStatus));
        statuses.addAll(setupStatuses);
        statuses.add(goBackStatus);
        statuses.add(startCheckStatus);
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
        for(String character : alphabet){
            Status status = new Status("push#" + character);
            pushingStatuses.add(status);
            status = new Status("push*" + character);
            taggedPushingStatuses.add(status);
        }
        for(int i = 0; i < alphabet.size(); i++){
            for(int j = 0; j < alphabet.size(); j++){
                pushingStatuses.get(i).addRule(new Rule(alphabet.get(j), alphabet.get(i), movement, pushingStatuses.get(j)));
                taggedPushingStatuses.get(i).addRule(new Rule(alphabet.get(j), alphabet.get(i)+"*", movement, pushingStatuses.get(j)));
                pushingStatuses.get(i).addRule(new Rule(alphabet.get(j)+"*", alphabet.get(i), movement, taggedPushingStatuses.get(j)));
            }
        }
        Status hashtagPush = new Status("push#");
        Status taggedPush = new Status("push*");
        for(int i = 0; i < alphabet.size(); i++){
            hashtagPush.addRule(new Rule(alphabet.get(i), "#", movement, pushingStatuses.get(i)));
            taggedPush.addRule(new Rule(alphabet.get(i)+"*", "*", movement, taggedPushingStatuses.get(i)));
            pushingStatuses.get(i).addRule(new Rule("#", alphabet.get(i), movement, hashtagPush));
            pushingStatuses.get(i).addRule(new Rule("*", alphabet.get(i), movement, taggedPush));
            pushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i), movement, controlStatus));
            taggedPushingStatuses.get(i).addRule(new Rule("*", alphabet.get(i)+"*", movement, taggedPush));
            taggedPushingStatuses.get(i).addRule(new Rule("#", alphabet.get(i)+"*", movement, hashtagPush));
            taggedPushingStatuses.get(i).addRule(new Rule(" ", alphabet.get(i)+"*", movement, controlStatus));
        }
        hashtagPush.addRule(new Rule("#", "#", movement, hashtagPush));
        hashtagPush.addRule(new Rule("*", "#", movement, taggedPush));
        taggedPush.addRule(new Rule("*", "*", movement, taggedPush));
        taggedPush.addRule(new Rule("#", "*", movement, hashtagPush));   
        hashtagPush.addRule(new Rule(" ", "#", movement, controlStatus));
        taggedPush.addRule(new Rule(" ", "*", movement, controlStatus));

        ArrayList<Status> returnStatuses = new ArrayList<Status>();
        returnStatuses.add(hashtagPush);
        returnStatuses.add(taggedPush);
        returnStatuses.addAll(pushingStatuses);
        returnStatuses.addAll(taggedPushingStatuses);
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
                Status readingStatus = new Status("read#" + status + "#" + name);                
                readingStatuses.add(readingStatus);
            }
            Status readingStatus = new Status("read#" + status);
            readingStatuses.add(readingStatus);
        }
        for(Status status : readingStatuses){
            for(String character : alphabet){
                status.addRule(new Rule(character, character, Movement.RIGHT, status));
                for(Status nextStatus : readingStatuses){
                    if(nextStatus.getName().contains(status.getName() + "#" + character)){
                        status.addRule(new Rule(character+"*", character+"*", Movement.RIGHT, nextStatus));
                    }
                }
            }
            status.addRule(new Rule("#", "#", Movement.RIGHT, status));
        }
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
        ArrayList<Status> writerStatuses = new ArrayList<Status>();
        for(int i = 0; i <inputRules.size(); i++){
            for(int j = 0; j < tapeNumber; j++){
                Status status = new Status("write#"+ inputRules.get(i).getState() + "#" + i + "#" + j);
                Status writerStatus = new Status("write#" + inputRules.get(i).getState() + "#" + i + "#" + j + "#writer");
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
                        status.addRule(new Rule(inputRules.get(i).getRead()[j], "*", convertMovement(inputRules.get(i).getMove()[j]), writerStatus));
                    } else {
                        status.addRule(new Rule(inputRules.get(i).getRead()[j], inputRules.get(i).getWrite()[j], convertMovement(inputRules.get(i).getMove()[j]), writerStatus));
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
        return writingStatuses;
    }

    /**
     * Creates the control status for the Turing machine
     */
    private void createControlStatus(){
        controlStatus = new Status("control");
    }

    private void createStatuses(){
        createControlStatus();
        ArrayList<Status> readingStatuses = createReadingStatuses();
        ArrayList<Status> movingStatuses = createMovingStatuses();
        ArrayList<Status> writingStatuses = createWritingStatuses();
        ArrayList<Status> pushingStatusesRight = createPushingRules(Movement.RIGHT);        
        ArrayList<Status> pushingStatusesLeft = createPushingRules(Movement.LEFT);

        //Connect start status to reading statuses
        for(int i=0; i<inputRules.size(); i++){
            StringBuilder statusName = new StringBuilder("read#");
            for(String read : inputRules.get(i).getRead()){
                
            }
        }

        //Add rules to writing statuses if the tape is the last tape, and connect reading statuses to writing statuses
       
        //Connect writing statuses to pushing statuses if the tape is ending

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

    public Head getHead() {
        return head;
    }

    public String getOutput() {
        return output;
    }
}