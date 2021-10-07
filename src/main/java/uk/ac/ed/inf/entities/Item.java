package uk.ac.ed.inf.entities;

public class Item {
    private String item;
    private Integer pence;

    public Item(String item, Integer pence) {
        this.item = item;
        this.pence = pence;
    }

    public String getItem() {
        return item;
    }

    public Integer getPence() {
        return pence;
    }
}

