package Converters;

import java.util.ArrayList;

public class Parser {
    private String input;
    private ArrayList<String> states = new ArrayList<String>();
    private ArrayList<ParsedRule> rules = new ArrayList<ParsedRule>();
    private ArrayList<String> alphabet = new ArrayList<String>();

    public Parser(String input) {
        this.input = input;
        parse();
    }

    private void parse() {
        //split input into lines
        String[] lines = input.split("\n");
        //group lines into states
        int i = 0;
        ParsedRule rule = new ParsedRule();
        for (String line : lines) {
            if(i == 0) {
                String state = line.split(":")[0];
                if(!states.contains(state)) {
                    states.add(state);
                }
                rule = new ParsedRule();
                rule.setState(state);
            } else if(i == 1) {
                String[] parts = line.split(";");
                for (String part : parts) {
                    if(!alphabet.contains(part)) {
                        alphabet.add(part);
                    }
                }
                rule.setRead(parts);
            } else if(i == 2) {
                if(line.equals("accept")) {
                    rule.setAccept(true);
                } else {
                    rule.setAccept(false);
                    String[] parts = line.split(";");
                    int half = parts.length / 2;
                    String[] write = new String[half];
                    String[] move = new String[half];
                    boolean writePart = true;
                    int k = 0;
                    for (int j = 0; j < parts.length; j++) {
                    if(writePart) {
                        write[k] = parts[j];
                    } else {
                        move[k] = parts[j];
                        k++;
                    }
                    writePart = !writePart;
                } 
                }                               
            } else {
                rules.add(rule);
                i=-1;
            }
            //check if line is empty
            if(!line.equals("")) {
                i++;   
            }else {
                i = 0;
            }
        }
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public ArrayList<ParsedRule> getRules() {
        return rules;
    }

    public ArrayList<String> getAlphabet() {
        return alphabet;
    }
}
