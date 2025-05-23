package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Administrator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
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

    @FXML
    void onLogin(ActionEvent event) {
        String username = txtUser.getText();
        String password = txtPassword.getText();

        // Validación de campos vacíos
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            showAlert("Campos vacíos", "Por favor ingrese usuario y contraseña.");
            return;
        }

        try {
            Persistence persistence = new Persistence();
            Person user = persistence.login(username.trim(), password.trim());

            if (user instanceof Administrator) {
                openView("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/AdministratorView.fxml", "Panel de Administrador");
            } else if (user instanceof Reader) {
                openView("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/ReaderView.fxml", "Panel de Lector");
            } else {
                showAlert("Error de inicio de sesión", "Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
            showAlert("Error", "Error al intentar iniciar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onRegister(ActionEvent event) {
        String name = txtNewName.getText();
        String username = txtNewUser.getText();
        String password = txtNewPassword.getText();

        // Validación de campos vacíos
        if (name == null || name.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos para registrarse.");
            return;
        }

        // Validación de longitud mínima
        if (password.trim().length() < 4) {
            showAlert("Contraseña inválida", "La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        try {
            Library library = Library.getInstance();
            boolean success = library.registerReader(name.trim(), username.trim(), password.trim());

            if (success) {
                showAlert("Registro exitoso", "El lector ha sido registrado correctamente.");
                clearRegistrationFields();
            } else {
                showAlert("Registro fallido", "El nombre de usuario ya está en uso.");
            }
        } catch (Exception e) {
            showAlert("Error", "Error al registrar usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane pane = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            stage.setTitle(title);
            stage.show();

            // Cierra la ventana actual
            Stage currentStage = (Stage) btnLogin.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            showAlert("Error", "No se pudo cargar la vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearRegistrationFields() {
        txtNewName.clear();
        txtNewUser.clear();
        txtNewPassword.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void initialize() {
        // Configuración inicial si es necesaria
        System.out.println("LoginController inicializado correctamente");
    }
}
