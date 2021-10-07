package uk.ac.ed.inf.entities;

import java.util.List;

public class Shop {
    private String name;
    private String location;
    private List<Item> menu;

    public Shop(String name, String location, List<Item> menu) {
        this.name = name;
        this.location = location;
        this.menu = menu;
    }

    public List<Item> getMenu() {
        return menu;
    }
}
