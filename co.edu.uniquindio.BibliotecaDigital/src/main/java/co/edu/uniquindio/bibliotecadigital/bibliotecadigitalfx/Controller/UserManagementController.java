package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserManagementController {
    Persistence persistence;
    Library library;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnUpdate;

    @FXML
    private TableView<?> tableReader;

    @FXML
    private TableColumn<?, ?> tcLoans;

    @FXML
    private TableColumn<?, ?> tcName;

    @FXML
    private TextField txtFilterbook;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPassword;

    @FXML
    private TextField txtUser;

    @FXML
    void onAdd(ActionEvent event) {


    }

    @FXML
    void onDelete(ActionEvent event) {
        String name = txtName.getText();
        String username = txtUser.getText();
        String password = txtPassword.getText();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos para registrarse.");
            return;
        }

        boolean success = library.registerReader(name, username, password);
        if (success) {
            showAlert("Registro exitoso", "El lector ha sido registrado correctamente.");
            txtName.clear();
            txtUser.clear();
            txtPassword.clear();
        } else {
            showAlert("Registro fallido", "El nombre de usuario ya está en uso.");
        }

    }

    @FXML
    void onUpdate(ActionEvent event) {

    }

    @FXML
    void initialize() {

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

