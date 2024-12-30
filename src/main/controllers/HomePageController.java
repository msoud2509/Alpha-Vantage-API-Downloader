package main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.DocumentationLoader;


import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HomePageController {
    private Stage primaryStage;

    public HomePageController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private Button getStarted;

    @FXML
    private void handleGetStarted() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/DownloadPage.fxml"));
            loader.setControllerFactory(controllerClass -> new DownloadPageController(this.primaryStage));
            Parent root = loader.load();

            Scene newScene = new Scene(root);
            primaryStage.setScene(newScene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private Button aboutMenuButton;

    @FXML
    private Hyperlink usageLimitLink;

    @FXML
    private void handleUsageLimitURL() {
        try {
            URI uri = new URI("https://www.alphavantage.co/support" +
                    "/#:~:text=Are%20there%20usage%2Ffrequency%20limits,to%2025%20requests%20per%20day.");
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
