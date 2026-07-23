package com.ptsl.kannelsimulator.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSender.class);

    private static volatile HttpClient CLIENT;

    private HttpSender() {

    }

    /**
     * Initialize HTTP Client.
     * Call once during application startup.
     */
    public static synchronized void initialize() {

        if (CLIENT != null) {
            return;
        }

        CLIENT = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        LOGGER.info("Java HttpClient initialized.");
    }

    /**
     * Send HTTP GET asynchronously.
     */
    public static CompletableFuture<HttpResponse<String>> sendGet(String url) {

        if (CLIENT == null) {
            throw new IllegalStateException("HttpSender is not initialized.");
        }

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();

        return CLIENT.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Shutdown hook.
     */
    public static void shutdown() {
        LOGGER.info("HttpSender shutdown completed.");
    }
}