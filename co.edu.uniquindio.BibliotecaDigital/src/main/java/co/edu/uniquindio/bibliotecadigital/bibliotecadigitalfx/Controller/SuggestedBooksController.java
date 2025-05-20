package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;

import java.util.List;

public class SuggestedBooksController {

    @FXML
    private ListView<Book> lvSuggestedBooks;

    /**
     * Inicializa el controlador. Configura el ListView vacío y un listener opcional para selección.
     */
    @FXML
    public void initialize() {
        assert lvSuggestedBooks != null : "fx:id=\"lvSuggestedBooks\" was not injected: check your FXML file.";

        // Inicializa el ListView vacío
        lvSuggestedBooks.setItems(FXCollections.observableArrayList());

        // Opcional: acción cuando se selecciona un libro
        lvSuggestedBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Aquí puedes manejar la acción cuando se selecciona un libro (mostrar detalles, etc.)
                showBookDetails(newSelection);
            }
        });
    }
    /**
     * Método para cargar y mostrar la lista de libros sugeridos.
     * @param suggestedBooks lista de libros sugeridos
     */
    public void setSuggestedBooks(List<Book> suggestedBooks) {
        if (suggestedBooks == null || suggestedBooks.isEmpty()) {
            showAlert("No hay libros", "No se encontraron libros sugeridos para mostrar.", Alert.AlertType.INFORMATION);
            lvSuggestedBooks.getItems().clear();
        } else {
            lvSuggestedBooks.setItems(FXCollections.observableArrayList(suggestedBooks));
        }
    }
    /**
     * Muestra detalles básicos del libro seleccionado en un alert (puedes cambiar a otra UI).
     * @param book libro seleccionado
     */
    private void showBookDetails(Book book) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del libro");
        alert.setHeaderText(book.getTitle());
        String content = "Autor: " + book.getAuthor() + "\n" +
                "Año: " + book.getYear() + "\n" +
                "ISBN: " + book.getIdBook();
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);  // Sin encabezado
        alert.setContentText(content);
        alert.showAndWait();
    }

}
