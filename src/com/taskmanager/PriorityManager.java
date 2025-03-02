package com.taskmanager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PriorityManager {
    private static final String FILE_PATH = "medialab/priorities.json";
    private static final Gson gson = new Gson();
    private List<String> priorities = new ArrayList<>();

    public PriorityManager() {
        this.priorities = loadPriorities();
        if (!priorities.contains("Default")) {
            priorities.add("Default"); // Ensure "Default" always exists
        }
    }

    public List<String> getPriorities() {
        return priorities;
    }

    public void addPriority(String priority) {
        if (!priorities.contains(priority)) {
            priorities.add(priority);
            savePriorities();
        }
    }

    public void renamePriority(String oldName, String newName, TaskManager taskManager) {
        if (!oldName.equals("Default") && priorities.contains(oldName) && !priorities.contains(newName)) {
            priorities.remove(oldName);
            priorities.add(newName);

            // Update priority in tasks
            for (Task task : taskManager.getTasks()) {
                if (task.getPriority().equals(oldName)) {
                    task.setPriority(newName);
                }
            }
            taskManager.saveTasks();
            savePriorities();
        }
    }

    public void deletePriority(String priority, TaskManager taskManager) {
        if (!priority.equals("Default") && priorities.remove(priority)) {
            // Assign "Default" priority to affected tasks
            for (Task task : taskManager.getTasks()) {
                if (task.getPriority().equals(priority)) {
                    task.setPriority("Default");
                }
            }
            savePriorities();
            taskManager.saveTasks();
        }
    }

    void savePriorities() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(priorities, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> loadPriorities() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            return gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
