package ar.edu.itba.protos.transport.metrics;

import java.util.LongSummaryStatistics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import com.google.inject.Singleton;

import ar.edu.itba.protos.transport.support.Attachment;

@Singleton
public class Metrics {

    private final ConcurrentMap<Attachment, LongAdder> accesses = new ConcurrentHashMap<>();
    private final ConcurrentMap<Attachment, LongAdder> bytesSent = new ConcurrentHashMap<>();
    private final ConcurrentMap<Attachment, LongAdder> bytesReceived = new ConcurrentHashMap<>();
    private final ConcurrentMap<Attachment, LongAdder> executedCommands = new ConcurrentHashMap<>();

    public void logAccess(final Attachment attachment) {
        accesses.computeIfAbsent(attachment, i -> new LongAdder()).increment();
    }

    public void logBytesReceived(final Attachment attachment, final Long bytesNumber) {
        bytesReceived.computeIfAbsent(attachment, i -> new LongAdder()).add(bytesNumber);
    }

    public void logBytesSent(final Attachment attachment, final Long bytesNumber) {
        bytesSent.computeIfAbsent(attachment, i -> new LongAdder()).add(bytesNumber);
    }

    public void logExecutedCommands(final Attachment attachment) {
        executedCommands.computeIfAbsent(attachment, i -> new LongAdder()).increment();
    }

    public Long accesses(final Attachment attachment) {
        return accesses.getOrDefault(attachment, new LongAdder()).longValue();
    }

    public Long bytesSent(final Attachment attachment) {
        return bytesSent.getOrDefault(attachment, new LongAdder()).longValue();
    }

    public Long bytesTransfered(final Attachment attachment) {
        return bytesReceived.getOrDefault(attachment, new LongAdder()).longValue();
    }

    public Long commands(final Attachment attachment) {
        return executedCommands.getOrDefault(attachment, new LongAdder()).longValue();
    }

    public String summarize() {
        final long commands = executedCommands.values().stream()
                .mapToLong(a -> a.longValue())
                .sum();

        final long accessesL = accesses.values().stream()
                .mapToLong(a -> a.longValue())
                .sum();

        return String.join("\r\n", new String[] {
                "Stats:",
                "",
                "  - commands:    " + commands + " commands executed.",
                "  - sent:        " + summarizeTotal(bytesSent),
                "  - received:    " + summarizeTotal(bytesReceived),
                "  - connections: " + accessesL + " (over lifetime)."
        });
    }

    public String summarizeTotal(final ConcurrentMap<Attachment, LongAdder> in) {
        final LongSummaryStatistics summary = in.values().stream()
                .mapToLong(a -> a.longValue())
                .summaryStatistics();

        return String.format("max: %10d bytes,\t min: %10d bytes,\t avg: %10.5f bytes",
                summary.getMax() == Long.MIN_VALUE ? 0 : summary.getMax(),
                        summary.getMin() == Long.MAX_VALUE ? 0 : summary.getMin(),
                                summary.getAverage());
    }
}
