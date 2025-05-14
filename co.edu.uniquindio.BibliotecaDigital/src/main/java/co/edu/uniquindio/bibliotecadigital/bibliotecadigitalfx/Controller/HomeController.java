package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class HomeController {

    private Library library;
    private ObservableList<Book> listBooks = FXCollections.observableArrayList();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnRequestBook;

    @FXML
    private TableView<Book> tbBooks;

    @FXML
    private TableColumn<Book, String> tcAuthor;

    @FXML
    private TableColumn<Book, String> tcCategory;

    @FXML
    private TableColumn<Book, String> tcRating;

    @FXML
    private TableColumn<Book, String> tcStatus;

    @FXML
    private TableColumn<Book, String> tcTitle;

    @FXML
    private TableColumn<Book, String> tcYear;

    @FXML
    private TextField txtSearchBook;

    @FXML
    void onRequestBook(ActionEvent event) {
        String title = txtSearchBook.getText();
        if (!title.isEmpty()) {
            requestBook(title);
        } else {
            showAlert(Alert.AlertType.WARNING, "Campo vacío", "Por favor ingrese un título para buscar.");
        }
    }


    @FXML
    void initialize() {
        library = LibraryUtil.initializeData();
        initDataBuilding();
    }

    private void initDataBuilding() {
        initializeTable();
    }

    private void initializeTable() {
        listBooks.addAll(library.getBookssList().stream().toList());
        tbBooks.setItems(listBooks);
    }

    private void requestBook(String title) {
        Book book = Reader.getBookByTittle(title, library);

        if (book != null && book.getStatus() == BookStatus.AVAILABLE) {
            Person user = Persistence.getCurrentUser();

            if (user instanceof Reader reader) {
                reader.lendBook(book);
                tbBooks.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Préstamo exitoso", "El libro ha sido prestado con éxito.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de préstamo", "No tienes permisos para realizar préstamos.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error de préstamo", "El libro no está disponible para préstamo.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}