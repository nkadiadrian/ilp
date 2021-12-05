package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderNo;
    private LongLat deliverTo;
    private String threeWordsDeliverTo;
    private int deliveryCost;
    private List<LongLat> shopLocations = new ArrayList<>();
    private List<String> items;

    public Order(String orderNo, LongLat deliverTo, String threeWordsDeliverTo) {
        this.orderNo = orderNo;
        this.deliverTo = deliverTo;
        this.threeWordsDeliverTo = threeWordsDeliverTo;
        this.items = new ArrayList<>();
    }

    public Order(LongLat deliverTo, List<LongLat> shopLocations) {
        this.deliverTo = deliverTo;
        this.shopLocations = shopLocations;
    }

    public void addItem(String item, LongLat shopLocation) {
        items.add(item);
        if (!shopLocations.contains(shopLocation)) {
            shopLocations.add(shopLocation);
        }
    }

    public LongLat getDeliverTo() {
        return deliverTo;
    }

    public void setDeliverTo(LongLat deliverTo) {
        this.deliverTo = deliverTo;
    }

    public String getThreeWordsDeliverTo() {
        return threeWordsDeliverTo;
    }

    public void setThreeWordsDeliverTo(String threeWordsDeliverTo) {
        this.threeWordsDeliverTo = threeWordsDeliverTo;
    }

    public List<LongLat> getShopLocations() {
        return shopLocations;
    }

    public void setShopLocations(ArrayList<LongLat> shopLocations) {
        this.shopLocations = shopLocations;
    }

    public int getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(int deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public List<LongLat> getAllLocations() {
        List<LongLat> locations = new ArrayList<>(this.shopLocations);
        locations.add(this.deliverTo);
        return locations;
    }
}
