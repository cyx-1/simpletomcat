package com.simpletomcat.todo;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * Main class to launch embedded Tomcat server
 */
public final class Main {
    private static final int PORT = 8080;
    private static final String CONTEXT_PATH = "";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String WEBAPP_DIR = "src/main/resources";
    private static final String STATIC_DIR = "src/main/resources/static";
    private static final String HTML_CONTENT = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Todo List</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    h1 {
                        color: #333;
                        text-align: center;
                    }
                    .task {
                        border: 1px solid #ddd;
                        padding: 15px;
                        margin-bottom: 10px;
                        border-radius: 5px;
                    }
                    .task h3 {
                        margin-top: 0;
                    }
                    .task.completed {
                        background-color: #f8fff8;
                        border-left: 5px solid #4CAF50;
                    }
                    .task-actions {
                        margin-top: 10px;
                    }
                    button {
                        padding: 5px 10px;
                        margin-right: 5px;
                        cursor: pointer;
                    }
                    .add-task {
                        margin-bottom: 20px;
                        padding: 15px;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                    }
                    .form-group {
                        margin-bottom: 10px;
                    }
                    input, textarea {
                        width: 100%;
                        padding: 8px;
                        box-sizing: border-box;
                    }
                    .submit-btn {
                        background-color: #4CAF50;
                        color: white;
                        border: none;
                        padding: 10px 15px;
                        text-align: center;
                        cursor: pointer;
                    }
                </style>
            </head>
            <body>
                <h1>Todo List</h1>
                
                <div class="add-task">
                    <h2>Add New Task</h2>
                    <div class="form-group">
                        <label for="taskTitle">Title:</label>
                        <input type="text" id="taskTitle" name="title" required>
                    </div>
                    <div class="form-group">
                        <label for="taskDescription">Description:</label>
                        <textarea id="taskDescription" name="description" rows="3" required></textarea>
                    </div>
                    <button type="button" class="submit-btn" onclick="addTask()">Add Task</button>
                </div>
                
                <div id="taskList">
                    <!-- Tasks will be loaded here -->
                </div>
                
                <script>
                    // Load tasks when page loads
                    document.addEventListener('DOMContentLoaded', loadTasks);
                    
                    // Function to load all tasks
                    function loadTasks() {
                        fetch('/api/tasks')
                            .then(response => response.json())
                            .then(tasks => {
                                const taskList = document.getElementById('taskList');
                                taskList.innerHTML = '';
                                
                                tasks.forEach(task => {
                                    const taskElement = createTaskElement(task);
                                    taskList.appendChild(taskElement);
                                });
                            })
                            .catch(error => console.error('Error loading tasks:', error));
                    }
                    
                    // Function to create a task element
                    function createTaskElement(task) {
                        const div = document.createElement('div');
                        div.className = 'task' + (task.completed ? ' completed' : '');
                        div.id = 'task-' + task.id;
                        
                        const title = document.createElement('h3');
                        title.textContent = task.title;
                        
                        const description = document.createElement('p');
                        description.textContent = task.description;
                        
                        const status = document.createElement('p');
                        status.innerHTML = `<strong>Status:</strong> ${task.completed ? 'Completed' : 'Pending'}`;
                        
                        const actions = document.createElement('div');
                        actions.className = 'task-actions';
                        
                        const toggleBtn = document.createElement('button');
                        toggleBtn.textContent = task.completed ? 'Mark as Pending' : 'Mark as Completed';
                        toggleBtn.onclick = () => toggleTaskStatus(task.id, !task.completed);
                        
                        const deleteBtn = document.createElement('button');
                        deleteBtn.textContent = 'Delete';
                        deleteBtn.onclick = () => deleteTask(task.id);
                        
                        actions.appendChild(toggleBtn);
                        actions.appendChild(deleteBtn);
                        
                        div.appendChild(title);
                        div.appendChild(description);
                        div.appendChild(status);
                        div.appendChild(actions);
                        
                        return div;
                    }
                    
