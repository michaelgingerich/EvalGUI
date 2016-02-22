//Doesn't cache prefColumnWidth
/*Modify record
 *Combo box
 *Change notification
 *JavaFX
 *Dynamic table structure
 *Credentials
 *Radio buttons
 */
package edu.umsl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

public class EvalUIManager extends JFrame implements ItemListener
{
    static final EvalUIManager EVAL_UI_MANAGER = new EvalUIManager();
    private JLabel aLabel;
    private JLabel aMinusLabel;
    private double averageBase = 4.0;
    private JLabel averageLabel;
    private JLabel averageOutput;
    private JLabel bLabel;
    private JLabel bMinusLabel;
    private JLabel bPlusLabel;
    private JPanel buttonPanel;
    private JLabel cLabel;
    private JLabel cMinusLabel;
    private JLabel cPlusLabel;
    private JLabel clarityLabel;
    private JSlider claritySlider;
    private JLabel commentsLabel;
    private JScrollPane commentsScrollPane;
    private JTextArea commentsTextArea;
    private Connection connection;
    private JButton createRecordButton;
    private String databaseDriver = "org.apache.derby.jdbc.ClientDriver";
    private String databaseURL = "jdbc:derby://localhost:1527/Evaluations";
    private Object[] dialogOptions = {
        "OK", "CANCEL"
    };
    private JPanel feedbackInputPanel;
    private JPanel feedbackLabelPanel;
    private Hashtable labelTable = new Hashtable();
    private boolean modifiedButtonChanged;
//    private JButton modifyRecordButton;
    private NumberFormat numberFormatInstance = NumberFormat.getInstance();
    private JLabel overallLabel;
    private JSlider overallSlider;
    private ResultSet resultSet;
    private Statement statement;
    private JFrame summaryFrame;
    private JTable summaryTable;
    private JScrollPane summaryTableScrollPane;
    private JComboBox teamNameComboBox;
    private JLabel teamNameLabel;
    private JPanel teamNamePanel;
    private JButton teamsSummaryButton;
    private JLabel techLabel;
    private JSlider techSlider;
    private JPanel topFeedbackPanel;
    private JFrame topFrame;
    private JLabel usefulLabel;
    private JSlider usefulSlider;

    public EvalUIManager()
    {
        
    }

