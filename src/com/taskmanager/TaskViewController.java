package com.taskmanager;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;


public class TaskViewController {

    // Task Details Section
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ChoiceBox<String> priorityChoiceBox;
    @FXML private DatePicker deadlinePicker;
    @FXML private ChoiceBox<Task.Status> statusChoiceBox;

    // Search and Category Management
    @FXML private TextField searchField;

    // Reminders Section
    @FXML private ChoiceBox<String> reminderTypeChoiceBox;
    @FXML private DatePicker reminderDatePicker;
    @FXML private ListView<Reminder> reminderListView;

    // Task List
    @FXML private ListView<Task> taskListView;

    // Summary Labels
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label delayedTasksLabel;
    @FXML private Label dueWithinWeekLabel;

    // Search and Category Management
    @FXML private TextField newCategoryField;
    @FXML private ComboBox<String> prioritySearchComboBox; // ADD THIS LINE
    @FXML private ComboBox<String> categorySearchComboBox; // ADD THIS LINE

    private TaskManager taskManager;
    private CategoryManager categoryManager;
    private PriorityManager priorityManager;
    private ReminderManager reminderManager;
    private Gson gson;

    private final ObservableList<Task> taskObservableList = FXCollections.observableArrayList();
    private final ObservableList<String> categoryObservableList = FXCollections.observableArrayList();
    private final ObservableList<Reminder> reminderObservableList = FXCollections.observableArrayList();

