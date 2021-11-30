package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import uk.ac.ed.inf.clients.DatabaseClient;
import uk.ac.ed.inf.clients.MenuWebsiteClient;
import uk.ac.ed.inf.entities.Order;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
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

        FeatureCollection noFlyZone = Menus.getMenuClient().getNoFlyZone();
        ArrayList<Order> destinations = new ArrayList<>(orders.values());
        Drone drone = new Drone(new LongLat(LongLat.APPLETON_TOWER_LONGITUDE, LongLat.APPLETON_TOWER_LATITUDE),
                noFlyZone,
                destinations);
        drone.visitLocations();
        noFlyZone.features().add(Feature.fromGeometry(LineString.fromLngLats(drone.flightpathData)));
        noFlyZone.toJson();

        Path path = Paths.get("drone-" + day + '-' + month + '-' + year + ".geojson");

        try {
            Files.writeString(path, noFlyZone.toJson());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
