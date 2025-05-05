package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class AffinityNetworkController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Pane PaneGraph;

    @FXML
    void initialize() {
        assert PaneGraph != null : "fx:id=\"PaneGraph\" was not injected: check your FXML file 'AffinityNetwork.fxml'.";

    }

}

