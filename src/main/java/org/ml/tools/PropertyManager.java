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

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom2.Element;

/**
 *
 * @author Dr. Matthias Laux
 */
public class PropertyManager implements Serializable {

    static final long serialVersionUID = 0427567567L;
    public final static ResolutionPolicy DEFAULT_RESOLUTION_POLICY = ResolutionPolicy.ALL_NAMESPACES;
    public final static Namespace DEFAULT_NAMESPACE = new Namespace("default_namespace_do_not_use_elsewhere ##$$%%");
    public final static String DEFAULT_MACRO_PATTERN = "##(.+?)##";

    private static boolean DEFAULT_AVOID_OVERWRITES = false;

    private ResolutionPolicy resolutionPolicy = DEFAULT_RESOLUTION_POLICY;
    private final Map<Namespace, Map<String, String>> namespaceData = new TreeMap<>();
    private boolean avoidOverwrites = DEFAULT_AVOID_OVERWRITES;
    private boolean resolveMacros = true;
    private static Matcher macroMatcher = Pattern.compile(DEFAULT_MACRO_PATTERN).matcher("");

    /**
     *
     */
    public enum XML {

        properties, property, name, namespace, propertySet, propertySets, parent, ignoreNamespace
    }

    /**
     *
     */
    public enum ResolutionPolicy {

        WITHIN_NAMESPACE, ALL_NAMESPACES, NONE
    }

    /**
     *
     */
    private class ResolutionResult {

        private String value = null;
        private boolean foundReplacement = false;

        /**
         *
         * @param value
         */
        public void setValue(String value) {
            if (value == null) {
                throw new IllegalArgumentException("value may not be null");
            }
            this.value = value;
        }

        /**
         *
         * @param foundReplacement
         */
        public void setFoundReplacement(boolean foundReplacement) {
            this.foundReplacement = foundReplacement;
        }

        /**
         *
         * @return
         */
        public String getValue() {
            return value;
        }

        /**
         *
         * @return
         */
        public boolean foundReplacement() {
            return foundReplacement;
        }
    }

    /**
     *
     */
    public PropertyManager() {
    }

