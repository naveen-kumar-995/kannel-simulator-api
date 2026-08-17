package com.ptsl.kannelsimulator.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ptsl.kannelsimulator.http.HttpSender;
import com.ptsl.kannelsimulator.poller.DlrPoller;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;


@WebListener
public class StartupListener implements ServletContextListener {

    private static final Logger    LOGGER = LoggerFactory.getLogger(StartupListener.class);
    private              DlrPoller dlrPoller;
    private              Thread    pollerThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        LOGGER.info("========================================");
        LOGGER.info("Starting Kannel Simulator...");
        LOGGER.info("========================================");

        try {

            /*
             * Initialize reusable HTTP client.
             */
            HttpSender.initialize();
            boolean disableDlrHandover =  Boolean.parseBoolean(System.getenv().getOrDefault("DISABLE-DLR-HANDOVER", "false"));
            LOGGER.error("disableDlrHandover: {}", disableDlrHandover);

            if(!disableDlrHandover) {
                /*
                 * Start Poller
                 */
                dlrPoller = new DlrPoller();

                pollerThread = new Thread(dlrPoller, "dlr-poller");
                pollerThread.setDaemon(false);
                pollerThread.start();

                LOGGER.info("DLR Poller started successfully.");
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to start Kannel Simulator.", e);
            throw new RuntimeException(e);
        }

        LOGGER.info("========================================");
        LOGGER.info("Kannel Simulator Started");
        LOGGER.info("========================================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        LOGGER.info("========================================");
        LOGGER.info("Stopping Kannel Simulator...");
        LOGGER.info("========================================");

        try {

            if (dlrPoller != null) {
                dlrPoller.shutdown();
            }

            if (pollerThread != null) {
                pollerThread.interrupt();
                pollerThread.join(5000);
            }

            HttpSender.shutdown();
        } catch (Exception e) {
            LOGGER.error("Error while shutting down simulator.", e);
        }

        LOGGER.info("========================================");
        LOGGER.info("Simulator Stopped");
        LOGGER.info("========================================");
    }
}