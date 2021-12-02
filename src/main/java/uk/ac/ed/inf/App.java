package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.clients.DatabaseClient;
import uk.ac.ed.inf.entities.Order;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Optimiser optimiser = new Optimiser(orders, noFlyZone);
        optimiser.useGreedy();
        System.out.println(optimiser.visitOrder);
//        optimiser.useSwapHeuristic(-1);
//        optimiser.useTwoOptHeuristic(-1);
        List<Order> destinations = optimiser.getOptimisedOrderList();
        LongLat startPosition = LongLat.APPLETON;

        Drone drone = new Drone(startPosition, noFlyZone, destinations);
        drone.visitLocations();
        noFlyZone.features().add(Feature.fromGeometry(LineString.fromLngLats(drone.flightpathData)));
        for (Order order : drone.fulfilledOrders) {
            noFlyZone.features().add(Feature.fromGeometry(Point.fromLngLat(order.getDeliverTo().longitude, order.getDeliverTo().latitude)));
        }

        Path path = Paths.get("drone-" + day + '-' + month + '-' + year + ".geojson");
        try {
            Files.writeString(path, noFlyZone.toJson());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        databaseClient.writeAllDeliveriesToTable(drone.fulfilledOrders);
        databaseClient.writeAllMovesToTable(drone.route);

        double moneyEarned = 0;
        for (Order order : drone.fulfilledOrders) {
            moneyEarned += order.getDeliveryCost();
        }
        double potentialMoney = 0;
        for (Order order : drone.orderLocations) {
            potentialMoney += order.getDeliveryCost();
        }

        System.out.println(drone.fulfilledOrders.size());
        System.out.println(drone.orderLocations.size());
        System.out.println(drone.fulfilledOrders.size() / (double) drone.orderLocations.size());
        System.out.println(moneyEarned);
        System.out.println(potentialMoney);
        System.out.println(moneyEarned / potentialMoney);
    }
}
