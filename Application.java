package banking;

import java.util.Scanner;

public class Application {
    private Scanner sc = new Scanner(System.in);
    private final SQLiteDatabase database;
    private int currentMenu = 0;
    private int currentItem = -1;
    private Account accessedAccount;
    private Account transferAccount;

    public Application(SQLiteDatabase database) {
        this.database = database;
    }

    public void start() {
        boolean isRunning = true;

        while(isRunning) {

            switch (currentMenu) {
                case 0 :
                    if (currentItem == 0) {
                        isRunning = false;
                    }
                    currentItem = mainMenu(currentItem);
                    break;
                case 1:
                    currentItem = accountMenu(currentItem);
                    break;
            }

        }
    }

    public int mainMenu(int item) {

        switch (item) {
            case -1: // Starting Menu
                System.out.println(
                        "1. Create an account\n" +
                        "2. Log into account\n" +
                        "3. Clear database\n" +
                        "0. Exit");
                return Integer.parseInt(getUserInput());
            case 0:
                System.out.println("Bye!");
                return 0;
            case 1: // Create Card
                // Generate card number, pin and add to database
                Account newAccount = Account.generateNewAccount(database);
                System.out.println(
                        "Your card has been created\n" +
                        "Your card number:\n" +
                        newAccount.getCreditCardNumber() + "\n" +
                        "Your card PIN:\n" +
                        newAccount.getPin() + "\n" +
                        "");
                return -1;
            case 2: // Log into Account
                System.out.println("Enter your card number:");
                String creditCardNumber = getUserInput();
                System.out.println("Enter your PIN:");
                this.accessedAccount = Account.verifyAccountExistsInDatabase(creditCardNumber, getUserInput(), database);
                if (this.accessedAccount == null) {
                    System.out.println("Wrong card number or PIN!");
                } else {
                    System.out.println("You have successfully logged in!");
                    this.currentMenu = 1;
                }
                return -1;
            case 3: // Clear the database
                System.out.println("Clearing database " + database.getDbName());
                database.clearDatabase();
                return -1;
            default:
                System.out.println("Unrecognized command.");
                break;
        }
        return -1;
    }

    public int accountMenu(int item) {

        switch (item) {
            case -1:
                System.out.println(
                        "1. Balance\n" +
                        "2. Add income\n" +
                        "3. Do transfer\n" +
                        "4. Close account\n" +
                        "5. Log out\n" +
                        "0. Exit");
                return Integer.parseInt(getUserInput());
            case 0:
                this.currentMenu = 0;
                this.accessedAccount = null;
                return 0;
            case 1:
                System.out.println("Balance: " + accessedAccount.getBalanceFromDatabase(this.database));
                System.out.println();
                return -1;
            case 2:
                System.out.println("Enter income:");
                int income = 0;
                try {
                    income = Integer.parseInt(getUserInput());
                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                }
                // Update table
                Boolean updatedIncome = this.accessedAccount.addMoneyToAccount(income, this.database);

                if (updatedIncome) {
                    System.out.println("Income was added!");
                } else {
                    System.out.println("No Income was added.");
                }
                System.out.println();

                return -1;
            case 3:
                // Ask for ccNum to transfer to
                System.out.println();
                System.out.println("Transfer");
                System.out.println("Enter card number:");
                String ccNum = getUserInput();

                // check card with Luhn
                if (!Account.verifyAuthenticityWithLuhnAlg(ccNum)) {
                    System.out.println("Probably you made a mistake in the card number.  Please try again!");
                    System.out.println();
                    return -1;
                }

                // check card exists
                this.transferAccount = Account.verifyAccountExistsInDatabase(ccNum, this.database);
                if (transferAccount == null) {
                    System.out.println("Such a card does not exist.");
                    System.out.println();
                    return -1;
                }

                // Ask for amount to transfer
                System.out.println("Enter how much money you want to transfer:");
                long transferAmount = Integer.parseInt(getUserInput());
                // check amount
                long accessedAmount = accessedAccount.getBalanceFromDatabase(this.database);
                System.out.println("From Account has: " + accessedAmount);
                System.out.println("Trying to transfer: " + transferAmount);
                if (accessedAmount <= transferAmount) {
                    System.out.println("Not enough money!");
                    System.out.println();
                    this.transferAccount = null;
                    return -1;
                }

                // do transfer
                Boolean isTransfered =
                        accessedAccount.transferMoneyToAccount(this.transferAccount, this.database, transferAmount);

                if (isTransfered) {
                    System.out.println("Success!");
                    System.out.println();
                } else {
                    System.out.println("An internal error occurred.\n");
                }
                this.transferAccount = null;

                return -1;
            case 4:
                this.accessedAccount.removeAccountFromDatabase(this.database);
                System.out.println("The account has been closed!");
                this.currentMenu = 0;
                this.accessedAccount = null;
                System.out.println();
                return -1;
            case 5:
                System.out.println("You have logged out.");
                this.accessedAccount = null;
                this.currentMenu = 0;
                System.out.println();
                return -1;
            default:
                System.out.println("Unrecognized command.");
                System.out.println();
                break;
        }
        return -1;
    }

    public String getUserInput() {
        return sc.nextLine();
    }
}
