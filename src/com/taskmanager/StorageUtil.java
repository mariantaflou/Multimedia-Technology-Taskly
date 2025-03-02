package com.taskmanager;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import com.taskmanager.LocalDateAdapter;


public class StorageUtil {
    private static final String DIRECTORY_PATH = "medialab";
    private static final String TASKS_FILE = DIRECTORY_PATH + "/tasks.json";
    private static final String REMINDERS_FILE = DIRECTORY_PATH + "/reminders.json";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();


    private static void ensureDirectoryExists() {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static List<Task> loadTasks() {
        ensureDirectoryExists();
        File file = new File(TASKS_FILE);
        if (!file.exists()) {
            System.out.println("tasks.json not found, returning empty list.");
            return List.of();
        }
        try (Reader reader = new FileReader(TASKS_FILE)) {
            Type taskListType = new TypeToken<List<Task>>() {}.getType();
            List<Task> tasks = gson.fromJson(reader, taskListType);
            System.out.println("Loaded tasks: " + tasks);
            return tasks;
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static void saveTasks(List<Task> tasks) {
        ensureDirectoryExists();
        try (Writer writer = new FileWriter(TASKS_FILE)) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Reminder> loadReminders() {
        ensureDirectoryExists();
        File file = new File(REMINDERS_FILE);
        if (!file.exists()) {
            System.out.println("reminders.json not found, returning empty list.");
            return List.of();
        }
        try (Reader reader = new FileReader(REMINDERS_FILE)) {
            Type reminderListType = new TypeToken<List<Reminder>>() {}.getType();
            List<Reminder> reminders = gson.fromJson(reader, reminderListType);
            System.out.println("Loaded reminders: " + reminders);
            return reminders;
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
