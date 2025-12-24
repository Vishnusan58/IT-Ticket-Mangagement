package com.ittm.model;

import java.time.LocalDateTime;

public class Note {
    private final int authorId;
    private final String authorName;
    private final String message;
    private final LocalDateTime createdAt;

    public Note(int authorId, String authorName, String message, LocalDateTime createdAt) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "[" + createdAt + "] " + authorName + ": " + message;
    }
}
