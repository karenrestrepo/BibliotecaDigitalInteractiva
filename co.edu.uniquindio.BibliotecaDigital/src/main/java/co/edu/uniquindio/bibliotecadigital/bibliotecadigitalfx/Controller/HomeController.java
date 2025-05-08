package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HomeController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnRequestBook;

    @FXML
    private TableView<?> tbBooks;

    @FXML
    private TableColumn<?, ?> tcAuthor;

    @FXML
    private TableColumn<?, ?> tcCategory;

    @FXML
    private TableColumn<?, ?> tcRating;

    @FXML
    private TableColumn<?, ?> tcStatus;

    @FXML
    private TableColumn<?, ?> tcTitle;

    @FXML
    private TableColumn<?, ?> tcYear;

    @FXML
    private TextField txtSearchBook;

    @FXML
    void onRequestBook(ActionEvent event) {
        String isbn = txtSearchBook.getText();
        requestBook(isbn);
    }


    @FXML
    void initialize() {

    }

    private void requestBook(String title) {
        Library library = LibraryUtil.initializeData();
        Book book = Reader.getBookByTittle(title, library);

        if (book != null && book.getStatus() == BookStatus.AVAILABLE) {
            // Obtén el usuario actual (lector o administrador)
            Object user = Persistence.getUser();

            if (user instanceof Reader) {
                // Realiza el préstamo del libro
                Reader.lendBook(book, user);

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

