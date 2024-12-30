package main.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.File;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import main.DocumentationLoader;


public class DownloadPageController implements Initializable {
    private Stage primaryStage;
    private JsonObject functions = DocumentationLoader.getAPIFunctions();

    @FXML
    private TextField apiField;

    @FXML
    private ChoiceBox sectionChoiceBox;

    @FXML
    private ChoiceBox functionChoiceBox;

    @FXML
    private TextFlow functionNameFlow;

    private Text functionNameText = new Text("");

    @FXML
    private GridPane paramPane;

    private TextField[] requiredParamsArr;

    private TextField[] optionalParamsArr;

    @FXML
    private Text parameterWarningText = new Text();

    @FXML
    private Button downloadButton;

    public DownloadPageController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        downloadButton.setDisable(true);

        // enable button if apifield is not empty and a function is selected
        apiField.textProperty().addListener((observableValue, oldVal, newVal) ->
                downloadButton.setDisable(newVal.trim().isEmpty() || functionChoiceBox.getValue() == null));

        functionNameText.setStyle("-fx-fill: #49b3ec; -fx-font-style: italic;");
        functionNameFlow.getChildren().add(functionNameText);
        for (String elem : functions.keySet()) {
            sectionChoiceBox.getItems().add(elem);
        }
        sectionChoiceBox.setOnAction(this::handleSectionChange);
        functionChoiceBox.setOnAction(this::handleFunctionChange);
    }

    private void handleSectionChange(Event e) {
        functionNameText.setText("");
        paramPane.getChildren().clear();
        functionChoiceBox.getItems().clear();
        downloadButton.setDisable(true);
        String section = (String) sectionChoiceBox.getValue();
        JsonObject functionObjects = functions.getAsJsonObject(section);
        for (String functionHeader : functionObjects.keySet()) {
            functionChoiceBox.getItems().add(functionHeader);
        }
    }

    private void handleFunctionChange(Event e) {
        if (!apiField.getText().isEmpty()) {
            downloadButton.setDisable(false);
        }
        paramPane.getChildren().clear();
        String sectionHeader = (String) sectionChoiceBox.getValue();
        String functionHeader = (String) functionChoiceBox.getValue();
        if (functionHeader == null) {
            return;
        }
        String functionName = functions.get(sectionHeader).getAsJsonObject().get(functionHeader)
                .getAsJsonObject().get("function").getAsString();
        functionNameText.setText(functionName);


        JsonArray requiredParams = null;
        JsonArray optionalParams = null;
        try {
            requiredParams = getParams("Required");
            optionalParams = getParams("Optional");
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        int row = 0;
        int col = 0;
        requiredParamsArr = new TextField[requiredParams.size()];
        int idx = 0;
        for (JsonElement elem : requiredParams) {
            VBox paramContainer = new VBox();
            Text paramLabel = new Text(elem.getAsString());
            Text reqSymbol = new Text("*");
            reqSymbol.setStyle("-fx-fill: red;");

            TextField currField = new TextField();
            requiredParamsArr[idx] = currField;
            idx++;

            paramContainer.getChildren().addAll(new TextFlow(paramLabel, reqSymbol), currField);
            paramPane.add(paramContainer, col, row);
            col++;
            if (col > 3) {
                row++;
                col = 0;
            }
        }
        for (JsonElement elem : optionalParams) {
            VBox paramContainer = new VBox();
            paramContainer.getChildren().addAll(new Text(elem.getAsString()), new TextField());
            paramPane.add(paramContainer, col, row);
            col++;
            if (col > 3) {
                row++;
                col = 0;
            }
        }
    }

    private JsonArray getParams(String reqOrOpt) throws Exception {
        if (!reqOrOpt.equals("Required") && !reqOrOpt.equals("Optional")) {
            throw new Exception("first parameter must be either 'Required' or 'Optional' for getParams method");
        } else {
            String sectionHeader = (String) sectionChoiceBox.getValue();
            String functionHeader = (String) functionChoiceBox.getValue();

            JsonObject paramObj = functions.get(sectionHeader).getAsJsonObject().get(functionHeader)
                    .getAsJsonObject().get("parameters").getAsJsonObject();
            return paramObj.get(reqOrOpt).getAsJsonArray();
        }
    }

    @FXML
    private void handleDownload() {
        JsonArray requiredParams = null;
        try {
            requiredParams = getParams("Required");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("function", functionNameText.getText());
        jsonData.addProperty("apikey", apiField.getText());
        for (int i = 0; i < requiredParamsArr.length; i++) {
            if (requiredParamsArr[i].getText().isEmpty()) {
                parameterWarningText.setText("One or more of the required parameter fields is not filled. " +
                        "All required fields are denoted by a (*).");
                return;
            } else {
                assert requiredParams != null; // shouldn't be null but intellij doesn't like me not having this so yeah
                jsonData.addProperty(requiredParams.get(i).getAsString(), requiredParamsArr[i].getText());
            }
        }

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
                InputStream inputStream = response.body();

                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select folder to place file...");
                File directory = directoryChooser.showDialog(primaryStage);
                String savePath = directory.getAbsolutePath() + File.separator + "test.csv";

                Files.copy(inputStream, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING); // Save the CSV file
                System.out.println("File downloaded successfully to: " + savePath);
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

}
