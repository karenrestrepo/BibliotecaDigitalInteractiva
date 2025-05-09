package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ManageBooksController {

    LinkedList<Book> listBooks = new LinkedList<>();

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
    void onAgregar(ActionEvent event) {

    }
    @FXML
    void onEliminarar(ActionEvent event) {

    }

    @FXML
    void initialize() {
        initView();


    }

    private void addBook(){
        // buildBook -> construirlibro
        Book book = buildBook();
        // validata -> validar datos
        if (validateData(book)){
            

        }
    }

    private boolean validateData(Book book) {
    }

    private Book buildBook() {
    }

    private void removeBook(){}

    private void initView() {
        initDataBinding();
        updateTableView();
        listenerSelection();
    }

    /// Agrega los elementos de la linkedlist
    private void updateTableView() {
        tableBook.getItems().clear();
        for (Book book : listBooks) {
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
            txtTitle.setText(selectedBook.getTitle());
            txtAuthor.setText(selectedBook.getAuthor());
            txtYear.setText(String.valueOf(selectedBook.getYear()));
            txtCategory.setText(selectedBook.getCategory());
            txtStatus.setText(String.valueOf(selectedBook.getStatus()));
            txtRating.setText(String.valueOf(selectedBook.getAverageRating()));
        }
    }

    private void initDataBinding() {
        tcTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        tcAuthor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthor()));
        tcYear.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));
        tcCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        tcStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        tcRating.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getAverageRating())));
    }


}

