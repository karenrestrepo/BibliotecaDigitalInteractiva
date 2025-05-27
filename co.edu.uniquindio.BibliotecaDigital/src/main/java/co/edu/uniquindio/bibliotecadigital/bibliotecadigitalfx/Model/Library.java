package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.SearchTypes.AuthorComparator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.SearchTypes.CategoryComparator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.SearchTypes.TitleComparator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

import java.io.File;
import java.util.Comparator;

public class Library {
    private static Library instance;
    private Persistence persistence;

    // UNIFICADO: Una sola fuente de verdad para cada tipo de dato
    private HashMap<String, Reader> readers = new HashMap<>();
    private HashMap<String, Rating> ratings = new HashMap<>();
    private HashMap<String, Book> books = new HashMap<>();
    private BinarySearchTree<Book> titleTree = new BinarySearchTree<>(new TitleComparator());
    private BinarySearchTree<Book> authorTree = new BinarySearchTree<>(new AuthorComparator());
    private BinarySearchTree<Book> categoryTree = new BinarySearchTree<>(new CategoryComparator());
    private PriorityQueue<Book> booksOnHold = new PriorityQueue<>();
    private HashMap<String, Book> loanBooks = new HashMap<>();

    private HashMap<String, Administrator> administrators = new HashMap<>();
    private Graph<String> readerConnections = new Graph<>();

    // CONSTRUCTOR PRIVADO para Singleton
    public Library() {
        // SOLUCIÓN: Asignar instance ANTES de cargar datos
        persistence = new Persistence();

        // NO llamamos loadDataFromPersistence aquí para evitar el ciclo
    }

    // CORREGIDO: Singleton thread-safe con inicialización lazy
    public static synchronized Library getInstance() {
        if (instance == null) {
            instance = new Library();
            // SOLUCIÓN: Cargar datos DESPUÉS de asignar instance
            instance.initializeData();
        }
        return instance;
    }

    // NUEVO: Método separado para inicializar datos sin ciclos
    private void initializeData() {
        try {
            loadDataFromPersistence();
        } catch (Exception e) {
            System.err.println("Error en inicialización: " + e.getMessage());
            createDefaultData();
        }
    }


    // NUEVO: Crear datos mínimos si no se pueden cargar
    private void createDefaultData() {
        System.out.println("🏗️ Creando datos mínimos por defecto...");

        // Administrador por defecto
        Administrator defaultAdmin = new Administrator("Admin Sistema", "admin@biblioteca.com", "admin123");
        administrators.put("admin@biblioteca.com", defaultAdmin);

        // Libros por defecto
        Book book1 = new Book("001", "El Quijote", "Miguel de Cervantes", 1605, "Clásico");
        Book book2 = new Book("002", "Cien Años de Soledad", "Gabriel García Márquez", 1967, "Realismo Mágico");
        books.put("001", book1);
        books.put("002", book2);

        System.out.println("✅ Datos por defecto creados exitosamente");
    }

