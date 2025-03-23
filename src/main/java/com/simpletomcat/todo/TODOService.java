package com.simpletomcat.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet to handle HTTP requests for the Simple Tomcat application
 */
public class TODOService extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TODOService.class);
    private static final String APPLICATION_JSON = "application/json";
    private static final String TASK_NOT_FOUND = "Task not found";
    private static final String INVALID_TASK_ID = "Invalid task ID";
    private static final String TASK_ID_REQUIRED = "Task ID is required";
    
    private final TaskManager taskManager;
    private final ObjectMapper objectMapper;
    
    public TODOService() {
        this.taskManager = new TaskManager();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // List all tasks
                listAllTasks(resp);
            } else {
                // Parse task ID from path
                int taskId = parseTaskId(pathInfo);
                getTaskById(taskId, resp);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid task ID format in request: {}", pathInfo, e);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, INVALID_TASK_ID);
        } catch (Exception e) {
            logger.error("Error processing GET request", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String requestBody = readRequestBody(req);
            Task taskRequest = objectMapper.readValue(requestBody, Task.class);
            
            // Create a new task
            Task newTask = taskManager.addTask(taskRequest.getTitle(), taskRequest.getDescription());
            
            // Return the created task
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, newTask);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid task data in request", e);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing POST request", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create task: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, TASK_ID_REQUIRED);
            return;
        }
        
        try {
            int taskId = parseTaskId(pathInfo);
            boolean deleted = taskManager.deleteTask(taskId);
            
            if (deleted) {
                sendSuccessResponse(resp, "Task deleted successfully");
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, TASK_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid task ID format in delete request: {}", pathInfo, e);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, INVALID_TASK_ID);
        } catch (Exception e) {
            logger.error("Error processing DELETE request", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, TASK_ID_REQUIRED);
            return;
        }
        
        try {
            int taskId = parseTaskId(pathInfo);
            String requestBody = readRequestBody(req);
            Task taskRequest = objectMapper.readValue(requestBody, Task.class);
            
            Task existingTask = taskManager.getTask(taskId);
            
            if (existingTask != null) {
                taskManager.updateTaskStatus(taskId, taskRequest.isCompleted());
                existingTask = taskManager.getTask(taskId);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, existingTask);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, TASK_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid task ID format in update request: {}", pathInfo, e);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, INVALID_TASK_ID);
        } catch (Exception e) {
            logger.error("Error processing PUT request", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update task: " + e.getMessage());
        }
    }
    
    private void listAllTasks(HttpServletResponse resp) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        sendJsonResponse(resp, HttpServletResponse.SC_OK, tasks);
    }
    
    private void getTaskById(int taskId, HttpServletResponse resp) throws IOException {
        Task task = taskManager.getTask(taskId);
        
        if (task != null) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, task);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, TASK_NOT_FOUND);
        }
    }
    
    private int parseTaskId(String pathInfo) {
        return Integer.parseInt(pathInfo.substring(1));
    }
    
    private String readRequestBody(HttpServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
    
    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType(APPLICATION_JSON);
        try (PrintWriter out = resp.getWriter()) {
            out.print(objectMapper.writeValueAsString(data));
            out.flush();
        }
    }
    
    private void sendSuccessResponse(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = resp.getWriter()) {
            out.write(message);
            out.flush();
        }
    }
    
    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        try (PrintWriter out = resp.getWriter()) {
            out.write(message);
            out.flush();
        }
    }
} 