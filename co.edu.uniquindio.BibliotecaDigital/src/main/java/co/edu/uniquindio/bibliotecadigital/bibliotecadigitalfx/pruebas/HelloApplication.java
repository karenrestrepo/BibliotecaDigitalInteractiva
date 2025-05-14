package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.pruebas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/AdministratorView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 832);
        stage.setTitle("Hello!");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
