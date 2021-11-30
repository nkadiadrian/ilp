package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import uk.ac.ed.inf.entities.Order;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/*
 * The Drone class represents a Drone, with a position and number of
 * moves as attributes.
 *
 */
public class Drone {

    private LongLat startPosition;
    private FeatureCollection noFlyZone;
    public List<Move> route = new ArrayList<Move>();
    public List<LongLat> homeRouteLandmarks = new ArrayList<LongLat>();
    public List<Order> orderLocations;
    public List<Order> fulfilledOrders = new ArrayList<Order>();
    public List<Point> flightpathData = new ArrayList<>();
    public LongLat currentDestination;
    private LongLat currentPosition;
    private int moves = 1500;
    private boolean returningToStart;
    private int currentOrderIndex = 0;
    private int currentLocationOrderIndex = 0;
    private List<Line2D> noFlyBoundaries;


    public Drone(LongLat startPosition, FeatureCollection noFlyZone, ArrayList<Order> orderLocations) {
        this.startPosition = startPosition;
        this.currentPosition = startPosition;
        this.returningToStart = false;
        this.noFlyZone = noFlyZone;
        this.orderLocations = orderLocations;
        this.currentDestination = this.orderLocations.get(0).getAllLocations().get(0);
        this.noFlyBoundaries = getNoFlyBoundaries();
        flightpathData.add(Point.fromLngLat(currentPosition.longitude, currentPosition.latitude));
    }

    /*
     * Move the drone to create a route of at most 150 moves, visiting all sensors (if possible).
     */
    public void visitLocations() {
        boolean continueFlight = true;
        while (continueFlight) {
            LongLat destination = currentDestination;
            int direction = this.currentPosition.getClosestAngleToDestination(destination);
            moveDrone(direction);
            // Check the stopping conditions
            if (this.moves == 0 || (currentPosition.closeTo(startPosition) && returningToStart)) {
                System.out.println("HOME");
                continueFlight = false;
            }
        }
    }

    private void setCurrentDestination() {
        ArrayList<LongLat> currOrderLocations = orderLocations.get(currentOrderIndex).getAllLocations();
        currentLocationOrderIndex++;
        if (returningToStart) {
            currentDestination = startPosition;
            return;
        }

        if (this.currentLocationOrderIndex == currOrderLocations.size()) {
            fulfilledOrders.add(orderLocations.get(currentOrderIndex));
            currentOrderIndex++;
            currentLocationOrderIndex = 0;
            if (this.currentOrderIndex == orderLocations.size()) {
                currentOrderIndex--; // TODO: Create a "HOME" order
                returningToStart = true;
                currentDestination = startPosition;
                return;
            }
            currOrderLocations = orderLocations.get(currentOrderIndex).getAllLocations();
        }

        currentDestination = currOrderLocations.get(currentLocationOrderIndex);
    }

    /*
     * Move the drone, update its position and add the new position coordinate to route
     */
    private void moveDrone(int angle) {
        LongLat proposedNextPosition = this.currentPosition.nextPosition(angle);

        // Check if the move involves flying through a No-Fly Zone
        if (moveIntersectsNoFlyZone(proposedNextPosition, this.currentPosition) || !proposedNextPosition.isConfined()) {
            angle = getNewAngleAnticlockwise(angle);
            moveDrone(angle);
            System.out.println("TRIGGER");
        }
        // Check if the suggested move has been repeated within the last 5 moves
//        else if (isRepeatedMove(proposedNextPoint, this.currentPosition.toPoint())) {
//            angle = getNewAngleAnticlockwise(angle - 20);
//        moveDrone(angle, currentPosition.nextPosition(angle));
//        }
        // The move is a legal move for the drone
        else {
            String orderNo = orderLocations.get(currentOrderIndex).getOrderNo();
            Move newMove = new Move(orderNo, angle, currentPosition, currentPosition.nextPosition(angle));
            this.moves--;
            this.currentPosition = proposedNextPosition;
            route.add(newMove);
            flightpathData.add(Point.fromLngLat(currentPosition.longitude, currentPosition.latitude));
            // Check if this new position is in range of a sensor and take reading if so
            double distanceHome = currentPosition.distanceTo(startPosition);
            if (distanceHome > (moves + 15)*LongLat.DRONE_MOVE_LENGTH) {
                returningToStart = true;
                setCurrentDestination();
            }
            if (currentPosition.closeTo(currentDestination)) {
                setCurrentDestination();
//                moveDrone(LongLat.HOVERING_ANGLE);
            }
        }
    }

    /*
     * Return true is the drone has already moved from point A to point B in the last 5 moves,
     * false if not.
     */
    private boolean isRepeatedMove(Move proposedNext, Move current) {
        for (int i = 1; i <= 5; i++) {
            if (this.route.size() > 5) {
                var pointA = this.route.get(this.route.size() - i);
                var pointB = this.route.get(this.route.size() - (i + 1));
                if (pointA.equals(proposedNext) && pointB.equals(current)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Return the next angle in anti-clockwise direction
     */
    private int getNewAngleAnticlockwise(int angle) {
        return ((angle - 10) % 360);
    }

    /*
     * Return true if the line formed by the move goes into any No-Fly Zone, false if not.
     */
    private boolean moveIntersectsNoFlyZone(LongLat newPosition, LongLat initialPosition) {
        Line2D moveLine = new Line2D.Double(initialPosition.getLongitude(), initialPosition.getLatitude(),
                newPosition.getLongitude(), newPosition.getLatitude());
        // Loop through all boundaries of No-Fly Zones and check if the move intersects.
        for (Line2D boundary : this.noFlyBoundaries) {
            if (boundary.intersectsLine(moveLine)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Line2D> getNoFlyBoundaries() {
        ArrayList<Line2D> noFlyBoundaries = new ArrayList<>();
        // Get all features from the map, and break them down into all boundary lines.
        // Add the boundary lines to noFlyBoundaries.
        assert this.noFlyZone.features() != null;
        for (var feature : this.noFlyZone.features()) {
            var polygon = (Polygon) feature.geometry();
            var coordinateLists = polygon.coordinates();
            var coordinateList = coordinateLists.get(0);
            for (int i = 0; i < coordinateList.size() - 1; i++) {
                var pointA = coordinateList.get(i);
                var pointB = coordinateList.get(i + 1);
                var line = new Line2D.Double(pointA.longitude(), pointA.latitude(), pointB.longitude(), pointB.latitude());
                noFlyBoundaries.add(line);
            }
        }
        return noFlyBoundaries;
    }
}