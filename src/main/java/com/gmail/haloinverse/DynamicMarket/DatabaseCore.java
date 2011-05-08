package com.gmail.haloinverse.DynamicMarket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;

public abstract class DatabaseCore {

    public Type databaseType = null;
    public String tableName; // default: SimpleMarket
    public DynamicMarket plugin = null;
    public String engine = "MyISAM";
    private static Connection conn = null;

    public DatabaseCore(Type databaseType, String tableAccessed, String thisEngine, DynamicMarket thisPlugin) {
        this.databaseType = databaseType;
        this.tableName = tableAccessed;
        if (thisEngine != null)
            engine = thisEngine;
        plugin = thisPlugin;
        initialize();
    }

    protected boolean initialize() {
        return initialize("");
    }
    
    protected boolean initialize(String tableSuffix) {
        if (!(checkTable(tableSuffix))) {
            DynamicMarket.log.info("[" + DynamicMarket.name + "] Creating database.");
            if (createTable(tableSuffix)) {
                DynamicMarket.log.info("[" + DynamicMarket.name + "] Database Created.");
                return true;
            } else {
                DynamicMarket.log.severe("[" + DynamicMarket.name + "] Database creation *failed*.");
                return false;
            }
        }
        return false;
    }

    protected boolean deleteDatabase() {
        return deleteDatabase("");
    }

    protected boolean deleteDatabase(String tableSuffix) {
        SQLHandler myQuery = new SQLHandler(this);
        myQuery.executeStatement("DROP TABLE " + tableName + tableSuffix + ";");
        myQuery.close();
        
        if (myQuery.isOK) {
            DynamicMarket.log.info("[" + DynamicMarket.name + "] Database table successfully deleted.");
        } else {
            DynamicMarket.log.severe("[" + DynamicMarket.name + "] Database table could not be deleted.");
        }

        return myQuery.isOK;
    }

    public boolean resetDatabase() {
        return resetDatabase("");
    }

    public boolean resetDatabase(String tableSuffix) {
        deleteDatabase(tableSuffix);
        return initialize(tableSuffix);
    }

    protected Connection connection() throws ClassNotFoundException, SQLException {
//    	DynamicMarket.log.info("connection: " +
//    			               "null? " + ((DatabaseCore.conn == null)?"true":"false") + 
//    			               " isClosed? " + (((DatabaseCore.conn != null) && DatabaseCore.conn.isClosed())?"true":"false"));
//    	new Throwable().printStackTrace();
        
        if ( DynamicMarket.debug ) DynamicMarket.log.info("DatabaseCore:connection() called");
    	
        if ( (DatabaseCore.conn != null) && (!DatabaseCore.conn.isClosed()) ){
            boolean bad = false;
            SQLWarning sw = conn.getWarnings();
            while ( sw != null ) {
                bad = true;
                DynamicMarket.log.info("leftover warning: "+sw.getMessage());
                sw = sw.getNextWarning();
            }
            if ( bad ) {
                if ( DynamicMarket.debug ) DynamicMarket.log.info("DatabaseCore:connection() not re-using connection");
                conn = null;
            } else {
                if ( DynamicMarket.debug ) DynamicMarket.log.info("DatabaseCore:connection() returning last connection");
                return DatabaseCore.conn;
            }
        }
        
        if ( DynamicMarket.debug ) DynamicMarket.log.info("DatabaseCore:connection() new connection");
        
        // CHANGED: Sets connections to auto-commit, rather than emergency
        // commit-on-close behaviour.
        Connection newConn;
        
        if (this.databaseType.equals(Type.SQLITE)) {
            Class.forName("org.sqlite.JDBC");
            newConn = DriverManager.getConnection(DynamicMarket.sqlite);
            newConn.setAutoCommit(true);
            DatabaseCore.conn = newConn;
            return DatabaseCore.conn;
        }

        Class.forName("com.mysql.jdbc.Driver");
        newConn = DriverManager.getConnection(DynamicMarket.mysql, DynamicMarket.mysql_user, DynamicMarket.mysql_pass);
        newConn.setAutoCommit(false);
        DatabaseCore.conn = newConn;
        return DatabaseCore.conn;
    }

    protected String dbTypeString() {
        return ((this.databaseType.equals(Type.SQLITE)) ? "sqlite" : "mysql");
    }

    protected void logSevereException(String exDesc, Exception exDetail) {
        DynamicMarket.log.severe("[" + DynamicMarket.name + "]: " + exDesc + ": " + exDetail);
    }

    protected void logSevereException(String exDesc) {
        DynamicMarket.log.severe("[" + DynamicMarket.name + "]: " + exDesc);
    }

    protected boolean checkTable(String tableSuffix) {
        boolean bool;
        SQLHandler myQuery = new SQLHandler(this);
        
        bool = myQuery.checkTable(tableName + tableSuffix);
        myQuery.close();
        return bool;
    }

    protected boolean checkTable() {
        return checkTable("");
    }

    protected abstract boolean createTable(String tableSuffix);

    protected boolean createTable() {
        return createTable("");
    }

    public abstract boolean add(Object newObject);

    public abstract boolean update(Object updateRef);

    public abstract boolean remove(ItemClump removed);

    public abstract ArrayList<?> list(int pageNum);

    public abstract Object data(ItemClump thisItem);

    public static enum Type {

        SQLITE, MYSQL, FLATFILE;
    }
}