    public static void main(String args[]) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        EVAL_UI_MANAGER.dbBootstrapper();
        EVAL_UI_MANAGER.evalInputUIBootstrapper();
    }
    
    private void calcEvalTableColPrefWidth()
    {
        int col = 5;
        TableColumn summaryColumn = ((DefaultTableColumnModel)summaryTable.getColumnModel()).getColumn(col);
        TableCellRenderer columnRenderer = summaryTable.getDefaultRenderer(EvalTableManager.EVAL_TABLE_MGR.getColumnClass(col));
        int prefCellWidth = 0;
        int prefColumnWidth = 0;
        int numRows = summaryTable.getRowCount();
        for (int row = 0; row < numRows; row++) 
        {
            prefCellWidth = ((Component)columnRenderer.getTableCellRendererComponent(summaryTable, summaryTable.getValueAt(row, col), false, false, row, col)).getPreferredSize().width;
            prefColumnWidth =  prefCellWidth > prefColumnWidth ? prefCellWidth : prefColumnWidth;
        }
        summaryColumn.setPreferredWidth(prefColumnWidth + 2);
    }
    
    private void dbBootstrapper()
    {
        try
        {
            DriverManager.registerDriver((Driver) Class.forName(databaseDriver).newInstance());
            connection = DriverManager.getConnection(databaseURL);
            statement = connection.createStatement();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Oooops!");
        }
    }
    
    private String getAverageOutputString()
    {
        return numberFormatInstance.format(((double) ((int) techSlider.getValue() + (int) usefulSlider.getValue() + (int) claritySlider.getValue() + (int) overallSlider.getValue()) / averageBase));
    }

    private  String getDatabaseDriver()
    {
        return databaseDriver;
    }
    
    private  void setDatabaseDriver(String databaseDriver)
    {
        this.databaseDriver = databaseDriver;
    }

    private  String getDatabaseURL()
    {
        return databaseURL;
    }
    
    private  void setDatabaseURL(String databaseURL)
    {
        this.databaseURL = databaseURL;
    }
    
    private void insertEvals()
    {
        try
        {
            String teamName = (String) teamNameComboBox.getSelectedItem();
            Object[] newRow =
            {
                teamName, techSlider.getValue(), usefulSlider.getValue(), claritySlider.getValue(), overallSlider.getValue(), validateComments()
            };
            String sql = "INSERT INTO APP.EVALUATIONS (TEAMNAME, TECH, USEFUL, CLARITY, OVERALL, COMMENTS) VALUES ('" + (String) newRow[0] + "', "
                    + (int) newRow[1] + ", " + (int) newRow[2] + ", " + (int) newRow[3] + ", " + (int) newRow[4] + ", '" + (String) newRow[5] + "')";
            statement.executeUpdate(sql);
            boolean isInitialized = EvalTableData.EVAL_TABLE_DATA.isInitialized();
            Object[][] evalTableElements;
            int evalTableWidth = EvalTableData.EVAL_TABLE_DATA.getEvalTableWidth();
            int evalTableLength;
            if(!isInitialized)
            {
                evalTableLength = 1;
                EvalTableData.EVAL_TABLE_DATA.setEvalTableLength(evalTableLength);
                evalTableElements = EvalTableData.EVAL_TABLE_DATA.getEvalTableElements();
            }
            else
            {
                evalTableLength = EvalTableData.EVAL_TABLE_DATA.getEvalTableLength() + 1;
                evalTableElements = EvalTableData.EVAL_TABLE_DATA.getEvalTableElements();
                EvalTableData.EVAL_TABLE_DATA.setEvalTableLength(evalTableLength);
                //Copy contents
                for (int row = 0; row < evalTableElements.length; row++)
                {
                    for (int col = 0; col < evalTableWidth; col++)
                    {
                        EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(row, col, evalTableElements[row][col]);
                    }
                }
            }
            int newRowIndex = evalTableLength - 1;
            //Copy last row
            for (int col = 0; col < evalTableWidth; col++)
            {
                EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(newRowIndex, col, newRow[col]);
            }
            if(!isInitialized)
            {
                evalSummaryUIBootstrapper();
                calcEvalTableColPrefWidth();
                EvalTableData.EVAL_TABLE_DATA.setIsInitialized(true);
                teamsSummaryButton.setEnabled(true);
            }
            else
            {
                EvalTableManager.EVAL_TABLE_MGR.fireTableRowsInserted(newRowIndex, newRowIndex);
            }
            calcEvalTableColPrefWidth();
        }
        catch (SQLException exception)
        {
        }
    }
    
    @Override
    public void itemStateChanged(ItemEvent event)
    {
        
    }
    
    private void selectEvals()
    {
        try
        {
            resultSet = statement.executeQuery("SELECT COUNT(*) AS EVALTABLELENGTH FROM APP.EVALUATIONS");
            resultSet.next();
            int evalTableLength = resultSet.getInt("EVALTABLELENGTH");
            if (evalTableLength > 0)
            {
                resultSet = statement.executeQuery("SELECT DISTINCT TEAMNAME, TECH, USEFUL, CLARITY, OVERALL, COMMENTS FROM APP.EVALUATIONS");
                EvalTableData.EVAL_TABLE_DATA.setEvalTableLength(evalTableLength);
                int row = 0, col = 0;
                while (resultSet.next())
                {
                    EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(row, col++, resultSet.getString("TEAMNAME"));
                    EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(row, col++, resultSet.getInt("TECH"));
                    EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(row, col++, resultSet.getInt("USEFUL"));
                    EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(row, col++, resultSet.getInt("CLARITY"));
                    EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(row, col++, resultSet.getInt("OVERALL"));
                    EvalTableData.EVAL_TABLE_DATA.setEvalTableElement(row, col, resultSet.getString("COMMENTS"));
                    row++;
                    col = 0;
                }
                evalSummaryUIBootstrapper();
                calcEvalTableColPrefWidth();
                EvalTableData.EVAL_TABLE_DATA.setIsInitialized(true);
                JOptionPane.showMessageDialog(topFrame, "Records were loaded from the database.", "Records Loaded", JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                teamsSummaryButton.setEnabled(false);
                JOptionPane.showMessageDialog(topFrame, "No records were found.", "No Records", JOptionPane.ERROR_MESSAGE);
            }
            resultSet.close();
        }
        catch (SQLException exception)
        {
        }
    }
    
    private void updateEvals()
    {
        
    }
    
    private void evalInputUIBootstrapper()
    {
        int leftPadding = 5;
        int teamNameLabelLeftPadding = leftPadding + 7;
        int teamNameComboBoxLeftPadding = 5;
        int feedbackInputPanelLeftPadding = 5;
        int sliderLabelBottomPadding = 30;
        int sliderBottomPadding = 5;
        Dimension topFeedbackPanelPreferredSize = new Dimension(287, 420);
        Dimension feedbackLabelPanelPreferredSize = new Dimension(75, 350);
        Dimension feedbackInputPanelPreferredSize = new Dimension(212, 350);
        FlowLayout leftFlowLayout = new FlowLayout(FlowLayout.LEFT);

        topFrame = new JFrame();
        topFrame.setPreferredSize(new Dimension(400, 495));
        topFrame.setResizable(false);
        topFrame.setLocationByPlatform(true);
        topFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        topFrame.setLayout(new BoxLayout(topFrame.getContentPane(), BoxLayout.Y_AXIS));

        teamNamePanel = new JPanel();
        teamNamePanel.setPreferredSize(new Dimension(287, 48));
        teamNamePanel.setLayout(leftFlowLayout);
        topFrame.add(teamNamePanel);

        teamNameLabel = new JLabel();
        teamNameLabel.setText("Teams:");
        teamNameLabel.setBorder(BorderFactory.createEmptyBorder(0, teamNameLabelLeftPadding, 0, 0));
        teamNamePanel.add(teamNameLabel);

        FocusAdapter focusAdapterInstance = new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                Object sourceObject = e.getSource();
                if (sourceObject instanceof JTextComponent)
                {
                    ((JTextComponent) sourceObject).selectAll();
                }
            }
        };
        
        teamNameComboBox = new JComboBox();
        teamNameComboBox.setEditable(true);
        teamNameComboBox.setPreferredSize(new Dimension(96, 25));
        teamNameComboBox.setBorder(BorderFactory.createEmptyBorder(0, teamNameComboBoxLeftPadding, 0, 0));
        ((JTextComponent) teamNameComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                createRecordButton.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                String teamName = ((JTextComponent) teamNameComboBox.getEditor().getEditorComponent()).getText();
                createRecordButton.setEnabled(teamName.length() > 0);
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
            }
        });
        ((JTextComponent) teamNameComboBox.getEditor().getEditorComponent()).addFocusListener(focusAdapterInstance);
        teamNamePanel.add(teamNameComboBox);
        ((JTextComponent) teamNameComboBox.getEditor().getEditorComponent()).requestFocus();

        topFeedbackPanel = new JPanel();
        topFeedbackPanel.setPreferredSize(topFeedbackPanelPreferredSize);
        topFeedbackPanel.setBorder(BorderFactory.createEtchedBorder());
        topFeedbackPanel.setLayout(leftFlowLayout);
        topFrame.add(topFeedbackPanel);

        feedbackLabelPanel = new JPanel();
        feedbackLabelPanel.setPreferredSize(feedbackLabelPanelPreferredSize);
        feedbackLabelPanel.setLayout(leftFlowLayout);
        topFeedbackPanel.add(feedbackLabelPanel);

        feedbackInputPanel = new JPanel();
        feedbackInputPanel.setPreferredSize(feedbackInputPanelPreferredSize);
        feedbackInputPanel.setLayout(leftFlowLayout);
        feedbackInputPanel.setBorder(BorderFactory.createEmptyBorder(0, feedbackInputPanelLeftPadding, 0, 0));
        topFeedbackPanel.add(feedbackInputPanel);

        aLabel = new JLabel("A");
        aMinusLabel = new JLabel("A-");
        bPlusLabel = new JLabel("B+");
        bLabel = new JLabel("B");
        bMinusLabel = new JLabel("B-");
        cPlusLabel = new JLabel("C+");
        cLabel = new JLabel("C");
        cMinusLabel = new JLabel("C-");

        labelTable.put(8, aLabel);
        labelTable.put(7, aMinusLabel);
        labelTable.put(6, bPlusLabel);
        labelTable.put(5, bLabel);
        labelTable.put(4, bMinusLabel);
        labelTable.put(3, cPlusLabel);
        labelTable.put(2, cLabel);
        labelTable.put(1, cMinusLabel);

        techLabel = new JLabel();
        techLabel.setText("Technical:");
        techLabel.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderLabelBottomPadding, 0));
        feedbackLabelPanel.add(techLabel);

        ChangeListener changeListenerInstance = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) throws UnsupportedOperationException
            {
                Object sourceObject = e.getSource();
                if (sourceObject instanceof JSlider)
                {
                    JSlider sourceSlider = (JSlider) sourceObject;
                    if (!sourceSlider.getValueIsAdjusting())
                    {
                        averageOutput.setText(getAverageOutputString());
                    }
                }
            }
        };
                
        techSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 8, 8);
        techSlider.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderBottomPadding, 0));
        techSlider.setMajorTickSpacing(1);
        techSlider.setInverted(true);
        techSlider.setSnapToTicks(true);
        techSlider.setLabelTable(labelTable);
        techSlider.setPaintTicks(true);
        techSlider.setPaintLabels(true);
        techSlider.addChangeListener(changeListenerInstance);
        feedbackInputPanel.add(techSlider);

        usefulLabel = new JLabel();
        usefulLabel.setText("Useful:");
        usefulLabel.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderLabelBottomPadding, 0));
        feedbackLabelPanel.add(usefulLabel);

        usefulSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 8, 8);
        usefulSlider.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderBottomPadding, 0));
        usefulSlider.setMajorTickSpacing(1);
        usefulSlider.setInverted(true);
        usefulSlider.setSnapToTicks(true);
        usefulSlider.setLabelTable(labelTable);
        usefulSlider.setPaintTicks(true);
        usefulSlider.setPaintLabels(true);
        usefulSlider.addChangeListener(changeListenerInstance);
        feedbackInputPanel.add(usefulSlider);

        clarityLabel = new JLabel();
        clarityLabel.setText("Clarity:");
        clarityLabel.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderLabelBottomPadding, 0));
        feedbackLabelPanel.add(clarityLabel);

        claritySlider = new JSlider(SwingConstants.HORIZONTAL, 1, 8, 8);
        claritySlider.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderBottomPadding, 0));
        claritySlider.setMajorTickSpacing(1);
        claritySlider.setInverted(true);
        claritySlider.setSnapToTicks(true);
        claritySlider.setLabelTable(labelTable);
        claritySlider.setPaintTicks(true);
        claritySlider.setPaintLabels(true);
        claritySlider.addChangeListener(changeListenerInstance);
        feedbackInputPanel.add(claritySlider);

        overallLabel = new JLabel();
        overallLabel.setText("Overall:");
        overallLabel.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderLabelBottomPadding, 0));
        feedbackLabelPanel.add(overallLabel);

        overallSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 8, 8);
        overallSlider.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, sliderBottomPadding, 0));
        overallSlider.setMajorTickSpacing(1);
        overallSlider.setInverted(true);
        overallSlider.setSnapToTicks(true);
        overallSlider.setLabelTable(labelTable);
        overallSlider.setPaintTicks(true);
        overallSlider.setPaintLabels(true);
        overallSlider.addChangeListener(changeListenerInstance);
        feedbackInputPanel.add(overallSlider);

        averageLabel = new JLabel();
        averageLabel.setText("Average:");
        averageLabel.setBorder(BorderFactory.createEmptyBorder(12, leftPadding, 5, 0));
        feedbackLabelPanel.add(averageLabel);

        averageOutput = new JLabel();
        averageOutput.setBorder(BorderFactory.createEmptyBorder(5, leftPadding, 5, 0));
        feedbackInputPanel.add(averageOutput);
        averageOutput.setText(getAverageOutputString());

        commentsLabel = new JLabel();
        commentsLabel.setText("Comments:");
        commentsLabel.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, 0, 0));
        feedbackLabelPanel.add(commentsLabel);

        commentsTextArea = new JTextArea("Enter your comments here.", 20, 18);
        commentsTextArea.setEditable(true);
        commentsTextArea.setLineWrap(true);
        commentsTextArea.setWrapStyleWord(true);
        commentsTextArea.addFocusListener(focusAdapterInstance);
        commentsScrollPane = new JScrollPane(commentsTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        commentsScrollPane.setPreferredSize(new Dimension(200, 100));
        commentsScrollPane.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, 0, 0));
        feedbackInputPanel.add(commentsScrollPane);

        buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(287, 48));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        topFrame.add(buttonPanel);

        ActionListener actionListenerInstance = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                Object sourceObject = event.getSource();
                if (sourceObject.equals(createRecordButton))
                {
                    createRecordButton.setEnabled(false);
                    insertEvals();
                    teamNameComboBox.getEditor().setItem(null);
                    commentsTextArea.setText("Enter your comments here.");
                    ((JTextComponent) teamNameComboBox.getEditor().getEditorComponent()).requestFocus();
                }
        //        if (sourceObject.equals(modifyRecordButton) && !modifiedButtonChanged)
        //        {
        //            modifyRecordButton.setText("Save record");
        //            modifiedButtonChanged = true;
        //        }
        //        else if (sourceObject.equals(modifyRecordButton) && modifiedButtonChanged)
        //        {
        //            modifyRecordButton.setText("Modify record");
        //            modifiedButtonChanged = false;
        //        }
                if (sourceObject.equals(teamsSummaryButton))
                {
                    String teamName = (String) teamNameComboBox.getSelectedItem() == null ? null : ((JTextComponent) teamNameComboBox.getEditor().getEditorComponent()).getText();
                    if (teamName != null && teamName.length() > 0)
                    {
                        int option = JOptionPane.showOptionDialog(null, "You haven't created a record for the evaluation that you already started.  Continue?", "Continue?", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                                dialogOptions, dialogOptions[0]);
                        if (option == 1)
                        {
                            return;
                        }
                    }
                    summaryFrame.setVisible(true);
                }
            }  
        };
        
        createRecordButton = new JButton("Create record");
        createRecordButton.setEnabled(false);
        buttonPanel.add(createRecordButton);
        createRecordButton.addActionListener(actionListenerInstance);

