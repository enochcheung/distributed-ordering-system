package com.enochc.software648.hw1;


public class BikeNotFoundException extends Exception {
    private static final long serialVersionUID = 7135526423720417981L;
    private final String itemNum;

    public BikeNotFoundException(String itemNum) {
        super("Bike "+itemNum+" not found.");
        this.itemNum = itemNum;
    }

    public String getItemNum(){
        return itemNum;
    }
}
