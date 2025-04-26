package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MyLoansController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableView<?> tbLoans;

    @FXML
    private TableColumn<?, ?> tcAuthor;

    @FXML
    private TableColumn<?, ?> tcCategory;

    @FXML
    private Button tcReturn;

    @FXML
    private TableColumn<?, ?> tcStatus;

    @FXML
    private TableColumn<?, ?> tcTitle;

    @FXML
    void initialize() {
        assert tbLoans != null : "fx:id=\"tbLoans\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcAuthor != null : "fx:id=\"tcAuthor\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcCategory != null : "fx:id=\"tcCategory\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcReturn != null : "fx:id=\"tcReturn\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcStatus != null : "fx:id=\"tcStatus\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcTitle != null : "fx:id=\"tcTitle\" was not injected: check your FXML file 'MyLoans.fxml'.";

    }

}

