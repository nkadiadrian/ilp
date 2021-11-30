package uk.ac.ed.inf.clients;

import uk.ac.ed.inf.LongLat;
import uk.ac.ed.inf.Menus;
import uk.ac.ed.inf.Move;
import uk.ac.ed.inf.entities.Item;
import uk.ac.ed.inf.entities.Order;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
                    String whatThreeWordsDeliverTo = rs.getString("DELIVERTO");
                    LongLat deliverTo = Menus.getMenuClient().getLongLatFromLocationWord(whatThreeWordsDeliverTo);
                    orders.put(orderNo, new Order(orderNo, deliverTo, whatThreeWordsDeliverTo));
                }

                String item = rs.getString("ITEM");
                LongLat itemLocation = Menus.getMenuClient().getLongLatFromLocationWord(menus.getItemMap().get(item).getLocation());
                orders.get(orderNo).addItem(item, itemLocation); // TODO: Add Item coordinate locations
            }
            for (Order order: orders.values()) {
                order.setDeliveryCost(menus.getDeliveryCost(order.getItems()));
            }
        } catch (SQLException e) {
            System.err.println("Could not retrieve the order information");
        }

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

    public void writeAllDeliveriesToTable(List<Order> fulfilledDeliveries) {
        try {
            conn.setAutoCommit(false);
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO deliveries (orderNo, deliveredTo, costInPence) VALUES (?,?,?)");
            for (Order order: fulfilledDeliveries) {
                preparedStatement.setString(1, order.getOrderNo());
                preparedStatement.setString(2, order.getThreeWordsDeliverTo());
                preparedStatement.setInt(3, order.getDeliveryCost());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Error writing to the deliveries table");
            System.err.println(e.getMessage());
        }
    }

    public void writeAllMovesToTable(List<Move> moves) {
        try {
            conn.setAutoCommit(false);
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO flightpath (orderNo, fromLongitude, fromLatitude, angle, toLongitude, toLatitude) VALUES (?,?,?,?,?,?)");
            for (Move move: moves) {
                preparedStatement.setString(1, move.getOrderNo());
                preparedStatement.setDouble(2, move.getFromLongitude());
                preparedStatement.setDouble(3, move.getFromLatitude());
                preparedStatement.setInt(4, move.getAngle());
                preparedStatement.setDouble(5, move.getToLongitude());
                preparedStatement.setDouble(6, move.getToLatitude());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Error writing to the flightpath table");
            System.err.println(e.getMessage());
        }
    }

    // TODO: Write everything to databases

    private void setJdbcString() {
        this.jdbcString = "jdbc:derby://" + machineName + ":" + port + DATABASE_NAME;
    }

}
