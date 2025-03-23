# Simple Todo Application with Embedded Tomcat

A Java 8 application that provides a simple Todo list functionality with a REST API.

## Features

- Add new tasks
- List all tasks
- Delete tasks
- Mark tasks as completed/pending
- Pre-populated with sample tasks
- Web interface to interact with tasks

## Technologies Used

- Java 8
- Embedded Tomcat (no web.xml required)
- Maven for build and dependency management
- JUnit 4 for testing
- JaCoCo for code coverage
- JavaScript/HTML/CSS for the frontend interface

## Project Structure

- `Main.java` - Launches the embedded Tomcat server
- `Task.java` - Model class representing a TODO item
- `TaskManager.java` - Manages tasks and provides operations like add, list, and delete
- `TODOService.java` - Servlet to handle HTTP requests for task operations
- `TaskManagerTest.java` - Unit tests for TaskManager

## Running the Application

### Prerequisites

- Java 8 (Path: C:\Program Files\Amazon Corretto\jre8)
- Maven

### Commands

To build the project:

```
mvn clean package
```

To run the application:

```
java -jar target/todo-1.0-SNAPSHOT.jar
```

Or directly using Maven:

```
mvn exec:java -Dexec.mainClass="com.simpletomcat.todo.Main"
```

### Accessing the Application

Once started, the application will be available at:

- Web Interface: http://localhost:8080/
- API Endpoint: http://localhost:8080/api/tasks

## API Endpoints

- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get a specific task by ID
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}` - Update a task (status)
- `DELETE /api/tasks/{id}` - Delete a task

## Testing

To run the tests and generate a coverage report:

```
mvn test jacoco:report
```

The JaCoCo code coverage report will be available in `target/site/jacoco`. 