package com.arena.ticketing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    public static double calculatePercentage(long part, long total) {
        if (total == 0) return 0.0;
        double percentage = (double) part / total * 100;
        return round(percentage, 4);
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}