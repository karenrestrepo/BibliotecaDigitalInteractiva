package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

public class AdministratorController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    void initialize() {
        System.out.println("AdministratorController inicializado");

        // Opcional: Mostrar información de debug sobre controladores registrados
        // después de un pequeño delay para permitir que se carguen los fx:include
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(1000); // Esperar 1 segundo
                ControllerRegistry.getInstance().printRegisteredControllers();

                if (ControllerRegistry.getInstance().areControllersReady()) {
                    System.out.println("✓ Todos los controladores necesarios están listos");
                } else {
                    System.out.println("⚠ Algunos controladores aún no están registrados");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

}
