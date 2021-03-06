<%@page contentType="text/html"%>

<!DOCTYPE html>
<html>
<head>
    <title>Enoch's Bike Shop</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
    <link rel="stylesheet" type="text/css" href="jquery.dynatable.css"/>
    <style>
        body {
            background-image: url("background.jpg");
            background-color: black;
        }
    </style>
    <script src="jquery-1.11.1.js"></script>
    <script src="jquery.dynatable.js"></script>
</head>


<body>
<div id="container">
    <div id="header">
        <h1>Enoch's Bike Shop</h1>

        <form id="login-form" action="">
            <label for="customerID">CustomerID:</label> <input type="text" name="customerID" id="customerID"/>
            <label for="password">Password:</label> <input type='password' name="password" id="password"/>
            <button type="submit">Login</button>
        </form>
        <label id="login-message"></label> <button type="button" id="logout">Log out</button>
    </div>
</div>


<div id="content">
    <div id="suppliers">Suppliers:</div>
    <p>SessionID: <%= session.getId() %></p>
    <select id="menu" onchange="updateFrame(this.value)">
        <option value="browse">Browse Bikes</option>
        <option value="lookup-bike">Lookup/Buy Bikes</option>
        <option value="new-customer">New Customer</option>
        <option value="order-history">Customer Info/Order History</option>
    </select>

    <iframe id="iframe" src="browse.html" width="100%" height="75%"></iframe>


</div>

<script>
    // Load list of available suppliers
    $(document).ready(loadAvailableSuppliers());


    $(document).ready(
            function () {
                if (window.sessionStorage.getItem("customerID") !== null &&
                        window.sessionStorage.getItem("token") !== null) {
                    loggedIn();
                } else {
                    document.getElementById("logout").style.display = "none";
                }

            }
    );


    $(document).ready(
            function () {
                $('#logout').on("click", function (e) {
                    window.sessionStorage.removeItem("customerID");
                    window.sessionStorage.removeItem("token");
                    document.getElementById("login-form").style.display = 'block';
                    document.getElementById("logout").style.display = 'none';
                    $("#login-message").text("");
                });
            }
    );


    $(document).ready(
            function () {
                $("#login-form").on("submit", function (e) {
                    e.preventDefault();
                    var customerID = $("#login-form").find("input[name=customerID]").val();
                    var password = $("#login-form").find("input[name=password]").val();
                    $("#login-message").text("");
                    $.ajax({
                        type: "GET",
                        headers: {"customerID": customerID,
                            "password": password},
                        dataType: "text",
                        url: "rest/orderingsystem/auth/"
                    }).done(function (data) {
                                window.sessionStorage.setItem("customerID", customerID);
                                window.sessionStorage.setItem("token", data);
                                loggedIn();
                            }
                    ).fail(function (event, jqxhr, settings, thrownError) {
                                $("#login-message").text("Login failed.");
                            }
                    );
                });
            }
    );

    function loggedIn() {
        document.getElementById("login-form").style.display = 'none';
        $("#login-message").text("Hello, " + window.sessionStorage.getItem("customerID") + "! (token: " + window.sessionStorage.getItem("token") + ")");
        document.getElementById("logout").style.display = 'inline';
    }

    function updateFrame(selection) {
        if (selection === "browse") {
            document.getElementById("iframe").src = "browse.html";
        } else if (selection === "lookup-bike") {
            document.getElementById("iframe").src = "lookup-bike.html";
        } else if (selection === "new-customer") {
            document.getElementById("iframe").src = "new-customer.html";
        } else if (selection === "order-history") {
            document.getElementById("iframe").src = "order-history.jsp";
        }
    }


    function loadAvailableSuppliers() {
        $.ajax({
            type: "GET",
            url: "rest/orderingsystem/suppliers",

            error: function () {
                $("#suppliers").text("Cannot retrieve suppliers.");
            }
        }).done(function (data) {
            $("#suppliers").text("Available Suppliers: " + data);
        });
    }

</script>
</body>
</html>
