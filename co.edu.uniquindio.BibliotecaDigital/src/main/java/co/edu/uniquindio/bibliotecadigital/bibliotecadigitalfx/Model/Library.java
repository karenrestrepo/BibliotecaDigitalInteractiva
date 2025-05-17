package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;

public class Library {
    Persistence persistence;
    private static Library instance;

    private HashMap<String, Book> books = new HashMap<>();

    LinkedList<Reader> readersList = new LinkedList<>();
    LinkedList<Book> bookssList = new LinkedList<>();
    LinkedList<Administrator> administrators = new LinkedList<>();
    private HashMap<String, Rating> ratings = new HashMap<>();
    private Graph<String> readerConnections = new Graph<>();
    public Library() {
        // Constructor sin excepciones
    }

    private void initializePersistence() throws IOException {
        if (persistence == null) {
            persistence = new Persistence();
            // Resto de inicialización
        }
    }

    // Los métodos que usen persistence deberían verificar primero
    public void someMethod() {
        try {
            initializePersistence();
            // Operaciones con persistence
        } catch (IOException e) {
            // Manejar error
        }
    }

    public static synchronized Library getInstance() throws IOException {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    public Library(Persistence persistence) {
        this.persistence = persistence;
    }

    public Book  createBook(String id, String title, String author, int year, String category){
        Book newbook = null;
        verifyBookDoesNotExist(id);

            newbook = new Book();
            newbook.setIdBook(id);
            newbook.setTitle(title);
            newbook.setAuthor(author);
            newbook.setYear(year);
            newbook.setCategory(category);
            books.put(id, newbook);
            persistence.saveBookToFile(newbook);

            return newbook;
    }

    public boolean removeBook(String id) {
        Book book = getBookById(id);
        if (book == null) {
            throw new RuntimeException("The book to delete does not exist");
        }
        books.remove(id);
        persistence.saveAllBooks();

        return true;
    }

    public boolean registerReader(String name, String username, String password) {
        if (persistence.getReaders().get(username) == null) {
            Reader reader = new Reader(name, username, password);
            persistence.saveReaderToFile(reader); // guardamos en el archivo
            persistence.getReaders().put(username, reader); // guardamos en el HashMap
            readersList.addEnd(reader); // agregamos a la lista en memoria
            return true;
        }
        return false;
    }
    public boolean deleteReader(String username) {
        if (!persistence.getReaders().containsKey(username)) {
            return false;
        }

        // Elimina el lector
        persistence.getReaders().remove(username);
        readersList.delete(getReaderByUsername(username));
        persistence.saveReaders(persistence.getAllReaders());
        return true;
    }

    public boolean updateReader(String username, String newName, String newPassword) {
        return persistence.updateReader(username, newName, newPassword); // implementado en Persistence
    }

    public Book getBookById(String id) {
        return books.get(id);

    }

    private void verifyBookDoesNotExist(String id) {
        if (bookExists(id)) {
            throw new RuntimeException("The book with ID: " + id + " already exists");
        }
    }

    public boolean bookExists(String id) {
        return books.containsKey(id);

    }

    public LinkedList<Reader> getReadersList() {
        if (readersList.isEmpty()) {
            System.out.println("No hay lectores registrados");
        }
        return readersList;
    }

    public String loadDataFromFile(File file) {
        if (persistence == null) {
            try {
                this.persistence = new Persistence(); // Inicializa persistence si es null
            } catch (IOException e) {
                return "Error inicializando Persistence: " + e.getMessage();
            }
        }

        try {
            persistence.loadDataFromFile(file);
            persistence.saveReaders(persistence.getAllReaders());
            persistence.saveAllBooks();

            refreshInternalData();
            return "Datos cargados exitosamente desde: " + file.getName();
        } catch (IOException e) {
            return "Error al cargar datos: " + e.getMessage();
        }
    }
    private void refreshInternalData() throws IOException {
        // Limpiar estructuras existentes
        readersList.clear();
        books.clear();

        // Recargar datos desde persistencia
        for (Reader reader : persistence.getAllReaders()) {
            reader.setLibrary(this);
            readersList.addEnd(reader);
        }

        for (Book book : persistence.getBooks().values()) {
            books.put(book.getIdBook(), book);
        }
    }

    public Reader getReaderByUsername(String username) {
        for (int i = 0; i < readersList.getSize(); i++) {
            Reader reader = readersList.getAmountNodo(i);
            if (reader.getUsername().equals(username)) {
                return reader;
            }
        }
        return null;
    }


    public void addRating(Rating rating) {
        if (rating == null || rating.getReader() == null || rating.getBook() == null) {
            throw new IllegalArgumentException("Rating inválido");
        }

        String key = rating.getReader().getUsername() + "|" + rating.getBook().getIdBook();
        if (!ratings.containsKey(key)) {
            ratings.put(key, rating);
            persistence.saveRatingToFile(rating);
        }
    }


    public void addConnection(Reader reader1, Reader reader2) {
        if (getReaderByUsername(reader1.getUsername()) != null &&
                getReaderByUsername(reader2.getUsername()) != null) {
            readerConnections.addEdge(reader1.getUsername(), reader2.getUsername());
            persistence.saveConnectionToFile(reader1.getUsername(), reader2.getUsername());
        }
    }



    public void setReadersList(LinkedList<Reader> readersList) {
        this.readersList = readersList;
    }

    public LinkedList<Book> getBookssList() {
        return bookssList;
    }

    public void setBookssList(LinkedList<Book> bookssList) {
        this.bookssList = bookssList;
    }

    public LinkedList<Administrator> getAdministrators() {
        return administrators;
    }

    public HashMap<String, Book> getBooks() {
        return books;
    }
    public void setAdministrators(LinkedList<Administrator> administrators) {
        this.administrators = administrators;
    }


    public LinkedList<Reader> getReaders() {
        return persistence.getAllReaders();
    }
}
