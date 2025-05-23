package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Rating;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.util.HashSet;

/**
 * Sistema avanzado de recomendación de libros que implementa múltiples algoritmos
 * de filtrado colaborativo y basado en contenido.
 *
 * Conceptos de Machine Learning aplicados:
 * 1. Filtrado Colaborativo (Collaborative Filtering)
 * 2. Filtrado Basado en Contenido (Content-Based Filtering)
 * 3. Sistema Híbrido que combina ambos enfoques
 * 4. Algoritmo de Puntuación Ponderada
 *
 * Lección pedagógica: Este sistema demuestra cómo las matemáticas y estadísticas
 * se aplican para crear experiencias de usuario personalizadas, similar a como
 * Netflix recomienda películas o Spotify recomienda música.
 */
public class BookRecommendationSystem {

    private Library library;
    private AffinitySystem affinitySystem;

    // Pesos para el sistema híbrido de recomendaciones
    private static final double COLLABORATIVE_WEIGHT = 0.6;  // 60% peso a filtrado colaborativo
    private static final double CONTENT_WEIGHT = 0.4;        // 40% peso a filtrado por contenido

    public BookRecommendationSystem(Library library) {
        this.library = library;
        this.affinitySystem = new AffinitySystem(library);
    }

    /**
     * Genera recomendaciones híbridas combinando múltiples algoritmos
     *
     * Este es el método principal que implementa lo que en Machine Learning
     * se conoce como "ensemble method" - combinar múltiples algoritmos
     * para obtener mejores resultados que cualquiera individualmente.
     */
    public LinkedList<BookRecommendation> getHybridRecommendations(Reader targetReader, int maxRecommendations) {
        // Obtener recomendaciones de cada algoritmo
        LinkedList<BookRecommendation> collaborativeRecs = getCollaborativeRecommendations(targetReader);
        LinkedList<BookRecommendation> contentBasedRecs = getContentBasedRecommendations(targetReader);

        // Combinar y puntuar usando sistema híbrido
        HashMap<String, BookRecommendation> combinedRecommendations = new HashMap<>();

        // Procesar recomendaciones colaborativas
        for (BookRecommendation rec : collaborativeRecs) {
            rec.adjustScore(COLLABORATIVE_WEIGHT);
            combinedRecommendations.put(rec.getBook().getIdBook(), rec);
        }

        // Procesar recomendaciones basadas en contenido
        for (BookRecommendation rec : contentBasedRecs) {
            String bookId = rec.getBook().getIdBook();

            if (combinedRecommendations.containsKey(bookId)) {
                // Si ya existe, combinar puntuaciones
                BookRecommendation existing = combinedRecommendations.get(bookId);
                double combinedScore = existing.getScore() + (rec.getScore() * CONTENT_WEIGHT);
                existing.setScore(combinedScore);

                // Combinar razones de recomendación
                existing.addReason(rec.getReason());
            } else {
                // Nueva recomendación basada en contenido
                rec.adjustScore(CONTENT_WEIGHT);
                combinedRecommendations.put(bookId, rec);
            }
        }

        // Convertir a lista y ordenar por puntuación
        LinkedList<BookRecommendation> finalRecommendations = new LinkedList<>();
        LinkedList<String> keys = combinedRecommendations.keySet();

        for (String key : keys) {
            finalRecommendations.add(combinedRecommendations.get(key));
        }

        // Ordenar por puntuación (de mayor a menor)
        sortRecommendationsByScore(finalRecommendations);

        // Limitar al número máximo solicitado
        LinkedList<BookRecommendation> result = new LinkedList<>();
        int count = Math.min(maxRecommendations, finalRecommendations.getSize());

        for (int i = 0; i < count; i++) {
            result.add(finalRecommendations.getAmountNodo(i));
        }

        return result;
    }

