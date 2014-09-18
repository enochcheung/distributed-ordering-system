## Starting the ordering system:

1. Make sure the ports in `settings.properties` are not occupied, and make sure the hosts match the ones you are running supplier1 and supplier 2 on.
2. `ant supplier1` to start Supplier1Server. Alternatively, compile and run Supplier1Server in an IDE.
3. `ant supplier2` to start Supplier2Server (in a separate window). Alternatively, compile and run Supplier2Server in an IDE.
4. `ant orderingsystem` to start the OrderingSystem (in a separate window). Alternatively, compile and run OrderSystem in an IDE. Do not manually modify data files when any of the three programs are running.

## Commands:

### Supplier1Server and Supplier2Server:
* `quit` to shut down the server.

### OrderingSystem:
* `help` Tells you to read the readme.
* `browse supplier_name page_num` Browses the inventory of chosen supplier. `page_num` could be `all`.
* `browseByPrice supplier_name page_num` Browses the inventory of chosen supplier by increasing price. `page_num` could be `all`.
* `lookupBike supplier_name item_number` Looks up information for a bike.
* `purchase supplier_name item_number quantity customerID` Buys a bike. An orderID will be assigned. `customerID` must first be created using `newCustomer customerID`.
* `newCustomer customerID` Creates a customer with given `customerID`. Additional prompts will ask for shipping information.
* `lookupCustomer customerID` Looks up the information on a customer, including a list of the orderID of their orders.
* `orderHistory customerID` Lists the orders of given customer.
* `listCustomers` Lists all customers by customerID.
* `lookupOrder orderID` Look up an order.
* `completeOrder orderID` Changes the status of an order to "complete".
* `quit` to shut down the ordering system.
