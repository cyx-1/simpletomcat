package com.simpletomcat.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet to handle HTTP requests for the TODO application
 */
public class TODOService extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final TaskManager taskManager;
    private final ObjectMapper objectMapper;
    
    public TODOService() {
        this.taskManager = new TaskManager();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // List all tasks
            listAllTasks(resp);
        } else {
            try {
                // Parse task ID from path
                int taskId = Integer.parseInt(pathInfo.substring(1));
                getTaskById(taskId, resp);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid task ID");
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Parse request body to get task details
            BufferedReader reader = req.getReader();
            String requestBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            Task taskRequest = objectMapper.readValue(requestBody, Task.class);
            
            // Create a new task
            Task newTask = taskManager.addTask(taskRequest.getTitle(), taskRequest.getDescription());
            
            // Return the created task
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(newTask));
            out.flush();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Failed to create task: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Task ID is required");
            return;
        }
        
        try {
            // Parse task ID from path
            int taskId = Integer.parseInt(pathInfo.substring(1));
            boolean deleted = taskManager.deleteTask(taskId);
            
            if (deleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Task deleted successfully");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Task not found");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid task ID");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Task ID is required");
            return;
        }
        
        try {
            // Parse task ID from path
            int taskId = Integer.parseInt(pathInfo.substring(1));
            BufferedReader reader = req.getReader();
            String requestBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            Task taskRequest = objectMapper.readValue(requestBody, Task.class);
            
            Task existingTask = taskManager.getTask(taskId);
            
            if (existingTask != null) {
                // Update task status
                taskManager.updateTaskStatus(taskId, taskRequest.isCompleted());
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                
                // Return the updated task
                existingTask = taskManager.getTask(taskId);
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(existingTask));
                out.flush();
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Task not found");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid task ID");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Failed to update task: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to list all tasks
     */
    private void listAllTasks(HttpServletResponse resp) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(objectMapper.writeValueAsString(tasks));
        out.flush();
    }
    
    /**
     * Helper method to get a task by ID
     */
    private void getTaskById(int taskId, HttpServletResponse resp) throws IOException {
        Task task = taskManager.getTask(taskId);
        
        if (task != null) {
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(task));
            out.flush();
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Task not found");
        }
    }
} 