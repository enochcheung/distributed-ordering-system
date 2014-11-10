# Distributed Ordering System (Enoch's Bike Shop)

## Configuring Apache HTTP server and Tomcat:

1. Download Apache HTTP Server at [http://httpd.apache.org/download.cgi](http://httpd.apache.org/download.cgi), and compile and install it (see `README.txt`)
2. Download mod_jk at [http://tomcat.apache.org/download-connectors.cgi](http://tomcat.apache.org/download-connectors.cgi), and compile and install it (path for `apxs` need to match the above) (see `native/BUILDING.txt`). Make sure that `mod_jk.so` is in the appropriate location, matching the one in `/PATHTO/distributed-ordering-system/mod-jk/mod-jk.conf`
3. Configure Apache HTTP Server by appending to `INSTALLEDLOCATION/apache2/conf/httpd.conf` the following line:
	```
	Include /PATHTO/distributed-ordering-system/mod-jk/mod-jk.conf
	```
	replacing `PATHTO` with the appropriate path.
4. Edit `mod-jk/mod-jk.conf` with the appropriate path to `mod-jk/workers.properties`
5. The following ports are used in `workers.properties`, and we will be configuring two instances of Tomcat to use these ports
	```
	tomcat1 server port = 8006
	tomcat1 connector (HTTP) port = 8081 
	tomcat1 connector (AJP) port = 8010 

	tomcat2 server port = 8007
	tomcat2 connector (HTTP) port = 8082
	tomcat2 connector (AJP) port = 8011
	```
6. Have two copies of Tomcat in separate directories, and replace `conf/server.xml` with the ones provided in `mod-jk/tomcat1/server.xml` and `mod-jk/tomcat2/server.xml`, or manually change the ports to match the above.


## Starting the ordering system:

1. Make sure the ports in `settings.properties` are not occupied, and make sure the hosts match the ones you are running supplier1 and supplier 2 and orderingsystem on.
2. `ant jar` to build jar files.
3. Run `Supplier1Server.jar`, then `Supplier2Server.jar`, then `OrderingSystem.jar` inside the directory where the jars are built. (Use `java -jar Supplier1Server.jar` etc)
4. Deploy `out/artifacts/BikeShop/BikeShop.war` on both Tomcat1 and Tomcat2.
5. Start the Apache server, Tomcat1 and Tomcat2.
6. Do not manually modify data files when anything is running.


## Use case:

1. Go to `localhost:80/BikeShop/` or whatever host and port the Apache server is deployed
2. Login not required for browsing bikes.
3. Create a new customer by filling out the form in `New Customer`.
4. Log in using the CustomerID and password specified.
5. To buy bikes, read the item number of the bikes you want, and add them to your cart under `Lookup/Buy bikes`. Your cart is not lost when navigating to different pages, or refreshing, but is restricted to current tab.
6. Press `Buy cart` to purchase items in the cart.
7. Check `Customer Info/Order History` to see your order. Total price is computed across all bikes in the same order (same Order Id).
8. Your order will be completed in approximately 1-2 minutes. A message will be in your inbox in `Customer Info/Order History`, but messages do not persist when the OrderingSystem is shut down. All other data will persist.

## Commands:


## Building mod_jk:

1. Go to `apache-mod-jk/native`, and build mod_jk with

	```
	./configure --with-apxs=/usr/sbin/apxs
	make
	```
2. Locate `apache-mod-jk/native/apache-2.0/mod_jk.so`

```
tomcat1 server port = 8006
tomcat1 connector (HTTP) port = 8081 
tomcat1 connector (AJP) port = 8010 

tomcat2 server port = 8007
tomcat2 connector (HTTP) port = 8082
tomcat2 connector (AJP) port = 8011
```

Default Mac OS location for apache http server: `/etc/apache2`
Start: `apachectl start`
Restart: `apachectl restart`
Check modules: `apachectl -t -D DUMP_MODULES`
assume httpd.conf has `Listen 80`


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