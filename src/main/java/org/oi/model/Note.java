package org.oi.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Note {
    private final String id;
    private String title;
    private String content;
    private final LocalDateTime createdAt;
    private LocalDateTime lastModified;

    public Note(String title, String content) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.lastModified = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateLastModified();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        updateLastModified();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }
}
