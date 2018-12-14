/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db_management;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Loco
 */
public class DB_Interface extends javax.swing.JFrame {

    static Connection dbcoConnection = null; // Connection object to connect to database
    static ResultSetMetaData resMet = null; // ResultSetMetaData object to get column metadata
    static DatabaseMetaData dbMet = null; // DatabaseMetaData object to get database metadata

    ImageIcon greenLed = new ImageIcon(getClass().getResource("GreenLed.png")); // successful connection led
    ImageIcon redLed = new ImageIcon(getClass().getResource("RedLed.png")); // not connected connection led

    /**
     * Creates new form DB_Interface
     */
    public DB_Interface() {
        initComponents();
    }

    void mysave() {
        int returnVal; // int variable to store the return state of showSaveDialog method								
        int result = JOptionPane.showConfirmDialog(this, "Save results?", "Save", JOptionPane.YES_NO_CANCEL_OPTION); // show options window

        switch (result) {
            case JOptionPane.YES_OPTION: // if the user chooses YES
                returnVal = jSaveChooser.showSaveDialog(this); // show save window
                FileWriter fw; // defining stream
                try {
                    fw = new FileWriter(jSaveChooser.getSelectedFile() + ".txt"); // creating output stream to write to write a file
                    SelectResults.write(fw); // writing the file
                    fw.close(); // closing the stream
                } catch (IOException obj) {
                    myErrorMessage(obj.getMessage());
                }
                return;
            case JOptionPane.NO_OPTION: // if user chooses NO
                return;
            case JOptionPane.CLOSED_OPTION: //  if the user closes the options window	
                return;
            case JOptionPane.CANCEL_OPTION: // if the user chooses CANCEL
                return;
        }
    }

