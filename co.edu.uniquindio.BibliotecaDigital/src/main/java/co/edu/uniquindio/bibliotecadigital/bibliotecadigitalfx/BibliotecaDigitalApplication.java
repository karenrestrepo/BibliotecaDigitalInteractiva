package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller.LoginController;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller.ReaderController;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BibliotecaDigitalApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Inicializar la biblioteca singleton
        Library.getInstance();

        mostrarLogin();
    }

    public static void mostrarLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(BibliotecaDigitalApplication.class.getResource("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login - Biblioteca Digital");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void mostrarAdministrador() throws IOException {
        FXMLLoader loader = new FXMLLoader(BibliotecaDigitalApplication.class.getResource("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/AdministratorView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Administrador - Biblioteca Digital");
        primaryStage.show();
    }

    public static void mostrarLector() throws IOException {
        FXMLLoader loader = new FXMLLoader(BibliotecaDigitalApplication.class.getResource("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/ReaderView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Panel del Lector - Biblioteca Digital");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}