package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HomeController {

    Library library;
    ObservableList<Book> listBooks;


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnRequestBook;

    @FXML
    private TableView<Book> tbBooks;

    @FXML
    private TableColumn<String, Book> tcAuthor;

    @FXML
    private TableColumn<String, Book> tcCategory;

    @FXML
    private TableColumn<String , Book> tcRating;

    @FXML
    private TableColumn<String , Book> tcStatus;

    @FXML
    private TableColumn<String, Book> tcTitle;

    @FXML
    private TableColumn<String, Book> tcYear;

    @FXML
    private TextField txtSearchBook;

    @FXML
    void onRequestBook(ActionEvent event) {
        String tittle = txtSearchBook.getText();
        requestBook(tittle);
    }


    @FXML
    void initialize() {
        initDataBuilding();
        library = LibraryUtil.initializeData();
    }

    private void initDataBuilding() {
        inicializeTable();
    }

    private void inicializeTable() {
        listBooks.addAll((Collection<? extends Book>) library.getBookssList());
        tbBooks.setItems(listBooks);
    }

    private void requestBook(String title) {
        Library library = LibraryUtil.initializeData();
        Book book = Reader.getBookByTittle(title, library);


        if (book != null && book.getStatus() == BookStatus.AVAILABLE) {
            // Obtén el usuario actual (lector o administrador)
            Person user = Persistence.getCurrentUser();

            if (user instanceof Reader) {
                // Realiza el préstamo del libro
                Reader.lendBook(book,(Reader) user);

                tbBooks.refresh();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Préstamo exitoso");
                alert.setContentText("El libro ha sido prestado con éxito.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de préstamo");
                alert.setContentText("No tienes permisos para realizar préstamos.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de préstamo");
            alert.setContentText("El libro no existe o no está disponible para préstamo.");
            alert.showAndWait();
        }
    }
}

