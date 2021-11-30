package uk.ac.ed.inf.clients;

import uk.ac.ed.inf.LongLat;
import uk.ac.ed.inf.Menus;
import uk.ac.ed.inf.entities.Order;

import java.sql.*;
import java.util.HashMap;

public class DatabaseClient {
    public static final String DATABASE_NAME = "/derbyDB";

    private final String machineName;
    private final String port;

    private Connection conn;
    private String jdbcString;

    public DatabaseClient(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;
        setJdbcString();
        try {
            this.conn = DriverManager.getConnection(jdbcString);
        } catch (SQLException e) {
            System.err.println("Can't create a connection to the database");
        }
        initialiseDeliveriesTable();
        initialiseFlightPathTable();
    }

    public HashMap<String, Order> getOrdersByDate(Date date, Menus menus) {
        HashMap<String, Order> orders = new HashMap<>();
        try {
            final String coursesQuery =
                    "select o.ORDERNO, ITEM, DELIVERTO from ORDERDETAILS join (select * from ORDERS where deliveryDate=(?)) o on ORDERDETAILS.orderNo=o.ORDERNO";
            PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, String.valueOf(date));
            ResultSet rs = psCourseQuery.executeQuery();

            while (rs.next()) {
                String orderNo = rs.getString("ORDERNO");
                if (!orders.containsKey(orderNo)) {
                    LongLat deliverTo = Menus.getMenuClient().getLongLatFromLocationWord(rs.getString("DELIVERTO"));
                    orders.put(orderNo, new Order(orderNo, deliverTo));
                }
                orders.get(orderNo).addItem(rs.getString("ITEM"), new LongLat(1, 2));
            }
            for (Order order: orders.values()) {
                order.setDeliveryCost(menus.getDeliveryCost(order.getItems().toArray(new String[4])));
            }
        } catch (SQLException e) {
            System.err.println("Could not retrieve the order information");
        }

//        Menus menus = new Menus(machineName, port);
//        for (Order order: orders.values()) {
//            String[] orderItems = order.getItems().toArray(new String[0]);
//            order.setDeliveryCost(menus.getDeliveryCost(orderItems));
//        }

        return orders;
    }

    private void initialiseDeliveriesTable() {
        try {
            Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet resultSet =
                    databaseMetaData.getTables(null, null, "DELIVERIES", null);
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }
            statement.execute("create table deliveries(orderNo char(8),\n" +
                    "deliveredTo varchar(19),\n" +
                    "costInPence int)\n");
        } catch (SQLException e) {
            System.err.println("Error initialising the deliveries table");
        }
    }

    private void initialiseFlightPathTable() {
        try {
            Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet resultSet =
                    databaseMetaData.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }
            statement.execute("create table flightpath(orderNo char(8),\n" +
                    "fromLongitude double,\n" +
                    "fromLatitude double,\n" +
                    "angle integer,\n" +
                    "toLongitude double,\n" +
                    "toLatitude double)\n");
        } catch (SQLException e) {
            System.err.println("Error initialising the flightpath table");
        }
    }

    private void setJdbcString() {
        this.jdbcString = "jdbc:derby://" + machineName + ":" + port + DATABASE_NAME;
    }

}
