package com.taskmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class Main extends Application {

    private static Main instance;
    private Gson gson;
    private TaskManager taskManager;

    public Main() {
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        taskManager = new TaskManager(gson); // Initialize TaskManager
        taskManager.updateOverdueTasks(); // Mark delayed tasks
        taskManager.showDelayedTasksPopup(); // Show popup if there are delayed tasks

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/taskview.fxml"));
        fxmlLoader.setControllerFactory(c -> {
            try {
                Object controller = c.getDeclaredConstructor().newInstance();
                if (controller instanceof TaskViewController) {
                    ((TaskViewController) controller).setGson(gson);
                    ((TaskViewController) controller).setTaskManager(taskManager);
                }
                return controller;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to create controller", e);
            }
        });

        var scene = new Scene(fxmlLoader.load(), 600, 400);
        primaryStage.setTitle("Taskly");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Gson getGson() {
        return gson;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
