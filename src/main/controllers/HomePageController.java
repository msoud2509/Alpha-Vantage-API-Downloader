package main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;


import java.awt.Desktop;
import java.net.URI;

public class HomePageController {

    @FXML
    private Button getStarted;

    @FXML
    private void handleGetStarted() {
        System.out.println("Button clicked!");
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
