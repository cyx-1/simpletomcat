package com.simpletomcat.todo;

import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for TaskManager
 */
public class TaskManagerTest {

    private TaskManager taskManager;

    @Before
    public void setUp() {
        taskManager = new TaskManager();
    }

    @Test
    public void testInitialState() {
        // Verify initial tasks are created
        List<Task> tasks = taskManager.getAllTasks();
        assertEquals("Should have 3 initial tasks", 3, tasks.size());
    }

    @Test
    public void testAddTask() {
        // Record initial count
        int initialCount = taskManager.getTaskCount();
        
        // Add a new task
        Task newTask = taskManager.addTask("Test Task", "Test Description");
        
        // Verify task was added
        assertEquals("Task count should increase by 1", initialCount + 1, taskManager.getTaskCount());
        assertTrue("Task should have a valid ID", newTask.getId() > 0);
        assertEquals("Task title should match", "Test Task", newTask.getTitle());
        assertEquals("Task description should match", "Test Description", newTask.getDescription());
        assertFalse("New task should not be completed", newTask.isCompleted());
    }

    @Test
    public void testGetTask() {
        // Add a task and get its ID
        Task addedTask = taskManager.addTask("Get Task Test", "Description for get test");
        int taskId = addedTask.getId();
        
        // Retrieve the task by ID
        Task retrievedTask = taskManager.getTask(taskId);
        
        // Verify task was retrieved correctly
        assertNotNull("Task should be found", retrievedTask);
        assertEquals("Task ID should match", taskId, retrievedTask.getId());
        assertEquals("Task title should match", "Get Task Test", retrievedTask.getTitle());
    }

    @Test
    public void testGetNonExistentTask() {
        // Try to retrieve a task with an ID that doesn't exist
        Task retrievedTask = taskManager.getTask(99999);
        
        // Verify null is returned
        assertNull("Non-existent task should return null", retrievedTask);
    }

    @Test
    public void testGetAllTasks() {
        // Add a few tasks
        taskManager.addTask("Task 1", "Description 1");
        taskManager.addTask("Task 2", "Description 2");
        
        // Get all tasks
        List<Task> allTasks = taskManager.getAllTasks();
        
        // Verify all tasks are returned
        int expectedCount = 5; // 3 initial + 2 added
        assertEquals("All tasks should be returned", expectedCount, allTasks.size());
    }

    @Test
    public void testDeleteTask() {
        // Add a task to delete
        Task taskToDelete = taskManager.addTask("Delete Me", "Task to be deleted");
        int taskId = taskToDelete.getId();
        int initialCount = taskManager.getTaskCount();
        
        // Delete the task
        boolean result = taskManager.deleteTask(taskId);
        
        // Verify the task was deleted
        assertTrue("Delete should return true for existing task", result);
        assertEquals("Task count should decrease by 1", initialCount - 1, taskManager.getTaskCount());
        assertNull("Deleted task should not be retrievable", taskManager.getTask(taskId));
    }

    @Test
    public void testDeleteNonExistentTask() {
        // Try to delete a task with an ID that doesn't exist
        boolean result = taskManager.deleteTask(99999);
        
        // Verify the operation returns false
        assertFalse("Delete should return false for non-existent task", result);
    }

    @Test
    public void testUpdateTaskStatus() {
        // Add a task to update
        Task task = taskManager.addTask("Update Status Test", "Task to update status");
        int taskId = task.getId();
        
        // Update the task status to completed
        boolean result = taskManager.updateTaskStatus(taskId, true);
        
        // Verify the status was updated
        assertTrue("Update should return true for existing task", result);
        Task updatedTask = taskManager.getTask(taskId);
        assertTrue("Task should be marked as completed", updatedTask.isCompleted());
        
        // Update the task status back to not completed
        result = taskManager.updateTaskStatus(taskId, false);
        
        // Verify the status was updated again
        assertTrue("Update should return true for existing task", result);
        updatedTask = taskManager.getTask(taskId);
        assertFalse("Task should be marked as not completed", updatedTask.isCompleted());
    }

    @Test
    public void testUpdateNonExistentTaskStatus() {
        // Try to update status of a task with an ID that doesn't exist
        boolean result = taskManager.updateTaskStatus(99999, true);
        
        // Verify the operation returns false
        assertFalse("Update should return false for non-existent task", result);
    }

    @Test
    public void testGetTaskCount() {
        // Initial count should match the number of initial tasks
        int initialCount = taskManager.getTaskCount();
        assertEquals("Initial count should be 3", 3, initialCount);
        
        // Add a task and verify count increases
        taskManager.addTask("Count Test", "Task for count test");
        assertEquals("Count should increase after adding a task", initialCount + 1, taskManager.getTaskCount());
        
        // Delete a task and verify count decreases
        Task task = taskManager.getAllTasks().get(0);
        taskManager.deleteTask(task.getId());
        assertEquals("Count should decrease after deleting a task", initialCount, taskManager.getTaskCount());
    }
} 