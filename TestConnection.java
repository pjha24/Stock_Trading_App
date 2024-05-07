import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.sql.DriverManager;
import java.sql.Date;  
import java.util.Calendar;
import java.util.Vector;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;
import java.util.Scanner;
//import java.util.Date;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class TestConnection {
    final static String DB_URL = "jdbc:oracle:thin:@pjjc_tp?TNS_ADMIN=/fs/student/paritoshjha/cs174a/Wallet_PJJC";
    final static String DB_USER = "ADMIN";
    final static String DB_PASSWORD = "Database123#";
    private static Connection connection;
    private static String logged_in_username;
    private static int logged_in_CID;
    private static java.sql.Date dayTime;
    private static boolean interestFirst;
    private static float interestRate;

    public static void setDate() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("You are changing the date of the system. You will now enter the date you would like to change to");
        System.out.print("Enter the month(2 digits):");
        int month = scanner.nextInt();
        System.out.print("Enter the day(2 digits):");
        int day = scanner.nextInt();
        System.out.print("Enter the year(4 digits):");
        int year = scanner.nextInt();
        LocalDate desiredDate = LocalDate.of(year, month, day); // Set the desired date
        dayTime = Date.valueOf(desiredDate); // Assign the desired date to dayTime
        System.out.println("new date: " + dayTime);

        Statement statement1 = connection.createStatement();
        ResultSet resultSet1 = statement1.executeQuery("Delete from DAI");
        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("Insert into dai values(TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD'))");
        
    }
    public static void getDate() throws SQLException{
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM dai");
        resultSet.next();
        dayTime = resultSet.getDate("dateTime");
        System.out.println(dayTime); 
    }
    
    public static void nextDay() throws SQLException{
        Statement statement6= connection.createStatement();
        ResultSet resultSet6= statement6.executeQuery("SELECT CID FROM customers");
        while(resultSet6.next()){
            int customerCID = resultSet6.getInt("CID");
              //grab current balance 
            Statement statement5 = connection.createStatement();
            ResultSet resultSet5= statement5.executeQuery("SELECT balance FROM marketAcct WHERE CID = " + customerCID);
            resultSet5.next();
            int balance = resultSet5.getInt("balance");
            //log the daily closing balance into daily balance table 
            Statement statement3 = connection.createStatement();
            ResultSet resultSet3 = statement3.executeQuery("Insert into dailyBalance values(" + customerCID+ ", " + balance + ",TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD'))");
        }
        
        int lastDayofMonth = getLastDayOfMonthUsingCalendar(dayTime);
        int day = dayTime.getDate();
        if (day == lastDayofMonth){
            accrueInterest(interestRate);
            System.out.println("We are about to move to the next month. Would you like to generate the monthly report OR generate a DTER report? You will be unable to do so tomorrow.");
            System.out.println("Enter 1 to generate monthly reports or DTER, Enter 2 to move onto the next month");
            String action;
            Scanner scanner = new Scanner(System.in);
            action = scanner.nextLine();
            if (action.equals("1")){
                while (true){
                    System.out.println("Press 1 to generate a monthly report or 2 to generate DTER. Press 3 to move onto the next month");
                    String action2 = scanner.nextLine();
                    if (action2.equals("1")){
                        generateMonthlyStatement();
                    }
                    else if (action2.equals("2")){
                        DTER();
                    }
                    else if (action2.equals("3")){
                        break;
                    }
                    
                }    
            }
        }       
        //move to next day and update database with next day
        dayTime = Date.valueOf(dayTime.toLocalDate().plusDays(1));
        System.out.println(dayTime);
        Statement statement1 = connection.createStatement();
        ResultSet resultSet1 = statement1.executeQuery("Delete from DAI");
        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("Insert into dai values(TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD'))");
    }
    public static void fastForward() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of days you would like to fast forward: ");
        int num = scanner.nextInt();

        for(int i = 0; i < num; i++){
            nextDay();
        }

    }
    
   

    public static boolean correctPassword(String username, String password){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM customers WHERE uname =\'"+username+ "\' AND password = \'" + password+ "\'" );

            int num = 0;
            while (resultSet.next()){
                num = resultSet.getInt(1);
            }
            if (num == 1) {
                //System.out.println("Login successfull");
                resultSet.close();
                statement.close();
                return true;
            } else{
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    public static boolean usernameValid(String username){
           
           try {
            // Create the SQL statement
            Statement statement = connection.createStatement();

            // Execute the query
            ResultSet resultSet = statement.executeQuery("SELECT * FROM customers WHERE uname = \'" + username + "\'");

            // Check if the value exists in the column
            if (resultSet.next()) {
                // returns false if the username does already exist in the DB
                //System.out.println("The value exists in the column.");
                return false;
            } else {
                //returns true if the username parameter does not exist in the DB already
                //System.out.println("The value does not exist in the column.");
                resultSet.close();
                statement.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static void register() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your state (2 letter code): ");
        String state = scanner.nextLine();
        while (state.length() != 2) {                                   //2 letters validation
            System.out.print("State must be 2 characters only. Please enter again: ");
            state = scanner.nextLine();
        }

        System.out.print("Enter your phone number (10 digits): ");
        String phoneNum = scanner.nextLine();
        while (phoneNum.length() != 10) {                                   //10 length validation
            System.out.print("Phone number must be exactly 10 digits long. Please enter again: ");
            phoneNum = scanner.nextLine();
        }

        System.out.print("Enter your email address: ");
        String email = scanner.nextLine();

        System.out.print("Enter your taxID: ");
        String taxID = scanner.nextLine();
        while (taxID.length() != 9) {                                   //9 length validation
            System.out.print("Tax ID must be exactly 9 digits long. Please enter again: ");
            taxID = scanner.nextLine();
        }

        System.out.print("Enter your username: ");          //needs to be unique in the database
        String username = scanner.nextLine();
        while(!usernameValid(username)){
            System.out.print("Unfortunately this username is taken. Please enter again: ");
            username = scanner.nextLine();
        }

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        
        insertCustomer(name,state,phoneNum,email,taxID,username,password);      //inserts customer into DB else reports error. 
       // scanner.close();

        //grabbing login information
        logged_in_username = username;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT CID FROM customers WHERE uname = \'" + username + "\'");
        resultSet.next();
        logged_in_CID = resultSet.getInt("CID");
        System.out.println("logged in after registering");




    }
    public static void connect() throws SQLException{
        try{
            connection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
            System.out.println("connected");
        }catch (Exception e){
            System.out.println("CONNECTION ERROR:");
            System.out.println(e);
        }

    }
    public static void login() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();
        System.out.println("Enter password: ");
        String password = scanner.nextLine();
        while(!correctPassword(username,password)){
            System.out.println("Login information incorrect. Please Try again");
            System.out.println("Username: ");
            username = scanner.nextLine();
            System.out.println("Password: ");
            password = scanner.nextLine();
        }
        System.out.println("Login Successful");
        
        //set login credentials for user session
        logged_in_username = username;

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT CID FROM customers WHERE uname = \'" + username + "\'");
        resultSet.next();
        logged_in_CID = resultSet.getInt("CID");
        System.out.println("logged in username: " + logged_in_username);
        System.out.println("logged in CID: " + logged_in_CID);


    }
    public static void showMarketAccountBalance() throws SQLException{
        // String query = "SELECT balance FROM marketAcct WHERE CID = " + logged_in_CID;
        // System.out.println(query);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT balance FROM marketAcct WHERE CID = " + logged_in_CID);
        resultSet.next();
        int balance = resultSet.getInt("balance");
        System.out.println("Balance: " + balance);

    }
    public static void depositMarketAccount() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the amount you would like to deposit: ");
        String amount = scanner.nextLine();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("UPDATE marketAcct SET balance = balance +"  + amount + " WHERE CID = " + logged_in_CID);
        showMarketAccountBalance();

        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("INSERT INTO marketTransactions values(marketTransactions_mti_seq.nextVal," + logged_in_CID + "," + "\'deposit\'," + amount + "," + "TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD'))");

    }
    public static void withdrawMarketAccount() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the amount you would like to withdraw: ");
        int amount = scanner.nextInt();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT balance FROM marketAcct WHERE CID = " + logged_in_CID);
        resultSet.next();
        int balance = resultSet.getInt("balance");
        if (balance >= amount) {
            Statement statement2 = connection.createStatement();
            ResultSet resultSet2 = statement2.executeQuery("UPDATE marketAcct SET balance = balance -"  + amount + " WHERE CID = " + logged_in_CID);

            Statement statement3 = connection.createStatement();
            ResultSet resultSet3 = statement3.executeQuery("INSERT INTO marketTransactions values(marketTransactions_mti_seq.nextVal," + logged_in_CID + "," + "\'withdraw\'," + amount + "," + "TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD'))");
        }
        else {
            System.out.println("Inefficient funds: ");
        }
        showMarketAccountBalance();

    }
    public static void showStockAccount() throws SQLException{
        //tells you how many shares you have of that stock in total and the current price of the stock
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter the stock symbol of which account you would like to view: ");
        String symbol = scanner.nextLine();

        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("Select SUM(quantity) as total_quantity from stockAcct where cid = " + logged_in_CID+ " AND symbol = \'" + symbol + "\'");
        resultSet2.next();
        int total_quantity = resultSet2.getInt("total_quantity");

        System.out.println("You currently have " + total_quantity + " shares of " + symbol);
        
    }
    
    public static void buyStock() throws SQLException{
        
        boolean validBuy = false;
        while (!validBuy){  
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the symbol of the stock you would like to buy: ");
            String symbol = scanner.nextLine();
            System.out.println("Enter the quantity of the stock you would like to buy: ");
            int quantity = scanner.nextInt();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT balance FROM marketAcct WHERE CID = " + logged_in_CID);
            resultSet.next();
            int balance = resultSet.getInt("balance");

            Statement statement2 = connection.createStatement();
            ResultSet resultSet2 = statement2.executeQuery("SELECT currPrice FROM stock WHERE SYMBOL = " + "\'" + symbol + "\'");
            resultSet2.next();
            int currPrice = resultSet2.getInt("currPrice");
            int amount = (currPrice * quantity);

            try {
                
                Statement statement8 = connection.createStatement();

                    // Execute the query
                ResultSet resultSet8 = statement8.executeQuery("SELECT * FROM stockAcct WHERE symbol = \'" + symbol + "\' AND CID = " + logged_in_CID + " AND PRICE = " + currPrice);

                // Check if the value exists in the column
                if (resultSet8.next()) {
                    System.out.println("You already have an account with that stock at the same price. Unable to confirm purchase. Enter you information again");
                    validBuy = false;
                } else {
                    //returns true if the username parameter does not exist in the DB already
                    //System.out.println("The value does not exist in the column.");
                    System.out.println("Valid purchase. Moving forward ...");
                    resultSet8.close();
                    statement8.close();
                    validBuy = true;
                    if (balance >= amount) {
                        amount = amount + 20; //commission
                        Statement statement3 = connection.createStatement();
                        ResultSet resultSet3 = statement3.executeQuery("UPDATE marketAcct SET balance = balance -"  + amount + " WHERE CID = " + logged_in_CID);

                        Statement statement4 = connection.createStatement();
                        ResultSet resultSet4 = statement4.executeQuery("INSERT INTO stockAcct values(" + logged_in_CID + "," + "\'" + symbol + "\'," + quantity + ","  + currPrice + ")");

                        Statement statement5 = connection.createStatement();
                        ResultSet resultSet5 = statement5.executeQuery("INSERT INTO stockTransactions values(stockTransactions_sti_seq.nextVal, "+ logged_in_CID + "," + "\'buy\'," + quantity + "," + "\'" + symbol + "\'," + currPrice + "," + "TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD'))");
                    }
                    else {
                        System.out.println("Inefficient funds: ");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }

    }

    public static void sellStock() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the symbol of the stock you would like to sell: ");
        String symbol = scanner.nextLine();
        System.out.println("How many shares do you want to sell: ");
        int quantity = scanner.nextInt();
        System.out.println("Enter the price that you bought the stock at: ");
        int price = scanner.nextInt();
        
        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("SELECT quantity FROM stockAcct WHERE SYMBOL = " + "\'" + symbol + "\'" + " AND price = " + price + " AND CID = " + logged_in_CID);
        resultSet2.next();
        int quantityOwned = resultSet2.getInt("quantity");

        Statement statement3 = connection.createStatement();
        ResultSet resultSet3 = statement3.executeQuery("SELECT currPrice FROM stock WHERE SYMBOL = " + "\'" + symbol + "\'");
        resultSet3.next();
        int currPrice = resultSet3.getInt("currPrice");
        int amount = (currPrice * quantity);

        if (quantityOwned >= quantity) {
            amount = amount - 20; //comission
            Statement statement4 = connection.createStatement();
            ResultSet resultSet4 = statement4.executeQuery("UPDATE marketAcct SET balance = balance +"  + amount + " WHERE CID = " + logged_in_CID);

            Statement statement5 = connection.createStatement();
            ResultSet resultSet5 = statement5.executeQuery("UPDATE stockAcct SET quantity = quantity -" + quantity + "WHERE SYMBOL = " + "\'" + symbol + "\'" + "AND price = " + price + "AND CID = " + logged_in_CID);

            Statement statement6 = connection.createStatement();
            ResultSet resultSet6 = statement6.executeQuery("INSERT INTO stockTransactions values(stockTransactions_sti_seq.nextVal," + logged_in_CID + "," + "\'sell\'," + quantity + "," + "\'" + symbol + "\'," + currPrice + "," + "TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD'))");
            System.out.println("Shares sold");
        }
        else {
            System.out.println("You don't have enough of that stock to sell it");
        }

       

    }
    public static void changeStockPrice() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the symbol of the stock you would like to change the price of: ");
        String symbol = scanner.nextLine();

        System.out.println("Enter the new price of the stock: ");
        int price = scanner.nextInt();

        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("UPDATE stock set currPrice = " + price + " where SYMBOL = " + "\'" + symbol + "\'");
        System.out.println("Stock price updated");


    }
    public static void listPriceOfStock() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the symbol of the stock you would like to check the price of: ");
        String symbol = scanner.nextLine();

        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("Select currPrice from stock where SYMBOL = " + "\'" + symbol + "\'");
        resultSet2.next();
        int price = resultSet2.getInt("currPrice");
        System.out.println("The current stock price per share for " + symbol + " is " + price);

    }

    public static void cancelTrade() throws SQLException{
        // check if the same day
        // look at transactions table
        // refund the money
        // update stock 
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the action of the stock Transaction you would like to cancel: ");
        String action = scanner.nextLine();
        System.out.println("Enter the symbol of the stock Transaction you would like to cancel: ");
        String symbol = scanner.nextLine();
        System.out.println("Enter the quantity of the stock Transaction you would like to cancel: ");
        int quantity = scanner.nextInt();
        System.out.println("Enter the price of the stock Transaction you would like to cancel: ");
        int price = scanner.nextInt();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT dateTime, action, quantity, pricePurchased FROM stockTransactions WHERE cid =" + logged_in_CID + " AND symbol =\'" + symbol + "\' AND action =\'" + action + "\' AND quantity =" + quantity + " AND pricePurchased =" + price + " AND dateTime = TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')");
        //SELECT dateTime, action, quantity, price FROM stockTransactions WHERE cid = logged_in_CID AND symbol = symbol AND action = action AND quantity = quantity AND price = price AND dateTime = dayTime
        int rowCount = 0;
        int amount = quantity * price;
        while (resultSet.next()) {
            rowCount++;
        }
        //resultSet.beforeFirst();
        if (rowCount == 1){
            if (action.equals("sell")) {
                amount = amount - 20;

            Statement statement2 = connection.createStatement();
            ResultSet resultSet2 = statement2.executeQuery("UPDATE stockAcct SET quantity = quantity +" + quantity + "WHERE SYMBOL = " + "\'" + symbol + "\'" + "AND price = " + price + "AND CID = " + logged_in_CID);

            Statement statement3 = connection.createStatement();
            ResultSet resultSet3 = statement3.executeQuery("UPDATE marketAcct SET balance = balance -"  + amount + " WHERE CID = " + logged_in_CID);

            Statement statement4 = connection.createStatement();
            ResultSet resultSet4 = statement4.executeQuery("DELETE FROM stockTransactions WHERE cid =" + logged_in_CID + " AND symbol =\'" + symbol + "\' AND action =\'" + action + "\' AND quantity =" + quantity + " AND pricePurchased =" + price + " AND dateTime = TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')");
            }

            else if (action.equals("buy")) {
                amount = amount - 20;
            Statement statement5 = connection.createStatement();
            ResultSet resultSet5 = statement5.executeQuery("UPDATE stockAcct SET quantity = quantity -" + quantity + "WHERE SYMBOL = " + "\'" + symbol + "\'" + "AND price = " + price + "AND CID = " + logged_in_CID);

            Statement statement6 = connection.createStatement();
            ResultSet resultSet6 = statement6.executeQuery("UPDATE marketAcct SET balance = balance +"  + amount + " WHERE CID = " + logged_in_CID);

            Statement statement7 = connection.createStatement();
            ResultSet resultSet7 = statement7.executeQuery("DELETE FROM stockTransactions WHERE cid =" + logged_in_CID + " AND symbol =\'" + symbol + "\' AND action =\'" + action + "\' AND quantity =" + quantity + " AND pricePurchased =" + price + " AND dateTime = TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')");
            }
        }
        else {
            System.out.println("Unable to cancel transaction");
        }
        // When we buy and sell stocks we are intentionally not adding a row to the marketTransactions table.
        // Only add a marketTransactions row if its a transaction (deposit/withdraw) directly from the user.
    }
    public static float profit(int CID) throws SQLException{

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT SUM(quantity * pricePurchased) as buyAmount FROM stockTransactions WHERE CID = " + CID + " AND action = " + "\'buy\'" + " AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime)");
        resultSet.next();
        int buyAmount = resultSet.getInt("buyAmount");

        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("SELECT SUM(quantity * pricePurchased) as sellAmount FROM stockTransactions WHERE CID = " + CID + " AND action = " + "\'sell\'" + "AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime)");
        resultSet2.next();
        int sellAmount = resultSet2.getInt("sellAmount");

        Statement statement3 = connection.createStatement();
        ResultSet resultSet3 = statement3.executeQuery("SELECT SUM(balance) as totalBalance, COUNT(*) as numDays FROM dailyBalance WHERE CID =" + CID + " AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime)");
        // SELECT SUM(balance) as totalBalance FROM dailyBalance WHERE CID = logged_in_CID AND MONTH(dayTime) = MONTH(dateTime)
        resultSet3.next();
        int totalBalance = resultSet3.getInt("totalBalance");
        int numDays = resultSet3.getInt("numDays");
        // System.out.println("numDays: " + numDays);
        // System.out.println("The current totalBalance is:" + totalBalance);
        float addInterest = ((float)totalBalance / numDays) * interestRate;
        

        float profit = sellAmount - buyAmount + addInterest;
        System.out.println("The current profit for CID: " + CID + " is " + profit);
        return profit;
    }
    
    public static int getMonth(java.sql.Date daay ){
        Calendar cal = Calendar.getInstance();
        cal.setTime(daay);
        int month = cal.get(Calendar.MONTH);
        // System.out.println(month);
        return month;
    }
    public static int getLastDayOfMonthUsingCalendar(java.sql.Date daay){
        int month = getMonth(daay);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH); 
    }
    public static void accrueInterestCheck() throws SQLException{
        int lastDayofMonth = getLastDayOfMonthUsingCalendar(dayTime);
        int day = dayTime.getDate();
        if (day != lastDayofMonth){
            System.out.println("It is not the last day of the month, so cannot accrue Interest");
        }
        else{
            System.out.println("Accruing interestin and moving to the next day");
            nextDay();
        }

    }

    public static void accrueInterest(float interest) throws SQLException{
        //when called, adds appropriate interest amount to market balance up to the called date
        //for example, if called on 1/18/2001, you go to your daily balance table, add up all the balances from 1/1 - 1/18 then divide by 18 then * with interest, then add that value to MA
        //so you can call this method multiple times in a day, month, year, etc..
        interestRate = interest;
        Statement statement6= connection.createStatement();
        ResultSet resultSet6= statement6.executeQuery("SELECT CID FROM customers");
        while(resultSet6.next()){
            int customerCID = resultSet6.getInt("CID");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT SUM(balance) as totalBalance, COUNT(*) as numDays FROM dailyBalance WHERE CID =" + customerCID + " AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime)");
            // SELECT SUM(balance) as totalBalance FROM dailyBalance WHERE CID = logged_in_CID AND MONTH(dayTime) = MONTH(dateTime)
            resultSet.next();
            int totalBalance = resultSet.getInt("totalBalance");
            int numDays = resultSet.getInt("numDays");
            System.out.println("numDays: " + numDays);
            // System.out.println("The current totalBalance is:" + totalBalance);
            float addInterest = ((float)totalBalance / numDays) * interest;
            Statement statement2 = connection.createStatement();
            ResultSet resultSet2 = statement2.executeQuery("UPDATE marketAcct SET balance = balance +"  + addInterest + " WHERE CID = " + customerCID);
            // System.out.println("Interest added is: " + addInterest);

        }

        
    }
    public static void showMarketTransactions(int customer) throws SQLException{

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT action, amount, dateTime FROM marketTransactions WHERE CID =" + customer + " AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime)");
       System.out.println("The following are your market transactions from the lastest month: ");
       while (resultSet.next()){
        String action = resultSet.getString("action");
        int amount = resultSet.getInt("amount");
        java.sql.Date timing = resultSet.getDate("dateTime");
        System.out.println(timing +" " + action + " $" + amount); 
        }


    }
    public static void showStockTransactions(int customer) throws SQLException{
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT action, quantity, symbol, pricePurchased,dateTime FROM stockTransactions WHERE CID =" + customer + " AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime)");
        System.out.println("The following are your stock transactions for the last month");
        System.out.println("Action\tQuant\tsymbol\tprice\tDate");
       while (resultSet.next()){
        String action = resultSet.getString("action");
        int quantity = resultSet.getInt("quantity");
        String symbol = resultSet.getString("symbol");
        int price = resultSet.getInt("pricePurchased");
        java.sql.Date timing = resultSet.getDate("dateTime");
        System.out.println(action + " \t " + quantity + " \t " + symbol + " \t $" + price + " \t " + timing); 
        }

    }
    public static void monthlyStatementCheck() throws SQLException{
        int lastDayofMonth = getLastDayOfMonthUsingCalendar(dayTime);
        int day = dayTime.getDate();
        if (day != lastDayofMonth){
            System.out.println("Can only generate monthly statement on last day of month.");
            return;
        }
        else{
            System.out.println("It is the last day of the month, but you must accrue interest for the month before generating the monthly report. Would you like to accrue interest now?");
            System.out.println("Enter 1 for yes and 2 for no");
            String action;
            Scanner scanner = new Scanner(System.in);
            action = scanner.nextLine();
            if (action.equals("1")){
                accrueInterestCheck();
            }
            else{
                return;
            }


        }
    }
    
    public static void generateMonthlyStatement() throws SQLException{
        
        Scanner scanner = new Scanner(System.in);
        int customer;
        System.out.println("Please enter the CID of the customer you would like to generate a monthly statement for: ");
        customer = scanner.nextInt();
        System.out.println("Generating monthly Statement....");
        System.out.println();
        //show market account transactions
        showMarketTransactions(customer);
        System.out.println();

        //show stock account transactions
        showStockTransactions(customer);
        System.out.println();
      
        //initial balance
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT balance FROM dailyBalance WHERE CID = "+ customer + " AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime) AND EXTRACT(DAY FROM dateTime) = 1");
        resultSet.next();
        int intialBal = resultSet.getInt("balance");
        System.out.println("Your balance on the first of the month was: $" + intialBal);
        
        //final balance
        // int lastDayofMonth = getLastDayOfMonthUsingCalendar(dayTime);
        // Statement statement2 = connection.createStatement();
        // ResultSet resultSet2 = statement2.executeQuery("SELECT balance FROM dailyBalance WHERE CID = "+ logged_in_CID + " AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime) AND EXTRACT(DAY FROM dateTime) = " + lastDayofMonth);
        // resultSet2.next();
        // int finalBal = resultSet2.getInt("balance");
        // System.out.println("Your balance on the last day of the month was: $" + finalBal);
        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("SELECT balance FROM marketAcct WHERE CID = " + customer);
        resultSet2.next();
        int finalBal = resultSet2.getInt("balance");
        //assume you are on the last day of the month when generating r
        System.out.println("Your final balance is: $" + finalBal);
       
        //profit
        profit(customer);
        //System.out.println("Total profit for the month is: " + (finalBal - intialBal));
        System.out.println();
        //total amount of commissions payed

    }

    public static void showMarketHistory() throws SQLException{
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT action, amount, dateTime FROM marketTransactions WHERE CID =" + logged_in_CID );
       System.out.println("The following are your market transactions history ");
       while (resultSet.next()){
        String action = resultSet.getString("action");
        int amount = resultSet.getInt("amount");
        java.sql.Date timing = resultSet.getDate("dateTime");
        System.out.println(timing +" " + action + " $" + amount); 
        }

    }
    public static void showStockHistory() throws SQLException{
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT action, quantity, symbol, pricePurchased,dateTime FROM stockTransactions WHERE CID = " + logged_in_CID);
        System.out.println("The following are your stock transactions history");
        System.out.println("Action\tQuant\tsymbol\tprice\tDate");
       while (resultSet.next()){
        String action = resultSet.getString("action");
        int quantity = resultSet.getInt("quantity");
        String symbol = resultSet.getString("symbol");
        int price = resultSet.getInt("pricePurchased");
        java.sql.Date timing = resultSet.getDate("dateTime");
        System.out.println(action + " \t " + quantity + " \t " + symbol + " \t $" + price + " \t " + timing); 
        }

    }
    public static void managerLogout(){
        System.out.println("Logging out of manager and exiting program");
        System.exit(0);
    }
    public static void listActiveCustomers() throws SQLException{
        Vector<Integer> activeCustomersCID = new Vector<>();
        Statement statement6= connection.createStatement();
        ResultSet resultSet6= statement6.executeQuery("SELECT CID FROM customers");
        while(resultSet6.next()){
            int customerCID = resultSet6.getInt("CID");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT SUM(quantity) as totalShares FROM stockTransactions WHERE CID = " + customerCID + "AND EXTRACT(MONTH FROM TO_DATE(\'"+ dayTime+"\','YYYY-MM-DD')) = EXTRACT(MONTH FROM dateTime)");
            resultSet.next();
            int totalShares = resultSet.getInt("totalShares");
            if (totalShares >= 1000){
                activeCustomersCID.add(customerCID);
            }
        }
        System.out.println("Generating list of Active Customers...");
        System.out.println();
        for(Integer cid: activeCustomersCID){
            Statement statement7= connection.createStatement();
            ResultSet resultSet7= statement7.executeQuery("SELECT uname from customers where CID = " + cid);
            resultSet7.next();
            String customerName = resultSet7.getString("uname");
            System.out.println(customerName);
        }
        System.out.println();
        System.out.println("list completed");
    }

    public static void DTER() throws SQLException{
        Vector<Integer> richCustomers = new Vector<>();
        Statement statement6= connection.createStatement();
        ResultSet resultSet6= statement6.executeQuery("SELECT CID FROM customers");
        while(resultSet6.next()){
            int customerCID = resultSet6.getInt("CID");
            float profit = profit(customerCID);
            if (profit > 10000){
                richCustomers.add(customerCID);
            }
        }
        System.out.println("Generating list of Rich Tax Evading Customers. We will not rest until we get their best. taxes for all taxes or we ball..");
        System.out.println();
        for(Integer cid: richCustomers){
            Statement statement7= connection.createStatement();
            ResultSet resultSet7= statement7.executeQuery("SELECT uname, state, name from customers where CID = " + cid);
            resultSet7.next();
            String uname = resultSet7.getString("uname");
            String state = resultSet7.getString("state");
            String name = resultSet7.getString("name");
            System.out.println("Name: " + name + " UserName: " + uname + " State: " + state);
        }
        System.out.println();
        System.out.println("list completed");

    }

    public static void customerReport() throws SQLException{
        int CID;          //declaring for menu purposes
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the CID of the user you want to generate a report on: ");
        CID = scanner.nextInt();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT CID, SYMBOL, SUM(QUANTITY) AS sumQuantity FROM stockAcct WHERE CID = " + CID + " GROUP BY SYMBOL, CID");
        System.out.println("Generating Customer Report for: " + CID);
        while (resultSet.next()) {
            String symbol = resultSet.getString("SYMBOL");
            int quantity = resultSet.getInt("sumQuantity");
            System.out.println("Stock Symbol: " + symbol + "\tQuantity: " + quantity);
        }

        Statement statement2 = connection.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("SELECT balance FROM marketAcct WHERE CID = " + CID);
        resultSet2.next();
        int balance = resultSet2.getInt("balance");
        System.out.println("Market Balance: " + balance);

        //SELECT CID, SYMBOL, SUM(QUANTITY) FROM stockAcct WHERE CID = CID GROUP BY SYMBOL


    }
    public static void deleteTransaction() throws SQLException{
        //delete market transactions
        Statement statement7= connection.createStatement();
        ResultSet resultSet7= statement7.executeQuery("Delete from marketTransactions");
        //delete stock transactions

        Statement statement= connection.createStatement();
        ResultSet resultSet= statement.executeQuery("Delete from stockTransactions");

        System.out.println("Deletion of stock and market transactions completed");
    }

    public static void actorProfile() throws SQLException {
        String symbol;          //declaring for menu purposes
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the Symbol of the actor you want to generate a report on: ");
        symbol = scanner.nextLine();

        Statement statement= connection.createStatement();
        ResultSet resultSet= statement.executeQuery("SELECT actorName, symbol, dateOfBirth FROM actors WHERE SYMBOL = " + "\'" + symbol + "\'");
        while (resultSet.next()) {
            String actorName = resultSet.getString("actorName");
            String sym = resultSet.getString("symbol");
            java.sql.Date dob = resultSet.getDate("dateOfBirth");
            System.out.println("Actor Name: " + actorName + "\tSymbol: " + sym + "\tDate of Birth: " + dob);
        }

    }

    public static void movieInformation() throws SQLException {
        String movieTitle;          //declaring for menu purposes
        int prodYear;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the Movie Title you want information of: ");
        movieTitle = scanner.nextLine();
        System.out.println("Enter the Year of the movie you want information of: ");
        prodYear = scanner.nextInt();

        Statement statement= connection.createStatement();
        ResultSet resultSet= statement.executeQuery("SELECT title, prodYear, reviews, rating FROM movies WHERE title = " + "\'" + movieTitle + "\'" + " AND prodYear = " + prodYear);
        while (resultSet.next()) {
            String title = resultSet.getString("title");
            int year2 = resultSet.getInt("prodYear");
            String reviews = resultSet.getString("reviews");
            String rating = resultSet.getString("reviews");
            System.out.println("Movie Title: " + title + "\tYear: " + year2 + "\tReviews: " + reviews + "\tRating: " + rating);
        }
    }

    public static void getHighestMovie() throws SQLException {
        int startYear;          //declaring for menu purposes
        int endYear;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the starting year of the Movies with the highest rating: ");
        startYear = scanner.nextInt();
        System.out.println("Enter the end year of the Movies with the highest rating: ");
        endYear = scanner.nextInt();

        Statement statement= connection.createStatement();
        ResultSet resultSet= statement.executeQuery("SELECT title FROM movies WHERE prodYear >=" + startYear + " AND prodYear <=" + endYear + " AND rating = 10.0");
        System.out.println("Movies with the highest rating between " + startYear + " and " + endYear + "are: ");
        while (resultSet.next()) {
            String title = resultSet.getString("title");
            System.out.println(title);
        }
    }
    public static void getMovieReviews() throws SQLException{
        String title; 
        int prodYear;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the movie title: ");
        title = scanner.nextLine();
        System.out.println("Enter the year the movie was produced: ");
        prodYear = scanner.nextInt();

        Statement statement= connection.createStatement();
        ResultSet resultSet= statement.executeQuery("SELECT reviews FROM movies WHERE title = " + "\'" + title + "\'" + " AND prodYear = " + prodYear);
        System.out.println("Outputting reviews...");
        while (resultSet.next()) {
            String review = resultSet.getString("reviews");
            System.out.println(review);
        }
        
    }
    public static void changeInterestRate(){
        Scanner scanner = new Scanner(System.in);
        float action;
        System.out.println("Enter Interest Rate as a decimal (0.1 = 10% interest rate): ");
        action = scanner.nextFloat();
        interestRate = action;
        System.out.println("New Interest Rate: " + (interestRate * 100) + "%");

    }


    public static void managerInterface() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        String action;
        while (true){
            System.out.println("Manager Interface Menu: Select the number of the action you would like to perform");
            System.out.println("1: Add Interest");
            System.out.println("2: Generate Monthly Statement");
            System.out.println("3: List Active Customers");
            System.out.println("4: Generate Government Drug & Tax Evasion Report (DTER)");
            System.out.println("5: Customer Report");
            System.out.println("6: Delete Transactions");
            System.out.println("7: logout");
            System.out.println("8: Sim to next day");
            System.out.println("9: Sim many days");
            System.out.println("10: Change Interest Rate");



            System.out.println("Enter the number of which action you would like to perform: ");
            action = scanner.nextLine();

            if (action.equals("1")){
                accrueInterestCheck();
            }
            else if (action.equals("2")){
                monthlyStatementCheck();
            }
            else if (action.equals("3")){
                listActiveCustomers();
            }
            else if (action.equals("4")){
                DTER();
            }
            else if (action.equals("5")){
                customerReport();
            }
            else if (action.equals("6")){
                deleteTransaction();
            }
            else if (action.equals("7")){
                managerLogout();
            }
            else if (action.equals("8")){
                nextDay();
            }
            else if (action.equals("9")){
                fastForward();
            }
            else if (action.equals("10")){
                changeInterestRate();
            }
        }
    }
    
   

public static void main(String args[]) throws SQLException{
        connect();
        getDate();
        interestRate = (float)0.1;     //Interest Rate default to 10%
        //Begin Menu
        
        String action;          //declaring for menu purposes
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("Login Menu: Select the number of the action you would like to perform");
            System.out.println("1: \t Register (No existing account)");
            System.out.println("2: \t Login (Have existing account)");
            System.out.println("3: \t Manager Interface");


            
            System.out.println("Enter the number of which action you would like to perform: ");
            action = scanner.nextLine();
            if (action.equals("1")){
                register();
                break;
            }
            else if (action.equals("2")){
                login();
                break;
            }
            else if (action.equals("3")){
                managerInterface();
            }
         }

         while (true){
            System.out.println("Current system: currUname: " + logged_in_username + ", currCID: " + logged_in_CID);
            System.out.println("Menu: Select the number of the action you would like to perform");
            System.out.println("1: Show Market Account Balance");
            System.out.println("2: Deposit into Market Account");
            System.out.println("3: Withdraw from Market Account");
            System.out.println("4: Buy Stock");
            System.out.println("5: Sell Stock");
            System.out.println("6: Cancel Trade");
            System.out.println("7: Show stock transaction history");
            System.out.println("8: List current price of stock and actor profile");
            System.out.println("9: List movie information");
            System.out.println("10: Top movies between the years");
            System.out.println("11: Display all reviews for a given movie");
            System.out.println("12: logout");
            System.out.println("13: Sim to next day");
            System.out.println("14: Sim many days forward");
            System.out.println("15: Change stock price");

            do {
                System.out.println("Enter the number of which action you would like to perform: ");
                action = scanner.nextLine();
            } while (action.isEmpty());

            if (action.equals("1")){
                showMarketAccountBalance();
            }
            else if (action.equals("2")){
                depositMarketAccount();
            }
            else if (action.equals("3")){
                withdrawMarketAccount();
            }
            else if (action.equals("4")){
                buyStock();
            }
            else if (action.equals("5")){
                sellStock();
            }
            else if (action.equals("6")){
                cancelTrade();
            }
            else if (action.equals("7")){
                showStockHistory();
            }
            else if (action.equals("8")){
                listPriceOfStock();
                actorProfile();
            }
            else if (action.equals("9")){
                movieInformation();
            }
            else if (action.equals("12")) {
                break;
            }
            else if (action.equals("10")){
                getHighestMovie();
            }
            else if (action.equals("11")){
                getMovieReviews();
            }
            else if (action.equals("13")){
                nextDay();
            }
            else if (action.equals("14")){
                fastForward();
            }
            else if (action.equals("15")){
                changeStockPrice();
            }
         }
        System.out.println("exited menu- closing program");

        //program done
        connection.close();
        

        
        
        
   
    }

    // Inserts customer into Customer table, then creates the Market Acct
    public static void insertCustomer(String name, String state, String phoneNum,String email,String taxID,String username,String password) throws SQLException { 
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("INSERT INTO CUSTOMERS VALUES(\'"+ name + "\',\'" + state + "\',\'" + phoneNum + "\',\'" + email + "\',\'" + taxID + "\',\'" + username+ "\',\'" + password + "\'," + "customers_cid_seq.NEXTVAL)"))
                {
                    System.out.println("Customer information stored");         //customer entered into DB
                    //create market account
                    Statement statement2 = connection.createStatement();
                    ResultSet resultSet2 = statement.executeQuery("INSERT into marketAcct Values(1000,marketAcct_mid_seq.nextVal,customers_cid_seq.currval)");
                    System.out.println("Market Account created");

                }
        } catch (Exception e) {
            System.out.println("ERROR: insertion failed.");
            System.out.println(e);
        }

    }

    // Displays data from Instructors table.
    public static void printInstructors(Connection connection) throws SQLException {
        // Statement and ResultSet are AutoCloseable and closed automatically. 
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM INSTRUCTORS"
                )
            ) {
                System.out.println("INSTRUCTORS:");
                System.out.println("I_ID\tI_NAME\t\tI_ROLE");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("I_ID") + "\t"
                        + resultSet.getString("I_NAME") + "\t"
                        + resultSet.getString("I_ROLE")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: selection failed.");
            System.out.println(e);
        }
    }
}
