package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserManagementController {
    Reader readerSelect;
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
    private TextField txtFilterReader;

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

        // Validación de campos vacíos
        if (name == null || name.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos para registrar un lector.");
            return;
        }

        // Validación de longitud mínima de contraseña
        if (password.trim().length() < 4) {
            showAlert("Contraseña inválida", "La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        try {
            Library library = Library.getInstance();
            boolean success = library.registerReader(name.trim(), username.trim(), password.trim());

            if (success) {
                showAlert("Registro exitoso", "El lector ha sido registrado correctamente.");
                clearFields();
                loadReadersTable();
            } else {
                showAlert("Registro fallido", "El nombre de usuario ya está en uso.");
            }
        } catch (Exception e) {
            showAlert("Error", "Error al registrar lector: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onDelete(ActionEvent event) {
        Reader selectedReader = tableReader.getSelectionModel().getSelectedItem();

        if (selectedReader == null) {
            showAlert("Selección requerida", "Por favor seleccione un lector para eliminar.");
            return;
        }

        try {
            Library library = Library.getInstance();
            boolean removed = library.deleteReader(selectedReader.getUsername());

            if (removed) {
                showAlert("Eliminado", "El lector ha sido eliminado correctamente.");
                clearFields();
                loadReadersTable();
            } else {
                showAlert("Error", "No se pudo eliminar el lector.");
            }
        } catch (Exception e) {
            showAlert("Error", "Error al eliminar lector: " + e.getMessage());
            e.printStackTrace();
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

        // Validación de campos vacíos
        if (newName == null || newName.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
            showAlert("Campos vacíos", "Por favor ingrese un nuevo nombre y contraseña.");
            return;
        }

        // Validación de longitud mínima de contraseña
        if (newPassword.trim().length() < 4) {
            showAlert("Contraseña inválida", "La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        try {
            Library library = Library.getInstance();
            boolean updated = library.updateReader(selectedReader.getUsername(), newName.trim(), newPassword.trim());

            if (updated) {
                showAlert("Actualizado", "El lector ha sido actualizado correctamente.");
                clearFields();
                loadReadersTable();
            } else {
                showAlert("Error", "No se pudo actualizar el lector.");
            }
        } catch (Exception e) {
            showAlert("Error", "Error al actualizar lector: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        try {
            setupTableColumns();
            setupTableSelection();
            setupFilterListener();
            loadReadersTable();
            listenerSelection();

            System.out.println("UserManagementController inicializado correctamente");

            // Registrar este controlador para comunicación con otros controladores
            ControllerRegistry.getInstance().registerController("UserManagementController", this);

        } catch (Exception e) {
            showAlert("Error de inicialización", "Error al inicializar el controlador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        // Configurar columnas de la tabla
        tcName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        tcLoans.setCellValueFactory(cellData -> {
            try {
                int loanCount = cellData.getValue().getLoanHistoryList().getSize();
                return new SimpleStringProperty(String.valueOf(loanCount));
            } catch (Exception e) {
                return new SimpleStringProperty("0");
            }
        });
    }

    private void setupTableSelection() {
        // Configurar selección única en la tabla
        tableReader.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Listener para actualizar campos al seleccionar un lector
        tableReader.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
    }

    private void setupFilterListener() {
        // Listener para el filtro de búsqueda
        txtFilterReader.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReaders(newValue);
        });
    }

    private void populateFields(Reader reader) {
        txtName.setText(reader.getName());
        txtUser.setText(reader.getUsername());
        txtPassword.setText(reader.getPassword());
    }

    private void filterReaders(String filterText) {
        try {
            if (filterText == null || filterText.trim().isEmpty()) {
                loadReadersTable();
                return;
            }

            Library library = Library.getInstance();
            LinkedList<Reader> allReaders = library.getReaders();
            ObservableList<Reader> filteredList = FXCollections.observableArrayList();

            String filter = filterText.toLowerCase().trim();

            for (int i = 0; i < allReaders.getSize(); i++) {
                Reader reader = allReaders.getAmountNodo(i);
                if (reader != null && reader.getName() != null &&
                        reader.getName().toLowerCase().contains(filter)) {
                    filteredList.add(reader);
                }
            }

            tableReader.setItems(filteredList);
            tableReader.refresh();
        } catch (Exception e) {
            showAlert("Error", "Error al filtrar lectores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadReadersTable() {
        try {
            Library library = Library.getInstance();
            LinkedList<Reader> readers = library.getReaders();
            ObservableList<Reader> readersList = FXCollections.observableArrayList();

            for (int i = 0; i < readers.getSize(); i++) {
                Reader reader = readers.getAmountNodo(i);
                if (reader != null) {
                    readersList.add(reader);
                }
            }

            tableReader.setItems(readersList);
            tableReader.refresh();
        } catch (Exception e) {
            showAlert("Error", "Error al cargar la tabla de lectores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void listenerSelection() {
        tableReader.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            readerSelect = newSelection;
            if (readerSelect != null) {
                populateFields(readerSelect);
            } else {
                clearFields(); // Limpiar campos si no hay selección
            }
        });
    }




    private void clearFields() {
        txtName.clear();
        txtUser.clear();
        txtPassword.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

