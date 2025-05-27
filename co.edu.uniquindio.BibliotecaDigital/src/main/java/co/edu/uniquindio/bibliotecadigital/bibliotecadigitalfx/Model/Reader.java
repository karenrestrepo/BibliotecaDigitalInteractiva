package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.AffinitySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.BookRecommendationSystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

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
    public LinkedList<Reader> getSuggestions(Library library) {
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

        try {
            // Crear nueva valoración
            Rating rating = new Rating(this, book, stars, comment);

            // CORRECCIÓN 1: Agregar a la lista en memoria
            ratingsList.add(rating);

            // CORRECCIÓN 2: Persistir inmediatamente
            Persistence persistence = new Persistence();
            boolean saved = persistence.saveRating(rating);

            if (!saved) {
                // Rollback si falla la persistencia
                ratingsList.delete(rating);
                throw new RuntimeException("Error al guardar la valoración");
            }

            // CORRECCIÓN 3: Actualizar la valoración promedio del libro
            book.addRating(stars);

            // CORRECCIÓN 4: Actualizar en la biblioteca global
            if (library != null) {
                String ratingKey = this.getUsername() + "|" + book.getIdBook();
                library.getRatings().put(ratingKey, rating);
            }

            System.out.println("✅ Valoración guardada y persistida: " + this.getName() +
                    " -> " + book.getTitle() + " (" + stars + "★)");

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error en valoración: " + e.getMessage());
            throw new RuntimeException("Error al procesar la valoración: " + e.getMessage());
        }
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

                // Recopilar autores y categorías preferidas sin duplicados
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

            // Verificar que el usuario no lo ha leído
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
     * Solicita el préstamo de un libro con persistencia completa CORREGIDA
     */
    public boolean requestLoan(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("El libro no puede ser null");
        }

        if (book.getStatus() != BookStatus.AVAILABLE) {
            System.out.println("❌ Libro no disponible para préstamo: " + book.getTitle());
            return false;
        }

        // Verificar que no tenga ya este libro prestado
        for (Book loanedBook : this.loanHistoryList) {
            if (loanedBook.getIdBook().equals(book.getIdBook()) &&
                    loanedBook.getStatus() == BookStatus.CHECKED_OUT) {
                System.out.println("❌ Ya tienes este libro en préstamo");
                return false;
            }
        }

        // Variables para rollback
        BookStatus originalStatus = book.getStatus();
        boolean wasInHistory = this.loanHistoryList.contains(book);

        try {
            System.out.println("🔄 Iniciando préstamo: " + this.getName() + " -> " + book.getTitle());

            Persistence persistence = new Persistence();

            // PASO 1: Verificar y guardar préstamo PRIMERO (antes de cambiar estados)
            boolean loanSaved = persistence.saveLoan(this, book);
            if (!loanSaved) {
                System.err.println("❌ Error persistiendo préstamo");
                return false;
            }

            // PASO 2: Actualizar estado del libro en archivo
            boolean bookUpdated = persistence.updateBookStatus(book.getIdBook(), BookStatus.CHECKED_OUT);
            if (!bookUpdated) {
                // Rollback: eliminar préstamo
                persistence.removeLoan(this.getUsername(), book.getIdBook());
                System.err.println("❌ Error persistiendo estado del libro");
                return false;
            }

            // PASO 3: Solo AHORA actualizar en memoria (después de persistir)
            book.setStatus(BookStatus.CHECKED_OUT);
            if (!wasInHistory) {
                this.loanHistoryList.add(book);
            }

            System.out.println("✅ Préstamo exitoso y persistido: " + this.getName() + " -> " + book.getTitle());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error en préstamo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Devuelve un libro con persistencia completa
     */
    public boolean returnBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("El libro no puede ser null");
        }

        try {
            System.out.println("🔄 Verificando préstamo en persistencia para: " + book.getTitle());

            Persistence persistence = new Persistence();

            // CORRECCIÓN: Verificar en persistencia en lugar de memoria
            HashMap<String, Persistence.LoanRecord> activeLoans = persistence.loadActiveLoans();
            String loanKey = this.getUsername() + "|" + book.getIdBook();

            if (!activeLoans.containsKey(loanKey)) {
                throw new RuntimeException("No se encontró un préstamo activo de este libro en tu cuenta.");
            }

            System.out.println("✅ Préstamo encontrado en persistencia: " + loanKey);

            // PASO 1: Cambiar estado del libro a disponible
            boolean bookUpdated = persistence.updateBookStatus(book.getIdBook(), BookStatus.AVAILABLE);
            if (!bookUpdated) {
                throw new RuntimeException("Error actualizando el estado del libro en persistencia.");
            }

            // PASO 2: Eliminar préstamo del archivo
            boolean loanRemoved = persistence.removeLoan(this.getUsername(), book.getIdBook());
            if (!loanRemoved) {
                // Intentar rollback
                persistence.updateBookStatus(book.getIdBook(), BookStatus.CHECKED_OUT);
                throw new RuntimeException("Error eliminando el registro de préstamo.");
            }

            // PASO 3: Actualizar estado en memoria (solo si persistencia fue exitosa)
            book.setStatus(BookStatus.AVAILABLE);

            // OPCIONAL: Limpiar del historial en memoria si existe
            this.loanHistoryList.stream()
                    .filter(b -> b.getIdBook().equals(book.getIdBook()))
                    .findFirst()
                    .ifPresent(b -> b.setStatus(BookStatus.AVAILABLE));

            System.out.println("✅ Libro devuelto exitosamente: " + this.getName() + " -> " + book.getTitle());
            return true;

        } catch (RuntimeException e) {
            // Re-lanzar errores de negocio
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error técnico en devolución: " + e.getMessage());
            throw new RuntimeException("Error técnico al procesar la devolución: " + e.getMessage());
        }
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

    public void syncLoanHistoryFromPersistence() {
        try {
            Persistence persistence = new Persistence();
            HashMap<String, Persistence.LoanRecord> activeLoans = persistence.loadActiveLoans();

            // Limpiar historial actual
            this.loanHistoryList.clear();

            // Agregar préstamos activos desde persistencia
            LinkedList<String> loanKeys = activeLoans.keySet();
            for (int i = 0; i < loanKeys.getSize(); i++) {
                String key = loanKeys.getAmountNodo(i);
                Persistence.LoanRecord loanRecord = activeLoans.get(key);

                if (loanRecord.getReader().getUsername().equals(this.getUsername())) {
                    Book book = loanRecord.getBook();
                    book.setStatus(BookStatus.CHECKED_OUT);
                    this.loanHistoryList.add(book);
                    System.out.println("📚 Sincronizado préstamo: " + book.getTitle());
                }
            }

            System.out.println("✅ Historial del Reader sincronizado: " + this.loanHistoryList.getSize() + " préstamos activos");

        } catch (Exception e) {
            System.err.println("❌ Error sincronizando historial: " + e.getMessage());
        }
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
