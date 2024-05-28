package Visuals;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Data.Head;

public class SimulatorWindow extends JFrame{
    private static final long serialVersionUID = 1L;
    private JPanel panel;
    private JTextField inputArea;
    private JButton startButton;
    private JButton stepButton;
    //private Head head;
    private JTextField leftTape2;
    private JTextField leftTape;
    private JTextField middleTape;
    private JTextField rightTape;
    private JTextField rightTape2;
    private JTextField status;
    boolean running = false;

    public SimulatorWindow(Head head2, JFrame mainFrame){
        super("Simulator");
        //this.head = head;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainFrame.setEnabled(true);
            }
        });
        inputArea = new JTextField(20);
        startButton = new JButton("Start");
        stepButton = new JButton("Step");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                head2.reset();
                String input = inputArea.getText();
                String[] parts = input.split(";");
                for(int i = 0; i < parts.length; i++){
                    if(parts[i].length() == 0){
                        parts[i] = " ";
                    }
                }
                head2.setup(parts);
                ArrayList<String> lines = head2.getLines();
                leftTape2.setText(lines.get(0));
                leftTape.setText(lines.get(1));
                middleTape.setText(lines.get(2));
                rightTape.setText(lines.get(3));
                rightTape2.setText(lines.get(4));
                status.setText(head2.getStatusName());
                running = true;
            }
        });
        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(running == false){
                    return;
                }
                head2.activate();
                ArrayList<String> lines = head2.getLines();
                leftTape2.setText(lines.get(0));
                leftTape.setText(lines.get(1));
                middleTape.setText(lines.get(2));
                rightTape.setText(lines.get(3));
                rightTape2.setText(lines.get(4));
                if(head2.isStopped()){
                    running = false;
                    if(head2.isAccept()){
                        status.setText("Accepted");
                    }else{
                        status.setText("Rejected");
                    }
                }else{
                    status.setText(head2.getStatusName());
                }
            }
        });
        panel = new JPanel(new BorderLayout());
        JPanel tapePanel = new JPanel(new FlowLayout());
        int tapeLength = head2.getLongestStatusName();
        leftTape2 = new JTextField(tapeLength+2);
        leftTape = new JTextField(tapeLength+2);
        middleTape = new JTextField(tapeLength+2);
        rightTape = new JTextField(tapeLength+2);
        rightTape2 = new JTextField(tapeLength+2);
        leftTape2.setEditable(false);
        leftTape.setEditable(false);
        middleTape.setEditable(false);
        rightTape.setEditable(false);
        rightTape2.setEditable(false);
        leftTape2.setHorizontalAlignment(JLabel.CENTER);
        leftTape.setHorizontalAlignment(JLabel.CENTER);
        middleTape.setHorizontalAlignment(JLabel.CENTER);
        rightTape.setHorizontalAlignment(JLabel.CENTER);
        rightTape2.setHorizontalAlignment(JLabel.CENTER);
        tapePanel.add(leftTape2);
        tapePanel.add(leftTape);
        tapePanel.add(middleTape);
        tapePanel.add(rightTape);
        tapePanel.add(rightTape2);
        panel.add(tapePanel, BorderLayout.NORTH);
        status = new JTextField();
        status.setEditable(false);
        panel.add(status, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(inputArea);
        buttonPanel.add(startButton);
        buttonPanel.add(stepButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        add(panel);

        // Set window properties
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setLocationRelativeTo(mainFrame);
    }
}