    /**
     * Implementa filtrado colaborativo: "Usuarios similares a ti también leyeron..."
     *
     * Algoritmo explicado:
     * 1. Encuentra usuarios con gustos similares (del grafo de afinidad)
     * 2. Identifica libros que ellos valoraron positivamente
     * 3. Filtra libros que el usuario objetivo no ha leído
     * 4. Calcula puntuaciones basadas en la fuerza de la conexión y valoraciones
     *
     * Este es el mismo principio que usa Amazon: "Los clientes que compraron
     * este artículo también compraron..."
     */
    private LinkedList<BookRecommendation> getCollaborativeRecommendations(Reader targetReader) {
        LinkedList<BookRecommendation> recommendations = new LinkedList<>();

        // Obtener usuarios similares del grafo de afinidad
        HashSet<Reader> similarUsers = affinitySystem.getAffinityGraph()
                .getAdjacentVertices(targetReader);

        if (similarUsers == null || similarUsers.isEmpty()) {
            return recommendations; // No hay usuarios similares
        }

        // Obtener libros que el usuario objetivo ya ha leído
        HashSet<String> alreadyReadBooks = getBooksReadByUser(targetReader);

        // Analizar valoraciones de usuarios similares
        HashMap<String, CollaborativeScore> bookScores = new HashMap<>();

        for (Reader similarUser : similarUsers) {
            LinkedList<Rating> userRatings = similarUser.getRatingsList();

            for (Rating rating : userRatings) {
                String bookId = rating.getBook().getIdBook();

                // Solo considerar libros no leídos por el usuario objetivo
                if (!alreadyReadBooks.contains(bookId) && rating.getStars() >= 4) {

                    if (bookScores.containsKey(bookId)) {
                        // Acumular puntuación
                        CollaborativeScore existing = bookScores.get(bookId);
                        existing.addRating(rating.getStars(), similarUser.getName());
                    } else {
                        // Nueva entrada
                        CollaborativeScore newScore = new CollaborativeScore(rating.getBook());
                        newScore.addRating(rating.getStars(), similarUser.getName());
                        bookScores.put(bookId, newScore);
                    }
                }
            }
        }

        // Convertir puntuaciones a recomendaciones
        LinkedList<String> bookIds = bookScores.keySet();
        for (String bookId : bookIds) {
            CollaborativeScore score = bookScores.get(bookId);

            // Calcular puntuación final normalizada (0-1)
            double finalScore = score.getAverageRating() / 5.0;

            String reason = String.format("Recomendado por %d usuarios similares (valoración promedio: %.1f⭐)",
                    score.getRecommenderCount(), score.getAverageRating());

            recommendations.add(new BookRecommendation(score.getBook(), finalScore, reason));
        }

        return recommendations;
    }

    /**
     * Implementa filtrado basado en contenido: "Si te gustó X, te gustará Y..."
     *
     * Algoritmo explicado:
     * 1. Analiza géneros/autores de libros valorados positivamente por el usuario
     * 2. Encuentra libros similares en la biblioteca
     * 3. Calcula puntuaciones basadas en la fuerza de las preferencias identificadas
     *
     * Este enfoque es como el que usa Spotify cuando dice: "Si te gusta el rock
     * alternativo, aquí hay más rock alternativo que podrías disfrutar"
     */
    private LinkedList<BookRecommendation> getContentBasedRecommendations(Reader targetReader) {
        LinkedList<BookRecommendation> recommendations = new LinkedList<>();

        // Analizar preferencias del usuario
        UserPreferences preferences = analyzeUserPreferences(targetReader);

        if (preferences.isEmpty()) {
            return recommendations; // No hay suficientes datos
        }

        // Obtener libros que el usuario no ha leído
        HashSet<String> alreadyReadBooks = getBooksReadByUser(targetReader);
        LinkedList<Book> allBooks = library.getBookssList();

        for (Book book : allBooks) {
            if (!alreadyReadBooks.contains(book.getIdBook())) {

                // Calcular puntuación basada en preferencias
                double score = calculateContentBasedScore(book, preferences);

                if (score > 0.3) { // Umbral mínimo para recomendación
                    String reason = generateContentBasedReason(book, preferences);
                    recommendations.add(new BookRecommendation(book, score, reason));
                }
            }
        }

        return recommendations;
    }

