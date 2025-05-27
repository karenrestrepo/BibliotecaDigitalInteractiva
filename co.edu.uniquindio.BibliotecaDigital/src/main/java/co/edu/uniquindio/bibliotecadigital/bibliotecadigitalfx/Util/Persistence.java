package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

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

    // ==================== CARGA DESDE ARCHIVOS EXTERNOS ====================

    /**
     * SOLUCIÓN 9: Método mejorado para cargar datos externos
     */
    public String loadDataFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return "❌ Archivo inválido o no se puede leer";
        }

        try {
            String fileName = file.getName().toLowerCase();
            int recordsLoaded = 0;

            if (fileName.contains("lector") || fileName.contains("reader")) {
                recordsLoaded = loadReadersFromExternalFile(file);
                return "✅ Se cargaron " + recordsLoaded + " lectores desde " + file.getName();
            }
            else if (fileName.contains("libro") || fileName.contains("book")) {
                recordsLoaded = loadBooksFromExternalFile(file);
                return "✅ Se cargaron " + recordsLoaded + " libros desde " + file.getName();
            }
            else if (fileName.contains("admin")) {
                recordsLoaded = loadAdminsFromExternalFile(file);
                return "✅ Se cargaron " + recordsLoaded + " administradores desde " + file.getName();
            }
            else {
                recordsLoaded = detectAndLoadFromContent(file);
                return "✅ Se detectó y cargó " + recordsLoaded + " registros desde " + file.getName();
            }

        } catch (Exception e) {
            return "❌ Error al cargar archivo: " + e.getMessage();
        }
    }

    private int loadReadersFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = getFileWriter(READERS_FILE, true)) {

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

    private int loadBooksFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = getFileWriter(BOOKS_FILE, true)) {

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

    private int detectAndLoadFromContent(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null && !firstLine.startsWith("#")) {
                String[] parts = firstLine.split(",");
                if (parts.length == 3) {
                    return loadReadersFromExternalFile(file);
                } else if (parts.length >= 5) {
                    return loadBooksFromExternalFile(file);
                }
            }
        }
        return 0;
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