    /**
     * Create an instance and try to add all the properties to the default
     * namespace; the actual namespace used may be overridden based on the rules
     * described for {@link #setProperties(Namespace, Element)}
     *
     * @param element
     */
    public PropertyManager(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }
        setProperties(element);
    }

    /**
     * Create an instance and try to add all the properties to the namespace
     * provided; the actual namespace used may be overridden based on the rules
     * described for {@link #setProperties(Namespace, Element)}
     *
     * @param namespace
     * @param element
     */
    public PropertyManager(Namespace namespace, Element element) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }
        setProperties(namespace, element);
    }

    /**
     * Create an instance and add all the properties to the default namespace
     *
     * @param properties
     */
    public PropertyManager(Map<String, String> properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        setProperties(properties);
    }

    /**
     * Create an instance and add all the properties to the namespace provided
     *
     * @param namespace
     * @param properties
     */
    public PropertyManager(Namespace namespace, Map<String, String> properties) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        setProperties(namespace, properties);
    }

    /**
     * Create an instance which inherits properties from the provided instances
     * (in the order in which they are provided which can be relevant, depending
     * on the selected overwrite policy)
     *
     * @param propertyManagers
     */
    public PropertyManager(PropertyManager... propertyManagers) {
        if (propertyManagers == null) {
            throw new IllegalArgumentException("propertyManagers may not be null");
        }
        for (PropertyManager propertyManager : propertyManagers) {
            for (Namespace namespace : propertyManager.getNamespaces()) {
                setProperties(namespace, propertyManager.getProperties(namespace));
            }
        }
    }

    /**
     *
     * @param element
     * @return
     */
    public static Map<String, PropertyManager> createPropertyManagers(Element element) {
        if (element == null) {
            throw new NullPointerException("element may not be null");
        }

        Map<String, PropertyManager> propertyManagers;

        if (element.getChild(XML.propertySets.toString()) != null) {
            propertyManagers = new HashMap<>();

            //.... Scan if there is a properties element without a name attribute - these are propagated to all the named sets
            PropertyManager generalManager = null;
            for (Element propertySetElement : element.getChild(XML.propertySets.toString()).getChildren(XML.propertySet.toString())) {
                if (propertySetElement.getAttribute(XML.name.toString()) == null) {
                    generalManager = new PropertyManager(propertySetElement);
                    break;
                }
            }
            //.... No generic properties to cascade to all new propertyManagers found
            if (generalManager == null) {
                generalManager = new PropertyManager();
            }

            //.... Find all named propertySet elements and process them
            for (Element propertySetElement : element.getChild(XML.propertySets.toString()).getChildren(XML.propertySet.toString())) {

                if (propertySetElement.getAttribute(XML.name.toString()) != null) {

                    PropertyManager namedManager;

                    //.... A derived property set, i. e. this property manager inherits properties from a parent
                    if (propertySetElement.getAttribute(XML.parent.toString()) != null) {
                        String parentName = propertySetElement.getAttributeValue(XML.parent.toString());
                        if (!propertyManagers.containsKey(parentName)) {
                            throw new UnsupportedOperationException("Unknown parent PropertySet referenced: " + parentName);
                        }

                        //.... Do we need to ignore certain namespaces when crrating the derived PropertyManager?
                        if (propertySetElement.getAttribute(XML.ignoreNamespace.toString()) != null) {

                            //.... Get the names of the namespaces to ignore when creating the new named manager
                            String[] ns = propertySetElement.getAttributeValue(XML.ignoreNamespace.toString()).split(":");
                            Set<String> nset = new HashSet<>();
                            Collections.addAll(nset, ns);
                            PropertyManager parentPropertyManager = propertyManagers.get(parentName);
                            namedManager = new PropertyManager(generalManager);

                            //.... Copy over the default namespace (it can not be ignored)
                            namedManager.setProperties(DEFAULT_NAMESPACE, parentPropertyManager.getProperties(DEFAULT_NAMESPACE));

                            //.... Copy over the other namespaces unless their name is part of the ignore set
                            for (Namespace namespace : parentPropertyManager.getNamespaces()) {
                                if (!nset.contains(namespace.toString())) {
                                    namedManager.setProperties(namespace, parentPropertyManager.getProperties(namespace));
                                }
                            }

                            //.... No namespaces to ignore
                        } else {
                            namedManager = new PropertyManager(generalManager, propertyManagers.get(parentName));
                        }

                        //.... No parent to inherit from
                    } else {
                        namedManager = new PropertyManager(generalManager);
                    }
                    namedManager.setProperties(propertySetElement);
                    String name = propertySetElement.getAttributeValue(XML.name.toString()).trim();
                    propertyManagers.put(name, namedManager);
                }
            }

        } else {
            propertyManagers = new HashMap<>();
        }
        return propertyManagers;
    }

    /**
     *
     * @param avoidOverwrites
     */
    public static void setDefaultAvoidOverwrites(boolean avoidOverwrites) {
        DEFAULT_AVOID_OVERWRITES = avoidOverwrites;
    }

    /**
     *
     * @param resolutionPolicy
     */
    public void setResolutionPolicy(ResolutionPolicy resolutionPolicy) {
        if (resolutionPolicy == null) {
            throw new IllegalArgumentException("resolutionPolicy may not be null");
        }
        this.resolutionPolicy = resolutionPolicy;
        resolveMacros = resolutionPolicy != ResolutionPolicy.NONE;
    }

    /**
     *
     * @return
     */
    public ResolutionPolicy getResolutionPolicy() {
        return resolutionPolicy;
    }

    /**
     *
     * @param patternString
     */
    public void setMacroPattern(String patternString) {
        if (patternString == null) {
            throw new IllegalArgumentException("patternString may not be null");
        }
        macroMatcher = Pattern.compile(patternString).matcher("");
    }

    /**
     *
     * @param avoidOverwrites
     */
    public void setAvoidOverwrites(boolean avoidOverwrites) {
        this.avoidOverwrites = avoidOverwrites;
    }

    /**
     * Return a collection of all namespaces which are actually used for
     * properties
     *
     * @return
     */
    public Collection<Namespace> getNamespaces() {
        return namespaceData.keySet();
    }

    /**
     *
     * @return
     */
    public boolean doesAvoidOverwrites() {
        return avoidOverwrites;
    }

    /**
     * Check if the namespace provided is used for one or more properties
     *
     * @param namespace
     * @return
     */
    public boolean containsNamespace(Namespace namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        return namespaceData.containsKey(namespace);
    }

    /**
     * Set a property in the default namespace
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        setProperty(DEFAULT_NAMESPACE, key, value);
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setProperty(Enum key, String value) {
        setProperty(DEFAULT_NAMESPACE, key.toString(), value);
    }

    /**
     *
     * @param namespace
     * @param key
     * @param value
     */
    public void setProperty(Namespace namespace, Enum key, String value) {
        setProperty(namespace, key.toString(), value);
    }

    /**
     * Set a property in the namespace provided
     *
     * @param namespace
     * @param key
     * @param value
     */
    public void setProperty(Namespace namespace, String key, String value) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        if (avoidOverwrites && namespaceData.containsKey(namespace) && namespaceData.get(namespace).containsKey(key)) {
            return;
        }
        if (!namespaceData.containsKey(namespace)) {
            namespaceData.put(namespace, new TreeMap<>());
        }
        if (!resolveMacros) {
            namespaceData.get(namespace).put(key, value);
        } else {
            namespaceData.get(namespace).put(key, resolveMacros(namespace, value).getValue());
            backwardResolveMacros();  // Resolve backward references
        }
    }

    /**
     * Add all the properties to the default namespace; the actual namespace
     * used may be overridden based on the rules described for
     * {@link #setProperties(Namespace, Element)}
     *
     * @param element
     */
    public final void setProperties(Element element) {
        setProperties(DEFAULT_NAMESPACE, element);
    }

    /**
     * Add all the properties to the namespace provided. The actual namespace
     * depends on some more conditions or options:
     * <ol>
     * <li> first choice is the namespace provided here
     * <li> this can be overridden if a different namespace is provided as an
     * attribute to the &lt;properties&gt; element. This cascades down to all
     * the &lt;property&gt; elements below
     * <li> this again can be overridden if a different namespace is provided as
     * an attribute to a &lt;property&gt; element below the &lt;properties&gt;
     * element
     * </ol>
     *
     * @param namespace
     * @param element
     */
    public final void setProperties(Namespace namespace, Element element) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }

        Element propertiesElement = element.getChild(XML.properties.toString());
        if (propertiesElement != null) {

            //.... Check if there is a namespace attribute at the properties level
            Namespace topNamespace = null;
            if (propertiesElement.getAttribute(XML.namespace.toString()) != null) {
                topNamespace = new Namespace(propertiesElement.getAttributeValue(XML.namespace.toString()));
            }

            //.... Now check all children
            for (Element propertyElement : propertiesElement.getChildren(XML.property.toString())) {

                //.... Check if a name has been specified
                if (propertyElement.getAttribute(XML.name.toString()) == null) {
                    throw new IllegalArgumentException("Missing property attribute: " + XML.name.toString());
                }

                //.... First choice: if a namespace was given as argument, take that one
                Namespace actualNamespace = namespace;

                //.... Second approach: if there is an explicit namespace at the properties element level, take that one
                if (topNamespace != null) {
                    actualNamespace = topNamespace;
                }

                //.... Third approach: do we have an explicit namespace for this particular property? This overrides everything else
                if (propertyElement.getAttribute(XML.namespace.toString()) != null) {
                    actualNamespace = new Namespace(propertyElement.getAttributeValue(XML.namespace.toString()));
                }

                //.... Now finally set the property in the correct namespace
                setProperty(actualNamespace, propertyElement.getAttributeValue(XML.name.toString()), propertyElement.getTextTrim());
            }
        }
    }

    /**
     * Adds all the given properties to the default namespace
     *
     * @param properties
     */
    public final void setProperties(Map<String, String> properties) {
        setProperties(DEFAULT_NAMESPACE, properties);
    }

    /**
     * Adds all the given properties to the namespace provided
     *
     * @param namespace
     * @param properties
     */
    public final void setProperties(Namespace namespace, Map<String, String> properties) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties may not be null");
        }
        for (String key : properties.keySet()) {
            setProperty(namespace, key, properties.get(key));
        }
    }

    /**
     * Set all the properties in the specified PropertyManager instance in the
     * current one
     *
     * @param propertyManager
     */
    public void setProperties(PropertyManager propertyManager) {
        if (propertyManager == null) {
            throw new NullPointerException("propertyManager may not be null");
        }
        for (Namespace namespace : propertyManager.getNamespaces()) {
            setProperties(namespace, propertyManager.getProperties(namespace));
        }
    }

    /**
     * Get a property in the default namespace
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return getProperty(DEFAULT_NAMESPACE, key);
    }

    /**
     *
     * @param key
     * @return
     */
    public String getProperty(Enum key) {
        return getProperty(DEFAULT_NAMESPACE, key.toString());
    }

    /**
     *
     * @param namespace
     * @param key
     * @return
     */
    public String getProperty(Namespace namespace, Enum key) {
        return getProperty(namespace, key.toString());
    }

    /**
     * Get a property in the namespace provided
     *
     * @param namespace
     * @param key
     * @return
     */
    public String getProperty(Namespace namespace, String key) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (!containsProperty(namespace, key)) {
            return null;
        }
        return namespaceData.get(namespace).get(key);
    }

    /**
     * This can be quite handy to extract data from a property with a default
     * value in case it does not exist or is ill-formatted. One can argue
     * whether it is a good idea to return the default even if the property is
     * ill-formed, but for now let's try this
     *
     * @param namespace
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(Namespace namespace, String key, int defaultValue) {
        if (namespace == null) {
            throw new NullPointerException("namespace may not be null");
        }
        if (key == null) {
            throw new NullPointerException("key may not be null");
        }
        if (!containsProperty(namespace, key)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(getProperty(namespace, key));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(String key, int defaultValue) {
        return getInt(DEFAULT_NAMESPACE, key, defaultValue);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(Enum key, int defaultValue) {
        return getInt(DEFAULT_NAMESPACE, key.toString(), defaultValue);
    }

    /**
     *
     * @param namespace
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(Namespace namespace, Enum key, int defaultValue) {
        return getInt(namespace, key.toString(), defaultValue);
    }

    /**
     *
     * @param namespace
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(Namespace namespace, String key, String defaultValue) {
        if (namespace == null) {
            throw new NullPointerException("namespace may not be null");
        }
        if (key == null) {
            throw new NullPointerException("key may not be null");
        }
        if (!containsProperty(namespace, key)) {
            return defaultValue;
        }
        return getProperty(namespace, key);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        return getString(DEFAULT_NAMESPACE, key, defaultValue);
    }

    /**
     *
     * @param namespace
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(Namespace namespace, Enum key, String defaultValue) {
        return getString(namespace, key.toString(), defaultValue);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(Enum key, String defaultValue) {
        return getString(DEFAULT_NAMESPACE, key.toString(), defaultValue);
    }

    /**
     * Check if the property exists in the default namespace
     *
     * @param key
     * @return
     */
    public boolean containsProperty(String key) {
        return containsProperty(DEFAULT_NAMESPACE, key);
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean containsProperty(Enum key) {
        return containsProperty(DEFAULT_NAMESPACE, key.toString());
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean containsNonEmptyProperty(String key) {
        return containsNonEmptyProperty(DEFAULT_NAMESPACE, key);
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean containsNonEmptyProperty(Enum key) {
        return containsNonEmptyProperty(DEFAULT_NAMESPACE, key.toString());
    }

    /**
     *
     * @param namespace
     * @param key
     * @return
     */
    public boolean containsProperty(Namespace namespace, Enum key) {
        return containsProperty(namespace, key.toString());
    }

    /**
     * Check if the property exists in the namespace provided
     *
     * @param namespace
     * @param key
     * @return
     */
    public boolean containsProperty(Namespace namespace, String key) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (!namespaceData.containsKey(namespace)) {
            return false;
        }
        return namespaceData.get(namespace).containsKey(key);
    }

    /**
     *
     * @param namespace
     * @param key
     * @return
     */
    public boolean containsNonEmptyProperty(Namespace namespace, Enum key) {
        return containsNonEmptyProperty(namespace, key.toString());
    }

    /**
     *
     * @param namespace
     * @param key
     * @return
     */
    public boolean containsNonEmptyProperty(Namespace namespace, String key) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (!namespaceData.containsKey(namespace)) {
            return false;
        }
        return namespaceData.get(namespace).containsKey(key) && namespaceData.get(namespace).get(key).trim().length() > 0;
    }

    /**
     * Return all properties in the default namespace
     *
     * @return
     */
    public Map<String, String> getProperties() {
        return getProperties(DEFAULT_NAMESPACE);
    }

    /**
     * Return all properties in the namespace given
     *
     * @param namespace
     * @return
     */
    public Map<String, String> getProperties(Namespace namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (namespaceData.containsKey(namespace)) {
            return namespaceData.get(namespace);
        } else {
            return new HashMap<>();
        }
    }

    /**
     *
     * @param element
     * @return
     */
    public static boolean containsPropertiesElement(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }
        return element.getChild(XML.properties.toString()) != null;
    }

    /**
     *
     * @param enums
     */
    public void validatePropertyNames(Enum... enums) {
        validatePropertyNames(DEFAULT_NAMESPACE, enums);
    }

    /**
     *
     * @param namespace
     * @param enums
     */
    public void validatePropertyNames(Namespace namespace, Enum... enums) {
        if (enums == null) {
            throw new IllegalArgumentException("enums may not be null");
        }
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        for (Enum e : enums) {
            if (!containsProperty(namespace, e.toString())) {
                if (namespace.equals(DEFAULT_NAMESPACE)) {
                    throw new IllegalArgumentException("Missing property key '" + e.toString() + "' in default namespace");
                } else {
                    throw new IllegalArgumentException("Missing property key '" + e.toString() + "' in namespace " + namespace);
                }
            }
        }
    }

    /**
     *
     * @param <E>
     * @param propertyEnum
     */
    public <E extends Enum> void validateAllPropertyNames(E propertyEnum) {
        validateAllPropertyNames(DEFAULT_NAMESPACE, propertyEnum);
    }

    /**
     *
     * @param <E>
     * @param namespace
     * @param propertyEnum
     */
    public <E extends Enum> void validateAllPropertyNames(Namespace namespace, E propertyEnum) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (propertyEnum == null) {
            throw new IllegalArgumentException("propertyEnum may not be null");
        }
        for (Enum e : propertyEnum.getClass().getEnumConstants()) {
            if (!containsProperty(namespace, e.toString())) {
                if (namespace.equals(DEFAULT_NAMESPACE)) {
                    throw new IllegalArgumentException("Missing property key '" + e.toString() + "' in default namespace");
                } else {
                    throw new IllegalArgumentException("Missing property key '" + e.toString() + "' in namespace " + namespace);
                }
            }
        }
    }

    /**
     *
     * @param namespace
     * @param testValue
     * @return
     */
    private ResolutionResult resolveMacros(Namespace namespace, String testValue) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        if (testValue == null) {
            throw new IllegalArgumentException("testValue may not be null");
        }

        ResolutionResult resolutionResult = new ResolutionResult();

        StringBuffer sb = new StringBuffer(50);
        macroMatcher.reset(testValue);
        while (macroMatcher.find()) {
            resolutionResult.setFoundReplacement(true);

            String referencedKey = macroMatcher.group(1);
            switch (resolutionPolicy) {
                case ALL_NAMESPACES:

                    boolean found = false;
                    for (Namespace ns : namespaceData.keySet()) {
                        if (containsProperty(ns, referencedKey)) {
                            macroMatcher.appendReplacement(sb, getProperty(ns, referencedKey));
                            found = true;
                            break;
                        }
                    }
                    break;   // No fall-through required as we have convered all namespaces

                case WITHIN_NAMESPACE:

                    if (containsProperty(namespace, referencedKey)) {
                        macroMatcher.appendReplacement(sb, getProperty(namespace, referencedKey));
                    }
                    break;
            }
        }
        macroMatcher.appendTail(sb);

        resolutionResult.setValue(sb.toString());

        return resolutionResult;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(300);
        for (Namespace namespace : namespaceData.keySet()) {
            boolean isDefault = namespace.equals(DEFAULT_NAMESPACE);
            for (String key : getProperties(namespace).keySet()) {
                if (isDefault) {
                    sb.append("(): ");
                    sb.append(key);
                    sb.append(" - ");
                    sb.append(getProperty(namespace, key));
                    sb.append("\n");
                } else {
                    sb.append("(");
                    sb.append(namespace);
                    sb.append("): ");
                    sb.append(key);
                    sb.append(" - ");
                    sb.append(getProperty(namespace, key));
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Currently we can not detect circular references, which is a stupid thing
     * to do in the first place
     */
    private void backwardResolveMacros() {
        for (Namespace namespace : namespaceData.keySet()) {
            for (String key : namespaceData.get(namespace).keySet()) {
                ResolutionResult resolutionResult = resolveMacros(namespace, namespaceData.get(namespace).get(key));
                if (resolutionResult.foundReplacement()) {
                    namespaceData.get(namespace).put(key, resolutionResult.getValue());
                }
            }
        }
    }

    /**
     * Remark: this unfortunately does not account for namespace information
     *
     * @param element
     * @return
     */
    public static Map<String, String> extractProperties(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }
        if (element.getChild(XML.properties.toString()) != null) {
            Map<String, String> p = new HashMap<>();
            for (Element propertyElement : element.getChild(XML.properties.toString()).getChildren(XML.property.toString())) {
                p.put(propertyElement.getAttributeValue(XML.name.toString()), propertyElement.getTextTrim());
            }
            return p;
        } else {
            throw new IllegalArgumentException("element does not contain child: " + XML.properties.toString());
        }
    }
}
