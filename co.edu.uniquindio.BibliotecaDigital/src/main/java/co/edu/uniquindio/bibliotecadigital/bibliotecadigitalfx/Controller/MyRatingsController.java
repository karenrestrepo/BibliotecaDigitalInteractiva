package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Rating;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador para gestionar las valoraciones personales del lector
 *
 * Funcionalidades implementadas:
 * - Visualización de todas las valoraciones del usuario
 * - Edición de valoraciones existentes
 * - Eliminación de valoraciones
 * - Estadísticas personales de lectura
 * - Filtrado por puntuación y libro
 * - Análisis de patrones de lectura del usuario
 *
 * Conceptos de UX aplicados:
 * - Interfaz de gestión de contenido generado por el usuario
 * - Feedback visual para diferentes tipos de valoraciones
 * - Herramientas de análisis personal para fomentar la reflexión
 */
public class MyRatingsController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TableView<RatingInfo> tbRatings;
    @FXML private TableColumn<RatingInfo, String> tcTitle;
    @FXML private TableColumn<RatingInfo, String> tcAuthor;
    @FXML private TableColumn<RatingInfo, String> tcRating;
    @FXML private TableColumn<RatingInfo, String> tcComment;
    @FXML private TableColumn<RatingInfo, String> tcDate;
    @FXML private TableColumn<RatingInfo, Void> tcActions;

    // Controles adicionales que podríamos añadir al FXML
    private ComboBox<String> filterCombo;
    private TextField searchField;
    private Label statsLabel;

    private Reader currentReader;
    private Library library;
    private ObservableList<RatingInfo> ratingsList;

    @FXML
    void initialize() {
        assert tbRatings != null : "fx:id=\"tbRatings\" was not injected: check your FXML file 'MyRatings.fxml'.";
        assert tcTitle != null : "fx:id=\"tcTitle\" was not injected: check your FXML file 'MyRatings.fxml'.";
        assert tcAuthor != null : "fx:id=\"tcAuthor\" was not injected: check your FXML file 'MyRatings.fxml'.";
        assert tcRating != null : "fx:id=\"tcRating\" was not injected: check your FXML file 'MyRatings.fxml'.";

        initializeUserData();
        setupTableColumns();
        setupFilters();
        loadUserRatings();
        calculatePersonalStats();
    }

    /**
     * Inicializa los datos del usuario actual
     */
    private void initializeUserData() {
        try {
            Person currentUser = Persistence.getCurrentUser();

            if (currentUser instanceof Reader) {
                this.currentReader = (Reader) currentUser;
                this.library = Library.getInstance();
                this.ratingsList = FXCollections.observableArrayList();

                System.out.println("Gestor de valoraciones inicializado para: " + currentReader.getName());
            } else {
                showAlert("Error", "Solo los lectores pueden acceder a las valoraciones.");
                disableInterface();
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo inicializar el gestor de valoraciones: " + e.getMessage());
            disableInterface();
        }
    }

    /**
     * Configura las columnas de la tabla de valoraciones
     */
    private void setupTableColumns() {
        // Información básica del libro
        tcTitle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRating().getBook().getTitle()));

        tcAuthor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRating().getBook().getAuthor()));

        // Columna de valoración con estrellas visuales
        tcRating.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStarsDisplay()));

        tcRating.setCellFactory(column -> new TableCell<RatingInfo, String>() {
            @Override
            protected void updateItem(String starsDisplay, boolean empty) {
                super.updateItem(starsDisplay, empty);

                if (empty || starsDisplay == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(starsDisplay);

                    // Color según la puntuación
                    RatingInfo ratingInfo = getTableView().getItems().get(getIndex());
                    int stars = ratingInfo.getRating().getStars();

                    if (stars >= 4) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;"); // Verde para buenas valoraciones
                    } else if (stars >= 3) {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;"); // Naranja para moderadas
                    } else {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;"); // Rojo para bajas
                    }
                }
            }
        });

        // Columna de comentario (truncado si es muy largo)
        if (tcComment != null) {
            tcComment.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getTruncatedComment()));
        }

        // Columna de fecha (si existe en el FXML)
        if (tcDate != null) {
            tcDate.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        }

        // Columna de acciones
        setupActionsColumn();
    }

    /**
     * Configura la columna de acciones con botones para editar/eliminar
     */
    private void setupActionsColumn() {
        if (tcActions != null) {
            tcActions.setCellFactory(column -> new TableCell<RatingInfo, Void>() {
                private final HBox buttonContainer = new HBox(5);
                private final Button editButton = new Button("✏️");
                private final Button deleteButton = new Button("🗑️");
                private final Button viewButton = new Button("👁️");

                {
                    // Configurar estilos
                    editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                    deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 10px;");
                    viewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");

                    // Tooltips para mejor UX
                    editButton.setTooltip(new Tooltip("Editar valoración"));
                    deleteButton.setTooltip(new Tooltip("Eliminar valoración"));
                    viewButton.setTooltip(new Tooltip("Ver detalles completos"));

                    buttonContainer.getChildren().addAll(viewButton, editButton, deleteButton);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setGraphic(null);
                    } else {
                        RatingInfo ratingInfo = getTableView().getItems().get(getIndex());

                        // Configurar acciones
                        viewButton.setOnAction(e -> viewRatingDetails(ratingInfo));
                        editButton.setOnAction(e -> editRating(ratingInfo));
                        deleteButton.setOnAction(e -> deleteRating(ratingInfo));

                        setGraphic(buttonContainer);
                    }
                }
            });
        }
    }

    /**
     * Configura filtros y búsqueda para las valoraciones
     */
    private void setupFilters() {
        // En una implementación completa, estos controles estarían en el FXML
        // Por ahora, simulamos la configuración
        System.out.println("Filtros de valoraciones configurados");

        // Configurar listener para selección en tabla
        tbRatings.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Podrías actualizar un panel de detalles aquí
                showRatingPreview(newSelection);
            }
        });
    }

    /**
     * Carga todas las valoraciones del usuario actual
     */
    private void loadUserRatings() {
        if (currentReader == null) {
            return;
        }

        ratingsList.clear();
        LinkedList<Rating> userRatings = currentReader.getRatingsList();

        for (Rating rating : userRatings) {
            RatingInfo ratingInfo = new RatingInfo(rating);
            ratingsList.add(ratingInfo);
        }

        // Ordenar por fecha (más recientes primero)
        sortRatingsByDate();

        tbRatings.setItems(ratingsList);
        updateRatingsDisplay();
    }

    /**
     * Ordena las valoraciones por fecha de más reciente a más antigua
     */
    private void sortRatingsByDate() {
        // Implementación simple de ordenamiento por burbuja
        // En producción usarías Collections.sort()
        for (int i = 0; i < ratingsList.size() - 1; i++) {
            for (int j = 0; j < ratingsList.size() - i - 1; j++) {
                RatingInfo current = ratingsList.get(j);
                RatingInfo next = ratingsList.get(j + 1);

                if (current.getDateCreated().isBefore(next.getDateCreated())) {
                    // Intercambiar posiciones
                    ratingsList.set(j, next);
                    ratingsList.set(j + 1, current);
                }
            }
        }
    }

    /**
     * Actualiza la visualización con información adicional
     */
    private void updateRatingsDisplay() {
        int totalRatings = ratingsList.size();

        if (totalRatings == 0) {
            tbRatings.setPlaceholder(new Label(
                    "No has valorado ningún libro aún.\n\n" +
                            "💡 Valora los libros que leas para:\n" +
                            "• Llevar registro de tus lecturas\n" +
                            "• Ayudar a otros lectores\n" +
                            "• Obtener mejores recomendaciones"
            ));
        } else {
            System.out.println("Mostrando " + totalRatings + " valoraciones");
        }
    }

    /**
     * Muestra los detalles completos de una valoración
     */
    private void viewRatingDetails(RatingInfo ratingInfo) {
        Rating rating = ratingInfo.getRating();
        Book book = rating.getBook();

        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Detalles de Valoración");
        detailsAlert.setHeaderText(book.getTitle());

        String details = String.format(
                "📖 Libro: %s\n" +
                        "✍️ Autor: %s\n" +
                        "📅 Año: %d\n" +
                        "🏷️ Categoría: %s\n" +
                        "⭐ Tu valoración: %s (%d/5)\n" +
                        "📝 Tu comentario:\n%s\n\n" +
                        "📊 Valoración promedio del libro: %.1f⭐\n" +
                        "📅 Fecha de tu valoración: %s",
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.getCategory(),
                ratingInfo.getStarsDisplay(),
                rating.getStars(),
                rating.getComment() != null && !rating.getComment().trim().isEmpty() ?
                        rating.getComment() : "(sin comentario)",
                book.getAverageRating(),
                ratingInfo.getFormattedDate()
        );

        detailsAlert.setContentText(details);
        detailsAlert.showAndWait();
    }

    /**
     * Permite editar una valoración existente
     */
    private void editRating(RatingInfo ratingInfo) {
        Rating rating = ratingInfo.getRating();

        // Crear diálogo de edición
        Dialog<ButtonType> editDialog = new Dialog<>();
        editDialog.setTitle("Editar Valoración");
        editDialog.setHeaderText("Modificar valoración de \"" + rating.getBook().getTitle() + "\"");

        // Crear contenido
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20px;");

        Label starsLabel = new Label("Nueva calificación (1-5 estrellas):");
        ComboBox<Integer> starsCombo = new ComboBox<>();
        starsCombo.getItems().addAll(1, 2, 3, 4, 5);
        starsCombo.setValue(rating.getStars()); // Valor actual

        Label commentLabel = new Label("Nuevo comentario:");
        TextArea commentArea = new TextArea(rating.getComment());
        commentArea.setPrefRowCount(4);

        content.getChildren().addAll(starsLabel, starsCombo, commentLabel, commentArea);

        editDialog.getDialogPane().setContent(content);
        editDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Procesar resultado
        editDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // Crear nueva valoración (simulamos edición)
                    int newStars = starsCombo.getValue();
                    String newComment = commentArea.getText().trim();

                    // En una implementación real, actualizarías la valoración existente
                    // Aquí simulamos eliminando la vieja y creando una nueva
                    currentReader.getRatingsList().delete(rating);
                    currentReader.rateBook(rating.getBook(), newStars, newComment);

                    showAlert("Valoración Actualizada",
                            "Tu valoración de \"" + rating.getBook().getTitle() + "\" ha sido actualizada.");

                    // Recargar tabla
                    loadUserRatings();

                } catch (Exception e) {
                    showAlert("Error", "No se pudo actualizar la valoración: " + e.getMessage());
                }
            }
            return buttonType;
        });

        editDialog.showAndWait();
    }

    /**
     * Elimina una valoración con confirmación
     */
    private void deleteRating(RatingInfo ratingInfo) {
        Rating rating = ratingInfo.getRating();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Eliminación");
        confirmation.setHeaderText("¿Eliminar esta valoración?");
        confirmation.setContentText("Valoración de \"" + rating.getBook().getTitle() +
                "\" (" + ratingInfo.getStarsDisplay() + ")\n\n" +
                "Esta acción no se puede deshacer.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                currentReader.getRatingsList().delete(rating);

                showAlert("Valoración Eliminada",
                        "La valoración de \"" + rating.getBook().getTitle() + "\" ha sido eliminada.");

                // Recargar tabla
                loadUserRatings();
                calculatePersonalStats();

            } catch (Exception e) {
                showAlert("Error", "No se pudo eliminar la valoración: " + e.getMessage());
            }
        }
    }

    /**
     * Muestra vista previa de una valoración seleccionada
     */
    private void showRatingPreview(RatingInfo ratingInfo) {
        // En una UI más completa, esto actualizaría un panel lateral
        System.out.println("Previa: " + ratingInfo.getRating().getBook().getTitle() +
                " - " + ratingInfo.getStarsDisplay());
    }

    /**
     * Calcula y muestra estadísticas personales de lectura
     *
     * Este análisis ayuda al usuario a entender sus patrones de lectura
     */
    private void calculatePersonalStats() {
        if (ratingsList.isEmpty()) {
            return;
        }

        int totalRatings = ratingsList.size();
        double totalStars = 0;
        int[] starCounts = new int[6]; // Índices 1-5 para estrellas

        String favoriteAuthor = "";
        String favoriteCategory = "";

        // Contadores para análisis
        java.util.Map<String, Integer> authorCounts = new java.util.HashMap<>();
        java.util.Map<String, Integer> categoryCounts = new java.util.HashMap<>();

        for (RatingInfo ratingInfo : ratingsList) {
            Rating rating = ratingInfo.getRating();
            Book book = rating.getBook();

            // Acumular estadísticas
            totalStars += rating.getStars();
            starCounts[rating.getStars()]++;

            // Contar autores y categorías
            authorCounts.merge(book.getAuthor(), 1, Integer::sum);
            categoryCounts.merge(book.getCategory(), 1, Integer::sum);
        }

        // Calcular promedios y favoritos
        double averageRating = totalStars / totalRatings;

        favoriteAuthor = authorCounts.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("N/A");

        favoriteCategory = categoryCounts.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("N/A");

        // Mostrar estadísticas
        String stats = String.format(
                "📊 Tus Estadísticas de Lectura 📊\n\n" +
                        "📚 Total de libros valorados: %d\n" +
                        "⭐ Valoración promedio que das: %.1f/5\n" +
                        "✍️ Tu autor favorito: %s\n" +
                        "🏷️ Tu categoría favorita: %s\n\n" +
                        "Distribución de tus valoraciones:\n" +
                        "⭐⭐⭐⭐⭐ (5 estrellas): %d libros\n" +
                        "⭐⭐⭐⭐ (4 estrellas): %d libros\n" +
                        "⭐⭐⭐ (3 estrellas): %d libros\n" +
                        "⭐⭐ (2 estrellas): %d libros\n" +
                        "⭐ (1 estrella): %d libros",
                totalRatings, averageRating, favoriteAuthor, favoriteCategory,
                starCounts[5], starCounts[4], starCounts[3], starCounts[2], starCounts[1]
        );

        // En una UI completa, esto se mostraría en un panel dedicado
        System.out.println(stats);
    }

    /**
     * Filtra valoraciones por puntuación
     */
    public void filterByStars(int stars) {
        if (stars == 0) {
            tbRatings.setItems(ratingsList); // Mostrar todas
            return;
        }

        ObservableList<RatingInfo> filtered = FXCollections.observableArrayList();
        for (RatingInfo ratingInfo : ratingsList) {
            if (ratingInfo.getRating().getStars() == stars) {
                filtered.add(ratingInfo);
            }
        }

        tbRatings.setItems(filtered);
    }

    /**
     * Busca valoraciones por título o autor
     */
    public void searchRatings(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            tbRatings.setItems(ratingsList);
            return;
        }

        String term = searchTerm.toLowerCase().trim();
        ObservableList<RatingInfo> filtered = FXCollections.observableArrayList();

        for (RatingInfo ratingInfo : ratingsList) {
            Book book = ratingInfo.getRating().getBook();
            if (book.getTitle().toLowerCase().contains(term) ||
                    book.getAuthor().toLowerCase().contains(term)) {
                filtered.add(ratingInfo);
            }
        }

        tbRatings.setItems(filtered);
    }

    /**
     * Método público para refrescar las valoraciones
     */
    public void refreshRatings() {
        loadUserRatings();
        calculatePersonalStats();
    }

    /**
     * Deshabilita la interfaz en caso de errores
     */
    private void disableInterface() {
        tbRatings.setDisable(true);
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

    // ================== CLASE AUXILIAR PARA INFORMACIÓN DE VALORACIONES ==================

    /**
     * Wrapper para Rating que añade metadata y funcionalidades de presentación
     */
    public static class RatingInfo {
        private Rating rating;
        private LocalDateTime dateCreated;

        public RatingInfo(Rating rating) {
            this.rating = rating;
            this.dateCreated = LocalDateTime.now(); // En una app real, esto vendría de la DB
        }

        public Rating getRating() { return rating; }
        public LocalDateTime getDateCreated() { return dateCreated; }

        public String getStarsDisplay() {
            int stars = rating.getStars();
            return "★".repeat(stars) + "☆".repeat(5 - stars) + " (" + stars + "/5)";
        }

        public String getTruncatedComment() {
            String comment = rating.getComment();
            if (comment == null || comment.trim().isEmpty()) {
                return "(sin comentario)";
            }

            if (comment.length() > 50) {
                return comment.substring(0, 47) + "...";
            }
            return comment;
        }

        public String getFormattedDate() {
            return dateCreated.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        @Override
        public String toString() {
            return rating.getBook().getTitle() + " - " + getStarsDisplay();
        }
    }
}

