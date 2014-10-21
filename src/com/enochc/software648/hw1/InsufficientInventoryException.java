package com.enochc.software648.hw1;


public class InsufficientInventoryException extends Exception {
    private static final long serialVersionUID = 73497335116782542L;

    private final String itemNum;

    public InsufficientInventoryException(String itemNum) {
        super("Insufficient inventory for "+itemNum);
        this.itemNum = itemNum;
    }

    public String getItemNum(){
        return itemNum;
    }
}
