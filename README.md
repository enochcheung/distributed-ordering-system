# Distributed Ordering System (Enoch's Bike Shop)

## AWS Cloud portion:
The final part of this project incorporates a DynamoDB (NoSQL) backend, along with nodes configured to be hosted on AWS cloud. Not all parts are in public repos, but here is the [Warehouse Portion](https://github.com/enochcheung/Team-2-Bike-Shop).

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
6. Have two copies of Tomcat in separate directories, and replace `conf/server.xml` with the ones provided in `mod-jk/tomcat1/server.xml` and `mod-jk/tomcat2/server.xml`, or manually change the ports to match the above, and add `jvmRoute="tomcat1"` to the line
	```
	<Engine name="Catalina" defaultHost="localhost" jvmRoute="tomcat1">
	```
	and similar for tomcat2.


## Starting the ordering system:

1. Make sure the ports in `settings.properties` are not occupied, and make sure the hosts match the ones you are running supplier1 and supplier 2 and orderingsystem on.
2. `ant jar` to build jar files. (Optional - the jars should already have been built).
3. Run `Database.jar`, then `Supplier1Server.jar`, then `Supplier2Server.jar`,  then `OrderingSystem1.jar`, then `OrderingSystem2.jar`, inside the directory where the jars are built. (Use `java -jar Supplier1Server.jar` etc)
4. Deploy `WAR/tomcat1/BikeShop.war` on Tomcat1, and `WAR/tomcat2/BikeShop.war` Tomcat2.
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

