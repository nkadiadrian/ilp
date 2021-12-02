package uk.ac.ed.inf.entities;


import java.util.List;

/**
 * An entity class used by gson to serialise the list of shops from the json file on the website server
 */
public class Shop { //TODO: PARSE THIS AS HASHMAP INSTEAD
    private final String name;
    private final String location;
    private final List<Item> menu;

    /**
     * @param name     the name of the shop
     * @param location the location of the shop
     * @param menu     the menu of the shop
     */
    public Shop(String name, String location, List<Item> menu) {
        this.name = name;
        this.location = location;
        this.menu = menu;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    /**
     * @return the menu of the given shop
     */
    public List<Item> getMenu() {
        return menu;
    }
}
