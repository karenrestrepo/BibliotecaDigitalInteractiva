package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controlador para gestionar los préstamos personales del lector
 *
 * Funcionalidades implementadas:
 * - Visualización de libros prestados actualmente
 * - Devolución de libros con confirmación
 * - Renovación de préstamos (si está disponible)
 * - Histórico de préstamos pasados
 * - Cálculo automático de fechas de vencimiento
 * - Alertas por libros próximos a vencer
 *
 * Conceptos aplicados:
 * - Gestión de estado temporal (fechas de préstamo/devolución)
 * - Validación de business rules (límites de renovación, etc.)
 * - Interfaz adaptativa según el estado de cada préstamo
 */
public class MyLoansController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TableView<LoanInfo> tbLoans;
    @FXML private TableColumn<LoanInfo, String> tcTitle;
    @FXML private TableColumn<LoanInfo, String> tcAuthor;
    @FXML private TableColumn<LoanInfo, String> tcCategory;
    @FXML private TableColumn<LoanInfo, String> tcStatus;
    @FXML private TableColumn<LoanInfo, String> tcDueDate;
    @FXML private TableColumn<LoanInfo, Void> tcActions;

    @FXML private Button tcReturn;


    private Reader currentReader;
    private Library library;
    private ObservableList<LoanInfo> loansList;
    private HomeController homeController;

    // Configuración del sistema de préstamos
    private static final int LOAN_DURATION_DAYS = 14;  // 2 semanas
    private static final int MAX_RENEWALS = 2;         // Máximo 2 renovaciones
    private static final int WARNING_DAYS = 3;         // Alerta 3 días antes del vencimiento

    @FXML
    void onReturn(ActionEvent event) {
        returnBook();

    }



    @FXML
    void onRefresh(ActionEvent event) {
        System.out.println("🔄 Usuario presionó botón Refresh");

        // Primero hacer debug para diagnosticar
        debugLoansState();

        // Luego hacer el refresh
        refreshLoans();
    }

    @FXML
    void initialize() {
        System.out.println("🔄 Inicializando MyLoansController...");

        try {
            // PASO 1: Inicializar datos del usuario
            initializeUserData();

            // PASO 2: Configurar tabla
            setupTableColumns();
            setupSelectionListener();

            // PASO 3: Verificar que tenemos un usuario válido ANTES de continuar
            if (currentReader == null) {
                System.err.println("❌ No se pudo obtener usuario actual");
                disableInterface();
                return;
            }

            // PASO 4: Sincronizar con persistencia
            currentReader.syncLoanHistoryFromPersistence();

            // PASO 5: Cargar préstamos con delay para asegurar que todo esté listo
            Platform.runLater(() -> {
                try {
                    Thread.sleep(200); // Pequeño delay para asegurar inicialización
                    loadCurrentLoans();
                    setupPeriodicChecks();
                    System.out.println("✅ MyLoansController inicializado completamente");
                } catch (Exception e) {
                    System.err.println("❌ Error en inicialización tardía: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error en inicialización de MyLoansController: " + e.getMessage());
            e.printStackTrace();
            disableInterface();
        }
    }


    /**
     * Inicializa los datos del usuario y biblioteca
     */
    private void initializeUserData() {
        try {
            Person currentUser = Persistence.getCurrentUser();

            if (currentUser instanceof Reader) {
                this.currentReader = (Reader) currentUser;
                this.library = Library.getInstance();
                this.loansList = FXCollections.observableArrayList();

                System.out.println("Gestor de préstamos inicializado para: " + currentReader.getName());
            } else {
                showAlert("Error", "Solo los lectores pueden acceder a la gestión de préstamos.");
                disableInterface();
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo inicializar el gestor de préstamos: " + e.getMessage());
            disableInterface();
        }
    }

    private void verifyDataConsistency() {
        try {
            System.out.println("🔍 Verificando consistencia de datos...");

            // Verificar que loansList y tabla coincidan
            int listSize = loansList != null ? loansList.size() : 0;
            int tableSize = tbLoans.getItems() != null ? tbLoans.getItems().size() : 0;

            if (listSize != tableSize) {
                System.err.println("⚠️ INCONSISTENCIA: loansList tiene " + listSize +
                        " elementos pero tabla tiene " + tableSize);

                // Forzar sincronización
                if (loansList != null) {
                    tbLoans.setItems(loansList);
                    tbLoans.refresh();
                }
            }

            // Verificar duplicados en loansList
            if (loansList != null) {
                Set<String> seenBooks = new HashSet<>();
                for (LoanInfo loan : loansList) {
                    String bookId = loan.getBook().getIdBook();
                    if (seenBooks.contains(bookId)) {
                        System.err.println("⚠️ DUPLICADO DETECTADO en loansList: " + bookId);
                    } else {
                        seenBooks.add(bookId);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error verificando consistencia: " + e.getMessage());
        }
    }

    /**
     * Configura las columnas de la tabla con sus respectivos data bindings
     */
    private void setupTableColumns() {
        // Columnas básicas de información
        tcTitle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getTitle()));

        tcAuthor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getAuthor()));

        tcCategory.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getCategory()));

        // Columna de estado con colores según urgencia
        tcStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatusDescription()));

        tcStatus.setCellFactory(column -> new TableCell<LoanInfo, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);

                    // Aplicar estilos según el estado
                    LoanInfo loanInfo = getTableView().getItems().get(getIndex());
                    if (loanInfo.isOverdue()) {
                        setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                    } else if (loanInfo.isDueSoon()) {
                        setStyle("-fx-text-fill: #F57C00; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #388E3C;");
                    }
                }
            }
        });

        // Columna de fecha de vencimiento
        if (tcDueDate != null) {
            tcDueDate.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFormattedDueDate()));
        }

        // Columna de acciones con botones personalizados
        setupActionsColumn();
    }

    /**
     * Configura la columna de acciones con botones dinámicos
     *
     * Pattern: Command Pattern - Cada botón encapsula una acción específica
     */
    private void setupActionsColumn() {
        if (tcActions != null) {
            tcActions.setCellFactory(column -> new TableCell<LoanInfo, Void>() {
                private final HBox buttonContainer = new HBox(5);
                private final Button returnButton = new Button("Devolver");
                private final Button renewButton = new Button("Renovar");
                private final Button rateButton = new Button("Valorar");

                {
                    // Configurar estilos de botones
                    returnButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                    renewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                    rateButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 10px;");

                    buttonContainer.getChildren().addAll(returnButton, renewButton, rateButton);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setGraphic(null);
                    } else {
                        LoanInfo loanInfo = getTableView().getItems().get(getIndex());

                        // Configurar acciones de botones
                        returnButton.setOnAction(e -> returnBook(loanInfo));
                        renewButton.setOnAction(e -> renewLoan(loanInfo));
                        rateButton.setOnAction(e -> rateBook(loanInfo));

                        // Habilitar/deshabilitar según el estado
                        renewButton.setDisable(loanInfo.getRenewalCount() >= MAX_RENEWALS || loanInfo.isOverdue());

                        setGraphic(buttonContainer);
                    }
                }
            });
        }
    }

    /**
     * Configura listener para selección en la tabla
     */
    private void setupSelectionListener() {
        tbLoans.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateReturnButton(newSelection);
            }
        });
    }

    /**
     * Actualiza el estado del botón de devolución principal
     */
    private void updateReturnButton(LoanInfo selectedLoan) {
        if (tcReturn != null) {
            tcReturn.setDisable(false);
            tcReturn.setText("Devolver: " + selectedLoan.getBook().getTitle());
        }
    }

    /**
     * Carga los préstamos actuales del lector
     *
     * Proceso:
     * 1. Obtiene historial de préstamos del lector
     * 2. Filtra solo libros actualmente prestados
     * 3. Crea objetos LoanInfo con metadata adicional
     * 4. Calcula fechas de vencimiento y estados
     */
    /**
     * MÉTODO CORREGIDO: Carga los préstamos actuales del lector desde persistencia
     */
    private void loadCurrentLoans() {
        if (currentReader == null) {
            System.err.println("❌ currentReader es null en loadCurrentLoans");
            return;
        }

        System.out.println("🔄 Cargando préstamos para: " + currentReader.getName() + " (" + currentReader.getUsername() + ")");

        try {
            // PASO 1: Limpiar COMPLETAMENTE la lista
            loansList.clear();

            // PASO 2: Verificar que la tabla esté limpia
            if (tbLoans.getItems() != null) {
                tbLoans.getItems().clear();
            }

            // PASO 3: Cargar desde persistencia
            Persistence persistence = new Persistence();
            HashMap<String, Persistence.LoanRecord> activeLoans = persistence.loadActiveLoans();

            System.out.println("📊 Total préstamos en archivo: " + activeLoans.size());

            // PASO 4: Filtrar solo préstamos del usuario actual con verificación extra
            LinkedList<String> loanKeys = activeLoans.keySet();
            int loansForCurrentUser = 0;

            for (int i = 0; i < loanKeys.getSize(); i++) {
                String key = loanKeys.getAmountNodo(i);
                Persistence.LoanRecord loanRecord = activeLoans.get(key);

                // VERIFICACIÓN EXTRA: Comparar username exactamente
                String loanUsername = loanRecord.getReader().getUsername().trim();
                String currentUsername = currentReader.getUsername().trim();

                if (loanUsername.equals(currentUsername)) {
                    Book book = loanRecord.getBook();

                    // VERIFICACIÓN ANTI-DUPLICADOS: Comprobar si ya está en la lista
                    boolean alreadyExists = false;
                    for (LoanInfo existingLoan : loansList) {
                        if (existingLoan.getBook().getIdBook().equals(book.getIdBook())) {
                            alreadyExists = true;
                            System.out.println("⚠️ Préstamo duplicado detectado y omitido: " + book.getTitle());
                            break;
                        }
                    }

                    if (!alreadyExists) {
                        book.setStatus(BookStatus.CHECKED_OUT);
                        LoanInfo loanInfo = new LoanInfo(book, loanRecord.getLoanDate(), loanRecord.getDueDate());
                        loansList.add(loanInfo);
                        loansForCurrentUser++;

                        System.out.println("📚 Préstamo válido cargado: " + book.getTitle() +
                                " (vence: " + loanRecord.getDueDate() + ")");
                    }
                }
            }

            // PASO 5: Actualizar tabla una sola vez
            tbLoans.setItems(loansList);
            tbLoans.refresh();

            // PASO 6: Actualizar resumen y verificaciones
            updateLoansSummary();
            checkUpcomingDueDates();

            System.out.println("✅ Préstamos cargados correctamente: " + loansForCurrentUser + " préstamos únicos");

        } catch (Exception e) {
            System.err.println("❌ Error cargando préstamos: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar los préstamos: " + e.getMessage());
        }
    }

    /**
     * CORREGIDO: Método de refresh mejorado
     */
    public void refreshLoans() {
        try {
            System.out.println("🔄 REFRESH iniciado por usuario...");

            // IMPORTANTE: Verificar que tenemos un usuario válido
            if (currentReader == null) {
                System.err.println("❌ No hay usuario actual para refresh");
                showAlert("Error", "No hay usuario activo. Por favor reinicia sesión.");
                return;
            }

            // LIMPIAR COMPLETAMENTE antes de recargar
            if (loansList != null) {
                loansList.clear();
            }
            if (tbLoans.getItems() != null) {
                tbLoans.getItems().clear();
            }

            // Forzar actualización visual inmediata
            Platform.runLater(() -> {
                try {
                    // RECARGAR desde persistencia
                    loadCurrentLoans();

                    // VERIFICACIÓN FINAL
                    System.out.println("✅ Refresh completado - Préstamos en tabla: " + tbLoans.getItems().size());

                    // Mostrar mensaje de confirmación al usuario
                    if (tbLoans.getItems().size() == 0) {
                        showAlert("Información", "No tienes préstamos activos actualmente.");
                    } else {
                        System.out.println("📋 Préstamos actualizados exitosamente");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error en refresh tardío: " + e.getMessage());
                    showAlert("Error", "Error actualizando préstamos: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error en refreshLoans: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Error refrescando préstamos: " + e.getMessage());
        }
    }


    /**
     * Actualiza el resumen de préstamos en la interfaz
     */
    private void updateLoansSummary() {
        int totalLoans = loansList.size();
        int overdueLoans = 0;
        int dueSoonLoans = 0;

        for (LoanInfo loan : loansList) {
            if (loan.isOverdue()) {
                overdueLoans++;
            } else if (loan.isDueSoon()) {
                dueSoonLoans++;
            }
        }

        System.out.println(String.format("Resumen de préstamos - Total: %d, Vencidos: %d, Por vencer: %d",
                totalLoans, overdueLoans, dueSoonLoans));
    }

    /**
     * Verifica y alerta sobre libros próximos a vencer
     */
    private void checkUpcomingDueDates() {
        StringBuilder warnings = new StringBuilder();

        for (LoanInfo loan : loansList) {
            if (loan.isOverdue()) {
                warnings.append("⚠️ VENCIDO: \"").append(loan.getBook().getTitle())
                        .append("\" (vencía el ").append(loan.getFormattedDueDate()).append(")\n");
            } else if (loan.isDueSoon()) {
                warnings.append("⏰ Próximo a vencer: \"").append(loan.getBook().getTitle())
                        .append("\" (vence el ").append(loan.getFormattedDueDate()).append(")\n");
            }
        }

        if (warnings.length() > 0) {
            showAlert("Recordatorio de Devoluciones", warnings.toString());
        }
    }

    /**
     * Maneja la renovación de un préstamo
     */
    private void renewLoan(LoanInfo loanInfo) {
        if (loanInfo.getRenewalCount() >= MAX_RENEWALS) {
            showAlert("Renovación No Permitida",
                    "Este libro ya ha alcanzado el máximo número de renovaciones (" + MAX_RENEWALS + ").");
            return;
        }

        if (loanInfo.isOverdue()) {
            showAlert("Renovación No Permitida",
                    "No se pueden renovar libros con retraso. Por favor devuelve el libro primero.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Renovación");
        confirmation.setHeaderText("¿Renovar préstamo?");
        confirmation.setContentText("\"" + loanInfo.getBook().getTitle() +
                "\"\nNueva fecha de vencimiento: " +
                loanInfo.getDueDate().plusDays(LOAN_DURATION_DAYS).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loanInfo.renew(LOAN_DURATION_DAYS);
            showAlert("Renovación Exitosa",
                    "El préstamo ha sido renovado. Nueva fecha de vencimiento: " + loanInfo.getFormattedDueDate());

            // Actualizar tabla
            tbLoans.refresh();
        }
    }


    /**
     * Crea diálogo personalizado para valorar libros
     */
    private Dialog<ButtonType> createRatingDialog(Book book) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Valorar Libro");
        dialog.setHeaderText("¿Qué te pareció \"" + book.getTitle() + "\"?");

        // Crear contenido del diálogo
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20px;");

        Label starsLabel = new Label("Calificación (1-5 estrellas):");
        ComboBox<Integer> starsCombo = new ComboBox<>();
        starsCombo.getItems().addAll(1, 2, 3, 4, 5);
        starsCombo.setValue(5);

        Label commentLabel = new Label("Comentario (opcional):");
        TextArea commentArea = new TextArea();
        commentArea.setPrefRowCount(3);
        commentArea.setPromptText("¿Qué te gustó más? ¿Lo recomendarías?");

        content.getChildren().addAll(starsLabel, starsCombo, commentLabel, commentArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // CORRECCIÓN: Manejar resultado directamente aquí
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    int stars = starsCombo.getValue();
                    String comment = commentArea.getText().trim();

                    // USAR EL MÉTODO CORREGIDO
                    boolean success = currentReader.rateBook(book, stars, comment);

                    if (success) {
                        // Mostrar confirmación
                        Platform.runLater(() -> {
                            showAlert("Valoración Guardada",
                                    "Gracias por valorar \"" + book.getTitle() + "\" con " + stars + " estrellas.");

                            // Actualizar controladores
                            updateMyRatingsController();
                            updateLibraryStatsController();
                        });
                    } else {
                        Platform.runLater(() -> {
                            showAlert("Error", "No se pudo guardar la valoración.");
                        });
                    }

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Error al guardar la valoración: " + e.getMessage());
                    });
                    e.printStackTrace();
                }
            }
            return buttonType;
        });

        return dialog;
    }

    /**
     * MÉTODO SIMPLIFICADO: Valorar libro (ahora solo abre el diálogo)
     */
    private void rateBook(LoanInfo loanInfo) {
        Book book = loanInfo.getBook();

        if (hasRatedBook(book)) {
            showAlert("Libro Ya Valorado",
                    "Ya has valorado este libro anteriormente.");
            return;
        }

        // Simplemente crear y mostrar el diálogo
        Dialog<ButtonType> ratingDialog = createRatingDialog(book);
        ratingDialog.showAndWait();
    }

    /**
     * Maneja el botón principal de devolución
     */
    @FXML
    void onReturnBook(ActionEvent event) {
        LoanInfo selectedLoan = tbLoans.getSelectionModel().getSelectedItem();

        if (selectedLoan == null) {
            showAlert("Selección Requerida", "Por favor selecciona un libro para devolver.");
            return;
        }

        returnBook(selectedLoan);
    }

    /**
     * Verifica si el usuario ya valoró un libro
     */
    private boolean hasRatedBook(Book book) {
        LinkedList<co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Rating> ratings = currentReader.getRatingsList();

        for (co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Rating rating : ratings) {
            if (rating.getBook().getIdBook().equals(book.getIdBook())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Pregunta al usuario si quiere valorar un libro recién devuelto
     */
    private void askForRating(Book book) {
        Alert ratingPrompt = new Alert(Alert.AlertType.CONFIRMATION);
        ratingPrompt.setTitle("Valorar Libro");
        ratingPrompt.setHeaderText("¿Te gustaría valorar este libro?");
        ratingPrompt.setContentText("\"" + book.getTitle() +
                "\"\n\nTus valoraciones ayudan a otros lectores y mejoran las recomendaciones.");

        if (ratingPrompt.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            rateBookWithUpdate(book);
        }
    }

    private void rateBookWithUpdate(Book book) {
        try {
            // Crear diálogo de valoración personalizado
            Dialog<ButtonType> ratingDialog = createRatingDialog(book);

            ratingDialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        // Obtener valores del diálogo (necesitarás acceso a los controles)
                        VBox content = (VBox) ratingDialog.getDialogPane().getContent();
                        ComboBox<Integer> starsCombo = (ComboBox<Integer>) content.getChildren().get(1);
                        TextArea commentArea = (TextArea) content.getChildren().get(3);

                        int stars = starsCombo.getValue();
                        String comment = commentArea.getText().trim();

                        // CORRECCIÓN: Usar el método corregido de valoración
                        boolean success = currentReader.rateBook(book, stars, comment);

                        if (success) {
                            showAlert("Valoración Guardada",
                                    "Gracias por valorar \"" + book.getTitle() + "\" con " + stars + " estrellas.");

                            // CORRECCIÓN: Actualizar MyRatingsController usando el registry
                            updateMyRatingsController();

                            // CORRECCIÓN: También actualizar estadísticas
                            updateLibraryStatsController();

                        } else {
                            showAlert("Error", "No se pudo guardar la valoración.");
                        }

                    } catch (Exception e) {
                        showAlert("Error", "Error al guardar la valoración: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return buttonType;
            });

            ratingDialog.showAndWait();

        } catch (Exception e) {
            showAlert("Error", "Error creando diálogo de valoración: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO MÉTODO: Actualizar MyRatingsController
     */
    private void updateMyRatingsController() {
        try {
            // Intentar obtener MyRatingsController del registry
            ControllerRegistry registry = ControllerRegistry.getInstance();
            MyRatingsController ratingsController = registry.getController("MyRatingsController", MyRatingsController.class);

            if (ratingsController != null) {
                ratingsController.refreshRatings();
                System.out.println("✅ MyRatingsController actualizado tras nueva valoración");
            } else {
                System.out.println("⚠️ MyRatingsController no encontrado en registry");
            }
        } catch (Exception e) {
            System.err.println("❌ Error actualizando MyRatingsController: " + e.getMessage());
        }
    }

    /**
     * NUEVO MÉTODO: Actualizar LibraryStatsController
     */
    private void updateLibraryStatsController() {
        try {
            ControllerRegistry registry = ControllerRegistry.getInstance();
            LibraryStatsController statsController = registry.getController("LibraryStatsController", LibraryStatsController.class);

            if (statsController != null) {
                statsController.refreshAfterRatingsLoaded();
                System.out.println("✅ LibraryStatsController actualizado tras nueva valoración");
            }
        } catch (Exception e) {
            System.err.println("❌ Error actualizando LibraryStatsController: " + e.getMessage());
        }
    }

    public void debugLoansState() {
        try {
            System.out.println("🔍 DEBUG - Estado de préstamos:");
            System.out.println("   - Usuario actual: " + (currentReader != null ? currentReader.getName() : "NULL"));
            System.out.println("   - Username: " + (currentReader != null ? currentReader.getUsername() : "NULL"));
            System.out.println("   - Items en loansList: " + (loansList != null ? loansList.size() : "NULL"));
            System.out.println("   - Items en tabla: " + (tbLoans.getItems() != null ? tbLoans.getItems().size() : "NULL"));

            if (loansList != null && loansList.size() > 0) {
                System.out.println("   - Préstamos en memoria:");
                for (int i = 0; i < loansList.size(); i++) {
                    LoanInfo loan = loansList.get(i);
                    System.out.println("     " + (i+1) + ". " + loan.getBook().getTitle() +
                            " (" + loan.getBook().getIdBook() + ")");
                }
            }

            // También verificar persistencia
            Persistence persistence = new Persistence();
            persistence.debugLoansState();

        } catch (Exception e) {
            System.err.println("❌ Error en debug: " + e.getMessage());
        }
    }

    /**
     * Calcula multa por retraso en la devolución
     */
    private double calculateLateFee(int daysOverdue) {
        final double DAILY_FINE = 0.50; // $0.50 por día
        return daysOverdue * DAILY_FINE;
    }

    /**
     * Configura verificaciones periódicas para alertas automáticas
     */
    private void setupPeriodicChecks() {
        // En una app real, esto sería un servicio de background
        // Aquí solo configuramos el mensaje inicial
        System.out.println("Sistema de alertas de vencimiento activado");
    }

    /**
     * Deshabilita la interfaz en caso de errores
     */
    private void disableInterface() {
        tbLoans.setDisable(true);
        if (tcReturn != null) tcReturn.setDisable(true);
    }

    /**
     * Muestra alertas al usuario
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================== CLASE AUXILIAR PARA INFORMACIÓN DE PRÉSTAMOS ==================

    /**
     * Clase que encapsula información completa sobre un préstamo
     *
     * Incluye el libro, fechas de préstamo/vencimiento, renovaciones, etc.
     * Pattern: Value Object con lógica de dominio
     */
    public static class LoanInfo {
        private Book book;
        private LocalDate loanDate;
        private LocalDate dueDate;
        private int renewalCount;

        // Constructor original (para compatibilidad)
        public LoanInfo(Book book) {
            this.book = book;
            this.loanDate = LocalDate.now();
            this.dueDate = loanDate.plusDays(LOAN_DURATION_DAYS);
            this.renewalCount = 0;
        }

        // NUEVO: Constructor con fechas específicas (para persistencia)
        public LoanInfo(Book book, LocalDate loanDate, LocalDate dueDate) {
            this.book = book;
            this.loanDate = loanDate;
            this.dueDate = dueDate;
            this.renewalCount = 0;
        }

        public Book getBook() { return book; }
        public LocalDate getLoanDate() { return loanDate; }
        public LocalDate getDueDate() { return dueDate; }
        public int getRenewalCount() { return renewalCount; }

        public boolean isOverdue() {
            return LocalDate.now().isAfter(dueDate);
        }

        public boolean isDueSoon() {
            return !isOverdue() && LocalDate.now().plusDays(WARNING_DAYS).isAfter(dueDate);
        }

        public int getDaysOverdue() {
            if (!isOverdue()) return 0;
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }

        public int getDaysUntilDue() {
            if (isOverdue()) return 0;
            return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        }

        public void renew(int additionalDays) {
            this.dueDate = dueDate.plusDays(additionalDays);
            this.renewalCount++;
        }

        public String getStatusDescription() {
            if (isOverdue()) {
                return "VENCIDO (" + getDaysOverdue() + " días)";
            } else if (isDueSoon()) {
                return "Por vencer (" + getDaysUntilDue() + " días)";
            } else {
                return "Vigente (" + getDaysUntilDue() + " días restantes)";
            }
        }

        public String getFormattedDueDate() {
            return dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }
    public void updateTable() {
        LinkedList<Book> loanHistory = currentReader.getLoanHistoryList();

        for (Book book : loanHistory) {
            // Solo mostrar libros actualmente prestados
            if (book.getStatus() == BookStatus.CHECKED_OUT) {
                LoanInfo loanInfo = new LoanInfo(book);
                loansList.add(loanInfo);
            }
        }

        tbLoans.setItems(loansList);

        // Mostrar resumen

        // Verificar libros próximos a vencer
        checkUpcomingDueDates();
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    /**
     * MÉTODO CORREGIDO: Maneja la devolución de un libro con persistencia completa
     */
    private void returnBook(LoanInfo loanInfo) {
        try {
            Book book = loanInfo.getBook();

            // Confirmar devolución
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmar Devolución");
            confirmation.setHeaderText("¿Devolver este libro?");
            confirmation.setContentText("\"" + book.getTitle() + "\" por " + book.getAuthor());

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {

                System.out.println("🔄 Procesando devolución: " + book.getTitle());

                // CORRECCIÓN: Usar el método corregido de Reader
                boolean success = currentReader.returnBook(book);

                if (success) {
                    // Calcular multa por retraso si aplica
                    String message = "Libro devuelto exitosamente.";
                    if (loanInfo.isOverdue()) {
                        int daysOverdue = loanInfo.getDaysOverdue();
                        double fine = calculateLateFee(daysOverdue);
                        message += String.format("\n\nMulta por retraso: $%.2f (%d días)", fine, daysOverdue);
                    }

                    showAlert("Devolución Exitosa", message);

                    // Preguntar por valoración si no ha valorado
                    if (!hasRatedBook(book)) {
                        askForRating(book);
                    }

                    // CORRECCIÓN: Actualización inmediata y completa
                    System.out.println("✅ Devolución exitosa, actualizando interfaces...");

                    // 1. Recargar préstamos inmediatamente (sin delay)
                    loadCurrentLoans();

                    // 2. Actualizar interfaz de libros en HomeController
                    if (homeController != null) {
                        homeController.refreshBooksTable();
                        System.out.println("✅ Tabla de libros actualizada tras devolución");
                    } else {
                        System.err.println("⚠️ HomeController no disponible para actualizar");
                    }

                } else {
                    showAlert("Error", "No se pudo procesar la devolución.");
                }
            }

        } catch (RuntimeException e) {
            // Mostrar error específico del negocio
            showAlert("Error de Devolución", e.getMessage());
            System.err.println("❌ Error de negocio en devolución: " + e.getMessage());
        } catch (Exception e) {
            // Mostrar error técnico genérico
            showAlert("Error Técnico", "Error inesperado al devolver el libro: " + e.getMessage());
            System.err.println("❌ Error técnico en devolución: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * NUEVO: Método principal de devolución (para el botón principal)
     */
    private void returnBook() {
        LoanInfo selectedLoan = tbLoans.getSelectionModel().getSelectedItem();

        if (selectedLoan == null) {
            showAlert("Selección requerida", "Por favor selecciona un préstamo de la tabla.");
            return;
        }

        returnBook(selectedLoan); // Usar el método mejorado
    }

}

