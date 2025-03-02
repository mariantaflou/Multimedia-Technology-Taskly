package com.taskmanager;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a task in a task management application.
 * Each task has a title, description, category, priority, deadline, and status.
 *
 * @author Maria Elpida Ntaflou
 * @version 1.0
 * @since 19-02-2025
 */
public class Task {
    /**
     * Enum representing the possible statuses of a task.
     * <ul>
     *     <li>{@code OPEN}: Task is newly created and not yet started.</li>
     *     <li>{@code IN_PROGRESS}: Task is currently being worked on.</li>
     *     <li>{@code POSTPONED}: Task has been delayed and will be addressed later.</li>
     *     <li>{@code COMPLETED}: Task is finished and done.</li>
     *     <li>{@code DELAYED}: Task deadline has passed + it is not yet completed.</li>
     * </ul>
     */
    public enum Status {
        OPEN, IN_PROGRESS, POSTPONED, COMPLETED, DELAYED
    }

    private String title;
    private String description;
    private String category;
    private String priority;
    private LocalDate deadline;
    private Status status;

    /**
     * Constructs a new Task object.
     * Initializes a task with a title, description, category, priority, and deadline.
     * The status is automatically set to {@link Status#OPEN}.
     * If priority is null, it defaults to "Default".
     *
     * @param title       The title of the task. Must not be null or empty.
     * @param description A detailed description of the task.
     * @param category    The category to which the task belongs.
     * @param priority    The priority level of the task (e.g., "High", "Medium", "Low"). Defaults to "Default" if null.
     * @param deadline    The deadline for completing the task. Can be null if no deadline.
     * @throws NullPointerException if title is null.
     * @throws IllegalArgumentException if title is empty.
     */
    public Task(String title, String description, String category, String priority, LocalDate deadline) {
        if (title == null) {
            throw new NullPointerException("Task title cannot be null");
        }
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : "Default";
        this.deadline = deadline;
        this.status = Status.OPEN;
    }

    /**
     * Gets the title of the task.
     *
     * @return The title of the task.
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the task.
     *
     * @param title The new title for the task. Must not be null or empty.
     * @throws NullPointerException if title is null.
     * @throws IllegalArgumentException if title is empty.
     */
    public void setTitle(String title) {
        if (title == null) {
            throw new NullPointerException("Task title cannot be null");
        }
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        this.title = title;
    }

    /**
     * Gets the description of the task.
     *
     * @return The description of the task.
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the task.
     *
     * @param description The new description for the task.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the category of the task.
     *
     * @return The category of the task.
     */
    public String getCategory() { return category; }

    /**
     * Sets the category of the task.
     *
     * @param category The new category for the task.
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * Gets the priority of the task.
     *
     * @return The priority of the task.
     */
    public String getPriority() { return priority; }

    /**
     * Sets the priority of the task.
     *
     * @param priority The new priority for the task.
     */
    public void setPriority(String priority) { this.priority = priority; }

    /**
     * Gets the deadline of the task.
     *
     * @return The deadline of the task, can be null if no deadline is set.
     */
    public LocalDate getDeadline() { return deadline; }

    /**
     * Sets the deadline of the task.
     *
     * @param deadline The new deadline for the task. Can be null to remove deadline.
     */
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    /**
     * Gets the status of the task.
     *
     * @return The status of the task.
     */
    public Status getStatus() { return status; }

    /**
     * Sets the status of the task.
     *
     * @param status The new status for the task.
     */
    public void setStatus(Status status) { this.status = status; }

    /**
     * Checks if the task is overdue and updates its status to {@link Status#DELAYED} if the deadline
     * has passed and the task is not already marked as {@link Status#COMPLETED}.
     * This method should be called periodically to ensure task statuses are up-to-date.
     */
    public void checkAndUpdateStatus() {
        if (status != Status.COMPLETED && deadline != null && deadline.isBefore(LocalDate.now())) {
            this.status = Status.DELAYED;
        }
    }

    /**
     * Returns a string representation of the Task object.
     * The string includes the title, category, status, deadline, and priority of the task.
     *
     * @return A formatted string representing the task.
     */
    @Override
    public String toString() {
        return String.format("%s [%s] - %s (Deadline: %s, Priority: %s)",
                title, category, status, deadline, priority);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Tasks are considered equal if all their properties (title, description, category,
     * priority, deadline, and status) are the same.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                Objects.equals(category, task.category) &&
                Objects.equals(priority, task.priority) &&
                Objects.equals(deadline, task.deadline) &&
                status == task.status;
    }

    /**
     * Returns a hash code value for the Task object.
     * This hash code is based on all significant fields of the Task object to ensure
     * consistency with the {@link #equals(Object)} method.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, description, category, priority, deadline, status);
    }
}