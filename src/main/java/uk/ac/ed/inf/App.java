package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;
import uk.ac.ed.inf.clients.DatabaseClient;
import uk.ac.ed.inf.drone.Drone;
import uk.ac.ed.inf.drone.helpers.OutputUtils;
import uk.ac.ed.inf.entities.db.Order;
import uk.ac.ed.inf.optimisers.Optimiser;
import uk.ac.ed.inf.optimisers.heuristics.GreedyHeuristic;
import uk.ac.ed.inf.optimisers.heuristics.SwapHeuristic;
import uk.ac.ed.inf.optimisers.heuristics.TwoOptHeuristic;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static final String TEST_MODE = "1";

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
        optimiser.useHeuristic(new GreedyHeuristic());
//        optimiser.useHeuristic(new SwapHeuristic());
//        optimiser.useHeuristic(new TwoOptHeuristic());
        List<Order> destinations = optimiser.getOptimisedOrderList();

        Drone drone = new Drone(startPosition, noFlyZone, destinations);
        drone.deliver();
        OutputUtils.saveRouteGeoJson(drone, day, month, year);
        databaseClient.writeAllDeliveriesToTable(drone.getFulfilledOrders());
        databaseClient.writeAllMovesToTable(drone.getFlightPathData());


        if (args.length > 5 && args[5].equals(TEST_MODE)) {
            OutputUtils.printSimpleStatistics(drone);
        } else {
            OutputUtils.printStatistics(drone);
        }
    }
}
