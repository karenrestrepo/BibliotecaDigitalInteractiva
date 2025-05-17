package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;

public class ReaderController {
    private Reader reader;
    private Library library;
    private Persistence persistence;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button profileButton;

    @FXML
    private ListView<Book> loansListView;
    @FXML
    private ListView<Book> recommendationsListView;
    @FXML
    private ListView<String> friendsListView;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private Button sendMessageButton;

    @FXML
    private ListView<Book> adminBooksListView;
    @FXML
    private ListView<String> adminUsersListView;
    @FXML
    private Pane graphPane;

    public void setLector(Reader reader) throws IOException {
        this.reader = reader;
        this.library = getLibraryFromPersistence();
        loadReaderData();
    }

    @FXML
    private void initialize() {
    }
    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }
    public void loadReaderData() {
        if (reader != null && library != null) {
            loansListView.getItems().addAll(reader.getLoanHistoryList().stream().toList());
            recommendationsListView.getItems().addAll(reader.getRecommendations());

            // Asegúrate de pasar el tipo correcto, por ejemplo, si necesitas un LibrarySystem:
            LibrarySystem librarySystem = getLibrarySystemFromLibrary(library);
            friendsListView.getItems().addAll(reader.getSuggestions(librarySystem).stream().map(r -> r.getUsername()).toList());
        }
    }

    private LibrarySystem getLibrarySystemFromLibrary(Library library) {
        // Lógica para obtener LibrarySystem desde Library
        return new LibrarySystem(); // O alguna otra lógica que tengas
    }


    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        // Implementar búsqueda de libros con lógica adicional
    }

    @FXML
    private void handleSendMessage() {
        String message = messageTextArea.getText();
        String recipientUsername = friendsListView.getSelectionModel().getSelectedItem();
        if (recipientUsername != null && !message.isEmpty() && library != null) {
            Reader recipient = null;
            for (int i = 0; i < library.getReadersList().getSize(); i++) {
                Reader r = library.getReadersList().getAmountNodo(i);
                if (r.getUsername().equals(recipientUsername)) {
                    recipient = r;
                    break;
                }
            }
            if (recipient != null) {
                reader.sendMessage(recipient, message);
            }
        }
    }

    private Library getLibraryFromPersistence() throws IOException {
        // Método temporal mientras no exista Persistence.getLibrary()
        return LibraryUtil.initializeData(); // Asume que Library tiene un singleton o acceso global similar
    }
}
