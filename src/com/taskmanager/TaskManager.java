package com.taskmanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String FILE_PATH = "medialab/tasks.json";
    private List<Task> tasks = new ArrayList<>();
    private final Gson gson;

    public TaskManager(Gson gson) {
        this.gson = gson;
        this.tasks = loadTasks();
        updateOverdueTasks(); // Mark overdue tasks when loading
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    public void updateTask(Task oldTask, String title, String description, String category, String priority, LocalDate deadline, Task.Status status) {
        oldTask.setTitle(title);
        oldTask.setDescription(description);
        oldTask.setCategory(category);
        oldTask.setPriority(priority);
        oldTask.setDeadline(deadline);
        oldTask.setStatus(status);
        saveTasks();
    }

    public void removeTask(Task taskToRemove) {
        tasks.remove(taskToRemove);
        saveTasks();
    }

    public List<Task> searchTasks(String query, String priorityFilter, String categoryFilter) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            boolean nameMatch = query == null || query.isEmpty() || task.getTitle().toLowerCase().contains(query);
            boolean priorityMatch = priorityFilter == null || priorityFilter.isEmpty() || task.getPriority().equals(priorityFilter);
            boolean categoryMatch = categoryFilter == null || categoryFilter.isEmpty() || task.getCategory().equals(categoryFilter);

            if (nameMatch && priorityMatch && categoryMatch) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    public void updateOverdueTasks() {
        LocalDate today = LocalDate.now();
        for (Task task : tasks) {
            if (task.getStatus() != Task.Status.COMPLETED && task.getDeadline() != null && task.getDeadline().isBefore(today)) {
                task.setStatus(Task.Status.DELAYED);
            }
        }
        saveTasks();
    }

    public void saveTasks() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks saved to " + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Task> loadTasks() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type taskListType = new TypeToken<ArrayList<Task>>() {}.getType();
            List<Task> loadedTasks = gson.fromJson(reader, taskListType);
            if (loadedTasks != null) {
                System.out.println("Tasks loaded from " + FILE_PATH);
                return loadedTasks;
            }
        } catch (IOException e) {
            System.out.println("No tasks file found or error reading " + FILE_PATH + ", starting with empty tasks.");
        }
        return new ArrayList<>();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void showDelayedTasksPopup() {
        StringBuilder delayedTasks = new StringBuilder();
        for (Task task : tasks) {
            if (task.getStatus() == Task.Status.DELAYED) {
                delayedTasks.append(task.getTitle()).append(" (Due: ").append(task.getDeadline()).append(")\n");
            }
        }

        if (delayedTasks.length() > 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Delayed Tasks");
                alert.setHeaderText("The following tasks are overdue:");
                alert.setContentText(delayedTasks.toString());
                alert.showAndWait();
            });
        }
    }
}
