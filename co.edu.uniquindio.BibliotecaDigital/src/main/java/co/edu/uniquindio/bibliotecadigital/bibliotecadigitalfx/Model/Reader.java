package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Reader extends Person {
    private LinkedList<Book> loanHistoryList;
    private LinkedList<Rating> ratingsList;
    private Library library;

    public Reader(String name, String username, String password, Library library) {
        super(name, username, password);
        this.loanHistoryList = new LinkedList<>();
        this.ratingsList = new LinkedList<>();
        this.library = library;  }

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

    public List<Book> getBooksByAuthor(String author, Library library) {
        List<Book> results = new ArrayList<>();
        for (Book book : library.getBooks().values()) {
            if (book.getAuthor().equalsIgnoreCase(author)) {
                results.add(book);
            }
        }
        return results;
    }

    public Book getBookByYear(String year) {
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
    public void lendBook(Book book) {
        if (book.getStatus() == BookStatus.AVAILABLE) {
            book.setStatus(BookStatus.CHECKED_OUT);
         this.loanHistoryList.add(book);
        } else {
            throw new RuntimeException("El libro no está disponible.");
        }
    }

    //Devolver libros.
    public void returnBook(Book book) {
        for (int i = 0; i < loanHistoryList.getSize(); i++) {
            if (loanHistoryList.getAmountNodo(i).equals(book)) {
                book.setStatus(BookStatus.AVAILABLE); // Devuelve disponibilidad
                return;
            }
        }
        throw new RuntimeException("El lector no tiene este libro en su historial.");
    }

    //Valorar libros.
    public void rateBook(Reader reader, Book book, int stars, String comment) {
        Rating rating = new Rating(reader, book, stars, comment);
        ratingsList.add(rating);

        // Solo se añade el número de estrellas al libro (no el comentario)
        book.addRating(stars);
    }

    //Consultar recomendaciones de libros según valoraciones propias.
    public List<Book> getRecommendations() {
        LinkedList<Book> recommendations = new LinkedList<>();
        for (int i = 0; i < ratingsList.getSize(); i++) {
            Rating rating = ratingsList.getAmountNodo(i);
            if (rating.getStars() >= 4) {
                Book ratedBook = rating.getBook();
                for (int j = 0; j < loanHistoryList.getSize(); j++) {
                    Book otherBook = loanHistoryList.getAmountNodo(j);
                    if (!otherBook.equals(ratedBook) &&
                            (otherBook.getAuthor().equalsIgnoreCase(ratedBook.getAuthor()) ||
                                    otherBook.getCategory().equalsIgnoreCase(ratedBook.getCategory()))) {
                        recommendations.add(otherBook);
                    }
                }
            }
        }
        return recommendations.stream().toList();
    }

    //Ver sugerencias de lectores con gustos similares.
    public List<Reader> getSuggestions(LibrarySystem library) {
        Graph<Reader> graph = library.getAffinityGraph();
        Set<Reader> connections = graph.getAdjacentVertices(this);  // ✅ corregido

        List<Reader> similarReaders = new ArrayList<>();
        for (Person person : connections) {
            if (person instanceof Reader reader) {
                similarReaders.add(reader);
            }
        }
        return similarReaders;
    }

    //Enviar mensajes a lectores conectados.
    public void sendMessage(Reader recipient, String message) {
        System.out.println("Mensaje de " + getUsername() + " para " + recipient.getUsername() + ": " + message);
    }

    public LinkedList<Book> getLoanHistoryList() {
        return loanHistoryList;
    }

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }
}
