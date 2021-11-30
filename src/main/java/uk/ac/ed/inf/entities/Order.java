package uk.ac.ed.inf.entities;

import uk.ac.ed.inf.LongLat;

import java.util.ArrayList;
import java.util.Collections;

public class Order {
    private String orderNo;
    private LongLat deliverTo;
    private int deliveryCost;
    private ArrayList<LongLat> shopLocations = new ArrayList<>();
    private ArrayList<String> items;
    private boolean visitFirstShopFirst = true;

    public Order(String orderNo, LongLat deliverTo) {
        this.orderNo = orderNo;
        this.deliverTo = deliverTo;
        this.items = new ArrayList<>();
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

    public int getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(int deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public ArrayList<LongLat> getShopLocations() {
        if (this.visitFirstShopFirst || this.shopLocations.size() == 1) {
            return shopLocations;
        }
        return swap(this.shopLocations);
    }

    public static ArrayList<LongLat> swap(ArrayList<LongLat> arr){
        ArrayList<LongLat> reversedArr = new ArrayList<>();
        reversedArr.add(arr.get(1));
        reversedArr.add(arr.get(0));
        return reversedArr;
    }

    public ArrayList<LongLat> getAllLocations(){
        ArrayList<LongLat> locations = new ArrayList<>(this.shopLocations);
        locations.add(this.deliverTo);
        return locations;
    }

    public void setShopLocations(ArrayList<LongLat> shopLocations) {
        this.shopLocations = shopLocations;
    }

    public boolean isVisitFirstShopFirst() {
        return visitFirstShopFirst;
    }

    public void setVisitFirstShopFirst(boolean visitFirstShopFirst) {
        this.visitFirstShopFirst = visitFirstShopFirst;
    }
}
