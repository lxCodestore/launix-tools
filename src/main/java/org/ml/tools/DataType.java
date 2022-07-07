/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ml.tools;

import java.util.HashSet;
import java.util.Set;

import static org.ml.tools.DataKind.*;

/**
 * @author osboxes
 */
public enum DataType {

    TypeString("", StringKind),
    TypeInteger(0, IntegerKind, NumericKind),
    TypeEmail("", StringKind),
    TypeURL("", StringKind),
    TypeBoolean(false, BooleanKind),
    TypeDouble(0.0d, DoubleKind, NumericKind),
    TypeIntegerPercentage(0, IntegerKind, NumericKind),
    TypeDoublePercentage(0.0d, DoubleKind, NumericKind),
    TypeUndefined("Undefined", StringKind);

    Comparable defaultValue;
    Set<DataKind> dataKind = new HashSet<>();

    /**
     * @return
     */
    public Comparable getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return
     */
    public Set<DataKind> getDataKinds() {
        return dataKind;
    }

    /**
     * @param dataKind
     * @return
     */
    public boolean isOfDataKind(DataKind dataKind) {
        if (dataKind == null) {
            throw new NullPointerException("dataKind may not be null");
        }
        return this.dataKind.contains(dataKind);
    }

    /**
     * @param defaultValue
     * @param dataKind
     */
    DataType(Comparable defaultValue, DataKind... dataKind) {
        for (DataKind d : dataKind) {
            this.dataKind.add(d);
        }
        this.defaultValue = defaultValue;
    }
}
