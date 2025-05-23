package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro singleton para manejar comunicación entre controladores
 * cuando se usan fx:include en las pestañas
 */
public class ControllerRegistry {
    private static ControllerRegistry instance;
    private Map<String, Object> controllers = new HashMap<>();

    private ControllerRegistry() {}

    public static ControllerRegistry getInstance() {
        if (instance == null) {
            instance = new ControllerRegistry();
        }
        return instance;
    }

    /**
     * Registra un controlador en el registro
     */
    public void registerController(String name, Object controller) {
        controllers.put(name, controller);
        System.out.println("Controlador registrado: " + name);

        // Intentar configurar comunicación cada vez que se registra un controlador
        attemptSetupCommunication();
    }

    /**
     * Obtiene un controlador del registro
     */
    public <T> T getController(String name, Class<T> type) {
        Object controller = controllers.get(name);
        if (type.isInstance(controller)) {
            return type.cast(controller);
        }
        return null;
    }

    /**
     * Intenta configurar la comunicación entre controladores
     * Se ejecuta cada vez que se registra un controlador
     */
    private void attemptSetupCommunication() {
        LoadDataController loadController = getController("LoadDataController", LoadDataController.class);
        UserManagementController userController = getController("UserManagementController", UserManagementController.class);

        if (loadController != null && userController != null) {
            // Solo configurar si no está ya configurado
            loadController.setDataLoadListener(() -> {
                System.out.println("Datos cargados desde archivo, actualizando tabla de lectores...");
                userController.loadReadersTable();
            });
            System.out.println("✓ Comunicación entre LoadDataController y UserManagementController establecida");
        }
    }

    /**
     * Verifica si todos los controladores necesarios están registrados
     */
    public boolean areControllersReady() {
        return controllers.containsKey("LoadDataController") &&
                controllers.containsKey("UserManagementController");
    }

    /**
     * Limpia el registro (útil para testing o reinicio de aplicación)
     */
    public void clear() {
        controllers.clear();
        System.out.println("Registro de controladores limpiado");
    }

    /**
     * Obtiene información de debug sobre controladores registrados
     */
    public void printRegisteredControllers() {
        System.out.println("=== Controladores Registrados ===");
        controllers.forEach((name, controller) -> {
            System.out.println("- " + name + ": " + controller.getClass().getSimpleName());
        });
        System.out.println("================================");
    }
}
