package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.AffinitySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.BookRecommendationSystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.PriorityQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase Reader mejorada con sistema completo de recomendaciones,
 * valoraciones y gestión de afinidad
 */
public class Reader extends Person {
    private LinkedList<Book> loanHistoryList;
    private LinkedList<Rating> ratingsList;
    private LinkedList<String> messages; // Mensajes recibidos
    private Library library;

    public Reader(String name, String username, String password, Library library) {
        super(name, username, password);
        this.loanHistoryList = new LinkedList<>();
        this.ratingsList = new LinkedList<>();
        this.messages = new LinkedList<>();
        this.library = library;
    }

    public Reader() {
        this.loanHistoryList = new LinkedList<>();
        this.ratingsList = new LinkedList<>();
        this.messages = new LinkedList<>();
    }

    public Reader(String name, String username, String password) {
        super(name, username, password);
        this.loanHistoryList = new LinkedList<>();
        this.ratingsList = new LinkedList<>();
        this.messages = new LinkedList<>();
    }

    // =============== MÉTODOS DE BÚSQUEDA DE LIBROS ===============

    // Reemplazar el método getBookByTittle en Reader.java

    /**
     * Busca un libro por título en la biblioteca
     * @param title título del libro a buscar
     * @param library biblioteca donde buscar
     * @return el libro encontrado o null si no existe
     */
    public static Book getBookByTitle(String title, Library library) {
        if (title == null || title.trim().isEmpty() || library == null) {
            return null; // Retorna null en lugar de lanzar excepción
        }

        LinkedList<Book> allBooks = library.getBookssList();

        // Búsqueda lineal por el título (case-insensitive)
        for (int i = 0; i < allBooks.getSize(); i++) {
            Book book = allBooks.getAmountNodo(i);
            if (book != null && book.getTitle() != null &&
                    book.getTitle().equalsIgnoreCase(title.trim())) {
                return book;
            }
        }

        return null; // No se encontró el libro
    }

    // También agregar método de instancia para mayor conveniencia
    public Book searchBookByTitle(String title) {
        return getBookByTitle(title, this.library);
    }

    /**
     * Busca libros por autor - retorna lista porque un autor puede tener varios libros
     */
    public LinkedList<Book> getBooksByAuthor(String author) {
        LinkedList<Book> results = new LinkedList<>();
        LinkedList<Book> allBooks = library.getBookssList();

        for (int i = 0; i < allBooks.getSize(); i++) {
            Book book = allBooks.getAmountNodo(i);
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

    /**
     * Busca libros por categoría
     */
    public LinkedList<Book> getBooksByCategory(String category) {
        LinkedList<Book> results = new LinkedList<>();
        LinkedList<Book> allBooks = library.getBookssList();

        for (int i = 0; i < allBooks.getSize(); i++) {
            Book book = allBooks.getAmountNodo(i);
            if (book.getCategory().equalsIgnoreCase(category)) {
                results.add(book);
            }
        }
        return results;
    }

    // =============== SISTEMA DE PRÉSTAMOS ===============

    /**
     * Solicita el préstamo de un libro
     * Utiliza cola de prioridad si el libro no está disponible
     */
    public boolean requestLoan(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("El libro no puede ser null");
        }

        if (book.getStatus() == BookStatus.AVAILABLE) {
            // Libro disponible - préstamo inmediato
            book.setStatus(BookStatus.CHECKED_OUT);
            this.loanHistoryList.add(book);
            return true;
        } else {
            // Libro no disponible - añadir a lista de espera
            // Aquí implementarías la cola de prioridad
            System.out.println("Libro no disponible. Añadido a lista de espera.");
            return false;
        }
    }

    /**
     * Devuelve un libro prestado
     */
    public boolean returnBook(Book book) {
        // Verificar que el lector tiene el libro
        boolean hasBook = false;
        for (int i = 0; i < loanHistoryList.getSize(); i++) {
            if (loanHistoryList.getAmountNodo(i).getIdBook().equals(book.getIdBook())) {
                hasBook = true;
                break;
            }
        }

        if (!hasBook) {
            throw new RuntimeException("El lector no tiene este libro en préstamo.");
        }

        book.setStatus(BookStatus.AVAILABLE);
        // Aquí podrías notificar a la cola de espera
        return true;
    }

    // Agregar estos métodos a la clase Reader.java

    /**
     * Obtiene recomendaciones de libros usando el sistema de ML implementado
     */
    public LinkedList<Book> getRecommendations() {
        if (library == null) {
            return new LinkedList<>(); // Retorna lista vacía si no hay biblioteca
        }

        // Usar el sistema de recomendaciones híbrido
        BookRecommendationSystem recommendationSystem = new BookRecommendationSystem(library);
        LinkedList<BookRecommendationSystem.BookRecommendation> hybridRecs =
                recommendationSystem.getHybridRecommendations(this, 10);

        // Convertir a lista simple de libros
        LinkedList<Book> recommendations = new LinkedList<>();
        for (BookRecommendationSystem.BookRecommendation rec : hybridRecs) {
            recommendations.add(rec.getBook());
        }

        return recommendations;
    }

    /**
     * Obtiene sugerencias de amigos usando el sistema de afinidad
     */
    public LinkedList<Reader> getSuggestions(LibrarySystem librarySystem) {
        if (library == null) {
            return new LinkedList<>();
        }

        // Usar el sistema de afinidad para encontrar lectores similares
        AffinitySystem affinitySystem = new AffinitySystem(library);
        return affinitySystem.getSuggestedFriends(this);
    }

    /**
     * Método corregido para prestar libros (era lendBook, ahora loanBook)
     */
    public boolean loanBook(Book book) {
        return requestLoan(book); // Usa el método existente
    }

    /**
     * Método para enviar mensajes a otros lectores
     */
    public boolean sendMessage(Reader recipient, String message) {
        if (recipient == null || message == null || message.trim().isEmpty()) {
            return false;
        }

        // Verificar que están conectados en el grafo de afinidad
        if (library != null) {
            AffinitySystem affinitySystem = new AffinitySystem(library);
            LinkedList<Reader> path = affinitySystem.getShortestPath(this, recipient);

            if (path.getSize() == 0) {
                throw new RuntimeException("No estás conectado con este lector");
            }
        }

        // Crear mensaje
        String formattedMessage = "De " + this.getUsername() + ": " + message;
        recipient.receiveMessage(formattedMessage);

        return true;
    }

    // =============== SISTEMA DE VALORACIONES ===============

    /**
     * Valora un libro - solo se puede valorar libros que se han leído
     */
    public boolean rateBook(Book book, int stars, String comment) {
        // Validaciones
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("La valoración debe estar entre 1 y 5 estrellas");
        }

        // Verificar que ha leído el libro (está en su historial)
        boolean hasRead = false;
        for (int i = 0; i < loanHistoryList.getSize(); i++) {
            if (loanHistoryList.getAmountNodo(i).getIdBook().equals(book.getIdBook())) {
                hasRead = true;
                break;
            }
        }

        if (!hasRead) {
            throw new RuntimeException("Solo puedes valorar libros que has leído");
        }

        // Verificar si ya valoró este libro
        for (int i = 0; i < ratingsList.getSize(); i++) {
            Rating existingRating = ratingsList.getAmountNodo(i);
            if (existingRating.getBook().getIdBook().equals(book.getIdBook())) {
                throw new RuntimeException("Ya has valorado este libro");
            }
        }

        // Crear nueva valoración
        Rating rating = new Rating(this, book, stars, comment);
        ratingsList.add(rating);

        // Actualizar la valoración promedio del libro
        book.addRating(stars);

        // Notificar al sistema de afinidad que se actualizó una valoración
        if (library != null) {
            // Aquí triggearías la actualización del grafo de afinidad
        }

        return true;
    }

