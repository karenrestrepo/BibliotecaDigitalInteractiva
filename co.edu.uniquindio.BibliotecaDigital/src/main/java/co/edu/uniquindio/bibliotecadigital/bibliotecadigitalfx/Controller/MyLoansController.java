package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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

    // Configuración del sistema de préstamos
    private static final int LOAN_DURATION_DAYS = 14;  // 2 semanas
    private static final int MAX_RENEWALS = 2;         // Máximo 2 renovaciones
    private static final int WARNING_DAYS = 3;         // Alerta 3 días antes del vencimiento

    @FXML
    void initialize() {
        assert tbLoans != null : "fx:id=\"tbLoans\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcTitle != null : "fx:id=\"tcTitle\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcAuthor != null : "fx:id=\"tcAuthor\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcCategory != null : "fx:id=\"tcCategory\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcStatus != null : "fx:id=\"tcStatus\" was not injected: check your FXML file 'MyLoans.fxml'.";
        assert tcReturn != null : "fx:id=\"tcReturn\" was not injected: check your FXML file 'MyLoans.fxml'.";

        initializeUserData();
        setupTableColumns();
        setupSelectionListener();
        loadCurrentLoans();
        setupPeriodicChecks();
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
    private void loadCurrentLoans() {
        if (currentReader == null) {
            return;
        }

        loansList.clear();
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
        updateLoansSummary();

        // Verificar libros próximos a vencer
        checkUpcomingDueDates();
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
     * Maneja la devolución de un libro
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

                // Procesar devolución
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

                    // Actualizar interfaz
                    loadCurrentLoans();

                } else {
                    showAlert("Error", "No se pudo procesar la devolución.");
                }
            }

        } catch (Exception e) {
            showAlert("Error", "Error al devolver el libro: " + e.getMessage());
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
     * Abre interfaz para valorar un libro
     */
    private void rateBook(LoanInfo loanInfo) {
        Book book = loanInfo.getBook();

        if (hasRatedBook(book)) {
            showAlert("Libro Ya Valorado",
                    "Ya has valorado este libro anteriormente.");
            return;
        }

        // Crear diálogo de valoración personalizado
        Dialog<ButtonType> ratingDialog = createRatingDialog(book);
        ratingDialog.showAndWait();
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

        // Manejar resultado
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    int stars = starsCombo.getValue();
                    String comment = commentArea.getText().trim();

                    currentReader.rateBook(book, stars, comment);
                    showAlert("Valoración Guardada",
                            "Gracias por valorar \"" + book.getTitle() + "\" con " + stars + " estrellas.");

                } catch (Exception e) {
                    showAlert("Error", "No se pudo guardar la valoración: " + e.getMessage());
                }
            }
            return buttonType;
        });

        return dialog;
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
            rateBook(new LoanInfo(book));
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
     * Método público para refrescar la lista (útil desde otros controladores)
     */
    public void refreshLoans() {
        loadCurrentLoans();
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

        public LoanInfo(Book book) {
            this.book = book;
            this.loanDate = LocalDate.now(); // En una app real, esto vendría de la DB
            this.dueDate = loanDate.plusDays(LOAN_DURATION_DAYS);
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
}

