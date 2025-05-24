package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Persistence {
    private static Person currentUser;

    // CONFIGURACIÓN DE RUTAS - Problema original identificado
    private static final String RESOURCES_PACKAGE = "Archivos/";
    private static final String DEV_BASE_PATH = "co.edu.uniquindio.BibliotecaDigital/src/main/resources/Archivos/";

    // Rutas específicas para cada tipo de archivo
    private static final String READERS_PATH = "Readers/Readers.txt";
    private static final String ADMINS_PATH = "Administrators/Administrators.txt";
    private static final String BOOKS_PATH = "Books/Books.txt";
    private static final String RATINGS_PATH = "Ratings/Ratings.txt";
    private static final String CONNECTIONS_PATH = "Connections/Connections.txt";

    public Persistence() {
        System.out.println("🔄 Inicializando sistema de persistencia...");
        initializeAndVerifyFiles();
    }

    /**
     * MÉTODO CENTRAL: Verifica que todos los archivos sean accesibles
     * Este método aplica el principio de "fail fast" - detectar problemas temprano
     */
    private void initializeAndVerifyFiles() {
        System.out.println("📂 Verificando archivos de datos...");

        // Verificar cada archivo crítico
        verifyFile("Administradores", ADMINS_PATH);
        verifyFile("Lectores", READERS_PATH);
        verifyFile("Libros", BOOKS_PATH);
        verifyFile("Valoraciones", RATINGS_PATH);
        verifyFile("Conexiones", CONNECTIONS_PATH);

        // Crear datos por defecto si es necesario
        ensureMinimalData();
    }

    /**
     * MÉTODO PEDAGÓGICO: Verifica si un archivo específico es accesible
     * Demuestra manejo de recursos en aplicaciones Java
     */
    private void verifyFile(String description, String relativePath) {
        boolean foundInClasspath = canReadFromClasspath(relativePath);
        boolean foundInFilesystem = canReadFromFilesystem(relativePath);

        System.out.println(String.format("📄 %s - Classpath: %s | Filesystem: %s",
                description,
                foundInClasspath ? "✅" : "❌",
                foundInFilesystem ? "✅" : "❌"));
    }

    /**
     * CONCEPTO CLAVE: Estrategia dual de lectura
     * En desarrollo usamos filesystem, en producción usamos classpath
     */
    private boolean canReadFromClasspath(String relativePath) {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(RESOURCES_PACKAGE + relativePath)) {
            return stream != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canReadFromFilesystem(String relativePath) {
        Path path = Paths.get(DEV_BASE_PATH + relativePath);
        return Files.exists(path) && Files.isReadable(path);
    }

    /**
     * MÉTODO GENÉRICO MEJORADO: Lee cualquier archivo usando estrategia dual
     * Este patrón es reutilizable para todos los tipos de archivo
     */
    private BufferedReader getFileReader(String relativePath) throws IOException {
        // ESTRATEGIA 1: Intentar leer desde classpath (para JAR/producción)
        InputStream classPathStream = getClass().getClassLoader()
                .getResourceAsStream(RESOURCES_PACKAGE + relativePath);

        if (classPathStream != null) {
            System.out.println("📖 Leyendo " + relativePath + " desde classpath");
            return new BufferedReader(new InputStreamReader(classPathStream));
        }

        // ESTRATEGIA 2: Leer desde filesystem (para desarrollo en IntelliJ)
        Path filesystemPath = Paths.get(DEV_BASE_PATH + relativePath);
        if (Files.exists(filesystemPath)) {
            System.out.println("📖 Leyendo " + relativePath + " desde filesystem");
            return Files.newBufferedReader(filesystemPath);
        }

        throw new IOException("No se puede encontrar " + relativePath + " en classpath ni filesystem");
    }

    public HashMap<String, Administrator> loadAdministrators() {
        HashMap<String, Administrator> admins = new HashMap<>();

        try (BufferedReader reader = getFileReader(ADMINS_PATH)) {
            String line;
            int lineNumber = 0;
            int validLinesProcessed = 0;

            System.out.println("🔍 INICIANDO LECTURA DETALLADA DE ADMINISTRADORES:");

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                System.out.println("📝 Línea " + lineNumber + " RAW: [" + line + "] (longitud: " + line.length() + ")");

                // Mostrar cada caracter para detectar caracteres ocultos
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c < 32 || c > 126) { // Caracteres no imprimibles
                        System.out.println("   ⚠️ Caracter no imprimible en posición " + i + ": " + (int)c);
                    }
                }

                line = line.trim();
                System.out.println("📝 Línea " + lineNumber + " TRIM: [" + line + "] (longitud: " + line.length() + ")");

                // Ignorar líneas vacías y comentarios
                if (line.isEmpty()) {
                    System.out.println("   ⏭️ Línea vacía, saltando");
                    continue;
                }

                if (line.startsWith("#")) {
                    System.out.println("   ⏭️ Comentario, saltando");
                    continue;
                }

                try {
                    String[] parts = line.split(",");
                    System.out.println("   🔧 Partes encontradas: " + parts.length);
                    for (int i = 0; i < parts.length; i++) {
                        System.out.println("      Parte[" + i + "]: [" + parts[i] + "] (longitud: " + parts[i].length() + ")");
                    }

                    if (parts.length >= 3) {
                        String name = parts[0].trim();
                        String username = parts[1].trim();
                        String password = parts[2].trim();

                        System.out.println("   ✂️ Después del trim:");
                        System.out.println("      name: [" + name + "] (longitud: " + name.length() + ", vacío: " + name.isEmpty() + ")");
                        System.out.println("      username: [" + username + "] (longitud: " + username.length() + ", vacío: " + username.isEmpty() + ")");
                        System.out.println("      password: [" + password + "] (longitud: " + password.length() + ", vacío: " + password.isEmpty() + ")");

                        // Validación de datos
                        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                            System.err.println("   ❌ CAMPOS VACÍOS detectados en línea " + lineNumber);
                            System.err.println("      name.isEmpty(): " + name.isEmpty());
                            System.err.println("      username.isEmpty(): " + username.isEmpty());
                            System.err.println("      password.isEmpty(): " + password.isEmpty());
                            continue;
                        }

                        Administrator admin = new Administrator(name, username, password);
                        admins.put(username, admin);
                        validLinesProcessed++;

                        System.out.println("   🎉 ÉXITO: Admin creado - " + name + " (" + username + ")");

                    } else {
                        System.err.println("   ❌ FORMATO INCORRECTO en línea " + lineNumber + ": esperaba 3 partes, encontró " + parts.length);
                    }

                } catch (Exception e) {
                    System.err.println("   💥 EXCEPCIÓN procesando línea " + lineNumber + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("🏁 RESUMEN DE LECTURA:");
            System.out.println("   📊 Total líneas leídas: " + lineNumber);
            System.out.println("   ✅ Líneas válidas procesadas: " + validLinesProcessed);
            System.out.println("   👥 Administradores en HashMap: " + admins.size());

            // IMPORTANTE: Solo crear datos por defecto si NO se cargó NADA
            if (admins.size() == 0) {
                System.out.println("⚠️ CERO administradores cargados del archivo, creando datos por defecto");
                createDefaultAdministrators(admins);
            } else {
                System.out.println("✅ Administradores cargados exitosamente del archivo, NO creando datos por defecto");
            }

        } catch (IOException e) {
            System.err.println("❌ EXCEPCIÓN DE IO leyendo administradores: " + e.getMessage());
            e.printStackTrace();
            createDefaultAdministrators(admins);
        }

        System.out.println("🎯 RESULTADO FINAL: " + admins.size() + " administradores cargados");

        // Debug: mostrar todos los administradores cargados
        if (admins.size() > 0) {
            System.out.println("📋 LISTA FINAL DE ADMINISTRADORES:");
            LinkedList<String> keys = admins.keySet();
            for (int i = 0; i < keys.getSize(); i++) {
                String key = keys.getAmountNodo(i);
                Administrator admin = admins.get(key);
                System.out.println("   " + (i+1) + ". " + admin.getName() + " (" + key + ") [pass: " + admin.getPassword() + "]");
            }
        }

        return admins;
    }

    /**
     * CARGA DE LECTORES CORREGIDA
     * Mismo patrón que administradores pero para lectores
     */
    public HashMap<String, Reader> loadReaders() {
        HashMap<String, Reader> readers = new HashMap<>();

        try (BufferedReader reader = getFileReader(READERS_PATH)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    String[] parts = line.split(",");

                    if (parts.length >= 3) {
                        String name = parts[0].trim();
                        String username = parts[1].trim();
                        String password = parts[2].trim();

                        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                            System.err.println("⚠️ Lector línea " + lineNumber + " tiene campos vacíos");
                            continue;
                        }

                        Reader readerObj = new Reader(name, username, password);
                        // IMPORTANTE: Establecer referencia a la biblioteca
                        readerObj.setLibrary(Library.getInstance());
                        readers.put(username, readerObj);

                        System.out.println("📚 Lector cargado: " + name + " (" + username + ")");

                    } else {
                        System.err.println("⚠️ Lector línea " + lineNumber + " formato incorrecto: " + line);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error en lector línea " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Error leyendo lectores: " + e.getMessage());
            createDefaultReaders(readers);
        }

        System.out.println("✅ Total lectores cargados: " + readers.size());
        return readers;
    }

    /**
     * CARGA DE LIBROS CORREGIDA
     * Manejo específico para datos de libros con validación de año
     */
    public HashMap<String, Book> loadBooks() {
        HashMap<String, Book> books = new HashMap<>();

        try (BufferedReader reader = getFileReader(BOOKS_PATH)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    String[] parts = line.split(",");

                    if (parts.length >= 5) {
                        String id = parts[0].trim();
                        String title = parts[1].trim();
                        String author = parts[2].trim();
                        String yearStr = parts[3].trim();
                        String category = parts[4].trim();

                        // Validación específica para libros
                        if (id.isEmpty() || title.isEmpty() || author.isEmpty() || category.isEmpty()) {
                            System.err.println("⚠️ Libro línea " + lineNumber + " tiene campos vacíos");
                            continue;
                        }

                        // Validación del año
                        int year;
                        try {
                            year = Integer.parseInt(yearStr);
                        } catch (NumberFormatException e) {
                            System.err.println("⚠️ Libro línea " + lineNumber + " año inválido: " + yearStr);
                            continue;
                        }

                        Book book = new Book(id, title, author, year, category);
                        books.put(id, book);

                        System.out.println("📖 Libro cargado: " + title + " (" + id + ")");

                    } else {
                        System.err.println("⚠️ Libro línea " + lineNumber + " formato incorrecto: " + line);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error en libro línea " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Error leyendo libros: " + e.getMessage());
            createDefaultBooks(books);
        }

        System.out.println("✅ Total libros cargados: " + books.size());
        return books;
    }

    /**
     * SISTEMA DE AUTENTICACIÓN MEJORADO
     * Con debug detallado y manejo robusto de casos edge
     */
    public Person login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            System.out.println("❌ Login fallido: credenciales vacías");
            return null;
        }

        String cleanUsername = username.trim();
        String cleanPassword = password.trim();

        System.out.println("🔐 Intentando login: " + cleanUsername);

        // Intentar como administrador primero
        HashMap<String, Administrator> administrators = loadAdministrators();
        System.out.println("📊 Administradores disponibles: " + administrators.size());

        Administrator admin = administrators.get(cleanUsername);
        if (admin != null) {
            System.out.println("✅ Administrador encontrado: " + admin.getName());

            if (admin.getPassword().equals(cleanPassword)) {
                currentUser = admin;
                System.out.println("🎉 Login exitoso como administrador");
                return admin;
            } else {
                System.out.println("❌ Contraseña incorrecta para administrador");
            }
        }

        // Intentar como lector
        HashMap<String, Reader> readers = loadReaders();
        System.out.println("📊 Lectores disponibles: " + readers.size());

        Reader reader = readers.get(cleanUsername);
        if (reader != null) {
            System.out.println("✅ Lector encontrado: " + reader.getName());

            if (reader.getPassword().equals(cleanPassword)) {
                currentUser = reader;
                reader.setLibrary(Library.getInstance());
                System.out.println("🎉 Login exitoso como lector");
                return reader;
            } else {
                System.out.println("❌ Contraseña incorrecta para lector");
            }
        }

        System.out.println("❌ Usuario no encontrado: " + cleanUsername);
        return null;
    }

    /**
     * MÉTODOS DE ESCRITURA MEJORADOS
     * Solo escriben en filesystem (desarrollo), no en classpath
     */
    public boolean saveReader(Reader reader) {
        if (reader == null || reader.getUsername() == null || reader.getUsername().trim().isEmpty()) {
            return false;
        }

        try {
            // Crear directorio si no existe
            Path dirPath = Paths.get(DEV_BASE_PATH + "Readers");
            Files.createDirectories(dirPath);

            Path filePath = Paths.get(DEV_BASE_PATH + READERS_PATH);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND)) {

                String line = String.format("%s,%s,%s",
                        reader.getName(),
                        reader.getUsername(),
                        reader.getPassword());
                writer.write(line);
                writer.newLine();

                System.out.println("💾 Lector guardado: " + reader.getUsername());
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Error guardando lector: " + e.getMessage());
            return false;
        }
    }

    public boolean saveBook(Book book) {
        if (book == null || book.getIdBook() == null || book.getIdBook().trim().isEmpty()) {
            return false;
        }

        try {
            Path dirPath = Paths.get(DEV_BASE_PATH + "Books");
            Files.createDirectories(dirPath);

            Path filePath = Paths.get(DEV_BASE_PATH + BOOKS_PATH);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND)) {

                String line = String.format("%s,%s,%s,%d,%s",
                        book.getIdBook(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getYear(),
                        book.getCategory());
                writer.write(line);
                writer.newLine();

                System.out.println("💾 Libro guardado: " + book.getTitle());
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Error guardando libro: " + e.getMessage());
            return false;
        }
    }

    /**
     * OPERACIONES CRUD COMPLETAS
     */
    public boolean saveAllBooks(HashMap<String, Book> books) {
        try {
            Path dirPath = Paths.get(DEV_BASE_PATH + "Books");
            Files.createDirectories(dirPath);

            Path filePath = Paths.get(DEV_BASE_PATH + BOOKS_PATH);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
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

                System.out.println("💾 Guardados " + keys.getSize() + " libros");
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Error guardando todos los libros: " + e.getMessage());
            return false;
        }
    }

    public boolean updateReader(String username, String newName, String newPassword) {
        HashMap<String, Reader> readers = loadReaders();
        if (!readers.containsKey(username.trim())) {
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
            return false;
        }

        readers.remove(username.trim());
        return saveAllReaders(readers);
    }

    private boolean saveAllReaders(HashMap<String, Reader> readers) {
        try {
            Path dirPath = Paths.get(DEV_BASE_PATH + "Readers");
            Files.createDirectories(dirPath);

            Path filePath = Paths.get(DEV_BASE_PATH + READERS_PATH);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
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

                System.out.println("💾 Guardados " + keys.getSize() + " lectores");
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Error guardando todos los lectores: " + e.getMessage());
            return false;
        }
    }

    /**
     * MÉTODOS PARA DATOS POR DEFECTO
     * Se ejecutan cuando no se pueden cargar los archivos
     */
    private void ensureMinimalData() {
        // Verificar administradores
        HashMap<String, Administrator> admins = loadAdministrators();
        if (admins.size() == 0) {
            System.out.println("🏗️ Creando administradores por defecto...");
            createDefaultAdministrators(admins);
        }

        // Verificar libros
        HashMap<String, Book> books = loadBooks();
        if (books.size() == 0) {
            System.out.println("🏗️ Creando libros por defecto...");
            createDefaultBooks(books);
        }
    }

    private void createDefaultAdministrators(HashMap<String, Administrator> admins) {
        Administrator admin1 = new Administrator("Manuela Aristizabal", "administrador1@gmail.com", "1234");
        Administrator admin2 = new Administrator("Karen Restrepo", "administrador2@gmail.com", "4321");

        admins.put("administrador1@gmail.com", admin1);
        admins.put("administrador2@gmail.com", admin2);

        System.out.println("✅ Administradores por defecto disponibles:");
        System.out.println("   - administrador1@gmail.com / 1234");
        System.out.println("   - administrador2@gmail.com / 4321");
    }

    private void createDefaultReaders(HashMap<String, Reader> readers) {
        Reader reader1 = new Reader("Esteban", "esteban@gmail.com", "123");
        Reader reader2 = new Reader("Ana", "ana@gmail.com", "Ana123");

        reader1.setLibrary(Library.getInstance());
        reader2.setLibrary(Library.getInstance());

        readers.put("esteban@gmail.com", reader1);
        readers.put("ana@gmail.com", reader2);

        System.out.println("✅ Lectores por defecto disponibles:");
        System.out.println("   - esteban@gmail.com / 123");
        System.out.println("   - ana@gmail.com / Ana123");
    }

    private void createDefaultBooks(HashMap<String, Book> books) {
        Book book1 = new Book("02", "Rosario Tijeras", "Jorge Franco Ramos", 2010, "Urbana");
        Book book2 = new Book("03", "Orgullo y Prejuicio", "Jane Austen", 1813, "Romance Epoca");

        books.put("02", book1);
        books.put("03", book2);

        System.out.println("✅ Libros por defecto disponibles:");
        System.out.println("   - 02: Rosario Tijeras");
        System.out.println("   - 03: Orgullo y Prejuicio");
    }

    /**
     * CARGA DE DATOS EXTERNOS (para el LoadDataController)
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
            } else {
                return detectAndLoadFromContent(file);
            }
        } catch (Exception e) {
            return "Error al cargar archivo: " + e.getMessage();
        }
    }

    private String loadReadersFromExternalFile(File file) throws IOException {
        int count = 0;
        Path targetFile = Paths.get(DEV_BASE_PATH + READERS_PATH);

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = Files.newBufferedWriter(targetFile,
                     java.nio.file.StandardOpenOption.CREATE,
                     java.nio.file.StandardOpenOption.APPEND)) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    writer.write(line);
                    writer.newLine();
                    count++;
                }
            }
        }
        return "Se cargaron " + count + " lectores nuevos desde " + file.getName();
    }

    private String loadBooksFromExternalFile(File file) throws IOException {
        int count = 0;
        Path targetFile = Paths.get(DEV_BASE_PATH + BOOKS_PATH);

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = Files.newBufferedWriter(targetFile,
                     java.nio.file.StandardOpenOption.CREATE,
                     java.nio.file.StandardOpenOption.APPEND)) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    writer.write(line);
                    writer.newLine();
                    count++;
                }
            }
        }
        return "Se cargaron " + count + " libros nuevos desde " + file.getName();
    }

    private String detectAndLoadFromContent(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null) {
                String[] parts = firstLine.split(",");
                if (parts.length == 3) {
                    return loadReadersFromExternalFile(file);
                } else if (parts.length >= 5) {
                    return loadBooksFromExternalFile(file);
                }
            }
        }
        return "No se pudo determinar el tipo de archivo";
    }

    /**
     * MÉTODOS ADICIONALES PARA FUNCIONALIDADES ESPECÍFICAS
     */
    public boolean saveRating(Rating rating) {
        if (rating == null || rating.getReader() == null || rating.getBook() == null) {
            return false;
        }

        try {
            Path dirPath = Paths.get(DEV_BASE_PATH + "Ratings");
            Files.createDirectories(dirPath);

            Path filePath = Paths.get(DEV_BASE_PATH + RATINGS_PATH);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND)) {

                String line = String.format("%s,%s,%d,%s",
                        rating.getReader().getUsername(),
                        rating.getBook().getIdBook(),
                        rating.getStars(),
                        rating.getComment() != null ? rating.getComment() : "");
                writer.write(line);
                writer.newLine();

                System.out.println("💾 Valoración guardada");
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Error guardando valoración: " + e.getMessage());
            return false;
        }
    }

    public boolean saveConnection(String user1, String user2) {
        if (user1 == null || user2 == null || user1.trim().isEmpty() || user2.trim().isEmpty()) {
            return false;
        }

        try {
            Path dirPath = Paths.get(DEV_BASE_PATH + "Connections");
            Files.createDirectories(dirPath);

            Path filePath = Paths.get(DEV_BASE_PATH + CONNECTIONS_PATH);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND)) {

                writer.write(user1.trim() + "," + user2.trim());
                writer.newLine();

                System.out.println("💾 Conexión guardada: " + user1 + " <-> " + user2);
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Error guardando conexión: " + e.getMessage());
            return false;
        }
    }

    /**
     * GESTIÓN DE SESIÓN - Métodos estáticos para el usuario actual
     */
    public static Person getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Person user) {
        currentUser = user;
        if (user != null) {
            System.out.println("👤 Usuario actual establecido: " + user.getName() +
                    " (" + user.getClass().getSimpleName() + ")");
        }
    }

    public static void logout() {
        if (currentUser != null) {
            System.out.println("👋 Cerrando sesión para: " + currentUser.getName());
            currentUser = null;
        }
    }

    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    /**
     * MÉTODO DE DIAGNÓSTICO - Útil para debugging
     */
    public String getSystemInfo() {
        return String.format(
                "Sistema de Persistencia - Estado del Sistema:\n" +
                        "- Usuario actual: %s\n" +
                        "- Archivos accesibles desde classpath: %s\n" +
                        "- Archivos accesibles desde filesystem: %s\n" +
                        "- Modo de operación: %s",
                currentUser != null ? currentUser.getName() + " (" + currentUser.getClass().getSimpleName() + ")" : "Ninguno",
                canReadFromClasspath(ADMINS_PATH) ? "Sí" : "No",
                canReadFromFilesystem(ADMINS_PATH) ? "Sí" : "No",
                canReadFromClasspath(ADMINS_PATH) ? "Producción (JAR)" : "Desarrollo (IntelliJ)"
        );
    }
}