package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.BibliotecaDigitalApplication;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Administrator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private TextField txtNewName;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private TextField txtUser;
    @FXML private TextField txtNewUser;

    @FXML
    void onLogin(ActionEvent event) {
        String username = txtUser.getText();
        String password = txtPassword.getText();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            showAlert("Campos vacíos", "Por favor ingrese usuario y contraseña.");
            return;
        }

        try {
            Persistence persistence = new Persistence();
            Person user = persistence.login(username.trim(), password.trim());

            if (user instanceof Administrator) {
                Persistence.setCurrentUser(user);
                BibliotecaDigitalApplication.mostrarAdministrador();
            } else if (user instanceof Reader) {
                Persistence.setCurrentUser(user);
                BibliotecaDigitalApplication.mostrarLector();
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

        if (name == null || name.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos para registrarse.");
            return;
        }

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
        System.out.println("LoginController inicializado correctamente");
    }
}