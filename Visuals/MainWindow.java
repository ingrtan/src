
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
import javax.swing.JTextField;
import Data.Head;
import Converters.MultiTape;
import Converters.NonDetermenistic;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The main window of the application.
 */
public class MainWindow extends JFrame{
    private JFrame mainFrame;
    private JTextArea inputArea;
    private JTextArea convertedArea;
    /*private NewWindow newWindow;
    private SaveWindow saveWindow;
    private LoadWindow loadWindow;
    private String fileName;
    private int numberOfLines;*/
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
    }

    /**
     * Configures the frame.
     */
    private void configureFrame() {
        setTitle("Turing converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Creates the main panel.
     * @return
     */
    private JPanel createMainPanel(){
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new CardLayout());
        return mainPanel;
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
                    SimulatorWindow simWin = new SimulatorWindow(head, mainFrame);
                    simWin.setVisible(true);
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
                /*if (newWindow == null) {
                    newWindow = new NewWindow();
                }
                mainFrame.setEnabled(false);
                newWindow.setVisible(true);*/
                deterministic.setSelected(false);
                inputArea.setText("");
                convertedArea.setText("");
                head = null;
            }
        });
        /*JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (saveWindow == null) {
                    saveWindow = new SaveWindow();
                }
                mainFrame.setEnabled(false);
                saveWindow.setVisible(true);
            }
        });
        JMenuItem loadMenuItem = new JMenuItem("Load");
        loadMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loadWindow == null) {
                    loadWindow = new LoadWindow();
                }
                mainFrame.setEnabled(false);
                loadWindow.setVisible(true);
            }
        });*/
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(newMenuItem);
        //fileMenu.add(saveMenuItem);
        //fileMenu.add(loadMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        return menuBar;
    }
    
    /* 
        public NewWindow(){
            super("New");
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    mainFrame.setEnabled(true);
                }
            });
            deterministic = new JCheckBox("Non-Deterministic");    
            createButton = new JButton("Create");
            createButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    numberOfLines = Integer.parseInt(numberOfLinesField.getText());
                    mainFrame.setEnabled(true);
                    setVisible(false);
                    nonDeterministic = deterministic.isSelected();
                    numberOfLinesField.setText("");
                    //createNew();
                }
            });
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(deterministic);
            panel.add(createButton);
            add(panel);

            // Set window properties
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            pack();
            setLocationRelativeTo(mainFrame);
            setResizable(false);
        }
    
    }


    private class SaveWindow extends JFrame{
        private static final long serialVersionUID = 1L;
        private JTextField fileNameField;
        private JButton saveButton;

        public SaveWindow(){
            super("Save");
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    mainFrame.setEnabled(true);
                }
            });
            fileNameField = new JTextField(20);
            saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileName = fileNameField.getText();
                    setVisible(false);
                    fileNameField.setText("");
                    mainFrame.setEnabled(true);
                    mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    save();
                }
            });
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel("File Name:"));
            panel.add(fileNameField);
            panel.add(saveButton);
            add(panel);

            // Set window properties
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            pack();
            setLocationRelativeTo(mainFrame);
            setResizable(false);
        }
    }

    private class LoadWindow extends JFrame{
        private static final long serialVersionUID = 1L;
        private JTextField fileNameField;
        private JButton loadButton;

        public LoadWindow(){
            super("Load");
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    mainFrame.setEnabled(true);
                }
            });
            fileNameField = new JTextField(20);
            loadButton = new JButton("Load");
            loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileName = fileNameField.getText();
                    mainFrame.setEnabled(true);
                    setVisible(false);
                    fileNameField.setText("");
                    load();
                }
            });
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel("File Name:"));
            panel.add(fileNameField);
            panel.add(loadButton);
            add(panel);

            // Set window properties
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            pack();
            setLocationRelativeTo(mainFrame);
            setResizable(false);
        }
    }
 */
}


