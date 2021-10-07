package uk.ac.ed.inf;

import uk.ac.ed.inf.clients.MenuWebsiteClient;
import uk.ac.ed.inf.entities.Item;
import uk.ac.ed.inf.entities.Shop;

import java.util.ArrayList;

public class Menus {
    public static final int DELIVERY_CHARGE = 50;
    private static MenuWebsiteClient menuClient;

    public Menus(String machineName, String port) {
        menuClient = new MenuWebsiteClient(machineName, port);
    }

    public int getDeliveryCost(String... orders) {
        assert(orders.length > 0 & orders.length <= 4);
        ArrayList<Shop> shops = menuClient.getAllShopsMenus();

        int menuCost = getMenuCost(shops, orders);
        return DELIVERY_CHARGE + menuCost;
    }

    private int getMenuCost(ArrayList<Shop> shops, String[] orders) {
        int menuCost = 0;
        for (String order : orders) {
            for (Shop shop : shops) {
                for (Item item : shop.getMenu()) {
                    if (item.getItem().equals(order)) {
                        menuCost += item.getPence();
                        break;
                    }
                }
            }
        }
        return menuCost;
    }
}
