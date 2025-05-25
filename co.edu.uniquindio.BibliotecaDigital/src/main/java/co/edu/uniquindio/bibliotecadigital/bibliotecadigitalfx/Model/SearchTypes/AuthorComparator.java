package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.SearchTypes;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;

import java.util.Comparator;

public class AuthorComparator implements Comparator<Book> {

    @Override
    public int compare(Book a, Book b) {
        return a.getAuthor().compareToIgnoreCase(b.getAuthor());
    }
}
