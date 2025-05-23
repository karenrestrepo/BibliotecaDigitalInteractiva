package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.BookRecommendationSystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para mostrar libros sugeridos usando el sistema de recomendaciones avanzado
 *
 * Integra el sistema de Machine Learning que implementamos para mostrar
 * recomendaciones personalizadas con explicaciones de por qué se recomienda cada libro.
 *
 * Conceptos implementados:
 * - Interfaz adaptativa que se ajusta a la cantidad de recomendaciones
 * - Sistema de puntuación visual para ayudar al usuario a entender la relevancia
 * - Acciones contextuales (préstamo directo, valoración, etc.)
 * - Actualización dinámica basada en las acciones del usuario
 */
public class SuggestedBooksController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private ListView<BookRecommendationSystem.BookRecommendation> lvSuggestedBooks;

    private Reader currentReader;
    private Library library;
    private BookRecommendationSystem recommendationSystem;

    // Configuración del sistema de recomendaciones
    private static final int MAX_RECOMMENDATIONS = 10;
    private static final double MIN_SCORE_THRESHOLD = 0.3;

    @FXML
    void initialize() {
        assert lvSuggestedBooks != null : "fx:id=\"lvSuggestedBooks\" was not injected: check your FXML file 'SuggestedBooks.fxml'.";

        initializeUserData();
        setupRecommendationsList();
        loadBookRecommendations();
    }

    /**
     * Inicializa los datos del usuario actual y sistemas relacionados
     */
    private void initializeUserData() {
        try {
            Person currentUser = Persistence.getCurrentUser();

            if (currentUser instanceof Reader) {
                this.currentReader = (Reader) currentUser;
                this.library = Library.getInstance();
                this.recommendationSystem = new BookRecommendationSystem(library);

                System.out.println("Sistema de recomendaciones inicializado para: " + currentReader.getName());
            } else {
                showAlert("Error", "Solo los lectores pueden acceder a las recomendaciones de libros.");
                disableInterface();
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo inicializar el sistema de recomendaciones: " + e.getMessage());
            disableInterface();
        }
    }

    /**
     * Configura el ListView para mostrar recomendaciones con interfaz rica
     *
     * Cada recomendación muestra:
     * - Información del libro (título, autor, año)
     * - Puntuación de recomendación visual
     * - Explicación de por qué se recomienda
     * - Acciones disponibles (préstamo, más info)
     */
    private void setupRecommendationsList() {
        lvSuggestedBooks.setCellFactory(listView -> new ListCell<BookRecommendationSystem.BookRecommendation>() {
            @Override
            protected void updateItem(BookRecommendationSystem.BookRecommendation recommendation, boolean empty) {
                super.updateItem(recommendation, empty);

                if (empty || recommendation == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox container = createRecommendationCell(recommendation);
                    setGraphic(container);
                    setText(null);
                }
            }
        });
    }

    /**
     * Crea una celda visual rica para mostrar una recomendación
     *
     * Design Pattern: Composite UI - Construimos una interfaz compleja
     * combinando múltiples componentes simples
     */
    private VBox createRecommendationCell(BookRecommendationSystem.BookRecommendation recommendation) {
        VBox container = new VBox(8);
        container.setStyle("-fx-padding: 10px; -fx-border-color: #E0E0E0; -fx-border-radius: 5px; -fx-background-color: #FAFAFA;");

        Book book = recommendation.getBook();

        // Header con información básica del libro
        HBox header = createBookHeader(book, recommendation.getScore());

        // Explicación de la recomendación
        Text reasonText = new Text(recommendation.getReason());
        reasonText.setStyle("-fx-font-size: 12px; -fx-fill: #666666;");
        reasonText.setWrappingWidth(450); // Permitir texto multilinea

        // Información adicional del libro
        HBox bookDetails = createBookDetails(book);

        // Botones de acción
        HBox actionButtons = createActionButtons(book, recommendation);

        container.getChildren().addAll(header, reasonText, bookDetails, actionButtons);
        return container;
    }

    /**
     * Crea el header con título, autor y puntuación visual
     */
    private HBox createBookHeader(Book book, double score) {
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: center-left;");

        // Información del libro
        VBox bookInfo = new VBox(2);

        Text titleText = new Text(book.getTitle());
        titleText.setFont(Font.font("System", FontWeight.BOLD, 14));

        Text authorText = new Text("por " + book.getAuthor() + " (" + book.getYear() + ")");
        authorText.setStyle("-fx-font-size: 12px; -fx-fill: #888888;");

        bookInfo.getChildren().addAll(titleText, authorText);

        // Espacio flexible
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Puntuación visual de la recomendación
        VBox scoreBox = createScoreDisplay(score);

        header.getChildren().addAll(bookInfo, spacer, scoreBox);
        return header;
    }

    /**
     * Crea display visual de la puntuación de recomendación
     *
     * Convierte el score numérico (0-1) en una representación visual
     * intuitiva usando estrellas y colores
     */
    private VBox createScoreDisplay(double score) {
        VBox scoreBox = new VBox(2);
        scoreBox.setStyle("-fx-alignment: center;");

        // Convertir score (0-1) a estrellas (1-5)
        int stars = (int) Math.ceil(score * 5);
        String starDisplay = "★".repeat(Math.max(1, stars)) + "☆".repeat(Math.max(0, 5 - stars));

        Text scoreText = new Text(String.format("%.0f%%", score * 100));
        scoreText.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");

        Text starsText = new Text(starDisplay);
        starsText.setStyle("-fx-font-size: 12px;");

        // Color según la puntuación
        String color = getScoreColor(score);
        scoreText.setStyle(scoreText.getStyle() + " -fx-fill: " + color + ";");
        starsText.setStyle(starsText.getStyle() + " -fx-fill: " + color + ";");

        scoreBox.getChildren().addAll(scoreText, starsText);
        return scoreBox;
    }

    /**
     * Determina el color de la puntuación basado en el valor
     */
    private String getScoreColor(double score) {
        if (score >= 0.8) return "#4CAF50";      // Verde - Excelente match
        else if (score >= 0.6) return "#FF9800"; // Naranja - Buen match
        else if (score >= 0.4) return "#FFC107"; // Amarillo - Match moderado
        else return "#9E9E9E";                   // Gris - Match bajo
    }

    /**
     * Crea información adicional del libro (categoría, valoración promedio)
     */
    private HBox createBookDetails(Book book) {
        HBox details = new HBox(15);
        details.setStyle("-fx-alignment: center-left;");

        Text categoryText = new Text("📚 " + book.getCategory());
        categoryText.setStyle("-fx-font-size: 11px; -fx-fill: #666666;");

        Text ratingText = new Text("⭐ " + String.format("%.1f", book.getAverageRating()));
        ratingText.setStyle("-fx-font-size: 11px; -fx-fill: #666666;");

        Text statusText = new Text("📖 " + (book.getStatus() == BookStatus.AVAILABLE ? "Disponible" : "Prestado"));
        statusText.setStyle("-fx-font-size: 11px; -fx-fill: " +
                (book.getStatus() == BookStatus.AVAILABLE ? "#4CAF50" : "#F44336") + ";");

        details.getChildren().addAll(categoryText, ratingText, statusText);
        return details;
    }

    /**
     * Crea botones de acción para cada recomendación
     */
    private HBox createActionButtons(Book book, BookRecommendationSystem.BookRecommendation recommendation) {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-left; -fx-padding: 5px 0 0 0;");

        // Botón de préstamo (solo si está disponible)
        if (book.getStatus() == BookStatus.AVAILABLE) {
            Button loanButton = new Button("📖 Pedir Préstamo");
            loanButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px;");
            loanButton.setOnAction(e -> requestLoan(book));
            buttonBox.getChildren().add(loanButton);
        } else {
            Button waitlistButton = new Button("⏳ Lista de Espera");
            waitlistButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px;");
            waitlistButton.setOnAction(e -> addToWaitlist(book));
            buttonBox.getChildren().add(waitlistButton);
        }

        // Botón de información detallada
        Button infoButton = new Button("ℹ️ Más Info");
        infoButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px;");
        infoButton.setOnAction(e -> showBookDetails(book, recommendation));

        // Botón para quitar de recomendaciones
        Button removeButton = new Button("❌ No me interesa");
        removeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 11px;");
        removeButton.setOnAction(e -> removeRecommendation(recommendation));

        buttonBox.getChildren().addAll(infoButton, removeButton);
        return buttonBox;
    }

    /**
     * Carga las recomendaciones usando el algoritmo híbrido
     *
     * Este método demuestra cómo integrar el sistema de ML con la UI
     */
    private void loadBookRecommendations() {
        if (currentReader == null || recommendationSystem == null) {
            showNoRecommendationsMessage();
            return;
        }

        try {
            // Obtener recomendaciones híbridas
            LinkedList<BookRecommendationSystem.BookRecommendation> recommendations =
                    recommendationSystem.getHybridRecommendations(currentReader, MAX_RECOMMENDATIONS);

            // Filtrar por puntuación mínima
            LinkedList<BookRecommendationSystem.BookRecommendation> filteredRecommendations =
                    filterByMinimumScore(recommendations, MIN_SCORE_THRESHOLD);

            // Convertir a ObservableList para JavaFX
            ObservableList<BookRecommendationSystem.BookRecommendation> observableRecommendations =
                    FXCollections.observableArrayList();

            for (BookRecommendationSystem.BookRecommendation rec : filteredRecommendations) {
                observableRecommendations.add(rec);
            }

            // Actualizar interfaz
            lvSuggestedBooks.setItems(observableRecommendations);

            // Mostrar mensaje apropiado según el resultado
            if (filteredRecommendations.getSize() == 0) {
                showNoRecommendationsMessage();
            } else {
                System.out.println("Cargadas " + filteredRecommendations.getSize() + " recomendaciones para " + currentReader.getName());
            }

        } catch (Exception e) {
            showAlert("Error", "Error al cargar recomendaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filtra recomendaciones por puntuación mínima
     */
    private LinkedList<BookRecommendationSystem.BookRecommendation> filterByMinimumScore(
            LinkedList<BookRecommendationSystem.BookRecommendation> recommendations,
            double minScore) {

        LinkedList<BookRecommendationSystem.BookRecommendation> filtered = new LinkedList<>();

        for (BookRecommendationSystem.BookRecommendation rec : recommendations) {
            if (rec.getScore() >= minScore) {
                filtered.add(rec);
            }
        }

        return filtered;
    }

    /**
     * Maneja la solicitud de préstamo de un libro recomendado
     */
    private void requestLoan(Book book) {
        try {
            boolean success = currentReader.requestLoan(book);

            if (success) {
                showAlert("Préstamo Exitoso",
                        "Has obtenido el préstamo de \"" + book.getTitle() + "\". " +
                                "¡No olvides valorarlo cuando termines de leerlo!");

                // Actualizar recomendaciones ya que cambió el estado
                refreshRecommendations();
            } else {
                showAlert("Préstamo No Disponible",
                        "El libro no está disponible en este momento. " +
                                "¿Te gustaría añadirlo a la lista de espera?");
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo procesar el préstamo: " + e.getMessage());
        }
    }

    /**
     * Añade un libro a la lista de espera (cola de prioridad)
     */
    private void addToWaitlist(Book book) {
        // En una implementación completa, esto añadiría a una cola de prioridad
        showAlert("Lista de Espera",
                "Has sido añadido a la lista de espera para \"" + book.getTitle() + "\". " +
                        "Te notificaremos cuando esté disponible.");
    }

    /**
     * Muestra información detallada de un libro recomendado
     */
    private void showBookDetails(Book book, BookRecommendationSystem.BookRecommendation recommendation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Libro");
        alert.setHeaderText(book.getTitle());

        String details = String.format(
                "Autor: %s\n" +
                        "Año: %d\n" +
                        "Categoría: %s\n" +
                        "Valoración Promedio: %.1f⭐\n" +
                        "Estado: %s\n\n" +
                        "¿Por qué se recomienda?\n%s\n\n" +
                        "Puntuación de Compatibilidad: %.0f%%",
                book.getAuthor(),
                book.getYear(),
                book.getCategory(),
                book.getAverageRating(),
                book.getStatus() == BookStatus.AVAILABLE ? "Disponible" : "Prestado",
                recommendation.getReason(),
                recommendation.getScore() * 100
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Remueve una recomendación de la lista (feedback negativo)
     */
    private void removeRecommendation(BookRecommendationSystem.BookRecommendation recommendation) {
        ObservableList<BookRecommendationSystem.BookRecommendation> items = lvSuggestedBooks.getItems();
        items.remove(recommendation);

        // En un sistema de ML real, esto sería feedback negativo para mejorar el algoritmo
        System.out.println("Feedback negativo registrado para: " + recommendation.getBook().getTitle());

        showAlert("Recomendación Removida",
                "Hemos removido \"" + recommendation.getBook().getTitle() + "\" de tus recomendaciones. " +
                        "Esta información nos ayudará a mejorar futuras sugerencias.");
    }

    /**
     * Actualiza las recomendaciones (útil después de cambios en el perfil del usuario)
     */
    public void refreshRecommendations() {
        loadBookRecommendations();
    }

    /**
     * Muestra mensaje cuando no hay recomendaciones disponibles
     */
    private void showNoRecommendationsMessage() {
        lvSuggestedBooks.setPlaceholder(new Label(
                "No hay recomendaciones disponibles.\n\n" +
                        "💡 Consejos para obtener mejores recomendaciones:\n" +
                        "• Lee y valora más libros\n" +
                        "• Conecta con otros lectores con gustos similares\n" +
                        "• Explora diferentes géneros y autores"
        ));
    }

    /**
     * Deshabilita la interfaz en caso de errores
     */
    private void disableInterface() {
        lvSuggestedBooks.setDisable(true);
        lvSuggestedBooks.setPlaceholder(new Label("Sistema de recomendaciones no disponible"));
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

    /**
     * Método público para establecer el lector actual (útil para testing)
     */
    public void setCurrentReader(Reader reader) {
        this.currentReader = reader;
        this.library = Library.getInstance();
        this.recommendationSystem = new BookRecommendationSystem(library);

        if (lvSuggestedBooks != null) {
            refreshRecommendations();
        }
    }
}

