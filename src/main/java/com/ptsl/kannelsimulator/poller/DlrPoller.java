package com.ptsl.kannelsimulator.poller;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ptsl.kannelsimulator.http.HttpSender;
import com.ptsl.kannelsimulator.model.DlrRequest;
import com.ptsl.kannelsimulator.queue.DlrQueueHolder;
import com.ptsl.kannelsimulator.util.DlrUrlBuilder;

public class DlrPoller implements Runnable {

    private static final Logger LOGGER       = LoggerFactory.getLogger(DlrPoller.class);
    private static final int    MAX_INFLIGHT = 500;
    private final Semaphore semaphore = new Semaphore(MAX_INFLIGHT);
    private volatile boolean running = true;

    private static final ExecutorService VIRTUAL_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();
    @Override
    public void run() {

        LOGGER.info("DLR Poller Started.");
        while (running) {
            try {

                DlrRequest request = DlrQueueHolder.getInstance().poll(1, TimeUnit.SECONDS);
                if (request == null) {
                    continue;
                }

                semaphore.acquire();
                try {
                    VIRTUAL_EXECUTOR.submit(() -> process(request));
                } catch (Exception e) {
                    semaphore.release();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.error("Unexpected error.", e);
            }
        }

        LOGGER.info("DLR Poller Stopped.");
    }

    private void process(DlrRequest request) {

        try {

            String callbackUrl = DlrUrlBuilder.buildDeliveredUrl(request);

            if (callbackUrl == null) {
                return;
            }

            HttpSender.sendGet(callbackUrl).whenComplete((response, throwable) -> {

                try {
                    if (throwable != null) {
                        LOGGER.error("Unable to send DLR: {}", callbackUrl,  throwable);
                    } else if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("DLR Sent. HTTP={}", response.statusCode());
                    }
                } finally {
                    semaphore.release();
                }
            });

        } catch (Exception e) {
            semaphore.release();
            LOGGER.error("Unable to process DLR.", e);
        }
    }

    public void shutdown() {
        running = false;
    }

}