                    // Function to add a new task
                    function addTask() {
                        const title = document.getElementById('taskTitle').value.trim();
                        const description = document.getElementById('taskDescription').value.trim();
                        
                        if (!title || !description) {
                            alert('Please fill in all fields');
                            return;
                        }
                        
                        const newTask = {
                            title: title,
                            description: description
                        };
                        
                        fetch('/api/tasks', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(newTask)
                        })
                        .then(response => response.json())
                        .then(task => {
                            // Clear form
                            document.getElementById('taskTitle').value = '';
                            document.getElementById('taskDescription').value = '';
                            
                            // Add the new task to the list
                            const taskList = document.getElementById('taskList');
                            const taskElement = createTaskElement(task);
                            taskList.insertBefore(taskElement, taskList.firstChild);
                        })
                        .catch(error => console.error('Error adding task:', error));
                    }
                    
                    // Function to toggle task status
                    function toggleTaskStatus(id, completed) {
                        const updateTask = { completed: completed };
                        
                        fetch(`/api/tasks/${id}`, {
                            method: 'PUT',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(updateTask)
                        })
                        .then(response => response.json())
                        .then(updatedTask => {
                            // Update the task in the UI
                            const taskElement = document.getElementById('task-' + id);
                            if (updatedTask.completed) {
                                taskElement.classList.add('completed');
                            } else {
                                taskElement.classList.remove('completed');
                            }
                            
                            // Update status text
                            const status = taskElement.querySelector('p:nth-child(3)');
                            status.innerHTML = `<strong>Status:</strong> ${updatedTask.completed ? 'Completed' : 'Pending'}`;
                            
                            // Update toggle button
                            const toggleBtn = taskElement.querySelector('button:first-child');
                            toggleBtn.textContent = updatedTask.completed ? 'Mark as Pending' : 'Mark as Completed';
                            toggleBtn.onclick = () => toggleTaskStatus(id, !updatedTask.completed);
                        })
                        .catch(error => console.error('Error updating task:', error));
                    }
                    
                    // Function to delete a task
                    function deleteTask(id) {
                        if (confirm('Are you sure you want to delete this task?')) {
                            fetch(`/api/tasks/${id}`, {
                                method: 'DELETE'
                            })
                            .then(response => {
                                if (response.ok) {
                                    // Remove the task from the UI
                                    const taskElement = document.getElementById('task-' + id);
                                    taskElement.remove();
                                } else {
                                    console.error('Failed to delete task');
                                }
                            })
                            .catch(error => console.error('Error deleting task:', error));
                        }
                    }
                </script>
            </body>
            </html>
            """;

    private Main() {
        // Utility class should not be instantiated
    }
    
    public static void main(String[] args) {
        try {
            startServer();
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            System.exit(1);
        }
    }

    private static void startServer() throws LifecycleException, IOException {
        // Create Tomcat instance
        Tomcat tomcat = new Tomcat();
        
        // Configure the HTTP connector
        tomcat.getConnector();
        tomcat.setPort(PORT);
        
        // Create temp directory for work directory
        Path tempPath = createTempDirectory();
        tomcat.setBaseDir(tempPath.toString());
        
        // Create context
        Context context = tomcat.addWebapp(CONTEXT_PATH, new File(WEBAPP_DIR).getAbsolutePath());
        
        // Setup static resources
        setupStaticResources();
        
        // Add static file serving support
        String staticPath = new File(STATIC_DIR).getAbsolutePath();
        context.setDocBase(staticPath);
        context.addWelcomeFile("index.html");
        
        // Register task management service
        Tomcat.addServlet(context, "todoService", new TODOService());
        context.addServletMappingDecoded("/api/tasks/*", "todoService");
        
        // Start server
        tomcat.start();
        logger.info("Server started on port {}", PORT);
        logger.info("Access the application at http://localhost:{}/", PORT);
        
        // Keep server running
        tomcat.getServer().await();
    }

    private static Path createTempDirectory() throws IOException {
        return Files.createTempDirectory("tomcat-temp");
    }
    
    private static void setupStaticResources() throws IOException {
        // Create static directory if it doesn't exist
        Path staticDir = Paths.get(STATIC_DIR);
        if (!Files.exists(staticDir)) {
            Files.createDirectories(staticDir);
        }
        
        // Create HTML file if it doesn't exist
        Path htmlFile = staticDir.resolve("index.html");
        if (!Files.exists(htmlFile)) {
            createHtmlFile(htmlFile);
        }
    }
    
    private static void createHtmlFile(Path htmlFilePath) throws IOException {
        Files.write(htmlFilePath, HTML_CONTENT.getBytes(StandardCharsets.UTF_8));
    }
} 