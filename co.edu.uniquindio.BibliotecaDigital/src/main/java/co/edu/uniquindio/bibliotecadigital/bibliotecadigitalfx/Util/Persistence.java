package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller.LibraryStatsController;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller.ManageBooksController;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.BinarySearchTree;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class Persistence {
    private static Person currentUser;

    // CONFIGURACIÓN DE RUTAS UNIFICADA - SOLUCIÓN AL PROBLEMA PRINCIPAL
    private static final String BASE_PATH = "src/main/resources/Archivos/";
    private static final String RESOURCES_PACKAGE = "Archivos/";

    // Rutas específicas de cada archivo
    private static final String READERS_FILE = "Readers/Readers.txt";
    private static final String ADMINS_FILE = "Administrators/Administrators.txt";
    private static final String BOOKS_FILE = "Books/Books.txt";
    private static final String RATINGS_FILE = "Ratings/Ratings.txt";
    private static final String CONNECTIONS_FILE = "Connections/Connections.txt";

    public Persistence() {
        System.out.println("🔄 Inicializando sistema de persistencia corregido...");
        ensureDirectoriesExist();
        verifyAndCreateFiles();
    }

    /**
     * SOLUCIÓN 1: Asegurar que todas las carpetas necesarias existan
     */
    private void ensureDirectoriesExist() {
        try {
            String[] directories = {"Readers", "Administrators", "Books", "Ratings", "Connections"};

            for (String dir : directories) {
                Path dirPath = Paths.get(BASE_PATH + dir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                    System.out.println("📁 Carpeta creada: " + dirPath);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error creando directorios: " + e.getMessage());
        }
    }

    /**
     * SOLUCIÓN 2: Verificar y crear archivos con headers si no existen
     */
    private void verifyAndCreateFiles() {
        createFileIfNotExists(ADMINS_FILE, "# Archivo de administradores - Nombre,Usuario,Contraseña");
        createFileIfNotExists(READERS_FILE, "# Archivo de lectores - Nombre,Usuario,Contraseña");
        createFileIfNotExists(BOOKS_FILE, "# Archivo de libros - ID,Título,Autor,Año,Categoría");
        createFileIfNotExists(RATINGS_FILE, "# Archivo de valoraciones - Usuario,LibroID,Estrellas,Comentario");
        createFileIfNotExists(CONNECTIONS_FILE, "# Archivo de conexiones - Usuario1,Usuario2");
    }

    private void createFileIfNotExists(String relativePath, String header) {
        try {
            Path filePath = Paths.get(BASE_PATH + relativePath);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                Files.write(filePath, header.getBytes());
                System.out.println("📄 Archivo creado: " + relativePath);
            }
        } catch (IOException e) {
            System.err.println("❌ Error creando archivo " + relativePath + ": " + e.getMessage());
        }
    }

    /**
     * SOLUCIÓN 3: Método unificado de lectura de archivos
     */
    private BufferedReader getFileReader(String relativePath) throws IOException {
        // Prioridad 1: Leer desde filesystem (desarrollo)
        Path filesystemPath = Paths.get(BASE_PATH + relativePath);
        if (Files.exists(filesystemPath)) {
            System.out.println("✅ Leyendo " + relativePath + " desde filesystem");
            return Files.newBufferedReader(filesystemPath);
        }

        // Prioridad 2: Leer desde classpath (producción)
        InputStream classPathStream = getClass().getClassLoader()
                .getResourceAsStream(RESOURCES_PACKAGE + relativePath);
        if (classPathStream != null) {
            System.out.println("✅ Leyendo " + relativePath + " desde classpath");
            return new BufferedReader(new InputStreamReader(classPathStream));
        }

        throw new IOException("❌ No se puede encontrar " + relativePath);
    }

    /**
     * SOLUCIÓN 4: Método unificado de escritura de archivos
     */
    private BufferedWriter getFileWriter(String relativePath, boolean append) throws IOException {
        Path filePath = Paths.get(BASE_PATH + relativePath);

        // Asegurar que el directorio padre existe
        Files.createDirectories(filePath.getParent());

        if (append) {
            return Files.newBufferedWriter(filePath,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } else {
            return Files.newBufferedWriter(filePath,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    // ==================== MÉTODOS DE CARGA CORREGIDOS ====================

    public HashMap<String, Administrator> loadAdministrators() {
        HashMap<String, Administrator> admins = new HashMap<>();

        try (BufferedReader reader = getFileReader(ADMINS_FILE)) {
            String line;
            int validCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    String username = parts[1].trim();
                    String password = parts[2].trim();

                    if (!name.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                        Administrator admin = new Administrator(name, username, password);
                        admins.put(username, admin);
                        validCount++;
                        System.out.println("👤 Admin cargado: " + name);
                    }
                }
            }

            System.out.println("✅ Administradores cargados: " + validCount);

        } catch (IOException e) {
            System.err.println("⚠️ Error leyendo administradores, creando por defecto: " + e.getMessage());
            createDefaultAdministrators(admins);
        }

        // Si no hay administradores, crear por defecto
        if (admins.size() == 0) {
            createDefaultAdministrators(admins);
        }

        return admins;
    }

    public HashMap<String, Reader> loadReaders() {
        HashMap<String, Reader> readers = new HashMap<>();

        try (BufferedReader reader = getFileReader(READERS_FILE)) {
            String line;
            int validCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    String username = parts[1].trim();
                    String password = parts[2].trim();

                    if (!name.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                        Reader readerObj = new Reader(name, username, password);
                        readers.put(username, readerObj);
                        validCount++;
                        System.out.println("📚 Lector cargado: " + name);
                    }
                }
            }

            System.out.println("✅ Lectores cargados: " + validCount);

        } catch (IOException e) {
            System.err.println("⚠️ Error leyendo lectores, creando por defecto: " + e.getMessage());
            createDefaultReaders(readers);
        }

        // Si no hay lectores, crear por defecto
        if (readers.size() == 0) {
            createDefaultReaders(readers);
        }

        return readers;
    }

    public HashMap<String, Book> loadBooks() {
        HashMap<String, Book> books = new HashMap<>();

        try (BufferedReader reader = getFileReader(BOOKS_FILE)) {
            String line;
            int validCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String id = parts[0].trim();
                    String title = parts[1].trim();
                    String author = parts[2].trim();
                    String yearStr = parts[3].trim();
                    String category = parts[4].trim();

                    try {
                        int year = Integer.parseInt(yearStr);

                        if (!id.isEmpty() && !title.isEmpty() && !author.isEmpty() && !category.isEmpty()) {
                            Book book = new Book(id, title, author, year, category);
                            books.put(id, book);
                            validCount++;
                            System.out.println("📖 Libro cargado: " + title);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ Año inválido para libro: " + line);
                    }
                }
            }

            System.out.println("✅ Libros cargados: " + validCount);

        } catch (IOException e) {
            System.err.println("⚠️ Error leyendo libros, creando por defecto: " + e.getMessage());
            createDefaultBooks(books);
        }

        // Si no hay libros, crear por defecto
        if (books.size() == 0) {
            createDefaultBooks(books);
        }

        return books;
    }

    public BinarySearchTree<Book> loadBooksTree(Comparator<Book> comparator) {
        BinarySearchTree<Book> booksTree = new BinarySearchTree<>(comparator);
        HashMap<String, Book> books = loadBooks();

        // Insertar todos los libros en el árbol
        LinkedList<String> keys = books.keySet();
        for (int i = 0; i < keys.getSize(); i++) {
            Book book = books.get(keys.getAmountNodo(i));
            booksTree.insert(book);
        }

        System.out.println("🌳 Árbol de libros creado con " + booksTree.size() + " elementos");
        return booksTree;
    }

    // ==================== MÉTODOS DE ESCRITURA CORREGIDOS ====================

    /**
     * SOLUCIÓN 5: Guardar un solo lector (append)
     */
    public boolean saveReader(Reader reader) {
        if (reader == null || reader.getUsername() == null || reader.getUsername().trim().isEmpty()) {
            return false;
        }

        try (BufferedWriter writer = getFileWriter(READERS_FILE, true)) {
            String line = String.format("%s,%s,%s",
                    reader.getName(),
                    reader.getUsername(),
                    reader.getPassword());
            writer.write(line);
            writer.newLine();
            writer.flush();

            System.out.println("💾 Lector guardado: " + reader.getUsername());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error guardando lector: " + e.getMessage());
            return false;
        }
    }

    /**
     * SOLUCIÓN 6: Guardar un solo libro (append)
     */
    public boolean saveBook(Book book) {
        if (book == null || book.getIdBook() == null || book.getIdBook().trim().isEmpty()) {
            return false;
        }

        try (BufferedWriter writer = getFileWriter(BOOKS_FILE, true)) {
            String line = String.format("%s,%s,%s,%d,%s",
                    book.getIdBook(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear(),
                    book.getCategory());
            writer.write(line);
            writer.newLine();
            writer.flush();

            System.out.println("💾 Libro guardado: " + book.getTitle());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error guardando libro: " + e.getMessage());
            return false;
        }
    }

    /**
     * SOLUCIÓN 7: Reescribir todos los lectores (para updates/deletes)
     */
    public boolean saveAllReaders(HashMap<String, Reader> readers) {
        try (BufferedWriter writer = getFileWriter(READERS_FILE, false)) {
            writer.write("# Archivo de lectores - Nombre,Usuario,Contraseña");
            writer.newLine();

            LinkedList<String> keys = readers.keySet();
            for (int i = 0; i < keys.getSize(); i++) {
                String key = keys.getAmountNodo(i);
                Reader reader = readers.get(key);
                if (reader != null) {
                    String line = String.format("%s,%s,%s",
                            reader.getName(),
                            reader.getUsername(),
                            reader.getPassword());
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();

            System.out.println("💾 Todos los lectores guardados: " + keys.getSize());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error guardando todos los lectores: " + e.getMessage());
            return false;
        }
    }

    /**
     * SOLUCIÓN 8: Reescribir todos los libros (para updates/deletes)
     */
    public boolean saveAllBooks(HashMap<String, Book> books) {
        try (BufferedWriter writer = getFileWriter(BOOKS_FILE, false)) {
            writer.write("# Archivo de libros - ID,Título,Autor,Año,Categoría");
            writer.newLine();

            LinkedList<String> keys = books.keySet();
            for (int i = 0; i < keys.getSize(); i++) {
                String key = keys.getAmountNodo(i);
                Book book = books.get(key);
                if (book != null) {
                    String line = String.format("%s,%s,%s,%d,%s",
                            book.getIdBook(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getYear(),
                            book.getCategory());
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();

            System.out.println("💾 Todos los libros guardados: " + keys.getSize());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error guardando todos los libros: " + e.getMessage());
            return false;
        }
    }

    // ==================== OPERACIONES CRUD ====================

    public boolean updateReader(String username, String newName, String newPassword) {
        HashMap<String, Reader> readers = loadReaders();
        if (!readers.containsKey(username.trim())) {
            System.err.println("❌ Lector no encontrado: " + username);
            return false;
        }

        Reader reader = readers.get(username.trim());
        reader.setName(newName.trim());
        reader.setPassword(newPassword.trim());

        return saveAllReaders(readers);
    }

    public boolean deleteReader(String username) {
        HashMap<String, Reader> readers = loadReaders();
        if (!readers.containsKey(username.trim())) {
            System.err.println("❌ Lector no encontrado para eliminar: " + username);
            return false;
        }

        readers.remove(username.trim());
        return saveAllReaders(readers);
    }

    // ==================== OTROS TIPOS DE DATOS ====================

    public boolean saveRating(Rating rating) {
        if (rating == null || rating.getReader() == null || rating.getBook() == null) {
            return false;
        }

        try (BufferedWriter writer = getFileWriter(RATINGS_FILE, true)) {
            String line = String.format("%s,%s,%d,%s",
                    rating.getReader().getUsername(),
                    rating.getBook().getIdBook(),
                    rating.getStars(),
                    rating.getComment() != null ? rating.getComment().replace(",", ";") : "");
            writer.write(line);
            writer.newLine();
            writer.flush();

            System.out.println("💾 Valoración guardada");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error guardando valoración: " + e.getMessage());
            return false;
        }
    }

    public boolean saveConnection(String user1, String user2) {
        if (user1 == null || user2 == null || user1.trim().isEmpty() || user2.trim().isEmpty()) {
            return false;
        }

        try (BufferedWriter writer = getFileWriter(CONNECTIONS_FILE, true)) {
            writer.write(user1.trim() + "," + user2.trim());
            writer.newLine();
            writer.flush();

            System.out.println("💾 Conexión guardada: " + user1 + " <-> " + user2);
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error guardando conexión: " + e.getMessage());
            return false;
        }
    }



    private int loadAdminsFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = getFileWriter(ADMINS_FILE, true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    writer.write(line);
                    writer.newLine();
                    count++;
                }
            }
            writer.flush();
        }
        return count;
    }


    // ==================== SISTEMA DE AUTENTICACIÓN ====================

    public Person login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            System.out.println("❌ Credenciales vacías");
            return null;
        }

        String cleanUsername = username.trim();
        String cleanPassword = password.trim();

        System.out.println("🔐 Intentando login: " + cleanUsername);

        // Intentar como administrador
        HashMap<String, Administrator> administrators = loadAdministrators();
        Administrator admin = administrators.get(cleanUsername);
        if (admin != null && admin.getPassword().equals(cleanPassword)) {
            currentUser = admin;
            System.out.println("✅ Login exitoso como administrador: " + admin.getName());
            return admin;
        }

        // Intentar como lector
        HashMap<String, Reader> readers = loadReaders();
        Reader reader = readers.get(cleanUsername);
        if (reader != null && reader.getPassword().equals(cleanPassword)) {
            currentUser = reader;
            try {
                reader.setLibrary(Library.getInstance());
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo establecer referencia a biblioteca: " + e.getMessage());
            }
            System.out.println("✅ Login exitoso como lector: " + reader.getName());
            return reader;
        }

        System.out.println("❌ Usuario no encontrado o contraseña incorrecta: " + cleanUsername);
        return null;
    }

    // ==================== MÉTODO PRINCIPAL CORREGIDO ====================

    public String loadDataFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return "❌ Archivo inválido o no se puede leer";
        }

        try {
            String fileName = file.getName().toLowerCase();
            int recordsLoaded = 0;
            String resultMessage = "";
            String dataType = "";

            if (fileName.contains("lector") || fileName.contains("reader")) {
                recordsLoaded = loadReadersFromExternalFile(file);
                resultMessage = "✅ Se cargaron " + recordsLoaded + " lectores desde " + file.getName();
                dataType = "readers";
            }
            else if (fileName.contains("libro") || fileName.contains("book")) {
                recordsLoaded = loadBooksFromExternalFile(file);
                resultMessage = "✅ Se cargaron " + recordsLoaded + " libros desde " + file.getName();
                dataType = "books";
            }
            else if (fileName.contains("valoracion") || fileName.contains("rating") || fileName.contains("calificacion")) {
                recordsLoaded = loadRatingsFromExternalFile(file);
                resultMessage = "✅ Se cargaron " + recordsLoaded + " valoraciones desde " + file.getName();
                dataType = "ratings";
            }
            else if (fileName.contains("conexion") || fileName.contains("connection")) {
                recordsLoaded = loadConnectionsFromExternalFile(file);
                resultMessage = "✅ Se cargaron " + recordsLoaded + " conexiones desde " + file.getName();
                dataType = "connections";
            }
            else {
                recordsLoaded = detectAndLoadFromContent(file);
                resultMessage = "✅ Se detectó y cargó " + recordsLoaded + " registros desde " + file.getName();
                dataType = "auto";
            }

            // CORRECCIÓN: Actualización selectiva según el tipo de datos
            if (recordsLoaded > 0) {
                refreshSpecificData(dataType);
                resultMessage += "\n🔄 Datos específicos actualizados.";
            }

            return resultMessage;

        } catch (Exception e) {
            return "❌ Error al cargar archivo: " + e.getMessage();
        }
    }

    private void refreshSpecificData(String dataType) {
        try {
            switch (dataType) {
                case "readers":
                    refreshReadersData();
                    break;
                case "books":
                    refreshBooksData();
                    break;
                case "ratings":
                    refreshRatingsData();
                    break;
                case "connections":
                    // Las conexiones no requieren actualización especial
                    System.out.println("✅ Conexiones cargadas");
                    break;
                case "auto":
                    // Actualización completa solo si es detección automática
                    refreshDataFromPersistence();
                    break;
            }
        } catch (Exception e) {
            System.err.println("❌ Error en actualización específica: " + e.getMessage());
        }
    }

