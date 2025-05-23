package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ManageBooksController {

    Library library;
    Book selectedBook;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnEliminar;

    @FXML
    private TableView<Book> tableBook;

    @FXML
    private TableColumn<Book, String> tcAuthor;

    @FXML
    private TableColumn<Book, String> tcCategory;

    @FXML
    private TableColumn<Book, String> tcRating;

    @FXML
    private TableColumn<Book, String> tcStatus;
    @FXML
    private TableColumn<Book, String> tcId;

    @FXML
    private TableColumn<Book, String> tcTitle;

    @FXML
    private TableColumn<Book, String> tcYear;
    @FXML
    private TextField txtAuthor;

    @FXML
    private TextField txtCategory;

    @FXML
    private TextField txtFiltrarLibro;

    @FXML
    private TextField txtRating;

    @FXML
    private TextField txtStatus;

    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtYear;
    @FXML
    private TextField txtId;

    @FXML
    void onAdd(ActionEvent event) {
        addBook();

    }

    @FXML
    void onDelete(ActionEvent event) {
        removeBook();

    }

    // Reemplazar el método initialize en ManageBooksController.java

    @FXML
    void initialize() {
        try {
            // CORRECCIÓN: Usar el Singleton en lugar de crear nueva instancia
            library = Library.getInstance(); // En lugar de new Library()

            initView();

            System.out.println("ManageBooksController inicializado correctamente");

            // Mostrar estadísticas de la biblioteca para verificar la conexión
            System.out.println("Libros cargados en la biblioteca: " + library.getBookssList().getSize());

        } catch (Exception e) {
            showMessage("Error de inicialización", "No se pudo inicializar el controlador",
                    "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // También corregir el método buildBook para validar mejor los datos
    private Book buildBook() {
        try {
            // Validar que el ID no esté vacío
            String id = txtId.getText();
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("El ID del libro no puede estar vacío");
            }

            // Validar que el año sea un número válido
            String yearText = txtYear.getText();
            if (yearText == null || yearText.trim().isEmpty()) {
                throw new IllegalArgumentException("El año no puede estar vacío");
            }

            int year;
            try {
                year = Integer.parseInt(yearText.trim());
                if (year < 0 || year > java.time.Year.now().getValue() + 10) {
                    throw new IllegalArgumentException("El año debe estar entre 0 y " +
                            (java.time.Year.now().getValue() + 10));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El año debe ser un número válido");
            }

            return new Book(
                    id.trim(),
                    txtTitle.getText() != null ? txtTitle.getText().trim() : "",
                    txtAuthor.getText() != null ? txtAuthor.getText().trim() : "",
                    year,
                    txtCategory.getText() != null ? txtCategory.getText().trim() : ""
            );

        } catch (Exception e) {
            // Re-lanzar con mensaje más claro
            throw new RuntimeException("Error construyendo el libro: " + e.getMessage(), e);
        }
    }

    private void addBook() {
        Book book = buildBook();
        if (validateData(book)) {
            // Verificar si el libro ya existe
            if (library.bookExists(book.getIdBook())) {
                showMessage("Error", "Cannot add book", "The book with this ID already exists", Alert.AlertType.ERROR);
                return; // Salir si el libro ya existe
            }

            try {
                library.createBook(
                        book.getIdBook(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getYear(),
                        book.getCategory()
                );
                updateTableView();
                showMessage("Success", "Book added", "The book was added successfully", Alert.AlertType.INFORMATION);
            } catch (RuntimeException e) {
                showMessage("Error", "Cannot add book", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    private boolean validateData(Book book) {

        String message = "";
        if (book.getTitle() == null || book.getTitle().equals(""))
            message += "The title is not valid.\n";
        if (book.getAuthor() == null || book.getAuthor().equals(""))
            message += "The author is not valid.\n";
        if (book.getYear() == 0)
            message += "The year is not valid.\n";
        if (book.getCategory() == null || book.getCategory().equals(""))
            message += "The category is not valid.\n";

        if (message.equals("")) {
            return true;
        } else {
            showMessage("User Notification", "Invalid Data", message, Alert.AlertType.WARNING);
            return false;
        }
    }


    private void showMessage(String title, String header, String content, Alert.AlertType alertType) {
        Alert aler = new Alert(alertType);
        aler.setTitle(title);
        aler.setHeaderText(header);
        aler.setContentText(content);
        aler.showAndWait();
    }
    


    private void removeBook() {
        if (selectedBook != null) {
            try {
                library.removeBook(selectedBook.getIdBook());
                updateTableView();
                showMessage("Success", "Book deleted", "The book was deleted successfully", Alert.AlertType.INFORMATION);
            } catch (RuntimeException e) {
                showMessage("Error", "Cannot delete book", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void initView() {
        initDataBinding();
        updateTableView();
        listenerSelection();
    }


    private void updateTableView() {
        tableBook.getItems().clear();

        // Obtener todos los libros desde el HashMap
        LinkedList<Book> bookList = library.getBooks().values();
        for (Book book : bookList) {
            tableBook.getItems().add(book);
        }
    }





    private void listenerSelection() {
        tableBook.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedBook = newSelection;
            showUserInformation(selectedBook);
        });
    }

    private void showUserInformation(Book selectedBook) {
        if (this.selectedBook != null) {
            txtId.setText(selectedBook.getIdBook());
            txtTitle.setText(selectedBook.getTitle());
            txtAuthor.setText(selectedBook.getAuthor());
            txtYear.setText(String.valueOf(selectedBook.getYear()));
            txtCategory.setText(selectedBook.getCategory());
            txtStatus.setText(String.valueOf(selectedBook.getStatus()));
            txtRating.setText(String.valueOf(selectedBook.getAverageRating()));
        }
    }

    private void initDataBinding() {
        tcId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdBook()));
        tcTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        tcAuthor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthor()));
        tcYear.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));
        tcCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));

    }



}

