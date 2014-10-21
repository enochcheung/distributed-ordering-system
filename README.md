# Distributed Ordering System (Enoch's Bike Shop)

## Starting the ordering system:

1. Make sure the ports in `settings.properties` are not occupied, and make sure the hosts match the ones you are running supplier1 and supplier 2 and orderingsystem on.
2. `ant jar` to build jar files.
3. Run `Supplier1Server.jar`, then `Supplier2Server.jar`, then `OrderingSystem.jar` inside the directory where the jars are built.
4. Deploy `out/artifacts/BikeShop/BikeShop.war` on Tomcat.
5. Do not manually modify data files when any of the three programs are running.


## Use case:

1. Go to `localhost:8080/BikeShop/` or wherever `BikeShop.war` is deployed.
2. Login not required for browsing bikes.
3. Create a new customer by filling out the form in `New Customer`.
4. Log in using the CustomerID and password specified.
5. To buy bikes, read the item number of the bikes you want, and add them to your cart under `Lookup/Buy bikes`. Your cart is not lost when navigating to different pages, or refreshing, but is restricted to current tab.
6. Press `Buy cart` to purchase items in the cart.
7. Check `Customer Info/Order History` to see your order. Your order will be completed in approximately 1-2 minutes. A message will be in your inbox in `Customer Info/Order History`, but messages do not persist when the OrderingSystem is shut down. All other data will persist.

## Commands:


### OrderingSystem Console version (Deprecated, might not work):
Start OrderingSystem with param `console`

* `help` Tells you to read the readme.
* `browse supplier_name page_num` Browses the inventory of chosen supplier. `page_num` could be `all`.
* `browseByPrice supplier_name page_num` Browses the inventory of chosen supplier by increasing price. `page_num` could be `all`.
* `lookupBike item_number` Looks up information for a bike.
* `purchase supplier_name item_number quantity customerID` Buys a bike. An orderID will be assigned. `customerID` must first be created using `newCustomer customerID`.
* `newCustomer customerID` Creates a customer with given `customerID`. Additional prompts will ask for shipping information.
* `lookupCustomer customerID` Looks up the information on a customer, including a list of the orderID of their orders.
* `orderHistory customerID` Lists the orders of given customer.
* `listCustomers` Lists all customers by customerID.
* `lookupOrder orderID` Look up an order.
* `completeOrder orderID` Changes the status of an order to "complete".
* `quit` to shut down the ordering system.