package Data;

import Resources.Movement;

public class Rule {
    private String sign;
    private String write;
    private Movement direction;
    private Status next_state;


    public Rule(String sign, String write, Movement direction, Status next_state){
        this.sign = sign;
        this.write = write;
        this.direction = direction;
        this.next_state = next_state;
    }

    public String getSign() {
        return sign;
    }

    public String getWrite() {
        return write;
    }

    public Movement getDirection() {
        return direction;
    }

    public Status getNext_state() {
        return next_state;
    }

    public String toString(){
        try {
            return sign + "," + write + "," + direction + "," + next_state.getName();
        }catch (NullPointerException e){
            return sign + "," + write + "," + direction + "," + "null";
        }
    }
}
