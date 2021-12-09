package uk.ac.ed.inf.drone;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.LongLat;
import uk.ac.ed.inf.drone.helpers.BoundaryUtils;
import uk.ac.ed.inf.entities.db.Move;
import uk.ac.ed.inf.entities.db.Order;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class Drone {
    public static final int MOVES_ALLOWED = 1500;
    private static final int RETURN_HOME_SAFETY_BUFFER = 40;
    private static final int ZONE_AVOIDANCE_STEP = 10;
    private final List<Line2D> noFlyZoneBoundaries;
    private final LongLat startPosition;
    private final List<Order> orders;
    private final List<Order> fulfilledOrders = new ArrayList<>();
    private final List<Point> route = new ArrayList<>();
    private final List<Move> flightPathData = new ArrayList<>();
    private final FeatureCollection noFlyZone;
    private int movesRemaining = MOVES_ALLOWED;
    private boolean homeWhenDone = true;
    private LongLat currDestination;
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
        this.noFlyZoneBoundaries = BoundaryUtils.getNoFlyZoneBoundaries(this.noFlyZone);
        route.add(Point.fromLngLat(currPosition.longitude, currPosition.latitude));
    }

    public Drone(LongLat startPosition, FeatureCollection noFlyZone, List<Order> orders, boolean homeWhenDone) {
        this(startPosition, noFlyZone, orders);
        this.homeWhenDone = homeWhenDone;
    }

    /*
     * Return the next angle in anti-clockwise direction
     */
    private static int getNextCcwAngle(int angle) {
        return (Math.floorMod(angle - ZONE_AVOIDANCE_STEP, LongLat.MAX_BEARING));
    }

    /*
     * Move the drone to create a route of at most 1500 moves.
     */
    public void deliver() {
        boolean inFlight = true;
        while (inFlight) {
            int bearing = this.currPosition.getClosestAngleToDestination(currDestination);
            moveDrone(bearing);

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
        if (isReverseMove(angle)) {
            moveDrone(Math.floorMod(angle + 180, 360));
            return;
        }
        // Check if the move involves flying through a No-Fly Zone
        if (BoundaryUtils.moveIntersectsZone(proposedNextPosition, this.currPosition, noFlyZoneBoundaries) ||
                !proposedNextPosition.isConfined()) {
            angle = getNextCcwAngle(angle);
            moveDrone(angle);
        }
        // The move is a legal move for the drone
        else {
            String orderNo = orders.get(currOrderIndex).getOrderNo();
            Move newMove = new Move(orderNo, angle, currPosition, currPosition.nextPosition(angle));
            this.movesRemaining--;
            this.currPosition = proposedNextPosition;
            flightPathData.add(newMove);
            route.add(Point.fromLngLat(currPosition.longitude, currPosition.latitude));
            // Check if this new position is in range of a sensor and take reading if so
            if (currPosition.closeTo(currDestination)) {
                currDestination = getNextLocation();
                Move hoverMove = new Move(orderNo, LongLat.HOVERING_ANGLE, currPosition, currPosition);
                flightPathData.add(hoverMove);
                this.movesRemaining--;
            }
        }
    }

    private boolean isReverseMove(int proposedAngle) {
        if (flightPathData.size() > 0) {
            Move lastMove = flightPathData.get(flightPathData.size() - 1);
            int reversedLastAngle = Math.floorMod(lastMove.angle + 180, 360);
            return reversedLastAngle == proposedAngle;
        }
        return false;
    }

    public List<Move> getFlightPathData() {
        return flightPathData;
    }

    public List<Order> getFulfilledOrders() {
        return fulfilledOrders;
    }

    public int getMovesRemaining() {
        return movesRemaining;
    }

    public FeatureCollection getNoFlyZone() {
        return noFlyZone;
    }

    public List<Point> getRoute() {
        return route;
    }

    public List<Order> getOrders() {
        return orders;
    }
}