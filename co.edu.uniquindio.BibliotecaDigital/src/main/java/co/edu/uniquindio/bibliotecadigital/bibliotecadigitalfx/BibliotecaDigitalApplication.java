package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller.LoginController;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller.ReaderController;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class BibliotecaDigitalApplication extends Application {

    private static Stage primaryStage;
    private static Persistence persistence; // contenedor de datos

    static {
        persistence = new Persistence();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        mostrarLogin();
    }

    public static void mostrarLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(BibliotecaDigitalApplication.class.getResource("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/Login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setPersistence(persistence);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login - Biblioteca Digital");
        primaryStage.show();
    }

    public static void mostrarAdministrador() throws IOException {
        FXMLLoader loader = new FXMLLoader(BibliotecaDigitalApplication.class.getResource("AdministradorView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Administrador - Biblioteca Digital");
    }

    public static void mostrarLector(Reader reader) throws IOException {
        FXMLLoader loader = new FXMLLoader(BibliotecaDigitalApplication.class.getResource("ReaderView.fxml"));
        Parent root = loader.load();
        ReaderController controller = loader.getController();
        controller.setLector(reader);
        controller.setPersistence(persistence);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Lector - Biblioteca Digital");
    }

    public static void main(String[] args) {
        launch();
    }
}
