package com.ptsl.kannelsimulator.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptsl.kannelsimulator.model.DlrRequest;

public final class DlrUrlBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static Logger logger = LoggerFactory.getLogger(DlrUrlBuilder.class);
    private static final DateTimeFormatter DLR_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private DlrUrlBuilder() {

    }

    /**
     * Build Delivered DLR URL.
     */
    public static String buildDeliveredUrl(DlrRequest request) {


        if("REJECT".equalsIgnoreCase(request.getSenderId()))
        {
            return build(request, "UNDELIV" ,"001" , "1");
        }

        return build(
                request,
                "DELIVRD",
                "000",
                "0");
    }


    public static boolean isDlrHandoverDisabled() {

        String value = System.getenv("DISABLE-DLR-HANDOVER");

        logger.error(
                "Environment variable DISABLE-DLR-HANDOVER=[{}]",
                value);

        return Boolean.parseBoolean(value);
    }
    /**
     * Build Failed DLR URL.
     */
    public static String buildFailedUrl(
            DlrRequest request,
            String deliveryStatus,
            String errorCode) {

        return build(
                request,
                deliveryStatus,
                errorCode,
                "1");
    }

    private static String build(
            DlrRequest request,
            String deliveryStatus,
            String errorCode,
            String statusCode) {

        if (request == null || request.getDlrUrl() == null) {
            return null;
        }

        String url = request.getDlrUrl();

        String messageId = UUID.randomUUID().toString().replace("-" , "");

        String dr = buildDr(
                messageId,
                deliveryStatus,
                errorCode);

        url = url.replace("%a", encode(dr));
        url = url.replace("%i", encode(nullSafe(request.getSmscId())));
        url = url.replace("%d", encode(statusCode));
        url = url.replace("%o", encode(nullSafe(request.getUser())));

        return url;
    }

    /**
     * Build SMPP Delivery Receipt.
     */
    private static String buildDr(
            String messageId,
            String deliveryStatus,
            String errorCode) {

        String timestamp = LocalDateTime.now()
                .format(DLR_DATE_FORMAT);

        String dlvrd =
                "DELIVRD".equals(deliveryStatus)
                        ? "001"
                        : "000";

        return "id:" + messageId
                + " sub:001"
                + " dlvrd:" + dlvrd
                + " submit date:" + timestamp
                + " done date:" + timestamp
                + " stat:" + deliveryStatus
                + " err:" + errorCode
                + " text:dlr";
    }

    /**
     * Extract m_id from add_info JSON.
     */
    private static String extractMessageId(String addInfo) {

        if (addInfo == null || addInfo.isBlank()) {
            return "UNKNOWN";
        }

        try {

            JsonNode root =
                    OBJECT_MAPPER.readTree(addInfo);

            JsonNode node = root.get("m_id");

            if (node != null) {
                return node.asText();
            }

        } catch (Exception ignored) {

        }

        return "UNKNOWN";
    }

    private static String encode(String value) {

        return URLEncoder.encode(
                nullSafe(value),
                StandardCharsets.UTF_8);
    }

    private static String nullSafe(String value) {

        return value == null ? "" : value;
    }
}