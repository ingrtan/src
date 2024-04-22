package Converters;

public class ParsedRule {
    private String state;
    private String[] read;
    private String[] write;
    private String[] move;
    private boolean accept = false;

    public ParsedRule() {
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setRead(String[] read) {
        this.read = read;
    }

    public void setWrite(String[] write) {
        this.write = write;
    }

    public void setMove(String[] move) {
        this.move = move;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public int getTapeLength() {
        return read.length;
    }

    public boolean validate() {
        return (read.length == write.length && write.length == move.length) || accept;
    }

    public String getState() {
        return state;
    }

    public String[] getRead() {
        return read;
    }

    public String[] getWrite() {
        return write;
    }

    public String[] getMove() {
        return move;
    }

    public boolean isAccept() {
        return accept;
    }
}
