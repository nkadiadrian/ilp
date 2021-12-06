package uk.ac.ed.inf.clients;

import uk.ac.ed.inf.LongLat;
import uk.ac.ed.inf.Menus;
import uk.ac.ed.inf.entities.db.Move;
import uk.ac.ed.inf.entities.db.Order;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class DatabaseClient {
    public static final String DATABASE_NAME = "/derbyDB";
    public static final String GET_ORDERS_ON_DATE_QUERY = "select o.ORDERNO, ITEM, DELIVERTO from ORDERDETAILS join (select * from ORDERS where deliveryDate=(?)) o on ORDERDETAILS.orderNo=o.ORDERNO";
    public static final String ADD_ORDER_QUERY = "INSERT INTO deliveries (orderNo, deliveredTo, costInPence) VALUES (?,?,?)";
    public static final String ADD_MOVE_QUERY = "INSERT INTO flightpath (orderNo, fromLongitude, fromLatitude, angle, toLongitude, toLatitude) VALUES (?,?,?,?,?,?)";
    public static final String CREATE_DELIVERIES_TABLE_QUERY = "create table deliveries(orderNo char(8), deliveredTo varchar(19), costInPence int)";
    public static final String DROP_DELIVERIES_TABLE_QUERY = "drop table deliveries";
    public static final String DROP_FLIGHTPATH_TABLE_QUERY = "drop table flightpath";
    public static final String CREATE_FLIGHTPATH_TABLE_QUERY = "create table flightpath(orderNo char(8), fromLongitude double, fromLatitude double, angle integer,toLongitude double, toLatitude double)";

    private final String machineName;
    private final String port;

    private String jdbcString;

    public DatabaseClient(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;
        setJdbcString();
        initialiseDeliveriesTable();
        initialiseFlightPathTable();
    }

    public HashMap<String, Order> getOrdersByDate(Date date, Menus menus) {
        HashMap<String, Order> orders = new HashMap<>();
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            PreparedStatement psOrdersQuery = conn.prepareStatement(GET_ORDERS_ON_DATE_QUERY);
            psOrdersQuery.setString(1, String.valueOf(date));
            ResultSet rs = psOrdersQuery.executeQuery();

            while (rs.next()) {
                String orderNo = rs.getString("ORDERNO");
                if (!orders.containsKey(orderNo)) {
                    String whatThreeWordsDeliverTo = rs.getString("DELIVERTO");
                    LongLat deliverTo = Menus.getWebsiteClient().getLongLatFromLocationWord(whatThreeWordsDeliverTo);
                    orders.put(orderNo, new Order(orderNo, deliverTo, whatThreeWordsDeliverTo));
                }

                String item = rs.getString("ITEM");
                LongLat itemLocation = Menus.getWebsiteClient().getLongLatFromLocationWord(menus.getItemMap().get(item).getLocation());
                orders.get(orderNo).addItem(item, itemLocation);
            }
            for (Order order : orders.values()) {
                order.setDeliveryCost(menus.getDeliveryCost(order.getItems()));
            }
            rs.close();
            psOrdersQuery.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Could not retrieve the order information");
            System.err.println(e.getMessage());
        }
        return orders;
    }

    public void writeAllDeliveriesToTable(List<Order> fulfilledDeliveries) {
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            conn.setAutoCommit(false);
            PreparedStatement preparedStatement = conn.prepareStatement(ADD_ORDER_QUERY);
            for (Order order : fulfilledDeliveries) {
                preparedStatement.setString(1, order.getOrderNo());
                preparedStatement.setString(2, order.getThreeWordsDeliverTo());
                preparedStatement.setInt(3, order.getDeliveryCost());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error writing to the deliveries table");
            System.err.println(e.getMessage());
        }
    }

    public void writeAllMovesToTable(List<Move> moves) {
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            conn.setAutoCommit(false);
            PreparedStatement preparedStatement = conn.prepareStatement(ADD_MOVE_QUERY);
            for (Move move : moves) {
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
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error writing to the flightpath table");
            System.err.println(e.getMessage());
        }
    }

    private void initialiseDeliveriesTable() {
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet rs =
                    databaseMetaData.getTables(null, null, "DELIVERIES", null);
            if (rs.next()) {
                statement.execute(DROP_DELIVERIES_TABLE_QUERY);
            }
            statement.execute(CREATE_DELIVERIES_TABLE_QUERY);
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error initialising the deliveries table");
        }
    }

    private void initialiseFlightPathTable() {
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet rs =
                    databaseMetaData.getTables(null, null, "FLIGHTPATH", null);
            if (rs.next()) {
                statement.execute(DROP_FLIGHTPATH_TABLE_QUERY);
            }
            statement.execute(CREATE_FLIGHTPATH_TABLE_QUERY);
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error initialising the flightpath table");
        }
    }

    private void setJdbcString() {
        this.jdbcString = "jdbc:derby://" + machineName + ":" + port + DATABASE_NAME;
    }
}
