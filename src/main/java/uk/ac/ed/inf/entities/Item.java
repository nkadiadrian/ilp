package uk.ac.ed.inf.entities;

/**
 * An entity class used by json to serialise the items in the menu of each shop from the json file on the website server
 */
public class Item {
    private String item;
    private Integer pence;

    /**
     * @param item  the name of the item
     * @param pence the price of the item in pence
     */
    public Item(String item, Integer pence) {
        this.item = item;
        this.pence = pence;
    }

    /**
     * @return the name of the item
     */
    public String getItem() {
        return item;
    }

    /**
     * @return the price of the item in pence
     */
    public Integer getPence() {
        return pence;
    }
}

