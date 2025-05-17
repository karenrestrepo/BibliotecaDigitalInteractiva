package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LoadDataController {
    Library library = Library.getInstance();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnLoadData;

    public LoadDataController() throws IOException {
    }


    public void setLibrary(Library library) {
        this.library = library;
    }

    @FXML
    void onLoadData(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de datos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos TXT", "*.txt"));

        File selectedFile = fileChooser.showOpenDialog(btnLoadData.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // 1. Verificar que el archivo sea válido
                if (!selectedFile.exists() || !selectedFile.canRead()) {
                    showAlert("Error", "No se puede leer el archivo seleccionado");
                    return;
                }

                // 2. Procesar el archivo
                String result = library.loadDataFromFile(selectedFile);

                // 3. Mostrar feedback al usuario
                showAlert("Resultado", result);

                // 4. DEBUG: Imprimir en consola
                System.out.println("Archivo procesado: " + selectedFile.getName());
                System.out.println("Resultado: " + result);

            } catch (Exception e) {
                showAlert("Error", "Ocurrió un error: " + e.getMessage());
                e.printStackTrace();
            }
        }
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

    }

}

