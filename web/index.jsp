<%@ page import="com.enochc.software648.hw1.OrderingSystem" %>
<%@ page import="com.enochc.software648.hw1.TestClass" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Enoch's Bike Shop</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
<h1>Enoch's Bike Shop</h1>
<%
    OrderingSystem orderingSystem = new OrderingSystem();
%>
Available Suppliers: <%= orderingSystem.getAvailableSuppliers().toString()%>

</body>
</html>
