package main.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import com.google.gson.JsonObject;
import main.DocumentationLoader;


public class DownloadPageController implements Initializable {

    @FXML
    private TextField apiField;
    @FXML
    private TextField tickerField;
    @FXML
    private Button downloadButton;

    @FXML
    private void handleDownload() {
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("symbol", tickerField.getText());
        jsonData.addProperty("api_key", apiField.getText());

        String url = "http://127.0.0.1:5000/download";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData.toString()))
                    .build();

            // send request
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                String savePath = "C:/Users/msoud/Downloads/test_file.csv";
                InputStream inputStream = response.body();
                Files.copy(inputStream, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING); // Save the CSV file
                System.out.println("File downloaded successfully to " + savePath);
            } else {
                System.out.println("Error getting file: " + response.statusCode());
                try (InputStream errorStream = response.body()) {
                    String errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("Error response body: " + errorBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject functions = DocumentationLoader.getAPIFunctions();
    @FXML
    private ChoiceBox sectionChoiceBox;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (String elem : functions.keySet()) {
            sectionChoiceBox.getItems().add(elem);
        }
        sectionChoiceBox.setOnAction(this::handleFunctionChoiceBox);
    }

    @FXML
    private ChoiceBox functionChoiceBox;
    private void handleFunctionChoiceBox(Event e) {
        String section = (String) sectionChoiceBox.getValue();
        functionChoiceBox.getItems().clear();
        JsonArray functionNames = functions.getAsJsonArray(section);
        for (JsonElement name : functionNames) {
            functionChoiceBox.getItems().add(name.getAsString());
        }
    }

}
