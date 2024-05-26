package Visuals;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import Data.Head;

public class SimulatorWindow extends JPanel{
    private Head head;
    private JPanel tapePanel;
    private JPanel statusPanel;
    private JPanel controlPanel;
    private JPanel logPanel;
    
    public SimulatorWindow(Head head){
        this.head = head;
    }

    public void createTapePanel(){
        tapePanel = new JPanel();
        tapePanel.setSize(800, 200);
        tapePanel.setVisible(true);
    }

    public void createStatusPanel(){
        statusPanel = new JPanel();
        statusPanel.setSize(800, 200);
        statusPanel.setVisible(true);
    }

    public void createControlPanel(){
        controlPanel = new JPanel();
        controlPanel.setSize(800, 200);
        controlPanel.setVisible(true);
    }

    public void createLogPanel(){
        logPanel = new JPanel();
        JTextArea log = new JTextArea();
        JScrollPane scroll = new JScrollPane(log);
        logPanel.add(scroll);
        logPanel.setVisible(true);
    }

    public void setHead(Head head){
        this.head = head;
    }
}
