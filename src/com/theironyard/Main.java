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
        createTables(conn);


        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = selectUser(conn,name);

                    HashMap m = new HashMap();
                    if (user != null) {
                        m.put("name", user.name);
                    }
                    String filter = request.queryParams("filter");
                    ArrayList<Hurricane> hurricanes = hurricaneFilter(conn,filter,user);
                    m.put("hurricanes", hurricanes);
                    String nameFilter = request.queryParams("filter");
                    hurricaneFilter(conn, nameFilter,user);
                    return new ModelAndView(m,"home.html");

                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("userName");
                    String password = request.queryParams("password");

                    User user = selectUser(conn,name);
                    if (user == null) {
                        insertUser(conn,name,password);
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
                    User user = selectUser(conn, name);
                    if (user == null) {
                        return null;
                    }

                    String hName = request.queryParams("hName");
                    String hLocation = request.queryParams("hLocation");
                    int hCat = Integer.parseInt(request.queryParams("hCat"));
                    String hImage = request.queryParams("hImage");
                    String submitter = user.name;
                    int userID = user.id;
                    insertHurricane(conn,hName,hLocation,hCat,hImage,submitter);

                    response.redirect("/");
                    return null;
                }
        );
        Spark.post(
                "/delete-hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = selectUser(conn,name);
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
                    User user = selectUser(conn,name);
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
    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS hurricanes (id IDENTITY, name VARCHAR, location VARCHAR, image VARCHAR," +
                " category INT, submitter VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
    }
    public static void insertHurricane(Connection conn, String name, String location, int cat, String image, String submitter) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO hurricanes VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1,name);
        stmt.setString(2,location);
        stmt.setString(3,image);
        stmt.setInt(4,cat);
        stmt.setString(5,submitter);
        stmt.execute();
    }
    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }
    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1,name);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id,name,password);
        }
        return null;
    }
    public static Hurricane selectHurricanes(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users INNER JOIN hurricanes ON " +
                "hurricanes.submitter = users.name WHERE users.id = ?");
        stmt.setInt(1,id);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            String name = results.getString("hurricanes.name");
            String location = results.getString("hurricanes.location");
            String image = results.getString("hurricanes.image");
            int category = results.getInt("hurricanes.category");
            String submitter = results.getString("users.name");
            return new Hurricane(id,name,location,image,category,submitter);
        }
        return null;
    }
    public static ArrayList<Hurricane> hurricaneFilter (Connection conn, String filter, User user) throws SQLException {
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
                String submitter = results.getString("submitter");
                Hurricane hurricane = new Hurricane(id, name,location,image,cat,submitter);
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
                String submitter = results.getString("submitter");
                Hurricane hurricane = new Hurricane(id, name,location,image,cat,submitter);
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