//        modifyRecordButton = new JButton("Modify record");
//        buttonPanel.add(modifyRecordButton);
//        modifyRecordButton.addActionListener(actionListenerInstance);

        teamsSummaryButton = new JButton("Teams Summary");
        buttonPanel.add(teamsSummaryButton);
        teamsSummaryButton.addActionListener(actionListenerInstance);

        topFrame.pack();
        topFrame.setVisible(true);

        summaryFrame = new JFrame();
        summaryFrame.setResizable(false);
        summaryFrame.setLocationByPlatform(true);
        summaryFrame.setVisible(false);
        summaryFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
        //summaryFrame.setLayout(new GridLayout(1, 0));
        summaryFrame.setLayout(new BoxLayout(summaryFrame.getContentPane(), BoxLayout.Y_AXIS));
        summaryFrame.addWindowFocusListener(new WindowAdapter()
        {
            @Override
            public void windowLostFocus(WindowEvent e)
            {
                Object sourceObject = e.getSource();
                if(sourceObject.equals(summaryFrame))
                {
                    ((JTextComponent) teamNameComboBox.getEditor().getEditorComponent()).requestFocus();
                }
            }
        });
        selectEvals();
    }
    
    private void evalSummaryUIBootstrapper()
    {
        EvalTableManager.EVAL_TABLE_MGR = new EvalTableManager();
        summaryTable = new JTable(EvalTableManager.EVAL_TABLE_MGR);
        summaryTable.getModel().addTableModelListener(EvalTableManager.EVAL_TABLE_MGR);
        summaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        summaryTable.setFillsViewportHeight(true);
        summaryTableScrollPane = new JScrollPane(summaryTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        summaryFrame.add(summaryTableScrollPane);
        summaryFrame.pack();
    }
    
    private String validateComments()
    {
        String commentsFieldContents = commentsTextArea.getText();

        if (commentsFieldContents.equalsIgnoreCase("Enter your comments here."))
        {
            return "";
        }
        else
        {
            return commentsFieldContents;
        }
    }
}

