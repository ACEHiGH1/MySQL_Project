import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;


public class CreateSQLTable extends Application implements CodeConstants,MySQLQueries {

    private Connection connection; //A connection instance created used to connect with the MySQL database.
    private Statement statement; //A statement instance that will be used to send SQL queries.
    private TextField tfUsernameField; //Textfield where the user will provide their database username.
    private PasswordField pfPasswordField; //Passwordfield where the user will provide their database password.
    private Label errorLabel = new Label(""); //Label which is dynamically updated based on the errors found.
    private Integer currentPhaseCode; //Integer that will keep track of the errors found or the stage the program is currently at.
    private Scene scene; //The scene that will be displayed.
    private Button createUsersTable; //Button that creates the users table.
    private Button createLoginsTable; //Button which creates the logins table.
    private Button checkTablesStatus; //Button which checks the statuses of the user and login table.
    private Label userTableStatusLabel; //Label which shows the status of the user table.
    private Label loginTableStatusLabel; //Label which shows the status of the login table.
    @Override
    public void start(Stage primaryStage) throws Exception {
        currentPhaseCode = PROGRAM_STARTED; //sets the phaseCode to indicate that the program started.

        tfUsernameField = new TextField();
        pfPasswordField = new PasswordField();

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

        dbCredentials.add(welcomeLabel, 0, 1, 5, 1);
        dbCredentials.add(usernameLabel, 0, 2);
        dbCredentials.add(tfUsernameField, 2, 2);
        dbCredentials.add(passwordLabel, 0, 3);
        dbCredentials.add(pfPasswordField, 2, 3);
        dbCredentials.add(logIn, 4, 4);
        dbCredentials.add(errorLabel, 0, 4, 3, 1);

        scene = new Scene(dbCredentials, 500, 250); //sets the credentialsGridPane as a the root of the scene.

        logIn.setOnAction(e -> connectToDB()); //Eventhandler when the login button is pressed.

        primaryStage.setScene(scene); //sets the Scene
        primaryStage.setTitle("MySQL User Client"); //sets the title.
        primaryStage.show();
    }

    private void connectToDB() {
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
            connection = DriverManager.getConnection("jdbc:mysql://localhost/", username, password); //tries to establish a connection with the provided credentials.

        } catch (SQLException e) { //if a SQLException is caught, it means that the connection couldn't be established with the credentials.
            tfUsernameField.clear(); //clears the username TF.
            pfPasswordField.clear(); //clears the password PF.
            errorLabel.setText("Wrong Credentials."); //shows the user that the credentials provided are wrong.
            currentPhaseCode = WRONG_CREDENTIALS; //indicates that the credentials provided are wrong.
            System.out.println("Wrong Credentials.");
        }

        if (currentPhaseCode == PROGRAM_STARTED) //checks if the code hasn't changed from the beginning of the handler.
            currentPhaseCode = CONNECTED; //in that case, we have connected successfully and the code is reassigned to the new phase.

