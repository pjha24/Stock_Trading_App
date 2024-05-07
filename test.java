import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.sql.DriverManager;

import java.sql.DatabaseMetaData;
import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        String action;
        while (true){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Login Menu: Select the number of the action you would like to perform");
            System.out.println("1: \t Register (No existing account)");
            System.out.println("2: \t Login (Have existing account)");

            
            System.out.println("Enter the number of which action you would like to perform: ");
            action = scanner.nextLine();
            // scanner.close();
            if (action.equals("1")){
                //register();
                System.out.println("register");
                break;
            }
            else if (action.equals("2")){
                //login();
                System.out.println("login");
                break;
            }
        }
        String action2;
         while (true){
            // Scanner scanner2 = new Scanner(System.in);
            //System.out.println("Current system: currUname: " + logged_in_username + ", currCID: " + logged_in_CID);
            System.out.println("Menu: Select the number of the action you would like to perform");
            System.out.println("1: Show Market Account Balance");
            System.out.println("2: logout");

            // System.out.println("Enter the number of which action you would like to perform: ");
            Scanner scanner2 = new Scanner(System.in);
            do {
                System.out.println("Enter the number of which action you would like to perform: ");
                action2 = scanner2.nextLine();
                // scanner2.close();
            } while (action2.isEmpty());
            System.out.println("Action2: " + action2);
            // if (scanner2.hasNextLine()){
            //     action2 = scanner2.nextLine();
            //     // System.out.println("worked");
            //     do {

            //     } while(action2.isEmpty());

            // }
            // else{
            //     System.out.println("something messed up");
            //     action2 = "10";
            // }
            // scanner2.close();
            if (action2.equals("1")){
                System.out.println("showMarketAccountBal");
                //showMarketAccountBalance();
            }
            else if (action2.equals("2")){
                break;
            }
            
         }
         System.out.println("exiting program");
       
        }
}