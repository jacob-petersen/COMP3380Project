import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
import java.sql.*;

public class DBInterface {

    // Enum used to track the current state of the program
    enum ProgramState {
        MAIN_MENU,
        HELP_MENU,
        DISPLAY_QUERY,
        QUIT
    }

    // State variable and querySelection variable to track which query we want if the state is DISPLAY_QUERY.
    private ProgramState state = ProgramState.MAIN_MENU;
    private int querySelection; 

    // SQL database connection object
    private Connection connection;

    // Scanner for taking user input
    Scanner sc = new Scanner(System.in);

    // Constructor. Used to set up the database connection.
    public DBInterface() {

        // Set up the database connection. Throw and error and panic if it fails.
        try {
            final String CONNECTION_URL = "jdbc:sqlite:officialData.db";
            connection = DriverManager.getConnection(CONNECTION_URL);
        } catch (SQLException e) {
            System.out.println("Error opening .db file!");
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    public static void main(String[] args) {

        // DBInterface object
        DBInterface db = new DBInterface();
        
        while (db.state != ProgramState.QUIT) {
            switch(db.state) {
                case MAIN_MENU:
                    db.mainMenu();
                    break;
                case HELP_MENU:
                    db.helpMenu();
                    break;
                case DISPLAY_QUERY:
                    db.displayQuery();
                    break;
                case QUIT:
                    break;
            }
        }

    }

    /*
        Main system state functions.
    */

    // The main menu handler.
    public void mainMenu() {
        
        // Update this if you add or remove numeric options to the menu
        final int NUMBER_OF_OPTIONS = 16;
        
        clearTerminal();

        // TODO: make this look pretty
        System.out.println("\n\tCOMP 3380 Project Group 25 Interface");
        System.out.println("\tAviation Statistics Database");
        System.out.println("\tBrenlee Grant");
        System.out.println("\tJorja Prokpich");
        System.out.println("\tJascha Petersen\n");

        System.out.println("\t[ 1] Track pilot's journey in a day");
        System.out.println("\t[ 2] Common layover locations");
        System.out.println("\t[ 3] Most productive employees");
        System.out.println("\t[ 4] Most popular airlines");
        System.out.println("\t[ 5] Passengers flying home");
        System.out.println("\t[ 6] Top plane models requiring servicing");
        System.out.println("\t[ 7] All flights departing from airport");
        System.out.println("\t[ 8] All luggage owned by passenger");
        System.out.println("\t[ 9] All flights from airline");
        System.out.println("\t[10] Most common destination airport based on origin airport");
        System.out.println("\t[11] Average age of aircraft in airline fleet");
        System.out.println("\t[12] Average number of bags per passenger on flight");
        System.out.println("\t[13] Employee's completed jobs");
        System.out.println("\t[14] Average flight length from origin and destination");
        System.out.println("\t[15] Luggage lost per airport");
        System.out.println("\t[16] Raw table information");

        System.out.println("\n\t[Q] To exit program.");
        System.out.println("\n\t[HELP] For help.");

        // Get user input. Keep asking until their input is a valid int.
        
        boolean validInput = false;
        String userInput = "";

        while (!validInput) {
            System.out.print("\n\tEnter selection >>> ");
            userInput = sc.nextLine().trim().toLowerCase();

            if (userInput.equals("q")) {
                clearTerminal();
                System.exit(0);
            } else if (userInput.equals("help")) {
                this.state = ProgramState.HELP_MENU;
                return;
            }

            // If we get here, the user input is either an integer or something invalid.
            try {
                int userQuerySelection = Integer.parseInt(userInput);
                if (userQuerySelection > 0 && userQuerySelection <= NUMBER_OF_OPTIONS) {
                    validInput = true;
                    this.state = ProgramState.DISPLAY_QUERY;
                    this.querySelection = userQuerySelection;
                    return;
                }
            } catch (NumberFormatException e) {
                // Do nothing. validInput remains false, the loop continues.
            }

            System.out.println("\tPlease enter a valid input!");

        }

        

    }

    // Displays the help menu, gives instructions on specific commands.
    public void helpMenu() {

        System.out.println("\n\tTHIS IS THE HELP MENU PLACEHOLDER");
        this.state = ProgramState.QUIT;

    }

    // Displays a query based on what the user wants.
    public void displayQuery() {
        try {
            System.out.println("\n\tTHIS IS THE DISPLAY QUERY PLACEHOLDER");
            System.out.println("\tQUERY SELECTION: " + this.querySelection + "\n");
            
            // Build the query based on the desired selection from the user.
            String sql = "";
            PreparedStatement statement = null; // Scary but shouldn't cause issues. This won't stay null.
            ArrayList<String> columnNames = new ArrayList<String>();
            
            switch (this.querySelection) {
                case 16:
                    sql = "SELECT * FROM Airports";
                    statement = connection.prepareStatement(sql);
                    
                    break;

                default:
                    // Placeholder during development. This should never run in practice.
                    this.state = ProgramState.MAIN_MENU;
                    return;
                    
            }

            // Execute the query and gather some metadata on it
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int noColumns = metadata.getColumnCount();

            // ArrayList that holds metadata about each column
            ArrayList<QueryColumn> columnData = new ArrayList<QueryColumn>();

            // Print the actual query.
            clearTerminal();

            System.out.println("\tSQL Query successful. Retrieved " + noColumns + " columns.");

            // Populate the columnData list with information about each column
            for (int i = 1; i <= noColumns; i++) {

                QueryColumn thisColumn = new QueryColumn();

                thisColumn.number = i;
                thisColumn.name = metadata.getColumnName(i);
                thisColumn.maxFieldLength = getLongestFieldInColumn("Airports", thisColumn.name);
                thisColumn.displayWidth = Math.max(thisColumn.name.length(), thisColumn.maxFieldLength);

                columnData.add(thisColumn);
                
                System.out.println("Column " + thisColumn.number + ", Name " + thisColumn.name + ", maxFieldLength " + thisColumn.maxFieldLength + ", displayWidth " + thisColumn.displayWidth);
            }

            // Print column headers
            for (int i = 1; i <= noColumns; i++) {
                System.out.print(metadata.getColumnName(i) + "\t\t");
            }
            System.out.println();
            
            while (resultSet.next()) {
                for (String s: columnNames) {
                    String data = resultSet.getString(s);
                    System.out.print(data + "\t\t");
                }
                System.out.println();
            }

            this.state = ProgramState.QUIT;

        } catch (SQLException e) {
            // Something went wrong. Print error and panic.
            System.out.println("\nError: something when wrong attempting to execute the SQL query.");
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    /*
    
    Utility functions.

    */

    // Clears the terminal. Gives the illusion of a persistent UI when all we're doing is reprinting it.
    // Works using ANSI escape codes to clear the terminal. Should work on most modern terminals, unix or windows.
    public static void clearTerminal() {
        System.out.print("\033[H\033[2J");
    }

    public int getLongestFieldInColumn(String table, String column) {
        try {
            // Query to get the single longest entry in a column. 
            String sql = "SELECT " + column.trim() + " FROM " + table.trim() + " ORDER BY LENGTH(" + column.trim() + ") DESC LIMIT 1"; 
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            resultSet.next();

            String result = resultSet.getString(column.trim());
            
            return result.length();

        } catch (SQLException e) {
            // Something went wrong. Print error and panic.
            System.out.println("\nError: something went wrong attempting to execute SQL query to get max width of column.");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return -1; // This will never run. Compiler wants it though. 
        
    }

}

// Quick and dirty struct
class QueryColumn {
    public String name;
    public int number;
    public int maxFieldLength;
    public int displayWidth;
}