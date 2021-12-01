package uk.ac.ed.inf;

import uk.ac.ed.inf.entities.Order;

import java.util.*;
import java.util.stream.Collectors;

public class Optimiser {
    private List<List<Double>> distanceMatrix = new ArrayList<>();
    private List<List<Boolean>> firstPickUpMatrix = new ArrayList<>();
    private List<Order> orders;
    private List<Integer> visitOrder;

    public Optimiser(HashMap<String, Order> orderMap) {
        this.orders = new ArrayList<>(orderMap.values());
        initialiseMatrices(orderMap);
        initialiseVisitOrders();
    }

    public List<Order> getGreedySolution() {
        List<Integer> visitedIndices = new ArrayList<>();
        int currentindex = 0;
        visitedIndices.add(currentindex);

        while (visitedIndices.size() <= orders.size()) {
            List<Double> distanceRow = distanceMatrix.get(currentindex);
            double minDist = Double.POSITIVE_INFINITY;
            int nextMove = 0;

            for (int i = 1; i < distanceRow.size(); i++) {
                if (distanceRow.get(i) < minDist & !visitedIndices.contains(i) & i != currentindex ) {
                    minDist = distanceRow.get(i);
                    nextMove = i;
                }
            }
            visitedIndices.add(nextMove);
            currentindex = nextMove;
        }

        visitOrder = visitedIndices;

        return getOrderListFromIndices();
    }

    private void initialiseVisitOrders() {
        visitOrder = new ArrayList<>();
        for (int i = 0; i <= orders.size(); i++) {
            this.visitOrder.add(i++);
        }
    }

    private List<Order> getOrderListFromIndices() {
        List<Integer> indices = new ArrayList<>(visitOrder);
        indices.removeAll(List.of(0));
        int previousIndex = 0;
        List<Order> optimisedOrders = new ArrayList<>();
        for (int index: indices) {
            Order nextOrder = orders.get(index - 1);
            if (!firstPickUpMatrix.get(previousIndex).get(index)) {
                Collections.reverse(nextOrder.getShopLocations());
            }
            optimisedOrders.add(nextOrder);
        }
        return optimisedOrders;
    }

    private void initialiseMatrices(HashMap<String, Order> orderMap) {
        List<Order> matrixOrders = new ArrayList<>(this.orders);
        Order tempHomeOrder = new Order(LongLat.APPLETON, Collections.singletonList(LongLat.APPLETON));
        matrixOrders.add(0, tempHomeOrder);

        for (LongLat deliverFrom: matrixOrders.stream().map(Order::getDeliverTo).collect(Collectors.toList())) {
            List<Double> distanceRow = new ArrayList<>();
            List<Boolean> firstPickUpRow = new ArrayList<>();

            for (Order order: matrixOrders) {
                double distanceFirst = calcDistanceFirstOrderFirst(deliverFrom, order);
                double distanceSecond = calcDistanceSecondOrderFirst(deliverFrom, order);
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

    private double calcDistanceFirstOrderFirst(LongLat fromLocation, Order toOrder) {
        double distance = 0;
        List<LongLat> shopList = toOrder.getShopLocations();
        LongLat firstShop = shopList.get(0);
        LongLat lastShop = shopList.get(shopList.size() - 1);

        distance += fromLocation.distanceTo(firstShop);
        distance += firstShop.distanceTo(lastShop);
        distance += lastShop.distanceTo(toOrder.getDeliverTo());
        return distance;
    }

    private double calcDistanceSecondOrderFirst(LongLat fromLocation, Order toOrder) {
        double distance = 0;
        List<LongLat> shopList = toOrder.getShopLocations();
        LongLat firstShop = shopList.get(0);
        LongLat lastShop = shopList.get(shopList.size() - 1);

        distance += fromLocation.distanceTo(lastShop);
        distance += lastShop.distanceTo(firstShop);
        distance += firstShop.distanceTo(toOrder.getDeliverTo());
        return distance;
    }

    private int getTourValue() {
        int tourCost = 0;
        for (int i = 0; i < visitOrder.size(); i++) {
            tourCost += distanceMatrix.get(visitOrder.get(i)).get(visitOrder.get(i+1));
        }
        tourCost += distanceMatrix.get(visitOrder.size() - 1).get(0);
        return tourCost;
    }
}
