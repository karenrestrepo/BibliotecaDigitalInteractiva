package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class MessagesController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnSend;

    @FXML
    private ComboBox<?> cbFriends;

    @FXML
    private ListView<?> lvMessages;

    @FXML
    private TextField txtMessages;

    @FXML
    void onChat(ActionEvent event) {

    }

    @FXML
    void onSend(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert btnSend != null : "fx:id=\"btnSend\" was not injected: check your FXML file 'Messages.fxml'.";
        assert cbFriends != null : "fx:id=\"cbFriends\" was not injected: check your FXML file 'Messages.fxml'.";
        assert lvMessages != null : "fx:id=\"lvMessages\" was not injected: check your FXML file 'Messages.fxml'.";
        assert txtMessages != null : "fx:id=\"txtMessages\" was not injected: check your FXML file 'Messages.fxml'.";

    }

}
