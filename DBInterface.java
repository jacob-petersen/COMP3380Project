import java.util.Scanner;
import java.util.ArrayList;
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

        // This code will only run once the user has indicated they want to quit the program.
        System.out.print("\033[H\033[2J");
        System.exit(0);

    }

    /*
        Main system state functions.
    */

    // The main menu handler.
    public void mainMenu() {
        
        clearTerminal();

        // Update this if you add or remove numeric options to the menu
        final int NUMBER_OF_OPTIONS = 16;    

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
        System.out.println("\t[16] Raw tableName information");

        System.out.println("\n\t[Q] To exit program.");
        System.out.println("\n\t[HELP] For help.\n");

        // Get user input. Keep asking until their input is a valid int.
        
        boolean validInput = false;
        String userInput = "";

        while (!validInput) {
            System.out.print("\tEnter selection >>> ");
            userInput = sc.nextLine().trim().toLowerCase();

            if (userInput.equals("q")) {
                this.state = ProgramState.QUIT;
                return;
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
            // This sends the cursor up 2 lines so that we don't run off the screen if the user spams bad inputs
            System.out.print("\033[1A\033[1A\033[2K\r");

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
            
            switch (this.querySelection) {

                case 16:

                    clearTerminal();

                    // This is a special case with some suboptions
                    System.out.println("\tSelect tableName to dump.\n");
                    System.out.println("\t[1] Airlines");
                    System.out.println("\t[2] Airports");
                    System.out.println("\t[3] Flights");
                    System.out.println("\t[4] Planes");
                    System.out.println("\t[5] Runways\n");

                    boolean validInput = false;
                    String userInput = "";
                    int userTableSelection = 0;

                    while (!validInput) {
                        System.out.print("\tEnter selection >>> ");
                        userInput = sc.nextLine().trim();
                        
                        try {
                            userTableSelection = Integer.parseInt(userInput);
                            if (userTableSelection > 0 && userTableSelection <= 5) {
                                validInput = true;
                            } else {
                                System.out.println("Please enter a valid selection!");
                                System.out.print("\033[1A\033[1A\033[2K\r");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("\tPlease enter a valid selection!");
                            System.out.print("\033[1A\033[1A\033[2K\r");
                        }

                    }

                    // When we get here, userTableSelection is an int between 1 and 5 (inclusive)
                    String[] tableMap = {"", "Airlines", "Airports", "Flights", "Planes", "Runways"};
                    String tableName = tableMap[userTableSelection];

                    sql = "SELECT * FROM " + tableName;
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

            // The following code prints the actual query to the screen.
            clearTerminal();
            
            // Create a QueryResults object from the ResultSet.
            QueryResults queryResults = new QueryResults(resultSet);
            System.out.println("\tSQL Query successful. Retrieved " + noColumns + " columns.");


        
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

        // TODO: make this look pretty
        System.out.println("\n\tCOMP 3380 Project Group 25 Interface");
        System.out.println("\tAviation Statistics Database");
        System.out.println("\tBrenlee Grant");
        System.out.println("\tJorja Prokpich");
        System.out.println("\tJascha Petersen\n");
    }

    public int getLongestFieldInColumn(String tableName, String column) {
        try {
            // Query to get the single longest entry in a column. 
            String sql = "SELECT " + column.trim() + " FROM " + tableName.trim() + " ORDER BY LENGTH(" + column.trim() + ") DESC LIMIT 1"; 
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

// Helper class for processing query results
class QueryResults {

    /*
        columnNames keeps track of the name of each column.
        columnMaxFieldLengths keeps track of the longest field in each column. Needed for printing later.
        rows stores the actual data, in a 2D ArrayList of strings
    */
    ArrayList<String> columnNames = new ArrayList<String>();
    ArrayList<Integer> columnMaxFieldLengths = new ArrayList<Integer>();
    ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
    int noRows;
    int noColumns;

    // Constructor that ingests the results from resultSet and stores them in memory
    public QueryResults(ResultSet resultSet) throws SQLException {

        // Get metadata about the query results
        ResultSetMetaData metadata = resultSet.getMetaData();
        noColumns = metadata.getColumnCount();
        noRows = 0; // Updated later

        // Get the names of all columns
        for (int i = 1; i <= noColumns; i++) {
            columnNames.add(metadata.getColumnName(i));
            columnMaxFieldLengths.add(0);
        }

        while (resultSet.next()) {
            
            // Populate this row. Also update columnFieldMaxLength if necessary.
            ArrayList<String> thisRow = new ArrayList<String>();
            
            int i = 0;
            for (String cName: columnNames) {
                // Get the current field. Add it to this row.
                String thisField = resultSet.getString(cName);
                thisRow.add(thisField);
                
                // Check if the current field's length is larger than the previous maximum. If so, update the maximum.
                int thisFieldLength = thisField.length();
                int currentMaxLength = columnMaxFieldLengths.get(i);

                if (thisFieldLength > currentMaxLength) {
                    columnMaxFieldLengths.set(i, thisFieldLength);
                }
                i++;
            }

            // Add this row to the list of rows.
            rows.add(thisRow);
            noRows++;

        }

        return;
    }
}