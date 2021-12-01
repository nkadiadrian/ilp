package uk.ac.ed.inf;

import uk.ac.ed.inf.clients.WebsiteClient;
import uk.ac.ed.inf.entities.Item;
import uk.ac.ed.inf.entities.Shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class for retrieving all menu items and calculating the price of delivering from the menus on the website
 */
public class Menus {
    public static final int DELIVERY_CHARGE = 50;
    private static WebsiteClient websiteClient;
    private Map<String, Item> itemMap;


    /**
     * The constructor for the class initialises the menuClient using the parameters provided
     * The menuClient is static so all instances of the Menus class have the same menuClient configured.
     *
     * @param machineName The name of the machine the website is being run on
     * @param port        The port number the website is being run on
     */
    public Menus(String machineName, String port) {
        websiteClient = new WebsiteClient(machineName, port);
        List<Shop> shopList = websiteClient.getAllShopsMenus();

        itemMap = new HashMap<>();
        for (Shop shop: shopList) {
            for (Item item: shop.getMenu()) {
                item.setLocation(shop.getLocation());
                itemMap.put(item.getItem(), item);
            }
        }
    }

    /**
     * The method takes a list of 1 to 4 items and calculates the cost of delivering the items by adding
     * the cost of the items based on their price on the website and adding the standard delivery cost
     *
     * @param items A list of strings specifying the items ordered for the delivery
     * @return The total cost of the specified items along with their delivery price
     */
    public int getDeliveryCost(List<String> items) {
        assert (items.size() > 0 & items.size() <= 4);
        int menuCost = getMenuCost(itemMap, items);
        return DELIVERY_CHARGE + menuCost;
    }

    /**
     * A helper method which calculates the price of all the items asked for from a hashmap of all the items and their
     * associated prices derived from the list of shops and their menus obtained from the website
     *
     * @param itemMap A map of all items from all the menus of the shops the drone delivers for linking each item to
     *               its price and location
     * @param items A list of the items ordered for a given delivery
     * @return The total cost of the specified items
     */
    private int getMenuCost(Map<String, Item> itemMap, List<String> items) {
        int menuCost = 0;
        for (String item : items) {
            menuCost += itemMap.get(item).getPence();
        }
        return menuCost;
    }

    public Map<String, Item> getItemMap() {
        return itemMap;
    }

    public static WebsiteClient getWebsiteClient() {
        return websiteClient;
    }
}
