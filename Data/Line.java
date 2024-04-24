package Data;

import java.util.ArrayList;

import Resources.Movement;

public class Line {
    private Line left;
    private Line right;
    private String data;

    /**
     * Constructor for Line
     */
    public Line(){
        data = " ";
    }

    /**
     * Constructor for Line
     * @param data
     */
    public Line(String data){
        this.data = data;
    }

    /**
     * Writes data to the line
     * @param data
     */
    public void write(String data){
        this.data = data;
    }

    /**
     * Checks if the line is empty
     * @return
     */
    public boolean isEmpty(){
        if(data.equals(" ")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Reads the data from the line
     * @return
     */
    public String read(){
        return data;
    }

    /**
     * Steps the line in a direction
     * @param step
     * @return
     */
    public Line step(Movement step){
        switch (step){
            case LEFT: if(left == null){
                left = new Line();
                left.setRight(this);
            }
            return left;
            case RIGHT: if(right == null){
                right = new Line();
                right.setLeft(this);
            }
            return right;
            case STAY: return this;
        }
        return this;
    }

    /**
     * Returns the line with its neighbors
     * @return
     */
    public ArrayList<String> getLineWithNeigbors(){
        ArrayList<String> result = new ArrayList<String>();
        try{
            result.add(left.toString());
        }catch (NullPointerException e){
            result.add(" ");
        }
        result.add(data);
        try{
            result.add(right.toString());
        }catch (NullPointerException e){
            result.add(" ");
        }
        return result;
    }

    /**
     * Returns the line as a string
     */
    public String toString(){
        return data;
    }

    /**
     * Sets the right neighbor
     * @param right
     */
    public void setRight(Line right){
        this.right = right;
    }

    /**
     * Sets the left neighbor
     * @param left
     */
    public void  setLeft(Line left){
        this.left = left;
    }

    /**
     * Creates a string of the line
     * @return
     */
    public String createString(){
        if(right == null){
            return data;
        }else{
            return data + ", " + right.createString();
        }
    }

    /**
     * Searches for the left most line
     * @return
     */
    public String searchLeftMost(){
        if(left == null){
            return createString();
        }else{
            return left.searchLeftMost();
        }
    }
}
