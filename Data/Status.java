package Data;

import java.util.ArrayList;

public class Status {
    private String name;
    private ArrayList<Rule> rules = new ArrayList<Rule>();
    private boolean accept = false;

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
     * Sets the status to accept
     */
    public void setAccept(){
        accept = true;
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
     * Returns the rules of the status
     * @return
     */
    public ArrayList<Rule> getRules() {
        return rules;
    }

    /**
     * Returns if the status is accepting
     * @return
     */
    public boolean isAccept() {
        return accept;
    }

    /**
     * Returns the string representation of the status
     * @return
     */
    public String toString(){
        if(rules.isEmpty()){
            return "";
        }
        StringBuilder result = new StringBuilder();
        for(Rule r:rules){
            if(r == null){
                result.append(name + "null\n");
            }else{
            result.append(name + ": ");
            result.append(r.getReading() + "\n");
            result.append(r.getWrintingParts() + "\n");
            result.append("\n");
            }
        }
        return result.toString();
    }
}
