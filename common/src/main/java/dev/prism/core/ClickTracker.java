package dev.prism.core;

import java.util.ArrayDeque;

/** Counts mouse clicks in the last second. Poll it every rendered frame for accuracy. */
public final class ClickTracker {
    private final ArrayDeque<Long> left = new ArrayDeque<>();
    private final ArrayDeque<Long> right = new ArrayDeque<>();
    private boolean lastL, lastR;

    public void poll(boolean l, boolean r) {
        long now = System.currentTimeMillis();
        if (l && !lastL) left.addLast(now);
        if (r && !lastR) right.addLast(now);
        lastL = l;
        lastR = r;
        prune(left, now);
        prune(right, now);
    }

    private static void prune(ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
    }

    public int left() { return left.size(); }

    public int right() { return right.size(); }
}
