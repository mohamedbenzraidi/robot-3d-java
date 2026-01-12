package org.example.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


//runi had Lcommande f terminal ayy terminal makayhemch
//docker run --name my-postgres-container -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=tour_3d_db -p 5434:5432 -d postgres
//ghat9ad lik lcontainer ou database tahya
public class DBConnection {
    private static final String url = "jdbc:postgresql://localhost:5434/tour_3d_db";
    private static final String user = "user";
    private static final String password = "password";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public Connection getConnection(){
        Connection conn = null;
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                attempts++;
                System.out.println("🔌 Attempting database connection (attempt " + attempts + "/" + MAX_RETRIES + ")...");
                conn = DriverManager.getConnection(url, user, password);
                System.out.println("✅ SUCCESS! Connected to database: " + url);
                return conn;
            } catch (SQLException e) {
                System.err.println("❌ Connection attempt " + attempts + " failed: " + e.getMessage());

                if (attempts < MAX_RETRIES) {
                    System.out.println("⏳ Retrying in " + RETRY_DELAY_MS + "ms...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    System.err.println("❌ CRITICAL: All " + MAX_RETRIES + " connection attempts failed!");
                    System.err.println("❌ ERROR: " + e.getMessage());
                    System.err.println("\n⚠️  TROUBLESHOOTING STEPS:");
                    System.err.println("1. Check if Docker is running:");
                    System.err.println("   docker ps");
                    System.err.println("\n2. If container doesn't exist, create it:");
                    System.err.println("   docker run --name my-postgres-container -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=tour_3d_db -p 5432:5432 -d postgres");
                    System.err.println("\n3. If container exists but is stopped, start it:");
                    System.err.println("   docker start my-postgres-container");
                    System.err.println("\n4. Check if port 5432 is available:");
                    System.err.println("   netstat -an | findstr 5432  (Windows)");
                    System.err.println("   lsof -i :5432  (Mac/Linux)");
                    System.err.println("\n5. View container logs:");
                    System.err.println("   docker logs my-postgres-container\n");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void closeConnection(Connection conn){
        if (conn == null) {
            System.out.println("⚠️ Cannot close connection: Connection is null");
            return;
        }

        try {
            if (!conn.isClosed()) {
                conn.close();
                System.out.println("✅ Connection closed successfully.");
            } else {
                System.out.println("⚠️ Connection was already closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing connection:");
            e.printStackTrace();
        }
    }

    /**
     * Test if database connection is available
     */
    public boolean testConnection() {
        Connection conn = getConnection();
        if (conn != null) {
            closeConnection(conn);
            return true;
        }
        return false;
    }
}