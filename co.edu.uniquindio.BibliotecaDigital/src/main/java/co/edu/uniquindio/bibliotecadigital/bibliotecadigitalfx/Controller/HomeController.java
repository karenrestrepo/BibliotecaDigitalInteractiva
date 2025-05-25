package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;


public class HomeController {

    private Library library;
    private ObservableList<Book> listBooks = FXCollections.observableArrayList();

    @FXML
    private TableView<Book> tbBooks;

    @FXML
    private TableColumn<Book, String> tcAuthor, tcCategory, tcRating, tcStatus, tcTitle, tcYear;

    @FXML
    private TextField txtSearchBook;

    @FXML
    private Button btnRequestBook;

    @FXML
    void initialize() throws IOException {
        library = LibraryUtil.initializeData();
        initTable();
        loadBooksOrderedByTitle(); // Carga inicial
        setupLiveSearch();         // Búsqueda en tiempo real
    }

    private void initTable() {
        tcTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        tcAuthor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        tcCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        tcYear.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getYear())));
        tcStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        tcRating.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAverageRating())));

        tbBooks.setItems(listBooks);
    }

    private void loadBooksOrderedByTitle() {
        listBooks.clear();
        List<Book> books = library.getTitleTree().obtenerListainOrder();
        listBooks.addAll(books);
    }

    private void loadBooksOrderedByAuthor() {
        listBooks.clear();
        listBooks.addAll(library.getAuthorTree().obtenerListainOrder());
    }

    private void loadBooksOrderedByCategory() {
        listBooks.clear();
        listBooks.addAll(library.getCategoryTree().obtenerListainOrder());
    }

    /// hace un abusqueda en vivo
    private void setupLiveSearch() {
        txtSearchBook.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isBlank()) {
                loadBooksOrderedByTitle();
                return;
            }

            List<Book> result = new ArrayList<>();

            // Búsqueda parcial por título
            List<Book> byTitle = library.getTitleTree().searchPartialMatches(newText.toLowerCase(), Book::getTitle);
            result.addAll(byTitle);

            // Búsqueda parcial por autor (sin duplicados)
            List<Book> byAuthor = library.getAuthorTree().searchPartialMatches(newText.toLowerCase(), Book::getAuthor);
            for (Book b : byAuthor) {
                if (!result.contains(b)) {
                    result.add(b);
                }
            }

            // Búsqueda parcial por categoría (sin duplicados)
            List<Book> byCategory = library.getCategoryTree().searchPartialMatches(newText.toLowerCase(), Book::getCategory);
            for (Book b : byCategory) {
                if (!result.contains(b)) {
                    result.add(b);
                }
            }

            listBooks.setAll(result);
        });
    }




    @FXML
    void onRequestBook(ActionEvent event) {
        String title = txtSearchBook.getText();
        if (!title.isEmpty()) {
            requestBook(title.trim());
        } else {
            showAlert(Alert.AlertType.WARNING, "Campo vacío", "Por favor ingrese un título para buscar.");
        }
    }

    private void requestBook(String title) {
        try {
            if (title.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Título vacío",
                        "Por favor ingrese un título válido para buscar.");
                return;
            }

            Book book = library.getTitleTree().searchObject(new Book(title));

            if (book == null) {
                showAlert(Alert.AlertType.INFORMATION, "Libro no encontrado",
                        "No se encontró ningún libro con el título \"" + title + "\".\n" +
                                "Verifique la ortografía o intente con palabras clave.");
                return;
            }

            if (book.getStatus() != BookStatus.AVAILABLE) {
                showAlert(Alert.AlertType.WARNING, "Libro no disponible",
                        "El libro \"" + book.getTitle() + "\" está actualmente prestado.\n" +
                                "¿Le gustaría añadirlo a la lista de espera?");
                return;
            }

            Person currentUser = Persistence.getCurrentUser();

            if (currentUser == null || !(currentUser instanceof Reader)) {
                showAlert(Alert.AlertType.ERROR, "Sesión inválida",
                        "Debe iniciar sesión como lector para solicitar préstamos.");
                return;
            }

            Reader reader = (Reader) currentUser;

            if (reader.requestLoan(book)) {
                tbBooks.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Préstamo exitoso",
                        "¡Has obtenido el préstamo de \"" + book.getTitle() + "\"!");
                txtSearchBook.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de préstamo",
                        "Ya tienes este libro o alcanzaste el límite de préstamos.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error del sistema",
                    "Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}