package Data;

import java.util.ArrayList;

import Resources.Movement;

public class Head {
    private Status current_state;
    private Line spot;
    private boolean stopped = false;
    private ArrayList<Status> states = new ArrayList<Status>();

    public Head(Status start_state){
        current_state = start_state;
        spot = new Line();
    }

    public Head(Status start_state, Line spot){
        current_state = start_state;
        this.spot = spot;
    }

    public boolean isStopped() {
        return stopped;
    }

    public String getStatusName(){
        return current_state.getName();
    }

    public Line getSpot(){
        return spot;
    }

    public void run(){
        while(!stopped){
            activate();
        }
    }

    private void write(String data){
        spot.write(data);
    }

    private void step(Movement dirction){
        spot = spot.step(dirction);
    }

    public void setStatuses(ArrayList<Status> states){
        this.states = states;
    }

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

    public ArrayList<Status> getStatuses(){
        return states;
    }

    public ArrayList<String> getLines(){
        return spot.getLineWithNeigbors();
    }

    public void setup(ArrayList<String> lines){
        Line current = spot;
        for(String s:lines){
            current.write(s);
            current = current.step(Movement.RIGHT);
        }
    }

    public String getStatuString(){
        return current_state.toString();
    }
}
