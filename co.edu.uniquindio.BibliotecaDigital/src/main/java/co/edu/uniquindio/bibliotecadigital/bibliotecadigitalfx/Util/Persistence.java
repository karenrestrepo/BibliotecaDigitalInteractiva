package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Persistence {
    private static Person currentUser;

    // Rutas de archivos
    private static final String READERS_FILE =
            "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Readers/Readers.txt";
    private static final String ADMINS_FILE =
            "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Administrators/Administrators.txt";
    private static final String BOOKS_FILE =
            "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Books/Books.txt";
    private static final String RATINGS_FILE =
            "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Ratings/Ratings.txt";
    private static final String CONNECTIONS_FILE =
            "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Connections/Connections.txt";

    // ELIMINADO: Las estructuras de datos ya no se almacenan aquí
    // REEMPLAZADO: Solo manejo de persistencia, no de datos en memoria

    public Persistence() {
        // ELIMINADO: Inicialización de estructuras de datos
        // Solo verificamos que los archivos existan
        initializeFiles();
    }

    // NUEVO: Método para inicializar archivos si no existen
    private void initializeFiles() {
        createFileIfNotExists(READERS_FILE);
        createFileIfNotExists(ADMINS_FILE);
        createFileIfNotExists(BOOKS_FILE);
        createFileIfNotExists(RATINGS_FILE);
        createFileIfNotExists(CONNECTIONS_FILE);
    }

    // NUEVO: Método auxiliar para crear archivos
    private void createFileIfNotExists(String filePath) {
        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Error creating file " + filePath + ": " + e.getMessage());
        }
    }

    // CORREGIDO: Método para cargar lectores desde archivo
    public HashMap<String, Reader> loadReaders() {
        HashMap<String, Reader> readers = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(READERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // NUEVO: Limpiar espacios
                if (!line.isEmpty()) { // NUEVO: Verificar línea no vacía
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String name = parts[0].trim();
                        String username = parts[1].trim();
                        String password = parts[2].trim();
                        Reader user = new Reader(name, username, password);
                        readers.put(username, user);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading readers: " + e.getMessage());
        }
        return readers;
    }

    // CORREGIDO: Método para cargar administradores
    public HashMap<String, Administrator> loadAdministrators() {
        HashMap<String, Administrator> admins = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String name = parts[0].trim();
                        String username = parts[1].trim();
                        String password = parts[2].trim();
                        Administrator admin = new Administrator(name, username, password);
                        admins.put(username, admin);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading administrators: " + e.getMessage());
        }
        return admins;
    }

    // CORREGIDO: Método para cargar libros
    public HashMap<String, Book> loadBooks() {
        HashMap<String, Book> books = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String id = parts[0].trim();
                        String title = parts[1].trim();
                        String author = parts[2].trim();
                        try {
                            int year = Integer.parseInt(parts[3].trim());
                            String category = parts[4].trim();
                            Book book = new Book(id, title, author, year, category);
                            books.put(id, book);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing year for book: " + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
        return books;
    }

    // CORREGIDO: Método de login simplificado
    public Person login(String username, String password) {
        // Cargar datos frescos para cada login
        HashMap<String, Reader> readers = loadReaders();
        HashMap<String, Administrator> administrators = loadAdministrators();

        Reader user = readers.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }

        Administrator admin = administrators.get(username);
        if (admin != null && admin.getPassword().equals(password)) {
            currentUser = admin;
            return admin;
        }

        return null;
    }

    // CORREGIDO: Método para guardar lector
    public boolean saveReader(Reader reader) {
        // Verificar si ya existe
        HashMap<String, Reader> existingReaders = loadReaders();
        if (existingReaders.containsKey(reader.getUsername())) {
            return false; // Ya existe
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE, true))) {
            writer.write(reader.getName() + "," + reader.getUsername() + "," + reader.getPassword());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving reader: " + e.getMessage());
            return false;
        }
    }

    // CORREGIDO: Método para actualizar lector
    public boolean updateReader(String username, String newName, String newPassword) {
        HashMap<String, Reader> readers = loadReaders();

        if (!readers.containsKey(username)) {
            return false;
        }

        // Actualizar en memoria
        Reader reader = readers.get(username);
        reader.setName(newName);
        reader.setPassword(newPassword);

        // Guardar todos los lectores
        return saveAllReaders(readers);
    }

    // CORREGIDO: Método para eliminar lector
    public boolean deleteReader(String username) {
        HashMap<String, Reader> readers = loadReaders();

        if (!readers.containsKey(username)) {
            return false;
        }

        readers.remove(username);
        return saveAllReaders(readers);
    }

    // NUEVO: Método para guardar todos los lectores
    private boolean saveAllReaders(HashMap<String, Reader> readers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE))) {
            LinkedList<String> keys = readers.keySet();
            for (int i = 0; i < keys.getSize(); i++) {
                Reader reader = readers.get(keys.getAmountNodo(i));
                writer.write(reader.getName() + "," + reader.getUsername() + "," + reader.getPassword());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving all readers: " + e.getMessage());
            return false;
        }
    }

    // CORREGIDO: Método para guardar libro
    public boolean saveBook(Book book) {
        HashMap<String, Book> existingBooks = loadBooks();
        if (existingBooks.containsKey(book.getIdBook())) {
            return false; // Ya existe
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE, true))) {
            writer.write(book.getIdBook() + "," + book.getTitle() + "," +
                    book.getAuthor() + "," + book.getYear() + "," + book.getCategory());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving book: " + e.getMessage());
            return false;
        }
    }

    // CORREGIDO: Método para guardar todos los libros
    public boolean saveAllBooks(HashMap<String, Book> books) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            LinkedList<String> keys = books.keySet();
            for (int i = 0; i < keys.getSize(); i++) {
                Book book = books.get(keys.getAmountNodo(i));
                writer.write(book.getIdBook() + "," + book.getTitle() + "," +
                        book.getAuthor() + "," + book.getYear() + "," + book.getCategory());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving all books: " + e.getMessage());
            return false;
        }
    }

    // CORREGIDO: Método para cargar datos desde archivo externo
    public String loadDataFromFile(File file) {
        try {
            String fileName = file.getName().toLowerCase();

            if (fileName.contains("lectores") || fileName.contains("reader")) {
                return loadReadersFromExternalFile(file);
            } else if (fileName.contains("libros") || fileName.contains("book")) {
                return loadBooksFromExternalFile(file);
            } else if (fileName.contains("valoraciones") || fileName.contains("rating")) {
                return loadRatingsFromExternalFile(file);
            } else if (fileName.contains("conexiones") || fileName.contains("connection")) {
                return loadConnectionsFromExternalFile(file);
            } else {
                return "Tipo de archivo no reconocido: " + fileName;
            }
        } catch (IOException e) {
            return "Error al cargar archivo: " + e.getMessage();
        }
    }

    // NUEVO: Métodos específicos para cargar desde archivos externos
    private String loadReadersFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !fileContainsLine(Path.of(READERS_FILE), line)) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE, true))) {
                        writer.write(line);
                        writer.newLine();
                        count++;
                    }
                }
            }
        }
        return "Se cargaron " + count + " lectores nuevos desde " + file.getName();
    }

    private String loadBooksFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !fileContainsLine(Path.of(BOOKS_FILE), line)) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE, true))) {
                        writer.write(line);
                        writer.newLine();
                        count++;
                    }
                }
            }
        }
        return "Se cargaron " + count + " libros nuevos desde " + file.getName();
    }

    private String loadRatingsFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !fileContainsLine(Path.of(RATINGS_FILE), line)) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(RATINGS_FILE, true))) {
                        writer.write(line);
                        writer.newLine();
                        count++;
                    }
                }
            }
        }
        return "Se cargaron " + count + " valoraciones nuevas desde " + file.getName();
    }

    private String loadConnectionsFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !fileContainsLine(Path.of(CONNECTIONS_FILE), line)) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONNECTIONS_FILE, true))) {
                        writer.write(line);
                        writer.newLine();
                        count++;
                    }
                }
            }
        }
        return "Se cargaron " + count + " conexiones nuevas desde " + file.getName();
    }

    // MÉTODO AUXILIAR: Verificar si archivo contiene línea
    private boolean fileContainsLine(Path file, String lineToFind) throws IOException {
        if (!Files.exists(file)) return false;

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(lineToFind.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    // NUEVO: Método para guardar valoración
    public boolean saveRating(Rating rating) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RATINGS_FILE, true))) {
            writer.write(rating.getReader().getUsername() + "," +
                    rating.getBook().getIdBook() + "," +
                    rating.getStars() + "," + rating.getComment());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving rating: " + e.getMessage());
            return false;
        }
    }



    // NUEVO: Método para guardar conexión
    public boolean saveConnection(String user1, String user2) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONNECTIONS_FILE, true))) {
            writer.write(user1 + "," + user2);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving connection: " + e.getMessage());
            return false;
        }
    }

    // MÉTODOS ESTÁTICOS PARA USUARIO ACTUAL
    public static Person getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Person user) {
        currentUser = user;
    }
}
