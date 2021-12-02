package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;
import uk.ac.ed.inf.entities.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Optimiser {
    public List<Integer> visitOrder;
    private final List<List<Double>> distanceMatrix = new ArrayList<>();
    private final List<List<Boolean>> firstPickUpMatrix = new ArrayList<>();
    private final List<Order> orders;
    private final FeatureCollection noFlyZone;

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

    public void useGreedy() {
        List<Integer> visitedIndices = new ArrayList<>();
        int currentindex = 0;
        visitedIndices.add(currentindex);

        while (visitedIndices.size() <= orders.size()) {
            List<Double> distanceRow = distanceMatrix.get(currentindex);
            double minDist = Double.POSITIVE_INFINITY;
            int nextMove = 0;

            for (int i = 1; i < distanceRow.size(); i++) {
                if (distanceRow.get(i) < minDist & !visitedIndices.contains(i) & i != currentindex) {
                    minDist = distanceRow.get(i);
                    nextMove = i;
                }
            }
            visitedIndices.add(nextMove);
            currentindex = nextMove;
        }

        visitOrder = visitedIndices;
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
        matrixOrders.add(0, tempHomeOrder);

        for (LongLat deliverFrom : matrixOrders.stream().map(Order::getDeliverTo).collect(Collectors.toList())) {
            List<Double> distanceRow = new ArrayList<>();
            List<Boolean> firstPickUpRow = new ArrayList<>();

            for (Order order : matrixOrders) {
                double distanceFirst = calcDistanceFirstShopFirst(deliverFrom, order);
                double distanceSecond = calcDistanceLastShopFirst(deliverFrom, order);
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
//        FeatureCollection map = FeatureCollection.fromFeatures(new ArrayList<>(noFlyZone.features()));
//        map.features().add(Feature.fromGeometry(Point.fromLngLat(fromLocation.longitude, fromLocation.latitude)));
//        List<Point> line = new ArrayList<>();
//        for (LongLat spot: toOrder.getAllLocations()) {
//            map.features().add(Feature.fromGeometry(Point.fromLngLat(spot.longitude, spot.latitude)));
//        }
//        map.features().add(Feature.fromGeometry(LineString.fromLngLats(line)));
//        System.out.println(map.toJson());

        List<Order> order = Collections.singletonList(toOrder);

        Drone drone = new Drone(fromLocation, noFlyZone, order);
        drone.homeWhenDone = false;
        drone.visitLocations();
        return 1500 - drone.moves;
    }

    private double calcDistanceLastShopFirst(LongLat fromLocation, Order toOrder) {
        List<LongLat> shopLocations = new ArrayList<>(toOrder.getShopLocations());
        Collections.reverse(shopLocations);
        Order reversedOrder = new Order(fromLocation, shopLocations);
        List<Order> order = Collections.singletonList(reversedOrder);

        Drone drone = new Drone(fromLocation, noFlyZone, order);
        drone.homeWhenDone = false;
        drone.visitLocations();
        return 1500 - drone.moves;
    }
//
//    private double calcDistanceFirstShopFirst(LongLat fromLocation, Order toOrder) {
//        double distance = 0;
//        List<LongLat> shopList = toOrder.getShopLocations();
//        LongLat firstShop = shopList.get(0);
//        LongLat lastShop = shopList.get(shopList.size() - 1);
//
//        distance += fromLocation.distanceTo(firstShop);
//        distance += firstShop.distanceTo(lastShop);
//        distance += lastShop.distanceTo(toOrder.getDeliverTo());
//        return distance;
//    }
//
//    private double calcDistanceLastShopFirst(LongLat fromLocation, Order toOrder) {
//        double distance = 0;
//        List<LongLat> shopList = toOrder.getShopLocations();
//        LongLat firstShop = shopList.get(0);
//        LongLat lastShop = shopList.get(shopList.size() - 1);
//
//        distance += fromLocation.distanceTo(lastShop);
//        distance += lastShop.distanceTo(firstShop);
//        distance += firstShop.distanceTo(toOrder.getDeliverTo());
//        return distance;
//    }

    private double getTourValue() {
        double tourCost = 0;
        for (int i = 0; i < visitOrder.size() - 1; i++) {
            tourCost += distanceMatrix.get(visitOrder.get(i)).get(visitOrder.get(i + 1));
//            System.out.println(distanceMatrix.get(visitOrder.get(i)).get(visitOrder.get(i+1)));
//            System.out.println(tourCost);
        }
        tourCost += distanceMatrix.get(visitOrder.size() - 1).get(0);
        return tourCost;
    }

    private boolean trySwap(int i) {
        double oldCost = getTourValue();
        int j = (i + 1);
//        System.out.println(visitOrder);
//        System.out.println(oldCost);
        Collections.swap(visitOrder, i, j);
//        System.out.println(visitOrder);
//        System.out.println(getTourValue());
//        System.out.println(i);
//        System.out.println(j);
        if (getTourValue() < oldCost) {
//            System.out.println("Succesful");
            return true;
        } else {
            Collections.swap(visitOrder, i, j);
//            System.out.println("Failed");
            return false;
        }
    }

    private boolean tryReverse(int i, int j) {
        double oldCost = getTourValue();
        Collections.reverse(visitOrder.subList(i, j));
        if (getTourValue() < oldCost) {
            return true;
        } else {
            Collections.reverse(visitOrder.subList(i, j));
            return false;
        }
    }

    public void useSwapHeuristic(int k) {
        boolean better = true;
        int count = 0;
        while (better && (count < k || k == -1)) {
            better = false;
            count += 1;
            for (int i = 1; i < visitOrder.size() - 1; i++) {
                if (trySwap(i)) {
                    better = true;
                }
            }
        }
    }

    public void useTwoOptHeuristic(int k) {
        boolean better = true;
        int count = 0;
        while (better && (count < k || k == -1)) {
            better = false;
            count += 1;
            for (int j = 1; j < visitOrder.size(); j++) {
                for (int i = 1; i < j; i++) {
                    if (tryReverse(i, j)) {
                        better = true;
                    }
                }
            }
        }
    }
}
