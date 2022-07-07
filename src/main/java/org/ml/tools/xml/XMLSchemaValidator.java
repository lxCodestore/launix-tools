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
package org.ml.tools.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Validator for XML documents using XML schema
 *
 * @author Dr. Matthias Laux
 */
public class XMLSchemaValidator extends DefaultHandler {

    private String error = null;

    /**
     * @param xmlRoot
     * @param xsdSource
     * @return
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public boolean validate(Element xmlRoot, Source xsdSource) throws IOException, SAXException {
        if (xmlRoot == null) {
            throw new IllegalArgumentException("xmlRoot may not be null");
        }
        JDOMSource xmlSource = new JDOMSource(xmlRoot);
        return validate(xmlSource, xsdSource);
    }

    /**
     * @param xmlRoot
     * @param xsdFile
     * @return
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public boolean validate(Element xmlRoot, String xsdFile) throws IOException, SAXException {
        if (xmlRoot == null) {
            throw new IllegalArgumentException("xmlRoot may not be null");
        }
        JDOMSource xmlSource = new JDOMSource(xmlRoot);
        return validate(xmlSource, xsdFile);
    }

    /**
     * @param xmlFile
     * @param xsdSource
     * @return
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public boolean validate(File xmlFile, Source xsdSource) throws IOException, SAXException {
        if (xmlFile == null) {
            throw new IllegalArgumentException("xmlFile may not be null");
        }
        InputSource xmlSource = new InputSource(new BufferedReader(new FileReader(xmlFile)));
        return validate(new SAXSource(xmlSource), xsdSource);
    }

    /**
     *
     */
    public void reset() {
        error = null;
    }

    /**
     * The actual validation method. If validation is not successful, the errors
     * found can be retrieved using the {@link #getError()} method.
     * <p>
     * Note: this approach seems to be unable to resolve XSD includes ... use
     * the other validate() method that takes the XSD file name as input instead
     *
     * @param xmlSource
     * @param xsdSource
     * @return
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public boolean validate(Source xmlSource, Source xsdSource) throws IOException, SAXException {
        if (xmlSource == null) {
            throw new IllegalArgumentException("xmlSource may not be null");
        }
        if (xsdSource == null) {
            throw new IllegalArgumentException("xsdSource may not be null");
        }

        //.... Create a validator
        SchemaFactory factory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(xsdSource);
        Validator validator = schema.newValidator();

        validator.setErrorHandler(this);

        //.... Try to validate the XML data given
        validator.validate(xmlSource);

        return getError() == null;

    }

    /**
     * The actual validation method. If validation is not successful, the errors
     * found can be retrieved using the {@link #getError()} method.
     *
     * @param xmlSource
     * @param xsdFile
     * @return
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public boolean validate(Source xmlSource, String xsdFile) throws IOException, SAXException {
        if (xmlSource == null) {
            throw new IllegalArgumentException("xmlSource may not be null");
        }
        if (xsdFile == null) {
            throw new IllegalArgumentException("xsdFile may not be null");
        }

        //.... Create a validator
        SchemaFactory factory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        ClassLoader loader = this.getClass().getClassLoader();
        URL url = loader.getResource(xsdFile);
        Schema schema = factory.newSchema(url);
        Validator validator = schema.newValidator();

        validator.setErrorHandler(this);

        //.... Try to validate the XML data given
        validator.validate(xmlSource);

        return getError() == null;

    }

    //-----------------------------------------------------------------------------------------
    // Below are helper methods that are required to improve the error handling (specifically,
    // to make sure all thrown exceptions are reported, and row and column numbers are added
    // to the output for better debugging
    //-----------------------------------------------------------------------------------------

    /**
     * Retrieve the error message set by the <code>.ErrorHandler</code> methods.
     * If no error has been found, <code>null</code> is returned.
     * <p>
     *
     * @return A string describing the error encountered
     */
    public String getError() {
        return error;
    }

    /**
     * A method required by the <code>.ErrorHandler</code> interface
     * <p>
     *
     * @param ex A parsing exception
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void warning(SAXParseException ex) {
        getError("Warning", ex);
    }

    /**
     * A method required by the <code>.ErrorHandler</code> interface
     * <p>
     *
     * @param ex A parsing exception
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void error(SAXParseException ex) {
        getError("Error", ex);
    }

    /**
     * A method required by the <code>.ErrorHandler</code> interface
     * <p>
     *
     * @param ex A parsing exception
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void fatalError(SAXParseException ex) {
        getError("Fatal Error", ex);
    }

    /**
     * A helper method for formatting
     */
    private void getError(String type, SAXParseException ex) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        if (ex == null) {
            throw new NullPointerException("ex may not be null");
        }

        StringBuilder out = new StringBuilder(200);

        out.append(type);

        String systemId = ex.getSystemId();

        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            out.append(systemId);
        }
        out.append(": Row ");
        out.append(ex.getLineNumber());
        out.append(" /`Col ");
        out.append(ex.getColumnNumber());
        out.append(": ");
        out.append(ex.getMessage());

        //.... There may be multiple exceptions thrown, we don't want to miss any information
        if (error == null) {
            error = out.toString();
        } else {
            error += "\n" + out.toString();
        }

    }
}
