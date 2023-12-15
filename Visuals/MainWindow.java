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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import Loader.DataWrapper;
import Loader.Parser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
public class MainWindow {
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JPanel containerPanel;
    private JPanel buttonPanel;
    private JPanel inputPanel;
    private JPanel convertedPanel;
    private JPanel runPanel;
    private JButton inputButton;
    private JButton convertedButton;
    private JButton runButton;
    private JTextArea inputField;
    private JTextArea convertedField;
    private JButton convertButton;
    private NewWindow newWindow;
    private SaveWindow saveWindow;
    private LoadWindow loadWindow;
    private String fileName;
    private int numberOfLines;
    private boolean nonDeterministic;

    public MainWindow() {
        inicilizeMainFrame();
        initializeMenubar();
        inicilizePanels();
        inicilizButtons();
        showFrame();
    }

    private void createNew(){
        mainFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        mainFrame.setVisible(false);
        mainFrame.dispose();
        inicilizeMainFrame();
        initializeMenubar();
        inicilizePanels();
        inicilizButtons();
        showFrame();
    }

    private void inicilizeMainFrame(){
        mainFrame = new JFrame("Turing Simulator");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void inicilizePanels(){
        mainPanel = new JPanel(new BorderLayout());
        buttonPanel = new JPanel(new FlowLayout());
        containerPanel = new JPanel(new CardLayout());
        inputPanel = new JPanel(new BorderLayout());
        convertedPanel = new JPanel(new BorderLayout());
        runPanel = new JPanel(new BorderLayout());
        inicilizeMainPanel();
        inicilizeButtonPanel();
        inicilizeInputPanel();
        inicilizeConvertedPanel();
        inicilizeRunPanel();
    }

    private void inicilizeMainPanel(){
        mainFrame.add(mainPanel);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(containerPanel, BorderLayout.CENTER);
    }

    private void inicilizeButtonPanel(){
        inputButton = new JButton("Input");
        convertedButton = new JButton("Converted");
        runButton = new JButton("Run");
        buttonPanel.add(inputButton);
        buttonPanel.add(convertedButton);
        buttonPanel.add(runButton);
    }

    private void inicilizeInputPanel(){
        inputField = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(inputField);
        scrollPane.setBounds(10,60,780,500);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        inputPanel.add(scrollPane, BorderLayout.CENTER);
        containerPanel.add(inputPanel, "input");
    }

    private void inicilizeConvertedPanel(){
        convertedField = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(convertedField);
        scrollPane.setBounds(10,60,780,500);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        inicilizeConvertButton();
        convertedPanel.add(convertButton, BorderLayout.NORTH);
        convertedPanel.add(scrollPane, BorderLayout.CENTER);
        containerPanel.add(convertedPanel, "converted");
    }

    private void inicilizeConvertButton(){
        convertButton = new JButton("Convert");
        
    }

    private void inicilizeRunPanel(){
        containerPanel.add(runPanel, "run");
    }

    private void showFrame(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(screenSize);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private void inicilizButtons(){
        inicilizeInputButton();
        inicilizeConvertedButton();
        inicilizeRunButton();
    }

    private void inicilizeInputButton(){
        inputButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout) containerPanel.getLayout();
                cardLayout.show(containerPanel, "input");
            }
        });
    }

    private void inicilizeConvertedButton(){
        convertedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout) containerPanel.getLayout();
                cardLayout.show(containerPanel, "converted");
            }
        });
    }

    private void inicilizeRunButton(){
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout) containerPanel.getLayout();
                cardLayout.show(containerPanel, "run");
            }
        });
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

    private void initializeMenubar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (newWindow == null) {
                    newWindow = new NewWindow();
                }
                mainFrame.setEnabled(false);
                newWindow.setVisible(true);
            }
        });
        JMenuItem saveMenuItem = new JMenuItem("Save");
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
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(newMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        mainFrame.setJMenuBar(menuBar);
    }

    /**
     * The window which used to start creating a new Turing Machine.
     */
    private class NewWindow extends JFrame{
        private static final long serialVersionUID = 1L;
        private JTextField numberOfLinesField;
        private JButton createButton;
        private JCheckBox deterministic;

        /**
         * Creates a new window.
         */
        public NewWindow(){
            super("New");
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    mainFrame.setEnabled(true);
                }
            });
            numberOfLinesField = new JTextField(20);
            deterministic = new JCheckBox("Non-Deterministic");    
            createButton = new JButton("Create");
            createButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    numberOfLines = Integer.parseInt(numberOfLinesField.getText());
                    mainFrame.setEnabled(true);
                    setVisible(false);
                    nonDeterministic = deterministic.isSelected();
                    numberOfLinesField.setText("");
                    createNew();
                }
            });
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel("Number of Lines:"));
            panel.add(numberOfLinesField);
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

    /**
     * A window which warns the user that the table is wrong.
     */
    private class WrongTableWarning extends JFrame{
        private static final long serialVersionUID = 1L;
        private JButton okButton;

        /**
         * Creates a new WrongTableWarning.
         * @param message
         */
        public WrongTableWarning(String message){
            super("Warning");
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    mainFrame.setEnabled(true);
                }
            });
            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mainFrame.setEnabled(true);
                    setVisible(false);
                }
            });
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel(message));
            panel.add(okButton);
            add(panel);

            // Set window properties
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            pack();
            setLocationRelativeTo(mainFrame);
            setResizable(false);
        }
    }

    /**
     * A window which used to save the Turing Machine.
     */
    private class SaveWindow extends JFrame{
        private static final long serialVersionUID = 1L;
        private JTextField fileNameField;
        private JButton saveButton;

        /**
         * Creates a new SaveWindow.
         */
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

    /**
     * A window which used to load a Turing Machine.
     */
    private class LoadWindow extends JFrame{
        private static final long serialVersionUID = 1L;
        private JTextField fileNameField;
        private JButton loadButton;

        /**
         * Constructor.
         */
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
}