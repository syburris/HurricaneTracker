package com.theironyard;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {


    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTable(conn);

        HashMap<String, User> users = new HashMap<>();

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);

                    HashMap m = new HashMap();
                    if (user != null) {
                        m.put("name", user.name);
                    }
                    String filter = request.queryParams("filter");
                    ArrayList<Hurricane> hurricanes = selectHurricane(conn,filter);
                    m.put("hurricanes", hurricanes);
                    String nameFilter = request.queryParams("filter");
                    selectHurricane(conn, nameFilter);
                    return new ModelAndView(m,"home.html");

                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("userName");
                    String password = request.queryParams("password");

                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name, password);
                        users.put(name, user);
                    }
                    else if (!password.equals(user.password)) {
                        response.redirect("/");
                        return null;
                    }

                    Session session = request.session();
                    session.attribute("userName", name);
                    response.redirect("/");

                    return null;
                }
        );

        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();

                    response.redirect("/");
                    return null;
                }
        );
        Spark.post(
                "/create-hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    if (user == null) {
                        return null;
                    }

                    String hName = request.queryParams("hName");
                    String hLocation = request.queryParams("hLocation");
                    int hCat = Integer.parseInt(request.queryParams("hCat"));
                    String hImage = request.queryParams("hImage");
                    insertHurricane(conn,hName,hLocation,hCat,hImage);

                    response.redirect("/");
                    return null;
                }
        );
        Spark.post(
                "/delete-hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    if (user == null) {
                        return null;
                    }
                    int hurricane = Integer.parseInt(request.queryParams("hurricaneToDelete"));
                    deleteHurricane(conn,hurricane);
                    response.redirect("/");
                    return null;
                }
        );
        Spark.post(
                "/edit-hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    if (user == null) {
                        return null;
                    }
                    int id = Integer.parseInt(request.queryParams("hurricaneToEdit"));
                    String hName = request.queryParams("newName");
                    String hLocation = request.queryParams("newLocation");
                    int hCat = Integer.parseInt(request.queryParams("newCat"));
                    String hImage = request.queryParams("newImage");
                    updateHurricane(conn, id, hName, hLocation, hCat, hImage);
                    response.redirect("/");
                    return null;

                }
        );
    }
    public static void createTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS hurricanes (id IDENTITY, name VARCHAR, location VARCHAR, image VARCHAR," +
                " category INT)");
    }
    public static void insertHurricane(Connection conn, String name, String location, int cat, String image) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO hurricanes VALUES (NULL, ?, ?, ?, ?)");
        stmt.setString(1,name);
        stmt.setString(2,location);
        stmt.setString(3,image);
        stmt.setInt(4,cat);
        stmt.execute();
    }
    public static ArrayList<Hurricane> selectHurricane (Connection conn, String filter) throws SQLException {
        if (filter != null && !filter.isEmpty()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM hurricanes WHERE name = ?");
            stmt.setString(1,filter);
            ResultSet results = stmt.executeQuery();
            ArrayList<Hurricane> hurricanes = new ArrayList<>();
            while (results.next()) {
                int id = results.getInt("id");
                String name = results.getString("name");
                String location = results.getString("location");
                String image = results.getString("image");
                int cat = results.getInt("category");
                Hurricane hurricane = new Hurricane(id, name,location,image,cat);
                hurricanes.add(hurricane);
            }
            return hurricanes;
        }
        else {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM hurricanes");
            ResultSet results = stmt.executeQuery();
            ArrayList<Hurricane> hurricanes = new ArrayList<>();
            while (results.next()) {
                int id = results.getInt("id");
                String name = results.getString("name");
                String location = results.getString("location");
                String image = results.getString("image");
                int cat = results.getInt("category");
                Hurricane hurricane = new Hurricane(id, name,location,image,cat);
                hurricanes.add(hurricane);
            }
            return hurricanes;
        }

    }
    public static void deleteHurricane(Connection conn, int hurricane) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM hurricanes WHERE id = ?");
        stmt.setInt(1,hurricane);
        stmt.execute();
    }
    public static void updateHurricane(Connection conn, int id, String name, String location, int category, String image) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE hurricanes SET name = ?, location = ?, image = ?, category = ? WHERE id = ?");

        stmt.setString(1,name);
        stmt.setString(2,location);
        stmt.setString(3,image);
        stmt.setInt(4,category);
        stmt.setInt(5,id);
        stmt.execute();
    }
}
