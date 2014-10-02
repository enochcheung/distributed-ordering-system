package com.enochc.software648.hw1.suppliers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enochc.software648.hw1.Bike;
import com.enochc.software648.hw1.SupplierData;

public class Supplier2Data implements SupplierData {
	private static final String NAME = "supplier2";
	private static final String TMP_FILENAME = "data/Supplier1.tmp";
	private static final String FILENAME = "data/Supplier2.txt";
    private static final String SUPPLIER_PREFIX = "S2";

    private static final Pattern pricePattern = Pattern.compile("^\\$(.*)");
	private static final Pattern itemNumberPattern = Pattern
			.compile("^#([0-9]{2}-[0-9]{4})$");
    private static final Pattern externalItemNumberPattern = Pattern
            .compile("^([a-zA-Z0-9]{2})-([0-9]{2}-[0-9]{4}$)");
	private static final Pattern inventoryPattern = Pattern
			.compile("^inventory=(.*)");

	private LineNumberReader reader;

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

		// Read the inventory
		line = readNonemptyLine();
		if (line == null) {
			return null;
		}
		int inventory = readInventory(line);
		int inventoryLineNumber = reader.getLineNumber();

		Bike bike = new Bike(price, name, description, externalItemNumber, this.getName(), category,
				inventory, inventoryLineNumber);
		return bike;
	}

	@Override
	public void writeInventory(Bike bike, int newInventory) throws IOException {
		int targetLineNumber = bike.getInventoryLineNumber();
		String replacementLine = "inventory=" + newInventory;

		File tmpFile = new File(TMP_FILENAME);
		File dataFile = new File(FILENAME);

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
