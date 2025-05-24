package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes.ListNode;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes.Node;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
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
            loansListView.getItems().clear();
            recommendationsListView.getItems().clear();
            friendsListView.getItems().clear();

            // Cargar la lista de préstamos
            ListNode currentLoan = reader.getLoanHistoryList().getFirstNode();
            while (currentLoan != null) {
                // Asegúrate que getAmountNodo() devuelve el tipo correcto que espera loansListView (por ej Book)
                loansListView.getItems().add((Book) currentLoan.getAmountNodo());

                currentLoan = currentLoan.getNextNodo();
            }

            // Cargar las recomendaciones
            ListNode currentRec = reader.getRecommendations().getFirstNode();
            while (currentRec != null) {
                recommendationsListView.getItems().add((Book) currentRec.getAmountNodo());

                currentRec = currentRec.getNextNodo();
            }

            // Cargar las sugerencias de amigos (muestra sólo el username)
            LibrarySystem librarySystem = getLibrarySystemFromLibrary(library);
            ListNode currentSug = reader.getSuggestions(librarySystem).getFirstNode();
            while (currentSug != null) {
                Reader r = (Reader) currentSug.getAmountNodo();
                friendsListView.getItems().add(r.getUsername());
                currentSug = currentSug.getNextNodo();
            }
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
