package Visuals;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import Converters.ConvertNonDeterministic;
import Converters.ConverterMultiTread;
import Data.Head;
import Loader.DataWrapper;
import Loader.Parser;
import Loader.StatusSaver;
import Resources.WrongTableException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;




public class MainWindow {
    private JFrame frame;
    private JPanel panelContainer;
    private JPanel editorPanel;
    private JPanel showcasePanel;
    private JPanel runPanel;
    private JComboBox<String> statusDropdown;
    private ArrayList<String> statuses;
    private JButton addStatusButton;
    private AddStatusWindow addStatusWindow;
    private ArrayList<JTable> stageTables;
    private JPanel stagePanelContainer;
    private CardLayout stagePanelLayout;
    private HashMap<String, Integer> statusToStageIndexMap;
    private int numberOfLines = 1;
    private NewWindow newWindow;
    private LoadWindow loadWindow;
    private SaveWindow saveWindow;
    private boolean nonDeterministic = false;
    private JPanel buttonsPanel;
    private ConverterMultiTread converterMultiTread;
    private Head head;
    private int time = 15;
    private boolean running;
    private JPanel stringPanel1;
    private JPanel stringPanel2;
    private JPanel stringPanel3;
    private JTextField stringField1;
    private JTextField stringField2;
    private JTextField stringField3;
    private JTable inputTable;
    private CardLayout cardLayout;
    private String fileName;
    private boolean pause = false;

    /**
     * Initialize the contents of the frame.
     */
    public MainWindow() {
        initializeFrame();
        initializeMenubar();
        initializePanels();
        setupEditorPanel();
        initializePanelContainer();
        convertButton();
        showFrame();
    }

    /**
     * Initialize the contents of the edior panel.
     */
    private void setupEditorPanel() {
        initializeStatusDropdown();
        initializeAddStatusButton();
        initializeAddRowButton();
        initializeRemoveRowButton();
        initializeStageTables();
    }

    /**
    * Recreates the frame content to start a new Turing machine editor.
     */
    private void createNew(){
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setVisible(false);
        frame.dispose();
        initializeFrame();
        initializeMenubar();
        initializePanels();
        setupEditorPanel();
        initializePanelContainer();
        convertButton();
        showFrame();
    }
    
