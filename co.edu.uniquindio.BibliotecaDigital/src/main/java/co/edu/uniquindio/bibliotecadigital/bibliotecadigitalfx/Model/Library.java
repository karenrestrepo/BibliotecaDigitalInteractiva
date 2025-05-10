package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.ObservableList;

public class Library {
    Persistence persistence = new Persistence();

    private HashMap<String, Book> books = new HashMap<>();

    LinkedList<Reader> readersList = new LinkedList<>();
    LinkedList<Book> bookssList = new LinkedList<>();
    LinkedList<Administrator> administrators = new LinkedList<>();


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
}
