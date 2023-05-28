package Data;

import java.util.ArrayList;

public class Status {
    private String name;
    private ArrayList<Rule> rules = new ArrayList<Rule>();

    public Status(String name){
        this.name = name;
    }

    public void addRule(Rule rule){
        rules.add(rule);
    }

    public Rule getRule(String sign){
        for(Rule r:rules){
            if(sign.equals(r.getSign())){
                return r;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String toString(){
        String result = name + "\n";
        for(Rule r:rules){
            result += r.toString() + "\n";
        }
        return result;
    }
}
