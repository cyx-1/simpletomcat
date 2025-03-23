package com.simpletomcat.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages tasks in the TODO application
 */
public class TaskManager {
    private final List<Task> tasks;
    private final AtomicInteger idCounter;

    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.idCounter = new AtomicInteger(1);
        
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
     */
    public Task addTask(String title, String description) {
        int id = idCounter.getAndIncrement();
        Task task = new Task(id, title, description);
        tasks.add(task);
        return task;
    }

    /**
     * Get a task by its ID
     * @param id Task ID
     * @return The task if found, otherwise null
     */
    public Task getTask(int id) {
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all tasks
     * @return List of all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Delete a task by its ID
     * @param id Task ID
     * @return true if the task was found and deleted, false otherwise
     */
    public boolean deleteTask(int id) {
        Optional<Task> taskToRemove = tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst();
        
        return taskToRemove.isPresent() && tasks.remove(taskToRemove.get());
    }

    /**
     * Update the completion status of a task
     * @param id Task ID
     * @param completed New completion status
     * @return true if the task was found and updated, false otherwise
     */
    public boolean updateTaskStatus(int id, boolean completed) {
        Task task = getTask(id);
        if (task != null) {
            task.setCompleted(completed);
            return true;
        }
        return false;
    }

    /**
     * Get the current count of tasks
     * @return The number of tasks
     */
    public int getTaskCount() {
        return tasks.size();
    }
} 