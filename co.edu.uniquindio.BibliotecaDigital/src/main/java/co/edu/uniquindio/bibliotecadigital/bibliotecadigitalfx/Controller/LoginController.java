package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Administrator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private TextField txtNewName;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private TextField txtUser;

    @FXML
    private TextField txtNewUser;

    private Persistence persistence;
    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    @FXML
    void onLogin(ActionEvent event) {

        String username = txtUser.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Campos vacíos", "Por favor ingrese usuario y contraseña.");
            return;
        }

        Object user = persistence.login(username, password);


        if (user instanceof Administrator) {
            openView("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/AdministratorView.fxml");
        } else if (user instanceof Reader) {
            openView("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/ReaderView.fxml");
        } else {
            showAlert("Error de inicio de sesión", "Usuario o contraseña incorrectos.");
        }
    }

    @FXML
    void onRegister(ActionEvent event) {
        String name = txtNewName.getText();
        String username = txtNewUser.getText();
        String password = txtNewPassword.getText();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos para registrarse.");
            return;
        }

        boolean success = persistence.registerReader(name, username, password);
        if (success) {
            showAlert("Registro exitoso", "El lector ha sido registrado correctamente.");
            txtNewName.clear();
            txtNewUser.clear();
            txtNewPassword.clear();
        } else {
            showAlert("Registro fallido", "El nombre de usuario ya está en uso.");
        }
    }

    private void openView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane pane = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            stage.setTitle("Vista del sistema");
            stage.show();

            // Cierra la ventana actual
            Stage currentStage = (Stage) btnLogin.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            showAlert("Error", "No se pudo cargar la vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void initialize() {


    }
}
