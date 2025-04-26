package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class SuggestedBooksController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<?> lvSuggestedBooks;

    @FXML
    void initialize() {
        assert lvSuggestedBooks != null : "fx:id=\"lvSuggestedBooks\" was not injected: check your FXML file 'SuggestedBooks.fxml'.";

    }

}

