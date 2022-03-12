package si.microgramm.adyen.common;

import java.util.UUID;

/**
 * Using first 10 chars of UUID should ensure uniqueness for 48h
 */
public class ServiceIdGenerator {

    private static ServiceIdGenerator instance;

    public static synchronized ServiceIdGenerator getInstance() {
        if (instance == null) {
            instance = new ServiceIdGenerator();
        }

        return instance;
    }

    public String generate() {
        return toShortUUID(UUID.randomUUID().toString());
    }

    private String toShortUUID(String uuid) {
        return uuid.replaceAll("-", "").substring(0, 10);
    }
}
