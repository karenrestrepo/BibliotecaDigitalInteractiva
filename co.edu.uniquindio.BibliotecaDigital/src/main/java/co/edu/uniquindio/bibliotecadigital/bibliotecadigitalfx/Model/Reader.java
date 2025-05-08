package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.util.List;

public class Reader extends Person {
    private LinkedList<Book> loanHistoryList;
    private LinkedList<Rating> ratingsList;
    public Reader() {

    }

    public Reader(String name, String username, String password) {
        super(name, username, password);
        this.loanHistoryList = new LinkedList<>();
        this.ratingsList = new LinkedList<>();
    }

    public static Book getBookByTittle(String title, Library library) {
        for (int i = 0; i < library.getBookssList().getSize(); i++) {
            Book book = library.getBookssList().getAmountNodo(i);
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        throw new RuntimeException("No se encontró el libro con título: " + title);
    }
    public static Book getBookByAutor(String autor, Library library) {
        LinkedList<Book> books = new LinkedList<>();
        for (int i = 0; i < library.getBookssList().getSize(); i++) {
            Book book = library.getBookssList().getAmountNodo(i);
            if (book.getTitle().equalsIgnoreCase(autor)) {
                books.add(book);
            }
        }
        throw new RuntimeException("No se encontró el libro con autor: " + autor);
    }
    public static Book getBookByYear(String year, Library library) {
        LinkedList<Book> books = new LinkedList<>();
        for (int i = 0; i < library.getBookssList().getSize(); i++) {
            Book book = library.getBookssList().getAmountNodo(i);
            if (book.getTitle().equalsIgnoreCase(year)) {
                return book;
            }
        }
        throw new RuntimeException("No se encontró el libro con año: " + year);
    }
// metodo prestar libro
    public static void lendBook(Book book, Object user) {

    }
}
