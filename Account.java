package banking;

import java.util.Random;

public class Account {
    private final String ccNum;
    private String ccPin;
    private long balance;

    // MII - Major Industry Indentifier
    private static final char MII = '4';
    // BIN - Bank Indentification Number
    private static final char[] BIN = {'0', '0', '0', '0', '0'};
    // Random Account Number
    private static final int ACNT_SIZE = 9;
    private static final int PIN_SIZE = 4;

    public Account(String ccNum, String ccPin, long balance) {
        this.ccNum = ccNum;
        this.ccPin = ccPin;
        this.balance = balance;
    }

    Boolean addMoneyToAccount(int income, SQLiteDatabase database) {

        Boolean ret = false;
        int updatedRows = database.addMoneyToAccountInDatabase(this, income);

        if (updatedRows > 0) {
            ret = true;
        }

        return ret;
    }

    static Account generateNewAccount(SQLiteDatabase database) {

        char[] ccNum = new char[16];
        Random rand = new Random(database.getNumberOfAccounts());

        ccNum[0] = MII;
        ccNum[1] = BIN[0];
        ccNum[2] = BIN[1];
        ccNum[3] = BIN[2];
        ccNum[4] = BIN[3];
        ccNum[5] = BIN[4];
        for (int i = 0; i < ACNT_SIZE; i++) {
            ccNum[i + 6] = (char) (48 + rand.nextInt(10));
        }

        // Luhn Algorithm to generate CK_SUM (the 16th digit)
        int[] ccNumTemp = new int[15];
        int sum = 0;
        for (int i = 0; i < ccNum.length - 1; i++) {
            ccNumTemp[i] = (i + 1) % 2 == 0 ?
                    (ccNum[i] - 48) :
                    (ccNum[i] - 48) * 2 > 9 ?
                            (ccNum[i] - 48) * 2 - 9 :
                            (ccNum[i] - 48) * 2;
            sum += ccNumTemp[i];
        }
        ccNum[6 + ACNT_SIZE] = 10 - sum % 10 + 48 == 58 ? '0' : (char) (10 - sum % 10 + 48);

        // convert to string
        String retCCNum = "";
        for (char ch : ccNum) {
            retCCNum += ch;
        }

        // generate Pin
        String retPin = "";
        for (int i = 0; i < PIN_SIZE; i++) {
            retPin += (char) (48 + rand.nextInt(10));
        }

        Account ret = new Account(retCCNum, retPin, 0);

        database.addAccountToDatabase(ret);
        return ret;
    }

    long getBalanceFromDatabase(SQLiteDatabase database) {

        Account accountInDB = database.findAccountFromDatabase(this.ccNum);

        return accountInDB.getBalance();
    }

    Boolean removeAccountFromDatabase(SQLiteDatabase database) {

        Boolean ret = false;
        int updatedRows = database.clearAccountFromDatabase(this);

        if (updatedRows > 0) {
            ret = true;
        }

        return ret;
    }

    Boolean transferMoneyToAccount(Account transferAccount, SQLiteDatabase database, long amount) {

        Boolean ret = false;

        ret = database.transferMoneyBetweenAccountsInDatabase(this, transferAccount, amount);

        return ret;
    }

    static Account verifyAccountExistsInDatabase(String ccNum, String pin, SQLiteDatabase database) {

        Account ret = database.findAccountFromDatabase(ccNum);

        if (ret != null) {
            if (!ret.getPin().equals(pin)) {
                ret = null;
            }
        }

        return ret;
    }

    static Account verifyAccountExistsInDatabase(String ccNum, SQLiteDatabase database) {

        Account ret = database.findAccountFromDatabase(ccNum);

        return ret;
    }

    static Boolean verifyAuthenticityWithLuhnAlg(String ccNum) {

        Boolean ret = false;

        int[] ccNumArr = new int[16];
        char[] ccNumChArr = ccNum.toCharArray();

        for (int i = 0; i < ccNumChArr.length; i++) {
            ccNumArr[i] = (ccNumChArr[i] - 48);
            //System.out.println(ccNumArr[i]);
        }

        int sum = 0;
        for (int i = 0; i < ccNumArr.length - 1; i++) {
            int temp = (i + 1) % 2 == 0 ?
                    ccNumArr[i] :
                    ccNumArr[i] * 2 > 9 ?
                            ccNumArr[i] * 2 - 9 :
                            ccNumArr[i] * 2;
            sum += temp;
            //System.out.println(sum);
        }
        ret = sum % 10 == 0 ? sum % 10 == ccNumArr[15] : 10 - sum % 10 == ccNumArr[15];

        return ret;
    }

    long getBalance() {
        return this.balance;
    }

    String getCreditCardNumber() {
        return this.ccNum;
    }

    String getPin() {
        return this.ccPin;
    }
}
