package com.ptsl.kannelsimulator.status;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/status.xml")
public class StatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws IOException {

        resp.setContentType("application/xml");
        resp.setCharacterEncoding("UTF-8");

        try (var in = getClass().getClassLoader().getResourceAsStream("status.xml")) {
            if (in == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            in.transferTo(resp.getOutputStream());
        }
    }
}