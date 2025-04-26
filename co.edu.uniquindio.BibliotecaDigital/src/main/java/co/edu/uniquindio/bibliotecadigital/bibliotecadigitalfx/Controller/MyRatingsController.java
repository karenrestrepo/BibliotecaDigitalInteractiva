package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MyRatingsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableView<?> tbRatings;

    @FXML
    private TableColumn<?, ?> tcAuthor;

    @FXML
    private TableColumn<?, ?> tcRating;

    @FXML
    private TableColumn<?, ?> tcTitle;

    @FXML
    void initialize() {
        assert tbRatings != null : "fx:id=\"tbRatings\" was not injected: check your FXML file 'MyRatings.fxml'.";
        assert tcAuthor != null : "fx:id=\"tcAuthor\" was not injected: check your FXML file 'MyRatings.fxml'.";
        assert tcRating != null : "fx:id=\"tcRating\" was not injected: check your FXML file 'MyRatings.fxml'.";
        assert tcTitle != null : "fx:id=\"tcTitle\" was not injected: check your FXML file 'MyRatings.fxml'.";

    }

}

