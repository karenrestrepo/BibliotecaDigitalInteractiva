package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

import java.io.File;
import java.io.IOException;

public class Library {
    private static Library instance;
    private Persistence persistence;

    // UNIFICADO: Una sola fuente de verdad para cada tipo de dato
    private HashMap<String, Reader> readers = new HashMap<>();
    private HashMap<String, Book> books = new HashMap<>();
    private HashMap<String, Administrator> administrators = new HashMap<>();
    private HashMap<String, Rating> ratings = new HashMap<>();
    private Graph<String> readerConnections = new Graph<>();

    // CONSTRUCTOR PRIVADO para Singleton
    public Library() {
        persistence = new Persistence();
        loadDataFromPersistence();
    }

    // CORREGIDO: Singleton thread-safe
    public static synchronized Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    // ELIMINADO: Constructor público que recibía Persistence
    // RAZÓN: Violaba el patrón Singleton y creaba inconsistencias

    // CORREGIDO: Método unificado para cargar datos
    private void loadDataFromPersistence() {
        try {
            readers = persistence.loadReaders();
            books = persistence.loadBooks();
            administrators = persistence.loadAdministrators();
            // Establecer referencia a library en cada reader
            LinkedList<String> readerKeys = readers.keySet();
            for (int i = 0; i < readerKeys.getSize(); i++) {
                Reader reader = readers.get(readerKeys.getAmountNodo(i));
                reader.setLibrary(this); // Si el Reader tiene este método
            }
        } catch (Exception e) {
            System.err.println("Error loading data from persistence: " + e.getMessage());
        }
    }

    // CORREGIDO: Método para crear libro con mejor manejo de errores
    public Book createBook(String id, String title, String author, int year, String category)
            throws IllegalArgumentException {
        // Validación de parámetros
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

        // Verificar que no existe
        if (books.containsKey(id)) {
            throw new IllegalArgumentException("Book with ID '" + id + "' already exists");
        }

        Book newBook = new Book(id.trim(), title.trim(), author.trim(), year,
                category != null ? category.trim() : "");

        books.put(id, newBook);

        // Persistir cambios
        if (!persistence.saveBook(newBook)) {
            books.remove(id); // Rollback si falla la persistencia
            throw new RuntimeException("Failed to save book to persistence");
        }

        return newBook;
    }

    // CORREGIDO: Método para eliminar libro
    public boolean removeBook(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        Book book = books.get(id);
        if (book == null) {
            return false;
        }

        books.remove(id);

        // Guardar cambios en persistencia
        if (!persistence.saveAllBooks(books)) {
            books.put(id, book); // Rollback si falla
            return false;
        }

        return true;
    }

    // CORREGIDO: Método para registrar lector
    public boolean registerReader(String name, String username, String password) {
        // Validación de parámetros
        if (name == null || name.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();

        if (readers.containsKey(cleanUsername)) {
            return false; // Ya existe
        }

        Reader reader = new Reader(name.trim(), cleanUsername, password.trim());
        reader.setLibrary(this); // Si el Reader tiene este método

        // Guardar en memoria
        readers.put(cleanUsername, reader);

        // Persistir
        if (!persistence.saveReader(reader)) {
            readers.remove(cleanUsername); // Rollback
            return false;
        }

        return true;
    }

    // CORREGIDO: Método para eliminar lector
    public boolean deleteReader(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();

        if (!readers.containsKey(cleanUsername)) {
            return false;
        }

        Reader reader = readers.get(cleanUsername);
        readers.remove(cleanUsername);

        // Persistir cambios
        if (!persistence.deleteReader(cleanUsername)) {
            readers.put(cleanUsername, reader); // Rollback
            return false;
        }

        return true;
    }

    // CORREGIDO: Método para actualizar lector
    public boolean updateReader(String username, String newName, String newPassword) {
        if (username == null || username.trim().isEmpty() ||
                newName == null || newName.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();

        if (!readers.containsKey(cleanUsername)) {
            return false;
        }

        Reader reader = readers.get(cleanUsername);
        String oldName = reader.getName();
        String oldPassword = reader.getPassword();

        // Actualizar en memoria
        reader.setName(newName.trim());
        reader.setPassword(newPassword.trim());

        // Persistir cambios
        if (!persistence.updateReader(cleanUsername, newName.trim(), newPassword.trim())) {
            // Rollback
            reader.setName(oldName);
            reader.setPassword(oldPassword);
            return false;
        }

        return true;
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

            // Reestablecer referencias
            LinkedList<String> readerKeys = readers.keySet();
            for (int i = 0; i < readerKeys.getSize(); i++) {
                Reader reader = readers.get(readerKeys.getAmountNodo(i));
                reader.setLibrary(this); // Si el Reader tiene este método
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
        return persistence.saveRating(rating);
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
        return persistence.saveConnection(username1, username2);
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

    // NUEVO: Método para obtener estadísticas
    public String getLibraryStats() {
        return String.format("Biblioteca - Lectores: %d, Libros: %d, Administradores: %d, Valoraciones: %d",
                readers.size(), books.size(), administrators.size(), ratings.size());
    }
}
