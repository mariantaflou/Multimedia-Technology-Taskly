package com.taskmanager;

import java.time.LocalDate;
import java.util.Objects;

public class Reminder {
    private Task task;
    private LocalDate reminderDate;
    private boolean isShown; // ADDED: isShown field

    public Reminder(Task task, LocalDate reminderDate) {
        this.task = task;
        this.reminderDate = reminderDate;
        this.isShown = false; // Initialize isShown to false by default
    }

    public Task getTask() {
        return task;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    // ADDED: Getter for isShown
    public boolean isShown() {
        return isShown;
    }

    // ADDED: Setter for isShown
    public void setShown(boolean shown) {
        isShown = shown;
    }

    @Override
    public String toString() {
        return "Reminder for Task: " + task.getTitle() + " on " + reminderDate.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(task, reminder.task) && // Compare based on associated Task
                Objects.equals(reminderDate, reminder.reminderDate); // and reminderDate
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, reminderDate); // HashCode based on Task and reminderDate
    }
}