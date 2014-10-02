package com.enochc.software648.hw1;

import com.enochc.software648.hw1.Bike;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A data patch sent from a supplier to update the cache of an OrderingSystem
 * with new bike info, and an updated versionNumber
 */
public class SupplierDataPatch implements Serializable{
    private static final long serialVersionUID = 5858495255190196011L;

    private final ArrayList<Bike> bikes;
    private final String dataVersion;


    public SupplierDataPatch(ArrayList<Bike> bikes, String dataVersion) {
        this.bikes = bikes;
        this.dataVersion = dataVersion;
    }

    public ArrayList<Bike> getBikes() {
        return bikes;
    }


    public String getDataVersion() {
        return dataVersion;
    }
}
