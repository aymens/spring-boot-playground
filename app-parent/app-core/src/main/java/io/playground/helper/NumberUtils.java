package io.playground.helper;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {
    public static BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
    }
}
