package uk.ac.ed.inf.drone.helpers;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import uk.ac.ed.inf.drone.Drone;
import uk.ac.ed.inf.entities.db.Order;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OutputUtils {
    public static void printStatistics(Drone drone) {
        List<Order> fulfilledOrders = drone.getFulfilledOrders();
        List<Order> orders = drone.getOrders();

        int moneyEarned = 0;
        for (Order order : fulfilledOrders) {
            moneyEarned += order.getDeliveryCost();
        }
        int potentialMoney = 0;
        for (Order order : orders) {
            potentialMoney += order.getDeliveryCost();
        }

        System.out.println("Deliveries Fulfilled: " + fulfilledOrders.size());
        System.out.println("Total Deliveries: " + orders.size());
        System.out.println("Percentage Delivery Completion: " + (fulfilledOrders.size() / (double) orders.size()));
        System.out.println("Deliveries Fulfilled Value: " + moneyEarned);
        System.out.println("Total Deliveries Value: " + potentialMoney);
        System.out.println("Percentage Monetary Value: " + (moneyEarned / (double) potentialMoney));
        System.out.println("Moves Remaining: " + drone.getMovesRemaining());
    }

    public static void printSimpleStatistics(Drone drone) {
        List<Order> fulfilledOrders = drone.getFulfilledOrders();
        List<Order> orders = drone.getOrders();

        int moneyEarned = 0;
        for (Order order : fulfilledOrders) {
            moneyEarned += order.getDeliveryCost();
        }
        int potentialMoney = 0;
        for (Order order : orders) {
            potentialMoney += order.getDeliveryCost();
        }

        System.out.println(fulfilledOrders.size());
        System.out.println(orders.size());
        System.out.println((fulfilledOrders.size() / (double) orders.size()));
        System.out.println(moneyEarned);
        System.out.println(potentialMoney);
        System.out.println((moneyEarned / (double) potentialMoney));
        System.out.println(drone.getMovesRemaining());
    }

    public static void saveRouteGeoJson(Drone drone) {
        saveRouteGeoJson(drone, "test", "test", "test");
    }

    public static void saveRouteGeoJson(Drone drone, String day, String month, String year) {
        assert drone.getNoFlyZone().features() != null;
        FeatureCollection outputMap = FeatureCollection.fromFeatures(new ArrayList<>());
        assert outputMap.features() != null;
        outputMap.features().addAll(drone.getNoFlyZone().features());
        outputMap.features().add(Feature.fromGeometry(LineString.fromLngLats(drone.getRoute())));

        Path path = Paths.get("drone-" + day + '-' + month + '-' + year + ".geojson");
        try {
            Files.writeString(path, outputMap.toJson());
        } catch (IOException ex) {
            System.err.println("Error writing geojson to file");
        }
    }
}
