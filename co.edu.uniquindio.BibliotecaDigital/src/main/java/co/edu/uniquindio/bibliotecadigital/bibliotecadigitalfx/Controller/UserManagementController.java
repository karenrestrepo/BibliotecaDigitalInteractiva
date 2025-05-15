package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.beans.property.SimpleStringProperty;
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
    private TableView<Reader> tableReader;

    @FXML
    private TableColumn<Reader, String> tcLoans;

    @FXML
    private TableColumn<Reader, String> tcName;

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
            loadReadersTable();
        } else {
            showAlert("Registro fallido", "El nombre de usuario ya está en uso.");
        }

    }

    @FXML
    void onDelete(ActionEvent event) {
        Reader selectedReader = tableReader.getSelectionModel().getSelectedItem();

        if (selectedReader == null) {
            showAlert("Selección requerida", "Por favor seleccione un lector para eliminar.");
            return;
        }

        boolean removed = library.deleteReader(selectedReader.getUsername());
        if (removed) {
            showAlert("Eliminado", "El lector ha sido eliminado correctamente.");
            loadReadersTable();
        } else {
            showAlert("Error", "No se pudo eliminar el lector.");
        }
    }


    @FXML
    void onUpdate(ActionEvent event) {
        Reader selectedReader = tableReader.getSelectionModel().getSelectedItem();

        if (selectedReader == null) {
            showAlert("Selección requerida", "Por favor seleccione un lector para actualizar.");
            return;
        }

        String newName = txtName.getText();
        String newPassword = txtPassword.getText();

        if (newName.isEmpty() || newPassword.isEmpty()) {
            showAlert("Campos vacíos", "Por favor ingrese un nuevo nombre y contraseña.");
            return;
        }

        boolean updated = library.updateReader(selectedReader.getUsername(), newName, newPassword);
        if (updated) {
            showAlert("Actualizado", "El lector ha sido actualizado correctamente.");
            loadReadersTable();
        } else {
            showAlert("Error", "No se pudo actualizar el lector.");
        }
    }


    @FXML
    void initialize() {
        persistence = new Persistence();
        library = new Library(persistence);

        // Configurar columnas
        tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        tcLoans.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getLoanHistoryList().getSize())));

        // Configurar selección única
        tableReader.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Actualizar campos al seleccionar
        tableReader.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtName.setText(newSelection.getName());
                txtUser.setText(newSelection.getUsername());
                txtPassword.setText(newSelection.getPassword());
            }
        });

        loadReadersTable();
    }

    private void loadReadersTable() {
        tableReader.getItems().clear();
        for (int i = 0; i < library.getReaders().getSize(); i++) {
            tableReader.getItems().add(library.getReaders().getAmountNodo(i));
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

