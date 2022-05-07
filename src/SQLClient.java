import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.*;
import java.util.List;

public class SQLClient extends Application implements CodeConstants, MySQLQueries {

    private Connection connection;  //Instance of connection to communicate with the database.

    private Statement userTableStatement; //Instance of statement to send queries for the User Table.
    private Statement loginTableStatement; //Instance of statement to send queries for the Login Table.

    private ResultSet usersTableResultSet; //Instance of resultset to get the results of a executed query on the users table.
    private ResultSet loginsTableResultSet;//Instance of resultset to get the results of a executed query on the login table.

    private Label errorLabel = new Label(""); //Label which shows the errors to the user.

    private Scene loginScene; //The login scene before connecting with the database
    private Scene SQLClientScene; //The Client scene showing after we connect with the database.

    private Integer currentPhaseCode; //Integer that will keep track of the errors found or the stage the program is currently at.

    private TextField tfUsernameField = new TextField(); //Textfield where the user will provide their database username to connect to the db.
    private PasswordField pfPasswordField = new PasswordField(); //Passwordfield where the user will provide their database password to connect to the db.

    //Instances of comboBoxes to allow the user to choose among the possibilities.
    private ComboBox<String> cboInsertAccessLevel = new ComboBox<>();
    private ComboBox<String> cboUpdateFieldValue = new ComboBox<>();
    private ComboBox<String> cboSelectAccessLevel = new ComboBox<>();

    ListView selectFieldListView = new ListView(); //Instance of a listview to allow the user to choose multiple fields in the selection section.

    //Naming Buttons -> Prefix(Section where they belong) + Purpose
    private Button insertInformationButton = new Button("Insert"); //Button which allows the user to insert information into tables.
    private Button updateInformationButton = new Button("Update"); //Button which allows the user to update specific information a table.
    private Button deleteInformationButton = new Button("Delete"); //Button which allows the user to delete information from the tables.
    private Button refreshTablesButton = new Button("Refresh"); //Button which allows the user to refresh the information of the tables.
    private Button deleteTableInfoButton = new Button("Clear Records"); //Button which allows the user to delete all the information inside the tables.
    private Button selectInfoButton = new Button("Select"); //Button which allows the user to select information from tables.

    //Naming TextFields -> tf + Purpose + Section they belong to.
    private TextField tfFirstNameInsert = new TextField();
    private TextField tfLastNameInsert = new TextField();
    private TextField tfUsernameInsert = new TextField();
    private TextField tfEmailAddressInsert = new TextField();
    private TextField tfUserIdUpdate = new TextField();
    private TextField tfUpdateValue = new TextField();
    private TextField tfFirstNameSelect = new TextField();
    private TextField tfLastNameSelect = new TextField();
    private TextField tfUsernameSelect = new TextField();
    private TextField tfEmailSelect = new TextField();
    private TextField tfUserIDSelect = new TextField();
    private TextField tfLoginIdSelect = new TextField();
    private TextField tfUserIdDelete = new TextField();
    private PasswordField pfPasswordSelect = new PasswordField();
    private PasswordField pfPasswordFieldInsert = new PasswordField();
    private PasswordField pfPasswordFieldUpdate = new PasswordField();
    private TextArea userTableResult = new TextArea();
    private TextArea loginTableResult = new TextArea();

    private final Integer encryptionNumber = 5; //this number will be used in the encryption algorithm of the password.


    //Instances of preparedstatements which update tables.
    //Naming Conventions -> Purpose + Field Name + Table Name + PreparedStatement.
    private PreparedStatement deleteUserTablePreparedStatement;
    private PreparedStatement deleteLoginTablePreparedStatement;
    private PreparedStatement insertUserTablePreparedStatement;
    private PreparedStatement insertLoginTablePreparedStatement;
    private PreparedStatement updateFirstNameUserTablePreparedStatement;
    private PreparedStatement updateLastNameUserTablePreparedStatement;
    private PreparedStatement updateEmailUserTablePreparedStatement;
    private PreparedStatement updateAccessLevelUserTablePreparedStatement;
    private PreparedStatement updateUsernameLoginTablePreparedStatement;
    private PreparedStatement updatePasswordLoginTablePreparedStatement;

