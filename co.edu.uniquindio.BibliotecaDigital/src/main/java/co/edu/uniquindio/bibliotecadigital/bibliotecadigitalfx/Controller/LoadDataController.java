package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

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

        // CORRECCIÓN: Agregar más filtros para diferentes tipos de archivos
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos TXT", "*.txt"),
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        return fileChooser;
    }


    /**
     * NUEVO: Método para notificar a todos los controladores relevantes
     */
    private void notifyAllControllers() {
        ControllerRegistry registry = ControllerRegistry.getInstance();

        // Notificar al controlador de gestión de usuarios
        try {
            UserManagementController userController = registry.getController("UserManagementController", UserManagementController.class);
            if (userController != null) {
                userController.loadReadersTable();
                System.out.println("✅ Tabla de usuarios actualizada");
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo actualizar tabla de usuarios: " + e.getMessage());
        }

        // Notificar al controlador de gestión de libros
        try {
            ManageBooksController bookController = registry.getController("ManageBooksController", ManageBooksController.class);
            if (bookController != null) {
                bookController.updateTableView();
                System.out.println("✅ Tabla de libros actualizada");
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo actualizar tabla de libros: " + e.getMessage());
        }

        // Notificar al controlador de estadísticas
        try {
            LibraryStatsController statsController = registry.getController("LibraryStatsController", LibraryStatsController.class);
            if (statsController != null) {
                statsController.loadAllStatistics();
                System.out.println("✅ Estadísticas actualizadas");
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo actualizar estadísticas: " + e.getMessage());
        }

        // Notificar usando el listener tradicional si existe
        if (dataLoadListener != null) {
            dataLoadListener.onDataLoaded();
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

        // CORRECCIÓN: Aceptar más tipos de archivos
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".txt") && !fileName.endsWith(".csv")) {
            boolean proceed = showConfirmation("Archivo no reconocido",
                    "El archivo no tiene extensión .txt o .csv. ¿Desea continuar?");
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

    private void debugFileContent(File file) {
        System.out.println("🔍 DEBUGGING - Contenido del archivo: " + file.getName());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null && lineNumber <= 10) {
                System.out.println("Línea " + lineNumber + ": " + line);
                lineNumber++;
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo para debug: " + e.getMessage());
        }
    }

    private void processSelectedFile(File selectedFile) {
        try {
            if (!isValidFile(selectedFile)) {
                return;
            }

            String fileName = selectedFile.getName().toLowerCase();
            String dataType = determineFileType(fileName);

            System.out.println("🔍 Procesando archivo: " + fileName + " (Tipo: " + dataType + ")");

            // Procesar el archivo
            Library library = Library.getInstance();
            String result = library.loadDataFromFile(selectedFile);

            // CORRECCIÓN: Forzar actualización de la biblioteca
            if (result.contains("Se cargaron") && !result.contains("0 ")) {
                // Solo si realmente se cargaron datos
                library.forceRefreshAllData();

                // Esperar un momento para que los datos se establezcan
                Thread.sleep(500);

                // CORRECCIÓN: Notificación específica de controladores
                notifySpecificControllers(dataType);

                // Mostrar resultado
                showAlert("Resultado de la carga", result + "\n\n✅ Interfaces actualizadas correctamente.");
            } else {
                showAlert("Resultado de la carga", result);
            }

            System.out.println("✅ Archivo procesado exitosamente: " + selectedFile.getName());

        } catch (Exception e) {
            showAlert("Error", "Ocurrió un error al procesar el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Determina el tipo de archivo basándose en el nombre
     */
    private String determineFileType(String fileName) {
        if (fileName.contains("lector") || fileName.contains("reader")) {
            return "readers";
        } else if (fileName.contains("libro") || fileName.contains("book")) {
            return "books";
        } else if (fileName.contains("valoracion") || fileName.contains("rating") || fileName.contains("calificacion")) {
            return "ratings";
        } else if (fileName.contains("conexion") || fileName.contains("connection")) {
            return "connections";
        } else if (fileName.contains("admin")) {
            return "admins";
        } else {
            return "auto";
        }
    }

    /**
     * NUEVO: Actualización específica según tipo de datos
     */
    private void refreshSpecificDataType(String dataType, Library library) {
        System.out.println("🔄 Actualizando estructuras para tipo: " + dataType);

        switch (dataType) {
            case "readers":
                // Solo actualizar lectores - NO limpiar todo
                System.out.println("Actualizando solo datos de lectores...");
                break;

            case "books":
                // Solo actualizar libros
                System.out.println("Actualizando solo datos de libros...");
                break;

            case "ratings":
                // CRÍTICO: Actualizar valoraciones sin tocar lectores
                library.refreshRatingsFromFile();
                System.out.println("Actualizando solo valoraciones...");
                break;

            case "connections":
                System.out.println("Conexiones cargadas, no requiere actualización adicional");
                break;

            case "auto":
                // Solo para detección automática hacer refresh completo
                System.out.println("Detección automática - actualizando todo...");
                break;

            default:
                System.out.println("Tipo no reconocido, sin actualización específica");
                break;
        }
    }

    private void notifySpecificControllers(String dataType) {
        ControllerRegistry registry = ControllerRegistry.getInstance();
        System.out.println("🔔 Notificando controladores para tipo: " + dataType);

        switch (dataType) {
            case "readers":
                updateUserManagementController(registry);
                // También actualizar estadísticas porque afecta el conteo
                updateStatisticsController(registry);
                break;

            case "books":
                updateBooksManagementController(registry);
                // También actualizar estadísticas porque afecta las valoraciones
                updateStatisticsController(registry);
                break;

            case "ratings":
                // CORRECCIÓN CRÍTICA: Las valoraciones afectan tanto estadísticas como grafo
                updateStatisticsControllerAfterRatings(registry);
                updateAffinityNetworkController(registry);
                break;

            case "connections":
                // CORRECCIÓN CRÍTICA: Las conexiones afectan el grafo directamente
                updateAffinityNetworkController(registry);
                updateStatisticsControllerAfterConnections(registry);
                break;

            case "auto":
                // Para auto-detección actualizar todo
                updateAllControllers(registry);
                break;

            default:
                System.out.println("⚠️ Tipo de datos no reconocido: " + dataType);
                break;
        }
    }

    /**
     * NUEVO: Actualizar controlador de estadísticas específicamente después de cargar valoraciones
     */
    private void updateStatisticsControllerAfterRatings(ControllerRegistry registry) {
        try {
            LibraryStatsController statsController = registry.getController("LibraryStatsController", LibraryStatsController.class);
            if (statsController != null) {
                statsController.refreshAfterRatingsLoaded();
                System.out.println("✅ Estadísticas actualizadas tras cargar valoraciones");
            } else {
                System.err.println("⚠️ LibraryStatsController no encontrado");
            }
        } catch (Exception e) {
            System.err.println("❌ Error actualizando estadísticas tras valoraciones: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Actualizar controlador de estadísticas específicamente después de cargar conexiones
     */
    private void updateStatisticsControllerAfterConnections(ControllerRegistry registry) {
        try {
            LibraryStatsController statsController = registry.getController("LibraryStatsController", LibraryStatsController.class);
            if (statsController != null) {
                statsController.refreshAfterConnectionsLoaded();
                System.out.println("✅ Estadísticas actualizadas tras cargar conexiones");
            } else {
                System.err.println("⚠️ LibraryStatsController no encontrado");
            }
        } catch (Exception e) {
            System.err.println("❌ Error actualizando estadísticas tras conexiones: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Actualizar controlador del grafo de afinidad
     */
    private void updateAffinityNetworkController(ControllerRegistry registry) {
        try {
            AffinityNetworkController affinityController = registry.getController("AffinityNetworkController", AffinityNetworkController.class);
            if (affinityController != null) {
                affinityController.refreshVisualization();
                System.out.println("✅ Grafo de afinidad actualizado");
            } else {
                System.err.println("⚠️ AffinityNetworkController no encontrado");
            }
        } catch (Exception e) {
            System.err.println("❌ Error actualizando grafo de afinidad: " + e.getMessage());
        }
    }

    /**
     * MÉTODO AUXILIAR: Actualizar solo controlador de usuarios
     */
    private void updateUserManagementController(ControllerRegistry registry) {
        try {
            UserManagementController userController = registry.getController("UserManagementController", UserManagementController.class);
            if (userController != null) {
                userController.loadReadersTable();
                System.out.println("✅ Tabla de usuarios actualizada");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando tabla de usuarios: " + e.getMessage());
        }
    }

    /**
     * MÉTODO AUXILIAR: Actualizar solo controlador de libros
     */
    private void updateBooksManagementController(ControllerRegistry registry) {
        try {
            ManageBooksController bookController = registry.getController("ManageBooksController", ManageBooksController.class);
            if (bookController != null) {
                bookController.updateTableView();
                System.out.println("✅ Tabla de libros actualizada");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando tabla de libros: " + e.getMessage());
        }
    }

    /**
     * MÉTODO AUXILIAR: Actualizar solo controlador de estadísticas
     */
    private void updateStatisticsController(ControllerRegistry registry) {
        try {
            LibraryStatsController statsController = registry.getController("LibraryStatsController", LibraryStatsController.class);
            if (statsController != null) {
                statsController.loadAllStatistics();
                System.out.println("✅ Estadísticas actualizadas (incluye valoraciones)");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando estadísticas: " + e.getMessage());
        }
    }

    /**
     * MÉTODO AUXILIAR: Actualizar todos los controladores (solo para auto-detección)
     */
    private void updateAllControllers(ControllerRegistry registry) {
        updateUserManagementController(registry);
        updateBooksManagementController(registry);
        updateStatisticsController(registry);

        // Notificar usando el listener tradicional si existe
        if (dataLoadListener != null) {
            dataLoadListener.onDataLoaded();
        }
    }

    private void debugFileContent(File file, String expectedType) {
        System.out.println("🔍 DEBUGGING - Analizando archivo: " + file.getName());
        System.out.println("🔍 Tipo esperado: " + expectedType);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null && lineNumber <= 5) {
                System.out.println("   Línea " + lineNumber + ": " + line);

                // Analizar formato de la primera línea de datos
                if (lineNumber == 1 && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    System.out.println("   Campos detectados: " + parts.length);
                    for (int i = 0; i < parts.length; i++) {
                        System.out.println("     Campo " + (i+1) + ": '" + parts[i].trim() + "'");
                    }
                }
                lineNumber++;
            }
        } catch (IOException e) {
            System.err.println("Error en debugging: " + e.getMessage());
        }
    }


    @FXML
    void initialize() {
        System.out.println("LoadDataController inicializado correctamente");
        // Registrar este controlador para comunicación con otros controladores
        ControllerRegistry.getInstance().registerController("LoadDataController", this);
    }
}