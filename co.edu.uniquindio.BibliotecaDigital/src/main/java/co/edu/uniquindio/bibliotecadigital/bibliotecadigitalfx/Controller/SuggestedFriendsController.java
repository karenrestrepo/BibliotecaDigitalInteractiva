package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class SuggestedFriendsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<?> lvSuggestedFriends;

    @FXML
    void initialize() {
        assert lvSuggestedFriends != null : "fx:id=\"lvSuggestedFriends\" was not injected: check your FXML file 'SuggestedFriends.fxml'.";

    }

}

