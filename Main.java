package banking;

public class Main {
    public static void main(String[] args) {

        // String[] args = {"-fileName", "card.s3db"};
        // Get arguments

        String dbName = null;
        for (int i = 0; i < args.length; i = i + 2) {
            if (args[i].equals("-fileName")) {
                dbName = args[i + 1]; //
            }
        }

        //System.out.println("Name: " + dbName.substring(0, dbName.length() - 5));

        new Application(
            new SQLiteDatabase(dbName) // trim ".s3db"
        ).start();



    }
}
