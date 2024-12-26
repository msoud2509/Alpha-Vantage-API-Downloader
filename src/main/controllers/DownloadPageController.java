package main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import javafx.scene.control.TextField;

public class DownloadPageController {
    private String api_key = "marc";
    private String symbol = "harv";
    private String function = "lose";

    @FXML
    private Button downloadButton;
    @FXML
    private TextField apiField;
    @FXML
    private TextField tickerField;

    @FXML
    private void handleDownload() {
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("symbol", symbol);
        jsonData.addProperty("function", function);
        jsonData.addProperty("api_key", api_key);

        String url = "http://127.0.0.1:5000/download";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // send request
            System.out.println("Response Code: " + response.statusCode()); // check status code
            System.out.println("Response Body: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
