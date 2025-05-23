package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.AffinitySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controlador del sistema de mensajería entre lectores conectados
 *
 * Conceptos implementados:
 * - Comunicación en tiempo real (simulada)
 * - Validación de permisos basada en grafos
 * - Interfaz reactiva con actualizaciones automáticas
 * - Gestión de estado de conversaciones
 *
 * Lección de arquitectura: Este controlador demuestra cómo manejar
 * comunicación entre usuarios en un sistema distribuido, aplicando
 * principios de seguridad y validación de conectividad.
 */
public class MessagesController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button btnSend;
    @FXML private ComboBox<Reader> cbFriends;
    @FXML private ListView<TextFlow> lvMessages;
    @FXML private TextField txtMessages;

    private Reader currentReader;
    private Library library;
    private AffinitySystem affinitySystem;
    private Reader selectedContact;
    private Timer messageRefreshTimer;

    // Simulador de mensajes en tiempo real
    private LinkedList<Message> conversationHistory;

    @FXML
    void initialize() {
        assert btnSend != null : "fx:id=\"btnSend\" was not injected: check your FXML file 'Messages.fxml'.";
        assert cbFriends != null : "fx:id=\"cbFriends\" was not injected: check your FXML file 'Messages.fxml'.";
        assert lvMessages != null : "fx:id=\"lvMessages\" was not injected: check your FXML file 'Messages.fxml'.";
        assert txtMessages != null : "fx:id=\"txtMessages\" was not injected: check your FXML file 'Messages.fxml'.";

        conversationHistory = new LinkedList<>();

        initializeUserData();
        setupContactsList();
        setupMessageInterface();
        startMessagePolling();
    }

    /**
     * Inicializa los datos del usuario actual y sus sistemas asociados
     *
     * Patrón de diseño: Lazy initialization para cargar datos solo cuando se necesitan
     */
    private void initializeUserData() {
        try {
            Person currentUser = Persistence.getCurrentUser();

            if (currentUser instanceof Reader) {
                this.currentReader = (Reader) currentUser;
                this.library = Library.getInstance();
                this.affinitySystem = new AffinitySystem(library);

                System.out.println("Sistema de mensajería inicializado para: " + currentReader.getName());
            } else {
                showAlert("Error", "Solo los lectores pueden acceder al sistema de mensajería.");
                disableInterface();
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo inicializar el sistema de mensajería: " + e.getMessage());
            disableInterface();
        }
    }

    /**
     * Configura la lista de contactos disponibles basándose en el grafo de afinidad
     *
     * Lección de seguridad: Solo se permite comunicación entre usuarios conectados,
     * implementando un control de acceso basado en relaciones sociales.
     */
    private void setupContactsList() {
        if (currentReader == null || affinitySystem == null) {
            return;
        }

        // Obtener contactos directos del grafo de afinidad
        HashSet<Reader> directConnections = affinitySystem.getAffinityGraph()
                .getAdjacentVertices(currentReader);

        ObservableList<Reader> contacts = FXCollections.observableArrayList();

        if (directConnections != null) {
            for (Reader contact : directConnections) {
                contacts.add(contact);
            }
        }

        // Configurar ComboBox
        cbFriends.setItems(contacts);

        // Personalizar cómo se muestran los contactos
        cbFriends.setConverter(new javafx.util.StringConverter<Reader>() {
            @Override
            public String toString(Reader reader) {
                if (reader == null) return "";
                return reader.getName() + " (" + reader.getUsername() + ")";
            }

            @Override
            public Reader fromString(String string) {
                return null; // No necesitamos conversión inversa
            }
        });

        // Mostrar mensaje si no hay contactos
        if (contacts.isEmpty()) {
            showAlert("Información",
                    "No tienes contactos disponibles para mensajería. " +
                            "Conecta con otros lectores a través del sistema de afinidad.");
        }
    }

    /**
     * Configura la interfaz de mensajes con comportamientos reactivos
     *
     * Patrón Observer: La interfaz reacciona automáticamente a cambios de estado
     */
    private void setupMessageInterface() {
        // Listener para cambio de contacto seleccionado
        cbFriends.setOnAction(this::onContactSelected);

        // Configurar ListView para mostrar mensajes con formato
        lvMessages.setCellFactory(listView -> new ListCell<TextFlow>() {
            @Override
            protected void updateItem(TextFlow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
            }
        });

        // Permitir envío con Enter
        txtMessages.setOnAction(this::onSendMessage);

        // Deshabilitar envío inicialmente
        btnSend.setDisable(true);
        txtMessages.setDisable(true);
    }

    /**
     * Maneja la selección de un contacto para iniciar conversación
     */
    @FXML
    void onChat(ActionEvent event) {
        onContactSelected(event);
    }

    private void onContactSelected(ActionEvent event) {
        selectedContact = cbFriends.getValue();

        if (selectedContact != null) {
            btnSend.setDisable(false);
            txtMessages.setDisable(false);
            txtMessages.setPromptText("Escribe tu mensaje a " + selectedContact.getName() + "...");

            loadConversationHistory();
        } else {
            btnSend.setDisable(true);
            txtMessages.setDisable(true);
            lvMessages.getItems().clear();
        }
    }

    /**
     * Carga el historial de conversación con el contacto seleccionado
     *
     * En un sistema real, esto consultaría una base de datos.
     * Aquí simulamos con datos en memoria.
     */
    private void loadConversationHistory() {
        if (selectedContact == null) return;

        ObservableList<TextFlow> messageFlows = FXCollections.observableArrayList();

        // Filtrar mensajes de la conversación actual
        for (Message message : conversationHistory) {
            if (isMessageFromConversation(message, selectedContact)) {
                TextFlow messageFlow = createMessageDisplay(message);
                messageFlows.add(messageFlow);
            }
        }

        lvMessages.setItems(messageFlows);

        // Hacer scroll hasta el último mensaje
        Platform.runLater(() -> {
            if (!messageFlows.isEmpty()) {
                lvMessages.scrollTo(messageFlows.size() - 1);
            }
        });
    }

    /**
     * Verifica si un mensaje pertenece a la conversación actual
     */
    private boolean isMessageFromConversation(Message message, Reader contact) {
        return (message.getSender().equals(currentReader.getUsername()) &&
                message.getReceiver().equals(contact.getUsername())) ||
                (message.getSender().equals(contact.getUsername()) &&
                        message.getReceiver().equals(currentReader.getUsername()));
    }

    /**
     * Crea la representación visual de un mensaje
     *
     * Lección de UX: Los diferentes colores y alineaciones ayudan
     * a distinguir visualmente entre mensajes enviados y recibidos
     */
    private TextFlow createMessageDisplay(Message message) {
        TextFlow textFlow = new TextFlow();

        boolean isMyMessage = message.getSender().equals(currentReader.getUsername());

        // Crear elementos del mensaje
        Text senderText = new Text(isMyMessage ? "Tú" : selectedContact.getName());
        senderText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        senderText.setFill(isMyMessage ? Color.BLUE : Color.GREEN);

        Text timeText = new Text(" (" + message.getFormattedTime() + "): ");
        timeText.setStyle("-fx-font-size: 10px;");
        timeText.setFill(Color.GRAY);

        Text contentText = new Text(message.getContent() + "\n");
        contentText.setStyle("-fx-font-size: 12px;");

        textFlow.getChildren().addAll(senderText, timeText, contentText);

        // Alineación según quién envió el mensaje
        if (isMyMessage) {
            textFlow.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 5px; -fx-background-radius: 10px;");
        } else {
            textFlow.setStyle("-fx-background-color: #F1F8E9; -fx-padding: 5px; -fx-background-radius: 10px;");
        }

        return textFlow;
    }

    /**
     * Envía un mensaje al contacto seleccionado
     *
     * Proceso completo:
     * 1. Validación de entrada
     * 2. Verificación de permisos (conectividad en el grafo)
     * 3. Creación del mensaje
     * 4. Actualización de la interfaz
     * 5. Simulación de entrega
     */
    @FXML
    void onSendMessage(ActionEvent event) {
        if (selectedContact == null) {
            showAlert("Error", "Selecciona un contacto primero.");
            return;
        }

        String messageContent = txtMessages.getText().trim();
        if (messageContent.isEmpty()) {
            showAlert("Error", "El mensaje no puede estar vacío.");
            return;
        }

        try {
            // Verificar conectividad en el grafo (seguridad)
            if (!canSendMessageTo(selectedContact)) {
                showAlert("Error", "No tienes permisos para enviar mensajes a este usuario.");
                return;
            }

            // Crear y guardar el mensaje
            Message newMessage = new Message(
                    currentReader.getUsername(),
                    selectedContact.getUsername(),
                    messageContent,
                    LocalDateTime.now()
            );

            conversationHistory.add(newMessage);

            // Simular entrega del mensaje al destinatario
            selectedContact.receiveMessage("De " + currentReader.getName() + ": " + messageContent);

            // Actualizar interfaz
            refreshMessageDisplay();
            txtMessages.clear();

            // Mostrar confirmación visual
            showTemporaryStatus("Mensaje enviado ✓");

        } catch (Exception e) {
            showAlert("Error", "No se pudo enviar el mensaje: " + e.getMessage());
        }
    }

    /**
     * Verifica si el usuario actual puede enviar mensajes al destinatario
     *
     * Implementa control de acceso basado en el grafo de afinidad
     */
    private boolean canSendMessageTo(Reader recipient) {
        HashSet<Reader> connections = affinitySystem.getAffinityGraph()
                .getAdjacentVertices(currentReader);

        return connections != null && connections.contains(recipient);
    }

    /**
     * Actualiza la visualización de mensajes después de enviar uno nuevo
     */
    private void refreshMessageDisplay() {
        if (selectedContact != null) {
            loadConversationHistory();
        }
    }

    /**
     * Simula la recepción de mensajes en tiempo real
     *
     * En un sistema real, esto sería manejado por WebSockets o polling al servidor
     */
    private void startMessagePolling() {
        messageRefreshTimer = new Timer(true); // Timer como daemon thread

        messageRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Simular recepción de mensajes nuevos
                Platform.runLater(() -> checkForNewMessages());
            }
        }, 5000, 5000); // Verificar cada 5 segundos
    }

    /**
     * Verifica si hay mensajes nuevos para el usuario actual
     * En una implementación real, consultaría el servidor
     */
    private void checkForNewMessages() {
        if (currentReader == null) return;

        // Obtener mensajes del Reader (simula consulta a servidor)
        LinkedList<String> newMessages = currentReader.getMessages();

        // Por simplicidad, mostrar solo una notificación
        if (newMessages.getSize() > 0) {
            // En una app real, mostrarías notificaciones push
            System.out.println("Nuevos mensajes disponibles para " + currentReader.getName());
        }
    }

    /**
     * Muestra un estado temporal en la interfaz
     */
    private void showTemporaryStatus(String message) {
        // Cambiar el prompt del TextField temporalmente
        String originalPrompt = txtMessages.getPromptText();
        txtMessages.setPromptText(message);

        // Restaurar después de 2 segundos
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> txtMessages.setPromptText(originalPrompt));
            }
        }, 2000);
    }

    /**
     * Deshabilita la interfaz cuando hay errores de inicialización
     */
    private void disableInterface() {
        btnSend.setDisable(true);
        txtMessages.setDisable(true);
        cbFriends.setDisable(true);
        lvMessages.setDisable(true);
    }

    /**
     * Muestra alertas al usuario
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Limpia recursos cuando se cierra el controlador
     */
    public void cleanup() {
        if (messageRefreshTimer != null) {
            messageRefreshTimer.cancel();
        }
    }

    // ================== CLASE AUXILIAR PARA MENSAJES ==================

    /**
     * Clase que representa un mensaje en el sistema
     *
     * Patrón Value Object: inmutable, enfocado en datos, fácil de serializar
     */
    public static class Message {
        private final String sender;
        private final String receiver;
        private final String content;
        private final LocalDateTime timestamp;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        public Message(String sender, String receiver, String content, LocalDateTime timestamp) {
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getSender() { return sender; }
        public String getReceiver() { return receiver; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }

        public String getFormattedTime() {
            return timestamp.format(formatter);
        }

        @Override
        public String toString() {
            return String.format("[%s] %s → %s: %s",
                    getFormattedTime(), sender, receiver, content);
        }
    }
}
