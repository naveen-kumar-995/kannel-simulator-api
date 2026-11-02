package com.ptsl.kannelsimulator.util;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptsl.kannelsimulator.model.DlrRequest;

public final class DlrUrlBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final List<String> specificHeaders = Arrays.asList(
            "CANBNK", "HDFCBK", "ICICIB", "AXISBK", "SBIOTP",
            "PAYTMB", "AMAZON", "FLIPKT", "MYBANK", "UPIOTP"
    );

    private static AtomicLong dnReceivedCounter = new AtomicLong();

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
        } else if (specificHeaders.contains(request.getSenderId())) {

           long  count =   dnReceivedCounter.incrementAndGet();
            if(count % 50 == 0)
            {
                return build(request, "UNDELIV" ,"001" , "1");
            }
            else if(count % 101 ==0)
            {
                return build(request, "REJECTD" ,"002" , "1");
            }
        }

        return build(
                request,
                "DELIVRD",
                "000",
                "0");
    }


    public static boolean isDlrHandoverDisabled() {

        return Boolean.parseBoolean(
                System.getenv()
                        .getOrDefault(
                                "DISABLE_DLR_HANDOVER",
                                "false"));

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