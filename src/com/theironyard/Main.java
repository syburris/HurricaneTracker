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
//        ArrayList<Hurricane> hurricanes = new ArrayList<>();

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
//                    m.put("hurricanes", hurricanes);
                    hurricaneList(conn);
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
                    ArrayList<Hurricane> hurricanes = new ArrayList<>();


                    String hName = request.queryParams("hName");
                    String hLocation = request.queryParams("hLocation");
                    int hCat = Integer.parseInt(request.queryParams("hCat"));
                    String hImage = request.queryParams("hImage");
//                    Hurricane h = new Hurricane(hName,hLocation,hImage,hCat);
//                    hurricanes.add(h);
                    insertHurricane(conn,hName,hLocation,hCat,hImage);

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
    public static void hurricaneList (Connection conn) throws SQLException {
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
        System.out.printf(String.valueOf(hurricanes));
    }
}