    private TableView userTableView = new TableView();
    private TableView loginTableView = new TableView();
    @Override
    public void start(Stage primaryStage) throws Exception {
        currentPhaseCode = PROGRAM_STARTED; //sets the phaseCode to indicate that the program started.

        //JavaFX formatting.
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        Button logIn = new Button("Log In");
        Label welcomeLabel = new Label("Welcome! Provide the credentials of your user.");

        welcomeLabel.setTextFill(Color.BLUE);
        welcomeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        errorLabel.setTextFill(Color.RED);

        //Creating a gridPane for the login form when the application starts.
        GridPane dbCredentials = new GridPane();
        dbCredentials.setAlignment(Pos.CENTER);
        dbCredentials.setHgap(10);
        dbCredentials.setVgap(10);
        dbCredentials.setPadding(new Insets(25, 25, 25, 25));

        dbCredentials.add(welcomeLabel, 0, 0, 5, 1);
        dbCredentials.add(usernameLabel, 0, 2);
        dbCredentials.add(tfUsernameField, 2, 2);
        dbCredentials.add(passwordLabel, 0, 3);
        dbCredentials.add(pfPasswordField, 2, 3);
        dbCredentials.add(logIn, 4, 4);
        dbCredentials.add(errorLabel, 0, 4, 3, 1);

        loginScene = new Scene(dbCredentials, 500, 250); //sets the credentialsGridPane as a the root of the scene.

        logIn.setOnAction(e -> { //Eventhandler when the login button is pressed.
            try {
                connectToDB(primaryStage);
            } catch (SQLException ex) {
                System.out.println("Wrong credentials entered by the user.");
                ex.printStackTrace();
            }
        });

        primaryStage.setScene(loginScene); //sets the Scene
        primaryStage.setTitle("SQL Client"); //sets the title.
        primaryStage.show();
    }

    private void connectToDB(Stage primaryStage) throws SQLException {
        errorLabel.setText(""); //resets the errorLabel to empty.
        currentPhaseCode = PROGRAM_STARTED; //indicates that the program is still waiting for the credentials.

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //tries to load the JDBC driver.
        } catch (ClassNotFoundException e) { //if exception is caught,
            errorLabel.setText("Driver couldn't load."); //shows the user that the driver couldn't load through the label.
            currentPhaseCode = DRIVER_NOT_LOADED; //indicates that the driver was not loaded.
            System.out.println("Driver not loaded");
        }

        try {
            String username = tfUsernameField.getText().trim(); //gets the username written by the user
            String password = pfPasswordField.getText().trim(); //gets the password written by the user
            connection = DriverManager.getConnection("jdbc:mysql://localhost", username, password); //tries to establish a connection with the provided credentials.

        } catch (SQLException e) { //if a SQLException is caught, it means that the connection couldn't be established with the credentials.
            tfUsernameField.clear(); //clears the username TF.
            pfPasswordField.clear(); //clears the password PF.
            errorLabel.setText("Wrong Credentials."); //shows the user that the credentials provided are wrong.
            currentPhaseCode = WRONG_CREDENTIALS; //indicates that the credentials provided are wrong.
            System.out.println("Wrong Credentials.");
        }

