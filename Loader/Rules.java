package Loader;

public class Rules {
    private String state;
    private String stateToMove;
    private String[] symbolToRead;
    private String[] symbolToWrite;
    private String[] direction;

    /**
     * Constructor for Rules
     * @param state
     * @param stateToMove
     * @param symbolToRead
     * @param symbolToWrite
     * @param direction
     */
    public Rules(String state, String stateToMove, String[] symbolToRead, String[] symbolToWrite, String[] direction) {
        this.state = state;
        this.stateToMove = stateToMove;
        this.symbolToRead = symbolToRead;
        this.symbolToWrite = symbolToWrite;
        this.direction = direction;
    }

    /**
     * Returns the state
     * @return
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the state to move
     * @return
     */
    public String getStateToMove() {
        return stateToMove;
    }

    /**
     * Returns the symbol to read
     * @return
     */
    public String[] getSymbolToRead() {
        return symbolToRead;
    }

    /**
     * Returns the symbol to write
     * @return
     */
    public String[] getSymbolToWrite() {
        return symbolToWrite;
    }

    /**
     * Returns the direction
     * @return
     */
    public String[] getDirection() {
        return direction;
    }
}
