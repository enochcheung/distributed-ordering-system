package com.enochc.software648.hw1.suppliers;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enochc.software648.hw1.Bike;
import com.enochc.software648.hw1.SupplierData;

public class Supplier2Data implements SupplierData {
	private static final String NAME = "supplier2";
	private static final String TMP_FILENAME = "data/Warehouse2.tmp";
	private static final String FILENAME = "data/Warehouse2.txt";
    private static final String TMP_INV_FILENAME = "data/Inventory2.tmp";
    private static final String INV_FILENAME = "data/Inventory2.txt";
    private static final String SUPPLIER_PREFIX = "S2";

    private static final Pattern pricePattern = Pattern.compile("^\\$(.*)");
	private static final Pattern itemNumberPattern = Pattern
			.compile("^#([0-9]{2}-[0-9]{4})$");
    private static final Pattern externalItemNumberPattern = Pattern
            .compile("^([a-zA-Z0-9]{2})-([0-9]{2}-[0-9]{4}$)");
	private static final Pattern oldInventoryPattern = Pattern
			.compile("^inventory=(.*)");
    private static final Pattern inventoryPattern = Pattern
            .compile("^#([0-9]{2}-[0-9]{4}) ([0-9]+)$");

	private LineNumberReader reader;
    private LineNumberReader inventoryReader;
    private Map<String,Integer> lineNumberMap = new HashMap<String,Integer>();


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

	@Override
	public Bike readBike() throws IOException {
		String line;

		// Read the price
		line = readNonemptyLine();
		if (line == null) {
			return null;
		}
		int price = readPrice(line);

		// Read the bike name, should only be 1 line
		line = readNonemptyLine();
		if (line == null) {
			return null;
		}
		String name = readName(line);

		// Read the bike description (could be missing, or multi-line).
		// Read until itemNumberPattern is encountered
		StringBuilder descriptionBuilder = new StringBuilder();
		while (true) {
			line = readNonemptyLine();
			if (line == null) {
				return null;
			}
            Matcher matcher = itemNumberPattern.matcher(line);
			if (!matcher.matches()) {
				descriptionBuilder.append(line);

			} else {
				break;
			}
		}
		String description = descriptionBuilder.toString();

		// Read the item number
		String itemNumber = readItemNumber(line);
        String externalItemNumber = SUPPLIER_PREFIX+"-"+itemNumber;

		// Read the category
		line = readNonemptyLine();
		if (line == null) {
			return null;
		}
		String category = readCategory(line);

        /*
		// Read the inventory
		line = readNonemptyLine();
		if (line == null) {
			return null;
		}
		int inventory = readInventory(line);
		int inventoryLineNumber = reader.getLineNumber();
		*/

		Bike bike = new Bike(price, name, description, externalItemNumber, this.getName(), category,
				0);
		return bike;
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


    private String readNonemptyLine(BufferedReader reader1) throws IOException{
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
    public Map<String, Integer> getInventory() throws IOException {
        Map<String,Integer> map = new HashMap<String, Integer>();
        this.openInventoryReader();

        String line = readNonemptyLine(inventoryReader);
        while (line != null) {
            Matcher matcher = inventoryPattern.matcher(line);
            if (!matcher.matches()) {
                throw new IOException("Item number expected and inventory expected in \""+line+"\"");
            }
            String internalItemNumber = matcher.group(1);
            String externalItemNumber = SUPPLIER_PREFIX+"-"+internalItemNumber;

            Integer inventory = Integer.parseInt(matcher.group(2));

            int lineNumber = inventoryReader.getLineNumber();
            lineNumberMap.put(externalItemNumber,lineNumber);
            map.put(externalItemNumber,inventory);

            line = readNonemptyLine(inventoryReader);
        }

        this.closeInventoryReader();

        return map;    }

    private String getInternalItemNumber(String externalItemNumber) {
        Matcher matcher = externalItemNumberPattern.matcher(externalItemNumber);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }

	@Override
	public void writeInventory(Bike bike, Integer newInventory) throws IOException {
        String externalItemNumber = bike.getItemNumber();
        String internalItemNumber = getInternalItemNumber(externalItemNumber);
        if (internalItemNumber==null) {
            throw new IOException("Item number not found!");
        }
        Integer targetLineNumber = lineNumberMap.get(externalItemNumber);
        if (targetLineNumber==null) {
            throw new IOException("Line number not found!");
        }

        String replacementLine = "#"+internalItemNumber +" "+ newInventory.toString();

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
		// there is no pattern
		return line.trim();
	}

	private String readItemNumber(String line) throws IOException {
        Matcher matcher = itemNumberPattern.matcher(line);
		if (!matcher.matches()) {
			throw new IOException("Item number expected in \"" + line + "\"");
		}
		return matcher.group(1);
	}

	private String readCategory(String line) {
		// category has no pattern
		return line.trim();
	}

	private Integer readInventory(String line) throws IOException {
		Matcher matcher = inventoryPattern.matcher(line);
		if (!matcher.matches()) {
			throw new IOException("Inventory number expected in \"" + line
					+ "\"");
		}
		String inventory = matcher.group(1);
		return Integer.parseInt(inventory);
	}
}
