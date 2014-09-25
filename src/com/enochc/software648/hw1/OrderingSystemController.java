package com.enochc.software648.hw1;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet("/orderingsystem")
public class OrderingSystemController extends HttpServlet {
    private OrderingSystem orderingSystem;

    @Override
    public void init() throws ServletException {
        // Do required initialization
        // orderingSystem = new OrderingSystem();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter printWriter = response.getWriter();
        printWriter.println("<h1>Hello World!</h1>");

        /*request.setAttribute("suppliers","asdfasf");*/
        /*
        String query = request.getParameter("query");
        if (query.equals("getAvailableSuppliers")) {
            Set<String> suppliers = orderingSystem.getAvailableSuppliers();
            request.setAttribute("suppliers", suppliers.toString());
        }
        */

        System.out.println("asdfadsfadfdadfasfdasaf");
        //request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
