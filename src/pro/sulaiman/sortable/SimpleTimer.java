package pro.sulaiman.sortable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SimpleTimer {
    private static final Map<String, SimpleDuration> timers = new HashMap<>();

    public static void toggle(String timerName) {
        timers.computeIfAbsent(timerName, k -> new SimpleDuration()).toggle();
    }

    public static Duration getDuration(String timerName) {
        if (timers.get(timerName) == null) return Duration.ZERO;

        return timers.get(timerName).getDuration();
    }

    private static class SimpleDuration {
        private Instant start, end;
        private Duration acc = Duration.ZERO;

        public void toggle() {
            if (start == null) start = Instant.now();
            else if (end == null) end = Instant.now();
            else {
                acc = acc.plus(Duration.between(start, end));
                start = Instant.now();
                end = null;
            }
        }

        Duration getDuration() {
            start = start == null ? Instant.now() : start;
            end = end == null ? start : end;
            return acc.plus(Duration.between(start, end));
        }
    }
}
