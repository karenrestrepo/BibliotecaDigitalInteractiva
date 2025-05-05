package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class LibraryStatsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnSearch;

    @FXML
    private ComboBox<?> cbReaderA;

    @FXML
    private ComboBox<?> cbReaderB;

    @FXML
    private TextArea lblCamino;

    @FXML
    private TableView<?> tableConnection;

    @FXML
    private TableView<?> tableLoans;

    @FXML
    private TableView<?> tableRating;

    @FXML
    private TableColumn<?, ?> tcAmountConnection;

    @FXML
    private TableColumn<?, ?> tcAmountLoans;

    @FXML
    private TableColumn<?, ?> tcRating;

    @FXML
    private TableColumn<?, ?> tcReaderConnection;

    @FXML
    private TableColumn<?, ?> tcReaderLoans;

    @FXML
    private TableColumn<?, ?> tcTitle;

    @FXML
    private ListView<?> tvClusters;

    @FXML
    private TextField txtFilterReader;

    @FXML
    void initialize() {

    }

}