    // CORREGIDO: Método para obtener libro por ID
    public Book getBookById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return books.get(id.trim());
    }

    // CORREGIDO: Verificar existencia de libro
    public boolean bookExists(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return books.containsKey(id.trim());
    }

    // CORREGIDO: Obtener lista de lectores (conversión desde HashMap)
    public LinkedList<Reader> getReadersList() {
        LinkedList<Reader> list = new LinkedList<>();
        LinkedList<String> keys = readers.keySet();

        for (int i = 0; i < keys.getSize(); i++) {
            list.addEnd(readers.get(keys.getAmountNodo(i)));
        }

        return list;
    }

    // NUEVO: Método mejorado para obtener lector por username
    public Reader getReaderByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return readers.get(username.trim());
    }

    // CORREGIDO: Cargar datos desde archivo externo
    public String loadDataFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return "Archivo inválido o no se puede leer";
        }

        try {
            String result = persistence.loadDataFromFile(file);

            // Recargar datos en memoria después de la carga
            refreshDataFromPersistence();

            return result;
        } catch (Exception e) {
            return "Error al cargar archivo: " + e.getMessage();
        }
    }

    // NUEVO: Método para refrescar datos desde persistencia
    private void refreshDataFromPersistence() {
        try {
            readers.clear();
            books.clear();
            administrators.clear();

            readers = persistence.loadReaders();
            books = persistence.loadBooks();
            administrators = persistence.loadAdministrators();

            // Reestablecer referencias de forma segura
            LinkedList<String> readerKeys = readers.keySet();
            for (int i = 0; i < readerKeys.getSize(); i++) {
                Reader reader = readers.get(readerKeys.getAmountNodo(i));
                reader.setLibrary(this);
            }

            // LIMPIAR los árboles antes de volver a insertar
            titleTree.clear();
            authorTree.clear();
            categoryTree.clear();

            // REINSERTAR los libros en los árboles
            LinkedList<String> bookKeys = books.keySet();
            for (int i = 0; i < bookKeys.getSize(); i++) {
                Book b = books.get(bookKeys.getAmountNodo(i));
                titleTree.insert(b);
                authorTree.insert(b);
                categoryTree.insert(b);
            }

        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
        }
    }

    // CORREGIDO: Método para agregar valoración
    public boolean addRating(Rating rating) {
        if (rating == null || rating.getReader() == null || rating.getBook() == null) {
            return false;
        }

        String key = rating.getReader().getUsername() + "|" + rating.getBook().getIdBook();

        if (ratings.containsKey(key)) {
            return false; // Ya existe una valoración
        }

        ratings.put(key, rating);

        try {
            return persistence != null ? persistence.saveRating(rating) : true;
        } catch (Exception e) {
            System.err.println("Warning: Could not persist rating: " + e.getMessage());
            return true; // Mantener en memoria aunque no se persista
        }
    }

    // CORREGIDO: Método para agregar conexión entre lectores
    public boolean addConnection(Reader reader1, Reader reader2) {
        if (reader1 == null || reader2 == null ||
                reader1.getUsername() == null || reader2.getUsername() == null) {
            return false;
        }

        String username1 = reader1.getUsername().trim();
        String username2 = reader2.getUsername().trim();

        if (!readers.containsKey(username1) || !readers.containsKey(username2)) {
            return false; // Uno o ambos lectores no existen
        }

        if (username1.equals(username2)) {
            return false; // No puede conectarse consigo mismo
        }

        readerConnections.addEdge(username1, username2);

        try {
            return persistence != null ? persistence.saveConnection(username1, username2) : true;
        } catch (Exception e) {
            System.err.println("Warning: Could not persist connection: " + e.getMessage());
            return true; // Mantener en memoria aunque no se persista
        }
    }

    /**
     * CORRECCIÓN: Crear libro con persistencia inmediata
     */
    public Book createBook(String id, String title, String author, int year, String category)
            throws IllegalArgumentException {

        // Validaciones (mantén las que ya tienes)
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be null or empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Book author cannot be null or empty");
        }
        if (year < 0) {
            throw new IllegalArgumentException("Book year cannot be negative");
        }

        String cleanId = id.trim();

        // Verificar que no existe
        if (books.containsKey(cleanId)) {
            throw new IllegalArgumentException("Book with ID '" + cleanId + "' already exists");
        }

        // Crear libro
        Book newBook = new Book(cleanId, title.trim(), author.trim(), year,
                category != null ? category.trim() : "");

        // Guardar en memoria
        books.put(cleanId, newBook);
        titleTree.insert(newBook);
        authorTree.insert(newBook);
        categoryTree.insert(newBook);

        // CORRECCIÓN: Persistir inmediatamente
        try {
            if (!persistence.saveBook(newBook)) {
                // Rollback si falla la persistencia
                books.remove(cleanId);
                titleTree.delete(newBook);
                authorTree.delete(newBook);
                categoryTree.delete(newBook);
                throw new RuntimeException("Failed to save book to persistence");
            }
            System.out.println("✅ Libro creado y persistido: " + title);
        } catch (Exception e) {
            // Rollback
            books.remove(cleanId);
            titleTree.delete(newBook);
            authorTree.delete(newBook);
            categoryTree.delete(newBook);
            System.err.println("❌ Error persistiendo libro: " + e.getMessage());
            throw new RuntimeException("Failed to persist book", e);
        }

        return newBook;
    }

    /**
     * CORRECCIÓN: Registrar lector con persistencia inmediata
     */
    public boolean registerReader(String name, String username, String password) {
        // Validaciones (mantén las que ya tienes)
        if (name == null || name.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            System.err.println("❌ Campos vacíos en registro de lector");
            return false;
        }

        String cleanUsername = username.trim();

        if (readers.containsKey(cleanUsername)) {
            System.err.println("❌ Username ya existe: " + cleanUsername);
            return false;
        }

        // Crear lector
        Reader reader = new Reader(name.trim(), cleanUsername, password.trim());
        reader.setLibrary(this);

        // Guardar en memoria
        readers.put(cleanUsername, reader);

        // CORRECCIÓN: Persistir inmediatamente
        try {
            if (!persistence.saveReader(reader)) {
                readers.remove(cleanUsername);
                System.err.println("❌ Error persistiendo lector");
                return false;
            }
            System.out.println("✅ Lector registrado y persistido: " + name.trim());
            return true;
        } catch (Exception e) {
            readers.remove(cleanUsername);
            System.err.println("❌ Error en persistencia de lector: " + e.getMessage());
            return false;
        }
    }

    private void loadDataFromPersistence() {
        try {
            readers = persistence.loadReaders();
            books = persistence.loadBooks();
            titleTree = persistence.loadBooksTree(Comparator.comparing(book -> book.getTitle().toLowerCase()));
            authorTree = persistence.loadBooksTree(Comparator.comparing(book -> book.getAuthor().toLowerCase()));
            categoryTree = persistence.loadBooksTree(Comparator.comparing(book -> book.getCategory().toLowerCase()));
            administrators = persistence.loadAdministrators();

            // NUEVO: Cargar valoraciones después de lectores y libros
            ratings = persistence.loadRatings();

            // Establecer referencias DESPUÉS de cargar todo
            LinkedList<String> readerKeys = readers.keySet();
            for (int i = 0; i < readerKeys.getSize(); i++) {
                Reader reader = readers.get(readerKeys.getAmountNodo(i));
                reader.setLibrary(this);
            }

            System.out.println("✅ Biblioteca inicializada completamente:");
            System.out.println("   - Lectores: " + readers.size());
            System.out.println("   - Libros: " + books.size());
            System.out.println("   - Administradores: " + administrators.size());
            System.out.println("   - Valoraciones: " + ratings.size());

        } catch (Exception e) {
            System.err.println("Error loading data from persistence: " + e.getMessage());
            createDefaultData();
        }
    }

    // AGREGAR: Método específico para refrescar valoraciones
    public void refreshRatingsFromFile() {
        try {
            System.out.println("🔄 Actualizando valoraciones desde archivo...");

            // Limpiar valoraciones actuales de los lectores
            LinkedList<String> readerKeys = readers.keySet();
            for (int i = 0; i < readerKeys.getSize(); i++) {
                Reader reader = readers.get(readerKeys.getAmountNodo(i));
                reader.getRatingsList().clear();
            }

            // Recargar valoraciones desde persistencia
            ratings = persistence.loadRatings();

            System.out.println("✅ Valoraciones actualizadas: " + ratings.size());

        } catch (Exception e) {
            System.err.println("❌ Error actualizando valoraciones: " + e.getMessage());
        }
    }

    // CORRECCIÓN: Actualizar el método de estadísticas
    public String getLibraryStats() {
        return String.format("Biblioteca - Lectores: %d, Libros: %d, Administradores: %d, Valoraciones: %d",
                readers.size(), books.size(), administrators.size(), ratings.size());
    }

    /**
     * CORRECCIÓN: Eliminar libro con persistencia
     */
    public boolean removeBook(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String cleanId = id.trim();
        Book book = books.get(cleanId);
        if (book == null) {
            System.err.println("❌ Libro no encontrado para eliminar: " + cleanId);
            return false;
        }

        // Remover de memoria
        books.remove(cleanId);
        titleTree.delete(book);
        authorTree.delete(book);
        categoryTree.delete(book);

        // CORRECCIÓN: Persistir cambios reescribiendo todo el archivo
        try {
            if (!persistence.saveAllBooks(books)) {
                // Rollback si falla
                books.put(cleanId, book);
                titleTree.insert(book);
                authorTree.insert(book);
                categoryTree.insert(book);
                System.err.println("❌ Error persistiendo eliminación de libro");
                return false;
            }
            System.out.println("✅ Libro eliminado y cambios persistidos: " + book.getTitle());
            return true;
        } catch (Exception e) {
            // Rollback
            books.put(cleanId, book);
            titleTree.insert(book);
            authorTree.insert(book);
            categoryTree.insert(book);
            System.err.println("❌ Error en persistencia de eliminación: " + e.getMessage());
            return false;
        }
    }

    /**
     * CORRECCIÓN: Eliminar lector con persistencia
     */
    public boolean deleteReader(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();

        if (!readers.containsKey(cleanUsername)) {
            System.err.println("❌ Lector no encontrado para eliminar: " + cleanUsername);
            return false;
        }

        Reader reader = readers.get(cleanUsername);
        readers.remove(cleanUsername);

        // CORRECCIÓN: Usar método de persistencia mejorado
        try {
            if (!persistence.deleteReader(cleanUsername)) {
                readers.put(cleanUsername, reader); // Rollback
                System.err.println("❌ Error persistiendo eliminación de lector");
                return false;
            }
            System.out.println("✅ Lector eliminado y cambios persistidos: " + reader.getName());
            return true;
        } catch (Exception e) {
            readers.put(cleanUsername, reader); // Rollback
            System.err.println("❌ Error en persistencia de eliminación: " + e.getMessage());
            return false;
        }
    }

    /**
     * CORRECCIÓN: Actualizar lector con persistencia
     */
    public boolean updateReader(String username, String newName, String newPassword) {
        if (username == null || username.trim().isEmpty() ||
                newName == null || newName.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();

        if (!readers.containsKey(cleanUsername)) {
            System.err.println("❌ Lector no encontrado para actualizar: " + cleanUsername);
            return false;
        }

        Reader reader = readers.get(cleanUsername);
        String oldName = reader.getName();
        String oldPassword = reader.getPassword();

        // Actualizar en memoria
        reader.setName(newName.trim());
        reader.setPassword(newPassword.trim());

        // CORRECCIÓN: Usar método de persistencia mejorado
        try {
            if (!persistence.updateReader(cleanUsername, newName.trim(), newPassword.trim())) {
                // Rollback
                reader.setName(oldName);
                reader.setPassword(oldPassword);
                System.err.println("❌ Error persistiendo actualización de lector");
                return false;
            }
            System.out.println("✅ Lector actualizado y cambios persistidos: " + newName.trim());
            return true;
        } catch (Exception e) {
            // Rollback
            reader.setName(oldName);
            reader.setPassword(oldPassword);
            System.err.println("❌ Error en persistencia de actualización: " + e.getMessage());
            return false;
        }
    }

    public void forceRefreshAllData() {
        try {
            System.out.println("🔄 Forzando actualización completa de datos...");

            // Recargar desde persistencia
            Persistence persistence = new Persistence();
            HashMap<String, Reader> newReaders = persistence.loadReaders();
            HashMap<String, Book> newBooks = persistence.loadBooks();

            // Limpiar estructuras actuales
            readers.clear();
            books.clear();

            // Reestablecer datos
            readers = newReaders;
            books = newBooks;

            // Establecer referencias de biblioteca en lectores
            LinkedList<String> readerKeys = readers.keySet();
            for (int i = 0; i < readerKeys.getSize(); i++) {
                Reader reader = readers.get(readerKeys.getAmountNodo(i));
                reader.setLibrary(this);
            }

            // Reconstruir árboles
            titleTree.clear();
            authorTree.clear();
            categoryTree.clear();

            LinkedList<String> bookKeys = books.keySet();
            for (int i = 0; i < bookKeys.getSize(); i++) {
                Book book = books.get(bookKeys.getAmountNodo(i));
                titleTree.insert(book);
                authorTree.insert(book);
                categoryTree.insert(book);
            }

            System.out.println("✅ Actualización completa terminada:");
            System.out.println("   - Lectores: " + readers.size());
            System.out.println("   - Libros: " + books.size());

        } catch (Exception e) {
            System.err.println("❌ Error en actualización forzada: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // MÉTODOS GETTER CORREGIDOS
    public LinkedList<Reader> getReaders() {
        return getReadersList(); // Usar el método que convierte desde HashMap
    }

    public LinkedList<Book> getBookssList() {
        LinkedList<Book> list = new LinkedList<>();
        LinkedList<String> keys = books.keySet();

        for (int i = 0; i < keys.getSize(); i++) {
            list.addEnd(books.get(keys.getAmountNodo(i)));
        }

        return list;
    }

    public LinkedList<Administrator> getAdministrators() {
        LinkedList<Administrator> list = new LinkedList<>();
        LinkedList<String> keys = administrators.keySet();

        for (int i = 0; i < keys.getSize(); i++) {
            list.addEnd(administrators.get(keys.getAmountNodo(i)));
        }

        return list;
    }

    public HashMap<String, Book> getBooks() {
        return books;
    }

    public HashMap<String, Reader> getReadersMap() {
        return readers;
    }
    public HashMap<String, Rating> getRatings() {
        return ratings;
    }

    // Devuelve el árbol binario de búsqueda que organiza los libros por autor.
    public BinarySearchTree<Book> getAuthorTree() {
        return authorTree;
    }

    // Devuelve el árbol binario de búsqueda que organiza los libros por categoría.
    public BinarySearchTree<Book> getCategoryTree() {
        return categoryTree;
    }

    // Devuelve el árbol binario de búsqueda que organiza los libros por título.
    public BinarySearchTree<Book> getTitleTree() {
        return titleTree;
    }

    // Devuelve la cola de prioridad que contiene los libros que están en espera (por ejemplo, solicitados por usuarios).
    public PriorityQueue<Book> getBooksOnHold() {
        return booksOnHold;
    }

    // Devuelve un mapa hash que relaciona el ID del préstamo (o algún identificador) con el libro que está prestado.
    public HashMap<String, Book> getLoanBooks() {
        return loanBooks;
    }
}
