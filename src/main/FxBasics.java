package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FxBasics extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        StackPane layout = new StackPane();
        Scene scene = new Scene(layout, 300, 300);
        stage.setTitle("Fx start");
        stage.show();
    }
}