    /**
     * Analiza las preferencias del usuario basándose en sus valoraciones históricas
     */
    private UserPreferences analyzeUserPreferences(Reader reader) {
        UserPreferences preferences = new UserPreferences();
        LinkedList<Rating> ratings = reader.getRatingsList();

        for (Rating rating : ratings) {
            if (rating.getStars() >= 4) { // Solo valoraciones positivas
                Book book = rating.getBook();

                // Acumular preferencias por autor
                preferences.addAuthorPreference(book.getAuthor(), rating.getStars());

                // Acumular preferencias por categoría
                preferences.addCategoryPreference(book.getCategory(), rating.getStars());

                // Considerar año de publicación para detectar preferencias temporales
                preferences.addYearPreference(book.getYear(), rating.getStars());
            }
        }

        preferences.normalize(); // Normalizar puntuaciones
        return preferences;
    }

    /**
     * Calcula puntuación de contenido basada en las preferencias del usuario
     */
    private double calculateContentBasedScore(Book book, UserPreferences preferences) {
        double score = 0.0;

        // Puntuación por autor (40% del peso)
        score += preferences.getAuthorScore(book.getAuthor()) * 0.4;

        // Puntuación por categoría (50% del peso)
        score += preferences.getCategoryScore(book.getCategory()) * 0.5;

        // Puntuación por año (10% del peso) - para detectar preferencias por épocas
        score += preferences.getYearScore(book.getYear()) * 0.1;

        // Bonus por valoración promedio alta del libro
        if (book.getAverageRating() >= 4.0) {
            score += 0.1; // Bonus del 10%
        }

        return Math.min(score, 1.0); // Máximo 1.0
    }

    /**
     * Genera explicación de por qué se recomienda un libro
     */
    private String generateContentBasedReason(Book book, UserPreferences preferences) {
        StringBuilder reason = new StringBuilder("Te podría gustar porque ");

        boolean addedReason = false;

        // Razón por autor
        if (preferences.getAuthorScore(book.getAuthor()) > 0.5) {
            reason.append("has valorado positivamente otros libros de ").append(book.getAuthor());
            addedReason = true;
        }

        // Razón por categoría
        if (preferences.getCategoryScore(book.getCategory()) > 0.5) {
            if (addedReason) reason.append(" y ");
            reason.append("te gusta el género ").append(book.getCategory());
            addedReason = true;
        }

        // Razón por valoración general
        if (book.getAverageRating() >= 4.0) {
            if (addedReason) reason.append(". Además, ");
            reason.append("tiene excelentes valoraciones (").append(String.format("%.1f⭐", book.getAverageRating())).append(")");
        }

        return reason.toString();
    }

    /**
     * Obtiene IDs de libros que el usuario ya ha leído
     */
    private HashSet<String> getBooksReadByUser(Reader reader) {
        HashSet<String> readBooks = new HashSet<>();
        LinkedList<Book> loanHistory = reader.getLoanHistoryList();

        for (Book book : loanHistory) {
            readBooks.add(book.getIdBook());
        }

        return readBooks;
    }

    /**
     * Ordena recomendaciones por puntuación usando algoritmo de inserción
     */
    private void sortRecommendationsByScore(LinkedList<BookRecommendation> recommendations) {
        if (recommendations.getSize() <= 1) return;

        // Convertir a array para ordenamiento eficiente
        BookRecommendation[] array = new BookRecommendation[recommendations.getSize()];
        for (int i = 0; i < recommendations.getSize(); i++) {
            array[i] = recommendations.getAmountNodo(i);
        }

        // Ordenamiento por inserción (descendente)
        for (int i = 1; i < array.length; i++) {
            BookRecommendation key = array[i];
            int j = i - 1;

            while (j >= 0 && array[j].getScore() < key.getScore()) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }

        // Reconstruir la lista
        recommendations.clear();
        for (BookRecommendation rec : array) {
            recommendations.add(rec);
        }
    }

    // ================== CLASES AUXILIARES ==================

    /**
     * Representa una recomendación de libro con puntuación y explicación
     */
    public static class BookRecommendation {
        private Book book;
        private double score;
        private String reason;

        public BookRecommendation(Book book, double score, String reason) {
            this.book = book;
            this.score = score;
            this.reason = reason;
        }