class EvalTableData
{
    static final EvalTableData EVAL_TABLE_DATA = new EvalTableData();
    private String[] evalTableColNames =
    {
        "Team Name",
        "Tech",
        "Useful",
        "Clarity",
        "Overall",
        "Comments"
    };
    private int evalTableWidth = evalTableColNames.length;
    private boolean isInitialized = false;

    private Object[][] evalTableElements;

    protected int getEvalTableWidth()
    {
        return evalTableWidth;
    }

    protected Object[][] getEvalTableElements()
    {
        return evalTableElements;
    }

    protected String getEvalTableColName(int col)
    {
        return evalTableColNames[col];
    }

    protected Object getEvalTableElement(int row, int col)
    {
        return evalTableElements[row][col];
    }

    protected int getEvalTableLength()
    {
        return evalTableElements.length;
    }

    protected void setEvalTableLength(int length)
    {
        evalTableElements = new Object[length][evalTableWidth];
    }

    protected boolean isInitialized()
    {
        return isInitialized;
    }

    protected void setIsInitialized(boolean isInitialized)
    {
        this.isInitialized = isInitialized;
    }

    protected void setEvalTableElement(int row, int col, Object element)
    {
        evalTableElements[row][col] = element;
    }
}

class EvalTableManager extends AbstractTableModel implements TableModelListener
{
    static EvalTableManager EVAL_TABLE_MGR;

    @Override
    public int getColumnCount()
    {
        return EvalTableData.EVAL_TABLE_DATA.getEvalTableWidth();
    }

    @Override
    public int getRowCount()
    {
        return EvalTableData.EVAL_TABLE_DATA.getEvalTableLength();
    }

    @Override
    public String getColumnName(int col)
    {
        return EvalTableData.EVAL_TABLE_DATA.getEvalTableColName(col);
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        return EvalTableData.EVAL_TABLE_DATA.getEvalTableElement(row, col);
    }
    
    @Override
    public Class getColumnClass(int col)
    {
        return getValueAt(0, col).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int evalTableWidth = EvalTableData.EVAL_TABLE_DATA.getEvalTableWidth();
        for (int col = 0; col < evalTableWidth; col++)
        {
            EVAL_TABLE_MGR.setValueAt(EvalTableData.EVAL_TABLE_DATA.getEvalTableElement(row, col), row, col);
        }
    }
}
