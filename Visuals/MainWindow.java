
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
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class MainWindow extends JFrame{
    private JFrame mainFrame;
    private JTextArea inputArea;
    private JTextArea convertedArea;
    private NewWindow newWindow;
    private SaveWindow saveWindow;
    private LoadWindow loadWindow;
    private String fileName;
    private int numberOfLines;
    private boolean nonDeterministic;

    public MainWindow(){
        initializeGUI();
    }

    private void initializeGUI() {
        configureFrame();
        mainFrame = this;
        setJMenuBar(initializeMenubar());
        JPanel converterPanel = createConverterPanel();
        getContentPane().add(converterPanel);
        setVisible(true);
    }

    private void configureFrame() {
        setTitle("Turing converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel createMainPanel(){
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new CardLayout());
        return mainPanel;
    }

    private JPanel createConverterPanel() {
        JPanel converterPanel = new JPanel();
        converterPanel.setLayout(new BorderLayout());

        JSplitPane splitPane = createSplitPane();
        JPanel buttonPanel = createButtonPanel();

        converterPanel.add(splitPane, BorderLayout.CENTER);
        converterPanel.add(buttonPanel, BorderLayout.SOUTH);

        return converterPanel;
    }

    private JSplitPane createSplitPane() {
        inputArea = new JTextArea();
        convertedArea = new JTextArea();

        JScrollPane leftScrollPane = new JScrollPane(inputArea);
        JScrollPane rightScrollPane = new JScrollPane(convertedArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setResizeWeight(0.5);

        return splitPane;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton copyButton = new JButton("Copy Text");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertedArea.setText(inputArea.getText());
            }
        });
        buttonPanel.add(copyButton);
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
        return menuBar;
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
                    //createNew();
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