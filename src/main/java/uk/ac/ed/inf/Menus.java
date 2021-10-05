package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class Menus {
    private String machineName;
    private String port;
    private static HttpClient httpClient = HttpClient.newHttpClient();

    public Menus(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;
    }

    public int getDeliveryCost (String... orders) {

        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:9898/menus/menus.json"))
                .uri(URI.create("http://" + machineName + ":" + port + "/menus/menus.json"))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }

        System.out.println(response.statusCode());
        System.out.println(response.headers());
        System.out.println(response.body());

        Type shopsType = new TypeToken<ArrayList<Shop>>() {}.getType();
        ArrayList<Shop> shops = new Gson().fromJson(response.body(), shopsType);
        response.body();

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

        return 50 + menuCost;
    }
}
