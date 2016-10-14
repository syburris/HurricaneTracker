package com.theironyard;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by stevenburris on 10/13/16.
 */
public class MainTest {
    public Connection startConnection () throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn,"Steven","pass");
        User user = Main.selectUser(conn,"Steven");
        conn.close();
        assertTrue(user != null);
    }

    @Test
    public void testHurricane() throws SQLException {
        Connection conn = startConnection();
        Main.insertHurricane(conn, "Matt", "Chuck", 2, "www.google.com", "Steven");
        Main.insertHurricane(conn, "Floyd", "NC", 3, "www.google.com", "Andy");
        Main.insertHurricane(conn, "Katrina", "Nola", 3, "www.google.com", "Sam");
        ArrayList<Hurricane> hurricanes = Main.selectHurricanes(conn,"","");
        assertTrue(!hurricanes.isEmpty());
    }

    @Test
    public void testDeleteHurricane() throws SQLException {
        Connection conn = startConnection();
        Main.insertHurricane(conn, "Matt", "Chuck", 2, "www.google.com", "Steven");
        Main.insertHurricane(conn, "Floyd", "NC", 3, "www.google.com", "Andy");
        Main.insertHurricane(conn, "Katrina", "Nola", 3, "www.google.com", "Sam");
        Main.deleteHurricane(conn,1);
        ArrayList<Hurricane> hurricanes = Main.selectHurricanes(conn,"","");
        assertTrue(hurricanes.size() == 2);
    }

    @Test
    public void testUpdateHurricane() throws SQLException {
        Connection conn = startConnection();
        Main.insertHurricane(conn, "Matt", "Chuck", 2, "www.google.com", "Steven");
        Main.insertHurricane(conn, "Floyd", "NC", 3, "www.google.com", "Andy");
        Main.insertHurricane(conn, "Katrina", "Nola", 3, "www.google.com", "Sam");
        Main.updateHurricane(conn, 1, "Alex", "NC", 4, "www.google.com");
        ArrayList<Hurricane> hurricanes = Main.selectHurricanes(conn, "", "");
        assertTrue(hurricanes.get(0).name == "Alex");
    }

}