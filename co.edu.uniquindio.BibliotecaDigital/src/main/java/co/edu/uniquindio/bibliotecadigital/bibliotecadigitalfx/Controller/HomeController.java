package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;


public class HomeController {
    private MyLoansController myLoansController;

    private Library library;
    Book selectedBook;
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
        listenerSelection();
        initTable();
        loadBooksOrderedByTitle(); // Carga inicial
        setupLiveSearch();         // Búsqueda en tiempo real
        loadController();
    }

    private void loadController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/bibliotecadigital/bibliotecadigitalfx/MyLoans.fxml"));
            Parent loansRoot = loader.load(); // Solo carga, no lo muestras

            MyLoansController myLoans = loader.getController();
            this.setMyLoansController(myLoans);        // ← establecer referencia
            myLoans.setHomeController(this);           // ← conexión cruzada

        } catch (IOException e) {
            e.printStackTrace();
        }
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

        // Imprimir estado para debug
        System.out.println("📚 Libros cargados en tabla: " + books.size());
        for (Book book : books) {
            if (book.getStatus() == BookStatus.CHECKED_OUT) {
                System.out.println("   - " + book.getTitle() + " (PRESTADO)");
            }
        }
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

            Set<Book> result = new LinkedHashSet<>();

            // Búsqueda parcial por título
            result.addAll(library.getTitleTree().searchPartialMatches(newText.toLowerCase(), Book::getTitle));

            // Búsqueda parcial por autor
            result.addAll(library.getAuthorTree().searchPartialMatches(newText.toLowerCase(), Book::getAuthor));

            // Búsqueda parcial por categoría
            result.addAll(library.getCategoryTree().searchPartialMatches(newText.toLowerCase(), Book::getCategory));

            listBooks.clear(); // <- limpia la lista antes de actualizarla
            listBooks.setAll(result);
        });
    }





    @FXML
    void onRequestBook(ActionEvent event) throws IOException {
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

            // USAR EL MÉTODO MEJORADO que incluye persistencia
            if (reader.requestLoan(book)) {
                // ACTUALIZAR TABLAS INMEDIATAMENTE
                refreshBooksTable();

                showAlert(Alert.AlertType.INFORMATION, "Préstamo exitoso",
                        "¡Has obtenido el préstamo de \"" + book.getTitle() + "\"!\n" +
                                "Recuerda devolverlo en 14 días.");
                txtSearchBook.clear();

                // ACTUALIZAR MIS PRÉSTAMOS
                if (myLoansController != null) {
                    myLoansController.refreshLoans();
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Error de préstamo",
                        "No se pudo procesar el préstamo. Verifica que no tengas ya este libro.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error del sistema",
                    "Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO MÉTODO: Refresca la tabla de libros desde persistencia
     */
    public void refreshBooksTable() {
        try {
            System.out.println("🔄 Refrescando tabla de libros...");

            // Recargar libros desde persistencia para obtener estados actualizados
            Persistence persistence = new Persistence();
            co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap<String, Book> updatedBooks = persistence.loadBooks();

            // Actualizar el árbol de títulos de la biblioteca
            library.getTitleTree().clear();
            LinkedList<String> bookKeys = updatedBooks.keySet();
            for (int i = 0; i < bookKeys.getSize(); i++) {
                Book book = updatedBooks.get(bookKeys.getAmountNodo(i));
                library.getTitleTree().insert(book);
            }

            // Recargar la tabla
            loadBooksOrderedByTitle();

            System.out.println("✅ Tabla de libros actualizada");

        } catch (Exception e) {
            System.err.println("❌ Error refrescando tabla de libros: " + e.getMessage());
        }
    }

    private void listenerSelection() {
        tbBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedBook = newSelection;
            showUserInformation(selectedBook);
        });
    }

    private void showUserInformation(Book selectedBook) {
        if (this.selectedBook != null) {
            txtSearchBook.setText(selectedBook.getTitle());

        }
    }
    public void setMyLoansController(MyLoansController myLoansController) {
        this.myLoansController = myLoansController;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}