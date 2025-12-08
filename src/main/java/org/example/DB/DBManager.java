package org.example.DB;

import java.sql.*;
import java.util.List;


//hadi fiha dakchi general chwia bach 7ta ila bghina nkhedmo 3la logs li knt glti ndirohom biha nit
public class DBManager {
    private DBConnection db;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement pstmt;

    public DBManager(){
        this.db = new DBConnection();
        this.conn = this.db.getConnection();
        if (this.conn == null) {
            System.err.println("⚠️ WARNING: Database connection could not be established!");
            System.err.println("⚠️ The application will continue, but database features will not work.");
            System.err.println("⚠️ Please start the Docker container and restart the application.");
        } else {
            System.out.println("✅ DBManager initialized with active database connection");
        }
    }

    public void closeConnection(){
        try {
            if(!this.conn.isClosed())
                db.closeConnection(this.conn);
        } catch (SQLException e) {
            System.out.println("✅ Database connection already closed!");
            e.printStackTrace();
        }
    }

    public void createTable(String query){
        try {
            this.stmt = this.conn.createStatement();
            this.stmt.execute(query);
            this.stmt.close();
            System.out.println("Table Created successfully!");
        } catch (SQLException e) {
            System.out.println("Error creating table!");
            e.printStackTrace();
        }
    }

    //hadik Object...args katakhed ay ra9em ta3 les Objets ou safi ghir bach had lmethod tkon general chwia
    public void insertRow(String query, Object...args){
        try{
            this.pstmt = this.conn.prepareStatement(query);
            for(int i = 0; i< args.length; i++){
                this.pstmt.setObject(i+1, args[i]);
            }
            int rowsAffected = this.pstmt.executeUpdate();
            this.pstmt.close();
            System.out.println("Success! Inserted " + rowsAffected + " row(s).");
        }catch(SQLException e){
            System.out.println("Error inserting into the table!");
            e.printStackTrace();
        }
    }

    public void bulkInsert(String query, List<String[]> data){
        try(PreparedStatement pstmt = conn.prepareStatement(query)){
            this.pstmt = pstmt;
            this.conn.setAutoCommit(false);

            for (int i = 0; i<data.getFirst().length; i++) {
                for(int j=0; j< data.size(); j++){
                    if(j==3) {
                        this.pstmt.setInt(j+1,Integer.parseInt(data.get(j)[i]));
                    }else{
                        this.pstmt.setString(j+1, data.get(j)[i]);
                    }
                }
                this.pstmt.addBatch();
            }

            int[] rowsAffected = this.pstmt.executeBatch();

            this.conn.commit();

            System.out.println("Bulk insert complete! Rows: " + rowsAffected.length);

        } catch (SQLException e) {
            try {
                this.conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error inserting.");
            e.printStackTrace();
        } finally {
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet readQuery(String query){
        try{
            if (this.stmt != null && !this.stmt.isClosed()) {
                this.stmt.close();
            }
            this.stmt = this.conn.createStatement();
            return this.stmt.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Error Reading.");
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet getPaintings(){
        try{
            if (this.stmt != null && !this.stmt.isClosed()) {
                this.stmt.close();
            }
            this.stmt = this.conn.createStatement();
            return this.stmt.executeQuery("SELECT * FROM Paintings;");
        } catch (SQLException e) {
            System.out.println("Error Reading.");
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet getPaintingById(String id){
        // ✅ Check if connection is null (database not available)
        if (this.conn == null) {
            System.err.println("❌ ERROR: Database connection is null! Cannot query painting: " + id);
            System.err.println("⚠️ Make sure Docker PostgreSQL container is running.");
            return null;
        }

        try{
            if (this.stmt != null && !this.stmt.isClosed()) {
                this.stmt.close();
            }
            this.stmt = this.conn.createStatement();
            System.out.println("🔍 Executing query: SELECT * FROM Paintings WHERE id ='" + id + "';");
            return this.stmt.executeQuery("SELECT * FROM Paintings WHERE id ='"+id+"';");
        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR reading painting with id: " + id);
            System.err.println("❌ SQLException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ UNEXPECTED ERROR querying database:");
            e.printStackTrace();
        }
        return null;
    }



    public void dropTable(String table) {
        if (!table.matches("[a-zA-Z0-9_]+")) {
            System.out.println("Invalid table name!");
            return;
        }

        String query = "DROP TABLE IF EXISTS " + table + " CASCADE;";

        try (Statement stmt = this.conn.createStatement()) {
            stmt.execute(query);
            System.out.println("Table '" + table + "' dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.out.println("Error dropping table!");
            e.printStackTrace();
        }
    }

}
