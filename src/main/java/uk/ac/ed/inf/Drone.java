package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Drone {
    private final List<Line2D> noFlyZoneBoundaries;
    private final LongLat startPosition;
    public List<Move> route = new ArrayList<>();
    public List<Order> orders;
    public List<Order> fulfilledOrders = new ArrayList<>();
    public List<Point> flightpathData = new ArrayList<>();
    public int movesRemaining = 1500;
    public boolean homeWhenDone = true;
    public LongLat currentDestination;
    private FeatureCollection noFlyZone;
    private LongLat currPosition;
    private boolean returningToStart = false;
    private int currentOrderIndex = 0;
    private int currentLocationWithinOrderIndex = 0;

    public Drone(LongLat startPosition, FeatureCollection noFlyZone, List<Order> orders) {
        this.startPosition = startPosition;
        this.currPosition = startPosition;
        this.noFlyZone = noFlyZone;
        this.orders = orders;
        this.currentDestination = this.orders.get(0).getAllLocations().get(0);
        this.noFlyZoneBoundaries = getNoFlyZoneBoundaries();
        flightpathData.add(Point.fromLngLat(currPosition.longitude, currPosition.latitude));
    }

    /*
     * Move the drone to create a route of at most 1500 moves.
     */
    public void visitLocations() {
        boolean inFlight = true;
        while (inFlight) {
            LongLat destination = currentDestination;
            int heading = this.currPosition.getClosestAngleToDestination(destination);
            moveDrone(heading);

            double distanceHome = currPosition.distanceTo(startPosition);
            if (distanceHome > (movesRemaining - 30) * LongLat.DRONE_MOVE_LENGTH) {
                returningToStart = true;
                setCurrentDestinationToNextLocation();
            }
            if (this.movesRemaining == 0 || (currPosition.closeTo(currentDestination) && returningToStart)) {
                inFlight = false;
            }
        }
    }

    private void setCurrentDestinationToNextLocation() {
        List<LongLat> currOrderLocations = orders.get(currentOrderIndex).getAllLocations();
        currentLocationWithinOrderIndex++;
        if (returningToStart) {
            currentDestination = startPosition;
            return;
        }

        if (this.currentLocationWithinOrderIndex == currOrderLocations.size()) {
            fulfilledOrders.add(orders.get(currentOrderIndex));
            currentOrderIndex++;
            currentLocationWithinOrderIndex = 0;
            if (this.currentOrderIndex == orders.size()) {
                currentOrderIndex--;
                if (homeWhenDone) {
                    currentDestination = startPosition;
                }
                returningToStart = true;
                return;
            }
            currOrderLocations = orders.get(currentOrderIndex).getAllLocations();
        }

        currentDestination = currOrderLocations.get(currentLocationWithinOrderIndex);
    }

    /*
     * Move the drone, update its position and add the new position coordinate to route
     */
    private void moveDrone(int angle) {
        LongLat proposedNextPosition = this.currPosition.nextPosition(angle);

        // Check if the move involves flying through a No-Fly Zone
        if (moveIntersectsNoFlyZone(proposedNextPosition, this.currPosition) || !proposedNextPosition.isConfined()) {
            angle = getNewAngleAnticlockwise(angle);
            moveDrone(angle);
        }
        // The move is a legal move for the drone
        else {
            String orderNo = orders.get(currentOrderIndex).getOrderNo();
            Move newMove = new Move(orderNo, angle, currPosition, currPosition.nextPosition(angle));
            this.movesRemaining--;
            this.currPosition = proposedNextPosition;
            route.add(newMove);
            flightpathData.add(Point.fromLngLat(currPosition.longitude, currPosition.latitude));
            // Check if this new position is in range of a sensor and take reading if so
            if (currPosition.closeTo(currentDestination)) {
                setCurrentDestinationToNextLocation();
                Move hoverMove = new Move(orderNo, LongLat.HOVERING_ANGLE, currPosition, currPosition);
                this.movesRemaining--;
                route.add(hoverMove);
            }
        }
    }

    /*
     * Return the next angle in anti-clockwise direction
     */
    private int getNewAngleAnticlockwise(int angle) {
        return (Math.floorMod(angle - 10, 360));
    }

    /*
     * Return true if the line formed by the move goes into any No-Fly Zone, false if not.
     */
    private boolean moveIntersectsNoFlyZone(LongLat newPosition, LongLat initialPosition) {
        Line2D moveLine = new Line2D.Double(initialPosition.getLongitude(), initialPosition.getLatitude(),
                newPosition.getLongitude(), newPosition.getLatitude());
        // Loop through all boundaries of No-Fly Zones and check if the move intersects.
        for (Line2D boundary : this.noFlyZoneBoundaries) {
            if (boundary.intersectsLine(moveLine)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Line2D> getNoFlyZoneBoundaries() {
        ArrayList<Line2D> noFlyBoundaries = new ArrayList<>();
        // Get all features from the map, and break them down into all boundary lines.
        // Add the boundary lines to noFlyBoundaries.
        assert this.noFlyZone.features() != null;
        FeatureCollection fc = FeatureCollection.fromFeatures(new ArrayList<>());
        for (Feature feature : this.noFlyZone.features()) {
            Polygon polygon = (Polygon) feature.geometry();
            List<Point> pointList = polygon.coordinates().get(0);
            List<Point> convexHullPoints = ConvexHull.getConvexHull(pointList);
            fc.features().add(Feature.fromGeometry(Polygon.fromLngLats((Collections.singletonList(convexHullPoints)))));
            for (int i = 0; i < convexHullPoints.size() - 1; i++) {
                Point pointFrom = convexHullPoints.get(i);
                Point pointTo = convexHullPoints.get(i + 1);
                Line2D line = new Line2D.Double(pointFrom.longitude(), pointFrom.latitude(), pointTo.longitude(), pointTo.latitude());
                noFlyBoundaries.add(line);
            }
        }
        noFlyZone = fc;
        return noFlyBoundaries;
    }

    public void printStatistics() {
        double moneyEarned = 0;
        for (Order order : fulfilledOrders) {
            moneyEarned += order.getDeliveryCost();
        }
        double potentialMoney = 0;
        for (Order order : orders) {
            potentialMoney += order.getDeliveryCost();
        }

        System.out.println("Deliveries Fulfilled: " + fulfilledOrders.size());
        System.out.println("Total Deliveries: " + orders.size());
        System.out.println("Percentage Delivery Completion: " + (fulfilledOrders.size() / (double) orders.size()));
        System.out.println("Deliveries Fulfilled Value: " + moneyEarned);
        System.out.println("Total Deliveries Value: " + potentialMoney);
        System.out.println("Percentage Monetary Value: " + (moneyEarned / potentialMoney));
        System.out.println("Moves Remaining: " + movesRemaining);
    }

    public void saveRouteGeoJson(String day, String month, String year) {
        for (Order order : fulfilledOrders) {
            noFlyZone.features().add(Feature.fromGeometry(Point.fromLngLat(order.getDeliverTo().longitude, order.getDeliverTo().latitude)));
        }
        noFlyZone.features().add(Feature.fromGeometry(LineString.fromLngLats(flightpathData)));

        Path path = Paths.get("drone-" + day + '-' + month + '-' + year + ".geojson");
        try {
            Files.writeString(path, noFlyZone.toJson());
        } catch (IOException ex) {
            System.err.println("Error writing geojson to file");
        }
    }
}