        if (currentPhaseCode == PROGRAM_STARTED) //checks if the code hasn't changed from the beginning of the handler.
            currentPhaseCode = CONNECTED; //in that case, we have connected successfully and the code is reassigned to the new phase.
        if (currentPhaseCode == CONNECTED){ //checks if we are connected to the database.

            userTableStatement = connection.createStatement(); //initializes the statement for the user table in the new connection.
            loginTableStatement = connection.createStatement(); //initializes the statement for the login table in the new connection.

            //Creates and initializes all the preparedStatements with the queries found in the interface.
            //This preparedstatements will get the parameters from the users through textfields, comboboxes and execute.

            //insert prepared statements for both tables to put in new information
            insertUserTablePreparedStatement = connection.prepareStatement(INSERT_USER_TABLE_PREPAREDSTATEMENT_QUERY);
            insertLoginTablePreparedStatement = connection.prepareStatement(INSERT_LOGIN_TABLE_PREPAREDSTATEMENT_QUERY);

            //delete prepared statements for both tables, to delete specific information from the tables.
            deleteUserTablePreparedStatement = connection.prepareStatement(DELETE_USER_TABLE_PREPAREDSTATEMENT_QUERY);
            deleteLoginTablePreparedStatement = connection.prepareStatement(DELETE_LOGIN_TABLE_PREPAREDSTATEMENT_QUERY);

            //update prepared statements based on the field we want to update.
            updateFirstNameUserTablePreparedStatement = connection.prepareStatement(UPDATE_FIRST_NAME_USER_TABLE_PREPAREDSTATEMENT_QUERY);
            updateLastNameUserTablePreparedStatement = connection.prepareStatement(UPDATE_LAST_NAME_USER_TABLE_PREPAREDSTATEMENT_QUERY);
            updateEmailUserTablePreparedStatement = connection.prepareStatement(UPDATE_EMAIL_USER_TABLE_PREPAREDSTATEMENT_QUERY);
            updateAccessLevelUserTablePreparedStatement = connection.prepareStatement(UPDATE_ACCESS_USER_TABLE_PREPAREDSTATEMENT_QUERY);
            updateUsernameLoginTablePreparedStatement = connection.prepareStatement(UPDATE_USERNAME_LOGINS_TABLE_PREPAREDSTATEMENT_QUERY);
            updatePasswordLoginTablePreparedStatement = connection.prepareStatement(UPDATE_PASSWORD_LOGINS_TABLE_PREPAREDSTATEMENT_QUERY);


            userTableStatement.executeUpdate(USE_GDHIMA_DATABASE); //Specifies the database we want to use.

            //combobox for the access level when we want to insert new information.
            cboInsertAccessLevel.getItems().addAll(FXCollections.observableArrayList("Basic","Admin"));
            cboInsertAccessLevel.getSelectionModel().selectFirst();

            //combobox for the field value we want to update.
            cboUpdateFieldValue.getItems().addAll(FXCollections.observableArrayList("First Name", "Last Name", "Username", "Email", "Password" ,"Access Level"));
            cboUpdateFieldValue.getSelectionModel().selectFirst();

            //combobox for the Access Level in the filter selections.
            cboSelectAccessLevel.getItems().addAll(FXCollections.observableArrayList("", "Basic","Admin"));
            cboSelectAccessLevel.getSelectionModel().selectFirst();

            //listview with the fields we want to select in the selection section.
            selectFieldListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //allows the user to select multiple fields in the list.
            selectFieldListView.getItems().add("First Name");
            selectFieldListView.getItems().add("Last Name");
            selectFieldListView.getItems().add("Username");
            selectFieldListView.getItems().add("Email");
            selectFieldListView.getItems().add("Password");
            selectFieldListView.getItems().add("Access Level");

            //Formatting.
            selectFieldListView.setPrefHeight(142);
            selectFieldListView.setPrefWidth(100);

            userTableResult.setPrefWidth(533);
            userTableResult.setPrefHeight(450);
            loginTableResult.setPrefWidth(532);
            loginTableResult.setPrefHeight(450);

            //GridPane for the insert section.
            GridPane insertGridPane = new GridPane();
            insertGridPane.setPadding(new Insets(5, 5, 5, 5));
            insertGridPane.setHgap(5);
            insertGridPane.setVgap(5);

            insertGridPane.add(new Label("Insert Information to Database"),0,0,2,1);
            insertGridPane.add(new Label("First Name: "),0,3);
            insertGridPane.add(new Label("Last Name: "),0,4);
            insertGridPane.add(new Label("Username: "),0,5);
            insertGridPane.add(new Label("Email Address: "),0,6);
            insertGridPane.add(new Label("Password: "),0,7);
            insertGridPane.add(new Label("Access level: "),0,8);
            insertGridPane.add(tfFirstNameInsert,1,3);
            insertGridPane.add(tfLastNameInsert,1,4);
            insertGridPane.add(tfUsernameInsert,1,5);
            insertGridPane.add(tfEmailAddressInsert,1,6);
            insertGridPane.add(pfPasswordFieldInsert,1,7);
            insertGridPane.add(cboInsertAccessLevel,1,8);
            insertGridPane.add(insertInformationButton,2,9);
            insertGridPane.setStyle("-fx-border-color: black;");

            //Gridpane for the Update section.
            GridPane updateGridPane = new GridPane();
            updateGridPane.setPadding(new Insets(5,5,5,5));
            updateGridPane.setVgap(5);
            updateGridPane.setHgap(5);

            updateGridPane.add(new Label("Update information."),0,0,2,1);
            updateGridPane.add(new Label("User Id: "),0,1);
            updateGridPane.add(tfUserIdUpdate,1,1);
            updateGridPane.add(new Label("Field to update:"),0,2);
            updateGridPane.add(cboUpdateFieldValue,1,2);
            updateGridPane.add(new Label("New Value: "),0,3);
            updateGridPane.add(tfUpdateValue,1,3);

            updateGridPane.add(updateInformationButton,1,4);
            updateGridPane.setStyle("-fx-border-color: black;");

            //GridPane for the select section.
            GridPane selectGridPane = new GridPane();
            selectGridPane.setPadding(new Insets(5,5,5,5));
            selectGridPane.setStyle("-fx-border-color: black;");
            selectGridPane.setHgap(5);
            selectGridPane.setVgap(5);
            selectGridPane.add(new Label("Select Information."),0,0,2,1);
            selectGridPane.add(new Label("Fields Selected: "),0,4);
            selectGridPane.add(selectFieldListView,1,1,1,6);
            selectGridPane.add(new Label("Filters."),3,0);
            selectGridPane.add(new Label("User ID: "),2,1);
            selectGridPane.add(new Label("Login ID: "),2,2);
            selectGridPane.add(new Label("First Name: "),2,3);
            selectGridPane.add(new Label("Last Name:"),2,4);
            selectGridPane.add(new Label("Username:"),2,5);
            selectGridPane.add(new Label("Email:"),2,6);
            selectGridPane.add(new Label("Password:"),2,7);
            selectGridPane.add(new Label("Access:"),2,8);
            selectGridPane.add(tfUserIDSelect,3,1);
            selectGridPane.add(tfLoginIdSelect,3,2);
            selectGridPane.add(tfFirstNameSelect,3,3);
            selectGridPane.add(tfLastNameSelect,3,4);
            selectGridPane.add(tfUsernameSelect,3,5);
            selectGridPane.add(tfEmailSelect,3,6);
            selectGridPane.add(pfPasswordSelect,3,7);
            selectGridPane.add(cboSelectAccessLevel,3,8);
            selectGridPane.add(selectInfoButton,3,9);

            //GridPane for the delete section.
            GridPane deleteGridPane = new GridPane();
            deleteGridPane.setPadding(new Insets(5,5,5,5));
            deleteGridPane.setHgap(5);
            deleteGridPane.setVgap(5);
            deleteGridPane.setStyle("-fx-border-color: black;");

            deleteGridPane.add(new Label("Delete information."),0,0,2,1);
            deleteGridPane.add(new Label("User ID:"),0,1);
            deleteGridPane.add(tfUserIdDelete,1,1);
            deleteGridPane.add(deleteInformationButton,1,2);

            //Building the scene.
            VBox updateAndDeleteVBox = new VBox();
            updateAndDeleteVBox.setPrefHeight(250);
            Label informationLabel = new Label("To select multiple fields from a table, hold CTRL/SHIFT and click."); //Label that gives some information.
            informationLabel.setTextFill(Color.RED);
            updateAndDeleteVBox.getChildren().addAll(updateGridPane,deleteGridPane,informationLabel,new Label("By Gerald Dhima"));

            HBox modifyTablesHbox = new HBox();
            modifyTablesHbox.getChildren().addAll(insertGridPane,updateAndDeleteVBox,selectGridPane);

            HBox tableResults = new HBox();
            //tableResults.getChildren().addAll(userTableResult,loginTableResult);
            userTableView.setPrefWidth(600);
            loginTableView.setPrefWidth(465);
            tableResults.getChildren().addAll(userTableView,loginTableView);

            HBox refreshClearAndInformation = new HBox();
            refreshClearAndInformation.getChildren().addAll(deleteTableInfoButton, refreshTablesButton,errorLabel);

            VBox sqlClientGui = new VBox();
            sqlClientGui.getChildren().addAll(modifyTablesHbox,tableResults,refreshClearAndInformation);

            refreshButtonEvent(); //shows all the tables' information.


            SQLClientScene = new Scene(sqlClientGui,1065,800); //sets the root of the scene.
            primaryStage.setScene(SQLClientScene);

            deleteTableInfoButton.setOnAction(e-> { //eventHandler when the user tries to delete all the information from both tables.
                try {
                    clearRecordsButtonEvent();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            refreshTablesButton.setOnAction(e-> { //eventHandler when the user tries to refresh the page.
                try {
                    refreshButtonEvent();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            insertInformationButton.setOnAction(e-> { //eventHandler when the user tries to insert information.
                try {
                    insertButtonEvent();
                } catch (SQLException ex) {
                    errorLabel.setText("\t\t\tThe input given is incorrect. Please read the manual for the client."); //prints out the error message to the user.
                    ex.printStackTrace();}
                finally {
                    //Clear all the textfields.
                    tfFirstNameInsert.clear();
                    tfLastNameInsert.clear();
                    tfEmailAddressInsert.clear();
                    tfUsernameInsert.clear();
                    pfPasswordFieldInsert.clear();
                }
            });

            deleteInformationButton.setOnAction(e-> { //eventhandler when the user tries to delete a record.
                try {
                    deleteButtonEvent();
                } catch (SQLException ex) {
                    errorLabel.setText("\t\t\tThe input given is incorrect. Please read the manual for the client.");
                    ex.printStackTrace();
                }finally {
                    //Clearing the textfield.
                    tfUserIdDelete.clear();
                }
            });

            updateInformationButton.setOnAction(e-> { //eventhandler when the user tries to update specific information
                try {
                    updateButtonEvent();
                } catch (SQLException ex) {
                    errorLabel.setText("\t\t\tThe input given is incorrect. Please read the manual for the client.");
                    ex.printStackTrace();
                }finally {
                    //Clears the text areas
                    tfUserIdUpdate.clear();
                    tfUpdateValue.clear();
                }
            });
            selectInfoButton.setOnAction(e-> { //eventHandler when the user tries to select some information.
                try {
                    selectButtonEvent();
                } catch (SQLException ex) {
                    errorLabel.setText("\t\t\tThe input given is incorrect. Please read the manual for the client.");
                    ex.printStackTrace();
                }finally {
                    //Clearing up the textfields.
                    tfFirstNameSelect.clear();
                    tfLastNameSelect.clear();
                    tfEmailSelect.clear();
                    tfUserIDSelect.clear();
                    tfLoginIdSelect.clear();
                    tfUsernameSelect.clear();
                    pfPasswordSelect.clear();
                }
            });
        }
    }
    private void refreshButtonEvent() throws SQLException { //function which shows all the information of both tables in their respective tableviews

        //clear the columns and data from each tableview so we don't repeatedly fill them.
        userTableView.getColumns().clear();
        loginTableView.getColumns().clear();
        userTableView.getItems().clear();
        loginTableView.getItems().clear();

        usersTableResultSet = userTableStatement.executeQuery(SHOW_USER_TABLE_QUERY); //executes the query that shows all the information of the user table and gets the result.
        loginsTableResultSet = loginTableStatement.executeQuery(SHOW_LOGIN_TABLE_QUERY); //executes the query that shows all the information of the login table and gets the result

        ObservableList<ObservableList> userTableData = FXCollections.observableArrayList(); //create an observable list where the data of the userTable will be stored at
        ObservableList<ObservableList> loginTableData = FXCollections.observableArrayList(); //creates a observable list where teh data of the loginTable will be stored.

        //creating the columns for the users table view.
        for(int i = 0; i < usersTableResultSet.getMetaData().getColumnCount(); i++){ //iterates through each one of the columns which will be shown.
            final int j = i;
            TableColumn column = new TableColumn(usersTableResultSet.getMetaData().getColumnName(i+1)); //creates an instance of the tablecolumn object,
            // which will be filled with the names of the columns.
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>, ObservableValue<String>>(){ //fills the instance of the tablecolumn with
                // the names of the columns.
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });

            userTableView.getColumns().addAll(column);  //adds the columns to the tableview.
        }
        //filling the user tableview with the data.
        while(usersTableResultSet.next()){ //goes through each row of the result set.
            ObservableList<String> row = FXCollections.observableArrayList(); //crates an observable list of the rows.
            for(int i=1 ; i<=usersTableResultSet.getMetaData().getColumnCount(); i++){ //goes throw each column in each row.
                row.add(usersTableResultSet.getString(i)); // gets the information for that record in that column and adds it to the row.
            }
            userTableData.add(row); //adds the current row to the tables data.

        }

        userTableView.setItems(userTableData); //adds the data to the table view.

        //Filling the logins table view with columns and data. Same process as with the user Table view.
        for(int i = 0; i < loginsTableResultSet.getMetaData().getColumnCount(); i++){
            final int j = i;
            TableColumn column = new TableColumn(loginsTableResultSet.getMetaData().getColumnName(i+1));
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>, ObservableValue<String>>(){
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });
            loginTableView.getColumns().addAll(column);
        }
        while(loginsTableResultSet.next()){
            ObservableList<String> row = FXCollections.observableArrayList();
            for(int i=1 ; i<=loginsTableResultSet.getMetaData().getColumnCount(); i++){
                row.add(loginsTableResultSet.getString(i));
            }
            loginTableData.add(row);
        }

        loginTableView.setItems(loginTableData);
    }

    private void clearRecordsButtonEvent() throws SQLException { //function which deletes all the information from both tables.
        loginTableStatement.executeUpdate(CLEAR_LOGIN_TABLE_QUERY); //executes the query that deletes all the information from the login table.
        userTableStatement.executeUpdate(CLEAR_USER_TABLE_QUERY); //executes the query that deletes all the information from the users table.

        refreshButtonEvent(); //refreshes the page.
    }

    private void insertButtonEvent() throws SQLException{ //function which inserts new information into both tables
        // Set the parameters of the prepared statements for the user table based on the values entered in the textfields.
        insertUserTablePreparedStatement.setString(1,tfFirstNameInsert.getText().trim());
        insertUserTablePreparedStatement.setString(2,tfLastNameInsert.getText().trim());
        insertUserTablePreparedStatement.setString(3,tfEmailAddressInsert.getText().trim());
        insertUserTablePreparedStatement.setString(4,cboInsertAccessLevel.getSelectionModel().getSelectedItem());
        insertUserTablePreparedStatement.executeUpdate(); //executes the filled preparedstatement with the values given by the user.

        // Set the parameters of the prepared statements for the login table based on the values entered in the textfields.
        insertLoginTablePreparedStatement.setString(1,tfUsernameInsert.getText().trim());
        insertLoginTablePreparedStatement.setString(2,simpleEncryptPassword(pfPasswordFieldInsert.getText().trim()));
        insertLoginTablePreparedStatement.executeUpdate(); //executes the filled preparedstatement with the values given by the user.

        refreshButtonEvent(); //refreshes the page.

    }

    private void deleteButtonEvent() throws SQLException { //Function which deletes a specific record from the table based on the userID value entered by the user.
        //Set the parameters for both preparedstatements for the login and user table.
        deleteLoginTablePreparedStatement.setString(1,tfUserIdDelete.getText().trim());
        deleteUserTablePreparedStatement.setString(1,tfUserIdDelete.getText().trim());

        //Executing the prepared statements that delete the record.
        deleteLoginTablePreparedStatement.executeUpdate();
        deleteUserTablePreparedStatement.executeUpdate();

        refreshButtonEvent(); //refreshing the page.
    }

    private void updateButtonEvent() throws SQLException { //Function which updates a user-chosen field in tables.

        String fieldToUpdate = cboUpdateFieldValue.getSelectionModel().getSelectedItem(); //gets the field the user wants to update from the combo box.
        String userID = tfUserIdUpdate.getText().trim(); //gets the userid for the user we want to update.
        String newValue = tfUpdateValue.getText().trim(); //gets the value we want to replace it with.

        switch (fieldToUpdate){ //Goes through each-one of the fields the user might have chosen in the combobox, executing the right preparedstatement based on it.
            case "First Name":
                updateFirstNameUserTablePreparedStatement.setString(1,newValue);
                updateFirstNameUserTablePreparedStatement.setInt(2, Integer.parseInt(userID));
                updateFirstNameUserTablePreparedStatement.executeUpdate();
                break;
            case "Last Name":
                updateLastNameUserTablePreparedStatement.setString(1,newValue);
                updateLastNameUserTablePreparedStatement.setInt(2, Integer.parseInt(userID));
                updateLastNameUserTablePreparedStatement.executeUpdate();
                break;
            case "Email":
                updateEmailUserTablePreparedStatement.setString(1,newValue);
                updateEmailUserTablePreparedStatement.setInt(2, Integer.parseInt(userID));
                updateEmailUserTablePreparedStatement.executeUpdate();
                break;
            case "Access Level":
                updateAccessLevelUserTablePreparedStatement.setString(1,newValue);
                updateAccessLevelUserTablePreparedStatement.setInt(2, Integer.parseInt(userID));
                updateAccessLevelUserTablePreparedStatement.executeUpdate();
                break;
            case "Username":
                updateUsernameLoginTablePreparedStatement.setString(1,newValue);
                updateUsernameLoginTablePreparedStatement.setInt(2, Integer.parseInt(userID));
                updateUsernameLoginTablePreparedStatement.executeUpdate();
                break;
            case "Password":
                updatePasswordLoginTablePreparedStatement.setString(1,newValue);
                updatePasswordLoginTablePreparedStatement.setInt(2, Integer.parseInt(userID));
                updatePasswordLoginTablePreparedStatement.executeUpdate();
                break;
        }

        refreshButtonEvent();
    }

    private void selectButtonEvent() throws SQLException {
        //Clearing the columns and data from each tableview to avoid repetition.
        userTableView.getColumns().clear();
        loginTableView.getColumns().clear();
        userTableView.getItems().clear();
        loginTableView.getItems().clear();

        usersTableResultSet = userTableStatement.executeQuery(createUserTableSelectQuery()); //executes the query created by the function for the usersTable
        loginsTableResultSet = loginTableStatement.executeQuery(createLoginTableSelectQuery()); //executes the query created by the function for the logins table.


            ObservableList<ObservableList> userTableData = FXCollections.observableArrayList(); //create an observable list where the data of the userTable will be stored at.
            ObservableList<ObservableList> loginTableData = FXCollections.observableArrayList();//create an observable list where the data of the loginTable will be stored at

            //creating the columns for the users table view.
            for (int i = 0; i < usersTableResultSet.getMetaData().getColumnCount(); i++) { //iterates through each one of the columns which will be shown.
                final int j = i;
                TableColumn column = new TableColumn(usersTableResultSet.getMetaData().getColumnName(i + 1)); //creates an instance of the tablecolumn object,
                // which will be filled with the names of the columns.
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() { //fills the instance of the tablecolumn with
                    // the names of the columns.
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                userTableView.getColumns().addAll(column);  //adds the columns to the tableview.
            }
            //filling the user tableview with the data.
            while (usersTableResultSet.next()) { //goes through each row of the result set.
                ObservableList<String> row = FXCollections.observableArrayList(); //crates an observable list of the rows.
                for (int i = 1; i <= usersTableResultSet.getMetaData().getColumnCount(); i++) { //goes throw each column in each row.
                    row.add(usersTableResultSet.getString(i)); // gets the information for that record in that column and adds it to the row.
                }
                userTableData.add(row); //adds the current row to the tables data.
            }

            //Filling the logins table view with columns and data. Same process as with the user Table view.
            for (int i = 0; i < loginsTableResultSet.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn column = new TableColumn(loginsTableResultSet.getMetaData().getColumnName(i + 1));
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                loginTableView.getColumns().addAll(column);
            }
            while (loginsTableResultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= loginsTableResultSet.getMetaData().getColumnCount(); i++) {
                    row.add(loginsTableResultSet.getString(i));
                }

                loginTableData.add(row);

            }

            //if there are no results in one table after we apply the filter, then the information on the other table gets deleted too.
            if((userTableData.isEmpty())){
                loginTableData.clear();
            }else if(loginTableData.isEmpty()){
                userTableData.clear();
            }

            //add the data to the table view.
            userTableView.setItems(userTableData);
            loginTableView.setItems(loginTableData);


        }

    private String createUserTableSelectQuery(){ //creates the SQL query for the users table to select the fields chosen by the user in the list, and apply the filters which are not blank.
        List<String> fieldsSelectedList = selectFieldListView.getSelectionModel().getSelectedItems(); //gets a list of the field the user wants to select.
        String fieldsSelected = String.join(",",fieldsSelectedList); //creates a string which consists of all the fields the user chose.

        //flags that determine what filters that are found in the user table did the user choose.
        Boolean firstNameFilterActivated = !tfFirstNameSelect.getText().trim().isBlank();
        Boolean lastNameFilterActivated = !tfLastNameSelect.getText().trim().isBlank();
        Boolean emailFilterActivated = !tfEmailSelect.getText().trim().isBlank();
        Boolean userIDFilterActivated = !tfUserIDSelect.getText().trim().isBlank();
        Boolean accessLevelFilterActivated = !cboSelectAccessLevel.getSelectionModel().getSelectedItem().isBlank();

        //Creating the first part of the query with the fields that need to be selected.
        String selectedFieldsSQLQuery = (fieldsSelected.contains("First Name") ? ",firstName " : "") +
                (fieldsSelected.contains("Last Name") ? ",lastName " : "")+
                (fieldsSelected.contains("Email") ? ",emailAddress " : "") +
                (fieldsSelected.contains("Access Level") ? ",accessLevel " : "");

        //Creating the second part of the query with all the filters that were left non-blank by the user.
        String filtersSQLQuery = (firstNameFilterActivated ? ("firstName = \'" + tfFirstNameSelect.getText().trim() + "\'") : "")+
                (firstNameFilterActivated && lastNameFilterActivated ? " AND " : "") +
                (lastNameFilterActivated ? ("lastName = \'" + tfLastNameSelect.getText().trim() + "\'") : "") +
                ((firstNameFilterActivated || lastNameFilterActivated) && emailFilterActivated ? " AND " : "") +
                (emailFilterActivated ? ("emailAddress = \'" + tfEmailSelect.getText().trim() + "\'") : "") +
                ((firstNameFilterActivated || lastNameFilterActivated || emailFilterActivated) && userIDFilterActivated ? " AND " : "") +
                (userIDFilterActivated ? ("userId = \'" + tfUserIDSelect.getText().trim() + "\'") : "") +
                ((firstNameFilterActivated || lastNameFilterActivated || emailFilterActivated || userIDFilterActivated) && accessLevelFilterActivated ? " AND " : "")+
                (accessLevelFilterActivated ? "accessLevel = \'" + cboSelectAccessLevel.getSelectionModel().getSelectedItem() + "\'" : "");

        //The full query.
        String fullQuery = "SELECT userID" + selectedFieldsSQLQuery + " FROM GDhimaUsers" +
                (filtersSQLQuery.trim().isBlank() ? ";" : " WHERE " + filtersSQLQuery + ";");


        System.out.println(fullQuery);
        return fullQuery;

    }

    private String createLoginTableSelectQuery(){ //creates the SQL query for the login table to select the fields chosen by the user in the list, and apply the filters which are not blank
        List<String> fieldsSelectedList = selectFieldListView.getSelectionModel().getSelectedItems(); //gets a list of the field the user wants to select.
        String fieldsSelected = String.join(",",fieldsSelectedList); //creates a string which consists of all the fields the user chose.

        //flags that determine what filters that are found in the login table did the user choose.
        Boolean loginIDFilterSelected = !tfLoginIdSelect.getText().trim().isBlank();
        Boolean userIDFilterSelected = !tfUserIDSelect.getText().trim().isBlank();
        Boolean usernameFilterSelected = !tfUsernameSelect.getText().trim().isBlank();
        Boolean passwordFilterSelected = !pfPasswordSelect.getText().trim().isBlank();

        //Creating the first part of the query with the fields that need to be selected.
        String selectedFieldsSQLQuery = (fieldsSelected.contains("Username") ? ",username " : "") +
                (fieldsSelected.contains("Password") ? ",password " : "");

        //Creating the second part of the query with all the filters that were left non-blank by the user.
        String filtersSQLQuery = (usernameFilterSelected ? ("username = \'" + tfUsernameSelect.getText().trim() + "\'") : "") +
                (usernameFilterSelected && userIDFilterSelected ? " AND " : "")+
                (userIDFilterSelected ? "userID = \'" + tfUserIDSelect.getText().trim() + "\'" : "") +
                ((userIDFilterSelected || usernameFilterSelected) && loginIDFilterSelected ? " AND " : "") +
                (loginIDFilterSelected ? "loginID = \'" + tfLoginIdSelect.getText().trim() + "\'" : "") +
                ((loginIDFilterSelected || userIDFilterSelected || usernameFilterSelected) && passwordFilterSelected ? " AND " : "")+
                (passwordFilterSelected ? "password = \'" + simpleEncryptPassword(pfPasswordSelect.getText().trim()) + "\'" : "");

        String fullQuery = "SELECT loginID, userID" + selectedFieldsSQLQuery + " FROM GDhimaLogins" +
                (filtersSQLQuery.trim().isBlank() ? ";" : " WHERE " + filtersSQLQuery + ";");

        System.out.println(fullQuery);
        return fullQuery;
    }

    private String simpleEncryptPassword(String password){ //returns the encrypted password according to a simple algorithm

        String encryptedPassword = "";
        for (int i = 0; i < password.length(); i++){
            encryptedPassword += (char)(password.charAt(i) + encryptionNumber);
        }
        return encryptedPassword;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
