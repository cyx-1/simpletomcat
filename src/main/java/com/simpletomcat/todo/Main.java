package com.simpletomcat.todo;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.servlets.DefaultServlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class to launch embedded Tomcat server
 */
public class Main {
    private static final int PORT = 8080;
    private static final String CONTEXT_PATH = "";
    
    public static void main(String[] args) throws LifecycleException, ServletException, IOException {
        // Create Tomcat instance
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        
        // Create temp directory for work directory
        Path tempPath = Files.createTempDirectory("tomcat-temp");
        tomcat.setBaseDir(tempPath.toString());
        
        // Create context
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addWebapp(CONTEXT_PATH, docBase);
        
        // Setup static resources
        setupStaticResources();
        
        // Add static file serving support
        File staticFolder = new File("src/main/resources/static");
        context.setDocBase(staticFolder.getAbsolutePath());
        context.addWelcomeFile("index.html");
        
        //Tomcat.addServlet(context, "default", new DefaultServlet());
        //context.addServletMappingDecoded("/", "default");
        
        // Register our TODO service
        Tomcat.addServlet(context, "todoService", new TODOService());
        context.addServletMappingDecoded("/api/tasks/*", "todoService");
        
        // Start server
        tomcat.start();
        System.out.println("Server started on port " + PORT);
        System.out.println("Access the application at http://localhost:" + PORT + "/");
        
        // Keep server running
        tomcat.getServer().await();
    }
    
    /**
     * Set up static resources directory
     */
    private static void setupStaticResources() throws IOException {
        // Create static directory if it doesn't exist
        Path staticDir = Paths.get("src", "main", "resources", "static");
        if (!Files.exists(staticDir)) {
            Files.createDirectories(staticDir);
        }
        
        // Create HTML file if it doesn't exist
        Path htmlFile = staticDir.resolve("index.html");
        if (!Files.exists(htmlFile)) {
            createHtmlFile(htmlFile);
        }
    }
    
