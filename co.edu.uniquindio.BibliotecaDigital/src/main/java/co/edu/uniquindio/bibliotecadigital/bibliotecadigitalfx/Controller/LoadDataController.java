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
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnLoadData;

    // Interfaz funcional para notificar actualizaciones
    public interface DataLoadListener {
        void onDataLoaded();
    }

    private DataLoadListener dataLoadListener;

    public void setDataLoadListener(DataLoadListener listener) {
        this.dataLoadListener = listener;
    }

    @FXML
    void onLoadData(ActionEvent event) {
        FileChooser fileChooser = createFileChooser();
        File selectedFile = fileChooser.showOpenDialog(btnLoadData.getScene().getWindow());

        if (selectedFile != null) {
            processSelectedFile(selectedFile);
        }
    }

    private FileChooser createFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de datos");

        // Agregar filtros para diferentes tipos de archivos
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos TXT", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        return fileChooser;
    }

    private void processSelectedFile(File selectedFile) {
        try {
            // Validaciones del archivo
            if (!isValidFile(selectedFile)) {
                return;
            }

            // Procesar el archivo
            Library library = Library.getInstance();
            String result = library.loadDataFromFile(selectedFile);

            // Mostrar resultado
            showAlert("Resultado de la carga", result);

            // Notificar a los listeners que se han cargado datos
            if (dataLoadListener != null) {
                dataLoadListener.onDataLoaded();
            }

            // Log para debugging
            System.out.println("Archivo procesado exitosamente: " + selectedFile.getName());
            System.out.println("Resultado: " + result);

        } catch (Exception e) {
            showAlert("Error", "Ocurrió un error al procesar el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidFile(File file) {
        if (!file.exists()) {
            showAlert("Error", "El archivo seleccionado no existe.");
            return false;
        }

        if (!file.canRead()) {
            showAlert("Error", "No se puede leer el archivo seleccionado. Verifique los permisos.");
            return false;
        }

        if (file.length() == 0) {
            showAlert("Advertencia", "El archivo seleccionado está vacío.");
            return false;
        }

        // Verificar que sea un archivo de texto
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".txt")) {
            boolean proceed = showConfirmation("Archivo no reconocido",
                    "El archivo no tiene extensión .txt. ¿Desea continuar?");
            return proceed;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait()
                .filter(response -> response == javafx.scene.control.ButtonType.OK)
                .isPresent();
    }

    @FXML
    void initialize() {
        System.out.println("LoadDataController inicializado correctamente");
        // Registrar este controlador para comunicación con otros controladores
        ControllerRegistry.getInstance().registerController("LoadDataController", this);
    }

}