    // =============== SISTEMA DE RECOMENDACIONES ===============

    /**
     * Obtiene recomendaciones de libros basadas en las valoraciones del lector
     * Algoritmo: Recomienda libros del mismo autor o categoría de libros valorados positivamente
     */
    public LinkedList<Book> getBookRecommendations() {
        LinkedList<Book> recommendations = new LinkedList<>();
        LinkedList<String> preferredAuthors = new LinkedList<>();
        LinkedList<String> preferredCategories = new LinkedList<>();

        // Analizar valoraciones positivas (4-5 estrellas)
        for (int i = 0; i < ratingsList.getSize(); i++) {
            Rating rating = ratingsList.getAmountNodo(i);
            if (rating.getStars() >= 4) {
                Book likedBook = rating.getBook();

                // Recopilar autores y categorías preferidas
                if (!containsString(preferredAuthors, likedBook.getAuthor())) {
                    preferredAuthors.add(likedBook.getAuthor());
                }
                if (!containsString(preferredCategories, likedBook.getCategory())) {
                    preferredCategories.add(likedBook.getCategory());
                }
            }
        }

        // Buscar libros de autores y categorías preferidas que no haya leído
        LinkedList<Book> allBooks = library.getBookssList();
        for (int i = 0; i < allBooks.getSize(); i++) {
            Book book = allBooks.getAmountNodo(i);

            // Verificar que no lo ha leído
            if (!hasReadBook(book)) {
                // Verificar si es de autor o categoría preferida
                if (containsString(preferredAuthors, book.getAuthor()) ||
                        containsString(preferredCategories, book.getCategory())) {
                    recommendations.add(book);
                }
            }
        }

        return recommendations;
    }

    /**
     * Verifica si el lector ha leído un libro
     */
    private boolean hasReadBook(Book book) {
        for (int i = 0; i < loanHistoryList.getSize(); i++) {
            if (loanHistoryList.getAmountNodo(i).getIdBook().equals(book.getIdBook())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método auxiliar para verificar si una lista contiene un string
     */
    private boolean containsString(LinkedList<String> list, String item) {
        for (int i = 0; i < list.getSize(); i++) {
            if (list.getAmountNodo(i).equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }

    // =============== SISTEMA SOCIAL ===============

    /**
     * Obtiene sugerencias de amigos usando el sistema de afinidad
     */
    public LinkedList<Reader> getFriendSuggestions() {
        if (library == null) {
            return new LinkedList<>();
        }

        AffinitySystem affinitySystem = new AffinitySystem(library);
        return affinitySystem.getSuggestedFriends(this);
    }
    

    /**
     * Recibe un mensaje
     */
    public void receiveMessage(String message) {
        messages.add(message);
    }



    // =============== GETTERS Y SETTERS ===============

    public LinkedList<Book> getLoanHistoryList() {
        return loanHistoryList;
    }

    public LinkedList<Rating> getRatingsList() {
        return ratingsList;
    }

    public LinkedList<String> getMessages() {
        return messages;
    }

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    @Override
    public String toString() {
        return getName() + " (" + getUsername() + ")";
    }
}
