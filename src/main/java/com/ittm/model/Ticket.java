package com.ittm.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private final int id;
    private final User requester;
    private User assignedAgent;
    private String category;
    private String title;
    private String description;
    private TicketStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer rating;
    private boolean agentFlagged;
    private final List<Note> notes = new ArrayList<>();
    private final List<TicketHistoryEntry> history = new ArrayList<>();

    public Ticket(int id, User requester, String title, String description, String category, LocalDateTime createdAt) {
        this.id = id;
        this.requester = requester;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = TicketStatus.RAISED;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public User getRequester() {
        return requester;
    }

    public User getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(User assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public boolean isAgentFlagged() {
        return agentFlagged;
    }

    public void setAgentFlagged(boolean agentFlagged) {
        this.agentFlagged = agentFlagged;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<TicketHistoryEntry> getHistory() {
        return history;
    }
}
