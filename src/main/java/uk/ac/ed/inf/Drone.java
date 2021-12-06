package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import uk.ac.ed.inf.entities.db.Move;
import uk.ac.ed.inf.entities.db.Order;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Drone {
    public static final int MOVES_ALLOWED = 1500;
    public static final int RETURN_HOME_SAFETY_BUFFER = 30;
    public static final int ZONE_AVOIDANCE_STEP = 10;
    private final List<Line2D> noFlyZoneBoundaries;
    private final LongLat startPosition;
    private final List<Order> orders;
    private final List<Order> fulfilledOrders = new ArrayList<>();
    private final List<Point> flightpathData = new ArrayList<>();
    private int movesRemaining = MOVES_ALLOWED;
    private final List<Move> route = new ArrayList<>();
    private boolean homeWhenDone = true;
    private LongLat currDestination;
    private final FeatureCollection noFlyZone;
    private LongLat currPosition;
    private boolean returningToStart = false;
    private int currOrderIndex = 0;
    private int currLocationWithinOrderIndex = 0;

    public Drone(LongLat startPosition, FeatureCollection noFlyZone, List<Order> orders) {
        this.startPosition = startPosition;
        this.currPosition = startPosition;
        this.noFlyZone = noFlyZone;
        this.orders = orders;
        this.currDestination = this.orders.get(0).getAllLocations().get(0);
        this.noFlyZoneBoundaries = getNoFlyZoneBoundaries();
        flightpathData.add(Point.fromLngLat(currPosition.longitude, currPosition.latitude));
    }

    public Drone(LongLat startPosition, FeatureCollection noFlyZone, List<Order> orders, boolean homeWhenDone) {
        this(startPosition, noFlyZone, orders);
        this.homeWhenDone = homeWhenDone;
    }

    /*
     * Move the drone to create a route of at most 1500 moves.
     */
    public void visitLocations() {
        boolean inFlight = true;
        while (inFlight) {
            LongLat destination = currDestination;
            int heading = this.currPosition.getClosestAngleToDestination(destination);
            moveDrone(heading);

            double distanceHome = currPosition.distanceTo(startPosition);
            if (distanceHome > (movesRemaining - RETURN_HOME_SAFETY_BUFFER) * LongLat.DRONE_MOVE_LENGTH) {
                returningToStart = true;
                currDestination = getNextLocation();
            }
            if (this.movesRemaining == 0 || (currPosition.closeTo(currDestination) && returningToStart)) {
                inFlight = false;
            }
        }
    }

    private LongLat getNextLocation() {
        List<LongLat> currOrderLocations = orders.get(currOrderIndex).getAllLocations();
        currLocationWithinOrderIndex++;
        if (returningToStart) {
            return startPosition;
        }

        if (this.currLocationWithinOrderIndex == currOrderLocations.size()) {
            fulfilledOrders.add(orders.get(currOrderIndex));
            currOrderIndex++;
            currLocationWithinOrderIndex = 0;
            if (this.currOrderIndex == orders.size()) {
                currOrderIndex--; //sets currOrderIndex to safe value to query orders later on
                returningToStart = true;
                if (homeWhenDone) {
                    return startPosition;
                }
                return currDestination;
            }
            currOrderLocations = orders.get(currOrderIndex).getAllLocations();
        }

        return currOrderLocations.get(currLocationWithinOrderIndex);
    }

    /*
     * Move the drone, update its position and add the new position coordinate to route
     */
    private void moveDrone(int angle) {
        LongLat proposedNextPosition = this.currPosition.nextPosition(angle);

        // Check if the move involves flying through a No-Fly Zone
        if (moveIntersectsNoFlyZone(proposedNextPosition, this.currPosition) || !proposedNextPosition.isConfined()) {
            angle = getNextCcwAngle(angle);
            moveDrone(angle);
        }
        // The move is a legal move for the drone
        else {
            String orderNo = orders.get(currOrderIndex).getOrderNo();
            Move newMove = new Move(orderNo, angle, currPosition, currPosition.nextPosition(angle));
            this.movesRemaining--;
            this.currPosition = proposedNextPosition;
            route.add(newMove);
            flightpathData.add(Point.fromLngLat(currPosition.longitude, currPosition.latitude));
            // Check if this new position is in range of a sensor and take reading if so
            if (currPosition.closeTo(currDestination)) {
                currDestination = getNextLocation();
                Move hoverMove = new Move(orderNo, LongLat.HOVERING_ANGLE, currPosition, currPosition);
                route.add(hoverMove);
                this.movesRemaining--;
            }
        }
    }

    /*
     * Return the next angle in anti-clockwise direction
     */
    private int getNextCcwAngle(int angle) {
        return (Math.floorMod(angle - ZONE_AVOIDANCE_STEP, LongLat.MAX_BEARING));
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
        for (Feature feature : this.noFlyZone.features()) {
            Polygon polygon = (Polygon) feature.geometry();
            assert polygon != null;
            List<Point> pointList = polygon.coordinates().get(0);
            List<Point> convexHullPoints = ConvexHull.getConvexHull(pointList);
            for (int i = 0; i < convexHullPoints.size() - 1; i++) {
                Point pointFrom = convexHullPoints.get(i);
                Point pointTo = convexHullPoints.get(i + 1);
                Line2D line = new Line2D.Double(pointFrom.longitude(), pointFrom.latitude(), pointTo.longitude(), pointTo.latitude());
                noFlyBoundaries.add(line);
            }
        }
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
        assert noFlyZone.features() != null;
        noFlyZone.features().add(Feature.fromGeometry(LineString.fromLngLats(flightpathData)));

        Path path = Paths.get("drone-" + day + '-' + month + '-' + year + ".geojson");
        try {
            Files.writeString(path, noFlyZone.toJson());
        } catch (IOException ex) {
            System.err.println("Error writing geojson to file");
        }
    }

    public List<Move> getRoute() {
        return route;
    }

    public List<Order> getFulfilledOrders() {
        return fulfilledOrders;
    }

    public int getMovesRemaining() {
        return movesRemaining;
    }

    public void setMovesRemaining(int movesRemaining) {
        this.movesRemaining = movesRemaining;
    }
}