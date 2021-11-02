package uk.ac.ed.inf;

import uk.ac.ed.inf.clients.MenuWebsiteClient;
import uk.ac.ed.inf.entities.Item;
import uk.ac.ed.inf.entities.Shop;

import java.util.ArrayList;

/**
 * A class for retrieving all menu items and calculating the price of delivering from the menus on the website
 */
public class Menus {
    public static final int DELIVERY_CHARGE = 50;
    private static MenuWebsiteClient menuClient;

    /**
     * The constructor for the class initialises the menuClient using the parameters provided
     * The menuClient is static so all instances of the Menus class have the same menuClient configured.
     *
     * @param machineName The name of the machine the website is being run on
     * @param port        The port number the website is being run on
     */
    public Menus(String machineName, String port) {
        menuClient = new MenuWebsiteClient(machineName, port);
    }

    /**
     * The method takes a list of 1 to 4 items and calculates the cost of delivering the items by adding
     * the cost of the items based on their price on the website and adding the standard delivery cost
     *
     * @param items A list of strings specifying the items ordered for the delivery
     * @return The total cost of the specified items along with their delivery price
     */
    public int getDeliveryCost(String... items) {
        assert (items.length > 0 & items.length <= 4);
        ArrayList<Shop> shops = menuClient.getAllShopsMenus();
        for (Shop shop : shops) {
            System.out.println(shop.getMenu().get(0).getItem());
        }

        int menuCost = getMenuCost(shops, items);
        return DELIVERY_CHARGE + menuCost;
    }

    /**
     * A helper method which calculates the price of all the items asked for from the list of shops and their menus
     * obtained from the website
     *
     * @param shops A list of all the shops the drone delivers for which contains the menus of all the shops
     * @param items A list of the items ordered for a given delivery
     * @return The total cost of the specified items
     */
    private int getMenuCost(ArrayList<Shop> shops, String[] items) {
        int menuCost = 0;
        for (String order : items) {
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