// ==================== NUEVOS MÉTODOS DE CARGA ====================

    /**
     * NUEVO: Carga valoraciones desde archivo externo
     */
    private int loadRatingsFromExternalFile(File file) throws IOException {
        int count = 0;
        HashMap<String, Reader> readers = loadReaders();
        HashMap<String, Book> books = loadBooks();

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = getFileWriter(RATINGS_FILE, true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String username = parts[0].trim();
                        String bookTitle = parts[1].trim();
                        String starsStr = parts[2].trim();
                        String comment = parts.length > 3 ? parts[3].trim() : "";

                        try {
                            int stars = Integer.parseInt(starsStr);

                            // Verificar que el lector existe
                            Reader readerObj = readers.get(username);
                            if (readerObj != null) {
                                // Buscar el libro por título
                                Book bookObj = findBookByTitle(bookTitle, books);
                                if (bookObj != null) {
                                    // CORRECCIÓN: Guardar en archivo
                                    writer.write(username + "," + bookObj.getIdBook() + "," + stars + "," + comment);
                                    writer.newLine();

                                    // CORRECCIÓN: También agregar a la lista del reader en memoria
                                    Rating rating = new Rating(readerObj, bookObj, stars, comment);
                                    readerObj.getRatingsList().add(rating);

                                    // CORRECCIÓN: Actualizar valoración promedio del libro
                                    bookObj.addRating(stars);

                                    count++;
                                    System.out.println("⭐ Valoración cargada: " + username + " -> " + bookTitle + " (" + stars + "★)");
                                } else {
                                    System.err.println("⚠️ Libro no encontrado: " + bookTitle);
                                }
                            } else {
                                System.err.println("⚠️ Lector no encontrado: " + username);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("⚠️ Puntuación inválida: " + line);
                        }
                    }
                }
            }
            writer.flush();
        }
        return count;
    }

    public HashMap<String, Rating> loadRatings() {
        HashMap<String, Rating> ratings = new HashMap<>();
        HashMap<String, Reader> readers = loadReaders();
        HashMap<String, Book> books = loadBooks();

        try (BufferedReader reader = getFileReader(RATINGS_FILE)) {
            String line;
            int validCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String username = parts[0].trim();
                    String bookId = parts[1].trim();
                    String starsStr = parts[2].trim();
                    String comment = parts.length > 3 ? parts[3].trim() : "";

                    try {
                        int stars = Integer.parseInt(starsStr);

                        Reader readerObj = readers.get(username);
                        Book bookObj = books.get(bookId);

                        if (readerObj != null && bookObj != null) {
                            Rating rating = new Rating(readerObj, bookObj, stars, comment);
                            String key = username + "|" + bookId;
                            ratings.put(key, rating);

                            // IMPORTANTE: Agregar a la lista del reader
                            readerObj.getRatingsList().add(rating);

                            validCount++;
                            System.out.println("⭐ Rating cargado en memoria: " + username + " -> " + bookObj.getTitle());
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ Puntuación inválida: " + line);
                    }
                }
            }

            System.out.println("✅ Valoraciones cargadas en memoria: " + validCount);

        } catch (IOException e) {
            System.err.println("⚠️ Error leyendo valoraciones: " + e.getMessage());
        }

        return ratings;
    }

    /**
     * NUEVO: Carga conexiones desde archivo externo
     */
    private int loadConnectionsFromExternalFile(File file) throws IOException {
        int count = 0;
        HashMap<String, Reader> readers = loadReaders();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String user1 = parts[0].trim();
                        String user2 = parts[1].trim();

                        // Verificar que ambos lectores existen
                        if (readers.containsKey(user1) && readers.containsKey(user2)) {
                            if (saveConnection(user1, user2)) {
                                count++;
                                System.out.println("🤝 Conexión cargada: " + user1 + " <-> " + user2);
                            }
                        } else {
                            System.err.println("⚠️ Uno o ambos lectores no encontrados: " + user1 + ", " + user2);
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * CORRECCIÓN 3: Método auxiliar para buscar libro por título
     */
    private Book findBookByTitle(String title, HashMap<String, Book> books) {
        LinkedList<String> keys = books.keySet();
        for (int i = 0; i < keys.getSize(); i++) {
            Book book = books.get(keys.getAmountNodo(i));
            if (book.getTitle().equalsIgnoreCase(title.trim())) {
                return book;
            }
        }
        return null;
    }

// ==================== MÉTODOS DE CARGA CORREGIDOS ====================

    /**
     * CORRECCIÓN 4: Método de carga de lectores mejorado (sin duplicados)
     */
    private int loadReadersFromExternalFile(File file) throws IOException {
        int count = 0;
        HashMap<String, Reader> existingReaders = loadReaders();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String name = parts[0].trim();
                        String username = parts[1].trim();
                        String password = parts[2].trim();

                        // VERIFICAR DUPLICADOS
                        if (!existingReaders.containsKey(username)) {
                            if (saveReader(new Reader(name, username, password))) {
                                count++;
                                System.out.println("👤 Lector cargado: " + name + " (" + username + ")");
                            }
                        } else {
                            System.out.println("⚠️ Lector ya existe, omitido: " + username);
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * CORRECCIÓN 5: Método de carga de libros mejorado (sin duplicados)
     */
    private int loadBooksFromExternalFile(File file) throws IOException {
        int count = 0;
        HashMap<String, Book> existingBooks = loadBooks();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String id = parts[0].trim();
                        String title = parts[1].trim();
                        String author = parts[2].trim();
                        String yearStr = parts[3].trim();
                        String category = parts[4].trim();

                        try {
                            int year = Integer.parseInt(yearStr);

                            // VERIFICAR DUPLICADOS POR ID Y POR TÍTULO
                            if (!existingBooks.containsKey(id) && !bookExistsByTitle(title, existingBooks)) {
                                Book newBook = new Book(id, title, author, year, category);
                                if (saveBook(newBook)) {
                                    count++;
                                    System.out.println("📖 Libro cargado: " + title + " (" + id + ")");
                                }
                            } else {
                                System.out.println("⚠️ Libro ya existe, omitido: " + title + " (ID: " + id + ")");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("⚠️ Año inválido para libro: " + line);
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * CORRECCIÓN 6: Método auxiliar para verificar si un libro existe por título
     */
    private boolean bookExistsByTitle(String title, HashMap<String, Book> books) {
        LinkedList<String> keys = books.keySet();
        for (int i = 0; i < keys.getSize(); i++) {
            Book book = books.get(keys.getAmountNodo(i));
            if (book.getTitle().equalsIgnoreCase(title.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * CORRECCIÓN 7: Método de detección inteligente mejorado
     */
    private int detectAndLoadFromContent(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null && !firstLine.startsWith("#")) {
                String[] parts = firstLine.split(",");

                // Detectar por número de campos y contenido
                if (parts.length == 3) {
                    // Podría ser lector o valoración
                    // Verificar si el tercer campo es numérico (valoración) o texto (contraseña)
                    try {
                        Integer.parseInt(parts[2].trim());
                        // Es numérico, probablemente una valoración
                        System.out.println("🔍 Detectado como archivo de valoraciones (3 campos, tercer campo numérico)");
                        return loadRatingsFromExternalFile(file);
                    } catch (NumberFormatException e) {
                        // No es numérico, probablemente un lector
                        System.out.println("🔍 Detectado como archivo de lectores (3 campos, tercer campo texto)");
                        return loadReadersFromExternalFile(file);
                    }
                }
                else if (parts.length == 4) {
                    // Probablemente valoración con comentario
                    System.out.println("🔍 Detectado como archivo de valoraciones (4 campos)");
                    return loadRatingsFromExternalFile(file);
                }
                else if (parts.length >= 5) {
                    // Probablemente libros
                    System.out.println("🔍 Detectado como archivo de libros (5+ campos)");
                    return loadBooksFromExternalFile(file);
                }
                else if (parts.length == 2) {
                    // Probablemente conexiones
                    System.out.println("🔍 Detectado como archivo de conexiones (2 campos)");
                    return loadConnectionsFromExternalFile(file);
                }
            }
        }
        System.out.println("⚠️ No se pudo detectar el tipo de archivo automáticamente");
        return 0;
    }

    /**
     * CORRECCIÓN: Método de actualización selectiva según el tipo de datos cargados
     */
    private void refreshDataFromPersistence() {
        // NO limpiar todo automáticamente
        System.out.println("🔄 Actualizando estructuras de datos específicas...");
    }

    /**
     * NUEVO: Método específico para actualizar solo lectores
     */
    public void refreshReadersData() {
        try {
            HashMap<String, Reader> newReaders = loadReaders();
            Library library = Library.getInstance();

            // Actualizar solo lectores
            library.getReadersMap().clear();
            LinkedList<String> readerKeys = newReaders.keySet();
            for (int i = 0; i < readerKeys.getSize(); i++) {
                String key = readerKeys.getAmountNodo(i);
                Reader reader = newReaders.get(key);
                reader.setLibrary(library);
                library.getReadersMap().put(key, reader);
            }

            System.out.println("✅ Lectores actualizados: " + newReaders.size());
        } catch (Exception e) {
            System.err.println("❌ Error actualizando lectores: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método específico para actualizar solo libros
     */
    public void refreshBooksData() {
        try {
            HashMap<String, Book> newBooks = loadBooks();
            Library library = Library.getInstance();

            // Actualizar solo libros
            library.getBooks().clear();
            LinkedList<String> bookKeys = newBooks.keySet();
            for (int i = 0; i < bookKeys.getSize(); i++) {
                String key = bookKeys.getAmountNodo(i);
                Book book = newBooks.get(key);
                library.getBooks().put(key, book);
            }

            // Reconstruir árboles de búsqueda solo para libros
            library.getTitleTree().clear();
            library.getAuthorTree().clear();
            library.getCategoryTree().clear();

            for (int i = 0; i < bookKeys.getSize(); i++) {
                Book book = newBooks.get(bookKeys.getAmountNodo(i));
                library.getTitleTree().insert(book);
                library.getAuthorTree().insert(book);
                library.getCategoryTree().insert(book);
            }

            System.out.println("✅ Libros actualizados: " + newBooks.size());
        } catch (Exception e) {
            System.err.println("❌ Error actualizando libros: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método específico para actualizar solo valoraciones
     */
    public void refreshRatingsData() {
        try {
            HashMap<String, Rating> newRatings = loadRatings();
            Library library = Library.getInstance();

            // Actualizar valoraciones
            library.getRatings().clear();
            LinkedList<String> ratingKeys = newRatings.keySet();
            for (int i = 0; i < ratingKeys.getSize(); i++) {
                String key = ratingKeys.getAmountNodo(i);
                library.getRatings().put(key, newRatings.get(key));
            }

            System.out.println("✅ Valoraciones actualizadas: " + newRatings.size());
        } catch (Exception e) {
            System.err.println("❌ Error actualizando valoraciones: " + e.getMessage());
        }
    }



    // ==================== DATOS POR DEFECTO ====================

    private void createDefaultAdministrators(HashMap<String, Administrator> admins) {
        System.out.println("🔧 Creando administradores por defecto...");

        Administrator admin1 = new Administrator("Admin Principal", "admin@biblioteca.com", "admin123");
        Administrator admin2 = new Administrator("María Bibliotecaria", "maria@biblioteca.com", "maria123");

        admins.put("admin@biblioteca.com", admin1);
        admins.put("maria@biblioteca.com", admin2);

        // Guardar en archivo
        saveAllAdministrators(admins);

        System.out.println("✅ Administradores por defecto creados:");
        System.out.println("   - admin@biblioteca.com / admin123");
        System.out.println("   - maria@biblioteca.com / maria123");
    }

    private void createDefaultReaders(HashMap<String, Reader> readers) {
        System.out.println("🔧 Creando lectores por defecto...");

        Reader reader1 = new Reader("Ana García", "ana@gmail.com", "ana123");
        Reader reader2 = new Reader("Juan Pérez", "juan@gmail.com", "juan123");
        Reader reader3 = new Reader("María López", "maria@gmail.com", "maria123");

        readers.put("ana@gmail.com", reader1);
        readers.put("juan@gmail.com", reader2);
        readers.put("maria@gmail.com", reader3);

        // Guardar en archivo
        saveAllReaders(readers);

        System.out.println("✅ Lectores por defecto creados:");
        System.out.println("   - ana@gmail.com / ana123");
        System.out.println("   - juan@gmail.com / juan123");
        System.out.println("   - maria@gmail.com / maria123");
    }

    private void createDefaultBooks(HashMap<String, Book> books) {
        System.out.println("🔧 Creando libros por defecto...");

        Book book1 = new Book("001", "El Quijote", "Miguel de Cervantes", 1605, "Clásico");
        Book book2 = new Book("002", "Cien Años de Soledad", "Gabriel García Márquez", 1967, "Realismo Mágico");
        Book book3 = new Book("003", "1984", "George Orwell", 1949, "Distopía");
        Book book4 = new Book("004", "Harry Potter", "J.K. Rowling", 1997, "Fantasía");
        Book book5 = new Book("005", "El Señor de los Anillos", "J.R.R. Tolkien", 1954, "Fantasía");

        books.put("001", book1);
        books.put("002", book2);
        books.put("003", book3);
        books.put("004", book4);
        books.put("005", book5);

        // Guardar en archivo
        saveAllBooks(books);

        System.out.println("✅ Libros por defecto creados:");
        System.out.println("   - 001: El Quijote");
        System.out.println("   - 002: Cien Años de Soledad");
        System.out.println("   - 003: 1984");
        System.out.println("   - 004: Harry Potter");
        System.out.println("   - 005: El Señor de los Anillos");
    }

    private boolean saveAllAdministrators(HashMap<String, Administrator> admins) {
        try (BufferedWriter writer = getFileWriter(ADMINS_FILE, false)) {
            writer.write("# Archivo de administradores - Nombre,Usuario,Contraseña");
            writer.newLine();

            LinkedList<String> keys = admins.keySet();
            for (int i = 0; i < keys.getSize(); i++) {
                String key = keys.getAmountNodo(i);
                Administrator admin = admins.get(key);
                if (admin != null) {
                    String line = String.format("%s,%s,%s",
                            admin.getName(),
                            admin.getUsername(),
                            admin.getPassword());
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();

            System.out.println("💾 Todos los administradores guardados");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error guardando administradores: " + e.getMessage());
            return false;
        }
    }

    // ==================== GESTIÓN DE SESIÓN ====================

    public static Person getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Person user) {
        currentUser = user;
        if (user != null) {
            System.out.println("👤 Usuario actual: " + user.getName() + " (" + user.getClass().getSimpleName() + ")");
        }
    }

    public static void logout() {
        if (currentUser != null) {
            System.out.println("👋 Cerrando sesión: " + currentUser.getName());
            currentUser = null;
        }
    }

    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }
}