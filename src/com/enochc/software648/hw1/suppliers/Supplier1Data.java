package com.enochc.software648.hw1.suppliers;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enochc.software648.hw1.Bike;
import com.enochc.software648.hw1.SupplierData;

public class Supplier1Data implements SupplierData {
    private static final String NAME = "supplier1";
    private static final String TMP_FILENAME = "data/Warehouse1.tmp";
    private static final String FILENAME = "data/Warehouse1.txt";
    private static final String INV_FILENAME = "data/Inventory1.txt";
    private static final String TMP_INV_FILENAME = "data/Inventory1.tmp";

    private static final String SUPPLIER_PREFIX = "S1";

    private static final Pattern pricePattern = Pattern
            .compile("^bike price[ ]*=[ ]*\\$(.*)");
    private static final Pattern namePattern = Pattern
            .compile("^bike name[ ]*=[ ]*(.*)");
    private static final Pattern itemNumberPattern = Pattern
            .compile("^item number[ ]*=[ ]*#(.*)");
    private static final Pattern externalItemNumberPattern = Pattern
            .compile("^([a-zA-Z0-9]{2})-([0-9]{2}-[0-9]{4}$)");
    private static final Pattern descriptionPattern = Pattern
            .compile("^(bike )?description[ ]*=");
    private static final Pattern categoryPattern = Pattern
            .compile("^category[ ]*=[ ]*(.*)");

    private static final Pattern oldInventoryPattern = Pattern
            .compile("^inventory[ ]*=[ ]*(.*)");

    private LineNumberReader reader;
    private LineNumberReader inventoryReader;
    private Map<String, Integer> lineNumberMap = new HashMap<String, Integer>();

    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public String getSupplierPrefix() {
        return SUPPLIER_PREFIX;
    }

    @Override
    public void openReader() throws FileNotFoundException {
        InputStream in = new FileInputStream(new File(FILENAME));
        reader = new LineNumberReader(new InputStreamReader(in));
    }

    @Override
    public void closeReader() {
        try {
            reader.close();
        } catch (IOException e) {
        }
    }

    private void openInventoryReader() throws FileNotFoundException {
        InputStream in = new FileInputStream(new File(INV_FILENAME));
        inventoryReader = new LineNumberReader(new InputStreamReader(in));
    }

    private void closeInventoryReader() {
        try {
            inventoryReader.close();
        } catch (IOException e) {
        }
    }

    @Override
    public Bike readBike() throws IOException {
        String line;

        // Read the price
        line = readNonemptyLine();
        if (line == null) {
            return null;
        }

        // skip over old inventory entries
        Integer inventory = null;
        Matcher matcher = oldInventoryPattern.matcher(line);
        if (matcher.matches()) {
            inventory = Integer.parseInt(matcher.group(1));
            line = readNonemptyLine();
            if (line == null) {
                return null;
            }
        }

        int price = readPrice(line);

        // Read the bike name
        line = readNonemptyLine();
        if (line == null) {
            return null;
        }
        String name = readName(line);

        // Read the bike description (could be missing, or multi-line)
        line = readNonemptyLine();
        matcher = descriptionPattern.matcher(line);
        if (!matcher.matches()) {
            throw new IOException("Bike description expected");
        }
        StringBuilder descriptionBuilder = new StringBuilder();
        while (true) {
            line = readNonemptyLine();
            if (line == null) {
                return null;
            }
            if (!itemNumberPattern.matcher(line).matches()) {
                descriptionBuilder.append(line);

            } else {
                break;
            }
        }
        String description = descriptionBuilder.toString();

        // Read the item number
        String internalItemNumber = readItemNumber(line);
        String externalItemNumber = SUPPLIER_PREFIX + "-" + internalItemNumber;

        // Read the category
        line = readNonemptyLine();
        if (line == null) {
            return null;
        }
        String category = readCategory(line);

        Bike bike = new Bike(price, name, description, externalItemNumber, this.getName(), category, 0);
        return bike;
    }

    @Override
    public Map<String, Integer> getInventory() throws IOException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        this.openInventoryReader();

        String line = readNonemptyLine(inventoryReader);
        while (line != null) {
            String internalItemNumber = readItemNumber(line);

            if (internalItemNumber == null) {
                throw new IOException("Item number expected.");
            }
            String externalItemNumber = SUPPLIER_PREFIX + "-" + internalItemNumber;


            line = readNonemptyLine(inventoryReader);
            if (line == null) {
                throw new IOException("Unexpected end of file.");
            }

            Integer inventory = readInventory(line);
            if (inventory == null) {
                throw new IOException("Inventory number expected.");
            }
            int lineNumber = inventoryReader.getLineNumber();
            lineNumberMap.put(externalItemNumber, lineNumber);
            map.put(externalItemNumber, inventory);

            line = readNonemptyLine(inventoryReader);
        }

        this.closeInventoryReader();

        return map;
    }

    private String readNonemptyLine(BufferedReader reader1) throws IOException {
        String line;
        do {
            line = reader1.readLine();
            if (line == null) {
                return null;
            }
            line = line.trim();
        } while (line.length() == 0);

        return line;
    }

    @Override
    public void writeInventory(Bike bike, Integer newInventory) throws IOException {
        Integer targetLineNumber = lineNumberMap.get(bike.getItemNumber());
        if (targetLineNumber == null) {
            throw new IOException("Line number not found!");
        }

        String replacementLine = newInventory.toString();

        File tmpFile = new File(TMP_INV_FILENAME);
        File dataFile = new File(INV_FILENAME);

        LineNumberReader reader = new LineNumberReader(new InputStreamReader(
                new FileInputStream(dataFile)));

        PrintWriter writer = new PrintWriter(new FileOutputStream(tmpFile));

        String line = "";
        while ((line = reader.readLine()) != null) {
            if (reader.getLineNumber() == targetLineNumber) {
                writer.println(replacementLine);
            } else {
                writer.println(line);
            }
        }

        writer.flush();
        writer.close();
        reader.close();

        dataFile.delete();
        tmpFile.renameTo(dataFile);

    }

    private String readNonemptyLine() throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                return null;
            }
            line = line.trim();
        } while (line.length() == 0);

        return line;
    }

    /**
     * Reads price in cents
     *
     * @param line
     * @return price in cents
     * @throws IOException
     */
    private int readPrice(String line) throws IOException {
        // read price in cents

        Matcher matcher = pricePattern.matcher(line);
        if (!matcher.matches()) {
            throw new IOException("Bike price expected in \"" + line + "\"");
        }

        String priceString = matcher.group(1);
        // remove commas
        priceString = priceString.replace(",", "");
        Double priceDouble = Double.parseDouble(priceString);

        // convert to cents
        priceDouble = priceDouble * 100;
        int price = priceDouble.intValue();
        return price;
    }

    private String readName(String line) {
        Matcher matcher = namePattern.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            return name;
        }
        return null;
    }

    private String readItemNumber(String line) {
        Matcher matcher = itemNumberPattern.matcher(line);
        if (matcher.matches()) {
            String itemNumber = matcher.group(1);
            return itemNumber;
        }
        return null;
    }

    private String readCategory(String line) {
        Matcher matcher = categoryPattern.matcher(line);
        if (matcher.matches()) {
            String category = matcher.group(1);
            return category;
        }
        return null;
    }

    private Integer readInventory(String line) throws IOException {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new IOException("Inventory number expected in \"" + line
                    + "\"");
        }
    }
}
