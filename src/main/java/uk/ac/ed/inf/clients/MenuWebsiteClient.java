package uk.ac.ed.inf.clients;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uk.ac.ed.inf.entities.Shop;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class MenuWebsiteClient {
    public static final String SHOPS_MENUS_DIRECTORY = "/menus/menus.json";
    private String machineName;
    private String port;
    private String serverAddress;
    private static HttpClient httpClient = HttpClient.newHttpClient();

    public MenuWebsiteClient(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;
        this.serverAddress = "http://" + machineName + ":" + port;
    }

    public String GET(String directory) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverAddress + directory))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (ConnectException e) {
            System.err.println("A connection to the website server with machine name: " + machineName + ", and port: " + port + ", could not be established");
            System.exit(1);
        } catch (MalformedURLException e) {
            System.err.println("The URL is malformed, can't access resource");
        } catch (IOException e) {
            System.err.println("There has been an I/O problem whilst attempting to retrieve the website resource");
        } catch (InterruptedException e) {
            System.err.println("The application has been interrupted");
        }
        return "";
    }

    public ArrayList<Shop> getAllShopsMenus() {
        String response = GET(SHOPS_MENUS_DIRECTORY);
        Type shopsType = new TypeToken<ArrayList<Shop>>() {
        }.getType();
        return new Gson().fromJson(response, shopsType);
    }
}
