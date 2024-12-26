package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.controllers.HomePageController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    private Process flaskProcess;
    private Thread flaskThread;
    private Thread flaskOutputThread;

    @Override
    public void start(Stage primaryStage) {
        startFlaskApp();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/HomePage.fxml"));
            // move stage to hompage controller
            loader.setControllerFactory(controllerClass -> new HomePageController(primaryStage));
            VBox root = loader.load();

            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("Alpha Vantage Data Downloader");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> {
                try {
                    stopFlaskApp();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start flask app automatically with opening of fx GUI.
     */
    private void startFlaskApp() {
        flaskThread = new Thread(() -> { // new thread to allow clean closing of both flask app and java app
            try {
                String command = "cmd.exe /c set FLASK_APP=backend.alpha_vantage_api&&" +
                        " flask run --host=127.0.0.1 --port=5000";
                ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
                flaskProcess = processBuilder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        flaskThread.setDaemon(true); // make flaskThread background thread
        flaskThread.start();
    }

    /**
     * get PID of flask process to delete after app closes
     * @return PID as integer
     * @throws Exception if unable to split and cast PID message into integer
     */
    private int getFlaskPid() throws Exception {
        Process findPIDProcess = new
                ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr :5000").start();
        BufferedReader pidReader = new BufferedReader(new InputStreamReader(findPIDProcess.getInputStream()));
        String pidLine;
        int pidToReturn = 0;
        while ((pidLine = pidReader.readLine()) != null) {
            String[] parts = pidLine.trim().split("\\s+");
            // pidLine returns whole log, only want the last part that is the actual id
            String pidStr = parts[parts.length - 1];
            try {
                pidToReturn = Integer.parseInt(pidStr);
            } catch (NumberFormatException e) {
                throw new Exception(e);
            }
        }
        findPIDProcess.destroy();
        return pidToReturn;
    }
    private void stopFlaskApp() throws Exception {
        if (flaskProcess != null) {
            flaskProcess.destroy();
            Runtime.getRuntime().exec("taskkill /F /PID " + getFlaskPid()); // kill flask process with given PID
            try {
                if (flaskProcess.waitFor(5, TimeUnit.SECONDS)) {
                    System.out.println("Flask process destroyed successfully.");
                } else {
                    System.out.println("Flask process destroyed forcibly.");
                }
            } catch (InterruptedException e) {
                System.out.println("Error while waiting for Flask process termination: " + e.getMessage());
            }
        }
        System.exit(0);
    }


    public static void main(String[] args) {
        launch();
    }
}
