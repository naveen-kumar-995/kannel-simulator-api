package com.ptsl.kannelsimulator.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ptsl.kannelsimulator.model.DlrRequest;
import com.ptsl.kannelsimulator.queue.DlrQueueHolder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "DlrReceiverServlet", urlPatterns = "/cgi-bin/sendsms")
public class DlrReceiverServlet extends HttpServlet {

    private static final long   serialVersionUID = 1L;
    private static final  Logger LOGGER           = LoggerFactory.getLogger(DlrReceiverServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            boolean disableDlrHandover =  Boolean.parseBoolean(System.getenv().getOrDefault("DISABLE-DLR-HANDOVER", "false"));

            if(disableDlrHandover)
            {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                response.getWriter().write("ACCEPTED");
                return;
            }

            /*
             * Read Parameters
             */
            String user = request.getParameter("user");
            String password = request.getParameter("password");
            String smsc = request.getParameter("smsc");
            String smscId = request.getParameter("smsc-id");
            String cliId = request.getParameter("cliid");
            String mobileNumber = request.getParameter("to");
            String senderId = request.getParameter("from");
            String message = request.getParameter("text");
            String metaData = request.getParameter("meta-data");

            /*
             * Decode DLR URL
             */
            String dlrUrl = request.getParameter("dlr-url");


            /*
             * Build Request
             */
            DlrRequest dlrRequest = new DlrRequest(user, password, smsc, smscId, cliId, mobileNumber, senderId, message, dlrUrl, metaData, System.currentTimeMillis());


            /*
             * Push into Queue
             */
            boolean accepted = DlrQueueHolder.getInstance().offer(dlrRequest);

            if (! accepted) {
                LOGGER.error("Queue Full. Mobile : " + mobileNumber);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Queue Full");
                return;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Queued DLR Request Mobile={}, SMSC={}, CLIID={}, dlrRequest={}", mobileNumber, smsc, cliId, dlrRequest.toString());
            }
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            response.getWriter().write("ACCEPTED");

        } catch (Exception e) {
            LOGGER.error("Unable to process request.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}