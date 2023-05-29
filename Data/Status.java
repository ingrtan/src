package Data;

import java.util.ArrayList;

public class Status {
    private String name;
    private ArrayList<Rule> rules = new ArrayList<Rule>();

    /**
     * Constructor for Status
     * @param name
     */
    public Status(String name){
        this.name = name;
    }

    /**
     * Adds a rule to the status
     * @param rule
     */
    public void addRule(Rule rule){
        rules.add(rule);
    }

    /**
     * Returns the rule with the given sign
     * @param sign
     * @return
     */
    public Rule getRule(String sign){
        for(Rule r:rules){
            if(sign.equals(r.getSign())){
                return r;
            }
        }
        return null;
    }

    /**
     * Returns the name of the status
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the string representation of the status
     * @return
     */
    public String toString(){
        String result = name + "\n";
        for(Rule r:rules){
            result += r.toString() + "\n";
        }
        return result;
    }
}