    /**
     * Setups the main JFrame of the window.
     */
    private void initializeFrame() {
        frame = new JFrame("Turing Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Initializes the Meunbar on the top of thw window.
     */ 
    private void initializeMenubar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (newWindow == null) {
                    newWindow = new NewWindow();
                }
                frame.setEnabled(false);
                newWindow.setVisible(true);
            }
        });
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (saveWindow == null) {
                    saveWindow = new SaveWindow();
                }
                frame.setEnabled(false);
                saveWindow.setVisible(true);
            }
        });
        JMenuItem loadMenuItem = new JMenuItem("Load");
        loadMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loadWindow == null) {
                    loadWindow = new LoadWindow();
                }
                frame.setEnabled(false);
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
        frame.setJMenuBar(menuBar);
    }

    /**
     * Inicializes the panels used in the frame.
     */
    private void initializePanels(){
        editorPanel = new JPanel(new BorderLayout());
        showcasePanel = new JPanel();
        buttonsPanel = new JPanel(new FlowLayout());
        runPanel = new JPanel(new BorderLayout());
    }

    /**
     * Setups the dropdown menu for the status selection.
     */
    private void initializeStatusDropdown(){
        statuses = new ArrayList<>();
        statuses.add("Start");
        statuses.add("Accept");
        statusDropdown = new JComboBox<>(statuses.toArray(new String[0]));
        statusDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedStatus = (String)statusDropdown.getSelectedItem();
                stagePanelLayout.show(stagePanelContainer, selectedStatus);
            }
        });
        buttonsPanel.add(statusDropdown);
    }

    /**
     * Setups the button for adding a new status.
     */
    private void initializeAddStatusButton() {
        addStatusButton = new JButton("Add Status");
        addStatusButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (addStatusWindow == null) {
                    addStatusWindow = new AddStatusWindow();
                }
                frame.setEnabled(false);
                addStatusWindow.setVisible(true);
            }
        });
        buttonsPanel.add(addStatusButton);
    }

    /**
     * Setups the button for adding a new row to the table.
     */
    private void initializeAddRowButton(){
        JButton addRowButton = new JButton(new AbstractAction("Add Row") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedStatus = (String)statusDropdown.getSelectedItem();
                int stageIndex = statusToStageIndexMap.get(selectedStatus);
                JTable stageTable = stageTables.get(stageIndex);
                DefaultTableModel model = (DefaultTableModel)stageTable.getModel();
                model.addRow(new Object[]{"", "", "", ""});
            }
        });
        buttonsPanel.add(addRowButton);
    }

    /**
     * Setups the button for removing a row from the table.
     */
    private void initializeRemoveRowButton(){
        JButton removeRowButton = new JButton(new AbstractAction("Remove Row") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedStatus = (String)statusDropdown.getSelectedItem();
                int stageIndex = statusToStageIndexMap.get(selectedStatus);
                JTable stageTable = stageTables.get(stageIndex);
                DefaultTableModel model = (DefaultTableModel)stageTable.getModel();
                int rowCount = model.getRowCount();
                if (rowCount > 0) {
                    model.removeRow(rowCount - 1);
                }
            }
        });
        buttonsPanel.add(removeRowButton);
    }

    /**
     * Setups the tables for each status.
     */
    private void initializeStageTables() {
        stageTables = new ArrayList<>();
        stagePanelContainer = new JPanel();
        stagePanelLayout = new CardLayout();
        stagePanelContainer.setLayout(stagePanelLayout);
        statusToStageIndexMap = new HashMap<>();
        for (int i = 0; i < statuses.size(); i++) {
            String status = statuses.get(i);
            addStatus(i, status);
        }
        editorPanel.add(stagePanelContainer);
    }

    /**
     * Setups the panel containing the panels for each state the proram can be in.
     */
    private void initializePanelContainer(){
        editorPanel.add(buttonsPanel, BorderLayout.NORTH);
        editorPanel.add(stagePanelContainer);
        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);
        panelContainer.add(editorPanel, "Editor");
        panelContainer.add(showcasePanel, "Setup");
        panelContainer.add(runPanel, "Run");
        cardLayout.first(panelContainer);
        frame.add(panelContainer);
    }

    /**
     * Shows the frame.
     */
    private void showFrame(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Adds a new status to the editor.
     * @param i The index of the status.
     * @param status The name of the status.
     */
    private void addStatus(int i, String status){
        ArrayList<String> line = new ArrayList<>();
        line.add("Status");
        ArrayList<String> lineData= new ArrayList<>();
        for(int j = 0; j < numberOfLines; j++){
            line.add("Read L"+j);
            line.add("Write L"+j);
            line.add("Step L"+j);
            lineData.add("");
            lineData.add("");
            lineData.add("");
        }
        String[] columnNames = line.toArray(new String[0]);
        Object[][] data = {lineData.toArray(new String[0])};
        TableModel model = new DefaultTableModel(data, columnNames);
        JTable stageTable = new JTable(model);
        JScrollPane stageTableScrollPane = new JScrollPane(stageTable);
        stageTables.add(stageTable);
        stagePanelContainer.add(stageTableScrollPane, status);
        statusToStageIndexMap.put(status, i);
    }

    private void addStatus(int i, String status, JTable table){
        JScrollPane stageTableScrollPane = new JScrollPane(table);
        stageTables.add(table);
        stagePanelContainer.add(stageTableScrollPane, status);
        statusToStageIndexMap.put(status, i);
    }

    /**
     * Initializes the button for starting the conversion.
     */
    private void convertButton(){
        JButton convertButton = new JButton(new AbstractAction("Convert") {
            @Override
            public void actionPerformed(ActionEvent e) {
                convert();
            }
        });
        buttonsPanel.add(convertButton);
    }

    /**
     * Converts the tables to a single Turing Machine, changes to the next panel and saves the status to a file.
     */
    private void convert(){
        try{
            if(nonDeterministic){
                ConvertNonDeterministic converter = new ConvertNonDeterministic();
                head = converter.convert(stageTables, statuses, numberOfLines);
                StatusSaver.saveStatusToFile(head.getStatuses(), "converted.txt");
            }else{
                converterMultiTread = new ConverterMultiTread();
                head = converterMultiTread.convert(stageTables, statuses, numberOfLines);
                StatusSaver.saveStatusToFile(head.getStatuses(), "converted.txt");
            }
            initializeShowcasePanel();
            cardLayout.show(panelContainer, "Setup");
        }catch(WrongTableException e){
            frame.setEnabled(false);
            new WrongTableWarning(e.getMessage()).setVisible(true);
        }
        
    }
    
    /**
     * Initializes the panel which makes the user create a starting thread.
     */
    private void initializeShowcasePanel(){
        Object[][] data = new Object[1][1];
        TableModel model = new DefaultTableModel(data, new String[1]);
        inputTable = new JTable(model);
        JScrollPane stageTableScrollPane = new JScrollPane(inputTable);
        JButton removeRowButton = new JButton("Remove Row");
        removeRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedRow(inputTable, (DefaultTableModel)inputTable.getModel());
            }
        });
        JButton addRowButton = new JButton("Add Row");
        addRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRow(inputTable, (DefaultTableModel)inputTable.getModel());
            }
        });
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });
        showcasePanel.add(addRowButton, BorderLayout.PAGE_END);
        showcasePanel.add(removeRowButton, BorderLayout.LINE_START);
        showcasePanel.add(startButton, BorderLayout.LINE_END);
        showcasePanel.add(stageTableScrollPane, BorderLayout.CENTER);
    }

    /**
     * Adds a row to a table.
     * @param table The table which needs a new row.
     * @param model The model of the table.
     */
    private void addRow(JTable table, DefaultTableModel model) {
        model.addRow(new Object[table.getColumnCount()]);
    }

    /**
     * Removes the selected row from a table
     * @param table The table which needs a row removed.
     * @param model The model of the table.
     */
    private void removeSelectedRow(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(selectedRow);
        }
    }

    /**
     * Starts to run the Turing Machine.
     */
    private void start(){
        head.setup(getColumnData());
        initializeRunPanel();
        running = true;
        ArrayList<String> lines = new ArrayList<>();
        lines = head.getLines();
        setStringValues(lines.get(0), lines.get(1), lines.get(2));
        SwingUtilities.updateComponentTreeUI(frame);
        time = 15;
        Thread thread = new Thread(new Runnable() { 
            @Override
            public void run() {
                System.out.println("Started");
                ArrayList<String> lines = new ArrayList<>();
                while(running){
                    while(pause){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(head.getStatuString());
                    head.run();
                    lines = head.getLines();
                    final String line1 = lines.get(0);
                    final String line2 = lines.get(1);
                    final String line3 = lines.get(2);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setStringValues(line1, line2, line3);
                        }
                    });
                    if(head.isStopped() || head.getStatusName().equals("Accept")){
                        running = false;
                    }
                    try {
                        SwingUtilities.updateComponentTreeUI(frame);
                        Thread.sleep(time * 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        System.out.println("Stopped");
    }

    /**
     * Inicializes the run panel.
     */
    private void initializeRunPanel(){
        JSlider slider = new JSlider(0, 30);
        runPanel.setLayout(new BorderLayout());
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = slider.getValue();
                setSliderValue(value);
            }
        });
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(pause){
                    continueProcess();
                    stopButton.setText("Stop");
                }else{
                    stopProcess();
                    stopButton.setText("Continue");
                }
            }
        });
        JButton stopButton2 = new JButton("Stop session");
        stopButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                continueProcess();
                stopTheCount();
            }
        });
        JPanel srtingContainer = new JPanel();
        stringField1 = new JTextField(10);
        stringField2 = new JTextField(10);
        stringField3 = new JTextField(10);
        stringPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        stringPanel1.add(new JLabel(""));
        stringPanel1.add(stringField1);
        srtingContainer.add(stringPanel1);
        stringPanel2 = new JPanel();
        stringPanel2.add(new JLabel(""));
        stringPanel2.add(stringField2);
        srtingContainer.add(stringPanel2);
        stringPanel3 = new JPanel();
        stringPanel3.add(new JLabel(""));
        stringPanel3.add(stringField3);
        srtingContainer.add(stringPanel3);
        JPanel buttonHolderpanel = new JPanel();
        buttonHolderpanel.setLayout(new BorderLayout());
        runPanel.add(srtingContainer, BorderLayout.NORTH);
        buttonHolderpanel.add(stopButton, BorderLayout.EAST);
        buttonHolderpanel.add(stopButton2, BorderLayout.WEST);
        runPanel.add(buttonHolderpanel, BorderLayout.SOUTH);
        runPanel.add(slider, BorderLayout.CENTER);
        cardLayout.show(panelContainer, "Run");
        panelContainer.revalidate();
        panelContainer.repaint();
    }

    /**
     * Stops the running of the Turing Machine.
     */
    private void stopTheCount() {
        running = false;
        cardLayout.show(panelContainer, "Edit");
        panelContainer.revalidate();
        panelContainer.repaint();
    }
    
    /**
     * Sets the value of the slider.
     */
    private void setSliderValue(int value) {
        time = value;
    }

    /**
     * Stops the process.
     */
    private void stopProcess(){
        pause = true;
    }

    private void continueProcess(){
        pause = false;
    }

    /**
     * Sets the values of the textfields to the strings.
     * @param string1 String for the first textfield.
     * @param string2 String for the second textfield.
     * @param string3 String for the third textfield.
     */
    private void setStringValues(String string1, String string2, String string3) {
        stringField1.setText(string1);
        stringField2.setText(string2);
        stringField3.setText(string3);
    }

    /**
     * Gets the data from the table.
     * @return The data from the table.
     */
    private ArrayList<String> getColumnData() {
        if(inputTable.isEditing()){
            inputTable.getCellEditor().stopCellEditing();
        }
        ArrayList<String> columnData = new ArrayList<>();
        TableModel dtm = inputTable.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        for (int i = 0 ; i < nRow ; i++){
            for (int j = 0 ; j < nCol ; j++){
                Object value = inputTable.getValueAt(i, j);
                if(value != null){
                    columnData.add((String)value);
                }
            }
        }
        return columnData;
    }

    /**
     * Saves the data to a file.
     */
    private void save(){
        Parser parser = new Parser();
        HashMap<String, JTable> dataToSave = new HashMap<>();
        for(int i = 0; i < statuses.size(); i++){
            dataToSave.put(statuses.get(i), stageTables.get(i));
        }
        parser.save(fileName, nonDeterministic, numberOfLines, dataToSave);
    }

    /**
     * Loads the data from a file.
     */
    private void load(){
        Parser parser = new Parser();
        DataWrapper dataToLoad = parser.load(fileName);
        nonDeterministic = dataToLoad.isBoolValue();
        numberOfLines = dataToLoad.getIntValue();
        statuses.clear();
        statusDropdown.removeAllItems();
        stageTables.clear();
        stagePanelContainer.removeAll();
        HashMap<String, JTable> data = dataToLoad.getTables();
        for(String status : data.keySet()){
            statuses.add(status);
            statusDropdown.addItem(status);
            addStatus(statuses.size() - 1, status, data.get(status));
            stageTables.add(data.get(status));
            JScrollPane scrollPane = new JScrollPane(stageTables.get(stageTables.size() - 1));
            stagePanelContainer.add(scrollPane, status);
        }
    }

    /**
     * The window which used to add a status to the Turing Machine.
     */
    private class AddStatusWindow extends JFrame {
        private static final long serialVersionUID = 1L;
        private JTextField statusField;
        private JButton addButton;

        /**
         * Creates a new AddStatusWindow.
         */
        public AddStatusWindow() {
            super("Add Status");

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.setEnabled(true);
                }
            });
            statusField = new JTextField(20);
            addButton = new JButton("Add");
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String newStatus = statusField.getText();
                    if (!statuses.contains(newStatus)) {
                        statuses.add(newStatus);
                        statusDropdown.addItem(newStatus);
                        int i = statuses.size() - 1;
                        addStatus(i, newStatus);
                    }
                    frame.setEnabled(true);
                    setVisible(false);
                    statusField.setText("");
                }
            });
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel("Status:"));
            panel.add(statusField);
            panel.add(addButton);
            add(panel);

            // Set window properties
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            pack();
            setLocationRelativeTo(frame);
            setResizable(false);
        }
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
                    frame.setEnabled(true);
                }
            });
            numberOfLinesField = new JTextField(20);
            deterministic = new JCheckBox("Non-Deterministic");    
            createButton = new JButton("Create");
            createButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    numberOfLines = Integer.parseInt(numberOfLinesField.getText());
                    frame.setEnabled(true);
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
            setLocationRelativeTo(frame);
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
                    frame.setEnabled(true);
                }
            });
            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.setEnabled(true);
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
            setLocationRelativeTo(frame);
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
                    frame.setEnabled(true);
                }
            });
            fileNameField = new JTextField(20);
            saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileName = fileNameField.getText();
                    setVisible(false);
                    fileNameField.setText("");
                    frame.setEnabled(true);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
            setLocationRelativeTo(frame);
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
                    frame.setEnabled(true);
                }
            });
            fileNameField = new JTextField(20);
            loadButton = new JButton("Load");
            loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileName = fileNameField.getText();
                    frame.setEnabled(true);
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
            setLocationRelativeTo(frame);
            setResizable(false);
        }
    }
}