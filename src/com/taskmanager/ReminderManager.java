package com.taskmanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReminderManager {
    private static final String FILE_PATH = "medialab/reminders.json";
    private List<Reminder> reminders = new ArrayList<>();
    private final Gson gson;

    public ReminderManager(Gson gson) {
        this.gson = gson;
        this.reminders = loadReminders();
    }

    private List<Reminder> loadReminders() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            List<Reminder> loadedReminders = gson.fromJson(reader, new TypeToken<List<Reminder>>() {}.getType());
            return loadedReminders == null ? new ArrayList<>() : loadedReminders;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }




    public void addReminder(Task task, LocalDate reminderDate) {
        if (task.getStatus() == Task.Status.COMPLETED) {
            System.out.println("Cannot add a reminder for a completed task.");
            return;
        }
        for (Reminder r : reminders) {
            if (r.getTask().equals(task) && r.getReminderDate().equals(reminderDate)) {
                System.out.println("Reminder already exists for this date.");
                return;
            }
        }
        reminders.add(new Reminder(task, reminderDate));
        saveReminders();
    }

    public void deleteRemindersForTask(Task task) {
        System.out.println("deleteRemindersForTask() method in ReminderManager is called for task: " + task.getTitle()); // Already present

        System.out.println("Number of reminders before deletion attempt: " + reminders.size()); // ADD THIS LINE

        int initialReminderCount = reminders.size(); // Store initial count

        reminders.removeIf(reminder -> {
            boolean isEqual = reminder.getTask().equals(task);
            System.out.println("  Checking reminder for task: " + reminder.getTask().getTitle() +
                    ", isEquals to task to delete (" + task.getTitle() + "): " + isEqual +
                    ", Reminder Task hashCode: " + reminder.getTask().hashCode() +
                    ", Task to Delete hashCode: " + task.hashCode()); // ADD THIS BLOCK

            return isEqual;
        });

        int remindersRemovedCount = initialReminderCount - reminders.size(); // Calculate removed count
        System.out.println("Number of reminders removed: " + remindersRemovedCount); // ADD THIS LINE
        System.out.println("Number of reminders after deletion attempt: " + reminders.size()); // ADD THIS LINE

        saveReminders();
    }

    public void saveReminders() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(reminders, writer);
            System.out.println("Reminders saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Reminder> getRemindersForTask(Task task) {
        List<Reminder> remindersForTask = new ArrayList<>();
        for (Reminder reminder : reminders) {
            if (reminder.getTask().equals(task)) {  // Ensure Reminder has a reference to its Task
                remindersForTask.add(reminder);
            }
        }
        return remindersForTask;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }
}