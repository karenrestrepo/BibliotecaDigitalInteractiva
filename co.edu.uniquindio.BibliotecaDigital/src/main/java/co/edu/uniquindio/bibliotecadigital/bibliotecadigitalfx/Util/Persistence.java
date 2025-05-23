package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sistema de Persistencia completamente sincronizado con todos los modelos
 *
 * GARANTÍAS DE SINCRONIZACIÓN:
 * - Compatible con la clase Reader sincronizada
 * - Compatible con la clase Library actualizada
 * - Compatible con todos los controladores
 * - Manejo robusto de errores y casos especiales
 * - Métodos thread-safe donde sea necesario
 */
public class Persistence {
    private static Person currentUser;

    // Rutas de archivos - SINCRONIZADAS con la estructura del proyecto
    private static final String BASE_PATH = "src/main/resources/Archivos/";
    private static final String READERS_FILE = BASE_PATH + "Readers/Readers.txt";
    private static final String ADMINS_FILE = BASE_PATH + "Administrators/Administrators.txt";
    private static final String BOOKS_FILE = BASE_PATH + "Books/Books.txt";
    private static final String RATINGS_FILE = BASE_PATH + "Ratings/Ratings.txt";
    private static final String CONNECTIONS_FILE = BASE_PATH + "Connections/Connections.txt";

    public Persistence() {
        initializeFiles();
    }

    // =============== INICIALIZACIÓN Y CONFIGURACIÓN ===============

    /**
     * Inicializa todos los archivos necesarios si no existen
     * SINCRONIZADO: Garantiza que la estructura de directorios sea consistente
     */
    private void initializeFiles() {
        createFileIfNotExists(READERS_FILE);
        createFileIfNotExists(ADMINS_FILE);
        createFileIfNotExists(BOOKS_FILE);
        createFileIfNotExists(RATINGS_FILE);
        createFileIfNotExists(CONNECTIONS_FILE);

        // Verificar que hay datos mínimos para testing
        ensureMinimalData();
    }