    // error_message_window method
    void myErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    // info_message_window method
    void myInfoMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "INFO", JOptionPane.INFORMATION_MESSAGE);
    }

    // connection method
    void connect() throws Exception {
        String user = User.getText(); // username of the database user
        String password = new String(Password.getPassword()); // password of the database user
        String IP = IPadress.getText(); // IP of the database
        try {
            Class.forName("com.mysql.jdbc.Driver");
            dbcoConnection = DriverManager.getConnection("jdbc:mysql://" + IP + ":3306/" + user, user, password);
        } catch (SQLDataException obj) {
            myErrorMessage("Error connecting to database\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("Connection to the database was successfuly established.");
        StatusLed.setIcon(greenLed); // change connection status led icon to green
        Status.setText("CONNECTED"); // change connection status label text
    }

    // insert_into_table method
    void insertCommand() throws Exception {
        String query = "INSERT INTO $tablename ($name) VALUES (?)"; // creating String to use it as a parameter in prepareStatement object ( query )
        String table = TableName_TF.getText(); // name of the table that user wants to insert values
        String field = Field_TF.getText(); // columns where the values will be inserted
        String value = Value_TF.getText(); // insert values

        query = query.replace("$tablename", table); // replacing the variable $tablename with the String table
        query = query.replace("$name", field); // replacing the variable $name with the String field which are the fields 
        PreparedStatement prst = dbcoConnection.prepareStatement(query); // creating a prepareStatement object which will execute the query that we built
        prst.setString(1, value); // replacing the first ? of theprepareStatement with the variable value
        try {
            prst.executeUpdate();
        } catch (SQLException obj) {
            myErrorMessage("Error executing INSERT\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("UPDATE was executed successfuly.");
    }

    // create table method
    void createCommand() throws Exception {
        String table = CreateTableName_T.getText(); // name of the new table 
        String fields = CreateTableFields_TA.getText(); // names of the columns of the new table
        String query = "CREATE TABLE $tablename $fields"; // creating query
        query = query.replace("$tablename", table); // replacing the variable $tablename with the String table
        query = query.replace("$fields", fields); // replacing the variable $fields with the String fields
        PreparedStatement prst = dbcoConnection.prepareStatement(query); // creating a prepareStatement which will execute the query that we built
        try {
            prst.executeUpdate();
        } catch (SQLException obj) {
            myErrorMessage("Error executing CREATE TABLE\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("Table " + table + " was created successfuly.");

    }

    // delete table method
    void deleteCommand() throws Exception {
        String query = "DROP TABLE " + TableToDelete.getText(); // name of the table that will be deleted
        Statement statement = dbcoConnection.createStatement(); // creating a statement
        try {
            statement.executeUpdate(query);
        } catch (SQLException obj) {
            myErrorMessage("Error executing DELETE TABLE\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("Table " + TableToDelete.getText() + " was deleted successfuly.");
        System.err.println("Table " + TableToDelete.getText() + " was deleted successfully");

    }

    // update table method
    void updateCommand() throws Exception {
        String table = TableToUpdate.getText(); // name of the table that the user wants to update values
        String field = FieldToUpdate.getText(); // columns that user want to update
        String conditionField = UpdateConditionField.getText(); // column that takes place in the condition
        String condition = UpdateCondition.getText(); // condition
        String newValue = NewValueOfUpdate.getText(); // new value of the column that user wants to update
        int updatedFields = 0; // metritis gia to poses eggrafes enimerothikan
        String query = "UPDATE $table SET $field = $value WHERE $conditionField = $condition";
        query = query.replace("$table", table); // replacing the variable $table with the String table
        query = query.replace("$field", field); // replacing the variable $field with the String field
        if (Update_Condition_CB.isSelected()) { // // if the checkbox to update under condition is selected
            query = query.replace("$value", "'" + newValue + "'");  // replacing the variable $value with the String value
            query = query.replace("$conditionField", conditionField); // replacing the variable $conditionField with the String conditionField
            query = query.replace("$condition", condition); // replacing the variable $condition with the String condition
        } else { // if the checkbox to update under condition is NOT selected
            query = query.replace("$value", "'" + newValue + "'" + "  --"); /* replacing the variable $value with the String value.
             Also we add the String "--"" so the statement will recognise the condition ( from the substring WHERE till the end of the query) as a comment */

        }
        Statement statement = dbcoConnection.createStatement();
        try {
            updatedFields = statement.executeUpdate(query);
        } catch (SQLException obj) {
            myErrorMessage("Error executing UPDATE\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("UPDATE was executed successfuly.\n" + updatedFields + " fields were updated");
    }

    //select method
    void selectCommand() throws Exception {
        String table = TableToSelect.getText(); // table on which the user will execute the select
        String query = ""; // creating query and initializing it with an empty String
        ResultSet rs; // creating resultset
        Statement statement = dbcoConnection.createStatement(); // creating a Statement object which will execute the query

        if (SelectAllFields_CB.isSelected()) { // if the user wants to select all fields of the table
            if (Order_CB.isSelected()) { // if user wants to order the results of the select
                String orderByFields = OrderBy_Fields.getText(); // columns under which the results will be sorted
                if (Descending_Order_CB.isSelected()) { // if the user chooses descending order	 
                    query = "SELECT * FROM " + table + " ORDER BY " + orderByFields + " DESC"; // adding descending order to the query
                } else {
                    query = "SELECT * FROM " + table + " ORDER BY " + orderByFields; // adding order to the query
                }
            } else {
                query = "SELECT * FROM " + table;

            }

            try {
                rs = statement.executeQuery(query); // storing the results in the resultset
            } catch (SQLException obj) {
                myErrorMessage("Error executing SELECT\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
                return; // return if errors occur
            }
            myInfoMessage("SELECT was executed successfuly.");

            resMet = rs.getMetaData(); // we will use the resulsetMetadata to find how many columns has the table
            System.out.println("\n RESULTS: \n"); // info message
            while (rs.next()) { // while there is a field
                for (int i = 1; i <= resMet.getColumnCount(); i++) { // for the number of columns of the table
                    SelectResults.setText(SelectResults.getText() + rs.getString(i) + "  ");
                    // System.out.print(rs.getString(i) + "  "); // display column and 3 spaces
                }
                SelectResults.setText(SelectResults.getText() + "\n"); // empty line
                SelectResults.setText(SelectResults.getText() + "----------------------"); // separator
                SelectResults.setText(SelectResults.getText() + "\n"); // empty line
            }
        } else if (!(SelectAllFields_CB.isSelected())) { // if the user chooses to select specific columns of the table
            String fields = FieldsToSelect.getText(); // fields that the user wants to select
            query = "SELECT " + fields + " FROM " + table; //creating the query
            if (SelectCondition_CB.isSelected()) { // if the checkbox to select under condition is selected
                if (SelectCondition.getText().equals("NULL") || SelectCondition.getText().equals("null") || SelectCondition.getText().equals("Null")) { // if the condition String is equal to NULL
                    if (Select_Condition_Not_CB.isSelected()) { // if NOT checkbox is selected
                        query = query + " WHERE " + SelectConditionFields.getText() + " IS NOT " + SelectCondition.getText(); // adding the condition with NOT (denial) to the query 
                    } else {
                        query = query + " WHERE " + SelectConditionFields.getText() + " IS " + SelectCondition.getText(); // adding the condition to the query
                    }
                } else {
                    if (Select_Condition_Not_CB.isSelected()) { // if NOT checkbox is selected
                        query = query + " WHERE " + SelectConditionFields.getText() + " != " + SelectCondition.getText(); // adding the condition with ! (denial) to the query
                    } else {
                        query = query + " WHERE " + SelectConditionFields.getText() + " = " + SelectCondition.getText(); // adding the condition to the query
                    }
                }
            }
            if (Order_CB.isSelected()) { // if the user wants to order the results of the select
                String orderByFields = OrderBy_Fields.getText(); // columns under which the results will be sorted
                if (Descending_Order_CB.isSelected()) { // if the user chooses descending order
                    query = query + " ORDER BY " + orderByFields + " DESC"; // adding descending order to the query
                } else {
                    query = query + " ORDER BY " + orderByFields; // adding order to the query
                }
            }
            try {
                rs = statement.executeQuery(query); // storing the results in a resultset
            } catch (SQLException obj) {
                myErrorMessage("Error executing SELECT\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
                return; // return if errors occur
            }
            myInfoMessage("SELECT was executed successfuly."); // info message
            fields = fields + ","; // adding a comma in the end of the field String so we can display the last selected column as well

            char[] charArrayFields = fields.toCharArray(); //  converting the String fields to charArray
            int comas = 0; // counter for columns. It counts how many commas are in the field String.The number of fields is equal to the number of commas because after each field follows a comma.
            int count = 0; // variable that is used as a start index in the substring method to extract the selected columns
            int countFields = 0; // variable that is used as index for the table in which we will store the names of the selected columns

            // for to count how many columns the user wants to display 
            for (int i = 0; i < charArrayFields.length; i++) {
                if (charArrayFields[i] == ',' || i == (charArrayFields.length)) { // if the current character is ',' (comma)
                    comas++; // increase the counter by one
                }

            }

            String[] ArrayFields = new String[comas]; // array in which will be stored the names of the selected columns to display
            for (int i = 0; i < charArrayFields.length; i++) { // for the length of the charArrayFields (fields length)
                if (charArrayFields[i] == ',') { // if the current character equals to character ',' (comma)
                    ArrayFields[countFields] = fields.substring(count, i); // extract the substring ( column name ) in the array of column names
                    count += (ArrayFields[countFields].length() + 1); // increasing the variable ( index ) count by the length of the extracted substring + 1
                    countFields++; // increasing the variable (index) countFields by one
                }
            }

            while (rs.next()) { // while there is a field
                for (int i = 0; i < ArrayFields.length; i++) { // for the number of fields that users chose to display
                    SelectResults.setText(SelectResults.getText() + rs.getString(ArrayFields[i]) + "   "); // displaying the column
                }
                SelectResults.setText(SelectResults.getText() + "\n"); // empty line
                SelectResults.setText(SelectResults.getText() + "----------------------"); // separator
                SelectResults.setText(SelectResults.getText() + "\n"); // empty line
            }
        }
    }

    // deleteFields method
    void deleteFieldsCommand() throws Exception {
        String table = TableToDelFields.getText(); // table from which fields will be deleted
        Statement statement = dbcoConnection.createStatement(); // creating statement
        String query = "DELETE FROM " + table; // creating query
        int deletedFields = 0; // counter to count how many fields were deleted
        if (DelFieldsCondition_CB.isSelected()) { // if the checkbox to delete fields under condition is selected
            query = query + " WHERE " + DelFieldsConditionField.getText() + " = " + DelFieldsCondition.getText(); // adding the condition in the query
        }
        try {
            deletedFields = statement.executeUpdate(query);
        } catch (SQLException obj) {
            myErrorMessage("Error executing DELETE FIELDS\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("DELETE FIELD was executed successfuly.\n" + deletedFields + " Were deleted");
    }

    //add column method
    void addColumnCommand() throws Exception {
        String table = TableToAddColumn.getText(); // table in which the column will be inserted 
        String columnName = AddColumnName.getText(); // name of the new column that we want to insert into the table
        String columnType = AddColumnType.getText(); // the type of the new column
        int columnTypeLength = Integer.valueOf(AddColumnTypeLength.getText()); // size of the new column

        String query = "ALTER TABLE " + table + " ADD " + columnName + " " + columnType + "(" + columnTypeLength + ")"; // creating query
        Statement statement = dbcoConnection.createStatement();

        try {
            statement.executeUpdate(query);
        } catch (SQLException obj) {
            myErrorMessage("Error executing ADD COLUMN\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("ADD COLUMN was executed successfuly.\n" + columnName + " was added in " + table);
    }

    //delete column method
    void deleteColumnCommand() throws Exception {
        String table = TableToDeleteColumn.getText(); // table from which the column will be deleted
        String columnName = DeleteColumnName.getText(); // name of the column that we want to delete

        String query = "ALTER TABLE " + table + " DROP " + columnName; // creating query
        Statement statement = dbcoConnection.createStatement();

        try {
            statement.executeUpdate(query);
        } catch (SQLException obj) {
            myErrorMessage("Error executing DELETE COLUMN\n" + obj.getMessage() + "\n" + "SQL_STATE: " + obj.getSQLState() + "\n" + "ERROR CODE: " + obj.getErrorCode());
            return; // return if errors occur
        }
        myInfoMessage("DELETE COLUMN was executed successfuly.\n" + columnName + " was deleted");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        InsertDialog = new javax.swing.JDialog();
        TableName_TF = new javax.swing.JTextField();
        Field_TF = new javax.swing.JTextField();
        Value_TF = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        InsertValues_B = new javax.swing.JButton();
        CreateDialog = new javax.swing.JDialog();
        CreateTableName_T = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        CreateTableFields_TA = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        Create_B = new javax.swing.JButton();
        UpdateDialog = new javax.swing.JDialog();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        TableToUpdate = new javax.swing.JTextField();
        FieldToUpdate = new javax.swing.JTextField();
        NewValueOfUpdate = new javax.swing.JTextField();
        Update_B = new javax.swing.JButton();
        Update_Condition_CB = new javax.swing.JCheckBox();
        UpdateConditionField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        UpdateCondition = new javax.swing.JTextField();
        AboutDialog = new javax.swing.JDialog();
        jLabel20 = new javax.swing.JLabel();
        DeleteDialog = new javax.swing.JDialog();
        TableToDelete = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        Delete_B = new javax.swing.JButton();
        SelectDialog = new javax.swing.JDialog();
        TableToSelect = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        FieldsToSelect = new javax.swing.JTextField();
        SelectCondition = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        SelectCondition_CB = new javax.swing.JCheckBox();
        SelectConditionFields = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        SELECT_B = new javax.swing.JButton();
        SelectAllFields_CB = new javax.swing.JCheckBox();
        Order_CB = new javax.swing.JCheckBox();
        jLabel35 = new javax.swing.JLabel();
        OrderBy_Fields = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        SelectResults = new javax.swing.JTextArea();
        SAVE_RESULTS_B = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        CLEAR_B = new javax.swing.JButton();
        Descending_Order_CB = new javax.swing.JCheckBox();
        jLabel37 = new javax.swing.JLabel();
        Select_Condition_Not_CB = new javax.swing.JCheckBox();
        DeleteFieldsDialog = new javax.swing.JDialog();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        DelFieldsCondition_CB = new javax.swing.JCheckBox();
        TableToDelFields = new javax.swing.JTextField();
        DelFieldsConditionField = new javax.swing.JTextField();
        DelFieldsCondition = new javax.swing.JTextField();
        DELETE_FIELDS_B = new javax.swing.JButton();
        AddColumnsDialog = new javax.swing.JDialog();
        jLabel27 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        TableToAddColumn = new javax.swing.JTextField();
        AddColumnName = new javax.swing.JTextField();
        AddColumnTypeLength = new javax.swing.JTextField();
        AddColumnType = new javax.swing.JTextField();
        ADD_COLUMN_B = new javax.swing.JButton();
        DeleteColumnDialog = new javax.swing.JDialog();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        TableToDeleteColumn = new javax.swing.JTextField();
        DeleteColumnName = new javax.swing.JTextField();
        Delete_Column_B = new javax.swing.JButton();
        jSaveChooser = new javax.swing.JFileChooser();
        User = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        Password = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        Connect_B = new javax.swing.JButton();
        IPadress = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ProductName_L = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        NumberOfTables_L = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        Insert_B = new javax.swing.JButton();
        CreateTable_B = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        UPDATE_B = new javax.swing.JButton();
        StatusLed = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        Status = new javax.swing.JLabel();
        About_B = new javax.swing.JButton();
        DeleteTable_B = new javax.swing.JButton();
        Select_B = new javax.swing.JButton();
        DeleteFields_B = new javax.swing.JButton();
        AddColumn_B = new javax.swing.JButton();
        DeleteColumn_B = new javax.swing.JButton();
        jLabel38 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();

        jLabel8.setText("TABLE TO INSERT:");

        jLabel9.setText("FIELD:");

        jLabel10.setText("VALUE:");

        InsertValues_B.setBackground(new java.awt.Color(113, 222, 54));
        InsertValues_B.setText("Execute Insert");
        InsertValues_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertValues_BActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout InsertDialogLayout = new javax.swing.GroupLayout(InsertDialog.getContentPane());
        InsertDialog.getContentPane().setLayout(InsertDialogLayout);
        InsertDialogLayout.setHorizontalGroup(
            InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(InsertDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, InsertDialogLayout.createSequentialGroup()
                        .addGroup(InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Value_TF, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Field_TF, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TableName_TF, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(142, 142, 142))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, InsertDialogLayout.createSequentialGroup()
                        .addComponent(InsertValues_B)
                        .addGap(32, 32, 32))))
        );
        InsertDialogLayout.setVerticalGroup(
            InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(InsertDialogLayout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TableName_TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(46, 46, 46)
                .addGroup(InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Field_TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(46, 46, 46)
                .addGroup(InsertDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Value_TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addComponent(InsertValues_B)
                .addContainerGap())
        );

        CreateTableName_T.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateTableName_TActionPerformed(evt);
            }
        });

        jLabel11.setText("Table Name:");

        CreateTableFields_TA.setColumns(20);
        CreateTableFields_TA.setRows(5);
        jScrollPane1.setViewportView(CreateTableFields_TA);

        jLabel12.setText("Fields:");

        Create_B.setBackground(new java.awt.Color(128, 246, 54));
        Create_B.setText("CREATE");
        Create_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Create_BActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout CreateDialogLayout = new javax.swing.GroupLayout(CreateDialog.getContentPane());
        CreateDialog.getContentPane().setLayout(CreateDialogLayout);
        CreateDialogLayout.setHorizontalGroup(
            CreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CreateDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(CreateDialogLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(CreateTableName_T, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CreateDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(CreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CreateDialogLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(505, 505, 505))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CreateDialogLayout.createSequentialGroup()
                                .addComponent(Create_B, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(CreateDialogLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        CreateDialogLayout.setVerticalGroup(
            CreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CreateDialogLayout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addGroup(CreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CreateTableName_T, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(29, 29, 29)
                .addComponent(jLabel12)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Create_B, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
        );

        jLabel14.setText("TABLE");

        jLabel15.setText("FIELD");

        jLabel16.setText("NEW VALUE");

        Update_B.setBackground(new java.awt.Color(111, 253, 43));
        Update_B.setText("UPDATE TABLE");
        Update_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Update_BActionPerformed(evt);
            }
        });

        Update_Condition_CB.setText("WITH CONDITION");
        Update_Condition_CB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Update_Condition_CBItemStateChanged(evt);
            }
        });
        Update_Condition_CB.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                Update_Condition_CBStateChanged(evt);
            }
        });
        Update_Condition_CB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Update_Condition_CBActionPerformed(evt);
            }
        });

        UpdateConditionField.setEnabled(false);

        jLabel17.setText("CONDITION FIELD");

        jLabel18.setText("CONDITION");

        UpdateCondition.setEnabled(false);

        javax.swing.GroupLayout UpdateDialogLayout = new javax.swing.GroupLayout(UpdateDialog.getContentPane());
        UpdateDialog.getContentPane().setLayout(UpdateDialogLayout);
        UpdateDialogLayout.setHorizontalGroup(
            UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpdateDialogLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel17)
                    .addComponent(jLabel18))
                .addGap(39, 39, 39)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(UpdateConditionField, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                    .addComponent(TableToUpdate)
                    .addComponent(FieldToUpdate)
                    .addComponent(NewValueOfUpdate)
                    .addComponent(UpdateCondition))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Update_B, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Update_Condition_CB, javax.swing.GroupLayout.Alignment.TRAILING)))
        );
        UpdateDialogLayout.setVerticalGroup(
            UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpdateDialogLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(TableToUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Update_Condition_CB))
                .addGap(42, 42, 42)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(FieldToUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(NewValueOfUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UpdateConditionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addGap(18, 18, 18)
                .addGroup(UpdateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(UpdateCondition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(Update_B)
                .addContainerGap())
        );

        jLabel20.setFont(new java.awt.Font("Segoe Print", 1, 18)); // NOI18N
        jLabel20.setText("<html> <i>Arnolnt Spyros</i><font face=\"Times New Roman\"> ©</font></html>");

        javax.swing.GroupLayout AboutDialogLayout = new javax.swing.GroupLayout(AboutDialog.getContentPane());
        AboutDialog.getContentPane().setLayout(AboutDialogLayout);
        AboutDialogLayout.setHorizontalGroup(
            AboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        AboutDialogLayout.setVerticalGroup(
            AboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        TableToDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TableToDeleteActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel21.setText("TYPE THE NAME OF THE TABLE THAT YOU WANT TO DELETE");

        Delete_B.setBackground(new java.awt.Color(63, 238, 29));
        Delete_B.setText("DELETE");
        Delete_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Delete_BActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout DeleteDialogLayout = new javax.swing.GroupLayout(DeleteDialog.getContentPane());
        DeleteDialog.getContentPane().setLayout(DeleteDialogLayout);
        DeleteDialogLayout.setHorizontalGroup(
            DeleteDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DeleteDialogLayout.createSequentialGroup()
                .addGroup(DeleteDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DeleteDialogLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(DeleteDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(TableToDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(DeleteDialogLayout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(Delete_B)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        DeleteDialogLayout.setVerticalGroup(
            DeleteDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DeleteDialogLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 89, Short.MAX_VALUE)
                .addComponent(TableToDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(Delete_B)
                .addGap(55, 55, 55))
        );

        jLabel22.setText("TABLE:");

        jLabel23.setText("FIELDS:");

        SelectCondition.setEnabled(false);

        jLabel24.setText("CONDITION:");

        SelectCondition_CB.setText("WITH CONDITION");
        SelectCondition_CB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectCondition_CBActionPerformed(evt);
            }
        });

        SelectConditionFields.setEnabled(false);

        jLabel25.setText("CONDITION FIELD:");

        SELECT_B.setBackground(new java.awt.Color(75, 246, 42));
        SELECT_B.setText("SELECT");
        SELECT_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SELECT_BActionPerformed(evt);
            }
        });

        SelectAllFields_CB.setText("ALL");
        SelectAllFields_CB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectAllFields_CBActionPerformed(evt);
            }
        });

        Order_CB.setText("ORDER BY");
        Order_CB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Order_CBActionPerformed(evt);
            }
        });

        jLabel35.setText("ORDER BY:");

        OrderBy_Fields.setEnabled(false);

        SelectResults.setColumns(20);
        SelectResults.setRows(5);
        jScrollPane2.setViewportView(SelectResults);

        SAVE_RESULTS_B.setText("SAVE");
        SAVE_RESULTS_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SAVE_RESULTS_BActionPerformed(evt);
            }
        });

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel36.setText("<html><u> RESULTS</u></html>");

        CLEAR_B.setBackground(new java.awt.Color(242, 249, 44));
        CLEAR_B.setText("CLEAR");
        CLEAR_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CLEAR_BActionPerformed(evt);
            }
        });

        Descending_Order_CB.setText("DESCENDING ORDER");
        Descending_Order_CB.setEnabled(false);

        jLabel37.setText("........");

        Select_Condition_Not_CB.setText("NOT");
        Select_Condition_Not_CB.setEnabled(false);

        javax.swing.GroupLayout SelectDialogLayout = new javax.swing.GroupLayout(SelectDialog.getContentPane());
        SelectDialog.getContentPane().setLayout(SelectDialogLayout);
        SelectDialogLayout.setHorizontalGroup(
            SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SelectDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SelectDialogLayout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addGroup(SelectDialogLayout.createSequentialGroup()
                        .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23)
                            .addComponent(jLabel25)
                            .addComponent(jLabel24)
                            .addComponent(jLabel22)
                            .addComponent(jLabel35))
                        .addGap(14, 14, 14)
                        .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(FieldsToSelect, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                            .addComponent(TableToSelect)
                            .addComponent(SelectConditionFields)
                            .addComponent(SelectCondition)
                            .addComponent(OrderBy_Fields))
                        .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SelectDialogLayout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(SelectCondition_CB)
                                    .addGroup(SelectDialogLayout.createSequentialGroup()
                                        .addComponent(Order_CB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel37)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(Descending_Order_CB)))
                                .addContainerGap())
                            .addGroup(SelectDialogLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(SelectAllFields_CB)
                                    .addComponent(Select_Condition_Not_CB))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
            .addGroup(SelectDialogLayout.createSequentialGroup()
                .addGap(269, 269, 269)
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(300, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SelectDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(SELECT_B)
                .addGap(18, 18, 18)
                .addComponent(SAVE_RESULTS_B, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(CLEAR_B)
                .addGap(7, 7, 7))
        );
        SelectDialogLayout.setVerticalGroup(
            SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SelectDialogLayout.createSequentialGroup()
                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SelectDialogLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TableToSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22)))
                    .addGroup(SelectDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(SelectCondition_CB)
                        .addGap(8, 8, 8)
                        .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Order_CB)
                            .addComponent(Descending_Order_CB)
                            .addComponent(jLabel37))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(FieldsToSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SelectAllFields_CB))
                .addGap(25, 25, 25)
                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SelectConditionFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addGap(26, 26, 26)
                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SelectCondition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(Select_Condition_Not_CB))
                .addGap(18, 18, 18)
                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel35)
                    .addComponent(OrderBy_Fields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SELECT_B)
                    .addComponent(SAVE_RESULTS_B)
                    .addComponent(CLEAR_B))
                .addContainerGap())
        );

        jLabel26.setText("TABLE:");

        jLabel28.setText("CONDITION FIELD:");

        jLabel29.setText("CONDITION:");

        DelFieldsCondition_CB.setText("WITH CONDITION");
        DelFieldsCondition_CB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelFieldsCondition_CBActionPerformed(evt);
            }
        });

        DelFieldsConditionField.setEnabled(false);

        DelFieldsCondition.setEnabled(false);

        DELETE_FIELDS_B.setBackground(new java.awt.Color(91, 244, 28));
        DELETE_FIELDS_B.setText("DELETE FIELDS");
        DELETE_FIELDS_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DELETE_FIELDS_BActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout DeleteFieldsDialogLayout = new javax.swing.GroupLayout(DeleteFieldsDialog.getContentPane());
        DeleteFieldsDialog.getContentPane().setLayout(DeleteFieldsDialogLayout);
        DeleteFieldsDialogLayout.setHorizontalGroup(
            DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DeleteFieldsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(DeleteFieldsDialogLayout.createSequentialGroup()
                        .addGroup(DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(DeleteFieldsDialogLayout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                                .addComponent(TableToDelFields, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(DeleteFieldsDialogLayout.createSequentialGroup()
                                .addGroup(DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel28)
                                    .addComponent(jLabel29))
                                .addGap(18, 18, 18)
                                .addGroup(DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(DelFieldsConditionField)
                                    .addComponent(DelFieldsCondition, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)))
                            .addComponent(DELETE_FIELDS_B, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(0, 115, Short.MAX_VALUE))
                    .addGroup(DeleteFieldsDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(DelFieldsCondition_CB)))
                .addGap(14, 14, 14))
        );
        DeleteFieldsDialogLayout.setVerticalGroup(
            DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DeleteFieldsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(DelFieldsCondition_CB)
                .addGap(28, 28, 28)
                .addGroup(DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(TableToDelFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(DelFieldsConditionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(DeleteFieldsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(DelFieldsCondition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(45, 45, 45)
                .addComponent(DELETE_FIELDS_B)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        jLabel27.setText("TABLE:");

        jLabel30.setText("COLUMN NAME:");

        jLabel31.setText("COLUMN TYPE:");

        jLabel32.setText("COLUMN TYPE LENGTH:");

        ADD_COLUMN_B.setBackground(new java.awt.Color(48, 244, 35));
        ADD_COLUMN_B.setText("ADD COLUMN");
        ADD_COLUMN_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ADD_COLUMN_BActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout AddColumnsDialogLayout = new javax.swing.GroupLayout(AddColumnsDialog.getContentPane());
        AddColumnsDialog.getContentPane().setLayout(AddColumnsDialogLayout);
        AddColumnsDialogLayout.setHorizontalGroup(
            AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddColumnsDialogLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel30)
                    .addComponent(jLabel32)
                    .addComponent(jLabel31))
                .addGap(22, 22, 22)
                .addGroup(AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TableToAddColumn)
                    .addComponent(AddColumnName)
                    .addComponent(AddColumnType)
                    .addComponent(AddColumnTypeLength, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AddColumnsDialogLayout.createSequentialGroup()
                .addContainerGap(205, Short.MAX_VALUE)
                .addComponent(ADD_COLUMN_B)
                .addGap(96, 96, 96))
        );
        AddColumnsDialogLayout.setVerticalGroup(
            AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddColumnsDialogLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(TableToAddColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(AddColumnName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(AddColumnsDialogLayout.createSequentialGroup()
                        .addGroup(AddColumnsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31)
                            .addComponent(AddColumnType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)
                        .addComponent(jLabel32))
                    .addComponent(AddColumnTypeLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(ADD_COLUMN_B)
                .addGap(28, 28, 28))
        );

        jLabel33.setText("TABLE:");

        jLabel34.setText("COLUMN");

        Delete_Column_B.setBackground(new java.awt.Color(73, 245, 40));
        Delete_Column_B.setText("DELETE COLUMN");
        Delete_Column_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Delete_Column_BActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout DeleteColumnDialogLayout = new javax.swing.GroupLayout(DeleteColumnDialog.getContentPane());
        DeleteColumnDialog.getContentPane().setLayout(DeleteColumnDialogLayout);
        DeleteColumnDialogLayout.setHorizontalGroup(
            DeleteColumnDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DeleteColumnDialogLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(DeleteColumnDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel33)
                    .addComponent(jLabel34))
                .addGap(70, 70, 70)
                .addGroup(DeleteColumnDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TableToDeleteColumn)
                    .addComponent(DeleteColumnName, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DeleteColumnDialogLayout.createSequentialGroup()
                .addContainerGap(185, Short.MAX_VALUE)
                .addComponent(Delete_Column_B)
                .addGap(102, 102, 102))
        );
        DeleteColumnDialogLayout.setVerticalGroup(
            DeleteColumnDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DeleteColumnDialogLayout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(DeleteColumnDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(TableToDeleteColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(72, 72, 72)
                .addGroup(DeleteColumnDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(DeleteColumnName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                .addComponent(Delete_Column_B)
                .addGap(46, 46, 46))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DB Interface");

        User.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("User:");

        Password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PasswordActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Password:");

        Connect_B.setBackground(new java.awt.Color(71, 233, 23));
        Connect_B.setText("Connect");
        Connect_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Connect_BActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("IP:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel4.setText("Product Name:");

        jLabel5.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel5.setText("Number of Tables:");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText("<html><u>CREATE COMMANDS:</u></html>");

        Insert_B.setText("INSERT INTO TABLE");
        Insert_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Insert_BActionPerformed(evt);
            }
        });

        CreateTable_B.setText("CREATE TABLE");
        CreateTable_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateTable_BActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setText("<html><u>SELECT - UPDATE COMMANDS:</u></html>");

        UPDATE_B.setText("UPDATE TABLE");
        UPDATE_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UPDATE_BActionPerformed(evt);
            }
        });

        StatusLed.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        StatusLed.setForeground(new java.awt.Color(255, 6, 6));
        StatusLed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/db_management/RedLed.png"))); // NOI18N

        jLabel19.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel19.setText("Status:");

        Status.setText("NOT CONNECTED");

        About_B.setForeground(new java.awt.Color(237, 12, 12));
        About_B.setText("ABOUT");
        About_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                About_BActionPerformed(evt);
            }
        });

        DeleteTable_B.setText("DELETE TABLE");
        DeleteTable_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteTable_BActionPerformed(evt);
            }
        });

        Select_B.setText("SELECT FROM TABLE");
        Select_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Select_BActionPerformed(evt);
            }
        });

        DeleteFields_B.setText("DELETE FIELDS");
        DeleteFields_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteFields_BActionPerformed(evt);
            }
        });

        AddColumn_B.setText("ADD COLUMN");
        AddColumn_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddColumn_BActionPerformed(evt);
            }
        });

        DeleteColumn_B.setText("DELETE COLUMN");
        DeleteColumn_B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteColumn_BActionPerformed(evt);
            }
        });

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel38.setText("<html><u>INSERT/ADD - DELETE COMMANDS:</u></html>");

        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator4.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ProductName_L, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(User, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel1)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(NumberOfTables_L, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(StatusLed)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Status)
                                .addGap(22, 22, 22))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(68, 68, 68)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(Connect_B, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(IPadress, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel3))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(Password, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(57, 57, 57))))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(CreateTable_B, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(2, 2, 2)
                                            .addComponent(AddColumn_B, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(DeleteColumn_B, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(DeleteFields_B, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(DeleteTable_B, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(Insert_B, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(283, 283, 283))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(About_B)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(189, 189, 189)
                        .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(64, 64, 64)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Select_B)
                            .addComponent(UPDATE_B, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 8, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(Status))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(NumberOfTables_L, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ProductName_L, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel19)
                    .addComponent(StatusLed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(User, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(IPadress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(27, 27, 27)
                .addComponent(Connect_B, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(UPDATE_B)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Select_B)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(About_B))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Insert_B)
                            .addComponent(CreateTable_B))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(AddColumn_B)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DeleteTable_B)
                        .addGap(18, 18, 18)
                        .addComponent(DeleteFields_B)
                        .addGap(18, 18, 18)
                        .addComponent(DeleteColumn_B)
                        .addGap(0, 54, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void InsertValues_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertValues_BActionPerformed
        // TODO add your handling code here:
        try {
            insertCommand();
        } catch (Exception obj) {
        }
    }//GEN-LAST:event_InsertValues_BActionPerformed

    private void Create_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Create_BActionPerformed
        // TODO add your handling code here:
        try {
            createCommand();
        } catch (Exception obj) {
        }
    }//GEN-LAST:event_Create_BActionPerformed

    private void CreateTableName_TActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateTableName_TActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_CreateTableName_TActionPerformed

    // check box of update condition (where).If it's selected then the update runs with a condition
    private void Update_Condition_CBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Update_Condition_CBActionPerformed
        // TODO add your handling code here:    if(Condition_CHECKBox.isEnabled())
        if (Update_Condition_CB.isSelected()) {
            UpdateConditionField.setEnabled(true);
            UpdateCondition.setEnabled(true);
        } else if (!(Update_Condition_CB.isSelected())) {
            UpdateConditionField.setEnabled(false);
            UpdateCondition.setEnabled(false);
        }
    }//GEN-LAST:event_Update_Condition_CBActionPerformed

    private void Update_Condition_CBStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_Update_Condition_CBStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_Update_Condition_CBStateChanged

    private void Update_Condition_CBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Update_Condition_CBItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_Update_Condition_CBItemStateChanged

    private void Update_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Update_BActionPerformed
        // TODO add your handling code here:
        try {
            updateCommand();
        } catch (Exception obj) {
        }
    }//GEN-LAST:event_Update_BActionPerformed

    private void UPDATE_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UPDATE_BActionPerformed
        // TODO add your handling code here:
        UpdateDialog.setVisible(true);
        UpdateDialog.setSize(700, 400);
    }//GEN-LAST:event_UPDATE_BActionPerformed

    private void About_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_About_BActionPerformed
        // TODO add your handling code here:
        AboutDialog.setVisible(true);
        AboutDialog.setSize(200, 100);
    }//GEN-LAST:event_About_BActionPerformed

    private void Connect_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Connect_BActionPerformed
        // TODO add your handling code here:
        try {
            connect(); // calling the connect method
            dbMet = dbcoConnection.getMetaData();
            ProductName_L.setText(dbMet.getDatabaseProductName()); // displaying the database product name
            int i = 0; // counter to count how many tables the database has 
            ResultSet rs = dbMet.getTables(null, null, "%", null);
            while (rs.next()) { // while the is a table
                i++; // increase counter by one
            }
            NumberOfTables_L.setText(String.valueOf(i)); // anathesi tis timis i sto label tou plithous ton pinakon
        } catch (Exception obj) {

        }
    }//GEN-LAST:event_Connect_BActionPerformed

    private void PasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PasswordActionPerformed

    private void UserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UserActionPerformed

    private void CreateTable_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateTable_BActionPerformed
        // TODO add your handling code here:
        CreateDialog.setVisible(true);
        CreateDialog.setSize(700, 400);
    }//GEN-LAST:event_CreateTable_BActionPerformed

    private void Insert_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Insert_BActionPerformed
        // TODO add your handling code here:
        InsertDialog.setVisible(true);
        InsertDialog.setSize(500, 400);
    }//GEN-LAST:event_Insert_BActionPerformed

    private void DeleteTable_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteTable_BActionPerformed
        // TODO add your handling code here:
        DeleteDialog.setVisible(true);
        DeleteDialog.setSize(500, 400);
    }//GEN-LAST:event_DeleteTable_BActionPerformed

    private void TableToDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TableToDeleteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TableToDeleteActionPerformed

    private void Delete_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Delete_BActionPerformed
        // TODO add your handling code here:
        try {
            deleteCommand();
        } catch (Exception obj) {

        }
    }//GEN-LAST:event_Delete_BActionPerformed

    private void Select_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Select_BActionPerformed
        // TODO add your handling code here:
        SelectDialog.setVisible(true);
        SelectDialog.setSize(800, 700);
    }//GEN-LAST:event_Select_BActionPerformed

    private void DeleteFields_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteFields_BActionPerformed
        // TODO add your handling code here:
        DeleteFieldsDialog.setVisible(true);
        DeleteFieldsDialog.setSize(500, 400);
    }//GEN-LAST:event_DeleteFields_BActionPerformed

    private void DELETE_FIELDS_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DELETE_FIELDS_BActionPerformed
        // TODO add your handling code here:
        try {
            deleteFieldsCommand();
        } catch (Exception obj) {

        }
    }//GEN-LAST:event_DELETE_FIELDS_BActionPerformed

    private void DelFieldsCondition_CBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelFieldsCondition_CBActionPerformed
        // TODO add your handling code here:
        if (DelFieldsCondition_CB.isSelected()) {
            DelFieldsConditionField.setEnabled(true);
            DelFieldsCondition.setEnabled(true);
        } else if (!(DelFieldsCondition_CB.isSelected())) {
            DelFieldsConditionField.setEnabled(false);
            DelFieldsCondition.setEnabled(false);
        }
    }//GEN-LAST:event_DelFieldsCondition_CBActionPerformed

    private void AddColumn_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddColumn_BActionPerformed
        // TODO add your handling code here:
        AddColumnsDialog.setVisible(true);
        AddColumnsDialog.setSize(500, 400);
    }//GEN-LAST:event_AddColumn_BActionPerformed

    private void DeleteColumn_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteColumn_BActionPerformed
        // TODO add your handling code here:
        DeleteColumnDialog.setVisible(true);
        DeleteColumnDialog.setSize(500, 400);
    }//GEN-LAST:event_DeleteColumn_BActionPerformed

    private void ADD_COLUMN_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ADD_COLUMN_BActionPerformed
        // TODO add your handling code here:
        try {
            addColumnCommand();
        } catch (Exception obj) {

        }
    }//GEN-LAST:event_ADD_COLUMN_BActionPerformed

    private void Delete_Column_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Delete_Column_BActionPerformed
        // TODO add your handling code here:
        try {
            deleteColumnCommand();
        } catch (Exception obj) {

        }
    }//GEN-LAST:event_Delete_Column_BActionPerformed

    //Clear text area button
    private void CLEAR_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CLEAR_BActionPerformed
        // TODO add your handling code here:
        SelectResults.setText("");
    }//GEN-LAST:event_CLEAR_BActionPerformed

    // save select results button
    private void SAVE_RESULTS_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SAVE_RESULTS_BActionPerformed
        // TODO add your handling code here:
        mysave();
    }//GEN-LAST:event_SAVE_RESULTS_BActionPerformed

    private void Order_CBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Order_CBActionPerformed
        // TODO add your handling code here:
        if (Order_CB.isSelected()) {
            OrderBy_Fields.setEnabled(true);
            Descending_Order_CB.setEnabled(true);
        } else if (!(Order_CB.isSelected())) {
            OrderBy_Fields.setEnabled(false);
            Descending_Order_CB.setEnabled(false);
        }
    }//GEN-LAST:event_Order_CBActionPerformed

    private void SelectAllFields_CBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectAllFields_CBActionPerformed
        // TODO add your handling code here:
        if (SelectAllFields_CB.isSelected()) {
            FieldsToSelect.setEnabled(false);
            SelectCondition_CB.setEnabled(false);
        } else if (!(SelectAllFields_CB.isSelected())) {
            FieldsToSelect.setEnabled(true);
            SelectCondition_CB.setEnabled(true);
        }
    }//GEN-LAST:event_SelectAllFields_CBActionPerformed

    private void SELECT_BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SELECT_BActionPerformed
        // TODO add your handling code here:
        try {
            selectCommand();
        } catch (Exception obj) {

        }
    }//GEN-LAST:event_SELECT_BActionPerformed

    // check box of select condition (where).If it's selected then the select runs with a condition
    private void SelectCondition_CBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectCondition_CBActionPerformed
        // TODO add your handling code here:
        if (SelectCondition_CB.isSelected()) {
            SelectConditionFields.setEnabled(true);
            SelectCondition.setEnabled(true);
            Select_Condition_Not_CB.setEnabled(true);
        } else if (!(SelectCondition_CB.isSelected())) {
            SelectConditionFields.setEnabled(false);
            SelectCondition.setEnabled(false);
            Select_Condition_Not_CB.setEnabled(false);
        }
    }//GEN-LAST:event_SelectCondition_CBActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DB_Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DB_Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DB_Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DB_Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DB_Interface().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ADD_COLUMN_B;
    private javax.swing.JDialog AboutDialog;
    private javax.swing.JButton About_B;
    private javax.swing.JTextField AddColumnName;
    private javax.swing.JTextField AddColumnType;
    private javax.swing.JTextField AddColumnTypeLength;
    private javax.swing.JButton AddColumn_B;
    private javax.swing.JDialog AddColumnsDialog;
    private javax.swing.JButton CLEAR_B;
    private javax.swing.JButton Connect_B;
    private javax.swing.JDialog CreateDialog;
    private javax.swing.JTextArea CreateTableFields_TA;
    private javax.swing.JTextField CreateTableName_T;
    private javax.swing.JButton CreateTable_B;
    private javax.swing.JButton Create_B;
    private javax.swing.JButton DELETE_FIELDS_B;
    private javax.swing.JTextField DelFieldsCondition;
    private javax.swing.JTextField DelFieldsConditionField;
    private javax.swing.JCheckBox DelFieldsCondition_CB;
    private javax.swing.JDialog DeleteColumnDialog;
    private javax.swing.JTextField DeleteColumnName;
    private javax.swing.JButton DeleteColumn_B;
    private javax.swing.JDialog DeleteDialog;
    private javax.swing.JDialog DeleteFieldsDialog;
    private javax.swing.JButton DeleteFields_B;
    private javax.swing.JButton DeleteTable_B;
    private javax.swing.JButton Delete_B;
    private javax.swing.JButton Delete_Column_B;
    private javax.swing.JCheckBox Descending_Order_CB;
    private javax.swing.JTextField FieldToUpdate;
    private javax.swing.JTextField Field_TF;
    private javax.swing.JTextField FieldsToSelect;
    private javax.swing.JTextField IPadress;
    private javax.swing.JDialog InsertDialog;
    private javax.swing.JButton InsertValues_B;
    private javax.swing.JButton Insert_B;
    private javax.swing.JTextField NewValueOfUpdate;
    private javax.swing.JLabel NumberOfTables_L;
    private javax.swing.JTextField OrderBy_Fields;
    private javax.swing.JCheckBox Order_CB;
    private javax.swing.JPasswordField Password;
    private javax.swing.JLabel ProductName_L;
    private javax.swing.JButton SAVE_RESULTS_B;
    private javax.swing.JButton SELECT_B;
    private javax.swing.JCheckBox SelectAllFields_CB;
    private javax.swing.JTextField SelectCondition;
    private javax.swing.JTextField SelectConditionFields;
    private javax.swing.JCheckBox SelectCondition_CB;
    private javax.swing.JDialog SelectDialog;
    private javax.swing.JTextArea SelectResults;
    private javax.swing.JButton Select_B;
    private javax.swing.JCheckBox Select_Condition_Not_CB;
    private javax.swing.JLabel Status;
    private javax.swing.JLabel StatusLed;
    private javax.swing.JTextField TableName_TF;
    private javax.swing.JTextField TableToAddColumn;
    private javax.swing.JTextField TableToDelFields;
    private javax.swing.JTextField TableToDelete;
    private javax.swing.JTextField TableToDeleteColumn;
    private javax.swing.JTextField TableToSelect;
    private javax.swing.JTextField TableToUpdate;
    private javax.swing.JButton UPDATE_B;
    private javax.swing.JTextField UpdateCondition;
    private javax.swing.JTextField UpdateConditionField;
    private javax.swing.JDialog UpdateDialog;
    private javax.swing.JButton Update_B;
    private javax.swing.JCheckBox Update_Condition_CB;
    private javax.swing.JTextField User;
    private javax.swing.JTextField Value_TF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JFileChooser jSaveChooser;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    // End of variables declaration//GEN-END:variables
}
