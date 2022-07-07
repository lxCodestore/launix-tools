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

import org.jdom2.Element;

/**
 *
 * @author Dr. Matthias Laux
 */
public class ConnectionData {

    private String databaseDriver;
    private String databaseURL;
    private String username;
    private String password;

    /**
     *
     */
    public enum RequiredKey {
        databaseDriver, databaseURL, username, password
    }

    /**
     *
     * @param propertyManager
     */
    public ConnectionData(PropertyManager propertyManager) {
        if (propertyManager == null) {
            throw new NullPointerException("propertyManager may not be null");
        }
        propertyManager.validateAllPropertyNames(RequiredKey.databaseDriver);
        this.databaseDriver = propertyManager.getProperty(RequiredKey.databaseDriver.toString());
        this.databaseURL = propertyManager.getProperty(RequiredKey.databaseURL.toString());
        this.username = propertyManager.getProperty(RequiredKey.username.toString());
        this.password = propertyManager.getProperty(RequiredKey.password.toString());
    }

    /**
     *
     * @param databaseDriver
     * @param databaseURL
     * @param username
     * @param password
     */
    public ConnectionData(String databaseDriver, String databaseURL, String username, String password) {
        if (databaseDriver == null) {
            throw new IllegalArgumentException("databaseDriver may not be null");
        }
        if (databaseURL == null) {
            throw new IllegalArgumentException("databaseURL may not be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("username may not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("password may not be null");
        }

        this.databaseDriver = databaseDriver;
        this.databaseURL = databaseURL;
        this.username = username;
        this.password = password;
    }

    /**
     *
     * @param connectionDataElement
     */
    public ConnectionData(Element connectionDataElement) {
        this(new PropertyManager(connectionDataElement));
    }

    /**
     * @return the databaseDriver
     */
    public String getDatabaseDriver() {
        return databaseDriver;
    }

    /**
     * @return the databaseURL
     */
    public String getDatabaseURL() {
        return databaseURL;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
