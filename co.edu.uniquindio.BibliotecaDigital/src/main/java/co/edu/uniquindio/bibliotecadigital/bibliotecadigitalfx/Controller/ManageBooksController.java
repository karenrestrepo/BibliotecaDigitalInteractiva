package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ManageBooksController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnEliminar;

    @FXML
    private TableView<?> tableBook;

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
        assert btnAgregar != null : "fx:id=\"btnAgregar\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert btnEliminar != null : "fx:id=\"btnEliminar\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert tableBook != null : "fx:id=\"tableBook\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert tcAuthor != null : "fx:id=\"tcAuthor\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert tcCategory != null : "fx:id=\"tcCategory\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert tcRating != null : "fx:id=\"tcRating\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert tcStatus != null : "fx:id=\"tcStatus\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert tcTitle != null : "fx:id=\"tcTitle\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert tcYear != null : "fx:id=\"tcYear\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert txtAuthor != null : "fx:id=\"txtAuthor\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert txtCategory != null : "fx:id=\"txtCategory\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert txtFiltrarLibro != null : "fx:id=\"txtFiltrarLibro\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert txtRating != null : "fx:id=\"txtRating\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert txtStatus != null : "fx:id=\"txtStatus\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert txtTitle != null : "fx:id=\"txtTitle\" was not injected: check your FXML file 'ManageBooks.fxml'.";
        assert txtYear != null : "fx:id=\"txtYear\" was not injected: check your FXML file 'ManageBooks.fxml'.";

    }

}