    /**
     * Create a simple HTML page to display tasks
     */
    private static void createHtmlFile(Path htmlFilePath) throws IOException {
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Todo List</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            max-width: 800px;\n" +
                "            margin: 0 auto;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        h1 {\n" +
                "            color: #333;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .task {\n" +
                "            border: 1px solid #ddd;\n" +
                "            padding: 15px;\n" +
                "            margin-bottom: 10px;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .task h3 {\n" +
                "            margin-top: 0;\n" +
                "        }\n" +
                "        .task.completed {\n" +
                "            background-color: #f8fff8;\n" +
                "            border-left: 5px solid #4CAF50;\n" +
                "        }\n" +
                "        .task-actions {\n" +
                "            margin-top: 10px;\n" +
                "        }\n" +
                "        button {\n" +
                "            padding: 5px 10px;\n" +
                "            margin-right: 5px;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "        .add-task {\n" +
                "            margin-bottom: 20px;\n" +
                "            padding: 15px;\n" +
                "            border: 1px solid #ddd;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .form-group {\n" +
                "            margin-bottom: 10px;\n" +
                "        }\n" +
                "        input, textarea {\n" +
                "            width: 100%;\n" +
                "            padding: 8px;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "        .submit-btn {\n" +
                "            background-color: #4CAF50;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            padding: 10px 15px;\n" +
                "            text-align: center;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Todo List</h1>\n" +
                "    \n" +
                "    <div class=\"add-task\">\n" +
                "        <h2>Add New Task</h2>\n" +
                "        <div class=\"form-group\">\n" +
                "            <label for=\"taskTitle\">Title:</label>\n" +
                "            <input type=\"text\" id=\"taskTitle\" name=\"title\" required>\n" +
                "        </div>\n" +
                "        <div class=\"form-group\">\n" +
                "            <label for=\"taskDescription\">Description:</label>\n" +
                "            <textarea id=\"taskDescription\" name=\"description\" rows=\"3\" required></textarea>\n" +
                "        </div>\n" +
                "        <button type=\"button\" class=\"submit-btn\" onclick=\"addTask()\">Add Task</button>\n" +
                "    </div>\n" +
                "    \n" +
                "    <div id=\"taskList\">\n" +
                "        <!-- Tasks will be loaded here -->\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        // Load tasks when page loads\n" +
                "        document.addEventListener('DOMContentLoaded', loadTasks);\n" +
                "        \n" +
                "        // Function to load all tasks\n" +
                "        function loadTasks() {\n" +
                "            fetch('/api/tasks')\n" +
                "                .then(response => response.json())\n" +
                "                .then(tasks => {\n" +
                "                    const taskList = document.getElementById('taskList');\n" +
                "                    taskList.innerHTML = '';\n" +
                "                    \n" +
                "                    tasks.forEach(task => {\n" +
                "                        const taskElement = createTaskElement(task);\n" +
                "                        taskList.appendChild(taskElement);\n" +
                "                    });\n" +
                "                })\n" +
                "                .catch(error => console.error('Error loading tasks:', error));\n" +
                "        }\n" +
                "        \n" +
                "        // Function to create a task element\n" +
                "        function createTaskElement(task) {\n" +
                "            const div = document.createElement('div');\n" +
                "            div.className = 'task' + (task.completed ? ' completed' : '');\n" +
                "            div.id = 'task-' + task.id;\n" +
                "            \n" +
                "            const title = document.createElement('h3');\n" +
                "            title.textContent = task.title;\n" +
                "            \n" +
                "            const description = document.createElement('p');\n" +
                "            description.textContent = task.description;\n" +
                "            \n" +
                "            const status = document.createElement('p');\n" +
                "            status.innerHTML = `<strong>Status:</strong> ${task.completed ? 'Completed' : 'Pending'}`;\n" +
                "            \n" +
                "            const actions = document.createElement('div');\n" +
                "            actions.className = 'task-actions';\n" +
                "            \n" +
                "            const toggleBtn = document.createElement('button');\n" +
                "            toggleBtn.textContent = task.completed ? 'Mark as Pending' : 'Mark as Completed';\n" +
                "            toggleBtn.onclick = () => toggleTaskStatus(task.id, !task.completed);\n" +
                "            \n" +
                "            const deleteBtn = document.createElement('button');\n" +
                "            deleteBtn.textContent = 'Delete';\n" +
                "            deleteBtn.onclick = () => deleteTask(task.id);\n" +
                "            \n" +
                "            actions.appendChild(toggleBtn);\n" +
                "            actions.appendChild(deleteBtn);\n" +
                "            \n" +
                "            div.appendChild(title);\n" +
                "            div.appendChild(description);\n" +
                "            div.appendChild(status);\n" +
                "            div.appendChild(actions);\n" +
                "            \n" +
                "            return div;\n" +
                "        }\n" +
                "        \n" +
                "        // Function to add a new task\n" +
                "        function addTask() {\n" +
                "            const title = document.getElementById('taskTitle').value.trim();\n" +
                "            const description = document.getElementById('taskDescription').value.trim();\n" +
                "            \n" +
                "            if (!title || !description) {\n" +
                "                alert('Please fill in all fields');\n" +
                "                return;\n" +
                "            }\n" +
                "            \n" +
                "            const newTask = {\n" +
                "                title: title,\n" +
                "                description: description\n" +
                "            };\n" +
                "            \n" +
                "            fetch('/api/tasks', {\n" +
                "                method: 'POST',\n" +
                "                headers: {\n" +
                "                    'Content-Type': 'application/json'\n" +
                "                },\n" +
                "                body: JSON.stringify(newTask)\n" +
                "            })\n" +
                "            .then(response => response.json())\n" +
                "            .then(task => {\n" +
                "                // Clear form\n" +
                "                document.getElementById('taskTitle').value = '';\n" +
                "                document.getElementById('taskDescription').value = '';\n" +
                "                \n" +
                "                // Add the new task to the list\n" +
                "                const taskList = document.getElementById('taskList');\n" +
                "                const taskElement = createTaskElement(task);\n" +
                "                taskList.insertBefore(taskElement, taskList.firstChild);\n" +
                "            })\n" +
                "            .catch(error => console.error('Error adding task:', error));\n" +
                "        }\n" +
                "        \n" +
                "        // Function to toggle task status\n" +
                "        function toggleTaskStatus(id, completed) {\n" +
                "            const updateTask = { completed: completed };\n" +
                "            \n" +
                "            fetch(`/api/tasks/${id}`, {\n" +
                "                method: 'PUT',\n" +
                "                headers: {\n" +
                "                    'Content-Type': 'application/json'\n" +
                "                },\n" +
                "                body: JSON.stringify(updateTask)\n" +
                "            })\n" +
                "            .then(response => response.json())\n" +
                "            .then(updatedTask => {\n" +
                "                // Update the task in the UI\n" +
                "                const taskElement = document.getElementById('task-' + id);\n" +
                "                if (updatedTask.completed) {\n" +
                "                    taskElement.classList.add('completed');\n" +
                "                } else {\n" +
                "                    taskElement.classList.remove('completed');\n" +
                "                }\n" +
                "                \n" +
                "                // Update status text\n" +
                "                const status = taskElement.querySelector('p:nth-child(3)');\n" +
                "                status.innerHTML = `<strong>Status:</strong> ${updatedTask.completed ? 'Completed' : 'Pending'}`;\n" +
                "                \n" +
                "                // Update toggle button\n" +
                "                const toggleBtn = taskElement.querySelector('button:first-child');\n" +
                "                toggleBtn.textContent = updatedTask.completed ? 'Mark as Pending' : 'Mark as Completed';\n" +
                "                toggleBtn.onclick = () => toggleTaskStatus(id, !updatedTask.completed);\n" +
                "            })\n" +
                "            .catch(error => console.error('Error updating task:', error));\n" +
                "        }\n" +
                "        \n" +
                "        // Function to delete a task\n" +
                "        function deleteTask(id) {\n" +
                "            if (confirm('Are you sure you want to delete this task?')) {\n" +
                "                fetch(`/api/tasks/${id}`, {\n" +
                "                    method: 'DELETE'\n" +
                "                })\n" +
                "                .then(response => {\n" +
                "                    if (response.ok) {\n" +
                "                        // Remove the task from the UI\n" +
                "                        const taskElement = document.getElementById('task-' + id);\n" +
                "                        taskElement.remove();\n" +
                "                    } else {\n" +
                "                        console.error('Failed to delete task');\n" +
                "                    }\n" +
                "                })\n" +
                "                .catch(error => console.error('Error deleting task:', error));\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        Files.write(htmlFilePath, htmlContent.getBytes());
    }
} 