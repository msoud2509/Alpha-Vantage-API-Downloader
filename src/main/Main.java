package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.controllers.HomePageController;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/HomePage.fxml"));
            // move stage to hompage controller
            loader.setControllerFactory(controllerClass -> new HomePageController(primaryStage));
            VBox root = loader.load();

            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("JavaFX Application");
            primaryStage.setScene(scene);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
