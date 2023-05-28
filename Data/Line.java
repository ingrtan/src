package Data;

import java.util.ArrayList;

import Resources.Movement;

public class Line {
    private Line left;
    private Line right;
    private String data;

    public Line(){
        data = "Blank";
    }

    public Line(String data){
        this.data = data;
    }

    public void write(String data){
        this.data = data;
    }

    public boolean isEmpty(){
        if(data.equals("Blank")){
            return true;
        }else{
            return false;
        }
    }

    public String read(){
        return data;
    }

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

    public ArrayList<String> getLineWithNeigbors(){
        ArrayList<String> result = new ArrayList<String>();
        try{
            result.add(left.toString());
        }catch (NullPointerException e){
            result.add("Blank");
        }
        result.add(data);
        try{
            result.add(right.toString());
        }catch (NullPointerException e){
            result.add("Blank");
        }
        return result;
    }

    public String toString(){
        return data;
    }

    public void setRight(Line right){
        this.right = right;
    }

    public void  setLeft(Line left){
        this.left = left;
    }

    public String createString(){
        if(right == null){
            return data;
        }else{
            return data + ", " + right.createString();
        }
    }

    public String searchLeftMost(){
        if(left == null){
            return createString();
        }else{
            return left.searchLeftMost();
        }
    }
}