        public Book getBook() { return book; }
        public double getScore() { return score; }
        public String getReason() { return reason; }

        public void setScore(double score) { this.score = score; }
        public void adjustScore(double multiplier) { this.score *= multiplier; }
        public void addReason(String additionalReason) {
            this.reason += " | " + additionalReason;
        }

        @Override
        public String toString() {
            return String.format("%s (%.2f) - %s", book.getTitle(), score, reason);
        }
    }

    /**
     * Almacena puntuaciones colaborativas agregadas
     */
    private static class CollaborativeScore {
        private Book book;
        private double totalScore;
        private int ratingCount;
        private LinkedList<String> recommenders;

        public CollaborativeScore(Book book) {
            this.book = book;
            this.totalScore = 0.0;
            this.ratingCount = 0;
            this.recommenders = new LinkedList<>();
        }

        public void addRating(int stars, String recommenderName) {
            totalScore += stars;
            ratingCount++;
            recommenders.add(recommenderName);
        }

        public double getAverageRating() {
            return ratingCount > 0 ? totalScore / ratingCount : 0.0;
        }

        public int getRecommenderCount() { return ratingCount; }
        public Book getBook() { return book; }
    }

    /**
     * Almacena las preferencias analizadas de un usuario
     */
    private static class UserPreferences {
        private HashMap<String, Double> authorScores;
        private HashMap<String, Double> categoryScores;
        private HashMap<Integer, Double> yearScores;

        public UserPreferences() {
            authorScores = new HashMap<>();
            categoryScores = new HashMap<>();
            yearScores = new HashMap<>();
        }

        public void addAuthorPreference(String author, int rating) {
            double currentScore = authorScores.containsKey(author) ? authorScores.get(author) : 0.0;
            authorScores.put(author, currentScore + rating);
        }

        public void addCategoryPreference(String category, int rating) {
            double currentScore = categoryScores.containsKey(category) ? categoryScores.get(category) : 0.0;
            categoryScores.put(category, currentScore + rating);
        }

        public void addYearPreference(int year, int rating) {
            double currentScore = yearScores.containsKey(year) ? yearScores.get(year) : 0.0;
            yearScores.put(year, currentScore + rating);
        }

        public void normalize() {
            // Normalizar puntuaciones entre 0 y 1
            normalizeHashMap(authorScores);
            normalizeHashMap(categoryScores);
            normalizeHashMap(yearScores);
        }

        /**
         * Normaliza los valores de un HashMap dividiendo cada valor por el valor máximo.
         *
         * @param <K> El tipo de las claves del HashMap (determinado automáticamente)
         * @param scores HashMap con puntuaciones a normalizar
         */
        private <K> void normalizeHashMap(HashMap<K, Double> scores) {
            // Si no hay elementos, no hay nada que normalizar
            if (scores.size() == 0) return;

            // Paso 1: Encontrar la puntuación máxima
            double maxScore = 0.0;
            LinkedList<K> keys = scores.keySet(); // Keys del tipo específico K

            // Iterar sobre todas las claves para encontrar el máximo
            for (K key : keys) {
                double score = scores.get(key); // Java sabe que key es del tipo correcto
                if (score > maxScore) {
                    maxScore = score;
                }
            }

            // Paso 2: Normalizar todas las puntuaciones (solo si el máximo > 0)
            if (maxScore > 0) {
                for (K key : keys) {
                    double currentScore = scores.get(key);
                    double normalizedScore = currentScore / maxScore;
                    scores.put(key, normalizedScore); // Ahora Java permite esta operación
                }
            }
        }

        public double getAuthorScore(String author) {
            return authorScores.containsKey(author) ? authorScores.get(author) : 0.0;
        }

        public double getCategoryScore(String category) {
            return categoryScores.containsKey(category) ? categoryScores.get(category) : 0.0;
        }

        public double getYearScore(int year) {
            return yearScores.containsKey(year) ? yearScores.get(year) : 0.0;
        }

        public boolean isEmpty() {
            return authorScores.size() == 0 && categoryScores.size() == 0 && yearScores.size() == 0;
        }
    }
}