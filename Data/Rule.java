package Data;

import Resources.Movement;

public class Rule {
    private String sign;
    private String write;
    private Movement direction;
    private Status next_state;


    /**
     * Constructor for Rule
     * @param sign
     * @param write
     * @param direction
     * @param next_state
     */
    public Rule(String sign, String write, Movement direction, Status next_state){
        this.sign = sign;
        this.write = write;
        this.direction = direction;
        this.next_state = next_state;
    }

    /**
     * Returns the sign of the rule
     * @return
     */
    public String getSign() {
        return sign;
    }

    /**
     * Returns the write of the rule
     * @return
     */
    public String getWrite() {
        return write;
    }

    /**
     * Returns the direction of the rule
     * @return
     */
    public Movement getDirection() {
        return direction;
    }

    /**
     * Returns the next state of the rule
     * @return
     */
    public Status getNext_state() {
        return next_state;
    }

    /**
     * Returns the rule as a string
     */
    public String toString(){
        try {
            return sign + "," + write + "," + direction + "," + next_state.getName();
        }catch (NullPointerException e){
            return sign + "," + write + "," + direction + "," + "null";
        }
    }

    private String readable(String write2){
        if (!write2.equals(" ")) {
            return write2;
        } else {
            return "_";
        }
    }

    public String getWrintingParts(){
        if(next_state == null){
            return "NULL" + " " + readable(write) + " " + direction;
        }
        return next_state.getName() + " " + readable(write) + " " + direction;
    }

    public String getReading() {
        return readable(sign);
    }
}
