package uk.ac.ed.inf;

import uk.ac.ed.inf.clients.DatabaseClient;
import uk.ac.ed.inf.entities.Order;

import java.sql.Date;
import java.util.HashMap;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String dateString = year + '-' + month + '-' + day;
        Date date = Date.valueOf(dateString);

        String webServerPort = args[3];
        String databaseServerPort = args[4];
        String machineName = "localhost";

//        MenuWebsiteClient menuWebsiteClient = new MenuWebsiteClient(machineName, webServerPort);
        DatabaseClient databaseClient = new DatabaseClient(machineName, databaseServerPort);
        HashMap<String, Order> orders = databaseClient.getOrdersByDate(date);

        for (Order order : orders.values()) {
            System.out.println(order);
        }
    }
}
