package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LoadDataController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnLoadData;

    @FXML
    void onLoadData(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert btnLoadData != null : "fx:id=\"btnLoadData\" was not injected: check your FXML file 'LoadData.fxml'.";

    }

}

