// Util imports
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Properties;

// SQL imports
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
        // This code taken from Rob Guderian's SQLServerDemo.java file.

        Properties prop = new Properties();
        String fileName = "auth.cfg";

        try {
            FileInputStream configFile = new FileInputStream(fileName);
            prop.load(configFile);
            configFile.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Cound not find config file.");
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Error reading config file.");
            System.exit(1);
        }

        String username = prop.getProperty("username");
        String password = prop.getProperty("password");

        if (username == null || password == null) {
            System.out.println("Username or password not provided.");
            System.exit(1);
        }

        try {
            // final String CONNECTION_URL = "jdbc:sqlite:officialData.db";
            final String CONNECTION_URL = "jdbc:sqlserver://uranium.cs.umanitoba.ca:1433;"
                + "database=cs3380;"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "encrypt=false;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;";

            connection = DriverManager.getConnection(CONNECTION_URL);
        } catch (SQLException e) {
            System.out.println("Error connecting to SQL server!");
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    public static void main(String[] args) {

        // DBInterface object
        DBInterface db = new DBInterface();
        
        // This is the main program loop. The program is ALWAYS somewhere in here.
        while (true) {
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
                    db.shutdown();
                    System.out.print("\033[H\033[2J");
                    System.exit(0);
                    break;
            }
        }        
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
        System.out.println("\t[16] Raw table information");

        System.out.println("\n\t[Q] To exit program.");
        System.out.println("\n\t[H] For help.\n");

        // Get user input. Keep asking until their input is a valid int.
        
        boolean validInput = false;
        String userInput = "";

        while (!validInput) {
            System.out.print("\tEnter selection >>> ");
            userInput = sc.nextLine().trim().toLowerCase();

            if (userInput.equals("q")) {
                this.state = ProgramState.QUIT;
                return;
            } else if (userInput.equals("h") || userInput.equals("help")) {
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
            // ANSI spaghetti. This sends the cursor up 2 lines so that we don't run off the screen if the user spams bad inputs
            System.out.print("\033[1A\033[1A\033[2K\r");

        }

    }

    // Displays the help menu, gives instructions on specific commands.
    public void helpMenu() {

        clearTerminal();

        System.out.println("\n\tTHIS IS THE HELP MENU PLACEHOLDER");
        System.out.print("\n\tPress any key to return to main menu... ");
        sc.nextLine();
        
        this.state = ProgramState.MAIN_MENU;
    }

    // Displays a query based on what the user wants.
    // First ingests the query into the rows 2D array list and also some metadata useful later.
    public void displayQuery() {
        try {
            
            // Build the query based on the desired selection from the user.
            String sql = "";
            PreparedStatement statement = null; // Scary but shouldn't cause issues. This won't stay null.

            String tempString = "";
            int tempInt = -1;
            
            switch (this.querySelection) {
                
                // Track pilot's journey in a day
                case 1:
                    sql = """
                    SELECT Flights.flightNum, Flights.origin, Flights.schedDep, Flights.destination, Flights.schedArr FROM Fly
                    JOIN Flights ON Fly.flightNum = Flights.flightNum
                    WHERE SIN = ?
                    ORDER BY Flights.schedDep ASC
                    """;
                    statement = connection.prepareStatement(sql);
                    tempInt = getUserIntInput("Enter pilot SIN");
                    statement.setInt(1, tempInt);
                    break;

                // Most productive employees 
                case 3:
                    sql = """
                    SELECT CAST(Employee.SIN AS VARCHAR(50)) as SIN, CAST(Employee.first AS VARCHAR(50)) as first, CAST(Employee.last AS VARCHAR(50)) as last, COUNT(*) as jobsCompleted
                    FROM Employee JOIN

                    (SELECT Service.SIN FROM Service
                    UNION ALL
                    SELECT Guide.SIN FROM Guide
                    UNION ALL 
                    SELECT Fly.SIN FROM Fly) temp_table

                    ON Employee.SIN = temp_table.SIN
                    GROUP BY CAST(Employee.SIN AS VARCHAR(50)), CAST(Employee.first AS VARCHAR(50)), CAST(Employee.last AS VARCHAR(50))
                    ORDER BY COUNT(*) DESC, last ASC
                    """;
                    statement = connection.prepareStatement(sql);
                    break;

                // Most popular airlines
                case 4:
                    sql = """
                    SELECT TOP 10 Book.airline, COUNT(*) as bookings FROM Book
                    GROUP BY Book.airline
                    ORDER BY bookings DESC        
                    """;
                    statement = connection.prepareStatement(sql);
                    break;

                // Get number of passengers flying home
                // I may modify this to just print the passengers. Total number is implied by number of rows.
                case 5:
                    sql = """
                    SELECT COUNT(*) as numPassengersFlyingHome FROM Passenger
                    JOIN Book ON Passenger.passNum = Book.passNum
                    JOIN Flights ON Book.flightNum = Flights.flightNum
                    JOIN Airports ON Flights.destination = Airports.icao
                    WHERE CAST(Airports.country AS VARCHAR(10)) = CAST(Passenger.citizen AS VARCHAR(10))        
                    """;
                    statement = connection.prepareStatement(sql);
                    break;

                // Get most serviced plane models
                case 6:
                    sql = """
                    SELECT CAST(Planes.manufacturer AS VARCHAR(200)) as manufacturer, CAST(Planes.model AS VARCHAR(200)) as model, COUNT(*) as numberOfServices FROM Service
                    JOIN Planes ON Service.tailNum = Planes.tailNum
                    WHERE Planes.model IS NOT NULL
                    GROUP BY CAST(Planes.model AS VARCHAR(200)), CAST(Planes.manufacturer AS VARCHAR(200))
                    ORDER BY numberOfServices DESC        
                    """;
                    statement = connection.prepareStatement(sql);
                    break;

                // All flights departing from an airport
                case 7:
                    sql = """
                    SELECT flightNum, origin, destination, Airlines.airlineName
                    FROM Flights f 
                    JOIN Airports a
                    ON f.origin = a.icao 
                    JOIN Planes p 
                    ON f.tailNum = p.tailNum
                    JOIN Airlines 
                    ON Airlines.airlineName = p.airline
                    WHERE origin = ?
                    ORDER BY Airlines.airlineName, f.flightNum
                    ASC
                    """;
                    statement = connection.prepareStatement(sql);
                    tempString = getUserStringInput("Enter airport ICAO code").toUpperCase();
                    statement.setString(1, tempString);
                    break;

                // Get all luggage belonging to a passenger, based on phone number
                case 8:
                    sql = """
                    SELECT Luggage.ID, Luggage.type FROM Passenger
                    JOIN Luggage on Passenger.passNum = Luggage.passNum
                    WHERE CAST(Passenger.phoneNum AS VARCHAR(50)) = ?
                    ORDER BY Luggage.ID ASC
                    """;
                    statement = connection.prepareStatement(sql);
                    tempString = getUserStringInput("Enter passenger phone number (with hyphens)");
                    statement.setString(1, tempString);
                    break;

                // All flights from an airline
                case 9:
                    sql = """
                    SELECT destination, COUNT(*) AS numFlights
                    FROM Flights
                    WHERE origin = ?
                    GROUP BY destination
                    ORDER BY numFlights
                    DESC
                    """;
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, getUserStringInput("Enter airport ICAO code").toUpperCase());
                    break;

                // Get most common destination airport given an origin airport code
                case 10:
                    sql = """
                    SELECT Flights.destination as airportCode, CAST(Airports.airportName AS VARCHAR(200)) as airportName, COUNT(*) as numberOfFlights FROM Flights
                    JOIN Airports ON Flights.destination = Airports.icao
                    WHERE Flights.origin = ?
                    GROUP BY Flights.destination, CAST(Airports.airportName AS VARCHAR(200))
                    ORDER BY numberOfFlights DESC
                    """;
                    statement = connection.prepareStatement(sql);
                    tempString = getUserStringInput("Enter airport ICAO code").toUpperCase();
                    statement.setString(1, tempString);
                    break;

                // Average age of aircraft in airline fleet
                case 11:
                    sql = """
                    SELECT Airlines.airlineName, FORMAT(AVG(2025 - 1.0 * Planes.year), 'N2') as averageAge FROM Planes 
                    JOIN Airlines ON Planes.airline = Airlines.airlineName
                    GROUP BY Airlines.airlineName
                    HAVING AVG(2025 - Planes.year) IS NOT NULL
                    ORDER BY AVG(2025 - Planes.year) DESC
                    """;
                    statement = connection.prepareStatement(sql);
                    break;
                
                // Average number of bags per passenger on a flight
                case 12:
                    sql = """
                    SELECT AVG(numBags) as avgBagsPerPassenger FROM 
                    (SELECT Passenger.passNum, COUNT(*) as numBags FROM Luggage
                    JOIN Passenger ON Luggage.passNum = Passenger.passNum
                    JOIN Book ON Passenger.passNum = Book.passNum
                    WHERE Book.flightNum = ?
                    GROUP BY Passenger.passNum
                    ) temp
                    HAVING AVG(numBags) IS NOT NULL
                    """;
                    statement = connection.prepareStatement(sql);
                    tempString = getUserStringInput("Enter flight number").toUpperCase();
                    statement.setString(1, tempString);
                    break;

                // Employee's completed jobs 
                case 13:
                    sql = """
                    WITH flyJobs AS 

                    ((SELECT 'Service' AS jobType, Service.SIN, Service.tailNum AS tailOrFlightNumber FROM Service)
                    UNION ALL
                    (SELECT 'Guide' AS jobType, Guide.SIN, Guide.tailNum AS tailOrFlightNumber FROM Guide)
                    UNION ALL 
                    (SELECT 'Fly' AS jobType, Fly.SIN, Fly.flightNum AS tailOrFlightNumber FROM Fly))

                    SELECT jobType, tailOrFlightNumber FROM flyJobs WHERE flyJobs.SIN = ?
                    """;
                    statement = connection.prepareStatement(sql);
                    tempInt = getUserIntInput("Enter employee SIN");
                    statement.setInt(1, tempInt);

                    break;

                // Raw table information
                case 16:

                    clearTerminal();

                    // This is a special case with some suboptions
                    System.out.println("\tSelect a table to dump.\n");
                    System.out.println("\t[ 1] Airlines");
                    System.out.println("\t[ 2] Airports");
                    System.out.println("\t[ 3] Attend");
                    System.out.println("\t[ 4] Book");
                    System.out.println("\t[ 5] CreditCards");
                    System.out.println("\t[ 6] Employee");
                    System.out.println("\t[ 7] Flights");
                    System.out.println("\t[ 8] Fly");
                    System.out.println("\t[ 9] Guide");
                    System.out.println("\t[10] Luggage");
                    System.out.println("\t[11] Passenger");
                    System.out.println("\t[12] Planes");
                    System.out.println("\t[13] Runways");
                    System.out.println("\t[14] Service\n");

                    boolean validInput = false;
                    String userInput = "";
                    int userTableSelection = 0;

                    while (!validInput) {
                        System.out.print("\tEnter selection >>> ");
                        userInput = sc.nextLine().trim();
                        
                        try {
                            userTableSelection = Integer.parseInt(userInput);
                            if (userTableSelection > 0 && userTableSelection <= 14) {
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
                    String[] tableMap = {"", "Airlines", "Airports", "Attend", "Book", "CreditCards", "Employee", "Flights", "Fly", "Guide", "Luggage", "Passenger", "Planes", "Runways", "Service"};
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
            System.out.println("\tSQL Query successful. Retrieved " + noColumns + " columns.\n");

            // Print the QueryResults nicely.
            int currentRow = 1;

            while (true) {

                clearTerminal();
                queryResults.printFields(currentRow, 15);
                System.out.println("\t[B] to scroll up a row, [N] to scroll down a row, [M] to return to menu, [Q] to quit\n");

                System.out.print("\t>>> ");
                String userInput = sc.nextLine().trim().toLowerCase();

                if (userInput.equals("b")) {
                    currentRow -= 15;
                    if (currentRow < 1) currentRow = 1;
                } else if (userInput.equals("n")) {
                    currentRow += 15;

                    // Maximum allowed starting row to still show a (possibly partial) page
                    int maxFirstRow = Math.max(1, queryResults.noRows - 15 + 1);
                    if (currentRow > maxFirstRow) currentRow = maxFirstRow;

                } else if (userInput.equals("m")) {
                    this.state = ProgramState.MAIN_MENU;
                    return;
                } else if (userInput.equals("q")) {
                    this.state = ProgramState.QUIT;
                    return;
                } else {
                    System.out.println("\tPlease enter a valid input!");
                    // ANSI spaghetti. This sends the cursor up 2 lines so that we don't run off the screen if the user spams bad inputs    
                    System.out.print("\033[1A\033[1A\033[2K\r");
                }

            }

        } catch (SQLException e) {
            // Something went wrong. Print error and panic.
            System.out.println("\nError: something went wrong attempting to execute the SQL query.");
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    // Quick function that cleanly shuts things down.
    public void shutdown() {
        sc.close();

        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error: unable to close DB connection during shutdown.");
            System.out.println(e.getMessage());
        }

        return;
    }

    // Get additional argument for a query
    private String getUserStringInput(String text) {

        boolean validInput = false;
        String userInput = "";

        while (!validInput) {
            System.out.print("\t" + text + " >>> ");
            userInput = sc.nextLine().trim();
            
            if (userInput.length() > 0) {
                validInput = true;
            } else {
                System.out.println("\tPlease enter a valid string!");
                System.out.print("\033[1A\033[1A\033[2K\r");
            }

        }

        return userInput;
        
    }

    private int getUserIntInput(String text) {
        
        boolean validInput = false;
        String userInput = "";
        int result = -1;

        while (!validInput) {
            System.out.print("\t" + text + " >>> ");
            userInput = sc.nextLine().trim();

            try {
                result = Integer.parseInt(userInput);
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("\tPlease enter a valid int!");
                System.out.print("\033[1A\033[1A\033[2K\r");
            }   
        }

        return result;

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
    // It also "injects" a row # for the row in the query table, just for pretty printing
    public QueryResults(ResultSet resultSet) throws SQLException {

        // Get metadata about the query results
        ResultSetMetaData metadata = resultSet.getMetaData();
        noColumns = metadata.getColumnCount();
        noRows = 0; // Updated later

        // Add the row column
        columnNames.add("rowNum");
        columnMaxFieldLengths.add(0);

        // Get the names of all columns
        for (int i = 1; i <= noColumns; i++) {
            columnNames.add(metadata.getColumnName(i));
            columnMaxFieldLengths.add(0);
        }

        while (resultSet.next()) {
            
            // Populate this row. Also update columnFieldMaxLength if necessary.
            ArrayList<String> thisRow = new ArrayList<String>();

            // Add the row number.
            thisRow.add(Integer.toString(noRows + 1));
            
            // Skip index 1 because that's the "rowNum" column we're injecting
            for (int i = 1; i <= noColumns; i++) {
                String cName = columnNames.get(i);

                // Get the current field. Add it to this row. Set it to a string if it's null.
                String thisField = resultSet.getString(cName);
                if (thisField == null) thisField = "NULL";
                thisRow.add(thisField);
                
                // Check if the current field's length is larger than the previous maximum. If so, update the maximum.
                int thisFieldLength = thisField.length();
                int currentMaxLength = columnMaxFieldLengths.get(i);

                if (thisFieldLength > currentMaxLength) {
                    columnMaxFieldLengths.set(i, thisFieldLength);
                }
            }

            // Add this row to the list of rows.
            rows.add(thisRow);
            noRows++;

        }

        // It could be that maxFieldLength needs to be bigger to accomodate the name of the column, if it's longer.
        // Skip first column since that's our rowNum column
        for (int i = 1; i < this.columnMaxFieldLengths.size(); i++) {
            String columnName = this.columnNames.get(i);
            int currentMaxWidth = this.columnMaxFieldLengths.get(i);
            int newMaxWidth = Math.max(columnName.length(), currentMaxWidth);
            this.columnMaxFieldLengths.set(i, newMaxWidth);
        }

        // Manually do the rowNum column
        this.columnMaxFieldLengths.set(0, Math.max(this.columnNames.get(0).length(), Integer.toString(noRows).length()));

        return;
    }

    public void printFields(int firstRow, int numRowsToPrint) {

        final String BOX_TOP_LEFT = "┌";
        final String BOX_TOP_RIGHT = "┐";
        final String BOX_BOTTOM_LEFT = "└";
        final String BOX_BOTTOM_RIGHT = "┘";
        final String BOX_HORIZONTAL_LINE = "─";
        final String BOX_VERTICAL_LINE = "│";
        final String BOX_VERTICAL_RIGHT_BAR = "├"; 
        final String BOX_VERTICAL_LEFT_BAR = "┤";

        // Soft preconditions checks
        // Check if we are trying to access something before the beginning of the row
        if (firstRow < 1) {
            //System.out.println("WARNING: printFields received firstRow less than 1 (" + firstRow + "). Setting to 1.");
            firstRow = 1;
        }
        // Check if number of rows requested exceeds the number of rows available
        if (firstRow + numRowsToPrint - 1 > noRows) {
            //System.out.println("WARNING: printFields received numRows exceeding total rows in query (" + firstRow + " + " + numRowsToPrint + " = " + (firstRow + numRowsToPrint) + "). Setting to " + (noRows % 15 - 1));
            numRowsToPrint = noRows - firstRow + 1;

        }

        // Calculate the width of the table in characters
        int tableWidth = 0;
        for (Integer i: this.columnMaxFieldLengths) {
            tableWidth += i;
        }
        tableWidth += columnNames.size() + 1; // Account for dividers and outside borders
        
        // Calculate height of the table in characters
        int tableHeight = numRowsToPrint + 4;

        // Print the top of boundary of the table
        System.out.print("\t" + BOX_TOP_LEFT);
        for (int i = 0; i < tableWidth - 2; i++) {
            System.out.print(BOX_HORIZONTAL_LINE);
        }
        System.out.println(BOX_TOP_RIGHT);

        // Print the table headers (column names)
        System.out.print("\t" + BOX_VERTICAL_LINE);
        for (int i = 0; i < columnNames.size(); i++) {
            System.out.print(this.columnNames.get(i));
            for (int j = this.columnNames.get(i).length(); j < this.columnMaxFieldLengths.get(i); j++) {
                System.out.print(" ");
            }
            System.out.print(BOX_VERTICAL_LINE);
        }
        System.out.println();
        
        // Print the header divider bar
        System.out.print("\t" + BOX_VERTICAL_RIGHT_BAR);
        for (int i = 0; i < tableWidth - 2; i++) {
            System.out.print(BOX_HORIZONTAL_LINE);
        }
        System.out.println(BOX_VERTICAL_LEFT_BAR);

        // Print the rows
        for (int i = firstRow; i < firstRow + numRowsToPrint; i++) {
            System.out.print("\t" + BOX_VERTICAL_LINE);
            for (int j = 0; j < this.columnNames.size(); j++) {
                int thisColumnMaxLength = this.columnMaxFieldLengths.get(j);
                String thisField = this.rows.get(i - 1).get(j);
                System.out.print(thisField);
                for (int k = thisField.length(); k < thisColumnMaxLength; k++) {
                    System.out.print(" ");
                }
                System.out.print(BOX_VERTICAL_LINE);
            }
            System.out.println();
        }

        // Print the bottom boundary of the table
        System.out.print("\t" + BOX_BOTTOM_LEFT);
        for (int i = 0; i < tableWidth - 2; i++) {
            System.out.print(BOX_HORIZONTAL_LINE);
        }
        System.out.println(BOX_BOTTOM_RIGHT);

        // Done!
        System.out.println();

        return;

    }

}