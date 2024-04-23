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
    private int tapeLength;
    private ArrayList<Status> statuses;
    private Status startStatus;


    public MultiTape(String input) {
        this.input = input;
    }

    public void convert() {
        parse();
        tapeLength = inputRules.get(0).getTapeLength();
        for (ParsedRule rule : inputRules) {
            if(!rule.validate()) {
                output = "Invalid rule";
                return;
            }
            if(tapeLength != rule.getTapeLength()) {
                output = "Invalid tape length";
                return;
            }
        }
        statuses = new ArrayList<Status>();
        startStatus = new Status("start");
        statuses.add(startStatus);
        createSetupRules();
        createPushingRules();
        createReadingRules();
        createMovingRules();
        createWritingRules();
        head = new Head(startStatus);
    }

    private void parse(){
        Parser parser = new Parser(input);
        states = parser.getStates();
        alphabet = parser.getAlphabet();
        inputRules = parser.getRules();
    }
    
    private void createSetupRules(){
        Status startingStatus = new Status("startSetup");
        for(String character : alphabet){
            startingStatus.addRule(new Rule(character, character, Movement.RIGHT, startingStatus));
        }
        ArrayList<Status> setupStatuses = new ArrayList<Status>();
        setupStatuses.add(startingStatus);
        for(int i = 1; i < tapeLength; i++){
            Status status = new Status("setup#" + i);
            setupStatuses.add(status);
            status = new Status("setup*" + i);
            setupStatuses.add(status);
        }
        boolean isHashtag = true;
        for(int i = 0; i < tapeLength - 1; i++){
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

    private void createPushingRules(){
        for(ParsedRule rule : inputRules){
            
        }

    }

    private void createReadingRules(){
        for(ParsedRule rule : inputRules){
            
        }

    }

    private void createMovingRules(){
        for(ParsedRule rule : inputRules){
            
        }
    }

    private void createWritingRules(){
        for(ParsedRule rule : inputRules){
            
        }
    }

    public Head getHead() {
        return head;
    }

    public String getOutput() {
        return output;
    }
}
