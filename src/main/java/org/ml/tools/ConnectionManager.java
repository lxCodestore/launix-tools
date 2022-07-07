/*
 * The MIT License
 *
 * Copyright 2019 Dr. Matthias Laux.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.ml.tools;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dr. Matthias Laux
 */
public class ConnectionManager {

    private static ConnectionData myConnectionData = null;
    private static Connection myConnection = null;
    private static final ConnectionManager INSTANCE = new ConnectionManager();
    private final Map<String, PreparedStatement> statementCache;
    private static boolean init = false;

    /**
     *
     */
    private ConnectionManager() {
        this.statementCache = new HashMap<>();
    }

    /**
     * @param connectionData
     */
    public static void init(ConnectionData connectionData) {
        if (connectionData == null) {
            throw new IllegalArgumentException("connectionData may not be null");
        }
        myConnectionData = connectionData;
        init = true;
    }

    /**
     * @param connection
     */
    public static void init(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection may not be null");
        }
        myConnection = connection;
        init = true;
    }

    /**
     * @return
     */
    public static ConnectionManager getInstance() {
        if (!init) {
            throw new UnsupportedOperationException("ConnectionManager not yet properly initialized");
        }
        return INSTANCE;
    }

    /**
     * @return @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws InstantiationException
     */
    public Connection getConnection() throws ClassNotFoundException, IllegalAccessException, SQLException, InstantiationException {
        if (myConnection == null) {
            DriverManager.registerDriver((Driver) Class.forName(myConnectionData.getDatabaseDriver()).newInstance());
            myConnection = DriverManager.getConnection(myConnectionData.getDatabaseURL(), myConnectionData.getUsername(), myConnectionData.getPassword());
        }
        return myConnection;
    }

    /**
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        if (myConnection != null) {
            myConnection.close();
        }
    }

    /**
     * @param sql
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public PreparedStatement getStatement(String sql) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if (sql == null) {
            throw new IllegalArgumentException("sql may not be null");
        }
        if (!statementCache.containsKey(sql)) {
            statementCache.put(sql, ConnectionManager.getInstance().getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS));
        }
        return statementCache.get(sql);
    }
}
