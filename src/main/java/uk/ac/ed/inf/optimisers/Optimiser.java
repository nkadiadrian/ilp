package uk.ac.ed.inf.optimisers;

import com.mapbox.geojson.FeatureCollection;
import uk.ac.ed.inf.LongLat;
import uk.ac.ed.inf.drone.Drone;
import uk.ac.ed.inf.entities.db.Order;
import uk.ac.ed.inf.optimisers.heuristics.Heuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Optimiser {
    public static final int ASSUMED_GOING_HOME_COST = 10;
    public final List<List<Double>> distanceMatrix = new ArrayList<>();
    private final List<List<Boolean>> firstPickUpMatrix = new ArrayList<>();
    private final List<Order> orders;
    private final FeatureCollection noFlyZone;
    public List<Integer> visitOrder;

    public Optimiser(HashMap<String, Order> orderMap, FeatureCollection noFlyZone) {
        this.orders = new ArrayList<>(orderMap.values());
        this.noFlyZone = noFlyZone;
        initialiseMatrices();
        initialiseVisitOrders();
    }

    public List<Order> getOptimisedOrderList() {
        List<Integer> indices = new ArrayList<>(visitOrder);
        indices.removeAll(List.of(0));
        int previousIndex = 0;
        List<Order> optimisedOrders = new ArrayList<>();
        for (int index : indices) {
            Order nextOrder = orders.get(index - 1);
            if (!firstPickUpMatrix.get(previousIndex).get(index)) {
                Collections.reverse(nextOrder.getShopLocations());
            }
            optimisedOrders.add(nextOrder);
        }
        return optimisedOrders;
    }

    public double getTourValue() {
        double tourCost = 0;
        for (int i = 0; i < visitOrder.size() - 1; i++) {
            tourCost += distanceMatrix.get(visitOrder.get(i)).get(visitOrder.get(i + 1));
        }
        tourCost += distanceMatrix.get(visitOrder.size() - 1).get(0);
        return tourCost;
    }

    public void useHeuristic(Heuristic heuristic) {
        visitOrder = heuristic.applyHeuristic(this);
    }

    private void initialiseVisitOrders() {
        visitOrder = new ArrayList<>();
        for (int i = 0; i <= orders.size(); i++) {
            this.visitOrder.add(i);
        }
    }

    private void initialiseMatrices() {
        List<Order> matrixOrders = new ArrayList<>(this.orders);
        Order tempHomeOrder = new Order(LongLat.APPLETON, Collections.singletonList(LongLat.APPLETON));
        tempHomeOrder.setDeliveryCost(ASSUMED_GOING_HOME_COST);
        matrixOrders.add(0, tempHomeOrder);

        for (LongLat deliverFrom : matrixOrders.stream().map(Order::getDeliverTo).collect(Collectors.toList())) {
            List<Double> distanceRow = new ArrayList<>();
            List<Boolean> firstPickUpRow = new ArrayList<>();
            for (Order order : matrixOrders) {
                double distanceFirst = calcDistanceFirstShopFirst(deliverFrom, order) / order.getDeliveryCost();
                double distanceSecond = calcDistanceLastShopFirst(deliverFrom, order) / order.getDeliveryCost();
                if (distanceFirst <= distanceSecond) {
                    distanceRow.add(distanceFirst);
                    firstPickUpRow.add(true);
                } else {
                    distanceRow.add(distanceSecond);
                    firstPickUpRow.add(false);
                }
            }

            distanceMatrix.add(distanceRow);
            firstPickUpMatrix.add(firstPickUpRow);
        }
    }

    private double calcDistanceFirstShopFirst(LongLat fromLocation, Order toOrder) {
        List<Order> order = Collections.singletonList(toOrder);

        Drone drone = new Drone(fromLocation, noFlyZone, order, false);
        drone.deliver();
        return Drone.MOVES_ALLOWED - drone.getMovesRemaining();
    }

    private double calcDistanceLastShopFirst(LongLat fromLocation, Order toOrder) {
        List<LongLat> shopLocations = new ArrayList<>(toOrder.getShopLocations());
        Collections.reverse(shopLocations);
        Order reversedOrder = new Order(fromLocation, shopLocations);
        List<Order> order = Collections.singletonList(reversedOrder);

        Drone drone = new Drone(fromLocation, noFlyZone, order, false);
        drone.deliver();
        return Drone.MOVES_ALLOWED - drone.getMovesRemaining();
    }
}
