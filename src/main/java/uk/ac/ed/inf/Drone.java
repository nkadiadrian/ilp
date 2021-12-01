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


    public Drone(LongLat startPosition, FeatureCollection noFlyZone, List<Order> orderLocations) {
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

            double distanceHome = currentPosition.distanceTo(startPosition);
            if (distanceHome > (moves - 30)*LongLat.DRONE_MOVE_LENGTH) {
                returningToStart = true;
                setCurrentDestination();
            }
            // Check the stopping conditions
            if (this.moves == 0) {
                System.out.println("DEAD");
                continueFlight = false;
            }
            if (currentPosition.closeTo(startPosition) && returningToStart) {
                System.out.println("HOME");
                continueFlight = false;
            }
        }
    }

    private void setCurrentDestination() {
        List<LongLat> currOrderLocations = orderLocations.get(currentOrderIndex).getAllLocations();
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
        Move proposedMove = new Move(orderLocations.get(currentOrderIndex).getOrderNo(), angle, currentPosition, proposedNextPosition);
//        if (route.size() > 0) {
//            Move reversedLastMove = new Move(route.get(route.size() - 1));
//            reversedLastMove.angle =  Math.floorMod(reversedLastMove.angle + 180, 360);
//            if (reversedLastMove.getOrderNo().equals(proposedMove.getOrderNo()) && reversedLastMove.angle == proposedMove.angle) {
//                moveDrone(getNewAngleAnticlockwise(route.get(route.size() - 1).angle - 10));
//            }
//        }

        if (moveIntersectsNoFlyZone(proposedNextPosition, this.currentPosition) || !proposedNextPosition.isConfined()) {
            angle = getNewAngleAnticlockwise(angle);
            moveDrone(angle);
        }
        // Check if the suggested move has been repeated within the last 5
        else if (isRepeatedMove(proposedMove)) {
            angle = getNewAngleAnticlockwise((angle - 30));
            moveDrone(angle);
            moveDrone(angle);
            moveDrone(angle);
        }
        // The move is a legal move for the drone
        else {
            String orderNo = orderLocations.get(currentOrderIndex).getOrderNo();
            Move newMove = new Move(orderNo, angle, currentPosition, currentPosition.nextPosition(angle));
            this.moves--;
            this.currentPosition = proposedNextPosition;
            route.add(newMove);
            flightpathData.add(Point.fromLngLat(currentPosition.longitude, currentPosition.latitude));
            // Check if this new position is in range of a sensor and take reading if so
            if (currentPosition.closeTo(currentDestination)) {
                setCurrentDestination();
                Move hoverMove = new Move(orderNo, LongLat.HOVERING_ANGLE, currentPosition, currentPosition);
                this.moves--;
                route.add(hoverMove);
            }
        }
    }

    /*
     * Return true is the drone has already moved from point A to point B in the last 5 moves,
     * false if not.
     */
    private boolean isRepeatedMove(Move move) {
        if (this.route.size() > 5) {
            List<Move> last5Moves = route.subList(route.size() - 5, route.size());
            if (last5Moves.contains(move)) {
                System.out.println("REPETITON");
                return true;
            }
        }
        return false;
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