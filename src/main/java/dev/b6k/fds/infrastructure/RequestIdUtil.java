package dev.b6k.fds.infrastructure;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@UtilityClass
public class RequestIdUtil {
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    public static String getCurrentRequestId() {
        return MDC.get(REQUEST_ID_MDC_KEY);
    }
}