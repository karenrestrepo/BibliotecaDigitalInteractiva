package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.ObservableList;

public class Library {
    Persistence persistence;

    private HashMap<String, Book> books = new HashMap<>();

    LinkedList<Reader> readersList = new LinkedList<>();
    LinkedList<Book> bookssList = new LinkedList<>();
    LinkedList<Administrator> administrators = new LinkedList<>();


    public Library() {
        this.persistence = new Persistence();
        for (Reader reader : persistence.getAllReaders()) {
            reader.setLibrary(this);
        }
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
        persistence.saveReaders(persistence.getAllReaders());
        return true;
    }

    public boolean updateReader(String username, String newName, String newPassword) {
        return persistence.updateReader(username, newName, newPassword); // implementado en Persistence
    }

    private Book getBookById(String id) {
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