        if (currentPhaseCode == CONNECTED) { //checks if we are connected to the database.

            int result; //keeps track of the number of changes made through the queries.
            try {
                statement = connection.createStatement(); //creates an instance of a statement.
                result = statement.executeUpdate(CREATE_SQL_DATABASE); //executes the query responsible for creating the database
                //and assigns the row count for the changes made to the result variable.
                if(result != 0) //checks if there were made changes and the database was actually created.
                    System.out.println("Database created");

                //Creating a GridPane for the Connection Phase.
                GridPane successfulLogin = new GridPane();
                successfulLogin.setAlignment(Pos.CENTER);
                successfulLogin.setHgap(10);
                successfulLogin.setVgap(10);
                successfulLogin.setPadding(new Insets(25, 25, 25, 25));

                Label successfulLoginLabel = new Label("Welcome");
                successfulLoginLabel.setTextFill(Color.BLUE);
                successfulLoginLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

                Label tablesStatus = new Label("Status");
                Label userTableLabel = new Label("User Table:");
                userTableStatusLabel = new Label("Unknown");
                Label loginTableLabel = new Label("Login Table:");
                loginTableStatusLabel = new Label("Unknown");
                checkTablesStatus = new Button("Check Tables' Statuses");
                createLoginsTable = new Button("Create Login Table");
                createUsersTable = new Button("Create Users Table");
                createUsersTable.setDisable(true); //Doesn't allow the user to create the User Table without checking the status first.
                createLoginsTable.setDisable(true); //Doesn't allow the user to create the Login Table without checking the status first.

                successfulLogin.add(successfulLoginLabel, 1, 0, 3, 1);
                successfulLogin.add(tablesStatus, 1, 1);
                successfulLogin.add(userTableLabel, 0, 2);
                successfulLogin.add(userTableStatusLabel, 1, 2);
                successfulLogin.add(loginTableLabel, 0, 3);
                successfulLogin.add(loginTableStatusLabel, 1, 3);
                successfulLogin.add(checkTablesStatus, 3, 3, 3, 1);
                successfulLogin.add(createUsersTable,0,4,2,1);
                successfulLogin.add(createLoginsTable,3,4,2,1);

                scene.setRoot(successfulLogin); //sets the root of the scene accordingly to the new phase.

                checkTablesStatus.setOnAction(e-> { //eventHandler for the checkTables button.
                    try {
                        checkTables();
                    } catch (SQLException ex) {
                        System.out.println("Closed connection or database error occurred.");
                        ex.printStackTrace();
                    }
                });
                System.out.println(currentPhaseCode);
            } catch (SQLException e) {
                System.out.println("SQL query is invalid.");
                e.printStackTrace();
            }
        }

    }
    private void checkTables() throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData(); //gets the metadata of the database.
        Boolean userTableFound; //flag whether the user table exists or not.
        Boolean loginTableFound; //flag whether the login table exists or not.
        checkTablesStatus.setDisable(true); //doesn't allow the user to check for the tables' status again.

        ResultSet tables = dbm.getTables(null,null,"GDhimaUsers",null); //creates a result set
        //which checks whether they are any tables named GDhimaUsers.
        if(tables.next()){ //checks whether the result set is not empty.
            userTableFound = true; //if it's not empty, the table exists and we rearrange the flag accordingly
        }else{
            userTableFound = false; //if it's empty, the table does not exist and the flag is arrenged accordingly.
        }

        tables = dbm.getTables(null,null,"GDhimaLogins",null); //gets the result set of
        //the tables which are named GDhimaLogins.
        if(tables.next()){ //if the result set is not empty, the table exists, make the flag for the login table true.
            loginTableFound = true;
        }else{ //if the set is empty, the table doesn't exist and the flag is equal to false.
            loginTableFound = false;
        }

        //Creating 4 possible outcomes regarding the tables' statuses.
        if(userTableFound && loginTableFound){ //both tables exist.
            currentPhaseCode = USER_FOUND_LOGIN_FOUND;
            userTableStatusLabel.setText("Found");
            userTableStatusLabel.setTextFill(Color.GREEN);

            loginTableStatusLabel.setText("Found");
            loginTableStatusLabel.setTextFill(Color.GREEN);

        }else if(!userTableFound && loginTableFound){ //the user table is missing, the login table exists.
            currentPhaseCode = USER_NOTFOUND_LOGIN_FOUND; //changes the code accordingly to the phase
            userTableStatusLabel.setText("Missing");
            userTableStatusLabel.setTextFill(Color.RED);

            loginTableStatusLabel.setText("Found");
            loginTableStatusLabel.setTextFill(Color.GREEN);
            createUsersTable.setDisable(false); //allows the user to create the user table.
            createUsersTable.setOnAction(e-> { //eventhandler for the button which allows the user to create the users table.
                try {
                    createUsersTableEvent();
                } catch (SQLException ex) {
                    System.out.println("Unable to create the users table.");
                    ex.printStackTrace();
                }
            });

        }else if(userTableFound && !loginTableFound){ //The login table is missing, user table exists.
            currentPhaseCode = USER_FOUND_LOGIN_NOTFOUND; //changes the code accordingly to the phase
            userTableStatusLabel.setText("Found");
            userTableStatusLabel.setTextFill(Color.GREEN);

            loginTableStatusLabel.setText("Missing");
            loginTableStatusLabel.setTextFill(Color.RED);
            createLoginsTable.setDisable(false); //allows the user to press the button to create the logins table.
            createLoginsTable.setOnAction(e -> { //eventhandler for that button
                try {
                    createLoginTableEvent();
                } catch (SQLException ex) {
                    System.out.println("Unable to create the login table");
                    ex.printStackTrace();
                }
            });

        }else if (!userTableFound && !loginTableFound){ //Neither of the tables is found.
            currentPhaseCode = USER_NOTFOUND_LOGIN_NOTFOUND;
            userTableStatusLabel.setText("Missing");
            userTableStatusLabel.setTextFill(Color.RED);

            loginTableStatusLabel.setText("Missing");
            loginTableStatusLabel.setTextFill(Color.RED);
            createLoginsTable.setDisable(false); //allows the user to press the button to create the logins table.
            createUsersTable.setDisable(false); //allows the user to press the button to create the users table.

            createLoginsTable.setOnAction(e -> { //event handler for the button which creates the logins table.
                try {
                    createLoginTableEvent();
                } catch (SQLException ex) {
                    System.out.println("Unable to create the login table.");
                    ex.printStackTrace();
                }
            });

            createUsersTable.setOnAction(e-> { // event handler for the button which creates the users table.
                try {
                    createUsersTableEvent();
                } catch (SQLException ex) {
                    System.out.println("Unable to create the users table.");
                    ex.printStackTrace();
                }
            });
        }
    }

    private void createLoginTableEvent() throws SQLException {
        statement.executeUpdate(USE_GDHIMA_DATABASE); //tells the program to use the database we created earlier.
        statement.executeUpdate(CREATE_LOGIN_TABLE); //executes the query to create the login table.
        System.out.println("Login Table Created.");
        createLoginsTable.setDisable(true); //prohibits user from creating the login table again.
        loginTableStatusLabel.setText("Created"); //changes the status to created.
        loginTableStatusLabel.setTextFill(Color.GREEN);
    }
    private void createUsersTableEvent() throws SQLException {
        statement.executeUpdate(USE_GDHIMA_DATABASE);  //tells the program to use the database we created earlier.
        statement.executeUpdate(CREATE_USERS_TABLE); //executes the query to create the users table.
        createUsersTable.setDisable(true); //prohibits user from creating the user table again.
        userTableStatusLabel.setText("Created"); //changes the status to created.
        userTableStatusLabel.setTextFill(Color.GREEN);

        System.out.println("User Table Created.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}