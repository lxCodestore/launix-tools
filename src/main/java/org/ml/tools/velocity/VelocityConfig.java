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
package org.ml.tools.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.ml.tools.Namespace;
import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A target for multiple output files using the same template
 *
 * @author mlaux
 */
public class VelocityConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(VelocityConfig.class.getName());
    private Template template;

    /**
     *
     */
    public enum RequiredKey {
        templateName
    }

    /**
     *
     */
    public enum OptionalKey {
        templateDirectory, inputEncoding, templateEncoding
    }

    /**
     * @param propertyManager
     * @throws Exception
     */
    public VelocityConfig(PropertyManager propertyManager) throws Exception {
        this(PropertyManager.DEFAULT_NAMESPACE, propertyManager);
    }

    /**
     * @param namespace
     * @param propertyManager
     */
    public VelocityConfig(Namespace namespace, PropertyManager propertyManager) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }

        //.... We need at least the template name
        propertyManager.validateAllPropertyNames(namespace, RequiredKey.templateName);
        String templateName = propertyManager.getProperty(namespace, RequiredKey.templateName);
        LOGGER.log(Level.INFO, "Template name: {0}", templateName);

        //.... Get an engine and set it up for templates in separate files and accessible via the classpath 
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file, class");
        //.... This is not set as default value in Velocity, so we need to specify the class name for the classpath loader
        velocityEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        //.... If a path has been specified, use that to configure loading via the File class loader
        if (propertyManager.containsProperty(namespace, OptionalKey.templateDirectory)) {
            String templateDirectory = propertyManager.getProperty(namespace, OptionalKey.templateDirectory);
            velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templateDirectory);
            LOGGER.log(Level.INFO, "Setting template directory for file loader to {0}", templateDirectory);
        }

        //.... Override default (UTF-8) if desired
        if (propertyManager.containsProperty(namespace, OptionalKey.inputEncoding)) {
            velocityEngine.setProperty(RuntimeConstants.INPUT_ENCODING, propertyManager.getProperty(namespace, OptionalKey.inputEncoding));
        }

        //.... Override default (UTF-8) if desired for the template encoding
        String templateEncoding = RuntimeConstants.ENCODING_DEFAULT;
        if (propertyManager.containsProperty(namespace, OptionalKey.templateEncoding)) {
            templateEncoding = propertyManager.getProperty(namespace, OptionalKey.templateEncoding);
        }

        velocityEngine.init();

        template = velocityEngine.getTemplate(templateName, templateEncoding);
    }

    /**
     * @return
     */
    public Template getTemplate() {
        return template;
    }
}
