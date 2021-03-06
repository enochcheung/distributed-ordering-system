<%@page contentType="text/html"%>

<!DOCTYPE html>
<html>
<head>
    <title>Order History</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
    <link rel="stylesheet" type="text/css" href="jquery.dynatable.css"/>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="jquery.dynatable.js"></script>
</head>


<body>
<div id="frame">
    <h3>Customer Info</h3>
    <p>SessionID: <%= session.getId() %></p>
    <p id="message"></p>
    <button type="button" id="refresh">Refresh</button>
    <br>
    <p id="address-text"></p>

    <br>
    <h3>Order History</h3>
    <div id="table">

        <table id="orders-table">
            <thead>
            <th>Customer</th>
            <th>Item Number</th>
            <th>Bike Name</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Order Id</th>
            <th>Date</th>
            <th>Status</th>
            <th>Total Price</th>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>

    <br>
    <h3>Inbox</h3>
    <p id="inbox-text"></p>


</div>
<script>
    $(document).ready(function() {
        lookupCustomer();
        $("#refresh").on("click",function(e) {
            lookupCustomer();
        });
    });
    var dynatable = $('#orders-table').dynatable({records: []}).data('dynatable');

    var message = $("#message");
    var addressText = $("#address-text");



    function lookupCustomer() {
        message.text("");
        var customerID = window.sessionStorage.getItem("customerID");
        var token = window.sessionStorage.getItem("token");

        if (customerID === null || token === null) {
            message.text("Login required.");
            return;
        }

        $.ajax({
            type: "GET",
            headers: {"customerID":customerID,"token":token},
            url: "rest/orderingsystem/customer/" + customerID
        }).done(function (data) {
            document.getElementById("address-text").innerText = data;
        }).fail(function (jqXHR, textStatus, errorThrown) {
                    if (jqXHR.status == 403) {
                        message.text("Permission denied. (try logging in again)");
                    } else if (jqXHR.status == 404) {
                        message.text(customerID + " not found.");
                    } else {
                        message.text("Error requesting "+event.url);
                    }
                }
        );

        $.ajax({
            type: "GET",
            headers: {"customerID":customerID,"token":token},
            datatype: "json",
            url: "rest/orderingsystem/orderhistory/" + customerID

        }).done(function(data) {
            dynatable.settings.dataset.originalRecords=data;
            dynatable.process();
        });


        $.ajax({
            type: "GET",
            headers: {"customerID":customerID,"token":token},
            url: "rest/orderingsystem/inbox/" + customerID
        }).done(function (data) {
            document.getElementById("inbox-text").innerText = data;
        })
    }


</script>
</body>
</html>
