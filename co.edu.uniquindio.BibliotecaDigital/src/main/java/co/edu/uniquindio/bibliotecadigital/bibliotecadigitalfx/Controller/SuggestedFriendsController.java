package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.AffinitySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para gestionar las sugerencias de amigos basadas en afinidad
 *
 * Explicación pedagógica:
 * Este controlador implementa el patrón Observer/Observable de JavaFX
 * para mostrar dinámicamente las sugerencias de amigos. Utiliza
 * ListView con celdas personalizadas para una mejor experiencia de usuario.
 */
public class SuggestedFriendsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<Reader> lvSuggestedFriends;

    private AffinitySystem affinitySystem;
    private Reader currentReader;
    private Library library;

    /**
     * Método de inicialización automático de JavaFX
     * Se ejecuta después de cargar el FXML
     */
    @FXML
    void initialize() {
        // Verificar que el ListView se inicializó correctamente
        assert lvSuggestedFriends != null : "fx:id=\"lvSuggestedFriends\" was not injected: check your FXML file 'SuggestedFriends.fxml'.";

        // Configurar el ListView con celdas personalizadas
        setupCustomListView();

        // Cargar datos del usuario actual
        loadCurrentUserData();

        // Cargar sugerencias
        loadSuggestedFriends();
    }

    /**
     * Configura el ListView para mostrar información detallada de cada lector sugerido
     *
     * Explicación: Usamos setCellFactory para personalizar cómo se muestra cada elemento.
     * Esto es más avanzado que el toString() básico y permite crear interfaces más ricas.
     */
    private void setupCustomListView() {
        lvSuggestedFriends.setCellFactory(listView -> new ListCell<Reader>() {
            @Override
            protected void updateItem(Reader reader, boolean empty) {
                super.updateItem(reader, empty);

                if (empty || reader == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Crear una interfaz personalizada para cada sugerencia
                    HBox container = createReaderSuggestionCell(reader);
                    setGraphic(container);
                    setText(null);
                }
            }
        });
    }

    /**
     * Crea una celda personalizada para mostrar información del lector sugerido
     *
     * Lección de UX: Una buena interfaz muestra información relevante de forma clara.
     * Aquí mostramos el nombre, cuántos libros ha leído, y por qué se sugiere esta persona.
     */
    private HBox createReaderSuggestionCell(Reader reader) {
        HBox container = new HBox(10); // 10px de espaciado
        container.setStyle("-fx-alignment: center-left; -fx-padding: 5px;");

        // Información del lector
        Text nameText = new Text(reader.getName());
        nameText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Text detailsText = new Text(getReaderDetails(reader));
        detailsText.setStyle("-fx-font-size: 12px; -fx-fill: gray;");

        // Botón para conectar
        Button connectButton = new Button("Conectar");
        connectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        connectButton.setOnAction(e -> sendConnectionRequest(reader));

        // Espacio flexible para empujar el botón a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.getChildren().addAll(nameText, detailsText, spacer, connectButton);
        return container;
    }

    /**
     * Genera detalles informativos sobre por qué se sugiere este lector
     */
    private String getReaderDetails(Reader reader) {
        int booksRead = reader.getLoanHistoryList().getSize();
        int ratingsGiven = reader.getRatingsList().getSize();

        return String.format("Ha leído %d libros • %d valoraciones • Gustos compatibles",
                booksRead, ratingsGiven);
    }

    /**
     * Carga los datos del usuario actualmente logueado
     */
    private void loadCurrentUserData() {
        try {
            Person currentUser = Persistence.getCurrentUser();

            if (currentUser instanceof Reader) {
                this.currentReader = (Reader) currentUser;
                this.library = Library.getInstance();
                this.affinitySystem = new AffinitySystem(library);
            } else {
                showAlert("Error", "Solo los lectores pueden ver sugerencias de amigos.");
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo cargar la información del usuario: " + e.getMessage());
        }
    }

    /**
     * Carga las sugerencias de amigos usando el algoritmo de afinidad
     *
     * Proceso explicado:
     * 1. Usa el sistema de afinidad para encontrar "amigos de amigos"
     * 2. Filtra lectores que ya son amigos directos
     * 3. Ordena por relevancia (número de conexiones mutuas)
     */
    private void loadSuggestedFriends() {
        if (currentReader == null || affinitySystem == null) {
            showAlert("Error", "No se pudo cargar las sugerencias. Usuario no identificado.");
            return;
        }

        try {
            // Obtener sugerencias usando el algoritmo de afinidad
            LinkedList<Reader> suggestions = affinitySystem.getSuggestedFriends(currentReader);

            // Convertir a ObservableList para JavaFX
            ObservableList<Reader> observableSuggestions = FXCollections.observableArrayList();

            for (Reader suggestion : suggestions) {
                observableSuggestions.add(suggestion);
            }

            // Actualizar la interfaz
            lvSuggestedFriends.setItems(observableSuggestions);

            // Mostrar mensaje informativo si no hay sugerencias
            if (suggestions.getSize() == 0) {
                showAlert("Información",
                        "No hay sugerencias disponibles. Intenta valorar más libros para obtener mejores recomendaciones.");
            }

        } catch (Exception e) {
            showAlert("Error", "Error al cargar sugerencias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maneja el envío de solicitudes de conexión entre lectores
     *
     * En un sistema real, esto podría:
     * - Enviar una notificación al otro usuario
     * - Guardar la solicitud en una base de datos
     * - Permitir aceptar/rechazar conexiones
     */
    private void sendConnectionRequest(Reader targetReader) {
        try {
            // Verificar que no sea el mismo usuario
            if (targetReader.getUsername().equals(currentReader.getUsername())) {
                showAlert("Error", "No puedes conectarte contigo mismo.");
                return;
            }

            // Simular envío de mensaje de conexión
            String connectionMessage = "¡Hola! He visto que tenemos gustos similares en libros. " +
                    "¿Te gustaría conectarte para compartir recomendaciones?";

            boolean messageSent = currentReader.sendMessage(targetReader, connectionMessage);

            if (messageSent) {
                showAlert("Éxito",
                        "Solicitud de conexión enviada a " + targetReader.getName() + ". " +
                                "Recibirás una notificación cuando responda.");

                // Actualizar la interfaz removiendo al usuario de la lista
                ObservableList<Reader> currentItems = lvSuggestedFriends.getItems();
                currentItems.remove(targetReader);

            } else {
                showAlert("Error", "No se pudo enviar la solicitud. Verifica tu conexión.");
            }

        } catch (Exception e) {
            showAlert("Error", "Error al enviar solicitud: " + e.getMessage());
        }
    }

    /**
     * Método público para actualizar las sugerencias externamente
     * Útil cuando se añaden nuevas valoraciones o cambia el grafo de afinidad
     */
    public void refreshSuggestions() {
        if (affinitySystem != null) {
            affinitySystem.updateAffinityGraph();
            loadSuggestedFriends();
        }
    }

    /**
     * Método auxiliar para mostrar alertas al usuario
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Método para establecer el lector actual externamente
     * Útil para testing o cuando se inicializa desde otro controlador
     */
    public void setCurrentReader(Reader reader) {
        this.currentReader = reader;
        this.library = Library.getInstance();
        this.affinitySystem = new AffinitySystem(library);

        // Recargar sugerencias con el nuevo usuario
        if (lvSuggestedFriends != null) {
            loadSuggestedFriends();
        }
    }
}
