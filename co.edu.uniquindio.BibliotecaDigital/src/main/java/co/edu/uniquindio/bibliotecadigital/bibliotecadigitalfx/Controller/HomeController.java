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
    void initialize() throws IOException {
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

    // Reemplazar el método requestBook en HomeController.java

    private void requestBook(String title) {
        try {
            // Validación inicial de entrada
            if (title == null || title.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Título vacío",
                        "Por favor ingrese un título válido para buscar.");
                return;
            }

            // Buscar el libro usando el método corregido
            Book book = Reader.getBookByTitle(title.trim(), library);

            // Verificar si el libro existe
            if (book == null) {
                showAlert(Alert.AlertType.INFORMATION, "Libro no encontrado",
                        "No se encontró ningún libro con el título \"" + title + "\".\n" +
                                "Verifique la ortografía o intente con palabras clave.");
                return;
            }

            // Verificar si el libro está disponible
            if (book.getStatus() != BookStatus.AVAILABLE) {
                showAlert(Alert.AlertType.WARNING, "Libro no disponible",
                        "El libro \"" + book.getTitle() + "\" está actualmente prestado.\n" +
                                "¿Le gustaría añadirlo a la lista de espera?");
                return;
            }

            // Obtener el usuario actual de manera segura
            Person currentUser = Persistence.getCurrentUser();

            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error de sesión",
                        "No hay una sesión activa. Por favor inicie sesión nuevamente.");
                return;
            }

            if (!(currentUser instanceof Reader)) {
                showAlert(Alert.AlertType.ERROR, "Permisos insuficientes",
                        "Solo los lectores pueden solicitar préstamos de libros.");
                return;
            }

            Reader reader = (Reader) currentUser;

            // Intentar realizar el préstamo
            boolean loanSuccess = reader.requestLoan(book);

            if (loanSuccess) {
                // Actualizar la tabla para reflejar el cambio de estado
                tbBooks.refresh();

                showAlert(Alert.AlertType.INFORMATION, "Préstamo exitoso",
                        "¡Felicitaciones! Has obtenido el préstamo de \"" + book.getTitle() + "\".\n" +
                                "Recuerda devolverlo a tiempo y no olvides valorarlo cuando termines de leerlo.");

                // Limpiar el campo de búsqueda
                txtSearchBook.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error en el préstamo",
                        "No se pudo procesar el préstamo. Es posible que ya tengas este libro o " +
                                "hayas alcanzado el límite de préstamos simultáneos.");
            }

        } catch (Exception e) {
            // Manejo de errores inesperados
            showAlert(Alert.AlertType.ERROR, "Error del sistema",
                    "Ocurrió un error inesperado: " + e.getMessage() + "\n" +
                            "Por favor contacta al administrador del sistema.");

            // Log del error para debugging
            System.err.println("Error en requestBook: " + e.getMessage());
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