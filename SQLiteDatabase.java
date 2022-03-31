package banking;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class SQLiteDatabase {
    private final String dbURL;
    private final String dbName;

    public SQLiteDatabase(String dbPath) {
        this.dbURL = "jdbc:sqlite:./" + dbPath;
        this.dbName = dbPath.substring(0, dbPath.length() - 5);

        File dbFile = new File("./" + dbPath);
        if (!dbFile.exists()) {
            createNewSQLiteDatabase();
        }
        createTable(this.dbName);
    }

    public void createNewSQLiteDatabase() {

        try (Connection connection = connect()) {
            DatabaseMetaData meta = connection.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
            System.out.println("A new database has been created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    String getDbName() {
        return this.dbName;
    }

    // ---------------------
    // Commands to SQL
    // ---------------------

    void addAccountToDatabase(Account account) {

        String sqlStatement = ""
                + "INSERT INTO " + this.dbName + "(number, pin) \n"
                + "VALUES ('" + account.getCreditCardNumber() + "', '" + account.getPin() + "');";

        executeSQLUpdate(sqlStatement);
    }

    int addMoneyToAccountInDatabase(Account account, int income) {

        String sqlStatement = ""
                + "UPDATE " + this.dbName + "\n"
                + "SET balance = " + (account.getBalance() + income) + "\n"
                + "WHERE number = '" + account.getCreditCardNumber() + "';";

        int ret = executeSQLUpdate(sqlStatement);

        if (income == 0) {
            ret = 0;
        }

        return ret;
    }

    void clearDatabase() {

        String sqlStatement = ""
                + "DROP TABLE " + this.dbName + ";";

        //System.out.println(sqlStatement);

        int isCleared = executeSQLUpdate(sqlStatement);

        if (isCleared != 0) {
            System.out.println("Database " + this.dbName + " has been cleared");
        }
    }

    int clearAccountFromDatabase(Account account) {

        String sqlStatement = ""
                + "DELETE FROM " + this.dbName + "\n"
                + "WHERE number = '" + account.getCreditCardNumber() +"';";

        int ret = executeSQLUpdate(sqlStatement);

        return ret;
    }

    private void createTable(String dbName) {

        String sqlStatement = ""
                + "CREATE TABLE IF NOT EXISTS " + dbName + " (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	number TEXT NOT NULL,\n"
                + "	pin TEXT NOT NULL,\n"
                + " balance INTEGER DEFAULT 0\n"
                + ");";

        executeSQLUpdate(sqlStatement);
    }

    Account findAccountFromDatabase(String ccNum) {

        Account ret = null;

        String sqlStatement = ""
                + "SELECT\n"
                    + "number,\n"
                    + "pin,\n"
                    + "balance\n"
                + "FROM\n"
                    + this.dbName + "\n"
                + "WHERE\n"
                    + "number = '" + ccNum + "'\n"
                + ";";

        ArrayList<String> resultSet = executeSQLQuery(sqlStatement);
        if (resultSet != null && resultSet.size() != 0) {
            String ccNumFromDB = resultSet.get(0);
            //System.out.println("Found credit card number: " + ccNumFromDB);
            String ccPinFromDB = resultSet.get(1);
            //System.out.println("Found pin: " + ccPinFromDB);
            String ccBalanceFromDB = resultSet.get(2);
            //System.out.println("balance is: " + ccBalanceFromDB);

            ret = new Account(ccNumFromDB, ccPinFromDB, Integer.parseInt(ccBalanceFromDB));
        }

        return ret;
    }

    int getNumberOfAccounts() {

        String sqlStatement = ""
                + "SELECT COUNT(*) FROM " + this.dbName + ";";

        int ret = 0;

        ArrayList<String> resultSet = executeSQLQuery(sqlStatement);
        if (resultSet != null) {
            ret = Integer.parseInt(resultSet.get(0));
        }

        //System.out.println("Found " + ret + " accounts.");

        return ret;
    }

    Boolean transferMoneyBetweenAccountsInDatabase(Account fromAccount, Account toAccount, long amount) {

        Boolean ret = false;

        String sqlStatementFrom = ""
                + "UPDATE " + this.dbName + "\n"
                + "SET balance = balance - " + amount + "\n"
                + "WHERE number = '" + fromAccount.getCreditCardNumber() + "';";

        String sqlStatementTo = ""
                + "UPDATE " + this.dbName + "\n"
                + "SET balance = balance + " + amount + "\n"
                + "WHERE number = '" + toAccount.getCreditCardNumber() + "';";

        int updatedRowsTotal = executeUpdateTransaction(sqlStatementFrom, sqlStatementTo);

        if (updatedRowsTotal > 0) {
            ret = true;
        }

        return ret;
    }

    // -------------
    // SQL Wrappers
    // -------------

    private Connection connect() {
        Connection ret = null;
        try {
            ret = DriverManager.getConnection(this.dbURL);
            //System.out.println("Established connection to " + this.dbName + " database");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }

    // executeUpdate for INSERT, DELETE, UPDATE, CREATE, DROP
    private int executeSQLUpdate(String sqlStatement) {

        int ret = 0;

        try (
          Connection connection = connect();
          Statement statement = connection.createStatement();
        ) {
            ret = statement.executeUpdate(sqlStatement);
            //System.out.println(
            //        "Result of SQL: \n" + sqlStatement + "\n" +
            //                "is " + ret + " updated rows in " + this.dbName + " database."
            //);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return ret;
    }

    // executeQuery for SELECT
    private ArrayList<String> executeSQLQuery(String sqlStatement) {

        ArrayList<String> ret = new ArrayList<>();

        //System.out.println("Attempting \n" + sqlStatement);

        try (
                Connection connection = connect();
                Statement statement = connection.createStatement();
        ) {
            ResultSet resultSet = statement.executeQuery(sqlStatement);
            //System.out.println(
            //        "Result of SQL: \n" + sqlStatement + "\n" +
            //        "is " + resultSet
            //);

            while (resultSet.next()) {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    ret.add(resultSet.getString(i));
                    //System.out.println("Column " + i + ": " + resultSet.getString(i));
                }
            }

            return ret;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return ret;
    }

    private int executeUpdateTransaction(String sqlStatement1, String sqlStatement2) {

        int ret = 0;

        try (Connection connection = connect()) {

            connection.setAutoCommit(false);

            try (
                PreparedStatement statement1 = connection.prepareStatement(sqlStatement1);
                PreparedStatement statement2 = connection.prepareStatement(sqlStatement2)
            ) {
                ret += statement1.executeUpdate();
                ret += statement2.executeUpdate();

                connection.commit();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return ret;
    }
}
