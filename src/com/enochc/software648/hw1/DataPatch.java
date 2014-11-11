package com.enochc.software648.hw1;

import com.enochc.software648.hw1.Bike;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A data patch sent to update the cache
 * with new data, and an updated versionNumber
 */
public class DataPatch<T> implements Serializable{
    private static final long serialVersionUID = 5858495255190196011L;

    private final T data;
    private final String dataVersion;


    public DataPatch(T data, String dataVersion) {
        this.data = data;
        this.dataVersion = dataVersion;
    }

    public T getData() {
        return data;
    }


    public String getDataVersion() {
        return dataVersion;
    }
}
