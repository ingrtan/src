
package Visuals;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import Data.Head;
import Converters.MultiTape;
import Converters.NonDetermenistic;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * The main window of the application.
 */
public class MainWindow extends JFrame{
    private JFrame mainFrame;
    private JTextArea inputArea;
    private JTextArea convertedArea;
    private boolean nonDeterministic;
    private Head head;
    private JCheckBox deterministic;

    /**
     * Constructor.
     */
    public MainWindow(){
        initializeGUI();
    }

    /**
     * Initializes the GUI.
     */
    private void initializeGUI() {
        configureFrame();
        mainFrame = this;
        setJMenuBar(initializeMenubar());
        JPanel converterPanel = createConverterPanel();
        getContentPane().add(converterPanel);
        setVisible(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Configures the frame.
     */
    private void configureFrame() {
        setTitle("Turing converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        int Width = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int Height = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        this.setSize(Width/2,Height/2);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Creates the converter panel.
     * @return
     */
    private JPanel createConverterPanel() {
        JPanel converterPanel = new JPanel();
        converterPanel.setLayout(new BorderLayout());

        JSplitPane splitPane = createSplitPane();
        JPanel buttonPanel = createButtonPanel();

        converterPanel.add(splitPane, BorderLayout.CENTER);
        converterPanel.add(buttonPanel, BorderLayout.SOUTH);

        return converterPanel;
    }

    /**
     * Creates the split pane.
     * @return
     */
    private JSplitPane createSplitPane() {
        inputArea = new JTextArea();
        convertedArea = new JTextArea();
        convertedArea.setEditable(false);

        JScrollPane leftScrollPane = new JScrollPane(inputArea);
        JScrollPane rightScrollPane = new JScrollPane(convertedArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setResizeWeight(0.5);

        return splitPane;
    }

    /**
     * Creates the button panel.
     * @return
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton copyButton = new JButton("Convert");
        JButton simulateButton = new JButton("Simulate");
        deterministic = new JCheckBox("Non-Deterministic");
        buttonPanel.add(deterministic);
        deterministic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nonDeterministic = deterministic.isSelected();
            }
        });
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(nonDeterministic){
                    new NonDetermenistic(inputArea.getText());
                    NonDetermenistic nonDetermenistic = new NonDetermenistic(inputArea.getText());
                    nonDetermenistic.convert();
                    head = nonDetermenistic.getHead();
                    convertedArea.setText(nonDetermenistic.getOutput());
                }else{
                    new MultiTape(inputArea.getText());
                    MultiTape multiTape = new MultiTape(inputArea.getText());
                    multiTape.convert();
                    head = multiTape.getHead();
                    convertedArea.setText(multiTape.getOutput());
                }
            }
        });
        buttonPanel.add(copyButton);
        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(head != null){
                    SimulatorWindow simWin = new SimulatorWindow(head, simulateButton, mainFrame);
                    simulateButton.setEnabled(false);
                    simWin.setVisible(true);
                }else{
                    convertedArea.setText("Please convert first.");
                }
            }
        });
        buttonPanel.add(simulateButton);
        return buttonPanel;
    }

    /**
     * Saves the data to a file.
     */
    private void save(){
    }

    /**
     * Loads the data from a file.
     */
    private void load(){
    }

    private JMenuBar initializeMenubar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deterministic.setSelected(false);
                inputArea.setText("");
                convertedArea.setText("");
                head = null;
            }
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(newMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        return menuBar;
    }
}