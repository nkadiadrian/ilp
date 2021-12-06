package uk.ac.ed.inf.clients;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import uk.ac.ed.inf.LongLat;
import uk.ac.ed.inf.entities.web.Shop;
import uk.ac.ed.inf.entities.web.ThreeWordsDetails;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * The singular client used to access the data on the website. This contains the httpClient used to connect to the website
 * The class also contains the methods used to extract and serialise specific data files from said website
 */
public class WebsiteClient {
    // The address of the file containing the list of shops with their respective menus
    public static final String SHOPS_MENUS_DIRECTORY = "/menus/menus.json";
    public static final String NO_FLY_ZONES_DIRECTORY = "/buildings/no-fly-zones.geojson";
    public static final String DETAILS_JSON_ADDRESS = "/details.json";
    public static final String THREE_WORDS_DIRECTORY = "/words/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private String machineName;
    private String port;
    private String serverAddress;

    /**
     * The initialiser for the website client stores the server address as a single instance variable
     * to be used elsewhere in data fetching
     *
     * @param machineName The name of the machine the website is being run on
     * @param port        The port number the website is being run on
     */
    public WebsiteClient(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;
        setServerAddress();
    }

    /**
     * This acts as a general function for all get requests. It attempts to parse all requests to get data
     * from the website server directories and returns a serialisable String object to be used elsewhere.
     *
     * @param directory the location and file name from which the requested data is wanted
     * @return a string containing the raw data extracted from the specified location, or an empty string if none can be extracted
     */
    public String GET(String directory) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverAddress + directory))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (ConnectException e) {
            System.err.println("A connection to the website server with machine name: " + getMachineName() + ", and port: " + getPort() + ", could not be established");
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

    /**
     * Uses the GET method to obtain the list of shops and their menus and then serialises the response from the website
     * This provides the list of shops and therefore menu items that can be used to calculate order prices
     *
     * @return a list of all the shops and their menus available from the website.
     * An empty list is returned if no data is fetched.
     */
    public List<Shop> getAllShopsMenus() {
        String response = GET(SHOPS_MENUS_DIRECTORY);
        Type shopsType = new TypeToken<List<Shop>>() {
        }.getType();
        return gson.fromJson(response, shopsType);
    }

    public FeatureCollection getNoFlyZone() {
        String response = GET(NO_FLY_ZONES_DIRECTORY);
        return FeatureCollection.fromJson(response);
    }

    public LongLat getLongLatFromLocationWord(String word) {
        String wordsDirectory = word.replace('.', '/');
        String response = GET(THREE_WORDS_DIRECTORY + wordsDirectory + DETAILS_JSON_ADDRESS);
        ThreeWordsDetails threeWordsDetails = gson.fromJson(response, ThreeWordsDetails.class);
        return threeWordsDetails.getLongLat();
    }

    /**
     * @return the name of the machine used for connecting to the hosting website server
     */
    public String getMachineName() {
        return machineName;
    }

    /**
     * Sets the machine name but also adjusts the serverAddress to use the new machine name
     *
     * @param machineName the name of the machine used for connecting to the hosting website server
     */
    public void setMachineName(String machineName) {
        this.machineName = machineName;
        setServerAddress();
    }

    /**
     * @return the port on the machine used for connecting to the hosting website server
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the port name but also adjusts the serverAddress to use the new port specified
     *
     * @param port the port on the machine used for connecting to the hosting website server
     */
    public void setPort(String port) {
        this.port = port;
        setServerAddress();
    }

    /**
     * Helper method that uses the current machineName and port to set a singular variable
     * storing the full server address to be used to elsewhere
     */
    private void setServerAddress() {
        this.serverAddress = "http://" + machineName + ":" + port;
    }
}
