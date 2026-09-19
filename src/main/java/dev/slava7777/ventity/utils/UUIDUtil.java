package dev.slava7777.ventity.utils;

import java.util.UUID;

public class UUIDUtil {
    public static long key(UUID uuid) {
        return uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
    }
}
