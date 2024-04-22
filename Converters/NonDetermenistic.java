package Converters;

import Data.Head;

public class NonDetermenistic {
    private String input;
    private String output;
    private Head head;


    public NonDetermenistic(String input) {
        this.input = input;
    }

    public void convert() {
    }

    public Head getHead() {
        return head;
    }

    public String getOutput() {
        return output;
    }
}