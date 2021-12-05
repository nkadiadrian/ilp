package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;
import uk.ac.ed.inf.clients.DatabaseClient;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

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

        Menus menus = new Menus(machineName, webServerPort);
        DatabaseClient databaseClient = new DatabaseClient(machineName, databaseServerPort);
        HashMap<String, Order> orders = databaseClient.getOrdersByDate(date, menus);

        FeatureCollection noFlyZone = Menus.getWebsiteClient().getNoFlyZone();
        LongLat startPosition = LongLat.APPLETON;
        Optimiser optimiser = new Optimiser(orders, noFlyZone);
        optimiser.useGreedy();
//        optimiser.useSwapHeuristic(-1);
//        optimiser.useTwoOptHeuristic(-1);
        List<Order> destinations = optimiser.getOptimisedOrderList();

        Drone drone = new Drone(startPosition, noFlyZone, destinations);
        drone.visitLocations();
        drone.saveRouteGeoJson(day, month, year);
        databaseClient.writeAllDeliveriesToTable(drone.fulfilledOrders);
        databaseClient.writeAllMovesToTable(drone.route);

        drone.printStatistics();
    }
}
