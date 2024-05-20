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
        // split input into lines
        String[] lines = input.split("\n");
        // group lines into states
        int i = 0;
        ParsedRule rule = new ParsedRule();
        for (String line : lines) {
            if (line.equals("")) {
                i = 0;
                continue;
            }
            if (line.equals("accept")) {
                rule.setAccept(true);
                rules.add(rule);
                i = 0;
            } else {
                if (i==0){
                    rule = new ParsedRule();
                    rule.setState(line.split(": ")[0]);
                    rule.setRead(line.split(": ")[1].split(";"));
                    i++;
                } else {
                    rule.setStateGoTo(line.split(": ")[0]);
                    rule.setWrite(line.split(": ")[1].split(";"));
                    rule.setMove(line.split(": ")[2].split(";"));
                    rules.add(rule);
                    i = 0;
                }
                String state = line.split(": ")[0];
                if (!states.contains(state)) {
                    states.add(state);
                }
                String[] parts = line.split(": ")[1].split(";");
                for (String part : parts) {
                    if (!alphabet.contains(part)) {
                        alphabet.add(part);
                    }
                }
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
