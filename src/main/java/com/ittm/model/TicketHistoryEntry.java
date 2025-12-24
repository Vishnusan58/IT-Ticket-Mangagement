package com.ittm.model;

import java.time.LocalDateTime;

public class TicketHistoryEntry {
    private final LocalDateTime timestamp;
    private final String action;
    private final String performedBy;

    public TicketHistoryEntry(LocalDateTime timestamp, String action, String performedBy) {
        this.timestamp = timestamp;
        this.action = action;
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getAction() {
        return action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + action + " by " + performedBy;
    }
}
