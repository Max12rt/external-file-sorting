package org.example;

public record SortStats(
        int phases,
        long diskReads,
        long diskWrites,
        long durationMs
) {}
