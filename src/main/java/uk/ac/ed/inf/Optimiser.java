package uk.ac.ed.inf;

import uk.ac.ed.inf.entities.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Optimiser {
    private List<List<Double>> distanceMatrix = new ArrayList<>();
    private List<List<Boolean>> firstPickUpMatrix = new ArrayList<>();

    public Optimiser(HashMap<String, Order> orderMap) {
        initialiseMatrices(orderMap);
    }

    public List<Order> getGreedySolution() {
        return null;
    }

    private List<Order> getOrderListFromIndices(List<Integer> indices) {
        return null;
    }

    public void initialiseMatrices(HashMap<String, Order> orderMap) {
        Order[] orders = orderMap.values().toArray(Order[]::new);

        LongLat deliverfrom = new LongLat(LongLat.APPLETON_TOWER_LONGITUDE, LongLat.APPLETON_TOWER_LATITUDE);

        for (int i = 0; i < orderMap.size(); i++) {
            List<Double> distanceRow = new ArrayList<>();
            List<Boolean> firstPickUpRow = new ArrayList<>();

            for (int j = 0; j < orderMap.size(); j++) {
                double distanceFirst = calcDistanceFirstOrderFirst(orders[i].getDeliverTo(), orders[j]);
                double distanceSecond = calcDistanceSecondOrderFirst(orders[i].getDeliverTo(), orders[j]);
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

    public double calcDistanceFirstOrderFirst(LongLat fromLocation, Order toOrder) {
        double distance = 0;
        List<LongLat> shopList = toOrder.getShopLocations();

        distance += fromLocation.distanceTo(shopList.get(0));
        distance += shopList.get(0).distanceTo(shopList.get(shopList.size() - 1));
        distance += shopList.get(shopList.size() - 1).distanceTo(toOrder.getDeliverTo());

        return distance;
    }

    public double calcDistanceSecondOrderFirst(LongLat fromLocation, Order toOrder) {
        double distance = 0;
        List<LongLat> shopList = toOrder.getShopLocations();

        distance += fromLocation.distanceTo(shopList.get(shopList.size() - 1));
        distance += shopList.get(shopList.size() - 1).distanceTo(shopList.get(0));
        distance += shopList.get(0).distanceTo(toOrder.getDeliverTo());

        return distance;
    }
}
