package com.simpletomcat.todo;

import java.util.Objects;

/**
 * Represents a task in the Simple Tomcat application
 */
public class Task {
    private final int id;
    private String title;
    private String description;
    private boolean completed;

    public Task() {
        this.id = 0;
    }

    public Task(int id, String title, String description) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        this.completed = false;
    }

    public Task(int id, String title, String description, boolean completed) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        this.description = description.trim();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                '}';
    }
} 