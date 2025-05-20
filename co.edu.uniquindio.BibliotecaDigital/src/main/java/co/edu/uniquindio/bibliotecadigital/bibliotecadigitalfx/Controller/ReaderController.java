package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.util.List;

public class ReaderController {

    private Reader reader;
    private Library library;
    private Persistence persistence;

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button profileButton;
    @FXML private ListView<Book> loansListView;
    @FXML private ListView<Book> recommendationsListView;
    @FXML private ListView<String> friendsListView;
    @FXML private TextArea messageTextArea;
    @FXML private Button sendMessageButton;
    @FXML private ListView<Book> adminBooksListView;
    @FXML private ListView<String> adminUsersListView;
    @FXML private Pane graphPane;

    public void setLector(Reader reader) throws IOException {
        this.reader = reader;
        this.library = getLibraryFromPersistence();
        loadReaderData();
    }

    @FXML
    private void initialize() {
        configureListeners();
    }

    private void configureListeners() {
        sendMessageButton.setOnAction(e -> handleSendMessage());
        searchButton.setOnAction(e -> handleSearch());
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public void loadReaderData() {
        if (reader != null && library != null) {
            recommendationsListView.setItems(FXCollections.observableArrayList(reader.getRecommendations()));
            loansListView.setItems(FXCollections.observableArrayList(
                    reader.getLoanHistoryList().stream().toList()));

            LibrarySystem librarySystem = getLibrarySystemFromLibrary();
            List<String> suggestedFriends = reader.getSuggestions(librarySystem)
                    .stream()
                    .map(Reader::getUsername)
                    .toList();
            friendsListView.setItems(FXCollections.observableArrayList(suggestedFriends));
        }
    }


    private LibrarySystem getLibrarySystemFromLibrary() {
        return new LibrarySystem(persistence);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();

        if (query.isEmpty()) {
            showAlert("Campo vacío", "Por favor ingresa un término de búsqueda.", Alert.AlertType.WARNING);
            return;
        }

        List<Book> matchedBooks = searchBooks(query);


        if (matchedBooks.isEmpty()) {
            showAlert("Sin resultados", "No se encontraron libros con ese término.", Alert.AlertType.INFORMATION);
        } else {
            recommendationsListView.getItems().setAll(matchedBooks);
        }
    }
    private List<Book> searchBooks(String query) {
        return library.getBookssList().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(query) ||
                        book.getAuthor().toLowerCase().contains(query))
                .toList();
    }

    @FXML
    private void handleSendMessage() {
        String message = messageTextArea.getText().trim();
        String recipientUsername = friendsListView.getSelectionModel().getSelectedItem();

        if (recipientUsername == null) {
            showAlert("Usuario no seleccionado", "Debes seleccionar un amigo para enviar el mensaje.", Alert.AlertType.WARNING);
            return;
        }

        if (message.isEmpty()) {
            showAlert("Mensaje vacío", "No puedes enviar un mensaje vacío.", Alert.AlertType.WARNING);
            return;
        }

        Reader recipient = findReaderByUsername(recipientUsername);
        if (recipient != null) {
            reader.sendMessage(recipient, message);
            messageTextArea.clear();
            showAlert("Mensaje enviado", "El mensaje fue enviado correctamente.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Usuario no encontrado", "No se pudo encontrar el destinatario en la biblioteca.", Alert.AlertType.ERROR);
        }
    }

    private Reader findReaderByUsername(String username) {
        for (int i = 0; i < library.getReadersList().getSize(); i++) {
            Reader r = library.getReadersList().getAmountNodo(i);
            if (r.getUsername().equals(username)) {
                return r;
            }
        }
        return null;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Library getLibraryFromPersistence() throws IOException {
        return LibraryUtil.initializeData(); // Provisorio
    }
}