    public void setGson(Gson gson) {
        this.gson = gson;
        taskManager = new TaskManager(gson);
        categoryManager = new CategoryManager();
        priorityManager = new PriorityManager();
        reminderManager = new ReminderManager(gson);
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @FXML
    public void initialize() {
        initializeData();
    }

    private void initializeData() {
        refreshTaskList();
        refreshReminderList(null);

        categoryObservableList.addAll(categoryManager.getCategories());
        categoryComboBox.setItems(categoryObservableList);

        priorityChoiceBox.setItems(FXCollections.observableArrayList(priorityManager.getPriorities()));
        priorityChoiceBox.setValue("Default");
        statusChoiceBox.setItems(FXCollections.observableArrayList(Task.Status.values()));

        reminderTypeChoiceBox.setItems(FXCollections.observableArrayList("Specific Date", "Day Before", "Week Before", "One Month Before"));

        taskListView.setOnMouseClicked(event -> loadSelectedTask());

        Platform.runLater(() -> {
            Stage stage = (Stage) taskListView.getScene().getWindow();
            stage.setOnCloseRequest(event -> handleExit());
        });

        // --- Timer for Reminder Pop-ups ---
        Timer reminderTimer = new Timer(true); // Daemon thread, will not prevent application exit
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> checkAndShowReminders()); // Run on JavaFX Application Thread
            }
        }, 0, 60 * 1000);         // --- End Timer ---

        // --- Populate Search ComboBoxes ---
        ObservableList<String> prioritySearchOptions = FXCollections.observableArrayList();
        prioritySearchOptions.add("All Priorities"); // Option to clear filter
        prioritySearchOptions.addAll(priorityManager.getPriorities());
        prioritySearchComboBox.setItems(prioritySearchOptions);
        prioritySearchComboBox.setValue("All Priorities"); // Default selection

        ObservableList<String> categorySearchOptions = FXCollections.observableArrayList();
        categorySearchOptions.add("All Categories"); // Option to clear filter
        categorySearchOptions.addAll(categoryManager.getCategories());
        categorySearchComboBox.setItems(categorySearchOptions);
        categorySearchComboBox.setValue("All Categories"); // Default selection
        // --- End Populate Search ComboBoxes ---
    }


    private void checkAndShowReminders() {
        LocalDate today = LocalDate.now();
        List<Reminder> remindersToShow = new ArrayList<>(); // List to collect reminders to show

        for (Reminder reminder : reminderManager.getReminders()) {
            if ((reminder.getReminderDate().isEqual(today) || reminder.getReminderDate().isBefore(today)) && !reminder.isShown()) { // Check !isShown()
                remindersToShow.add(reminder); // Add to list to show
            }
        }

        for (Reminder reminder : remindersToShow) { // Iterate through collected reminders
            showReminderPopup(reminder);
            reminder.setShown(true); // Mark as shown
        }

        if (!remindersToShow.isEmpty()) { // Save only if any reminder was shown and marked as 'shown'
            reminderManager.saveReminders(); // Save reminders to persist 'isShown' status
        }
    }

    @FXML
    private void handleViewDelayedTasks() {
        taskManager.showDelayedTasksPopup();
    }


    @FXML
    private void addTask() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String category = categoryComboBox.getValue();
        String priority = priorityChoiceBox.getValue();
        LocalDate deadline = deadlinePicker.getValue();

        if (title.isEmpty()) {
            showAlert("Missing Information", "Task title cannot be empty.");
            return;
        }

        Task task = new Task(title, description, category, priority, deadline);
        taskManager.addTask(task);
        refreshTaskList();
        updateSummaryLabels(); // ADDED: Call updateSummaryLabels after refreshTaskList in addTask
        clearFields();
        refreshReminderList(null); // Refresh reminder list after task add
    }

    @FXML
    private void updateTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("No Task Selected", "Please select a task to update.");
            return;
        }

        Task.Status newStatus = statusChoiceBox.getValue() != null ? statusChoiceBox.getValue() : selectedTask.getStatus();

        taskManager.updateTask(
                selectedTask,
                titleField.getText(),
                descriptionField.getText(),
                categoryComboBox.getValue(),
                priorityChoiceBox.getValue(),
                deadlinePicker.getValue(),
                newStatus
        );
        refreshTaskList();
        updateSummaryLabels(); // ADDED: Call updateSummaryLabels after refreshTaskList in updateTask
        clearFields();
        refreshReminderList(selectedTask); // Refresh reminder list after task update, keeping selected task
    }

    @FXML
    private void removeTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("No Task Selected", "Please select a task to remove.");
            return;
        }

        System.out.println("removeTask() method in TaskViewController is called."); // ADDED DEBUG LINE
        System.out.println("Calling reminderManager.deleteRemindersForTask() for task: " + selectedTask.getTitle()); // ADDED DEBUG LINE
        reminderManager.deleteRemindersForTask(selectedTask);
        taskManager.removeTask(selectedTask);
        refreshTaskList();
        updateSummaryLabels(); // ADDED: Call updateSummaryLabels after refreshTaskList in removeTask
        clearFields();
        refreshReminderList(null); // Refresh reminder list after task remove
    }

    @FXML
    private void searchTasks() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedPriority = prioritySearchComboBox.getValue(); // Get selected priority
        String selectedCategory = categorySearchComboBox.getValue(); // Get selected category

        // Handle "All" options as null for no filter
        String priorityFilter = "All Priorities".equals(selectedPriority) ? null : selectedPriority;
        String categoryFilter = "All Categories".equals(selectedCategory) ? null : selectedCategory;


        List<Task> results = taskManager.searchTasks(query, priorityFilter, categoryFilter); // Pass filters
        taskObservableList.setAll(results);
        updateSummaryLabels(); // ADDED: Call updateSummaryLabels here, NOT refreshTaskList()
    }

    @FXML
    private void addCategory() {
        String newCategory = newCategoryField.getText().trim();
        if (!newCategory.isEmpty()) {
            categoryManager.addCategory(newCategory);
            categoryObservableList.setAll(categoryManager.getCategories());
            newCategoryField.clear();
        }
    }

    @FXML
    private void deleteCategory() {
        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null) {
            categoryManager.deleteCategory(selectedCategory, taskManager);
            categoryObservableList.setAll(categoryManager.getCategories());
            refreshTaskList();
            updateSummaryLabels(); // Summary labels need update after category delete which affects tasks
        }
    }

    @FXML
    private void addReminder() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Missing Information", "Please select a task first.");
            return;
        }

        if (selectedTask.getStatus() == Task.Status.COMPLETED) {
            showAlert("Cannot add reminder", "Cannot add reminder for Completed Task.");
            return; // Exit the method, do not add reminder
        }

        String reminderType = reminderTypeChoiceBox.getValue();
        LocalDate deadline = selectedTask.getDeadline();
        LocalDate reminderDate = reminderDatePicker.getValue();

        if (reminderType == null) {
            showAlert("Missing Information", "Please select a reminder type.");
            return;
        }

        if ("Specific Date".equals(reminderType)) {
            if (reminderDate == null) {
                showAlert("Missing Information", "Please select a specific reminder date.");
                return;
            }
        } else if ("Day Before".equals(reminderType)) {
            if (deadline == null) {
                showAlert("Invalid Reminder", "Task must have a deadline to set a 'Day Before' reminder.");
                return;
            }
            reminderDate = deadline.minusDays(1);
        } else if ("Week Before".equals(reminderType)) {
            if (deadline == null) {
                showAlert("Invalid Reminder", "Task must have a deadline to set a 'Week Before' reminder.");
                return;
            }
            reminderDate = deadline.minusWeeks(1);
        } else if ("One Month Before".equals(reminderType)) { // ADDED BLOCK
            if (deadline == null) {
                showAlert("Invalid Reminder", "Task must have a deadline to set a 'One Month Before' reminder.");
                return;
            }
            reminderDate = deadline.minusMonths(1);
        }

        if (reminderDate != null && reminderDate.isBefore(LocalDate.now())) {
            showAlert("Invalid Reminder Date", "Reminder date cannot be in the past.");
            return;
        }

        reminderManager.addReminder(selectedTask, reminderDate);
        refreshReminderList(selectedTask); // Refresh reminder list after reminder add, keeping selected task
        reminderDatePicker.setValue(null);
    }

    @FXML
    private void deleteReminder() {
        Reminder selectedReminder = reminderListView.getSelectionModel().getSelectedItem();
        if (selectedReminder == null) {
            showAlert("No Reminder Selected", "Please select a reminder to delete.");
            return;
        }

        Task associatedTask = selectedReminder.getTask();
        List<Reminder> remindersForTask = reminderManager.getRemindersForTask(associatedTask);

        remindersForTask.remove(selectedReminder);
        reminderManager.saveReminders();
        refreshReminderList(associatedTask); // Refresh reminder list after reminder delete, for associated task
    }


    private void loadSelectedTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            titleField.setText(selectedTask.getTitle());
            descriptionField.setText(selectedTask.getDescription());
            categoryComboBox.setValue(selectedTask.getCategory());
            priorityChoiceBox.setValue(selectedTask.getPriority());
            deadlinePicker.setValue(selectedTask.getDeadline());
            statusChoiceBox.setValue(selectedTask.getStatus());
            refreshReminderList(selectedTask); // Refresh reminder list when task is selected
        } else {
            refreshReminderList(null); // Refresh with null if no task selected (e.g., after clearing selection)
        }
    }

    private void refreshTaskList() {
        taskObservableList.setAll(taskManager.getTasks());
        taskListView.setItems(taskObservableList);
        updateSummaryLabels(); // ADDED: Call updateSummaryLabels at the end of refreshTaskList
    }

    private void refreshReminderList(Task selectedTask) { // Modified to take selectedTask (but now we'll ignore it for showing ALL reminders)
        refreshAllReminders(); // Reload all reminders from ReminderManager

        // Option 1: Always show ALL reminders in reminderListView
        reminderObservableList.setAll(reminderManager.getReminders()); // Set ALL reminders to the observable list

        reminderListView.setItems(reminderObservableList); // Update the ListView
    }

    private void refreshAllReminders() {
        reminderObservableList.setAll(reminderManager.getReminders()); // Method to reload ALL reminders
    }


    private void refreshReminderListForSelectedTask(Task selectedTask) { // Eliminated this method - using refreshReminderList(Task selectedTask) instead
        refreshReminderList(selectedTask);
    }

    private void clearFields() {
        titleField.clear();
        descriptionField.clear();
        categoryComboBox.setValue(null);
        priorityChoiceBox.setValue("Default");
        deadlinePicker.setValue(null);
        statusChoiceBox.setValue(Task.Status.OPEN);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showReminderPopup(Reminder reminder) {
        Task task = reminder.getTask();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Reminder!");
        alert.setHeaderText("Reminder for Task: " + task.getTitle());
        alert.setContentText("Category: " + task.getCategory() + "\n" +
                "Description: " + task.getDescription() + "\n" +
                "Deadline: " + task.getDeadline());

        alert.showAndWait(); // Show the alert and wait for the user to close it
    }

    private void handleExit() {
        taskManager.saveTasks();
        reminderManager.saveReminders();
        System.out.println("Tasks and reminders saved before exit.");
    }

    public void clearSearch(ActionEvent actionEvent) {
        searchField.clear();
        prioritySearchComboBox.setValue("All Priorities"); // Reset priority filter
        categorySearchComboBox.setValue("All Categories"); // Reset category filter
        taskObservableList.setAll(taskManager.getTasks());
        updateSummaryLabels();
    }


    @FXML
    private void manageCategories() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Category Management");
        dialog.setHeaderText("Manage Task Categories");

        // Set the button types.
        ButtonType addButtonType = new ButtonType("Add Category", ButtonBar.ButtonData.OK_DONE);
        ButtonType renameButtonType = new ButtonType("Rename Category", ButtonBar.ButtonData.APPLY);
        ButtonType deleteButtonType = new ButtonType("Delete Category", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, renameButtonType, deleteButtonType, cancelButtonType);

        // UI elements for category management
        VBox vbox = new VBox(10);
        TextField newCategoryNameField = new TextField();
        newCategoryNameField.setPromptText("Category Name");
        ComboBox<String> existingCategoryComboBox = new ComboBox<>(categoryObservableList);
        existingCategoryComboBox.setPromptText("Select Category to Rename/Delete");

        vbox.getChildren().addAll(new Label("New Category Name:"), newCategoryNameField,
                new Label("Existing Category:"), existingCategoryComboBox);

        dialog.getDialogPane().setContent(vbox);

        // Enable/Disable buttons based on selection
        Button renameButton = (Button) dialog.getDialogPane().lookupButton(renameButtonType);
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);

        renameButton.setDisable(true);
        deleteButton.setDisable(true);

        existingCategoryComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            renameButton.setDisable(newVal == null);
            deleteButton.setDisable(newVal == null);
        });

        // Result Converter (Handling button clicks)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String newCategoryName = newCategoryNameField.getText().trim();
                if (!newCategoryName.isEmpty() && !categoryObservableList.contains(newCategoryName)) {
                    addCategory(newCategoryName);
                    return "add:" + newCategoryName;
                } else if (categoryObservableList.contains(newCategoryName)) {
                    showAlert("Category Exists", "Category '" + newCategoryName + "' already exists.");
                } else {
                    showAlert("Invalid Input", "Category name cannot be empty.");
                }
            } else if (dialogButton == renameButtonType) {
                String oldCategoryName = existingCategoryComboBox.getValue();
                String newCategoryName = newCategoryNameField.getText().trim();
                if (oldCategoryName != null && !newCategoryName.isEmpty() && !categoryObservableList.contains(newCategoryName)) {
                    renameCategory(oldCategoryName, newCategoryName);
                    return "rename:" + oldCategoryName + " to " + newCategoryName;
                } else if (categoryObservableList.contains(newCategoryName)) {
                    showAlert("Category Exists", "Category '" + newCategoryName + "' already exists.");
                } else {
                    showAlert("Invalid Input", "Please select a category to rename and enter a new name.");
                }
            } else if (dialogButton == deleteButtonType) {
                String categoryToDelete = existingCategoryComboBox.getValue();
                if (categoryToDelete != null) {
                    deleteCategory(categoryToDelete);
                    return "delete:" + categoryToDelete;
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(action -> {
            System.out.println("Category Action: " + action);
            refreshCategoryList();
            clearCategoryFields();
        });
    }

    private void refreshCategoryList() {
        categoryObservableList.setAll(categoryManager.getCategories());
        categoryComboBox.setItems(categoryObservableList);
    }

    private void clearCategoryFields() {
        categoryComboBox.setValue(null);
        newCategoryField.clear();
    }

    private void addCategory(String newCategoryName) {
        categoryManager.addCategory(newCategoryName);
        categoryObservableList.add(newCategoryName);
        saveCategoriesToFile();
    }

    private void renameCategory(String oldCategoryName, String newCategoryName) {
        categoryManager.renameCategory(oldCategoryName, newCategoryName, taskManager);
        refreshCategoryList();
        saveCategoriesToFile();
    }

    private void deleteCategory(String categoryToDelete) {
        categoryManager.deleteCategory(categoryToDelete, taskManager);
        categoryObservableList.remove(categoryToDelete);
        saveCategoriesToFile();
        refreshTaskList();
        updateSummaryLabels(); // Summary labels need update after category delete which affects tasks
    }


    private void saveCategoriesToFile() {
        categoryManager.saveCategories();
    }


    @FXML
    private void managePriorities() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Priority Management");
        dialog.setHeaderText("Manage Task Priorities");

        // Set the button types.
        ButtonType addButtonType = new ButtonType("Add Priority", ButtonBar.ButtonData.OK_DONE);
        ButtonType renameButtonType = new ButtonType("Rename Priority", ButtonBar.ButtonData.APPLY);
        ButtonType deleteButtonType = new ButtonType("Delete Priority", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, renameButtonType, deleteButtonType, cancelButtonType);

        // UI elements for priority management
        VBox vbox = new VBox(10);
        TextField newPriorityNameField = new TextField();
        newPriorityNameField.setPromptText("Priority Level Name");
        ComboBox<String> existingPriorityComboBox = new ComboBox<>(FXCollections.observableArrayList(priorityManager.getPriorities()));
        // REMOVED: setPromptText for ChoiceBox - not applicable
        // existingPriorityComboBox.setPromptText("Select Priority to Rename/Delete");

        vbox.getChildren().addAll(new Label("New Priority Name:"), newPriorityNameField,
                new Label("Existing Priority:"), existingPriorityComboBox);

        dialog.getDialogPane().setContent(vbox);

        // Enable/Disable buttons based on selection
        Button renameButton = (Button) dialog.getDialogPane().lookupButton(renameButtonType);
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);

        renameButton.setDisable(true);
        deleteButton.setDisable(true);

        existingPriorityComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDefault = "Default".equals(newVal);
            renameButton.setDisable(newVal == null || isDefault);
            deleteButton.setDisable(newVal == null || isDefault);
        });


        // Result Converter (Handling button clicks)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String newPriorityName = newPriorityNameField.getText().trim();
                if (!newPriorityName.isEmpty() && !priorityManager.getPriorities().contains(newPriorityName)) {
                    addPriority(newPriorityName);
                    return "add:" + newPriorityName;
                } else if (priorityManager.getPriorities().contains(newPriorityName)) {
                    showAlert("Priority Exists", "Priority '" + newPriorityName + "' already exists.");
                }
                else {
                    showAlert("Invalid Input", "Priority name cannot be empty.");
                }
            } else if (dialogButton == renameButtonType) {
                String oldPriorityName = existingPriorityComboBox.getValue();
                String newPriorityName = newPriorityNameField.getText().trim();
                if (oldPriorityName != null && !newPriorityName.isEmpty() && !priorityManager.getPriorities().contains(newPriorityName)) {
                    renamePriority(oldPriorityName, newPriorityName);
                    return "rename:" + oldPriorityName + " to " + newPriorityName;
                } else if (priorityManager.getPriorities().contains(newPriorityName)) {
                    showAlert("Priority Exists", "Priority '" + newPriorityName + "' already exists.");
                }
                else {
                    showAlert("Invalid Input", "Please select a priority to rename and enter a new name.");
                }
            } else if (dialogButton == deleteButtonType) {
                String priorityToDelete = existingPriorityComboBox.getValue();
                if (priorityToDelete != null && !priorityToDelete.equals("Default")) {
                    deletePriority(priorityToDelete);
                    return "delete:" + priorityToDelete;
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(action -> {
            System.out.println("Priority Action: " + action);
            refreshPriorityChoiceBox();
            clearPriorityFields();
        });
    }

    private void refreshPriorityChoiceBox() {
        priorityChoiceBox.setItems(FXCollections.observableArrayList(priorityManager.getPriorities()));
    }

    private void clearPriorityFields() {
        priorityChoiceBox.setValue(null);
    }

    private void addPriority(String newPriorityName) {
        priorityManager.addPriority(newPriorityName);
        refreshPriorityChoiceBox();
        savePrioritiesToFile();
    }

    private void renamePriority(String oldPriorityName, String newPriorityName) {
        priorityManager.renamePriority(oldPriorityName, newPriorityName, taskManager);
        refreshPriorityChoiceBox();
        savePrioritiesToFile();
        refreshTaskList();
        updateSummaryLabels(); // Summary labels need update after priority rename which might affect tasks
    }


    private void deletePriority(String priorityToDelete) {
        priorityManager.deletePriority(priorityToDelete, taskManager);
        refreshPriorityChoiceBox();
        savePrioritiesToFile();
        refreshTaskList();
        updateSummaryLabels(); // Summary labels need update after priority delete which might affect tasks
    }

    private void savePrioritiesToFile() {
        priorityManager.savePriorities();
    }

    private void updateSummaryLabels() {
        updateTotalTasksCount();
        updateCompletedTasksCount();
        updateDelayedTasksCount();
        updateDueWithinWeekCount();
    }

    private void updateTotalTasksCount() {
        int totalTasks = taskManager.getTasks().size();
        totalTasksLabel.setText(String.valueOf(totalTasks));
    }

    private void updateCompletedTasksCount() {
        int completedTasks = 0;
        for (Task task : taskManager.getTasks()) {
            if (task.getStatus() == Task.Status.COMPLETED) {
                completedTasks++;
            }
        }
        completedTasksLabel.setText(String.valueOf(completedTasks));
    }

    private void updateDelayedTasksCount() {
        int delayedTasks = 0;
        for (Task task : taskManager.getTasks()) {
            if (task.getDeadline() != null && task.getDeadline().isBefore(LocalDate.now()) && task.getStatus() != Task.Status.COMPLETED) {
                delayedTasks++;
            }
        }
        delayedTasksLabel.setText(String.valueOf(delayedTasks));
    }

    private void updateDueWithinWeekCount() {
        int dueWithinWeek = 0;
        LocalDate today = LocalDate.now();
        LocalDate weekFromToday = today.plusDays(7);
        for (Task task : taskManager.getTasks()) {
            if (task.getDeadline() != null &&
                    (task.getDeadline().isEqual(today) || (task.getDeadline().isAfter(today) && task.getDeadline().isBefore(weekFromToday)))) {
                dueWithinWeek++;
            }
        }
        dueWithinWeekLabel.setText(String.valueOf(dueWithinWeek));
    }
}