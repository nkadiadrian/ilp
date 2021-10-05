package uk.ac.ed.inf;

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

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getPence() {
        return pence;
    }

    public void setPence(Integer pence) {
        this.pence = pence;
    }
}