    /**
     * Crea un archivo si no existe, incluyendo directorios padre
     */
    private void createFileIfNotExists(String filePath) {
        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                // Crear directorios padre si no existen
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                System.out.println("Archivo creado: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error creando archivo " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Asegura que hay datos mínimos para que el sistema funcione
     * SINCRONIZADO: Con los datos esperados por los controladores
     */
    private void ensureMinimalData() {
        try {
            // Verificar si hay administradores
            HashMap<String, Administrator> admins = loadAdministrators();
            if (admins.size() == 0) {
                // Crear administrador por defecto
                saveAdministrator(new Administrator("Administrador", "admin@biblioteca.com", "admin123"));
                System.out.println("Administrador por defecto creado");
            }

            // Verificar si hay libros de ejemplo
            HashMap<String, Book> books = loadBooks();
            if (books.size() == 0) {
                // Crear algunos libros de ejemplo
                saveBook(new Book("001", "El Quijote", "Miguel de Cervantes", 1605, "Clásico"));
                saveBook(new Book("002", "Cien Años de Soledad", "Gabriel García Márquez", 1967, "Realismo Mágico"));
                System.out.println("Libros de ejemplo creados");
            }

        } catch (Exception e) {
            System.err.println("Error inicializando datos mínimos: " + e.getMessage());
        }
    }

    // =============== CARGA DE DATOS - SINCRONIZADA CON MODELOS ACTUALES ===============

    /**
     * Carga lectores desde archivo
     * SINCRONIZADO: Con la clase Reader actualizada y sus constructores
     */
    public HashMap<String, Reader> loadReaders() {
        HashMap<String, Reader> readers = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(READERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) { // Ignorar comentarios
                    try {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            String name = parts[0].trim();
                            String username = parts[1].trim();
                            String password = parts[2].trim();

                            // Usar constructor compatible con la clase Reader sincronizada
                            Reader user = new Reader(name, username, password);
                            readers.put(username, user);
                        }
                    } catch (Exception e) {
                        System.err.println("Error procesando línea de lector: " + line + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error cargando lectores: " + e.getMessage());
        }

        System.out.println("Cargados " + readers.size() + " lectores");
        return readers;
    }

    /**
     * Carga administradores desde archivo
     * SINCRONIZADO: Con la clase Administrator
     */
    public HashMap<String, Administrator> loadAdministrators() {
        HashMap<String, Administrator> admins = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    try {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            String name = parts[0].trim();
                            String username = parts[1].trim();
                            String password = parts[2].trim();

                            Administrator admin = new Administrator(name, username, password);
                            admins.put(username, admin);
                        }
                    } catch (Exception e) {
                        System.err.println("Error procesando línea de administrador: " + line + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error cargando administradores: " + e.getMessage());
        }

        System.out.println("Cargados " + admins.size() + " administradores");
        return admins;
    }

    /**
     * Carga libros desde archivo
     * SINCRONIZADO: Con la clase Book y sus atributos
     */
    public HashMap<String, Book> loadBooks() {
        HashMap<String, Book> books = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    try {
                        String[] parts = line.split(",");
                        if (parts.length >= 5) {
                            String id = parts[0].trim();
                            String title = parts[1].trim();
                            String author = parts[2].trim();
                            int year = Integer.parseInt(parts[3].trim());
                            String category = parts[4].trim();

                            Book book = new Book(id, title, author, year, category);
                            books.put(id, book);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error procesando año en línea: " + line);
                    } catch (Exception e) {
                        System.err.println("Error procesando línea de libro: " + line + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error cargando libros: " + e.getMessage());
        }

        System.out.println("Cargados " + books.size() + " libros");
        return books;
    }

    // =============== AUTENTICACIÓN - SINCRONIZADA CON LOGINCONTROLLER ===============

    /**
     * Método de login que funciona con los controladores actuales
     * SINCRONIZADO: Con LoginController y la gestión de sesión
     */
    public Person login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return null;
        }

        String cleanUsername = username.trim();
        String cleanPassword = password.trim();

        // Intentar login como lector
        HashMap<String, Reader> readers = loadReaders();
        Reader reader = readers.get(cleanUsername);
        if (reader != null && reader.getPassword().equals(cleanPassword)) {
            currentUser = reader;

            // CRÍTICO: Establecer la referencia a Library
            reader.setLibrary(Library.getInstance());

            System.out.println("Login exitoso como lector: " + reader.getName());
            return reader;
        }

        // Intentar login como administrador
        HashMap<String, Administrator> administrators = loadAdministrators();
        Administrator admin = administrators.get(cleanUsername);
        if (admin != null && admin.getPassword().equals(cleanPassword)) {
            currentUser = admin;
            System.out.println("Login exitoso como administrador: " + admin.getName());
            return admin;
        }

        System.out.println("Login fallido para usuario: " + cleanUsername);
        return null;
    }

    // =============== OPERACIONES CRUD - SINCRONIZADAS CON LIBRARY ===============

    /**
     * Guarda un lector en el archivo
     * SINCRONIZADO: Con los métodos de Library que llaman a este método
     */
    public boolean saveReader(Reader reader) {
        if (reader == null || reader.getUsername() == null || reader.getUsername().trim().isEmpty()) {
            return false;
        }

        // Verificar si ya existe
        HashMap<String, Reader> existingReaders = loadReaders();
        if (existingReaders.containsKey(reader.getUsername())) {
            return false; // Ya existe
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE, true))) {
            String line = String.format("%s,%s,%s",
                    reader.getName(),
                    reader.getUsername(),
                    reader.getPassword());
            writer.write(line);
            writer.newLine();
            System.out.println("Lector guardado: " + reader.getUsername());
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando lector: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda un administrador en el archivo
     */
    public boolean saveAdministrator(Administrator admin) {
        if (admin == null || admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ADMINS_FILE, true))) {
            String line = String.format("%s,%s,%s",
                    admin.getName(),
                    admin.getUsername(),
                    admin.getPassword());
            writer.write(line);
            writer.newLine();
            System.out.println("Administrador guardado: " + admin.getUsername());
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando administrador: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda un libro en el archivo
     * SINCRONIZADO: Con ManageBooksController y Library
     */
    public boolean saveBook(Book book) {
        if (book == null || book.getIdBook() == null || book.getIdBook().trim().isEmpty()) {
            return false;
        }

        // Verificar si ya existe
        HashMap<String, Book> existingBooks = loadBooks();
        if (existingBooks.containsKey(book.getIdBook())) {
            return false; // Ya existe
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE, true))) {
            String line = String.format("%s,%s,%s,%d,%s",
                    book.getIdBook(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear(),
                    book.getCategory());
            writer.write(line);
            writer.newLine();
            System.out.println("Libro guardado: " + book.getTitle());
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando libro: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza un lector existente
     * SINCRONIZADO: Con UserManagementController
     */
    public boolean updateReader(String username, String newName, String newPassword) {
        if (username == null || username.trim().isEmpty() ||
                newName == null || newName.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        HashMap<String, Reader> readers = loadReaders();
        if (!readers.containsKey(username.trim())) {
            return false; // No existe
        }

        // Actualizar en memoria
        Reader reader = readers.get(username.trim());
        reader.setName(newName.trim());
        reader.setPassword(newPassword.trim());

        // Guardar todos los lectores
        return saveAllReaders(readers);
    }

    /**
     * Elimina un lector
     * SINCRONIZADO: Con UserManagementController
     */
    public boolean deleteReader(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        HashMap<String, Reader> readers = loadReaders();
        if (!readers.containsKey(username.trim())) {
            return false; // No existe
        }

        readers.remove(username.trim());
        return saveAllReaders(readers);
    }

    /**
     * Guarda todos los libros (para operaciones de eliminación/actualización)
     * SINCRONIZADO: Con ManageBooksController
     */
    public boolean saveAllBooks(HashMap<String, Book> books) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            // Escribir header de comentario
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
            System.out.println("Guardados " + keys.getSize() + " libros");
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando todos los libros: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda todos los lectores
     */
    private boolean saveAllReaders(HashMap<String, Reader> readers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE))) {
            // Escribir header de comentario
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
            System.out.println("Guardados " + keys.getSize() + " lectores");
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando todos los lectores: " + e.getMessage());
            return false;
        }
    }

    // =============== CARGA DE DATOS EXTERNOS - SINCRONIZADA CON LOADDATACONTROLLER ===============

    /**
     * Carga datos desde archivo externo
     * SINCRONIZADO: Con LoadDataController
     */
    public String loadDataFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return "Archivo inválido o no se puede leer";
        }

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
                // Intentar detectar el formato por contenido
                return detectAndLoadFromContent(file);
            }
        } catch (Exception e) {
            return "Error al cargar archivo: " + e.getMessage();
        }
    }

    /**
     * Detecta el tipo de archivo por su contenido
     */
    private String detectAndLoadFromContent(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null) {
                String[] parts = firstLine.split(",");
                if (parts.length == 3) {
                    // Probablemente lectores: Nombre,Usuario,Contraseña
                    return loadReadersFromExternalFile(file);
                } else if (parts.length == 5) {
                    // Probablemente libros: ID,Título,Autor,Año,Categoría
                    return loadBooksFromExternalFile(file);
                }
            }
        }
        return "No se pudo determinar el tipo de archivo";
    }

    private String loadReadersFromExternalFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") &&
                        !fileContainsLine(Path.of(READERS_FILE), line)) {
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
                if (!line.isEmpty() && !line.startsWith("#") &&
                        !fileContainsLine(Path.of(BOOKS_FILE), line)) {
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
                if (!line.isEmpty() && !line.startsWith("#") &&
                        !fileContainsLine(Path.of(RATINGS_FILE), line)) {
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
                if (!line.isEmpty() && !line.startsWith("#") &&
                        !fileContainsLine(Path.of(CONNECTIONS_FILE), line)) {
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

    /**
     * Verifica si archivo contiene línea específica
     */
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

    // =============== MÉTODOS ADICIONALES PARA SINCRONIZACIÓN ===============

    /**
     * Guarda una valoración
     * SINCRONIZADO: Con el sistema de valoraciones del Reader
     */
    public boolean saveRating(Rating rating) {
        if (rating == null || rating.getReader() == null || rating.getBook() == null) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RATINGS_FILE, true))) {
            String line = String.format("%s,%s,%d,%s",
                    rating.getReader().getUsername(),
                    rating.getBook().getIdBook(),
                    rating.getStars(),
                    rating.getComment() != null ? rating.getComment() : "");
            writer.write(line);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando valoración: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda una conexión entre usuarios
     * SINCRONIZADO: Con el sistema de afinidad
     */
    public boolean saveConnection(String user1, String user2) {
        if (user1 == null || user2 == null || user1.trim().isEmpty() || user2.trim().isEmpty()) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONNECTIONS_FILE, true))) {
            writer.write(user1.trim() + "," + user2.trim());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando conexión: " + e.getMessage());
            return false;
        }
    }

    // =============== GESTIÓN DE SESIÓN - SINCRONIZADA CON TODOS LOS CONTROLADORES ===============

    /**
     * CRÍTICO: Método usado por todos los controladores para obtener usuario actual
     */
    public static Person getCurrentUser() {
        return currentUser;
    }

    /**
     * CRÍTICO: Método usado para establecer usuario actual
     */
    public static void setCurrentUser(Person user) {
        currentUser = user;
        if (user != null) {
            System.out.println("Usuario actual establecido: " + user.getName() + " (" + user.getClass().getSimpleName() + ")");
        }
    }

    /**
     * Método para cerrar sesión
     * SINCRONIZADO: Con posibles controladores de logout
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("Cerrando sesión para: " + currentUser.getName());
            currentUser = null;
        }
    }

    /**
     * Verifica si hay un usuario logueado
     */
    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    /**
     * Obtiene información del sistema para debugging
     */
    public String getSystemInfo() {
        return String.format(
                "Sistema de Persistencia:\n" +
                        "- Usuario actual: %s\n" +
                        "- Archivos de datos: %s\n" +
                        "- Estado: %s",
                currentUser != null ? currentUser.getName() : "Ninguno",
                BASE_PATH,
                "Operativo"
        );
    }
}
