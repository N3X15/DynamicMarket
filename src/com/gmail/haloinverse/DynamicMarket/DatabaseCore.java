package com.gmail.haloinverse.DynamicMarket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class DatabaseCore {

    public Type databaseType = null;
    public String tableName; // default: SimpleMarket
    public DynamicMarket plugin = null;
    public String engine = "MyISAM";
    public Connection conn = null;

    public DatabaseCore(Type databaseType, String tableAccessed, String thisEngine, DynamicMarket thisPlugin) {
        this.databaseType = databaseType;
        this.tableName = tableAccessed;
        if (thisEngine != null)
            engine = thisEngine;
        plugin = thisPlugin;
        initialize();
    }

    protected boolean initialize() {
        try {
            conn = connection();
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException ex) {
            logSevereException("Database connector not found for "
                    + dbTypeString(), ex);
            conn = null;
        } catch (SQLException ex) {
            logSevereException("SQL Error connecting to " + dbTypeString()
                    + "database", ex);
            conn = null;
        }

        return initialize("");
    }

    protected boolean initialize(String tableSuffix) {
        if (!(checkTable(tableSuffix))) {
            plugin.log.info("[" + plugin.name + "] Creating database.");
            if (createTable(tableSuffix)) {
                plugin.log.info("[" + plugin.name + "] Database Created.");
                return true;
            } else {
                plugin.log.severe("[" + plugin.name + "] Database creation *failed*.");
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
            plugin.log.info("[" + plugin.name + "] Database table successfully deleted.");
        } else {
            plugin.log.severe("[" + plugin.name + "] Database table could not be deleted.");
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
        if (conn != null)
            return conn;

        // CHANGED: Sets connections to auto-commit, rather than emergency
        // commit-on-close behaviour.
        Connection newConn;
        if (this.databaseType.equals(Type.SQLITE)) {
            Class.forName("org.sqlite.JDBC");
            newConn = DriverManager.getConnection(DynamicMarket.sqlite);
            return newConn;
        }

        Class.forName("com.mysql.jdbc.Driver");
        newConn = DriverManager.getConnection(DynamicMarket.mysql, DynamicMarket.mysql_user, DynamicMarket.mysql_pass);
        newConn.setAutoCommit(true);
        return newConn;
    }

    protected String dbTypeString() {
        return ((this.databaseType.equals(Type.SQLITE)) ? "sqlite" : "mysql");
    }

    protected void logSevereException(String exDesc, Exception exDetail) {
        plugin.log.severe("[" + plugin.name + "]: " + exDesc + ": " + exDetail);
    }

    protected void logSevereException(String exDesc) {
        plugin.log.severe("[" + plugin.name + "]: " + exDesc);
    }

    protected boolean checkTable(String tableSuffix) {
        SQLHandler myQuery = new SQLHandler(this);

        boolean bool;
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
