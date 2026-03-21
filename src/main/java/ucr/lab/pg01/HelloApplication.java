package ucr.lab.pg01;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class HelloApplication extends Application {

    public static void main(String[] args) {

        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        start2(stage);
    }

    public void start2(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 720);

        scene.getStylesheets().add(HelloApplication.class.getResource("styles.css").toExternalForm());
        stage.setTitle("PG-01 IF-3001 Algortimos y Estructuras de Datos");
        stage.setScene(scene);
        stage.show();

    }

}