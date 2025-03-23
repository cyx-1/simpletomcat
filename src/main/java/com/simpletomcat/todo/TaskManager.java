package com.simpletomcat.todo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages tasks in the Simple Tomcat application
 */
public class TaskManager {
    private final List<Task> tasks;
    private final AtomicInteger idCounter;
    private final ReadWriteLock lock;

    public TaskManager() {
        this.tasks = Collections.synchronizedList(new ArrayList<>());
        this.idCounter = new AtomicInteger(1);
        this.lock = new ReentrantReadWriteLock();
        
        // Initialize with sample tasks
        addTask("Complete project", "Finish the SimpleTomcat project implementation");
        addTask("Buy groceries", "Milk, eggs, bread, and vegetables");
        addTask("Clean house", "Vacuum living room and mop kitchen");
    }

    /**
     * Add a new task with the specified title and description
     * @param title Task title
     * @param description Task description
     * @return The newly created task
     * @throws IllegalArgumentException if title or description is null or empty
     */
    public Task addTask(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }

        int id = idCounter.getAndIncrement();
        Task task = new Task(id, title.trim(), description.trim());
        
        lock.writeLock().lock();
        try {
            tasks.add(task);
            return task;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get a task by its ID
     * @param id Task ID
     * @return The task if found, otherwise null
     */
    public Task getTask(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Task ID must be positive");
        }

        lock.readLock().lock();
        try {
            return tasks.stream()
                    .filter(task -> task.getId() == id)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get all tasks
     * @return Unmodifiable list of all tasks
     */
    public List<Task> getAllTasks() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(tasks));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Delete a task by its ID
     * @param id Task ID
     * @return true if the task was found and deleted, false otherwise
     * @throws IllegalArgumentException if id is not positive
     */
    public boolean deleteTask(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Task ID must be positive");
        }

        lock.writeLock().lock();
        try {
            Optional<Task> taskToRemove = tasks.stream()
                    .filter(task -> task.getId() == id)
                    .findFirst();
            
            return taskToRemove.isPresent() && tasks.remove(taskToRemove.get());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update the completion status of a task
     * @param id Task ID
     * @param completed New completion status
     * @return true if the task was found and updated, false otherwise
     * @throws IllegalArgumentException if id is not positive
     */
    public boolean updateTaskStatus(int id, boolean completed) {
        if (id <= 0) {
            throw new IllegalArgumentException("Task ID must be positive");
        }

        lock.writeLock().lock();
        try {
            Task task = getTask(id);
            if (task != null) {
                task.setCompleted(completed);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get the current count of tasks
     * @return The number of tasks
     */
    public int getTaskCount() {
        lock.readLock().lock();
        try {
            return tasks.size();
        } finally {
            lock.readLock().unlock();
        }
    }
} 