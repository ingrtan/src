package Data;

import java.util.ArrayList;

import Resources.Movement;

public class Head {
    private Status current_state;
    private Line spot;
    private boolean stopped = false;
    private ArrayList<Status> states = new ArrayList<Status>();

    /**
     * Constructor for Head
     * @param start_state
     */
    public Head(Status start_state){
        current_state = start_state;
        spot = new Line();
    }

    /**
     * Constructor for Head
     * @param start_state
     * @param spot
     */
    public Head(Status start_state, Line spot){
        current_state = start_state;
        this.spot = spot;
    }

    /**
     * Returns the current state of the head
     * @return
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Returns the current statename of the head
     * @return
     */
    public String getStatusName(){
        return current_state.getName();
    }

    /**
     * Returns the current spot of the head
     * @return
     */
    public Line getSpot(){
        return spot;
    }

    /**
     * Steps the head one step.
     */
    public void run(){
        while(!stopped){
            activate();
        }
    }

    /**
     * Steps the head one step.
     * @param dirction
     */
    private void step(Movement dirction){
        spot = spot.step(dirction);
    }

    /**
     * Sets the states of the head
     * @param states
     */
    public void setStatuses(ArrayList<Status> states){
        this.states = states;
    }

    /**
     * Activates the head
     */
    private void activate(){
        Rule r = current_state.getRule(spot.read());
        System.out.println("\n Current state: " + current_state.getName());
        System.out.println(spot.read());
        System.out.println(spot.searchLeftMost());
        if(r == null){
            stopped = true;
            return;
        }
        System.out.println(r.toString());
        spot.write(r.getWrite());
        current_state = r.getNext_state();
        step(r.getDirection());
    }

    /**
     * Returns the states of the head
     * @return
     */
    public ArrayList<Status> getStatuses(){
        return states;
    }

    /**
     * Returns the lines of the head
     * @return
     */
    public ArrayList<String> getLines(){
        return spot.getLineWithNeigbors();
    }

    /**
     * Sets up the head
     * @param lines
     */
    public void setup(ArrayList<String> lines){
        Line current = spot;
        for(String s:lines){
            current.write(s);
            current = current.step(Movement.RIGHT);
        }
    }

    /**
     * Returns the current state of the head.
     * @return
     */
    public String getStatuString(){
        return current_state.toString();
    }
}
