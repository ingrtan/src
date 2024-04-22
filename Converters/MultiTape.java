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
        createSetupRules();
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
        goBackStatus.addRule(new Rule("#", "#", Movement.LEFT, setupStatuses.get(0)));
        goBackStatus.addRule(new Rule("*", "*", Movement.LEFT, goBackStatus));
        goBackStatus.addRule(new Rule(" ", " ", Movement.RIGHT, startStatus));
        statuses.addAll(setupStatuses);
        statuses.add(goBackStatus);
    }

    private void createPushingRules(){

    }

    private void createReadingRules(){

    }

    private void createMovingRules(){

    }

    private void createWritingRules(){

    }

    public Head getHead() {
        return head;
    }

    public String getOutput() {
        return output;
    }
}
