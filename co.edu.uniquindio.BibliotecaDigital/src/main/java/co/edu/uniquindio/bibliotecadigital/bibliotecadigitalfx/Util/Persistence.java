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
    private static final String READERS_FILE = "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Readers/Readers.txt";
    private static final String ADMINS_FILE = "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Administrators/Administrators.txt";
    private static final String BOOKS_FILE = "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Books/Books.txt";
    private static final String RATINGS_FILE = "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Ratings/Ratings.txt";
    private static final String CONNECTIONS_FILE = "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/Connections/Connections.txt";


    private HashMap<String, Reader> readers; // Key: username
    private HashMap<String, Administrator> administrators;

    private HashMap<String, Book> books;
    Library library;

    public Persistence() throws IOException {
        readers = new HashMap<>();
        administrators = new HashMap<>();
        books = new HashMap<>();
        loadReadersFromFile();
        loadAdmin(); // always ensures admin exists
        loadBooksFromFile();
    }

    private void loadAdmin() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    administrators.put(username, new Administrator(name, username, password));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading administrators: " + e.getMessage());
        }
    }

    private void loadReadersFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(READERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0];
                    String username = parts[1];
                    String password = parts[2];

                    Reader r = new Reader(name, username, password);
                    readers.put(username, r);
                    if (library != null) {
                        library.getReadersList().addEnd(r);
                    }
                }
            }
        }
    }

    public void saveReaderToFile(Reader reader) {
        if (readers.containsKey(reader.getUsername())) {
            System.out.println("El lector ya está registrado.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE, true))) {

            writer.write(reader.getName() + "," + reader.getUsername() + "," + reader.getPassword());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving reader: " + e.getMessage());
        }
    }

    public Person login(String username, String password) {
        Reader user = readers.get(username);
        Administrator administrator = administrators.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }
        if (administrator != null && administrator.getPassword().equals(password)) {
            currentUser = administrator;
            return administrator;
        }
        return null;
    }

    public void loadBooksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String id = parts[0].trim();
                    String title = parts[1].trim();
                    String author = parts[2].trim();
                    int year = Integer.parseInt(parts[3].trim());
                    String category = parts[4].trim();
                    Book book = new Book(id, title, author, year, category);
                    books.put(id, book); // insertamos en el mapa por ID
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
    }

    public void saveBookToFile(Book book) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE, true))) {
            writer.write(book.getIdBook() + "," + book.getTitle() + "," + book.getAuthor() + "," + book.getYear() + "," + book.getCategory());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving book: " + e.getMessage());
        }
    }

    public void saveAllBooks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books.values()) {
                writer.write(book.getIdBook() + "," + book.getTitle() + "," + book.getAuthor() + "," + book.getYear() + "," + book.getCategory());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing books: " + e.getMessage());
        }
    }


    public LinkedList<Reader> getAllReaders() {
        LinkedList<Reader> list = new LinkedList<>();
        LinkedList<String> keys = readers.keySet();
        for (int i = 0; i < keys.getSize(); i++) {
            Reader u = readers.get(keys.getAmountNodo(i));
            if (u instanceof Reader) {
                list.addEnd((Reader) u);
            }
        }
        return list;
    }

    public boolean deleteReader(String username) {
        if (!readers.containsKey(username)) {
            return false;
        }

        // Elimina el lector del HashMap
        readers.remove(username);  // Asumiendo que tu remove() es void

        // Guarda los cambios en el archivo
        saveReaders(getAllReaders());
        return true;
    }


    public boolean updateReader(String username, String newName, String newPassword) {
        LinkedList<Reader> readers = getAllReaders();
        boolean updated = false;

        for (int i = 0; i < readers.getSize(); i++) {
            Reader r = readers.getAmountNodo(i);
            if (r.getUsername().equals(username)) {
                r.setName(newName);
                r.setPassword(newPassword);
                updated = true;
                break;
            }
        }

        if (updated) {
            saveReaders(readers);
        }

        return updated;
    }
    public void initializeData() {
        try {
            loadReadersFromFile();
            loadBooksFromFile();
            loadRatingsFromFile();
            loadConnectionsFromFile();
        } catch (IOException e) {
            System.out.println("Error initializing data: " + e.getMessage());
        }
    }

    public void saveReaders(LinkedList<Reader> readers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE))) {
            for (int i = 0; i < readers.getSize(); i++) {
                Reader r = readers.getAmountNodo(i);
                writer.write(r.getName() + "," + r.getUsername() + "," + r.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving readers: " + e.getMessage());
        }
    }

    public void loadDataFromFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        Path filePath = file.toPath();

        if (fileName.contains("lectores") || fileName.contains("reader")) {
            combineFiles(filePath, Path.of(READERS_FILE));
            loadReadersFromFile(); // Recargar datos en memoria
        }
        else if (fileName.contains("libros") || fileName.contains("book")) {
            combineFiles(filePath, Path.of(BOOKS_FILE));
            loadBooksFromFile();
        }
        else if (fileName.contains("valoraciones") || fileName.contains("rating")) {
            combineFiles(filePath, Path.of(RATINGS_FILE));
            loadRatingsFromFile();
        }
        else if (fileName.contains("conexiones") || fileName.contains("connection")) {
            combineFiles(filePath, Path.of(CONNECTIONS_FILE));
            loadConnectionsFromFile();
        }
    }

    private void loadRatingsFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(RATINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String username = parts[0].trim();
                    String bookId = parts[1].trim();
                    int score = Integer.parseInt(parts[2].trim());
                    String comment = parts[3].trim();

                    // Obtener objetos completos desde Library
                    Reader readerObj = library.getReaderByUsername(username);
                    Book bookObj = library.getBookById(bookId);

                    if (readerObj != null && bookObj != null) {
                        library.addRating(new Rating(readerObj, bookObj, score, comment));
                    }
                }
            }
        }
    }

    private void loadConnectionsFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(CONNECTIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    // Ejemplo: username1,username2
                    String username1 = parts[0].trim();
                    String username2 = parts[1].trim();

                    Reader reader1 = library.getReaderByUsername(username1);
                    Reader reader2 = library.getReaderByUsername(username2);

                    if (reader1 != null && reader2 != null) {
                        // Asumiendo que tienes un método addConnection en Library
                        library.addConnection(reader1, reader2);
                    }
                }
            }
        }
    }

    private void combineFiles(Path source, Path destination) throws IOException {
        // 1. Crear directorios si no existen
        if (!Files.exists(destination.getParent())) {
            Files.createDirectories(destination.getParent());
        }

        // 2. Si el archivo destino no existe, copiar directamente
        if (!Files.exists(destination)) {
            Files.copy(source, destination);
            return;
        }

        // 3. Combinar archivos evitando duplicados
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardOpenOption.APPEND);
             BufferedReader reader = Files.newBufferedReader(source)) {

            // 4. Agregar separador solo si el archivo destino no está vacío
            if (Files.size(destination) > 0) {
                writer.newLine();
            }

            // 5. Leer línea por línea y escribir evitando duplicados
            String line;
            while ((line = reader.readLine()) != null) {
                if (!fileContainsLine(destination, line)) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    private boolean fileContainsLine(Path file, String lineToFind) throws IOException {
        if (!Files.exists(file)) return false;

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(lineToFind)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void saveRatingToFile (Rating rating){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RATINGS_FILE, true))) {
            writer.write(rating.getReader().getUsername() + "," + rating.getBook().getIdBook() + "," +
                    rating.getStars() + "," + rating.getComment());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error guardando valoración: " + e.getMessage());
        }
    }

    public void saveConnectionToFile (String user1, String user2){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONNECTIONS_FILE, true))) {
            writer.write(user1 + "," + user2);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error guardando conexión: " + e.getMessage());
        }
    }


    public static Person getCurrentUser () {
        return currentUser;
    }

    public static void setCurrentUser (Person user){
        currentUser = user;
    }

    public HashMap<String, Book> getBooks () {
        return books;
    }

    public HashMap<String, Reader> getReaders () {
        return readers;
    }


}
