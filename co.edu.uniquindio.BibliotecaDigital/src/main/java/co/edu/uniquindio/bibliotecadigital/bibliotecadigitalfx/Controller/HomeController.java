package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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

    }

    @FXML
    void initialize() {

    }

}

