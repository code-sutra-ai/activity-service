package io.code.sutra.activity.util;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public static long getNextId() {
        return ID_GENERATOR.getAndIncrement();
    }
}