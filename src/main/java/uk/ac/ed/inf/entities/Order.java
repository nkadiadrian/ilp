package uk.ac.ed.inf.entities;

import java.util.ArrayList;

public class Order {
    private String deliverTo;
    private int deliveryCost;
    private ArrayList<String> shopLocations;
    private ArrayList<String> items;

    public Order(String deliverTo) {
        this.deliverTo = deliverTo;
        this.items = new ArrayList<>();
    }

    public void addItem(String item) {
        items.add(item);
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public void setDeliverTo(String deliverTo) {
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
}
