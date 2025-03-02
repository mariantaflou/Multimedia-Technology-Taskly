package com.taskmanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {
    private static final String FILE_PATH = "medialab/categories.json";
    private List<String> categories = new ArrayList<>();
    private final Gson gson; // Add Gson field

    public CategoryManager() {
        this.gson = new Gson(); // Initialize Gson here if needed for loading
        this.categories = loadCategories();
    }

    public List<String> getCategories() {
        return categories;
    }

    public void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
            saveCategories();
        }
    }

    public void renameCategory(String oldName, String newName, TaskManager taskManager) {
        if (categories.contains(oldName) && !categories.contains(newName)) {
            categories.remove(oldName);
            categories.add(newName);

            // Update category in tasks (improved String comparison)
            for (Task task : taskManager.getTasks()) {
                if (oldName.equals(task.getCategory())) { // Use .equals()
                    task.setCategory(newName);
                }
            }

            taskManager.updateOverdueTasks(); // Call the method
            saveCategories();
            taskManager.saveTasks();
        }
    }

    public void deleteCategory(String category, TaskManager taskManager) {
        if (categories.remove(category)) {
            // Remove tasks associated with this category (improved with lambda)
            taskManager.getTasks().removeIf(task -> category.equals(task.getCategory())); // Use .equals()
            saveCategories();
            taskManager.saveTasks();
        }
    }

    void saveCategories() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(categories, writer);
            System.out.println("Categories saved successfully."); // Added for feedback
        } catch (IOException e) {
            e.printStackTrace(); // Handle or log the exception
        }
    }

    private List<String> loadCategories() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            List<String> loadedCategories = gson.fromJson(reader, new TypeToken<List<String>>() {
            }.getType());
            return loadedCategories == null ? new ArrayList<>() : loadedCategories; // Handle null case
        } catch (IOException e) {
            return new ArrayList<>(); // Return empty list if file not found
        }
    }
}