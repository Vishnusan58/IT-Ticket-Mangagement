package com.ittm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChangeRequest {
    private final int id;
    private final User requester;
    private String title;
    private String description;
    private ChangeRequestStatus status;
    private LocalDate expiryDate;
    private boolean archived;
    private String implementationNote;
    private LocalDateTime createdAt;

    public ChangeRequest(int id, User requester, String title, String description, LocalDate expiryDate, LocalDateTime createdAt) {
        this.id = id;
        this.requester = requester;
        this.title = title;
        this.description = description;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
        this.status = ChangeRequestStatus.RAISED;
    }

    public int getId() {
        return id;
    }

    public User getRequester() {
        return requester;
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

    public ChangeRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ChangeRequestStatus status) {
        this.status = status;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getImplementationNote() {
        return implementationNote;
    }

    public void setImplementationNote(String implementationNote) {
        this.implementationNote = implementationNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
