package uk.ac.ed.inf;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Item> getMenu() {
        return menu;
    }

    public void setMenu(List<Item> menu) {
        this.menu = menu;
    }
}
