public interface MySQLQueries {
    public String CREATE_SQL_DATABASE = "CREATE DATABASE IF NOT EXISTS GDHIMA;";
    public String USE_GDHIMA_DATABASE = "USE GDHIMA;";

    public String CREATE_LOGIN_TABLE = "create table GDhimaLogins(" +
            "loginId INT NOT NULL AUTO_INCREMENT," +
            "userId INT NOT NULL UNIQUE," +
            "username VARCHAR(12) NOT NULL," +
            "password VARCHAR(10000) NOT NULL," +
            "PRIMARY KEY ( loginId )," +
            "FOREIGN KEY (userID) REFERENCES GDhimaUsers(userId) " +
            ");";

    public String CREATE_USERS_TABLE = "create table GDhimaUsers(" +
            "userId INT NOT NULL AUTO_INCREMENT," +
            "firstName VARCHAR(12) NOT NULL," +
            "lastName VARCHAR(12) NOT NULL," +
            "emailAddress VARCHAR(25) NOT NULL," +
            "accessLevel CHAR(5) NOT NULL," +
            "PRIMARY KEY ( userId )" +
            ");";


    public String SHOW_USER_TABLE_QUERY = "SELECT * FROM GDhimaUsers;";
    public String SHOW_LOGIN_TABLE_QUERY = "SELECT * FROM GDhimaLogins;";
    public String CLEAR_USER_TABLE_QUERY = "DELETE FROM GDhimaUsers;";
    public String CLEAR_LOGIN_TABLE_QUERY = "DELETE FROM GDhimaLogins;";
    public String INSERT_USER_TABLE_PREPAREDSTATEMENT_QUERY = "INSERT INTO GDhimaUsers (firstName, lastName, emailAddress, accesslevel) VALUES (?,?,?,?);";
    public String INSERT_LOGIN_TABLE_PREPAREDSTATEMENT_QUERY = "INSERT INTO GDhimaLogins (userID, username, password) VALUES (LAST_INSERT_ID(),?,?);";
    public String DELETE_USER_TABLE_PREPAREDSTATEMENT_QUERY = "DELETE FROM GDhimaUsers WHERE userID = ?";
    public String DELETE_LOGIN_TABLE_PREPAREDSTATEMENT_QUERY = "DELETE FROM GDhimaLogins WHERE userID = ?";
    public String UPDATE_FIRST_NAME_USER_TABLE_PREPAREDSTATEMENT_QUERY = "UPDATE GDhimaUsers SET firstName = ? WHERE userID = ?;";
    public String UPDATE_LAST_NAME_USER_TABLE_PREPAREDSTATEMENT_QUERY = "UPDATE GDhimaUsers SET lastName = ? WHERE userID = ?;";
    public String UPDATE_EMAIL_USER_TABLE_PREPAREDSTATEMENT_QUERY = "UPDATE GDhimaUsers SET emailAddress = ? WHERE userID = ?;";
    public String UPDATE_ACCESS_USER_TABLE_PREPAREDSTATEMENT_QUERY = "UPDATE GDhimaUsers SET accessLevel = ? WHERE userID = ?;";
    public String UPDATE_USERNAME_LOGINS_TABLE_PREPAREDSTATEMENT_QUERY = "UPDATE GDhimaLogins SET username = ? WHERE userID = ?;";
    public String UPDATE_PASSWORD_LOGINS_TABLE_PREPAREDSTATEMENT_QUERY = "UPDATE GDhimaLogins SET password = ? WHERE userID = ?;";
}
