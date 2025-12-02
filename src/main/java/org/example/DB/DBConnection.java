package org.example.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


//runi had Lcommande f terminal ayy terminal makayhemch
//docker run --name my-postgres-container -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=tour_3d_db -p 5432:5432 -d postgres
//ghat9ad lik lcontainer ou database tahya
public class DBConnection {
    private static final String url = "jdbc:postgresql://localhost:5432/tour_3d_db";
    private static final String user = "user";
    private static  final String password = "password";

    public Connection getConnection(){
        try{
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Success! Connected to the database.");
            return conn;
        }catch (SQLException e){
            System.out.println("Connection Failed.");
            e.printStackTrace();
        }
        return null;
    }

    public void closeConnection(Connection conn){
        try{
            conn.close();
            System.out.println("Success! Connection closed.");
        }catch (SQLException e){
            System.out.println("Connection Failed.");
            e.printStackTrace();
        }
    }
}
